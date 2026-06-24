import { createSSRApp } from "vue";
import App from "./App.vue";
import { shareMixin } from "./utils/share";
import { initCustomPopupGlobal } from "./utils/customPopup";

// 初始化全局自定义弹窗，覆盖 uni.showModal 和 uni.showActionSheet
initCustomPopupGlobal();

export function createApp() {
  const app = createSSRApp(App);
  app.mixin(shareMixin);
  return {
    app,
  };
}
