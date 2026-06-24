<template>
  <view class="page-container">
    <!-- Tab栏 -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: activeTab === 'liked' }" @click="activeTab = 'liked'">
        <text>我喜欢的</text>
        <view class="tab-line" v-if="activeTab === 'liked'"></view>
      </view>
      <view class="tab-item" :class="{ active: activeTab === 'disliked' }" @click="activeTab = 'disliked'">
        <text>不喜欢的</text>
        <view class="tab-line" v-if="activeTab === 'disliked'"></view>
      </view>
    </view>

    <scroll-view scroll-y class="user-list">
      <view class="grid-container">
        <view class="user-card" v-for="user in currentList" :key="user.userId" @click="goDetail(user)">
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
            <view class="action-area" v-if="activeTab === 'disliked'">
              <view class="restore-btn" @click.stop="restoreUser(user)">
                <text class="ri-refresh-line"></text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="currentList.length === 0">
        <text class="ri-heart-line empty-icon"></text>
        <text class="empty-text">{{ activeTab === 'liked' ? '还没有喜欢的人' : '没有不喜欢的人' }}</text>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRandomAvatar } from '../../utils/mockData'

const activeTab = ref('liked')

onLoad((options) => {
  if (options && options.tab) {
    activeTab.value = options.tab
  }
})

const likedUsers = ref([
  { userId: 101, nickname: '温柔可人', age: 24, location: '上海', height: 165, occupation: '设计师', education: '本科', bio: '希望能遇到一个懂我的人', avatar: getRandomAvatar(101, 'female') },
  { userId: 103, nickname: '春风十里', age: 26, location: '北京', height: 168, occupation: '老师', education: '硕士', bio: '真诚、稳定，也保留一点浪漫', avatar: getRandomAvatar(103, 'female') }
])

const dislikedUsers = ref([
  { userId: 104, nickname: '知性学姐', age: 27, location: '北京', height: 170, occupation: '医生', education: '博士', bio: '安静的夜晚，仰望星空', avatar: getRandomAvatar(104, 'female') }
])

const currentList = computed(() => {
  return activeTab.value === 'liked' ? likedUsers.value : dislikedUsers.value
})

const goDetail = (user: any) => {
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.userId}` })
}

const restoreUser = (user: any) => {
  uni.showModal({
    title: '取消不喜欢',
    content: `确定取消对 ${user.nickname} 的不喜欢标记吗？`,
    success: (res) => {
      if (res.confirm) {
        dislikedUsers.value = dislikedUsers.value.filter(u => u.userId !== user.userId)
        uni.showToast({ title: '已恢复', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
}

.tab-bar {
  display: flex;
  background: $uni-bg-color;
  padding: 0 40rpx;
  border-bottom: 1px solid $uni-bg-color-page;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 26rpx 0;
  font-size: 30rpx;
  color: $uni-text-color-grey;
  position: relative;
}

.tab-item.active {
  color: $uni-color-primary;
  font-weight: 600;
}

.tab-line {
  position: absolute;
  bottom: 0; left: 50%; transform: translateX(-50%);
  width: 48rpx; height: 6rpx;
  background: $uni-color-primary;
  border-radius: 3rpx;
}

.user-list {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  width: 100%;
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

.action-area {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

.restore-btn {
  width: 64rpx; height: 64rpx;
  border-radius: 50%;
  background: $uni-color-primary-light;
  display: flex; align-items: center; justify-content: center;
  font-size: 32rpx;
  color: $uni-color-primary;
}

.empty-state { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; color: $uni-text-color-placeholder; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $uni-text-color-placeholder; }
</style>
