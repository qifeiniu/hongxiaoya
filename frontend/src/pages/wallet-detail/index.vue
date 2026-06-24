<template>
  <view class="wallet-detail-container">
    <view class="header-section">
      <view class="header-banner">
        <view class="commission-info" v-if="isMatchmaker">
          <text class="balance-title">可提现佣金(元)</text>
          <view class="commission-row">
            <text class="balance-num commission-num">{{ commissionBalance }}</text>
            <view class="withdraw-btn" @click="handleWithdraw">
              <text>提现</text>
              <text class="ri-arrow-right-s-line"></text>
            </view>
          </view>
        </view>
        <view class="balance-info" v-else>
          <text class="balance-title">可提现余额(元)</text>
          <view class="balance-row">
            <text class="balance-num">0.00</text>
            <view class="withdraw-btn" @click="handleWithdraw">
              <text>提现</text>
              <text class="ri-arrow-right-s-line"></text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="content-section">
      <view class="filter-bar">
        <view class="tab-list">
          <view class="tab" :class="{ active: currentTab === 'all' }" @click="currentTab = 'all'">全部</view>
          <view class="tab" :class="{ active: currentTab === 'income' }" @click="currentTab = 'income'">收入</view>
          <view class="tab" :class="{ active: currentTab === 'expense' }" @click="currentTab = 'expense'">支出</view>
          <view class="tab" :class="{ active: currentTab === 'consumption' }" @click="currentTab = 'consumption'">消费</view>
        </view>

        <picker mode="date" fields="month" :value="selectedMonth" @change="onMonthChange" :start="startDate" :end="endDate">
          <view class="month-picker">
            <text>{{ selectedMonth || '全部时间' }}</text>
            <text class="ri-calendar-line"></text>
          </view>
        </picker>
      </view>
      <view class="filter-desc" v-if="selectedMonth">可查询12个月内记录</view>

      <scroll-view scroll-y class="list-container">
        <view class="empty-state" v-if="filteredList.length === 0">
          <image src="/static/logo.png" class="empty-img" mode="aspectFit"></image>
          <text class="empty-text">暂无明细记录</text>
        </view>
        <view class="list-item" v-for="(item, index) in filteredList" :key="index">
          <view class="item-left">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-time">{{ item.time }}</text>
            <text class="item-expire" v-if="item.expireTime && item.amount > 0">有效期至: {{ item.expireTime }}</text>
          </view>
          <view class="item-right">
            <template v-if="item.type === 'recharge_rmb' || (item.rmbAmount && item.rmbAmount < 0)">
              <text class="item-amount expense">{{ item.rmbAmount }}元</text>
            </template>
            <template v-else-if="item.rmbAmount && item.rmbAmount > 0">
              <text class="item-amount income">+{{ item.rmbAmount }}元</text>
            </template>
          </view>
        </view>
      </scroll-view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'

const totalBalance = ref(0)
const commissionBalance = ref(0)
const isMatchmaker = ref(false)
const currentTab = ref('all')
const ledgerList = ref<any[]>([])

