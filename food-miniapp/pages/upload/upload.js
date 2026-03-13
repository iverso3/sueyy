// pages/upload/upload.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    menuItems: [],          // 菜品列表
    selectedItemIndex: -1,  // 选中的菜品索引
    selectedItemId: null,   // 选中的菜品ID
    selectedItemName: '',   // 选中的菜品名称
    tempFilePaths: [],      // 临时图片路径
    uploadResult: null,     // 上传结果
    uploading: false,       // 是否正在上传
    baseUrl: 'http://localhost:8080/api'  // 后端API地址
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    this.loadMenuItems();
  },

  /**
   * 加载菜品列表
   */
  loadMenuItems() {
    const app = getApp();
    wx.showLoading({ title: '加载中...' });

    app.apiRequest({
      url: '/menu/items',
      method: 'GET',
      data: {
        page: 1,
        size: 100
      }
    }).then(response => {
      wx.hideLoading();
      if (response.code === 200) {
        const data = response.data || {};
        const items = data.content || [];
        this.setData({ menuItems: items });
      } else {
        wx.showToast({
          title: '加载菜品失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('加载菜品失败:', error);
      wx.showToast({
        title: '加载菜品失败',
        icon: 'none'
      });
    });
  },

  /**
   * 选择菜品
   */
  onSelectItem(e) {
    const index = parseInt(e.detail.value);
    if (index >= 0 && index < this.data.menuItems.length) {
      const selectedItem = this.data.menuItems[index];
      this.setData({
        selectedItemIndex: index,
        selectedItemId: selectedItem.id,
        selectedItemName: selectedItem.name
      });
    }
  },

  /**
   * 选择图片
   */
  onChooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'], // 压缩图
      sourceType: ['album', 'camera'], // 相册或相机
      success: (res) => {
        // tempFilePath可以作为img标签的src属性显示图片
        const tempFilePaths = res.tempFilePaths;
        this.setData({
          tempFilePaths: tempFilePaths
        });
        wx.showToast({
          title: '选择成功',
          icon: 'success'
        });
      },
      fail: (error) => {
        console.error('选择图片失败:', error);
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 上传图片
   */
  onUploadImage() {
    const { tempFilePaths, selectedItemId } = this.data;

    if (!selectedItemId) {
      wx.showToast({
        title: '请先选择菜品',
        icon: 'none'
      });
      return;
    }

    if (tempFilePaths.length === 0) {
      wx.showToast({
        title: '请先选择图片',
        icon: 'none'
      });
      return;
    }

    this.setData({ uploading: true });
    const tempFilePath = tempFilePaths[0];

    // 先上传图片到服务器
    wx.uploadFile({
      url: `${this.data.baseUrl}/upload/image`,
      filePath: tempFilePath,
      name: 'file',
      formData: {},
      success: (res) => {
        try {
          const response = JSON.parse(res.data);
          if (response.code === 200) {
            const fileUrl = response.data.fileUrl;
            this.updateMenuItemImage(selectedItemId, fileUrl);
          } else {
            wx.showToast({
              title: '图片上传失败: ' + (response.message || '未知错误'),
              icon: 'none'
            });
            this.setData({ uploading: false });
          }
        } catch (error) {
          console.error('解析响应失败:', error);
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
          this.setData({ uploading: false });
        }
      },
      fail: (error) => {
        console.error('上传图片失败:', error);
        wx.showToast({
          title: '上传图片失败',
          icon: 'none'
        });
        this.setData({ uploading: false });
      }
    });
  },

  /**
   * 更新菜品图片URL
   */
  updateMenuItemImage(itemId, imageUrl) {
    const app = getApp();

    app.apiRequest({
      url: `/menu/items/${itemId}/image?imageUrl=${encodeURIComponent(imageUrl)}`,
      method: 'PUT'
    }).then(response => {
      this.setData({ uploading: false });
      if (response.code === 200) {
        wx.showToast({
          title: '菜品图片更新成功',
          icon: 'success'
        });

        // 更新本地菜品列表中的图片
        const updatedItems = this.data.menuItems.map(item => {
          if (item.id == itemId) {
            return { ...item, imageUrl: imageUrl };
          }
          return item;
        });

        this.setData({
          menuItems: updatedItems,
          uploadResult: {
            success: true,
            message: '菜品图片更新成功',
            imageUrl: imageUrl
          }
        });
      } else {
        wx.showToast({
          title: '更新菜品图片失败: ' + (response.message || '未知错误'),
          icon: 'none'
        });
      }
    }).catch(error => {
      this.setData({ uploading: false });
      console.error('更新菜品图片失败:', error);
      wx.showToast({
        title: '更新菜品图片失败',
        icon: 'none'
      });
    });
  },

  /**
   * 清除上传结果
   */
  clearResult() {
    this.setData({
      uploadResult: null,
      tempFilePaths: [],
      selectedItemIndex: -1,
      selectedItemId: null,
      selectedItemName: ''
    });
  },

  /**
   * 预览图片
   */
  previewImage() {
    const { tempFilePaths } = this.data;
    if (tempFilePaths.length > 0) {
      wx.previewImage({
        urls: tempFilePaths
      });
    }
  },

  /**
   * 复制图片URL
   */
  copyImageUrl() {
    const { uploadResult } = this.data;
    if (uploadResult && uploadResult.imageUrl) {
      wx.setClipboardData({
        data: uploadResult.imageUrl,
        success: () => {
          wx.showToast({
            title: '图片URL已复制',
            icon: 'success'
          });
        }
      });
    }
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '菜品图片上传',
      path: '/pages/upload/upload'
    };
  }
});