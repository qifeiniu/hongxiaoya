<template>
  <view class="page-container">
    <scroll-view scroll-y class="notification-list">
      <view class="noti-item" v-for="item in notifications" :key="item.id">
        <view class="noti-icon-wrap" :class="item.iconBg">
          <text :class="item.icon"></text>
        </view>
        <view class="noti-content">
          <view class="noti-header">
            <text class="noti-title">{{ item.title }}</text>
            <text class="noti-time">{{ item.time }}</text>
          </view>
          <text class="noti-desc">{{ item.desc }}</text>
        </view>
      </view>

      <view class="empty-state" v-if="notifications.length === 0">
        <text class="ri-notification-off-line empty-icon"></text>
        <text class="empty-text">暂无通知</text>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'

const notifications = ref([
  { id: 4, type: 'system', icon: 'ri-shield-check-fill', iconBg: 'bg-blue', title: '认证通过', desc: '你的实名认证已通过审核', time: '2小时前' },
  { id: 5, type: 'system', icon: 'ri-coin-fill', iconBg: 'bg-gold', title: '签到奖励', desc: '签到成功，获得10鸭蛋', time: '今天 08:00' },
  { id: 7, type: 'system', icon: 'ri-message-3-fill', iconBg: 'bg-primary', title: '系统通知', desc: '欢迎来到单身青年自救平台，期待你找到心仪的另一半。', time: '昨天' }
])
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
}

.notification-list {
  flex: 1;
  padding: 24rpx 32rpx;
  box-sizing: border-box;
  width: 100%;
}

.noti-item {
  display: flex;
  background: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: $uni-shadow-base;
}

.noti-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: $uni-border-radius-circle;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #fff;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.bg-blue { background: linear-gradient(135deg, #6B9DFF, #4081FF); }
.bg-gold { background: linear-gradient(135deg, #FFD700, #FFA500); }
.bg-primary { background: $uni-color-primary-gradient; }

.noti-content { flex: 1; }

.noti-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.noti-title { font-size: 32rpx; font-weight: 600; color: $uni-text-color; }
.noti-time { font-size: 24rpx; color: $uni-text-color-placeholder; }
.noti-desc { font-size: 28rpx; color: $uni-text-color-regular; display: block; line-height: 1.5; }

.empty-state {
  text-align: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  color: $uni-text-color-placeholder;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: $uni-text-color-placeholder;
}
</style>
