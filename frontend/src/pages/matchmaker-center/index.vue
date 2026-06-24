<template>
  <view class="mm-center-container">
    <view class="header-card">
      <view class="mm-info">
        <image class="avatar" :src="profile.avatar" mode="aspectFill"></image>
        <view class="info-right">
          <view class="name-row">
            <text class="name">{{ profile.nickname || '红娘老师' }}</text>
            <view class="level-badge">
              <text class="icon">👑</text>
              <text>{{ currentLevel.name }}</text>
            </view>
          </view>
          <text class="desc">您的直播相亲活动分成比例为：{{ currentLevel.commission }}</text>
        </view>
      </view>
      
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-val">¥{{ wallet.commissionBalance || '0.00' }}</text>
          <text class="stat-label">可提现佣金</text>
        </view>
        <view class="stat-item">
          <text class="stat-val">{{ myActivities.length }}</text>
          <text class="stat-label">主持活动数</text>
        </view>
      </view>

      <view class="create-btn" @click="goCreateActivity">
        <text class="ri-add-line"></text>
        <text>创建相亲活动</text>
      </view>
    </view>

    <!-- Rule tips -->
    <view class="rules-card">
      <view class="rule-title">
        <text class="ri-information-line"></text>
        <text>红娘佣金规则</text>
      </view>
      <text class="rule-text">• 白银红娘：直播相亲活动分成30%</text>
      <text class="rule-text">• 黄金红娘：直播相亲活动分成33%</text>
      <text class="rule-text">• 钻石红娘：直播相亲活动分成35%</text>
      <text class="rule-text">• 提现规则：活动结束24小时后自动分配佣金，可自由提现至微信/支付宝/银行卡。</text>
    </view>

    <!-- Hosted activities (last 6 months) -->
    <view class="section-title">我主持过的活动（近6个月）</view>
    
    <view class="activity-list" v-if="myActivities.length > 0">
      <view class="act-card" v-for="act in myActivities" :key="act.id">
        <image class="act-cover" :src="act.coverImg || act.cover || '/static/logo.png'" mode="aspectFill"></image>
        <view class="act-info">
          <view class="act-name">{{ act.name }}</view>
          <view class="act-time">时间：{{ formatTime(act.startTime) }}</view>
          <view class="act-status" :class="'status-' + act.status">
            {{ getStatusText(act.status) }}
          </view>
          
          <view class="commission-box" v-if="act.status === 2">
            <text class="comm-label">活动佣金：</text>
            <text class="comm-val">¥{{ act.commission || '0.00' }}</text>
          </view>
          
          <view class="act-actions" v-if="act.status === 0">
            <button class="manage-btn" @click="goManage(act)">管理报名/取消活动</button>
          </view>
          <view class="act-actions" v-else-if="act.status === 2 && act.canWithdraw">
            <button class="withdraw-btn" @click="goWithdraw(act)">提现佣金</button>
          </view>
          <view class="act-actions" v-else-if="act.status === 2 && !act.canWithdraw">
            <button class="withdraw-btn disabled">已结算/未满24小时</button>
          </view>
          <view class="act-actions" v-else-if="act.status === -1">
            <button class="withdraw-btn disabled" style="background-color: #f5f5f5; color: #999;">已取消并退款</button>
          </view>
        </view>
      </view>
    </view>
    <view class="empty-state" v-else>
      <text class="empty-text">暂无主持过的活动记录</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { DEFAULT_AVATAR } from '../../utils/mockData'

const profile = ref({
  nickname: '',
  avatar: DEFAULT_AVATAR
})

const wallet = ref({
  commissionBalance: 0
})

const currentLevel = ref({ name: '白银红娘', commission: '30%' })

const myActivities = ref<any[]>([])

const getStatusText = (status: number) => {
  if (status === -1) return '已取消'
  if (status === 0) return '即将开始'
  if (status === 1) return '进行中'
  if (status === 2) return '已结束'
  return '未知'
}

