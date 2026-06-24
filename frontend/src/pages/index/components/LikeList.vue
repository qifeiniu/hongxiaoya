<template>
  <view class="like-list-container" :style="{ paddingTop: `calc(${statusBarHeight}px + 44px)` }">
    <scroll-view scroll-y class="user-list">
      <view class="grid-container">
        <view class="user-card" v-for="user in likedUsers" :key="user.userId" @click="goDetail(user)">
          <image class="bg-image" :src="user.avatar" mode="aspectFill"></image>
          <view class="card-content">
            <view class="user-info">
              <view class="name-wrap">
                <text class="user-name">{{ user.nickname }}</text>
                <text class="user-id-small">{{ user.userId }}</text>
              </view>
              <text class="user-meta">{{ user.height || 165 }}cm · {{ user.occupation || '自由职业' }} · {{ user.education || '本科' }}</text>
              <text class="user-meta highlight">刚刚在线</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="likedUsers.length === 0">
        <text class="ri-heart-line empty-icon"></text>
        <text class="empty-text">还没有喜欢的人</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getRandomAvatar } from '../../../utils/mockData'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0

const likedUsers = ref([
  { userId: 101, nickname: '温柔可人', age: 24, location: '上海', height: 165, occupation: '设计师', education: '本科', bio: '希望能遇到一个懂我的人', avatar: getRandomAvatar(101, 'female') },
  { userId: 103, nickname: '春风十里', age: 26, location: '北京', height: 168, occupation: '老师', education: '硕士', bio: '真诚、稳定，也保留一点浪漫', avatar: getRandomAvatar(103, 'female') }
])

const goDetail = (user: any) => {
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.userId}` })
}
</script>

<style lang="scss" scoped>
.like-list-container {
  flex: 1;
  background: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-list {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
}

.grid-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.user-card {
  width: calc(50% - 10rpx);
  height: 400rpx;
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.card-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 40%, rgba(0,0,0,0.8) 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  padding: 30rpx 20rpx;
  box-sizing: border-box;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.name-wrap {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
  justify-content: center;
}
.user-name { font-size: 28rpx; font-weight: 600; color: #fff; }
.user-id-small { font-size: 24rpx; color: rgba(255, 255, 255, 0.8); margin-left: 12rpx; font-weight: normal; }
.user-meta { font-size: 22rpx; color: rgba(255, 255, 255, 0.8); margin-bottom: 6rpx; text-align: center; }
.user-meta.highlight { color: #FFD700; }
.user-bio { font-size: 24rpx; color: rgba(255, 255, 255, 0.6); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; width: 100%; text-align: center; }

.empty-state { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; color: $uni-text-color-placeholder; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $uni-text-color-placeholder; }
</style>
