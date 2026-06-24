<template>
  <view class="recharge-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <text class="ri-arrow-left-s-line back-btn" @click="handleBack"></text>
        <text class="nav-title">充值中心</text>
      </view>
    </view>

    <!-- 顶部资产卡片 -->
    <view class="header-bg" :style="{ paddingTop: `calc(${statusBarHeight}px + 100rpx)` }">
      <view class="wallet-card">
        <view class="card-top">
          <text class="card-title">当前鸭蛋</text>
          <view class="detail-link" @click="goEggBill">
            <text>鸭蛋明细</text>
            <text class="ri-arrow-right-s-line"></text>
          </view>
        </view>
        <view class="split-balance-wrap">
          <view class="balance-item">
            <text class="balance-label">永久有效</text>
            <view class="balance-value-wrap">
              <text class="balance-num">{{ permanentBalance }}</text>
              <text class="balance-unit">个</text>
            </view>
          </view>
          <view class="balance-divider"></view>
          <view class="balance-item">
            <text class="balance-label">期限有效</text>
            <view class="balance-value-wrap">
              <text class="balance-num">{{ tempBalance }}</text>
              <text class="balance-unit">个</text>
            </view>
          </view>
        </view>
        <view class="card-bg-icon">🥚</view>
      </view>
    </view>

    <!-- 充值选项 -->
    <view class="section packages-section">
      <text class="section-title">选择充值金额</text>
      <view class="grid">
        <view 
          class="grid-item" 
          v-for="item in rechargeList" 
          :key="item.id"
          :class="{ active: selectedRecharge === item.id }"
          @click="selectedRecharge = item.id"
        >
          <view class="tag" v-if="item.tag">{{ item.tag }}</view>
          <view class="item-content">
            <view class="item-amount">
              <text class="amount-num">{{ item.amount }}</text>
              <text class="amount-unit">鸭蛋</text>
            </view>
            <text class="item-price">¥{{ item.price }}</text>
          </view>
          <!-- 选中状态的角标 -->
          <view class="active-icon" v-if="selectedRecharge === item.id">
            <text class="ri-check-line"></text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 支付方式 -->
    <view class="section payment-section">
      <text class="section-title">支付方式</text>
      <view class="payment-methods">
        <view 
          class="method-item" 
          :class="{ active: paymentMethod === 'wechat' }"
          @click="paymentMethod = 'wechat'"
        >
          <view class="method-left">
            <text class="ri-wechat-pay-fill wechat-icon"></text>
            <text class="method-name">微信支付</text>
          </view>
          <view class="radio-circle">
            <view class="radio-inner" v-if="paymentMethod === 'wechat'"></view>
          </view>
        </view>
        
        <view 
          class="method-item" 
          :class="{ active: paymentMethod === 'alipay' }"
          @click="paymentMethod = 'alipay'"
        >
          <view class="method-left">
            <text class="ri-alipay-fill alipay-icon"></text>
            <text class="method-name">支付宝</text>
          </view>
          <view class="radio-circle">
            <view class="radio-inner" v-if="paymentMethod === 'alipay'"></view>
          </view>
        </view>
      </view>
    </view>

    <view class="tips-section">
      <view class="tips-header">
        <text class="ri-information-line"></text>
        <text class="tips-title">温馨提示</text>
      </view>
      <text class="tips-item">充值购买的鸭蛋永久有效。</text>
      <text class="tips-item">鸭蛋可用于送礼、查看访客、解锁微信等场景。</text>
      <text class="tips-item">鸭蛋属于虚拟商品，一经购买不支持退换，未成年人请在监护人陪同下充值。</text>
      <text class="tips-item">充值过程中遇到问题请联系客服。</text>
    </view>

    <!-- 底部支付悬浮 -->
    <view class="bottom-pay-bar">
      <view class="pay-info">
        <text class="total-label">合计：</text>
        <text class="total-unit">¥</text>
        <text class="total-price">{{ currentTarget?.price || 0 }}</text>
      </view>
      <button class="pay-btn" @click="handlePayRecharge">立即支付</button>
    </view>

    <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { request } from '../../utils/request'

