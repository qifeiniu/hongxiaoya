<template>
  <view class="joined-container">
    <scroll-view scroll-y class="list-container">
      <view class="activity-card" v-for="act in list" :key="act.id">
        <!-- Cover Image -->
        <view class="card-cover-wrap" @click="goRoom(act)">
          <image class="card-cover" :src="act.coverImg || act.cover || '/static/logo.png'" mode="aspectFill"></image>
          <view class="status-badge" :class="'status-' + act.status">
            {{ getStatusText(act.status) }}
          </view>
        </view>

        <!-- Matchmaker Info -->
        <view class="matchmaker-info">
          <image class="mm-avatar" :src="act.matchmaker?.avatar || '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg'" mode="aspectFill"></image>
          <text class="mm-name">{{ act.matchmaker?.name || '资深红娘' }}</text>
          <view class="joined-pill">
            已报{{ act.joinedCount || 0 }}
            <text class="ri-arrow-right-s-line"></text>
          </view>
        </view>

        <view class="act-title">{{ act.name || '神秘活动' }}</view>

        <!-- Tags -->
        <view class="tags-row" v-if="act.tags && act.tags.length > 0">
          <view class="tag-item" v-for="(tag, idx) in act.tags" :key="idx">{{ tag }}</view>
        </view>

        <!-- Enrollment Conditions -->
        <view class="conditions-box">
          <view class="condition-content">
            <text class="condition-text" :class="{ 'expanded': act.isExpanded }">
              <text class="condition-label">报名条件：</text>{{ act.conditions || act.requirements || '京户京房专场/体制内专场/硕博专场' }}
            </text>
            <view class="toggle-btn-wrap" :class="{ 'expanded': act.isExpanded }" @click.stop="toggleExpand(act)" v-if="getConditionLength(act) > 40">
              <text class="toggle-btn">{{ act.isExpanded ? '收起' : '展开' }}</text>
              <text :class="act.isExpanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'" class="toggle-icon"></text>
            </view>
          </view>
        </view>

        <view class="meta">
          <view class="meta-item">
            <text class="meta-icon ri-time-line"></text>
            <text class="meta-text">{{ formatTime(act.startTime) || act.time }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon ri-money-cny-circle-line"></text>
            <text class="meta-text price">¥{{ act.price || 0 }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon ri-team-line"></text>
            <text class="meta-text">男 {{ act.maleJoined || 0 }}/{{ act.maleQuota || 10 }} · 女 {{ act.femaleJoined || 0 }}/{{ act.femaleQuota || 10 }}</text>
          </view>
        </view>

        <view class="progress-row">
          <progress :percent="getPercent(act)" activeColor="#6B5EF7" stroke-width="4" border-radius="4" />
          <text class="percent">{{ getPercent(act) }}%</text>
        </view>

        <view class="action-buttons" v-if="act.status === 0">
          <button class="secondary-btn refund-btn" @click="handleApplyRefund(act)" v-if="!act.refundStatus">申请退款</button>
          <button class="secondary-btn disabled-btn" disabled v-else-if="act.refundStatus === 'pending'">退款审核中</button>
          <button class="secondary-btn disabled-btn" disabled v-else-if="act.refundStatus === 'refunded'">已退款</button>
          <button class="primary-btn disabled-btn" disabled>活动未开始</button>
        </view>
        <button
          v-else-if="act.status === 1"
          class="primary-btn enter-btn"
          @click="goRoom(act)"
        >进入房间</button>
        <button
          v-else-if="act.status === -1"
          class="primary-btn disabled-btn"
          disabled
        >活动已取消</button>
        <button
          v-else
          class="primary-btn disabled-btn"
          disabled
        >活动已结束</button>
      </view>
      
      <view class="empty-state" v-if="list.length === 0">
        <text class="ri-calendar-event-line empty-icon"></text>
        <text class="empty-text">暂无报名的活动</text>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'

const list = ref<any[]>([
  {
    id: 1,
    name: '周末同城单身交友派对',
    cover: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
    status: 1, // 进行中
    time: '开始时间 2026/8/10 20:00',
    matchmaker: { avatar: '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg', name: '资深红娘-小雅' },
    joinedCount: 12,
    tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
    startTime: new Date('2026/08/10 20:00:00').getTime(),
    price: 99,
    maleJoined: 6,
    maleQuota: 10,
    femaleJoined: 6,
    femaleQuota: 10,
    conditions: '京户京房专场/体制内专场/硕博专场，有京房/户|名校| 硕博学历 | 互联网、金融、国央企、名企精英 | 年收入30W+'
  },
  {
    id: 2,
    name: '金融IT行业专场',
    cover: '/static/activities/%E6%B4%BB%E5%8A%A802.jpg',
    status: 0, // 未开始
    time: '开始时间 2026/8/10 20:00',
    matchmaker: { avatar: '/static/avatars/stefan-stefancik-QXevDflbl8A-unsplash.jpg', name: '红娘老师-张姐' },
    joinedCount: 15,
    tags: ['上海', '高学历', '精英场'],
    startTime: new Date('2026/08/10 20:00:00').getTime(),
    price: 199,
    maleJoined: 8,
    maleQuota: 15,
    femaleJoined: 7,
    femaleQuota: 15,
    conditions: '名校硕博学历 | 互联网、金融、国央企精英 | 年收入40W+'
  }
])

const getStatusText = (status: number) => {
  if (status === 1) return '正在进行'
  if (status === 0) return '即将开始'
  if (status === 2) return '已结束'
  return '未知状态'
}

const getConditionLength = (act: any) => {
  const text = act.conditions || act.requirements || '京户京房专场/体制内专场/硕博专场'
  return text.length
}

const toggleExpand = (act: any) => {
  act.isExpanded = !act.isExpanded
}

const formatTime = (t: number) => {
  if (!t) return ''
  const d = new Date(t)
  return `开始时间 ${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

const handleApplyRefund = (act: any) => {
  uni.showModal({
    title: '申请退款',
    content: '确定要取消报名并申请退款吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交申请中...' })
        setTimeout(() => {
          act.refundStatus = 'pending'
          uni.hideLoading()
          uni.showToast({ title: '退款申请已提交', icon: 'success' })
        }, 800)
      }
    }
  })
}

const getPercent = (act: any) => {
  const total = Number(act?.maleQuota || 0) + Number(act?.femaleQuota || 0)
  const joined = Number(act?.joinedCount || 0)
  if (!total) return 0
  const p = Math.round((joined / total) * 100)
  if (p < 0) return 0
  if (p > 100) return 100
  return p
}

const goRoom = (act: any) => {
  uni.navigateTo({ url: `/pages/activity-room/index?id=${act.id}` })
}
</script>

<style lang="scss" scoped>
.joined-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  padding: 24rpx 32rpx;
  box-sizing: border-box;
}

.list-container {
  height: 100vh;
}

.activity-card {
  background-color: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: $uni-shadow-base;
  display: flex;
  flex-direction: column;
}

.card-cover-wrap {
  position: relative;
  width: 100%;
  height: 280rpx;
  border-radius: $uni-border-radius-base;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 22rpx;
  font-weight: bold;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.status-1 {
  background-color: $uni-color-accent;
}

.status-0 {
  background-color: $uni-color-primary;
}

.status-2 {
  background-color: $uni-text-color-grey;
}

.matchmaker-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.mm-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.mm-name {
  font-size: 26rpx;
  color: $uni-text-color-regular;
  flex: 1;
}

.joined-pill {
  background-color: $uni-bg-color-page;
  padding: 8rpx 16rpx;
  border-radius: $uni-border-radius-pill;
  color: $uni-text-color-regular;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.act-title {
  font-size: 32rpx;
  font-weight: 800;
  color: $uni-text-color;
  margin-bottom: 16rpx;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tag-item {
  font-size: 22rpx;
  color: $uni-color-primary;
  background-color: $uni-color-primary-light;
  padding: 4rpx 16rpx;
  border-radius: $uni-border-radius-sm;
}

.conditions-box {
  margin-bottom: 20rpx;
  padding: 0 4rpx;
}

.condition-label {
  font-size: 26rpx;
  font-weight: bold;
  color: $uni-text-color;
}

.condition-content {
  position: relative;
  margin-top: 6rpx;
  line-height: 40rpx;
}

.condition-text {
  font-size: 26rpx;
  color: $uni-text-color-regular;
  line-height: 40rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
  transition: all 0.3s;
  
  &.expanded {
    -webkit-line-clamp: unset;
    display: block;
  }
}

.toggle-btn-wrap {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  padding-left: 60rpx;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, #ffffff 30%, #ffffff 100%);
  
  &.expanded {
    position: relative;
    background: transparent;
    justify-content: flex-end;
    padding-left: 0;
    margin-top: 4rpx;
  }
}

.toggle-btn {
  font-size: 24rpx;
  color: $uni-color-primary;
}

.toggle-icon {
  font-size: 26rpx;
  color: $uni-color-primary;
  margin-left: 4rpx;
}

.meta {
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-base;
  padding: 16rpx 18rpx;
  margin-bottom: 18rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: $uni-text-color-regular;
}

.meta-item + .meta-item {
  margin-top: 12rpx;
}

.meta-icon {
  width: 44rpx;
  color: $uni-text-color-grey;
  font-size: 28rpx;
}

.meta-text {
  flex: 1;
  color: $uni-text-color-regular;
}

.price {
  color: $uni-color-primary;
  font-weight: 800;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 18rpx;
  
  progress, uni-progress {
    flex: 1;
  }
}

.percent {
  font-size: 22rpx;
  color: $uni-text-color-grey;
  width: 72rpx;
  text-align: right;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  width: 100%;
}

.secondary-btn {
  flex: 1;
  margin: 0;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 28rpx;
  font-weight: 800;
  background: #FFF;
  color: #6B5EF7;
  border: 2rpx solid #6B5EF7;
}
.secondary-btn::after {
  border: none;
}
.secondary-btn.disabled-btn {
  background: #F5F5F5;
  color: #999;
  border-color: #E5E5E5;
}

.primary-btn {
  flex: 1;
  width: 100%;
  margin: 0;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 28rpx;
  font-weight: 800;
  background: $uni-color-primary-gradient;
  color: $uni-bg-color;
  box-shadow: $uni-shadow-primary;
}

.primary-btn::after {
  border: none;
}

.enter-btn {
  background: $uni-color-primary-gradient;
  box-shadow: $uni-shadow-primary;
}

.disabled-btn {
  background: $uni-text-color-disable;
  box-shadow: none;
  color: #fff;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
  
  .empty-icon {
    font-size: 100rpx;
    color: $uni-text-color-placeholder;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: $uni-text-color-placeholder;
  }
}
</style>
