// pages/order/order.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    orders: [],           // 订单列表
    groupedOrders: [],    // 按日期分组的订单
    loading: false,
    hasMore: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 加载订单列表
    this.loadOrders();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 每次页面显示时刷新订单列表
    this.loadOrders();
  },

  /**
   * 加载订单列表
   */
  loadOrders() {
    const app = getApp();

    this.setData({ loading: true });

    app.apiRequest({
      url: '/orders',
      method: 'GET'
    }).then(response => {
      if (response.code === 200 && response.data) {
        // 处理订单数据，添加statusText字段
        const orders = (response.data.content || response.data).map(order => {
          const itemsInfo = this.getItemsText(order.items);
          return {
            ...order,
            statusText: this.getStatusText(order.status),
            itemsText: itemsInfo.text,
            itemList: itemsInfo.list
          };
        });
        // 按日期分组
        const groupedOrders = this.groupOrdersByDate(orders);
        this.setData({
          orders: orders,
          groupedOrders: groupedOrders,
          loading: false
        });
      }
    }).catch(error => {
      console.error('加载订单列表失败:', error);
      // 使用模拟数据
      if (app.globalData.useMockData) {
        this.setMockOrders();
      }
      this.setData({ loading: false });
    });
  },

  /**
   * 获取状态文本
   */
  getStatusText(status) {
    const statusMap = {
      'PLACED': '已下单',
      'COMPLETED': '已制作完成'
    };
    return statusMap[status] || status;
  },

  /**
   * 获取商品文本
   */
  getItemsText(items) {
    if (!items || items.length === 0) return { text: '暂无商品', list: [] };
    const itemNames = items.map(item => item.menuItemName || item.name || item.dishName || '菜品');
    if (itemNames.length === 1) return { text: itemNames[0], list: itemNames };
    return { text: `${itemNames[0]} 等${itemNames.length}件商品`, list: itemNames };
  },

  /**
   * 设置模拟订单数据
   */
  setMockOrders() {
    const mockOrders = [
      {
        id: 1,
        orderNo: '202603121830',
        storeName: '家庭厨房',
        status: 'PROCESSING',
        statusText: '进行中',
        items: [
          { name: '红烧肉' },
          { name: '清蒸鲈鱼' },
          { name: '宫保鸡丁' }
        ],
        itemsText: '红烧肉 等3件商品',
        actualAmount: '134',
        createdAt: '2026-03-12 18:30'
      },
      {
        id: 2,
        orderNo: '202603111215',
        storeName: '家庭厨房',
        status: 'COMPLETED',
        statusText: '已完成',
        items: [
          { name: '清蒸鲈鱼' },
          { name: '西红柿炒鸡蛋' }
        ],
        itemsText: '清蒸鲈鱼 等2件商品',
        actualAmount: '92',
        createdAt: '2026-03-11 12:15'
      },
      {
        id: 3,
        orderNo: '202603091830',
        storeName: '家庭厨房',
        status: 'COMPLETED',
        statusText: '已完成',
        items: [
          { name: '糖醋排骨' }
        ],
        itemsText: '糖醋排骨',
        actualAmount: '58',
        createdAt: '2026-03-09 18:30'
      }
    ];
    const groupedOrders = this.groupOrdersByDate(mockOrders);
    this.setData({
      orders: mockOrders,
      groupedOrders: groupedOrders
    });
  },

  /**
   * 按日期分组订单
   */
  groupOrdersByDate(orders) {
    const app = getApp();
    const isAdmin = app.globalData.userInfo && app.globalData.userInfo.role === 'ADMIN';

    // 管理员模式：先按日期分组，再按用户分组
    if (isAdmin) {
      return this.groupOrdersByDateAndUser(orders);
    }

    // 普通用户模式：只按日期分组
    const groups = {};

    orders.forEach(order => {
      const createdAt = order.createdAt;
      let dateKey = '';

      if (createdAt) {
        const orderDate = new Date(createdAt);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (this.isSameDate(orderDate, today)) {
          dateKey = '今天';
        } else if (this.isSameDate(orderDate, yesterday)) {
          dateKey = '昨天';
        } else {
          dateKey = this.formatDate(orderDate);
        }
      } else {
        dateKey = '未知';
      }

      if (!groups[dateKey]) {
        groups[dateKey] = [];
      }
      groups[dateKey].push(order);
    });

    const groupArray = Object.keys(groups).map(key => ({
      date: key,
      orders: groups[key],
      expanded: true
    }));

    groupArray.sort((a, b) => {
      if (a.date === '今天') return -1;
      if (b.date === '今天') return 1;
      if (a.date === '昨天') return -1;
      if (b.date === '昨天') return 1;
      return 0;
    });

    return groupArray;
  },

  /**
   * 按日期和用户分组订单（管理员模式）
   */
  groupOrdersByDateAndUser(orders) {
    const dateGroups = {};

    orders.forEach(order => {
      const createdAt = order.createdAt;
      let dateKey = '';

      if (createdAt) {
        const orderDate = new Date(createdAt);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (this.isSameDate(orderDate, today)) {
          dateKey = '今天';
        } else if (this.isSameDate(orderDate, yesterday)) {
          dateKey = '昨天';
        } else {
          dateKey = this.formatDate(orderDate);
        }
      } else {
        dateKey = '未知';
      }

      if (!dateGroups[dateKey]) {
        dateGroups[dateKey] = {};
      }

      // 按用户分组
      const userKey = order.userNickname || order.userId || '未知用户';
      if (!dateGroups[dateKey][userKey]) {
        dateGroups[dateKey][userKey] = [];
      }
      dateGroups[dateKey][userKey].push(order);
    });

    // 转换为数组结构
    const groupArray = Object.keys(dateGroups).map(dateKey => ({
      date: dateKey,
      expanded: true,
      userGroups: Object.keys(dateGroups[dateKey]).map(userKey => ({
        userName: userKey,
        orders: dateGroups[dateKey][userKey]
      }))
    }));

    groupArray.sort((a, b) => {
      if (a.date === '今天') return -1;
      if (b.date === '今天') return 1;
      if (a.date === '昨天') return -1;
      if (b.date === '昨天') return 1;
      return 0;
    });

    return groupArray;
  },

  /**
   * 判断两个日期是否同一天
   */
  isSameDate(date1, date2) {
    return date1.getFullYear() === date2.getFullYear() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getDate() === date2.getDate();
  },

  /**
   * 格式化日期
   */
  formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  /**
   * 切换分组展开/收起
   */
  toggleGroup(e) {
    const index = e.currentTarget.dataset.index;
    const groupedOrders = this.data.groupedOrders;
    groupedOrders[index].expanded = !groupedOrders[index].expanded;
    this.setData({ groupedOrders });
  },

  /**
   * 去菜单页面
   */
  goToMenu() {
    wx.switchTab({
      url: '/pages/menu/menu'
    });
  },

  /**
   * 查看订单详情
   */
  goToOrderDetail(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order-detail/order-detail?id=${orderId}`
    });
  }
});