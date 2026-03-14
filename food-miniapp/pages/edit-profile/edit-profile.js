// pages/edit-profile/edit-profile.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    nickname: '',
    phone: '',
    avatarUrl: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    const app = getApp();
    const userInfo = app.globalData.userInfo || {};

    this.setData({
      nickname: userInfo.nickname || '',
      phone: userInfo.phone || '',
      avatarUrl: userInfo.avatarUrl || ''
    });
  },

  /**
   * 昵称输入
   */
  onNicknameInput(e) {
    this.setData({
      nickname: e.detail.value
    });
  },

  /**
   * 手机号输入
   */
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    });
  },

  /**
   * 选择头像
   */
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    this.setData({
      avatarUrl: avatarUrl
    });
  },

  /**
   * 保存资料
   */
  onSave() {
    const { nickname, phone, avatarUrl } = this.data;

    if (!nickname || nickname.trim() === '') {
      wx.showToast({
        title: '请输入昵称',
        icon: 'none'
      });
      return;
    }

    const app = getApp();

    wx.showLoading({ title: '保存中...' });

    // 如果有新头像，先上传
    if (avatarUrl && avatarUrl.startsWith('wxfile://')) {
      wx.uploadFile({
        url: `${app.globalData.apiBaseUrl}/upload/image`,
        filePath: avatarUrl,
        name: 'file',
        success: (res) => {
          try {
            const response = JSON.parse(res.data);
            if (response.code === 200) {
              this.doSave({ nickname: nickname.trim(), phone, avatarUrl: response.data.fileUrl });
            } else {
              wx.showToast({ title: '头像上传失败', icon: 'none' });
            }
          } catch (error) {
            wx.showToast({ title: '保存失败', icon: 'none' });
          }
        },
        fail: () => {
          wx.showToast({ title: '保存失败', icon: 'none' });
        }
      });
    } else {
      this.doSave({ nickname: nickname.trim(), phone, avatarUrl });
    }
  },

  /**
   * 执行保存
   */
  doSave(data) {
    const app = getApp();

    app.request('/user/profile', {
      method: 'PUT',
      data: data
    }).then(res => {
      wx.hideLoading();

      if (res.code === 200) {
        // 更新全局用户信息
        app.globalData.userInfo = { ...app.globalData.userInfo, ...data };

        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: res.message || '保存失败',
          icon: 'none'
        });
      }
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({
        title: '保存失败',
        icon: 'none'
      });
    });
  }
});
