<template>
  <view class="history-container">
    <view class="header">
      <text class="title">活动回顾</text>
    </view>
    <scroll-view scroll-y class="list-container">
      <view class="activity-card" v-for="act in list" :key="act.id">
        <view class="card-cover-wrap">
          <image class="card-cover" :src="act.cover" mode="aspectFill"></image>
          <view class="status-badge status-2">已结束</view>
        </view>
        <view class="act-info">
          <view class="act-title">{{ act.name }}</view>
          <view class="meta-item">
            <text class="ri-calendar-check-line"></text>
            <text>{{ act.time }}</text>
          </view>
          <view class="meta-item">
            <text class="ri-user-heart-line"></text>
            <text>牵手成功 {{ act.successCount }} 对</text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-if="list.length === 0">
        <text class="ri-history-line empty-icon"></text>
        <text class="empty-text">暂无历史活动</text>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'

const list = ref([
  {
    id: 3,
    name: '520特别场：遇见你的那个TA',
    cover: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
    time: '2026.05.20',
    successCount: 5
  },
  {
    id: 4,
    name: '名企精英专场相亲会',
    cover: '/static/activities/%E6%B4%BB%E5%8A%A802.jpg',
    time: '2026.04.15',
    successCount: 3
  }
])
</script>

<style lang="scss" scoped>
.history-container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 30rpx;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.list-container {
  height: calc(100vh - 120rpx);
}

.activity-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
}

.card-cover-wrap {
  position: relative;
  height: 240rpx;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #FFFFFF;
  background-color: #999999;
}

.act-info {
  padding: 20rpx;
}

.act-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 10rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #666;
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  
  text {
    margin-right: 10rpx;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
  
  .empty-icon {
    font-size: 100rpx;
    color: #DDDDDD;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999999;
  }
}
</style>
