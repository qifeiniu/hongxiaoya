<template>
  <view class="container">
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="ri-arrow-left-s-line"></text>
      </view>
      <text class="title">漂流瓶记录</text>
      <view class="right-placeholder"></view>
    </view>

    <!-- 选项卡 -->
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 1 }" 
        @click="switchTab(1)"
      >
        <text>我扔的</text>
        <view class="active-line" v-if="currentTab === 1"></view>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 2 }" 
        @click="switchTab(2)"
      >
        <text>我捞的</text>
        <view class="active-line" v-if="currentTab === 2"></view>
      </view>
    </view>

    <!-- 列表区域 -->
    <scroll-view class="list-scroll" scroll-y @scrolltolower="loadMore">
      <view class="record-list" v-if="records.length > 0">
        <view class="record-item" v-for="item in records" :key="item.id">
          <view class="item-header">
            <text class="time">{{ formatTime(currentTab === 1 ? item.createdAt : item.pickedAt) }}</text>
            <text class="status" :class="item.status === 1 ? 'picked' : 'unpicked'" v-if="currentTab === 1">
              {{ item.status === 1 ? '已被捞起' : '漂流中' }}
            </text>
          </view>
          
          <view class="content">
            <text>{{ item.content }}</text>
          </view>

          <!-- 对方信息区域 -->
          <view class="target-info" v-if="item.targetUserId">
            <view class="user-info">
              <image class="avatar" :src="item.targetAvatar" mode="aspectFill"></image>
              <view class="name-wrap">
                <text class="nickname">{{ item.targetNickname }}</text>
                <text class="user-id-small">{{ item.targetUserId }}</text>
              </view>
              <text class="action-text">{{ currentTab === 1 ? '捞起了你的瓶子' : '扔出的瓶子' }}</text>
            </view>
            <button class="chat-btn" @click="goChat(item)">私聊</button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else-if="!loading">
        <text class="ri-inbox-archive-line empty-icon"></text>
        <text class="empty-text">暂无相关记录</text>
      </view>

      <view class="loading-state" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { request } from '../../utils/request'
import { checkAuth } from '../../utils/auth'

const currentTab = ref(1) // 1-我扔的, 2-我捞的
const records = ref<any[]>([])
const loading = ref(false)

const goBack = () => {
  uni.navigateBack()
}

const switchTab = (tab: number) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  fetchRecords()
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: `/bottle/records?type=${currentTab.value}`,
      method: 'GET'
    })
    
    // 兼容返回格式可能包裹在 data 中
    let data = res
    if (res && res.code && res.data) {
      data = res.data
    }
    
    records.value = data || []
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  // 简化的分页逻辑，暂时不实现真正的分页
}

const goChat = async (item: any) => {
  if (!item.targetUserId) return
  if (!(await checkAuth('聊天'))) return
  uni.navigateTo({
    url: `/pages/chat/index?id=${item.targetUserId}&name=${item.targetNickname}`
  })
}

const formatTime = (isoString: string) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(() => {
  fetchRecords()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background-color: $uni-bg-color;
  padding-top: var(--status-bar-height, 44px);
  position: sticky;
  top: 0;
  z-index: 100;

  .back-btn {
    font-size: 48rpx;
    color: $uni-text-color;
    padding: 10rpx;
    margin-left: -10rpx;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: $uni-text-color;
  }

  .right-placeholder {
    width: 68rpx;
  }
}

.tabs {
  display: flex;
  background-color: $uni-bg-color;
  padding: 0 40rpx;
  margin-bottom: 20rpx;

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 0;
    font-size: 30rpx;
    color: $uni-text-color-regular;
    position: relative;

    &.active {
      color: $uni-color-primary;
      font-weight: 500;
    }

    .active-line {
      position: absolute;
      bottom: 0;
      width: 40rpx;
      height: 6rpx;
      background-color: $uni-color-primary;
      border-radius: 3rpx;
    }
  }
}

.list-scroll {
  flex: 1;
  height: 0;
}

.record-list {
  padding: 0 20rpx 40rpx;
}

.record-item {
  background-color: $uni-bg-color;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .time {
      font-size: 24rpx;
      color: $uni-text-color-grey;
    }

    .status {
      font-size: 24rpx;
      padding: 4rpx 16rpx;
      border-radius: 20rpx;

      &.picked {
        background-color: $uni-color-primary-light;
        color: $uni-color-primary;
      }

      &.unpicked {
        background-color: rgba(0, 191, 255, 0.1);
        color: #00BFFF;
      }
    }
  }

  .content {
    font-size: 28rpx;
    color: $uni-text-color;
    line-height: 1.6;
    margin-bottom: 30rpx;
    word-break: break-all;
  }

  .target-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 24rpx;
    border-top: 1rpx solid $uni-border-color;

    .user-info {
      display: flex;
      align-items: center;

      .avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 16rpx;
      }

      .name-wrap {
        display: flex;
        align-items: baseline;
        margin-right: 12rpx;
      }

      .nickname {
        font-size: 26rpx;
        color: $uni-text-color;
      }

      .user-id-small {
        font-size: 20rpx;
        color: $uni-text-color-grey;
        margin-left: 8rpx;
        font-weight: normal;
      }

      .action-text {
        font-size: 24rpx;
        color: $uni-text-color-grey;
      }
    }

    .chat-btn {
      margin: 0;
      padding: 0 30rpx;
      height: 56rpx;
      line-height: 56rpx;
      font-size: 24rpx;
      color: #FFFFFF;
      background: $uni-color-primary-gradient;
      border-radius: 28rpx;

      &::after {
        border: none;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;

  .empty-icon {
    font-size: 120rpx;
    color: $uni-text-color-placeholder;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: $uni-text-color-grey;
  }
}

.loading-state {
  text-align: center;
  padding: 30rpx;
  font-size: 24rpx;
  color: $uni-text-color-grey;
}
</style>
