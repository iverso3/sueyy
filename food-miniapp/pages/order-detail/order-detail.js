// pages/order-detail/order-detail.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    order: null,
    loading: true
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

    app.apiRequest({
      url: `/orders/${orderId}`,
      method: 'GET'
    }).then(response => {
      if (response.code === 200 && response.data) {
        this.setData({
          order: response.data,
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
      path: '/pages/order-detail/order-detail'
    };
  }
});
