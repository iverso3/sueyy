// custom-tab-bar/index.js
Component({
  data: {
    current: 0,
    tabs: [],
    userRole: 'USER'
  },

  lifetimes: {
    attached() {
      console.log('Custom tab bar attached');
      this.initTabs();
      // 监听路由变化
      this.watchRouteChange();
    },

    show() {
      // 页面显示时刷新tab栏
      this.updateCurrentTab();
    }
  },

  pageLifetimes: {
    show() {
      // 页面显示时更新当前tab
      this.updateCurrentTab();
    }
  },

  methods: {
    initTabs() {
      // 统一的tabs：点菜、订单、我的
      const icons = {
        menu: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM5Q0EzRkYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXJ9InJvdW5kIiBzdHJva2UtbGluZW1pZD0icm91bmQiPgogIDxwYXRoIGQ9Ik0zIDJ2N2MwIDEuMS45IDIgMiAyaDQgMiAyIDIgMi0ydi0yIi8+CiAgPHBhdGggZD0iTTcgMnYyMCIvPgogIDxwYXRoIGQ9Ik0yMSAxNVYyYXYwYTUgNSAwIDAgMC01IDV2NmMwIDEuMS45IDIgMiAyaDNabTAgNXY3Ii8+Cjwvc3ZnPg==',
        menuActive: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVtaWQ9InJvdW5kIj4KICA8cGF0aCBkPSJNMzAydjdhMCAxLjEuOSAyIDIgMmg0YTIgMiAwIDAgMCAyLTIiLz4KICA8cGF0aCBkPSJNNyAydjIwIi8+CiAgPHBhdGggZD0iTTIxIDE1VjJhdjBhNSA1IDAgMCAwLTUgNXY2YzAuMSAxLjEuOSAyIDIgMmgzWm0wIDV2NyIvPgo8L3N2Zz4=',
        order: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM5Q0EzRkYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZW1pZD0icm91bmQiPgogIDxwYXRoIGQ9Ik02IDIgMyA2djE0YTIgMiAwIDAgMCAyIDJoMTRhMiAyIDAgMCAwIDItMnYtNEwtMy00WiIvPgogIDxwYXRoIGQ9Ik0zIDZoMTgiLz4KICA8cGF0aCBkPSJNMTYgMTBhNCA0IDAgMSAxLTggMCIvPgo8L3N2Zz4=',
        orderActive: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVtaWQ9InJvdW5kIj4KICA8cGF0aCBkPSJNNiAyIDMgNnYxNGEyIDIgMCAwIDAgMiAyaDE0YTIgMiAwIDAgMCAyLTJ2LTRsLTMtNFoiLz4KICA8cGF0aCBkPSJNMyA2aDE4Ii8+CiAgPHBhdGggZD0iTTE2IDEwYTIgMiAwIDAgMS04IDAiLz4KPC9zdmc+',
        profile: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM5Q0EzRkYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZW1pZD0icm91bmQiPgogIDxjaXJjbGUgY3g9IjEyIiBjeT0iOCIgcj0iNSIvPgogIDxwYXRoIGQ9Ik0yMCAyMWE4IDggMCAwIDAtMTYgMCIvPgo8L3N2Zz4=',
        profileActive: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVtaWQ9InJvdW5kIj4KICA8Y2lyY2xlIGN4PSIxMiIgY3k9IjgiIHI9IjUiLz4KICA8cGF0aCBkPSJNMjAgMjFhOCA4IDAgMCAwLTE2IDAiLz4KPC9zdmc+'
      };

      const tabs = [
        { pagePath: '/pages/menu/menu', text: '点菜', iconPath: '/images/tab/menu.svg', selectedIconPath: '/images/tab/menu_active.svg' },
        { pagePath: '/pages/order/order', text: '订单', iconPath: '/images/tab/order.svg', selectedIconPath: '/images/tab/order_active.svg' },
        { pagePath: '/pages/profile/profile', text: '我的', iconPath: '/images/tab/profile.svg', selectedIconPath: '/images/tab/profile_active.svg' }
      ];

      this.setData({
        tabs
      }, () => {
        this.updateCurrentTab();
      });

      console.log('Tabs initialized:', tabs);
    },

    // 更新当前高亮的tab
    updateCurrentTab() {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const currentPath = '/' + (currentPage ? currentPage.route : 'pages/menu/menu');
      console.log('Update current tab, path:', currentPath);

      const tabs = this.data.tabs;
      let currentIndex = 0;
      for (let i = 0; i < tabs.length; i++) {
        if (tabs[i].pagePath === currentPath) {
          currentIndex = i;
          break;
        }
      }

      console.log('Current tab index:', currentIndex);
      this.setData({ current: currentIndex });
    },

    // 监听路由变化
    watchRouteChange() {
      const originalSwitchTab = wx.switchTab;
      const self = this;

      wx.switchTab = function(options) {
        console.log('switchTab to:', options.url);
        // 先调用原始方法
        originalSwitchTab.apply(this, arguments);
        // 延迟更新tab高亮，确保页面已切换
        setTimeout(() => {
          self.updateCurrentTab();
        }, 100);
      };
    },

    onTabTap(e) {
      const index = e.currentTarget.dataset.index;
      const tab = this.data.tabs[index];
      if (tab) {
        wx.switchTab({ url: tab.pagePath });
      }
    }
  }
});
