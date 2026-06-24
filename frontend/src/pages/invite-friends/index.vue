<template>
  <view class="invite-container">
    <!-- Header with Poster -->
    <view class="poster-card">
      <view class="poster-content">
        <image class="poster-bg" src="../../static/invite-bg.jpg" mode="aspectFill"></image>
        <view class="poster-info">
          <image class="poster-avatar" :src="profile.avatar || DEFAULT_AVATAR" mode="aspectFill"></image>
          <text class="poster-name">{{ profile.nickname }}</text>
          <text class="poster-desc">我在红小鸭相亲交友，快来寻找你的另一半吧！</text>
          <view class="qr-box">
            <text class="ri-qr-code-line qr-icon-large"></text>
            <text class="qr-tips">扫码加入，送100鸭蛋</text>
          </view>
        </view>
      </view>
      <view class="poster-actions">
        <button class="invite-now-btn" @click="savePoster">立即邀请</button>
        <button class="share-poster-btn" @click="handleForward">
          <text class="ri-share-forward-line"></text>
          <text>转发好友</text>
        </button>
      </view>
    </view>

    <!-- Invited Friends List -->
    <view class="friends-list-container">
      <view class="list-header">
        <text class="list-title">成功邀请的好友</text>
        <text class="list-subtitle">已邀请 {{ invitedFriends.length }} 人</text>
      </view>
      
      <view class="friends-list" v-if="invitedFriends.length > 0">
        <view class="friend-item" v-for="(item, index) in invitedFriends" :key="index">
          <image class="friend-avatar" :src="item.avatar" mode="aspectFill"></image>
          <view class="friend-info">
            <text class="friend-name">{{ item.nickname }}</text>
            <text class="friend-time">{{ item.registerTime }}</text>
          </view>
          <view class="friend-reward">
            <text class="reward-text">+100鸭蛋</text>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else>
        <text class="ri-user-add-line empty-icon"></text>
        <text class="empty-text">暂无邀请记录，快去邀请好友吧！</text>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { DEFAULT_AVATAR, getRandomAvatar } from '../../utils/mockData'

const profile = ref({
  nickname: '神秘鸭鸭',
  avatar: DEFAULT_AVATAR
})

const invitedFriends = ref([
  {
    nickname: '张三',
    avatar: getRandomAvatar(1, 'male'),
    registerTime: '2026-06-10 10:20'
  },
  {
    nickname: '李四',
    avatar: getRandomAvatar(2, 'female'),
    registerTime: '2026-06-09 15:30'
  },
  {
    nickname: '王五',
    avatar: getRandomAvatar(3, 'male'),
    registerTime: '2026-06-08 09:15'
  }
])

const fetchProfile = async () => {
  try {
    const res = await request({ url: '/profile/info', method: 'GET' })
    if (res) {
      profile.value.nickname = res.nickname || '神秘鸭鸭'
      profile.value.avatar = res.avatar || DEFAULT_AVATAR
    }
  } catch (e) {}
}

const savePoster = () => {
  uni.showToast({ title: '已保存到相册', icon: 'success' })
}

const handleForward = () => {
  uni.showActionSheet({
    itemList: ['转发到微信', '复制链接', '生成海报图片'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.showToast({ title: '请在微信中打开分享', icon: 'none' })
      } else if (res.tapIndex === 1) {
        uni.setClipboardData({
          data: 'https://hongxiaoya.com/invite?uid=' + (profile.value.nickname || ''),
          success: () => {
            uni.showToast({ title: '链接已复制', icon: 'success' })
          }
        })
      } else if (res.tapIndex === 2) {
        savePoster()
      }
    }
  })
}

onShow(() => {
  fetchProfile()
})
</script>

<style lang="scss" scoped>
.invite-container {
  min-height: 100vh;
  background-color: #F4F5F9;
  padding: 30rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

.poster-card {
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.poster-content {
  width: 100%;
  height: 800rpx;
  background-color: #F8F8F8;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
}

.poster-bg {
  width: 100%;
  height: 400rpx;
}

.poster-info {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 430rpx;
  background-color: #FFF;
  border-radius: 30rpx 30rpx 0 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 40rpx;
}

.poster-avatar {
  width: 120rpx; height: 120rpx;
  border-radius: 50%;
  border: 8rpx solid #FFF;
  margin-top: -60rpx;
  background-color: #EEE;
}

.poster-name {
  font-size: 36rpx; font-weight: bold;
  color: #333; margin-top: 10rpx;
}

.poster-desc {
  font-size: 24rpx; color: #666;
  margin-top: 10rpx; text-align: center;
}

.qr-box {
  display: flex; flex-direction: column; align-items: center;
  margin-top: 20rpx;
}

.qr-icon-large { font-size: 200rpx; color: #333; line-height: 1; }
.qr-tips { font-size: 24rpx; color: #999; margin-top: 10rpx; }

.poster-actions {
  width: 100%;
  margin-top: 30rpx;
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.invite-now-btn {
  flex: 1;
  background: linear-gradient(90deg, #8B70FF 0%, #6A61F8 100%);
  color: #FFF;
  border-radius: 45rpx;
  height: 90rpx; line-height: 90rpx;
  font-size: 32rpx;
  box-shadow: 0 6rpx 16rpx rgba(106, 97, 248, 0.3);
  margin: 0;
}
.invite-now-btn::after { border: none; }

.share-poster-btn {
  flex: 1;
  background: transparent;
  color: #6A61F8;
  border: 2rpx solid #6A61F8;
  border-radius: 45rpx;
  height: 90rpx;
  line-height: 86rpx;
  font-size: 32rpx;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  box-sizing: border-box;
}
.share-poster-btn::after { border: none; }

.friends-list-container {
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.list-subtitle {
  font-size: 24rpx;
  color: #999;
}

.friends-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.friend-item {
  display: flex;
  align-items: center;
}

.friend-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: #EEE;
  margin-right: 20rpx;
}

.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.friend-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.friend-time {
  font-size: 22rpx;
  color: #999;
}

.friend-reward {
  display: flex;
  align-items: center;
}

.reward-text {
  font-size: 26rpx;
  color: #FA8C16;
  font-weight: bold;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  color: #CCC;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
}
</style>
