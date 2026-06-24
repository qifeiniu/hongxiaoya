<template>
  <view class="splash-page">
    <view class="soft-vignette"></view>
    <view class="grain-layer"></view>
    <view class="brand-wrap">
      <view class="logo-shell">
        <image class="brand-logo" src="/static/hongxiaoya-logo.png" mode="aspectFit"></image>
      </view>
      <text class="brand-name">红小鸭</text>
      <text class="brand-subtitle">靠谱单身⻘年脱单平台</text>
    </view>

      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { globalAuthGuard } from '../../utils/auth'

onMounted(() => {
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' })
  }, 3000)
})
</script>

<style lang="scss" scoped>
.splash-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.78) 0%, rgba(255, 255, 255, 0) 42%),
    radial-gradient(ellipse at 50% 6%, rgba(255, 255, 255, 0.96) 0%, rgba(255, 255, 255, 0) 42%),
    linear-gradient(155deg, #fbf9ff 0%, #f3f0ff 44%, #eeefff 100%);
}

.soft-vignette {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 16% 20%, rgba(106, 97, 248, 0.08) 0, rgba(106, 97, 248, 0) 260rpx),
    radial-gradient(circle at 84% 76%, rgba(255, 94, 125, 0.055) 0, rgba(255, 94, 125, 0) 300rpx),
    linear-gradient(180deg, rgba(106, 97, 248, 0) 0%, rgba(106, 97, 248, 0.05) 100%);
}

.grain-layer {
  position: absolute;
  inset: 0;
  opacity: 0.42;
  background-image:
    repeating-linear-gradient(90deg, rgba(106, 97, 248, 0.025) 0 1rpx, transparent 1rpx 18rpx),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.42) 0 1rpx, transparent 1rpx 20rpx);
  mask-image: linear-gradient(180deg, transparent 0%, #000 20%, #000 82%, transparent 100%);
}

.brand-wrap {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateY(-34rpx);
  animation: brandIn 720ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.logo-shell {
  width: 218rpx;
  height: 218rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo {
  width: 168rpx;
  height: 168rpx;
  border-radius: 40rpx;
  overflow: hidden;
}

.brand-name {
  margin-top: 40rpx;
  font-size: 46rpx;
  line-height: 1.2;
  font-weight: 800;
  color: $uni-text-color;
  letter-spacing: 0;
  text-align: center;
}

.brand-subtitle {
  margin-top: 18rpx;
  font-size: 26rpx;
  color: #77758c;
}

@keyframes brandIn {
  from {
    opacity: 0;
    transform: translateY(10rpx) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(-34rpx) scale(1);
  }
}
</style>