const statusBarHeight = ref(20)

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.statusBarHeight) {
    statusBarHeight.value = sysInfo.statusBarHeight
  }
})

const handleBack = () => {
  uni.navigateBack()
}

const balance = ref(0)
const permanentBalance = ref(0)
const tempBalance = ref(0)
const selectedRecharge = ref(2)
const paymentMethod = ref('wechat')

const rechargeList = [
  { id: 1, amount: 200, price: 28 },
  { id: 2, amount: 600, price: 68 },
  { id: 3, amount: 1300, price: 128 },
  { id: 4, amount: 2200, price: 198, tag: '热销' },
  { id: 5, amount: 3500, price: 298, tag: '热推' },
  { id: 6, amount: 5000, price: 398, tag: '最划算' }
]

const currentTarget = computed(() => {
  return rechargeList.find(i => i.id === selectedRecharge.value)
})

onMounted(async () => {
  fetchBalance()
})

const fetchBalance = async () => {
  try {
    const res: any = await request({ url: '/wallet/info', method: 'GET' })
    if (res) {
      balance.value = res.balance || 0
      permanentBalance.value = res.permanentBalance || 0
      tempBalance.value = res.tempBalance || 0
    } else {
      balance.value = 100
      permanentBalance.value = 100
      tempBalance.value = 0
    }
  } catch (e) {}
}

const goEggBill = () => {
  uni.navigateTo({ url: '/pages/egg-bill/index' })
}

