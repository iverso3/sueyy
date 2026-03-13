// pages/login/login.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    isLoading: false,
    errorMsg: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 检查是否已登录且token有效
    const app = getApp();
    if (app.isLoggedIn()) {
      // 已登录且token有效，跳转到菜单页
      wx.switchTab({
        url: '/pages/menu/menu'
      });
    }
    // 否则显示登录页面
  },

  /**
   * 处理微信登录
   */
  handleWeChatLogin() {
    const app = getApp();

    if (this.data.isLoading) {
      return;
    }

    this.setData({
      isLoading: true,
      errorMsg: ''
    });

    // 显示加载中
    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    // 调用微信登录
    app.wxLogin().then(data => {
      wx.hideLoading();

      // 登录成功，跳转到菜单页
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      });

      setTimeout(() => {
        wx.switchTab({
          url: '/pages/menu/menu'
        });
      }, 1500);

    }).catch(err => {
      wx.hideLoading();

      console.error('登录失败:', err);

      let errorMsg = '登录失败，请重试';
      if (err.message) {
        errorMsg = err.message;
      } else if (err.code === 1001) {
        errorMsg = '微信登录失败';
      }

      this.setData({
        isLoading: false,
        errorMsg
      });

      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 3000
      });

      // 测试模式下，允许使用模拟登录
      if (app.globalData.useMockData) {
        this.handleMockLogin();
      }
    });
  },

  /**
   * 模拟登录（用于测试）
   */
  handleMockLogin() {
    const app = getApp();

    // 生成模拟token和用户信息
    const mockData = {
      token: 'mock-token-' + Date.now(),
      userInfo: {
        id: 1,
        nickname: '测试用户',
        avatarUrl: 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
        openid: 'mock-openid'
      }
    };

    app.setToken(mockData.token);
    app.globalData.userInfo = mockData.userInfo;

    wx.showToast({
      title: '模拟登录成功',
      icon: 'success'
    });

    setTimeout(() => {
      wx.switchTab({
        url: '/pages/menu/menu'
      });
    }, 1500);
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '在线点餐小程序',
      path: '/pages/login/login'
    };
  }
});