const selectedMonth = ref('')
const endDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}.${d.getMonth() + 1}`
})
const startDate = computed(() => {
  const d = new Date()
  d.setMonth(d.getMonth() - 11) // 12个月内
  return `${d.getFullYear()}.${d.getMonth() + 1}`
})

const onMonthChange = (e: any) => {
  const [year, month] = e.detail.value.split('-')
  selectedMonth.value = `${year}.${parseInt(month)}`
}

const fetchProfile = async () => {
  try {
    const res: any = await request({ url: '/profile/info', method: 'GET' })
    isMatchmaker.value = res?.isMatchmaker || false
  } catch (e) {
    console.error(e)
  }
}

const fetchLedger = async () => {
  try {
    const res: any = await request({ url: '/wallet/ledger', method: 'GET' })
    totalBalance.value = res?.balance || 0
    commissionBalance.value = res?.commissionBalance || 0 // 假设后端返回
    ledgerList.value = res?.list || []
  } catch (e) {
    totalBalance.value = 0
    ledgerList.value = []
  }
}

const fetchWalletInfo = async () => {
  try {
    const res: any = await request({ url: '/wallet/info', method: 'GET' })
    commissionBalance.value = res?.commissionBalance || 0
  } catch (e) {}
}

const filteredList = computed(() => {
  let list = ledgerList.value
  
  if (selectedMonth.value) {
    list = list.filter(item => item.time.startsWith(selectedMonth.value))
  }

  // 过滤出只有人民币流水的记录
  list = list.filter(item => item.rmbAmount)

  if (currentTab.value === 'income') {
    return list.filter(item => item.rmbAmount > 0)
  } else if (currentTab.value === 'expense' || currentTab.value === 'consumption') {
    return list.filter(item => item.rmbAmount < 0)
  }
  return list
})

const handleWithdraw = () => {
  uni.navigateTo({ url: '/pages/withdraw/index' })
}

onShow(() => {
  fetchProfile()
  fetchWalletInfo()
  fetchLedger()
})
</script>

<style lang="scss" scoped>
.wallet-detail-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
}

.header-section {
  padding: 30rpx;
  background-color: $uni-bg-color;
  padding-bottom: 40rpx;
}

.header-banner {
  background: linear-gradient(135deg, #6B5EF7 0%, #8A81F8 100%);
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  color: #FFFFFF;
  border-radius: 30rpx;
  box-shadow: 0 12rpx 24rpx rgba(107, 94, 247, 0.25);
  gap: 30rpx;
}

.divider {
  height: 1rpx;
  background-color: rgba(255, 255, 255, 0.15);
  width: 100%;
}

.balance-info, .commission-info {
  display: flex;
  flex-direction: column;
}

.balance-row, .commission-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 10rpx;
}

.withdraw-btn {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 40rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  color: #FFFFFF;
  backdrop-filter: blur(10px);
  margin-bottom: 8rpx;
}

.withdraw-btn text:first-child {
  margin-right: 4rpx;
}

.balance-title {
  font-size: 26rpx;
  opacity: 0.85;
}

.balance-num {
  font-size: 72rpx;
  font-weight: 600;
  line-height: 1;
  font-family: 'DIN Alternate', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.commission-num {
  font-size: 60rpx;
}

.content-section {
  flex: 1;
  background-color: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  margin-top: -30rpx;
  padding: 30rpx 0 0;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx 20rpx;
}

.tab-list {
  display: flex;
  background-color: #F8F8F8;
  border-radius: 16rpx;
  padding: 6rpx;
}

.tab {
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: #666666;
  border-radius: 12rpx;
  transition: all 0.3s;
}

.tab.active {
  color: #333333;
  font-weight: 500;
  background-color: #FFFFFF;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}

.month-picker {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #333333;
  background: #F8F8F8;
  padding: 14rpx 24rpx;
  border-radius: 30rpx;
  font-weight: 500;
}

.month-picker .ri-calendar-line {
  margin-left: 8rpx;
  font-size: 28rpx;
  color: #666666;
}

.filter-desc {
  font-size: 24rpx;
  color: #999999;
  padding: 0 30rpx 20rpx;
  text-align: right;
}

.list-container {
  flex: 1;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  margin: 0 30rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.list-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.item-time {
  font-size: 24rpx;
  color: #999999;
}

.item-expire {
  font-size: 22rpx;
  color: #FF6B6B;
  margin-top: 8rpx;
  background: rgba(255, 107, 107, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  align-self: flex-start;
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.item-amount {
  font-size: 34rpx;
  font-weight: bold;
  font-family: 'DIN Alternate', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.income {
  color: #FF5A5F;
}

.expense {
  color: #333333;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 160rpx;
}

.empty-img {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
  filter: grayscale(100%);
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
}
</style>
