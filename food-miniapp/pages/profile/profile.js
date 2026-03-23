// pages/profile/profile.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: null,
    isAdmin: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    const app = getApp();
    const userInfo = app.globalData.userInfo;
    console.log('loadUserInfo - userInfo:', userInfo);

    if (userInfo) {
      // 检查是否是管理员（兼容大小写）
      const role = userInfo.role || '';
      const isAdmin = role === 'ADMIN' || role === 'admin' || role === 'Admin';
      console.log('isAdmin:', isAdmin, 'role:', role);

      this.setData({
        userInfo: userInfo,
        isAdmin: isAdmin
      });
    } else {
      // 使用模拟数据
      this.setData({
        userInfo: {
          username: '用户',
          avatarUrl: '',
          role: 'USER'
        },
        isAdmin: false
      });
    }
  },

  /**
   * 跳转到上传页面
   */
  goToUpload() {
    wx.navigateTo({
      url: '/admin/pages/upload/upload'
    });
  },

  /**
   * 跳转到菜单管理页面
   */
  goToMenuManagement() {
    wx.navigateTo({
      url: '/admin/pages/menu-management/menu-management'
    });
  },

  /**
   * 跳转到编辑资料页面
   */
  goToEditProfile() {
    wx.navigateTo({
      url: '/subpages/pages/edit-profile/edit-profile'
    });
  },

  /**
   * 每日签到
   */
  onSignIn() {
    wx.showToast({
      title: '签到成功',
      icon: 'success'
    });
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          const app = getApp();
          app.logout().then(() => {
            wx.reLaunch({
              url: '/subpages/pages/login/login'
            });
          }).catch(() => {
            // 即使后端失败也清除本地数据
            app.globalData.userInfo = null;
            app.globalData.token = null;
            wx.reLaunch({
              url: '/subpages/pages/login/login'
            });
          });
        }
      }
    });
  }
});