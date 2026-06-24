<template>
  <view class="block-list-container">
    <view class="list-wrap" v-if="blockList.length > 0">
      <view class="block-item" v-for="user in blockList" :key="user.userId" @click="goUserDetail(user)">
        <view class="user-info">
          <image class="avatar" :src="user.avatar" mode="aspectFill"></image>
          <view class="info">
            <view class="name-id">
              <text class="nickname">{{ user.nickname || '神秘鸭鸭' }}</text>
              <text class="id-text">ID:{{ user.userId }}</text>
            </view>
            <view class="sub-info">
              <text v-if="user.age">{{ user.age }}岁</text>
              <text v-if="user.height"> · {{ user.height }}cm</text>
              <text v-if="user.education"> · {{ user.education }}</text>
              <text v-if="user.profession"> · {{ user.profession }}</text>
            </view>
          </view>
        </view>
        <view class="unblock-btn" @click.stop="handleUnblock(user)">解除屏蔽</view>
      </view>
    </view>
    
    <view class="empty-state" v-else>
      <text class="ri-forbid-line empty-icon"></text>
      <text class="empty-text">暂无屏蔽列表</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { request } from '../../utils/request'
import { onShow } from '@dcloudio/uni-app'
import { getRandomAvatar } from '../../utils/mockData'

const blockList = ref<any[]>([])

const fetchBlockList = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res: any = await request({ url: '/relation/block-list', method: 'GET' })
    if (res && res.length > 0) {
      blockList.value = res.map((item: any) => {
        const gender = item.gender === 1 ? 'male' : 'female'
        return {
          ...item,
          avatar: item.avatar || getRandomAvatar(item.userId, gender)
        }
      })
    } else {
      blockList.value = []
    }
  } catch (e) {
    blockList.value = []
  } finally {
    uni.hideLoading()
  }
}

const handleUnblock = (user: any) => {
  uni.showModal({
    title: '解除屏蔽',
    content: `确定解除对 ${user.nickname} 的屏蔽吗？`,
    confirmColor: '#6A61F8',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({ url: `/relation/unblock?targetId=${user.userId}`, method: 'POST' })
          uni.showToast({ title: '已解除屏蔽', icon: 'none' })
          fetchBlockList()
        } catch (e) {}
      }
    }
  })
}

const goUserDetail = (user: any) => {
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.userId}` })
}

onShow(() => {
  fetchBlockList()
})
</script>

<style lang="scss" scoped>
.block-list-container {
  min-height: 100vh;
  background-color: #F4F5F9;
  padding: 20rpx;
}

.list-wrap {
  background-color: #FFF;
  border-radius: 20rpx;
  padding: 0 30rpx;
}

.block-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.block-item:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  margin-right: 20rpx;
}

.avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #EEE;
  flex-shrink: 0;
}

.info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  gap: 8rpx;
}

.name-id {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.nickname {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  max-width: 240rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.id-text {
  font-size: 24rpx;
  color: #999;
}

.sub-info {
  font-size: 24rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unblock-btn {
  background-color: #F5F5F5;
  color: #666;
  font-size: 24rpx;
  padding: 10rpx 24rpx;
  border-radius: 30rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 100rpx;
  color: #DDD;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
