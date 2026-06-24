<template>
  <view class="recharge-container">
    <!-- 顶部背景与会员卡片 -->
    <view class="header-bg">
      <view class="vip-card">
        <view class="user-info">
          <image class="avatar" :src="profile.avatar || getRandomAvatar(100, 'female')" mode="aspectFill"></image>
          <view class="info-right">
            <view class="name-row">
              <text class="nickname">{{ profile.nickname }}</text>
              <text class="user-id-small" v-if="profile.userId">{{ profile.userId }}</text>
              <view class="vip-badge" :class="{ 'is-vip': profile.isVip }">
                <text class="ri-vip-crown-fill"></text>
                <text>{{ profile.isVip ? '尊享会员' : '未开通' }}</text>
              </view>
            </view>
            <text class="vip-desc">{{ profile.isVip ? '您已解锁全部特权' : '开通会员，解锁九大会员特权' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 会员套餐选择 -->
    <view class="packages-section">
      <view class="section-header">
        <text class="section-title">选择套餐</text>
      </view>
      
      <scroll-view class="packages-scroll" scroll-x :show-scrollbar="false">
        <view class="packages-list">
          <view 
            class="package-item" 
            v-for="item in vipList" 
            :key="item.id"
            :class="{ active: selectedVip === item.id }"
            @click="selectedVip = item.id"
          >
            <view class="recommend-tag" v-if="item.recommend">推荐</view>
            <text class="pkg-name">{{ item.name }}</text>
            <view class="pkg-price">
              <text class="unit">¥</text>
              <text class="price-val">{{ item.price }}</text>
            </view>
            <text class="pkg-origin" v-if="item.originPrice">¥{{ item.originPrice }}</text>
            <text class="pkg-daily" v-if="item.dailyPrice">折合 ¥{{ item.dailyPrice }}/天</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 会员特权 -->
    <view class="privilege-section">
      <view class="section-header">
        <text class="section-title">VIP专属特权</text>
      </view>
      
      <view class="privilege-grid">
        <view class="privilege-item" v-for="(privilege, index) in privileges" :key="index">
          <view class="icon-wrap">
            <text :class="privilege.icon"></text>
          </view>
          <text class="privilege-name">{{ privilege.name }}</text>
          <text class="privilege-desc">{{ privilege.desc }}</text>
        </view>
      </view>
    </view>

    <!-- 底部支付悬浮 -->
    <view class="bottom-pay-bar">
      <view class="pay-info">
        <text class="total-label">总计：</text>
        <text class="total-unit">¥</text>
        <text class="total-price">{{ currentPackage?.price || 0 }}</text>
      </view>
      <button class="pay-btn" @click="handlePayVip">
        {{ profile.isVip ? '立即续费' : '立即开通' }}
      </button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { request } from '../../utils/request'
import { getRandomAvatar } from '../../utils/mockData'

const selectedVip = ref(4) // 默认选12个月
const profile = ref({
  nickname: '神秘鸭鸭',
  avatar: '',
  isVip: false,
  userId: 0
})

const vipList = [
  { id: 4, name: '12个月', months: 12, price: 128, originPrice: 198, dailyPrice: '0.35', recommend: true },
  { id: 1, name: '1个月', months: 1, price: 28, dailyPrice: '0.93' },
  { id: 2, name: '3个月', months: 3, price: 68, dailyPrice: '0.75' },
  { id: 3, name: '6个月', months: 6, price: 98, dailyPrice: '0.54' }
]

const currentPackage = computed(() => {
  return vipList.find(i => i.id === selectedVip.value)
})

const privileges = [
  { icon: 'ri-heart-line', name: '不限喜欢', desc: '每天无限次' },
  { icon: 'ri-eye-line', name: '最近访客', desc: '谁来看过你' },
  { icon: 'ri-filter-3-line', name: '高级筛选', desc: '精准匹配' },
  { icon: 'ri-user-heart-line', name: '谁喜欢我', desc: '查看喜欢你的人' },
  { icon: 'ri-message-3-line', name: '超级漂流瓶', desc: '每天各1组' },
  { icon: 'ri-chat-check-line', name: '消息已读', desc: '掌控聊天' },
  { icon: 'ri-calendar-check-line', name: '签到翻倍', desc: '鸭蛋奖励x2' },
  { icon: 'ri-rocket-line', name: '优先推荐', desc: '曝光率提升' },
  { icon: 'ri-vip-crown-line', name: '专属标识', desc: '尊贵身份' }
]

onMounted(async () => {
  try {
    const res = await request({ url: '/profile/info', method: 'GET' })
    if (res) {
      let nickname = res.nickname || '神秘鸭鸭'
      const defaultPattern = /^(红小鸭|微信用户|红小鸭用户)\d+$/
      if (defaultPattern.test(nickname)) {
        nickname = nickname.replace(/\d+$/, '')
      }
      profile.value.nickname = nickname
      profile.value.avatar = res.avatar
      profile.value.isVip = Boolean(res.isVip)
      profile.value.userId = res.userId || 0
    }
  } catch (e) {}
})

const handlePayVip = () => {
  const target = currentPackage.value
  uni.showModal({
    title: '开通VIP',
    content: `确认支付 ¥${target?.price} 开通${target?.name}吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '支付中...' })
        try {
          const months = target?.months || 1
          await request({ url: `/vip/buy?months=${months}`, method: 'POST' })
          uni.hideLoading()
          uni.showToast({ title: '开通成功，已解锁特权！', icon: 'none' })
          profile.value.isVip = true
        } catch (e) {
          uni.hideLoading()
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.recharge-container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding-bottom: 160rpx; // 为底部悬浮留出空间
}

.header-bg {
  background: linear-gradient(135deg, #1A1A1A 0%, #333333 100%);
  padding: 40rpx 30rpx 60rpx;
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
    background: radial-gradient(circle at top right, rgba(212, 175, 55, 0.15), transparent 70%);
    pointer-events: none;
  }
}

.vip-card {
  background: linear-gradient(105deg, #E6C887 0%, #D4AF37 50%, #C59B27 100%);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 16rpx 32rpx rgba(212, 175, 55, 0.2);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 200rpx;
    height: 200%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transform: rotate(30deg);
  }
}

.user-info {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 55rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  margin-right: 24rpx;
  background-color: #fff;
}

.info-right {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.nickname {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-right: 12rpx;
}

.user-id-small {
  font-size: 24rpx;
  color: #666;
  font-weight: normal;
  margin-right: 16rpx;
}

.vip-badge {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.3);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  color: #666;
  
  text {
    margin-right: 4rpx;
  }
  
  &.is-vip {
    background-color: #333;
    color: #F8D37B;
  }
}

.vip-desc {
  font-size: 24rpx;
  color: rgba(51, 51, 51, 0.8);
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.packages-section {
  margin-top: 40rpx;
  padding: 0 30rpx;
}

.packages-scroll {
  width: 100%;
  white-space: nowrap;
}

.packages-list {
  display: flex;
  padding: 10rpx 0;
}

.package-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 240rpx;
  flex-shrink: 0;
  background-color: #FFF;
  border: 4rpx solid transparent;
  border-radius: 20rpx;
  padding: 30rpx 0;
  margin-right: 24rpx;
  position: relative;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  
  &:last-child {
    margin-right: 0;
  }
  
  &.active {
    border-color: #D4AF37;
    background-color: #FFFDF8;
    
    .pkg-name {
      color: #D4AF37;
    }
  }
}

.recommend-tag {
  position: absolute;
  top: -16rpx;
  right: -10rpx;
  background: linear-gradient(90deg, #FF6B6B 0%, #FF4757 100%);
  color: #FFF;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx 16rpx 16rpx 0;
  font-weight: bold;
}

.pkg-name {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.pkg-price {
  color: #333;
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
  
  .unit {
    font-size: 24rpx;
    margin-right: 4rpx;
  }
  
  .price-val {
    font-size: 48rpx;
    font-weight: bold;
  }
}

.pkg-origin {
  font-size: 22rpx;
  color: #999;
  text-decoration: line-through;
  margin-bottom: 8rpx;
}

.pkg-daily {
  font-size: 20rpx;
  color: #999;
  background-color: #F5F5F5;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.privilege-section {
  margin-top: 40rpx;
  padding: 0 30rpx;
}

.privilege-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx 10rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.privilege-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
  box-sizing: border-box;
  padding: 0 4rpx;
}

.icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  background: linear-gradient(135deg, #FFF6E5 0%, #FDF0D5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  flex-shrink: 0;
  
  text {
    font-size: 40rpx;
    color: #D4AF37;
  }
}

.privilege-name {
  font-size: 24rpx;
  color: #333;
  margin-bottom: 6rpx;
  font-weight: 500;
  white-space: nowrap;
}

.privilege-desc {
  font-size: 18rpx;
  color: #999;
  white-space: nowrap;
}

.bottom-pay-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #FFF;
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
    color: #333;
  }
  
  .total-unit {
    font-size: 24rpx;
    color: #FF4757;
    margin-right: 4rpx;
  }
  
  .total-price {
    font-size: 48rpx;
    font-weight: bold;
    color: #FF4757;
  }
}

.pay-btn {
  margin: 0;
  width: 360rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(90deg, #E6C887 0%, #D4AF37 100%);
  color: #333;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  box-shadow: 0 8rpx 16rpx rgba(212, 175, 55, 0.3);
  
  &::after {
    border: none;
  }
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>
