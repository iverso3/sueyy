// pages/dish-edit/dish-edit.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    type: 'add', // add 或 edit
    dishId: null,

    // 表单数据
    name: '',
    categoryId: null,
    categoryIndex: 0,
    price: '',
    originalPrice: '',
    description: '',
    imageUrl: '',
    stock: '',
    isRecommended: false,
    isHot: false,
    sortOrder: 0,
    isActive: true,

    // 分类列表
    categories: [],

    // 图片相关
    tempFilePath: '',

    // 状态
    loading: false,
    submitting: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const { type, id } = options;
    this.setData({
      type: type || 'add',
      dishId: id || null
    });

    // 设置页面标题
    wx.setNavigationBarTitle({
      title: type === 'add' ? '新增菜品' : '编辑菜品'
    });

    // 加载分类列表
    this.loadCategories();

    // 如果是编辑模式，加载菜品详情
    if (type === 'edit' && id) {
      this.loadDishDetail(id);
    }
  },

  /**
   * 加载分类列表
   */
  loadCategories() {
    const app = getApp();

    return app.apiRequest({
      url: '/menu/categories',
      method: 'GET'
    }).then(response => {
      if (response.code === 200) {
        const categories = response.data || [];
        this.setData({ categories });
      }
    }).catch(error => {
      console.error('加载分类失败:', error);
      wx.showToast({
        title: '加载分类失败',
        icon: 'none'
      });
    });
  },

  /**
   * 加载菜品详情
   */
  loadDishDetail(id) {
    const app = getApp();
    this.setData({ loading: true });

    // 先确保分类列表已加载
    const loadCategories = this.loadCategories();

    app.apiRequest({
      url: `/menu/items/${id}`,
      method: 'GET'
    }).then(async response => {
      // 等待分类加载完成
      await loadCategories;

      this.setData({ loading: false });
      if (response.code === 200) {
        const dish = response.data;
        // 找到分类索引
        const categoryIndex = this.data.categories.findIndex(c => c.id === dish.categoryId);

        this.setData({
          name: dish.name || '',
          categoryId: dish.categoryId,
          categoryIndex: categoryIndex >= 0 ? categoryIndex : 0,
          price: dish.price ? String(dish.price) : '',
          originalPrice: dish.originalPrice ? String(dish.originalPrice) : '',
          description: dish.description || '',
          imageUrl: dish.imageUrl || '',
          stock: dish.stock !== undefined && dish.stock !== -1 ? String(dish.stock) : '',
          isRecommended: dish.isRecommended || false,
          isHot: dish.isHot || false,
          sortOrder: dish.sortOrder || 0,
          isActive: dish.isActive !== false
        });
      } else {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      this.setData({ loading: false });
      console.error('加载菜品详情失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  /**
   * 输入框绑定
   */
  onInput(e) {
    const { field } = e.currentTarget.dataset;
    const value = e.detail.value;
    this.setData({ [field]: value });
  },

  /**
   * 分类选择
   */
  onCategoryChange(e) {
    const index = parseInt(e.detail.value);
    const category = this.data.categories[index];
    this.setData({
      categoryIndex: index,
      categoryId: category.id
    });
  },

  /**
   * 开关绑定
   */
  onSwitchChange(e) {
    const { field } = e.currentTarget.dataset;
    const value = e.detail.value;
    this.setData({ [field]: value });
  },

  /**
   * 选择图片
   */
  onChooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        this.setData({
          tempFilePath: tempFilePath
        });
      },
      fail: (error) => {
        console.error('选择图片失败:', error);
      }
    });
  },

  /**
   * 预览图片
   */
  previewImage() {
    const { imageUrl, tempFilePath } = this.data;
    const urls = [];
    if (tempFilePath) {
      urls.push(tempFilePath);
    }
    if (imageUrl) {
      urls.push(imageUrl);
    }
    if (urls.length > 0) {
      wx.previewImage({
        urls: urls
      });
    }
  },

  /**
   * 移除图片
   */
  removeImage() {
    this.setData({
      tempFilePath: '',
      imageUrl: ''
    });
  },

  /**
   * 上传图片
   */
  uploadImage() {
    return new Promise((resolve, reject) => {
      const { tempFilePath } = this.data;
      if (!tempFilePath) {
        resolve('');
        return;
      }

      const app = getApp();

      wx.uploadFile({
        url: `${app.globalData.apiBaseUrl}/upload/image`,
        filePath: tempFilePath,
        name: 'file',
        success: (res) => {
          try {
            const response = JSON.parse(res.data);
            if (response.code === 200) {
              resolve(response.data.fileUrl);
            } else {
              reject(new Error('图片上传失败'));
            }
          } catch (error) {
            reject(error);
          }
        },
        fail: (error) => {
          reject(error);
        }
      });
    });
  },

  /**
   * 提交表单
   */
  async onSubmit() {
    const { type, name, categoryId, price } = this.data;

    // 验证必填项
    if (!name || !name.trim()) {
      wx.showToast({
        title: '请输入菜品名称',
        icon: 'none'
      });
      return;
    }

    if (!categoryId) {
      wx.showToast({
        title: '请选择分类',
        icon: 'none'
      });
      return;
    }

    if (!price || isNaN(parseFloat(price))) {
      wx.showToast({
        title: '请输入有效价格',
        icon: 'none'
      });
      return;
    }

    this.setData({ submitting: true });

    try {
      // 如果有新图片，先上传
      let imageUrl = this.data.imageUrl;
      if (this.data.tempFilePath) {
        imageUrl = await this.uploadImage();
      }

      const app = getApp();
      const requestData = {
        name: name.trim(),
        categoryId: categoryId,
        price: parseFloat(price),
        originalPrice: this.data.originalPrice ? parseFloat(this.data.originalPrice) : null,
        description: this.data.description.trim(),
        imageUrl: imageUrl,
        stock: this.data.stock ? parseInt(this.data.stock) : -1,
        isRecommended: this.data.isRecommended,
        isHot: this.data.isHot,
        sortOrder: this.data.sortOrder || 0,
        isActive: this.data.isActive
      };

      let response;
      if (type === 'add') {
        // 新增
        response = await app.apiRequest({
          url: '/menu/items',
          method: 'POST',
          data: requestData
        });
      } else {
        // 更新
        response = await app.apiRequest({
          url: `/menu/items/${this.data.dishId}`,
          method: 'PUT',
          data: requestData
        });
      }

      this.setData({ submitting: false });

      if (response.code === 200) {
        wx.showToast({
          title: type === 'add' ? '创建成功' : '更新成功',
          icon: 'success'
        });
        // 返回上一页
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: response.message || '操作失败',
          icon: 'none'
        });
      }
    } catch (error) {
      this.setData({ submitting: false });
      console.error('提交失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '菜品编辑',
      path: '/admin/pages/dish-edit/dish-edit'
    };
  }
});
