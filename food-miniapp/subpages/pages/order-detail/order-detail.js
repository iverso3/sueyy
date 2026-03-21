// pages/order-detail/order-detail.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    order: null,
    loading: true,
    isAdmin: false,
    canDelete: false,
    hasAllReviewed: false,
    // 评价相关
    showReviewPanel: false,
    reviewData: {}
  },

  /**
   * 打开评价面板
   */
  openReviewPanel() {
    const reviewData = {};
    // 初始化每个菜品的评价数据
    this.data.order.items.forEach(item => {
      reviewData[item.id] = {
        rating: 5,
        comment: ''
      };
    });
    this.setData({
      showReviewPanel: true,
      reviewData: reviewData
    });
  },

  /**
   * 关闭评价面板
   */
  closeReviewPanel() {
    this.setData({
      showReviewPanel: false
    });
  },

  /**
   * 设置评分
   */
  setRating(e) {
    const itemId = e.currentTarget.dataset.itemid;
    const rating = e.currentTarget.dataset.index + 1;

    const reviewData = this.data.reviewData;
    reviewData[itemId].rating = rating;

    // 更新对应item的显示
    const items = this.data.order.items.map(item => {
      if (item.id === itemId) {
        return { ...item, rating: rating };
      }
      return item;
    });

    this.setData({
      reviewData: reviewData,
      'order.items': items
    });
  },

  /**
   * 输入评价
   */
  onReviewInput(e) {
    const itemId = e.currentTarget.dataset.itemid;
    const comment = e.detail.value;

    const reviewData = this.data.reviewData;
    reviewData[itemId].comment = comment;

    // 更新对应item的显示
    const items = this.data.order.items.map(item => {
      if (item.id === itemId) {
        return { ...item, comment: comment };
      }
      return item;
    });

    this.setData({
      reviewData: reviewData,
      'order.items': items
    });
  },

  /**
   * 防止冒泡
   */
  preventBubble() {
    // 空方法
  },

  /**
   * 提交评价
   */
  submitReview() {
    const app = getApp();
    const reviewData = this.data.reviewData;
    const items = this.data.order.items;

    // 检查是否所有菜品都评价了
    let hasUnrated = false;
    items.forEach(item => {
      if (!reviewData[item.id] || !reviewData[item.id].rating) {
        hasUnrated = true;
      }
    });

    if (hasUnrated) {
      wx.showToast({
        title: '请为每道菜评分',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    // 逐个提交评价
    const promises = items.map(item => {
      return app.apiRequest({
        url: '/reviews',
        method: 'POST',
        data: {
          orderItemId: item.id,
          rating: reviewData[item.id].rating,
          comment: reviewData[item.id].comment || ''
        }
      });
    });

    Promise.all(promises).then(results => {
      wx.hideLoading();
      wx.showToast({
        title: '评价成功',
        icon: 'success'
      });
      this.setData({
        showReviewPanel: false
      });
    }).catch(error => {
      wx.hideLoading();
      console.error('评价失败:', error);
      wx.showToast({
        title: '评价失败，请重试',
        icon: 'none'
      });
    });
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      this.loadOrderDetail(options.id);
    } else {
      wx.showToast({
        title: '订单不存在',
        icon: 'none'
      });
      wx.navigateBack();
    }
  },

  /**
   * 加载订单详情
   */
  loadOrderDetail(orderId) {
    const app = getApp();
    const userInfo = app.globalData.userInfo || {};
    const userId = app.globalData.userId;

    this.setData({ loading: true });

    app.apiRequest({
      url: `/orders/${orderId}`,
      method: 'GET'
    }).then(response => {
      if (response.code === 200 && response.data) {
        const order = response.data;
        // 判断是否是管理员
        const isAdmin = userInfo.role === 'ADMIN';
        // 判断是否是当天的订单
        let canDelete = false;
        if (order.createdAt) {
          const orderDate = new Date(order.createdAt);
          const today = new Date();
          canDelete = orderDate.getFullYear() === today.getFullYear() &&
                     orderDate.getMonth() === today.getMonth() &&
                     orderDate.getDate() === today.getDate();
        }
        // 判断是否是自己的订单
        const isOwnOrder = order.userId === userId;
        // 订单已完成时，普通用户不能编辑，只能管理员编辑
        const isCompleted = order.status === 'COMPLETED';
        // 普通用户：当天订单且未完成才能编辑；管理员：始终可以编辑
        const canEdit = isAdmin || (!isCompleted && canDelete && isOwnOrder);

        // 检查是否所有菜品都已评价
        let hasAllReviewed = false;
        if (order.items && order.items.length > 0) {
          hasAllReviewed = order.items.every(item => item.hasReviewed);
        }

        this.setData({
          order: order,
          isAdmin: isAdmin,
          canDelete: canEdit,
          canEdit: canEdit,
          isOwnOrder: isOwnOrder,
          hasAllReviewed: hasAllReviewed,
          loading: false
        });
      }
    }).catch(error => {
      console.error('加载订单详情失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
      this.setData({ loading: false });
    });
  },

  /**
   * 删除订单
   */
  deleteOrder() {
    const orderId = this.data.order.id;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个订单吗？',
      success: (res) => {
        if (res.confirm) {
          this.doDeleteOrder(orderId);
        }
      }
    });
  },

  doDeleteOrder(orderId) {
    const app = getApp();

    wx.showLoading({ title: '删除中...' });

    app.apiRequest({
      url: `/orders/${orderId}`,
      method: 'DELETE'
    }).then(response => {
      wx.hideLoading();
      if (response.code === 200) {
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: response.message || '删除失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('删除订单失败:', error);
      wx.showToast({
        title: '删除失败',
        icon: 'none'
      });
    });
  },

  /**
   * 删除订单中的某个菜品
   */
  deleteOrderItem(e) {
    const itemId = e.currentTarget.dataset.id;
    const orderId = this.data.order.id;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这道菜吗？',
      success: (res) => {
        if (res.confirm) {
          this.doDeleteOrderItem(orderId, itemId);
        }
      }
    });
  },

  doDeleteOrderItem(orderId, itemId) {
    const app = getApp();

    wx.showLoading({ title: '删除中...' });

    app.apiRequest({
      url: `/orders/${orderId}/items/${itemId}`,
      method: 'DELETE'
    }).then(response => {
      wx.hideLoading();
      if (response.code === 200) {
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        });
        // 刷新订单详情
        this.loadOrderDetail(orderId);
      } else {
        wx.showToast({
          title: response.message || '删除失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('删除菜品失败:', error);
      wx.showToast({
        title: '删除失败',
        icon: 'none'
      });
    });
  },

  /**
   * 修改订单状态（管理员）
   */
  changeOrderStatus() {
    const order = this.data.order;
    const currentStatus = order.status;
    const newStatus = currentStatus === 'PLACED' ? 'COMPLETED' : 'PLACED';

    wx.showModal({
      title: '确认修改',
      content: `确定要将订单状态改为"${newStatus === 'COMPLETED' ? '已制作完成' : '已下单'}"吗？`,
      success: (res) => {
        if (res.confirm) {
          this.doChangeOrderStatus(order.id, newStatus);
        }
      }
    });
  },

  doChangeOrderStatus(orderId, status) {
    const app = getApp();

    wx.showLoading({ title: '修改中...' });

    app.apiRequest({
      url: `/orders/${orderId}/status`,
      method: 'PUT',
      data: { status: status }
    }).then(response => {
      wx.hideLoading();
      if (response.code === 200) {
        wx.showToast({
          title: '修改成功',
          icon: 'success'
        });
        // 刷新订单详情
        this.loadOrderDetail(orderId);
      } else {
        wx.showToast({
          title: response.message || '修改失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('修改订单状态失败:', error);
      wx.showToast({
        title: '修改失败',
        icon: 'none'
      });
    });
  },

  /**
   * 返回首页
   */
  goToHome() {
    wx.switchTab({
      url: '/pages/menu/menu'
    });
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '在线点餐 - 订单详情',
      path: '/subpages/pages/order-detail/order-detail'
    };
  }
});
