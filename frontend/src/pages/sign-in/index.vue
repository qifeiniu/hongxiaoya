<template>
  <view class="sign-in-container">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 自定义导航栏 -->
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <text class="ri-arrow-left-s-line"></text>
        </view>
        <text class="nav-title">每日签到</text>
        <view class="nav-right"></view>
      </view>

      <view class="egg-info-wrap">
        <view class="egg-info">
          <text class="label">我的鸭蛋</text>
          <text class="value">{{ wallet.balance || 0 }}</text>
        </view>
        <view class="sign-btn" :class="{ disabled: isSignedToday }" @click="doSignIn">
          {{ isSignedToday ? '今日已签到' : '立即签到' }}
        </view>
      </view>
    </view>

    <view class="calendar-card">
      <view class="month-header">
        <text class="arrow ri-arrow-left-s-line" @click="prevMonth"></text>
        <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
        <text class="arrow ri-arrow-right-s-line" @click="nextMonth"></text>
      </view>
      <view class="week-row">
        <text v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day" class="week-day">{{ day }}</text>
      </view>
      <view class="days-grid">
        <view
          v-for="(item, index) in calendarDays"
          :key="index"
          class="day-cell"
          :class="{
            'empty': !item.day,
            'today': item.isToday,
            'signed': item.isSigned,
            'future': item.isFuture
          }"
        >
          <text class="day-num" v-if="item.day">{{ item.day }}</text>
          <text class="signed-icon ri-check-line" v-if="item.isSigned"></text>
        </view>
      </view>
    </view>

    <view class="records-card">
      <view class="records-title">签到记录</view>
      <view class="record-list" v-if="records.length > 0">
        <view class="record-item" v-for="item in records" :key="item.id">
          <view class="record-left">
            <text class="record-action">{{ formatDateDot(item.createdAt || item.time) }}签到</text>
          </view>
          <view class="record-right">
            <text class="record-amount">+20鸭蛋🥚</text>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else>
        <text class="ri-calendar-todo-line empty-icon"></text>
        <text class="empty-text">暂无签到记录</text>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { request } from '../../utils/request'
import { onShow } from '@dcloudio/uni-app'

const statusBarHeight = ref(20)

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.statusBarHeight) {
    statusBarHeight.value = sysInfo.statusBarHeight
  }
})

const goBack = () => {
  uni.navigateBack()
}

const wallet = ref({
  balance: 0,
  tempBalance: 0,
  permanentBalance: 0
})

const records = ref<any[]>([])

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)

const parseDate = (timeStr: string | number) => {
  if (!timeStr) return new Date(NaN)
  if (typeof timeStr === 'string') {
    let str = timeStr.replace(/\./g, '/')
    if (str.includes('-') && !str.includes('T')) {
      str = str.replace(/-/g, '/')
    }
    return new Date(str)
  }
  return new Date(timeStr)
}

const signedDates = computed(() => {
  const dates = new Set<string>()
  records.value.forEach(record => {
    const timeStr = record.createdAt || record.time
    if (timeStr) {
      // Parse ISO string or timestamp
      const d = parseDate(timeStr)
      if (!isNaN(d.getTime())) {
        const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        dates.add(dateStr)
      }
    }
  })
  return dates
})

const isSignedToday = computed(() => {
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  return signedDates.value.has(todayStr)
})

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value - 1
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  const daysInMonth = lastDay.getDate()
  const startingDayOfWeek = firstDay.getDay() // 0-6, 0 is Sunday
  
  const days = []
  
  // Empty cells for days before the 1st of the month
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push({ day: null })
  }
  
  // Actual days
  for (let i = 1; i <= daysInMonth; i++) {
    const d = new Date(year, month, i)
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    
    const isToday = d.getFullYear() === today.getFullYear() && 
                    d.getMonth() === today.getMonth() && 
                    d.getDate() === today.getDate()
                    
    const isFuture = d.getTime() > today.getTime() && !isToday
    
    days.push({
      day: i,
      dateStr,
      isToday,
      isFuture,
      isSigned: signedDates.value.has(dateStr)
    })
  }
  
  return days
})

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value -= 1
  } else {
    currentMonth.value -= 1
  }
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value += 1
  } else {
    currentMonth.value += 1
  }
}