const handlePayRecharge = () => {
  const target = currentTarget.value
  if (!target) return
  
  const methodName = paymentMethod.value === 'wechat' ? '微信支付' : '支付宝'
  
  uni.showModal({
    title: '确认支付',
    content: `确认使用${methodName}支付 ¥${target.price} 购买 ${target.amount}鸭蛋吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '支付中...' })
        setTimeout(async () => {
          try {
            const payRes: any = await request({ url: `/wallet/recharge?amount=${target.amount}`, method: 'POST' })
            uni.hideLoading()
            uni.showToast({ title: '充值成功', icon: 'success' })
            balance.value = payRes?.balance || (balance.value + target.amount)
            fetchBalance()
          } catch (e) {
            uni.hideLoading()
          }
        }, 800)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.recharge-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  padding-bottom: 160rpx; // 为底部悬浮留出空间
}

.custom-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: transparent;
  
  .nav-content {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    position: relative;
    
    .back-btn {
      font-size: 44rpx;
      color: #fff;
      padding: 10rpx;
      margin-left: -10rpx;
    }
    
    .nav-title {
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      font-size: 32rpx;
      font-weight: 500;
      color: #fff;
    }
  }
}

.header-bg {
  background: $uni-color-primary-gradient;
  padding: 40rpx 30rpx 60rpx;
  padding-top: calc(40rpx + var(--status-bar-height));
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 300rpx;
    height: 100%;
    background: radial-gradient(circle at top right, rgba(255, 255, 255, 0.15), transparent 70%);
    pointer-events: none;
  }
}

.wallet-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  color: #fff;
  position: relative;
  overflow: hidden;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.05);
  
  .card-bg-icon {
    position: absolute;
    right: -20rpx;
    bottom: -40rpx;
    font-size: 160rpx;
    opacity: 0.2;
    transform: rotate(-15deg);
    pointer-events: none;
  }
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 2;
}

.card-title {
  font-size: 28rpx;
  opacity: 0.9;
}

.detail-link {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  
  .ri-arrow-right-s-line {
    margin-left: 4rpx;
    font-size: 24rpx;
  }
}

.split-balance-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
  margin-top: 20rpx;
  margin-bottom: 20rpx;
}

.balance-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.balance-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8rpx;
}

.balance-value-wrap {
  display: flex;
  align-items: baseline;
}

.balance-num {
  font-size: 72rpx;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
}

.balance-unit {
  font-size: 28rpx;
  margin-left: 8rpx;
  opacity: 0.9;
}

.balance-divider {
  width: 2rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.2);
  margin: 0 40rpx;
}

.section {
  background-color: $uni-bg-color;
  margin: -30rpx 30rpx 30rpx;
  position: relative;
  z-index: 10;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.03);
}

.payment-section {
  margin-top: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: $uni-text-color;
  margin-bottom: 30rpx;
  display: block;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.grid-item {
  width: 31%;
  border: 2rpx solid $uni-border-color;
  border-radius: 16rpx;
  padding: 30rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
  position: relative;
  box-sizing: border-box;
  background-color: $uni-bg-color;
  transition: all 0.3s ease;
  overflow: hidden;
  
  &.active {
    border-color: $uni-color-primary;
    background-color: rgba(107, 94, 247, 0.05); // 基于主题色的淡背景
  }
}

.item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.item-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
  
  .amount-num {
    font-size: 36rpx;
    color: $uni-text-color;
    font-weight: bold;
  }

  .amount-unit {
    font-size: 24rpx;
    color: $uni-text-color;
    margin-left: 4rpx;
  }
}

.item-price {
  font-size: 26rpx;
  color: $uni-text-color-grey;
}

.grid-item.active .item-amount .amount-num,
.grid-item.active .item-price {
  color: $uni-color-primary;
}

.tag {
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF4757 100%);
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx 0 12rpx 0;
  z-index: 2;
}

.active-icon {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 40rpx;
  height: 40rpx;
  background-color: $uni-color-primary;
  border-radius: 16rpx 0 12rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .ri-check-line {
    color: #fff;
    font-size: 24rpx;
  }
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-radius: 16rpx;
  background-color: $uni-bg-color-page;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  
  &.active {
    border-color: rgba(107, 94, 247, 0.3);
    background-color: rgba(107, 94, 247, 0.05);
  }
  
  .method-left {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }
  
  .wechat-icon {
    font-size: 48rpx;
    color: #09B83E;
  }
  
  .alipay-icon {
    font-size: 48rpx;
    color: #1677FF;
  }
  
  .method-name {
    font-size: 28rpx;
    font-weight: 500;
    color: $uni-text-color;
  }
  
  .radio-circle {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 2rpx solid $uni-text-color-disable;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  
  &.active .radio-circle {
    border-color: $uni-color-primary;
  }
  
  .radio-inner {
    width: 20rpx;
    height: 20rpx;
    border-radius: 50%;
    background-color: $uni-color-primary;
  }
}

.tips-section {
  padding: 0 40rpx;
  margin-bottom: 40rpx;
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  
  .ri-information-line {
    font-size: 28rpx;
    color: $uni-text-color-grey;
    margin-right: 8rpx;
  }
  
  .tips-title {
    font-size: 26rpx;
    color: $uni-text-color-grey;
    font-weight: 500;
  }
}

.tips-item {
  font-size: 24rpx;
  color: $uni-text-color-placeholder;
  display: block;
  line-height: 1.6;
  margin-bottom: 8rpx;
  padding-left: 36rpx;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 14rpx;
    top: 14rpx;
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background-color: $uni-text-color-disable;
  }
}

.bottom-pay-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $uni-bg-color;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.pay-info {
  display: flex;
  align-items: baseline;
  
  .total-label {
    font-size: 28rpx;
    color: $uni-text-color;
  }
  
  .total-unit {
    font-size: 28rpx;
    color: #FF4757;
    margin-right: 4rpx;
    font-weight: bold;
  }
  
  .total-price {
    font-size: 48rpx;
    font-weight: 800;
    color: #FF4757;
  }
}

.pay-btn {
  margin: 0;
  width: 320rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: $uni-color-primary-gradient;
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  box-shadow: $uni-shadow-primary;
  
  &::after {
    border: none;
  }
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>