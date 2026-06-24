<template>
  <view class="settings-container">
    <!-- 隐私管理 -->
    <view class="menu-list">
      <view class="menu-item" @click="handlePrivacySettings">
        <view class="menu-left">
          <text class="ri-eye-off-line menu-icon" style="color: #722ED1;"></text>
          <text class="menu-text">隐私管理</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
    </view>

    <!-- 账号管理 -->
    <view class="menu-list">
      <view class="menu-item" v-for="item in settingsActions" :key="item.title" @click="handleAction(item)">
        <view class="menu-left">
          <text :class="[item.icon, 'menu-icon']" :style="{ color: item.color }"></text>
          <text class="menu-text">{{ item.title }}</text>
        </view>
        <view class="menu-right">
          <text class="menu-extra" v-if="item.extra">{{ item.extra }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
    </view>

    <!-- 关于红小鸭 -->
    <view class="menu-list">
      <view class="menu-item" @click="handleAboutAction">
        <view class="menu-left">
          <text class="ri-information-line menu-icon" style="color: #1890FF;"></text>
          <text class="menu-text">关于红小鸭</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
    </view>
    
    <view class="logout-btn" @click="handleLogout" v-if="isLogin">
      <text>退出登录</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const isLogin = ref(false)
const cacheSize = ref('12.5MB')

onShow(() => {
  isLogin.value = !!uni.getStorageSync('token')
})

const handlePrivacySettings = () => {
  uni.navigateTo({ url: '/pages/privacy/index' })
}

const settingsActions = [
  { title: '手机号更换', icon: 'ri-smartphone-line', color: '#1890FF', action: 'change_phone' },
  { title: '微信更换', icon: 'ri-wechat-line', color: '#00B578', action: 'change_wechat' },
  { title: '清理缓存', icon: 'ri-delete-back-2-line', color: '#FFA940', action: 'clear_cache', extra: '12.5MB' },
  { title: '检查更新', icon: 'ri-download-2-line', color: '#52C41A', action: 'check_update' },
  { title: '帮助', icon: 'ri-questionnaire-line', color: '#52C41A', action: 'help' }
]

const handleAction = (item: any) => {
  if (item.action === 'change_phone') {
    uni.navigateTo({ url: '/pages/change-phone/index' })
    return
  }
  if (item.action === 'change_wechat') {
    uni.navigateTo({ url: '/pages/change-wechat/index' })
    return
  }
  if (item.action === 'clear_cache') {
    uni.showModal({
      title: '清理缓存',
      content: `当前缓存 ${cacheSize.value}，确定要清理吗？`,
      success: (res) => {
        if (res.confirm) {
          uni.showLoading({ title: '清理中...' })
          setTimeout(() => {
            cacheSize.value = '0KB'
            uni.hideLoading()
            uni.showToast({ title: '清理完成', icon: 'success' })
          }, 1000)
        }
      }
    })
    return
  }
  if (item.action === 'check_update') {
    uni.showLoading({ title: '检查中...' })
    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({ title: '已是最新版本', icon: 'none' })
    }, 1500)
    return
  }
  if (item.action === 'help') {
    uni.navigateTo({ url: '/pages/help/index' })
    return
  }
}

const handleAboutAction = () => {
  uni.navigateTo({ url: '/pages/about/index' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#6A61F8',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('isProfileComplete')
        isLogin.value = false
        uni.showToast({ title: '已退出登录', icon: 'none' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: 100vh;
  background-color: #F4F5F9;
  padding: 30rpx 0;
}

.menu-list {
  background-color: #FFF;
  margin: 0 30rpx 40rpx;
  border-radius: 24rpx;
  padding: 10rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
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
  left: 80rpx;
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

.menu-extra {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.logout-btn {
  margin: 0 30rpx 40rpx;
  background-color: #FFF;
  border-radius: 24rpx;
  height: 100rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.logout-btn text {
  color: #FF4D4F;
  font-size: 32rpx;
  font-weight: 500;
}
</style>
