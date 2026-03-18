// pages/menu-management/menu-management.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    menuItems: [],
    categories: [],
    selectedCategoryId: null,
    searchKeyword: '',
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 20
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    this.loadCategories();
    this.loadMenuItems(true);
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 每次显示时刷新数据
    this.loadMenuItems(true);
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
        const categories = response.data || [];
        this.setData({ categories });
      }
    }).catch(error => {
      console.error('加载分类失败:', error);
    });
  },

  /**
   * 加载菜品数据（管理端，获取所有菜品包括停售的）
   */
  loadMenuItems(refresh = false) {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    const app = getApp();
    const { page, pageSize, selectedCategoryId, searchKeyword } = this.data;

    let params = {
      page: refresh ? 1 : page,
      size: pageSize
    };

    if (selectedCategoryId) {
      params.categoryId = selectedCategoryId;
    }

    // 使用管理端API获取所有菜品（包括停售的）
    let url = '/menu/admin/items';
    if (searchKeyword) {
      params.keyword = searchKeyword;
    }

    app.apiRequest({
      url,
      method: 'GET',
      data: params
    }).then(response => {
      if (response.code === 200) {
        const data = response.data || {};
        const items = data.content || [];

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
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }).finally(() => {
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 切换分类
   */
  onCategoryChange(e) {
    const categoryId = e.detail.value;
    this.setData({
      selectedCategoryId: categoryId ? this.data.categories[categoryId]?.id : null,
      searchKeyword: ''
    });
    this.loadMenuItems(true);
  },

  /**
   * 搜索输入
   */
  onSearchInput(e) {
    const value = e.detail.value;
    if (!value) {
      this.setData({ searchKeyword: '' });
      this.loadMenuItems(true);
    }
  },

  /**
   * 搜索菜品
   */
  onSearch(e) {
    const keyword = e.detail.value.trim();
    this.setData({
      searchKeyword: keyword,
      selectedCategoryId: null
    });
    this.loadMenuItems(true);
  },

  /**
   * 跳转到新增菜品页面
   */
  goToAddDish() {
    wx.navigateTo({
      url: '/admin/pages/dish-edit/dish-edit?type=add'
    });
  },

  /**
   * 跳转到编辑菜品页面
   */
  goToEditDish(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/admin/pages/dish-edit/dish-edit?type=edit&id=${id}`
    });
  },

  /**
   * 删除菜品
   */
  onDeleteDish(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;

    wx.showModal({
      title: '停售确认',
      content: `确定要停售菜品"${name}"吗？停售后可在编辑页面重新上架。`,
      success: (res) => {
        if (res.confirm) {
          this.deleteMenuItem(id);
        }
      }
    });
  },

  /**
   * 删除菜品API
   */
  deleteMenuItem(id) {
    const app = getApp();

    app.apiRequest({
      url: `/menu/items/${id}`,
      method: 'DELETE'
    }).then(response => {
      if (response.code === 200) {
        wx.showToast({
          title: '已停售',
          icon: 'success'
        });
        // 刷新列表
        this.loadMenuItems(true);
      } else {
        wx.showToast({
          title: response.message || '删除失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      console.error('删除菜品失败:', error);
      wx.showToast({
        title: '删除失败',
        icon: 'none'
      });
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
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '菜单管理',
      path: '/admin/pages/menu-management/menu-management'
    };
  }
});
