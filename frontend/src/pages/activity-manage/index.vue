<template>
  <view class="manage-container">
    <view class="header-card">
      <view class="act-title">{{ actInfo.name }}</view>
      <view class="act-meta">
        <text class="ri-time-line"></text>
        <text>{{ actInfo.time }}</text>
      </view>
      <view class="act-meta">
        <text class="ri-money-cny-circle-line"></text>
        <text>报名费：¥{{ actInfo.price }}</text>
      </view>
      <view class="act-meta">
        <text class="ri-user-line"></text>
        <text>已报名：{{ members.length }}人</text>
      </view>
      <button class="cancel-act-btn" @click="handleCancelActivity" v-if="actInfo.status === 0">
        取消活动并退款
      </button>
      <view class="canceled-tag" v-if="actInfo.status === -1">
        活动已取消
      </view>
    </view>

    <view class="section-title">报名人员管理</view>
    <view class="member-list">
      <view class="member-card" v-for="member in members" :key="member.id">
        <image class="avatar" :src="member.avatar" mode="aspectFill"></image>
        <view class="member-info">
          <view class="name-row">
            <text class="name">{{ member.name }}</text>
            <text class="gender" :class="member.gender === 1 ? 'male' : 'female'">
              <text :class="member.gender === 1 ? 'ri-men-line' : 'ri-women-line'"></text>
            </text>
          </view>
          <view class="status-row">
            <text class="status-text" :class="'status-' + member.status">{{ getStatusText(member.status) }}</text>
          </view>
        </view>

        <view class="actions" v-if="actInfo.status !== -1">
          <template v-if="member.status === 'pending'">
            <button class="action-btn approve" @click="handleApprove(member)">通过</button>
            <button class="action-btn reject" @click="handleReject(member)">拒绝并退款</button>
          </template>
          <template v-if="member.status === 'refund_pending'">
            <button class="action-btn refund" @click="handleRefund(member)">同意退款</button>
          </template>
        </view>
      </view>
      
      <view class="empty-state" v-if="members.length === 0">
        <text class="empty-text">暂无报名人员</text>
      </view>
    </view>

    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const actId = ref('')

const actInfo = ref({
  id: '',
  name: '周末同城单身派对',
  time: '2026/08/10 20:00',
  price: 99,
  status: 0 // 0: Upcoming, -1: Canceled
})

const members = ref([
  {
    id: 1,
    name: '张三',
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
    gender: 1,
    status: 'pending'
  },
  {
    id: 2,
    name: '李四',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100',
    gender: 2,
    status: 'approved'
  },
  {
    id: 3,
    name: '王五',
    avatar: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100',
    gender: 1,
    status: 'refund_pending'
  }
])

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'refund_pending': '申请退款中',
    'refunded': '已退款'
  }
  return map[status] || '未知'
}

onLoad((options: any) => {
  if (options && options.id) {
    actId.value = options.id
    // Mock fetch activity info based on ID
  }
})

const handleApprove = (member: any) => {
  uni.showModal({
    title: '确认通过',
    content: `确定通过 ${member.name} 的报名申请吗？`,
    success: (res) => {
      if (res.confirm) {
        member.status = 'approved'
        uni.showToast({ title: '已通过', icon: 'success' })
      }
    }
  })
}

const handleReject = (member: any) => {
  uni.showModal({
    title: '确认拒绝',
    content: `拒绝后将自动退还报名费给 ${member.name}，是否继续？`,
    success: (res) => {
      if (res.confirm) {
        member.status = 'refunded'
        uni.showToast({ title: '已拒绝并退款', icon: 'success' })
      }
    }
  })
}

const handleRefund = (member: any) => {
  uni.showModal({
    title: '同意退款',
    content: `确认退还报名费给 ${member.name} 吗？`,
    success: (res) => {
      if (res.confirm) {
        member.status = 'refunded'
        uni.showToast({ title: '已退款', icon: 'success' })
      }
    }
  })
}

const handleCancelActivity = () => {
  uni.showModal({
    title: '取消活动',
    content: '取消活动后，所有已支付的报名费将原路退还给用户，此操作不可恢复，确定取消吗？',
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '正在取消并退款...' })
        setTimeout(() => {
          actInfo.value.status = -1
          members.value.forEach(m => {
            if (['pending', 'approved', 'refund_pending'].includes(m.status)) {
              m.status = 'refunded'
            }
          })
          uni.hideLoading()
          uni.showToast({ title: '活动已取消，退款已发放', icon: 'none' })
        }, 1500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.manage-container {
  min-height: 100vh;
  background-color: #F8F9FA;
  padding: 30rpx;
}

.header-card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);

  .act-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .act-meta {
    font-size: 28rpx;
    color: #666;
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;

    text {
      margin-right: 12rpx;
    }
  }

  .cancel-act-btn {
    margin-top: 30rpx;
    background-color: #FFF1F0;
    color: #FF4D4F;
    border: 2rpx solid #FFA39E;
    border-radius: 12rpx;
    font-size: 28rpx;
    line-height: 2.4;
  }

  .canceled-tag {
    margin-top: 30rpx;
    display: inline-block;
    padding: 10rpx 20rpx;
    background-color: #F5F5F5;
    color: #999;
    border-radius: 8rpx;
    font-size: 28rpx;
  }
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.member-card {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;

  .avatar {
    width: 90rpx;
    height: 90rpx;
    border-radius: 50%;
    margin-right: 20rpx;
  }

  .member-info {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        margin-right: 10rpx;
      }

      .gender {
        font-size: 24rpx;
        &.male { color: #1890FF; }
        &.female { color: #EB2F96; }
      }
    }

    .status-row {
      font-size: 24rpx;
      
      .status-text {
        padding: 4rpx 12rpx;
        border-radius: 6rpx;
        
        &.status-pending { background-color: #FFF7E6; color: #FA8C16; }
        &.status-approved { background-color: #F6FFED; color: #52C41A; }
        &.status-rejected { background-color: #F5F5F5; color: #999; }
        &.status-refund_pending { background-color: #E6F7FF; color: #13C2C2; }
        &.status-refunded { background-color: #F5F5F5; color: #999; }
      }
    }
  }

  .actions {
    display: flex;
    flex-direction: column;
    gap: 10rpx;

    .action-btn {
      margin: 0;
      padding: 0 20rpx;
      height: 50rpx;
      line-height: 50rpx;
      font-size: 24rpx;
      border-radius: 8rpx;

      &.approve { background-color: #6A61F8; color: #FFF; }
      &.reject { background-color: #FFF1F0; color: #FF4D4F; border: 1rpx solid #FFA39E; }
      &.refund { background-color: #13C2C2; color: #FFF; }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>