const fetchWallet = async () => {
  try {
    const res = await request({ url: '/wallet/info', method: 'GET' })
    if (res) wallet.value = res
  } catch (e) {}
}

const fetchRecords = async () => {
  try {
    const res: any = await request({ url: '/wallet/ledger?scene=sign_in', method: 'GET' })
    let list = []
    if (res && res.list) {
      list = res.list
    } else if (Array.isArray(res)) {
      list = res
    }
    // Filter for sign-in records just in case mock returns all
    records.value = list.filter((item: any) => item.scene === 'sign_in' || item.title === '每日签到')
  } catch (e) {}
}

const doSignIn = async () => {
  if (isSignedToday.value) return
  
  try {
    uni.showLoading({ title: '签到中...' })
    await request({ url: '/wallet/sign-in', method: 'POST' })
    uni.hideLoading()
    uni.showToast({ title: `签到成功`, icon: 'none' })
    fetchWallet()
    fetchRecords()
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '签到失败', icon: 'none' })
  }
}

const formatDateDot = (timeStr: string | number) => {
  if (!timeStr) return ''
  const d = parseDate(timeStr)
  if (isNaN(d.getTime())) return timeStr as string
  const y = d.getFullYear()
  const m = d.getMonth() + 1
  const day = d.getDate()
  return `${y}.${m}.${day}`
}

onShow(() => {
  fetchWallet()
  fetchRecords()
})
</script>

<style lang="scss" scoped>
.sign-in-container {
  min-height: 100vh;
  background-color: #F4F5F9;
  padding-bottom: 40rpx;
}

.header {
  background: linear-gradient(135deg, #6A61F8 0%, #8D85FF 100%);
  padding-bottom: 80rpx;
  display: flex;
  flex-direction: column;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
  color: #FFF;
}

.nav-bar {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.back-btn {
  font-size: 48rpx;
  width: 60rpx;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
}

.nav-right {
  width: 60rpx;
}

.egg-info-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 40rpx 0;
}

.egg-info {
  display: flex;
  flex-direction: column;
}

.egg-info .label {
  font-size: 28rpx;
  opacity: 0.8;
  margin-bottom: 10rpx;
}

.egg-info .value {
  font-size: 64rpx;
  font-weight: bold;
}

.sign-btn {
  background-color: #FFF;
  color: #6A61F8;
  font-size: 28rpx;
  font-weight: bold;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.sign-btn.disabled {
  background-color: rgba(255,255,255,0.4);
  color: #FFF;
  box-shadow: none;
}

.calendar-card {
  margin: -40rpx 30rpx 30rpx;
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.month-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.arrow {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
}

.week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 20rpx;
}

.week-day {
  text-align: center;
  font-size: 26rpx;
  color: #999;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16rpx 0;
}

.day-cell {
  height: 70rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.day-num {
  font-size: 28rpx;
  color: #333;
  z-index: 1;
}

.day-cell.future .day-num {
  color: #CCC;
}

.day-cell.today .day-num {
  color: #6A61F8;
  font-weight: bold;
}

.day-cell.signed::after {
  content: '';
  position: absolute;
  width: 56rpx;
  height: 56rpx;
  background-color: #F0EFFF;
  border-radius: 50%;
  z-index: 0;
}

.day-cell.signed .day-num {
  color: #6A61F8;
}

.signed-icon {
  position: absolute;
  bottom: 0;
  right: 10rpx;
  font-size: 20rpx;
  color: #FFF;
  background-color: #6A61F8;
  border-radius: 50%;
  width: 24rpx;
  height: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.records-card {
  margin: 0 30rpx;
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

.records-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  display: flex;
  flex-direction: column;
}

.record-action {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.record-time {
  font-size: 24rpx;
  color: #999;
}

.record-amount {
  font-size: 28rpx;
  font-weight: bold;
  color: #FA8C16;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  color: #DDD;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
