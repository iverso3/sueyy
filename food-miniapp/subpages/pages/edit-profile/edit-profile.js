// pages/edit-profile/edit-profile.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    nickname: '',
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
    console.log('onSave called');
    const { nickname, avatarUrl } = this.data;
    console.log('data:', { nickname, avatarUrl });

    if (!nickname || nickname.trim() === '') {
      wx.showToast({
        title: '请输入昵称',
        icon: 'none'
      });
      return;
    }

    const app = getApp();
    console.log('token:', app.globalData.token);

    wx.showLoading({ title: '保存中...' });

    // 如果有新头像（微信临时文件或本地文件），先上传
    if (avatarUrl && (avatarUrl.startsWith('wxfile://') || avatarUrl.startsWith('http://tmp/'))) {
      wx.uploadFile({
        url: `${app.globalData.apiBaseUrl}/upload/image`,
        filePath: avatarUrl,
        name: 'file',
        success: (res) => {
          try {
            const response = JSON.parse(res.data);
            if (response.code === 200) {
              this.doSave({ nickname: nickname.trim(), avatarUrl: response.data.fileUrl });
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
      this.doSave({ nickname: nickname.trim(), avatarUrl });
    }
  },

  /**
   * 执行保存
   */
  doSave(data) {
    const app = getApp();
    const apiBaseUrl = app.globalData.apiBaseUrl;
    const token = app.globalData.token;
    const userId = app.globalData.userId;

    console.log('doSave - userId:', userId);
    console.log('doSave - apiBaseUrl:', apiBaseUrl);

    wx.request({
      url: apiBaseUrl + '/user/profile',
      method: 'PUT',
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token,
        'X-User-Id': userId
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          app.globalData.userInfo = { ...app.globalData.userInfo, ...data };
          wx.showToast({ title: '保存成功', icon: 'success' });
          setTimeout(() => { wx.navigateBack(); }, 1500);
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '请求失败', icon: 'none' });
        console.error('保存失败:', err);
      }
    });
  }
});
