// pages/cart/cart.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    cartItems: [],        // 想吃商品列表
    totalPrice: 0,        // 总价
    totalQuantity: 0,     // 总数量
    loading: false,
    editing: false,        // 是否编辑模式
    // 勾选功能
    selectedCount: 0,     // 选中数量
    selectedPrice: 0,     // 选中金额
    selectAll: true       // 是否全选（默认全选）
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadCartData();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 页面显示时刷新想吃数据
    this.loadCartData();
  },

  /**
   * 格式化价格，处理Java BigDecimal对象
   */
  formatPrice(price) {
    if (!price) return '0.00';
    if (typeof price === 'string') return price;
    if (typeof price === 'number') return price.toFixed(2);
    // 处理Java BigDecimal对象
    if (price.toFixed) return price.toFixed(2);
    if (price.value) return String(price.value);
    return String(price);
  },

  /**
   * 加载想吃数据
   */
  loadCartData() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    const app = getApp();

    app.apiRequest({
      url: '/cart/get',
      method: 'GET'
    }).then(response => {
      if (response.code === 200 && response.data) {
        const cartData = response.data;
        let cartItems = cartData.items || [];

        // 处理价格数据，转换为字符串
        cartItems = cartItems.map(item => ({
          ...item,
          price: this.formatPrice(item.price),
          subtotal: this.formatPrice(item.subtotal),
          selected: true
        }));

        const totalPrice = this.formatPrice(cartData.totalPrice);
        const totalQuantity = cartData.totalQuantity || 0;

        // 计算选中数量和金额
        const { selectedCount, selectedPrice } = this.calculateSelected(cartItems);

        this.setData({
          cartItems,
          totalPrice,
          totalQuantity,
          selectedCount,
          selectedPrice: selectedPrice.toFixed ? selectedPrice.toFixed(2) : String(selectedPrice),
          selectAll: cartItems.length > 0,
          loading: false
        });
      }
    }).catch(error => {
      console.error('加载购物车失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
      this.setData({ loading: false });

      // 使用模拟数据
      if (app.globalData.useMockData) {
        this.setMockCartData();
      }
    });
  },

  /**
   * 设置模拟购物车数据
   */
  setMockCartData() {
    const mockItems = [
      {
        id: 1,
        menuItemId: 1,
        name: '红烧肉',
        imageUrl: 'https://images.unsplash.com/photo-1563245372-f21724e3856d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        price: 38.00,
        quantity: 2,
        subtotal: 76.00,
        selected: true
      },
      {
        id: 2,
        menuItemId: 2,
        name: '清蒸鲈鱼',
        imageUrl: 'https://images.unsplash.com/photo-1565557623262-b51c2513a641?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        price: 58.00,
        quantity: 1,
        subtotal: 58.00,
        selected: true
      }
    ];

    const totalPrice = mockItems.reduce((sum, item) => sum + item.subtotal, 0);
    const totalQuantity = mockItems.reduce((sum, item) => sum + item.quantity, 0);
    const { selectedCount, selectedPrice } = this.calculateSelected(mockItems);

    this.setData({
      cartItems: mockItems,
      totalPrice,
      totalQuantity,
      selectedCount,
      selectedPrice,
      selectAll: true,
      loading: false
    });
  },

  /**
   * 更新商品数量
   */
  updateQuantity(e) {
    const itemId = e.currentTarget.dataset.id;
    const type = e.currentTarget.dataset.type; // 'increase' 或 'decrease'
    const cartItem = this.data.cartItems.find(item => item.id === itemId);

    if (!cartItem) return;

    let newQuantity = cartItem.quantity;
    if (type === 'increase') {
      newQuantity += 1;
    } else if (type === 'decrease') {
      if (newQuantity <= 1) {
        // 数量为1时减少则删除
        this.deleteItem(itemId);
        return;
      }
      newQuantity -= 1;
    }

    this.updateCartItem(itemId, newQuantity);
  },

  /**
   * 更新购物车商品数量
   */
  updateCartItem(itemId, quantity) {
    const app = getApp();

    app.apiRequest({
      url: `/cart/items/${itemId}?quantity=${quantity}`,
      method: 'PUT'
    }).then(response => {
      if (response.code === 200) {
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        });
        this.loadCartData();
      }
    }).catch(error => {
      console.error('更新购物车失败:', error);
      wx.showToast({
        title: '更新失败，请重试',
        icon: 'none'
      });
    });
  },

  /**
   * 删除商品
   */
  deleteItem(e) {
    const itemId = typeof e === 'number' ? e : e.currentTarget.dataset.id;

    wx.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: (res) => {
        if (res.confirm) {
          this.confirmDelete(itemId);
        }
      }
    });
  },

  /**
   * 确认删除商品
   */
  confirmDelete(itemId) {
    const app = getApp();

    app.apiRequest({
      url: `/cart/items/${itemId}`,
      method: 'DELETE'
    }).then(response => {
      if (response.code === 200) {
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        });
        this.loadCartData();
      }
    }).catch(error => {
      console.error('删除商品失败:', error);
      wx.showToast({
        title: '删除失败，请重试',
        icon: 'none'
      });
    });
  },

  /**
   * 切换编辑模式
   */
  toggleEdit() {
    this.setData({
      editing: !this.data.editing
    });
  },

  /**
   * 跳转到菜单页
   */
  goToMenu() {
    wx.switchTab({
      url: '/pages/menu/menu'
    });
  },

  /**
   * 计算选中数量和金额
   */
  calculateSelected(cartItems) {
    let selectedCount = 0;
    let selectedPrice = 0;

    cartItems.forEach(item => {
      if (item.selected) {
        selectedCount += item.quantity;
        selectedPrice += parseFloat(item.subtotal) || 0;
      }
    });

    return { selectedCount, selectedPrice };
  },

  /**
   * 勾选/取消单个菜品
   */
  toggleSelect(e) {
    const itemId = e.currentTarget.dataset.id;
    const cartItems = this.data.cartItems.map(item => {
      if (item.id === itemId) {
        return { ...item, selected: !item.selected };
      }
      return item;
    });

    const { selectedCount, selectedPrice } = this.calculateSelected(cartItems);
    const selectAll = cartItems.length > 0 && cartItems.every(item => item.selected);

    this.setData({
      cartItems,
      selectedCount,
      selectedPrice: selectedPrice.toFixed ? selectedPrice.toFixed(2) : String(selectedPrice),
      selectAll
    });
  },

  /**
   * 全选/取消全选
   */
  toggleSelectAll() {
    const selectAll = !this.data.selectAll;
    const cartItems = this.data.cartItems.map(item => ({
      ...item,
      selected: selectAll
    }));

    const { selectedCount, selectedPrice } = this.calculateSelected(cartItems);

    this.setData({
      cartItems,
      selectedCount,
      selectedPrice: selectedPrice.toFixed ? selectedPrice.toFixed(2) : String(selectedPrice),
      selectAll
    });
  },

  /**
   * 跳转到订单确认页
   */
  goToOrder() {
    if (this.data.cartItems.length === 0) {
      wx.showToast({
        title: '想吃列表为空',
        icon: 'none'
      });
      return;
    }

    if (this.data.selectedCount === 0) {
      wx.showToast({
        title: '请先选择菜品',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '确认下单',
      content: `已选${this.data.selectedCount}个菜品，共¥${this.data.selectedPrice}，是否确认下单？`,
      success: (res) => {
        if (res.confirm) {
          this.submitOrder();
        }
      }
    });
  },

  /**
   * 提交订单（直接调用后端API）
   */
  submitOrder() {
    const app = getApp();
    const selectedIds = this.data.cartItems
      .filter(item => item.selected)
      .map(item => item.id);

    if (selectedIds.length === 0) {
      wx.showToast({
        title: '请先选择菜品',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    app.apiRequest({
      url: '/orders',
      method: 'POST',
      data: {
        itemIds: selectedIds,
        deliveryTime: '尽快送达',
        remark: ''
      }
    }).then(response => {
      wx.hideLoading();
      if (response.code === 200) {
        const orderId = response.data.orderId || response.data.id;

        wx.showToast({
          title: '下单成功',
          icon: 'success'
        });

        // 跳转到订单详情页
        setTimeout(() => {
          wx.navigateTo({
            url: `/pages/order-detail/order-detail?id=${orderId}`
          });
        }, 1500);
      } else {
        throw new Error(response.message || '下单失败');
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('下单失败:', error);
      wx.showToast({
        title: error.message || '下单失败，请重试',
        icon: 'none',
        duration: 3000
      });
    });
  },

  /**
   * 清空购物车
   */
  clearCart() {
    if (this.data.cartItems.length === 0) {
      return;
    }

    wx.showModal({
      title: '提示',
      content: '确定要清空购物车吗？',
      success: (res) => {
        if (res.confirm) {
          this.confirmClearCart();
        }
      }
    });
  },

  /**
   * 确认清空购物车
   */
  confirmClearCart() {
    const app = getApp();

    // 使用后端的清空购物车接口
    app.apiRequest({
      url: '/cart',
      method: 'DELETE'
    }).then(() => {
      wx.showToast({
        title: '已清空购物车',
        icon: 'success'
      });
      this.loadCartData();
    }).catch(error => {
      console.error('清空购物车失败:', error);
      wx.showToast({
        title: '清空失败，请重试',
        icon: 'none'
      });
    });
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '在线点餐 - 购物车',
      path: '/pages/cart/cart'
    };
  }
});