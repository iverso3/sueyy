// 小程序全局变量
App({
  // 全局数据
  globalData: {
    userInfo: null,
    userId: null, // 用户ID
    token: null,
    tokenExpiresTime: null, // token过期时间
    apiBaseUrl: 'https://qmihmahoouke.cloud.sealos.io/api',
    // apiBaseUrl: 'http://localhost:8080/api',
    // 本地测试时可使用模拟数据
    useMockData: false
  },

  // 小程序启动时执行
  onLaunch() {
    console.log('小程序启动');

    // 从本地存储获取token
    try {
      const token = wx.getStorageSync('token');
      const tokenExpireTime = wx.getStorageSync('tokenExpireTime');

      if (token) {
        this.globalData.token = token;
        console.log('从本地存储恢复token');

        // 检查token是否过期
        if (tokenExpireTime) {
          const expireTime = parseInt(tokenExpireTime);
          const now = Date.now();
          if (now >= expireTime) {
            console.log('token已过期，需要重新登录');
            this.clearLoginData();
          } else {
            this.globalData.tokenExpireTime = expireTime;
            console.log('token过期时间:', new Date(expireTime).toLocaleString());
          }
        }
      }
    } catch (e) {
      console.error('读取token失败', e);
    }

    // 从本地存储获取用户ID
    try {
      const userId = wx.getStorageSync('userId');
      if (userId) {
        this.globalData.userId = userId;
        console.log('从本地存储恢复用户ID:', userId);
      }
    } catch (e) {
      console.error('读取用户ID失败', e);
    }
  },

  // 设置token（包含过期时间）
  setToken(token, expiresIn = 3600) {
    this.globalData.token = token;
    // 计算过期时间（当前时间 + expiresIn秒）
    const expireTime = Date.now() + (expiresIn * 1000);
    this.globalData.tokenExpireTime = expireTime;

    try {
      wx.setStorageSync('token', token);
      wx.setStorageSync('tokenExpireTime', expireTime.toString());
      console.log('保存token，过期时间:', new Date(expireTime).toLocaleString());
    } catch (e) {
      console.error('保存token失败', e);
    }
  },

  // 设置用户ID
  setUserId(userId) {
    this.globalData.userId = userId;
    try {
      wx.setStorageSync('userId', userId);
    } catch (e) {
      console.error('保存用户ID失败', e);
    }
  },

  // 清除登录数据（保留用户名和ID用于显示）
  clearLoginData() {
    this.globalData.token = null;
    this.globalData.tokenExpireTime = null;
    try {
      wx.removeStorageSync('token');
      wx.removeStorageSync('tokenExpireTime');
    } catch (e) {
      console.error('清除token失败', e);
    }
  },

  // 清除用户ID
  clearUserId() {
    this.globalData.userId = null;
    try {
      wx.removeStorageSync('userId');
    } catch (e) {
      console.error('清除用户ID失败', e);
    }
  },

  // 检查是否已登录（token有效）
  isLoggedIn() {
    const { token, tokenExpireTime } = this.globalData;
    if (!token || !tokenExpireTime) {
      return false;
    }
    return Date.now() < parseInt(tokenExpireTime);
  },

  // 跳转到登录页
  redirectToLogin() {
    this.clearLoginData();
    this.clearUserId();
    this.globalData.userInfo = null;
    wx.redirectTo({
      url: '/pages/login/login'
    });
  },

  // 统一的API请求方法
  apiRequest(options) {
    const { url, method = 'GET', data = {}, header = {}, needAuth = true } = options;
    const fullUrl = this.globalData.apiBaseUrl + url;

    // 添加内容类型
    if (!header['Content-Type']) {
      header['Content-Type'] = 'application/json';
    }

    // 添加Bearer Token（如果需要认证）
    if (needAuth && this.globalData.token) {
      header['Authorization'] = 'Bearer ' + this.globalData.token;
    }

    // 添加用户ID头部（如果有）
    if (this.globalData.userId) {
      header['X-User-Id'] = this.globalData.userId;
    }

    // 添加用户角色头部（如果有）
    if (this.globalData.userInfo && this.globalData.userInfo.role) {
      header['X-User-Role'] = this.globalData.userInfo.role;
    }

    return new Promise((resolve, reject) => {
      wx.request({
        url: fullUrl,
        method,
        data,
        header,
        success: (res) => {
          // 处理token过期
          if (res.statusCode === 401 || res.statusCode === 403) {
            console.log('Token无效或已过期');
            this.redirectToLogin();
            reject({ code: res.statusCode, message: '登录已过期，请重新登录' });
            return;
          }

          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data);
          } else {
            reject(res.data || { code: res.statusCode, message: '请求失败' });
          }
        },
        fail: (err) => {
          console.error('API请求失败:', err);
          reject({ code: -1, message: '网络请求失败' });
        }
      });
    });
  },

  // 微信登录
  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) {
            // 调用后端登录接口
            this.apiRequest({
              url: '/auth/login',
              method: 'POST',
              data: { code: res.code },
              needAuth: false // 登录接口不需要认证
            }).then(response => {
              if (response.code === 200 && response.data) {
                // 保存token和过期时间
                const expiresIn = response.data.expiresIn || 3600;
                this.setToken(response.data.token, expiresIn);

                this.globalData.userInfo = response.data.userInfo;
                // 保存用户ID
                if (response.data.userInfo && response.data.userInfo.id) {
                  this.setUserId(response.data.userInfo.id);
                }

                // 刷新自定义tab栏（根据用户角色显示不同tab）
                if (this.getTabBar) {
                  this.getTabBar().initTabs();
                }

                resolve(response.data);
              } else {
                reject(response);
              }
            }).catch(reject);
          } else {
            reject({ code: -1, message: '微信登录失败' });
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  // 退出登录
  logout() {
    const app = this;

    // 调用后端登出接口
    if (this.globalData.token) {
      this.apiRequest({
        url: '/auth/logout',
        method: 'POST',
        needAuth: true
      }).catch(err => {
        console.error('登出请求失败:', err);
      }).finally(() => {
        // 清除本地登录数据
        app.clearLoginData();
        // 跳转到登录页
        app.redirectToLogin();
      });
    } else {
      // 没有token，直接清除并跳转
      this.clearLoginData();
      this.redirectToLogin();
    }
  }
});
