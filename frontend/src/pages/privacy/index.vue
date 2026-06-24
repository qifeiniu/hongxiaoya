<template>
  <view class="privacy-container">
    <view class="settings-list">
      <view class="setting-item">
        <view class="setting-info">
          <text class="title">隐身模式</text>
          <text class="desc">开启后，将隐藏你的地理位置和距离信息</text>
        </view>
        <switch :checked="invisibleMode" color="#6A61F8" @change="(e: any) => invisibleMode = e.detail.value" />
      </view>

      <view class="setting-item">
        <view class="setting-info">
          <text class="title">隐藏在线状态</text>
          <text class="desc">开启后，其他用户将无法看到你的在线状态</text>
        </view>
        <switch :checked="hideOnline" color="#6A61F8" @change="(e: any) => hideOnline = e.detail.value" />
      </view>
    </view>
    
    <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const invisibleMode = ref(false)
const hideOnline = ref(false)
</script>

<style lang="scss" scoped>
.privacy-container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 30rpx 0;
}

.settings-list {
  background-color: #FFFFFF;
  padding: 0 30rpx;
  margin-bottom: 40rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 2rpx solid #F5F5F5;
  
  &:last-child {
    border-bottom: none;
  }
}

.setting-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  margin-right: 30rpx;
}

.title {
  font-size: 30rpx;
  color: #333333;
  margin-bottom: 10rpx;
}

.desc {
  font-size: 24rpx;
  color: #999999;
}
</style>
