<template>
  <view class="page-container">
    <view class="header-card">
      <view class="header-bg-shape shape-1"></view>
      <view class="header-bg-shape shape-2"></view>
      <view class="header-content">
        <text class="header-icon">💍</text>
        <text class="header-title">成为红小鸭红娘</text>
        <text class="header-desc">帮助更多人找到幸福，赚取丰厚分成收益</text>
      </view>
    </view>

    <!-- 等级体系 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">红娘等级体系</text>
        <text class="section-sub">等级越高，分成比例越大</text>
      </view>
      <view class="level-list">
        <view class="level-card" v-for="level in levels" :key="level.name">
          <view class="level-badge" :style="{ background: level.color, boxShadow: `0 8rpx 20rpx ${level.shadow}` }">
            <text>{{ level.icon }}</text>
          </view>
          <view class="level-info">
            <text class="level-name">{{ level.name }}</text>
            <view class="level-req-wrap">
              <text class="ri-user-add-line req-icon"></text>
              <text class="level-req">{{ level.requirement }}</text>
            </view>
          </view>
          <view class="level-reward-wrap">
            <text class="reward-label">直播相亲活动分成</text>
            <text class="level-reward">{{ level.commission }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 申请条件 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">申请条件</text>
        <text class="section-sub">满足以下全部条件即可申请</text>
      </view>
      <view class="condition-grid">
        <view class="condition-item" v-for="(c, i) in conditions" :key="i">
          <view class="condition-icon-wrap">
            <text class="ri-check-line condition-icon"></text>
          </view>
          <text class="condition-text">{{ c }}</text>
        </view>
      </view>
    </view>

    <!-- 收益说明 -->
    <view class="section" style="margin-bottom: 60rpx;">
      <view class="section-header">
        <text class="section-title">收益说明</text>
      </view>
      <view class="earning-card">
        <view class="earning-item">
          <view class="earning-icon-box bg-primary">
            <text class="ri-money-cny-circle-line"></text>
          </view>
          <view class="earning-text">
            <text class="earning-label">活动创建分成</text>
            <text class="earning-value">按活动报名费用按比例分成</text>
          </view>
        </view>
        <view class="earning-item">
          <view class="earning-icon-box bg-primary">
            <text class="ri-user-heart-line"></text>
          </view>
          <view class="earning-text">
            <text class="earning-label">用户引荐奖励</text>
            <text class="earning-value">邀请新用户注册可得现金奖励</text>
          </view>
        </view>
        <view class="earning-item">
          <view class="earning-icon-box bg-primary">
            <text class="ri-bank-card-line"></text>
          </view>
          <view class="earning-text">
            <text class="earning-label">便捷提现</text>
            <text class="earning-value">支持微信/支付宝，极速到账</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 申请按钮 -->
    <view class="apply-area">
      <button class="apply-btn" @click="handleApply">立即申请入驻</button>
      <text class="apply-tip">提交后系统将自动审核您的条件</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { request } from '../../utils/request'

const levels = [
  { name: '白银红娘', icon: '🥈', color: 'linear-gradient(135deg, #F5F5F5, #E0E0E0)', shadow: 'rgba(0,0,0,0.05)', requirement: '邀请10人注册', commission: '30%' },
  { name: '黄金红娘', icon: '🥇', color: 'linear-gradient(135deg, #FFF3E0, #FFE0B2)', shadow: 'rgba(255, 152, 0, 0.15)', requirement: '邀请50人注册', commission: '33%' },
  { name: '钻石红娘', icon: '👑', color: 'linear-gradient(135deg, #F3E5F5, #E1BEE7)', shadow: 'rgba(156, 39, 176, 0.15)', requirement: '邀请100人注册', commission: '35%' }
]

const conditions = [
  '已完成实名认证',
  '成功邀请10人',
  '无任何违规记录',
  '年满18周岁'
]

const handleApply = async () => {
  try {
    uni.showLoading({ title: '申请中...' })
    await request({
      url: '/profile/apply-matchmaker',
      method: 'POST'
    })
    uni.hideLoading()
    uni.showModal({
      title: '申请已提交',
      content: '您的红娘入驻申请已提交，请等待平台审核。如有疑问请联系客服。',
      confirmText: '联系客服',
      cancelText: '返回',
      success: (r) => {
        if (r.confirm) {
          uni.navigateTo({ url: '/pages/customer-service/index' })
        } else {
          uni.navigateBack()
        }
      }
    })
  } catch (e: any) {
    uni.hideLoading()
    uni.showModal({
      title: '申请未通过',
      content: e.message || '申请失败，请确保满足申请条件',
      showCancel: false
    })
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8F9FA;
  padding: 30rpx;
  padding-bottom: 220rpx;
}

.header-card {
  position: relative;
  background: linear-gradient(135deg, #B57BFF 0%, #6A61F8 100%);
  border-radius: 32rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  margin-bottom: 50rpx;
  overflow: hidden;
  box-shadow: 0 16rpx 40rpx rgba(106, 97, 248, 0.3);
}

.header-bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 200rpx;
  height: 200rpx;
  top: -40rpx;
  right: -40rpx;
}

.shape-2 {
  width: 120rpx;
  height: 120rpx;
  bottom: 20rpx;
  left: -20rpx;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-icon { 
  font-size: 88rpx; 
  display: block; 
  margin-bottom: 24rpx;
  text-shadow: 0 8rpx 16rpx rgba(0,0,0,0.1);
}

.header-title { 
  font-size: 44rpx; 
  font-weight: 800; 
  color: #fff; 
  display: block; 
  margin-bottom: 16rpx; 
  letter-spacing: 2rpx;
}

.header-desc { 
  font-size: 26rpx; 
  color: rgba(255,255,255,0.9); 
  display: block; 
}

.section {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  align-items: flex-end;
  margin-bottom: 24rpx;
  padding: 0 10rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-right: 16rpx;
}

.section-sub {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 4rpx;
}

.level-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.level-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
  transition: all 0.3s ease;
}

.level-badge {
  width: 90rpx;
  height: 90rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  margin-right: 30rpx;
  flex-shrink: 0;
}

.level-info { 
  flex: 1; 
}

.level-name { 
  font-size: 30rpx; 
  font-weight: 700; 
  color: #000;
  display: block; 
  margin-bottom: 12rpx; 
}

.level-req-wrap {
  display: flex;
  align-items: center;
}

.req-icon {
  font-size: 24rpx;
  color: #999;
  margin-right: 6rpx;
}

.level-req { 
  font-size: 24rpx; 
  color: #666; 
}

.level-reward-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.reward-label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.level-reward { 
  font-size: 36rpx; 
  color: #6A61F8; 
  font-weight: bold; 
  font-family: DIN-Bold, sans-serif;
}

.condition-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.condition-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.condition-icon-wrap {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(106, 97, 248, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.condition-icon {
  font-size: 24rpx;
  color: #6A61F8;
  font-weight: bold;
}

.condition-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.earning-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 10rpx 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.earning-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
}

.earning-item + .earning-item {
  border-top: 2rpx solid #F5F5F5;
}

.earning-icon-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  font-size: 36rpx;
  color: #fff;
}

.bg-primary { background: linear-gradient(135deg, #B57BFF 0%, #6A61F8 100%); color: #fff; }

.earning-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.earning-label { 
  font-size: 28rpx; 
  color: #333; 
  font-weight: 600;
  margin-bottom: 8rpx;
}

.earning-value { 
  font-size: 24rpx; 
  color: #999; 
}

.apply-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 40rpx;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  padding-bottom: calc(env(safe-area-inset-bottom) + 24rpx);
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  z-index: 100;
}

.apply-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #B57BFF 0%, #6A61F8 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 48rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(106, 97, 248, 0.3);
}

.apply-btn:active {
  transform: scale(0.98);
}

.apply-tip {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: #999;
}
</style>
