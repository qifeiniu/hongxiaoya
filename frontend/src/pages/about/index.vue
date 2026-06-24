<template>
  <view class="about-container">
    <view class="logo-section">
      <image class="logo" src="/static/hongxiaoya-logo.png" mode="aspectFit"></image>
      <text class="app-name">红小鸭交友</text>
      <text class="app-version">Version 1.0.0</text>
    </view>

    <view class="menu-list">
      <view class="menu-item" v-for="item in aboutActions" :key="item.title" @click="handleAction(item)">
        <view class="menu-left">
          <text :class="[item.icon, 'menu-icon']" :style="{ color: item.color }"></text>
          <text class="menu-text">{{ item.title }}</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const aboutActions = [
  { title: '证照公示', icon: 'ri-award-line', color: '#722ED1', action: 'license' },
  { title: '用户协议', icon: 'ri-file-text-line', color: '#333', action: 'user_agreement' },
  { title: '隐私政策', icon: 'ri-shield-keyhole-line', color: '#333', action: 'privacy_policy' },
  { title: '增值服务协议', icon: 'ri-service-line', color: '#333', action: 'vip_agreement' }
]

const handleAction = (item: any) => {
  uni.navigateTo({ url: `/pages/document/index?type=${item.action}` })
}
</script>

<style lang="scss" scoped>
.about-container {
  min-height: 100vh;
  background-color: #F4F5F9;
  padding: 30rpx 0;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  margin-bottom: 20rpx;
}

.app-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.app-version {
  font-size: 24rpx;
  color: #999;
}

.menu-list {
  background-color: #FFF;
  margin: 0 30rpx;
  border-radius: 24rpx;
  padding: 10rpx 0;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  position: relative;
}

.menu-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30rpx;
  right: 30rpx;
  height: 1rpx;
  background-color: #F5F5F5;
}

.menu-item:last-child::after {
  display: none;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.menu-right {
  display: flex;
  align-items: center;
}

.arrow {
  color: #CCC;
  font-size: 36rpx;
}
</style>
