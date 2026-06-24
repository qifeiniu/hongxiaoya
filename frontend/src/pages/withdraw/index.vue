<template>
  <view class="withdraw-container">
    <view class="card">
      <view class="card-header">
        <text class="title">提现金额</text>
        <text class="balance">可提现余额：¥{{ availableAmount }}</text>
      </view>
      <view class="input-wrap">
        <text class="currency">¥</text>
        <input type="digit" class="amount-input" v-model="amount" placeholder="0.00" />
        <view class="all-btn" @click="withdrawAll">全部提现</view>
      </view>
    </view>
    
    <view class="card method-card">
      <view class="method-title">提现方式</view>
      
      <view class="method-list">
        <view class="method-item" @click="currentMethod = 'wechat'">
          <text class="ri-wechat-pay-fill method-icon wechat"></text>
          <text class="method-name">微信零钱</text>
          <text class="ri-checkbox-circle-fill check-icon" :class="{ active: currentMethod === 'wechat' }"></text>
        </view>
        <view class="method-item" @click="currentMethod = 'alipay'">
          <text class="ri-alipay-fill method-icon alipay"></text>
          <text class="method-name">支付宝</text>
          <text class="ri-checkbox-circle-fill check-icon" :class="{ active: currentMethod === 'alipay' }"></text>
        </view>
        
        <view class="account-info" v-if="currentMethod === 'alipay'">
          <view class="info-row">
            <text class="label">支付宝账号</text>
            <input class="input" v-model="alipayAccount" placeholder="请输入支付宝账号" />
          </view>
          <view class="info-row">
            <text class="label">真实姓名</text>
            <input class="input" v-model="alipayName" placeholder="请输入真实姓名" />
          </view>
        </view>

        <view class="method-item" @click="currentMethod = 'bank'">
          <text class="ri-bank-card-fill method-icon bank"></text>
          <text class="method-name">银行卡</text>
          <text class="ri-checkbox-circle-fill check-icon" :class="{ active: currentMethod === 'bank' }"></text>
        </view>
        
        <view class="account-info" v-if="currentMethod === 'bank'">
          <view class="info-row">
            <text class="label">银行名称</text>
            <input class="input" v-model="bankName" placeholder="例如：招商银行" />
          </view>
          <view class="info-row">
            <text class="label">银行卡号</text>
            <input class="input" type="number" v-model="bankAccount" placeholder="请输入银行卡号" />
          </view>
          <view class="info-row">
            <text class="label">真实姓名</text>
            <input class="input" v-model="bankUserName" placeholder="请输入持卡人姓名" />
          </view>
        </view>
      </view>
      <view class="tips">预计2小时内到账</view>
    </view>

    <button class="submit-btn" :disabled="!isValid" @click="handleSubmit">确认提现</button>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'

const availableAmount = ref('0.00')
const amount = ref('')
const currentMethod = ref('wechat')

const alipayAccount = ref('')
const alipayName = ref('')

const bankName = ref('')
const bankAccount = ref('')
const bankUserName = ref('')

const fetchWalletInfo = async () => {
  try {
    const res: any = await request({ url: '/wallet/info', method: 'GET' })
    availableAmount.value = (res?.commissionBalance || 0).toFixed(2)
  } catch (e) {}
}

const isValid = computed(() => {
  const val = parseFloat(amount.value)
  const isAmountValid = !isNaN(val) && val > 0 && val <= parseFloat(availableAmount.value)
  if (!isAmountValid) return false
  
  if (currentMethod.value === 'alipay') {
    return alipayAccount.value.trim() !== '' && alipayName.value.trim() !== ''
  }
  if (currentMethod.value === 'bank') {
    return bankName.value.trim() !== '' && bankAccount.value.trim() !== '' && bankUserName.value.trim() !== ''
  }
  return true
})

const withdrawAll = () => {
  amount.value = availableAmount.value
}

const handleSubmit = async () => {
  if (!isValid.value) return
  uni.showLoading({ title: '提交中...' })
  try {
    await request({
      url: '/wallet/withdraw',
      method: 'POST',
      data: {
        amount: parseFloat(amount.value)
      }
    })
    uni.hideLoading()
    uni.showToast({ title: '提现申请已提交', icon: 'success' })
    availableAmount.value = (parseFloat(availableAmount.value) - parseFloat(amount.value)).toFixed(2)
    amount.value = ''
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '提现失败', icon: 'none' })
  }
}

onShow(() => {
  fetchWalletInfo()
})
</script>

<style lang="scss" scoped>
.withdraw-container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 30rpx;
}

.card {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
  margin-bottom: 30rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.title {
  font-size: 32rpx;
  color: #333333;
  font-weight: 500;
}

.balance {
  font-size: 26rpx;
  color: #999999;
}

.input-wrap {
  display: flex;
  align-items: baseline;
  border-bottom: 2rpx solid #EEEEEE;
  padding-bottom: 20rpx;
}

.currency {
  font-size: 60rpx;
  color: #333333;
  font-weight: bold;
  margin-right: 20rpx;
}

.amount-input {
  flex: 1;
  font-size: 80rpx;
  height: 100rpx;
  font-weight: bold;
}

.all-btn {
  font-size: 28rpx;
  color: #6B5EF7;
  padding: 10rpx 0 10rpx 20rpx;
}

.method-card {
  padding: 30rpx 40rpx;
}

.method-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
}

.method-list {
  display: flex;
  flex-direction: column;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #F5F5F5;
  
  &:last-child {
    border-bottom: none;
  }
}

.method-icon {
  font-size: 44rpx;
  margin-right: 20rpx;
  
  &.wechat { color: #09B83E; }
  &.alipay { color: #1677FF; }
  &.bank { color: #FF9800; }
}

.method-name {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.check-icon {
  font-size: 40rpx;
  color: #E0E0E0;
  
  &.active {
    color: #6B5EF7;
  }
}

.account-info {
  background-color: #F9F9F9;
  border-radius: 12rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  font-size: 26rpx;
  color: #666666;
  width: 160rpx;
}

.input {
  flex: 1;
  font-size: 26rpx;
  color: #333333;
}

.tips {
  font-size: 24rpx;
  color: #999999;
  margin-top: 20rpx;
  text-align: center;
}

.submit-btn {
  background: linear-gradient(135deg, #6B5EF7 0%, #8A81F8 100%);
  color: #FFFFFF;
  border-radius: 44rpx;
  margin-top: 60rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  
  &[disabled] {
    opacity: 0.5;
  }
}
</style>