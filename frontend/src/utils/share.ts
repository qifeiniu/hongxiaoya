export const shareMixin = {
  onLoad() {
    // #ifdef MP-WEIXIN
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    });
    // #endif
  },
  onShareAppMessage(res: any) {
    return {
      title: '红小鸭相亲交友',
      path: '/pages/index/index',
      imageUrl: '/static/hongxiaoya-logo.png'
    };
  },
  onShareTimeline() {
    return {
      title: '红小鸭相亲交友',
      query: '',
      imageUrl: '/static/hongxiaoya-logo.png'
    };
  }
};