const formatTime = (ts: number) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const fetchData = async () => {
  try {
    const pRes = await request({ url: '/profile/info', method: 'GET' })
    profile.value.nickname = pRes.nickname || '神秘鸭鸭'
    profile.value.avatar = pRes.avatar || DEFAULT_AVATAR
    // Mock level based on VIP or something, or just static
    if (pRes.isVip) {
      currentLevel.value = { name: '钻石红娘', commission: '35%' }
    }
    
    const wRes = await request({ url: '/wallet/info', method: 'GET' })
    wallet.value.commissionBalance = wRes?.commissionBalance || 0

    // Fetch activities and filter hosted by me in last 6 months
    const aRes = await request({ url: '/activity/list', method: 'GET' })
    const now = Date.now()
    const sixMonthsAgo = now - 6 * 30 * 24 * 60 * 60 * 1000
    
    // For demo purpose, just show some mock ones and set commission
    myActivities.value = (aRes || []).filter((act: any) => {
      // simulate all activities as mine for demo if it's empty
      return true
    }).slice(0, 5).map((act: any, idx: number) => {
      // Mock commission data
      act.commission = (1000 / 5).toFixed(2)
      act.canWithdraw = act.status === 2
      return act
    })
    
  } catch (e) {}
}

const goCreateActivity = () => {
  uni.navigateTo({ url: '/pages/activity-create/index' })
}

const goManage = (act: any) => {
  uni.navigateTo({ url: `/pages/activity-manage/index?id=${act.id}` })
}

const goWithdraw = (act?: any) => {
  uni.navigateTo({ url: '/pages/withdraw/index' })
}

onShow(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.mm-center-container {
  min-height: 100vh;
  background-color: #F8F9FA;
  padding: 30rpx;
}

.header-card {
  background: linear-gradient(135deg, #6B5EF7 0%, #A096F9 100%);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  color: #FFFFFF;
  box-shadow: 0 10rpx 30rpx rgba(107, 94, 247, 0.2);
  margin-bottom: 30rpx;
}

.mm-info {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  margin-right: 24rpx;
}

.info-right {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.name {
  font-size: 34rpx;
  font-weight: bold;
  margin-right: 16rpx;
}

.level-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  
  .icon {
    margin-right: 4rpx;
  }
}

.desc {
  font-size: 24rpx;
  opacity: 0.9;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  margin-bottom: 40rpx;
  background: rgba(255, 255, 255, 0.1);
  padding: 24rpx 0;
  border-radius: 16rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.8;
}

.create-btn {
  background: #FFFFFF;
  color: #6B5EF7;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 500;
  
  text {
    margin: 0 4rpx;
  }
}

.rules-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.rule-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
  
  text.ri-information-line {
    color: #6B5EF7;
    margin-right: 8rpx;
    font-size: 32rpx;
  }
}

.rule-text {
  display: block;
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
  margin-bottom: 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
}

.activity-list {
  display: flex;
  flex-direction: column;
}

.act-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.act-cover {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  margin-right: 24rpx;
}

.act-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.act-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.act-time {
  font-size: 24rpx;
  color: #999999;
  margin-bottom: 12rpx;
}

.act-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  align-self: flex-start;
  margin-bottom: 16rpx;
  
  &.status-0 {
    background: #E8EAF6;
    color: #3F51B5;
  }
  &.status-1 {
    background: #FFF3E0;
    color: #FF9800;
  }
  &.status-2 {
    background: #F5F5F5;
    color: #9E9E9E;
  }
}

.commission-box {
  background: #FFF9E6;
  padding: 12rpx 16rpx;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
}

.comm-label {
  font-size: 24rpx;
  color: #666666;
}

.comm-val {
  font-size: 28rpx;
  color: #FF5722;
  font-weight: bold;
}

.act-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

.withdraw-btn {
  margin: 0;
  background: #6B5EF7;
  color: #FFFFFF;
  font-size: 24rpx;
  height: 56rpx;
  line-height: 56rpx;
  border-radius: 28rpx;
  padding: 0 32rpx;
  
  &.disabled {
    background: #E0E0E0;
    color: #999999;
  }
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
