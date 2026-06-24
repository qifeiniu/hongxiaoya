<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
onLaunch(() => {
  console.log("App Launch");
  console.log("未登录用户可浏览页面，互动功能由业务请求统一拦截");
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
</script>
<style lang="scss">
@import 'remixicon/fonts/remixicon.css';
/* 引入 HarmonyOS Sans SC 字体 (通过 jsDelivr CDN) */
@import url('https://cdn.jsdelivr.net/npm/harmonyos-sans-webfont-splitted@1.2.1/dist/HarmonyOS_Sans_SC/index.min.css');

/* 全局字体设置 */
html, body, page, view, text {
  font-family: 'HarmonyOS Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}

/* #ifdef H5 */
.uni-tabbar {
  padding-bottom: 0 !important;
}
.uni-tabbar__safe-area {
  display: none !important;
}
/* #endif */
</style>
