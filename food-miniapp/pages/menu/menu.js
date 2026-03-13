// pages/menu/menu.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    categories: [],  // 分类列表
    currentCategory: 0,  // 当前选中分类ID
    menuItems: [],  // 菜品列表
    featuredItems: [],  // 招牌必点
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 10,
    searchKeyword: '',  // 搜索关键词
    isSearching: false,
    userInfo: null,  // 用户信息
    showUserMenu: false,  // 是否显示用户菜单
    wantCount: 0  // 想吃数量
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadInitialData();
    this.loadUserInfo();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 检查登录状态
    const app = getApp();
    if (!app.isLoggedIn()) {
      // token无效或过期，跳转到登录页
      app.redirectToLogin();
      return;
    }

    // 页面显示时刷新想吃数量
    this.getWantCount();
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    const app = getApp();
    this.setData({
      userInfo: app.globalData.userInfo
    });
  },

  /**
   * 显示用户菜单
   */
  showUserMenu() {
    this.setData({
      showUserMenu: true
    });
  },

  /**
   * 隐藏用户菜单
   */
  hideUserMenu() {
    this.setData({
      showUserMenu: false
    });
  },

  /**
   * 处理退出登录
   */
  handleLogout() {
    const app = getApp();

    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            showUserMenu: false
          });
          app.logout();
        }
      }
    });
  },

  /**
   * 加载初始数据
   */
  loadInitialData() {
    this.loadCategories();
    this.loadFeaturedItems();
    this.loadMenuItems(true);
  },

  /**
   * 加载招牌必点数据
   */
  loadFeaturedItems() {
    const app = getApp();

    app.apiRequest({
      url: '/menu/featured',
      method: 'GET'
    }).then(response => {
      if (response.code === 200) {
        this.setData({
          featuredItems: response.data || []
        });
      }
    }).catch(error => {
      console.error('加载招牌必点失败:', error);
      // 使用模拟数据
      if (app.globalData.useMockData) {
        this.setData({
          featuredItems: [
            {
              id: 1,
              name: '红烧肉',
              price: 38.00,
              imageUrl: 'https://images.unsplash.com/photo-1563245372-f21724e3856d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
            },
            {
              id: 2,
              name: '清蒸鲈鱼',
              price: 58.00,
              imageUrl: 'https://images.unsplash.com/photo-1565557623262-b51c2513a641?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
            },
            {
              id: 3,
              name: '宫保鸡丁',
              price: 32.00,
              imageUrl: 'https://images.unsplash.com/photo-1559847844-5315695dadae?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
            }
          ]
        });
      }
    });
  },

  /**
   * 加载分类数据
   */
  loadCategories() {
    const app = getApp();

    app.apiRequest({
      url: '/menu/categories',
      method: 'GET'
    }).then(response => {
      if (response.code === 200) {
        let categories = response.data || [];
        // 添加店长推荐作为第一个分类
        categories.unshift({
          id: 0,
          name: '店长推荐',
          icon: '🔥'
        });
        // 为每个分类添加默认图标
        const iconMap = {
          1: '🍖', 2: '🥬', 3: '🥣', 4: '🍚', 5: '🥗', 6: '🍷'
        };
        categories = categories.map(cat => ({
          ...cat,
          icon: cat.icon || iconMap[cat.id] || '🍽️'
        }));
        this.setData({ categories });
      }
    }).catch(error => {
      console.error('加载分类失败:', error);
      // 使用模拟数据
      if (app.globalData.useMockData) {
        this.setData({
          categories: [
            { id: 0, name: '店长推荐', icon: '🔥' },
            { id: 1, name: '招牌硬菜', icon: '🍖' },
            { id: 2, name: '家常小炒', icon: '🥬' },
            { id: 3, name: '滋补靓汤', icon: '🥣' },
            { id: 4, name: '主食点心', icon: '🍚' },
            { id: 5, name: '凉菜', icon: '🥗' }
          ]
        });
      }
    });
  },

  /**
   * 加载菜品数据
   */
  loadMenuItems(refresh = false) {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    const app = getApp();
    const { page, pageSize, currentCategory, searchKeyword, isSearching } = this.data;

    // 构建请求参数
    let params = {
      page: refresh ? 1 : page,
      size: pageSize
    };

    if (currentCategory > 0) {
      params.categoryId = currentCategory;
    }

    if (searchKeyword && isSearching) {
      params.keyword = searchKeyword;
    }

    // 确定请求URL
    let url = '/menu/items';
    if (isSearching) {
      url = '/menu/items/search';
    }

    app.apiRequest({
      url,
      method: 'GET',
      data: params
    }).then(response => {
      if (response.code === 200) {
        const data = response.data || {};
        const items = data.content || data.items || [];

        // 更新数据
        if (refresh) {
          this.setData({
            menuItems: items,
            page: 1,
            hasMore: items.length >= pageSize
          });
        } else {
          this.setData({
            menuItems: this.data.menuItems.concat(items),
            page: this.data.page + 1,
            hasMore: items.length >= pageSize
          });
        }
      }
    }).catch(error => {
      console.error('加载菜品失败:', error);
      // 使用模拟数据
      if (app.globalData.useMockData) {
        this.setMockMenuItems(refresh);
      }
    }).finally(() => {
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 设置模拟菜品数据
   */
  setMockMenuItems(refresh) {
    const mockItems = [
      {
        id: 1,
        name: '红烧肉',
        description: '经典红烧肉，肥而不腻',
        price: 38.00,
        originalPrice: 45.00,
        imageUrl: 'https://images.unsplash.com/photo-1563245372-f21724e3856d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        isRecommended: true,
        isHot: true,
        stock: 100,
        categoryId: 1,
        categoryName: '招牌菜'
      },
      {
        id: 2,
        name: '清蒸鲈鱼',
        description: '鲜嫩清蒸，原汁原味',
        price: 58.00,
        originalPrice: 68.00,
        imageUrl: 'https://images.unsplash.com/photo-1565557623262-b51c2513a641?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        isRecommended: true,
        isHot: false,
        stock: 50,
        categoryId: 1,
        categoryName: '招牌菜'
      },
      {
        id: 3,
        name: '宫保鸡丁',
        description: '麻辣鲜香，经典川菜',
        price: 32.00,
        originalPrice: 38.00,
        imageUrl: 'https://images.unsplash.com/photo-1559847844-5315695dadae?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        isRecommended: false,
        isHot: true,
        stock: 80,
        categoryId: 2,
        categoryName: '热销菜'
      },
      {
        id: 4,
        name: '拍黄瓜',
        description: '清爽开胃凉菜',
        price: 12.00,
        originalPrice: 15.00,
        imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        isRecommended: false,
        isHot: false,
        stock: 200,
        categoryId: 3,
        categoryName: '凉菜'
      },
      {
        id: 5,
        name: '西红柿鸡蛋汤',
        description: '家常汤品，营养丰富',
        price: 18.00,
        originalPrice: 22.00,
        imageUrl: 'https://images.unsplash.com/photo-1547592180-85f173990554?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        isRecommended: true,
        isHot: false,
        stock: 120,
        categoryId: 4,
        categoryName: '汤类'
      }
    ];

    if (refresh) {
      this.setData({
        menuItems: mockItems,
        page: 1,
        hasMore: true
      });
    } else {
      this.setData({
        menuItems: this.data.menuItems.concat(mockItems),
        page: this.data.page + 1,
        hasMore: true
      });
    }
  },

  /**
   * 切换分类
   */
  switchCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    if (categoryId === this.data.currentCategory) {
      return;
    }

    this.setData({
      currentCategory: categoryId,
      searchKeyword: '',
      isSearching: false
    });

    // 招牌必点分类显示推荐菜品
    if (categoryId === 0) {
      this.loadFeaturedItems();
      // 加载所有菜品
      this.loadMenuItems(true);
    } else {
      // 重新加载菜品
      this.loadMenuItems(true);
    }
  },

  /**
   * 搜索输入
   */
  onSearchInput(e) {
    const value = e.detail.value;
    if (!value) {
      this.clearSearch();
    }
  },

  /**
   * 搜索菜品
   */
  onSearch(e) {
    const keyword = e.detail.value.trim();
    if (!keyword) {
      // 清空搜索，显示全部
      this.setData({
        searchKeyword: '',
        isSearching: false
      });
      this.loadMenuItems(true);
      return;
    }

    this.setData({
      searchKeyword: keyword,
      isSearching: true
    });

    this.loadMenuItems(true);
  },

  /**
   * 清除搜索
   */
  clearSearch() {
    this.setData({
      searchKeyword: '',
      isSearching: false
    });
    this.loadMenuItems(true);
  },

  /**
   * 添加到想吃
   */
  addToCart(e) {
    const menuItemId = e.currentTarget.dataset.id;
    const app = getApp();

    app.apiRequest({
      url: '/cart/items',
      method: 'POST',
      data: {
        menuItemId: menuItemId,
        quantity: 1
      }
    }).then(response => {
      if (response.code === 200) {
        wx.showToast({
          title: '已加入想吃',
          icon: 'success'
        });

        // 更新想吃数量
        this.getWantCount();
      }
    }).catch(error => {
      console.error('添加到想吃失败:', error);
      wx.showToast({
        title: '添加失败，请重试',
        icon: 'none'
      });
    });
  },

  /**
   * 获取想吃数量
   */
  getWantCount() {
    const app = getApp();

    app.apiRequest({
      url: '/cart/get',
      method: 'GET'
    }).then(response => {
      if (response.code === 200 && response.data) {
        const wantCount = response.data.totalQuantity || 0;
        this.setData({ wantCount });
      }
    }).catch(error => {
      console.error('获取想吃数量失败:', error);
    });
  },

  /**
   * 跳转到想吃页面
   */
  goToWantList() {
    wx.navigateTo({
      url: '/pages/cart/cart'
    });
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadMenuItems(true);
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMenuItems();
    }
  },

  /**
   * 跳转到上传页面
   */
  gotoUpload() {
    wx.navigateTo({
      url: '/pages/upload/upload'
    });
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '在线点餐 - 美味佳肴',
      path: '/pages/menu/menu'
    };
  }
});