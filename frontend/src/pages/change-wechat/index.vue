<template>
  <view class="container">
    <view class="wechat-info">
      <view class="icon-wrap">
        <text class="ri-wechat-fill wechat-icon"></text>
      </view>
      <text class="status">当前已绑定微信</text>
      <text class="tips">更换微信需要重新授权</text>
    </view>
    
    <view class="auth-btn" @click="handleAuth">
      <text class="ri-wechat-fill btn-icon"></text>
      <text>重新授权微信</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
const handleAuth = () => {
  uni.showLoading({ title: '授权中...' })
  // 模拟微信授权
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '微信更换成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}
</script>

<style lang="scss" scoped>
.container {
  padding: 100rpx 60rpx;
  background-color: #F4F5F9;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.wechat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;
}
.icon-wrap {
  width: 160rpx;
  height: 160rpx;
  background-color: #00B578;
  border-radius: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40rpx;
}
.wechat-icon {
  font-size: 80rpx;
  color: #fff;
}
.status {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 20rpx;
}
.tips {
  font-size: 26rpx;
  color: #999;
}
.auth-btn {
  width: 100%;
  height: 96rpx;
  background-color: #00B578;
  border-radius: 48rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}
.auth-btn .btn-icon {
  font-size: 40rpx;
  color: #fff;
  margin-right: 12rpx;
}
.auth-btn text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
}
</style>