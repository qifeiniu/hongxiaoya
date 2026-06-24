<template>
  <view class="profile-container">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>

    <!-- 个人信息 -->
    <view class="user-card" @click="goProfileEdit" :style="{ paddingTop: (statusBarHeight + 15) + 'px' }">
      <view class="avatar-wrap">
        <image class="avatar" :src="profile.avatar || DEFAULT_AVATAR" mode="aspectFill"></image>
        <view class="auth-badge" v-if="profile.isRealAuth === 1">
          <text class="ri-shield-check-fill"></text> 已实名
        </view>
      </view>
      <view class="info">
        <view class="nickname-wrap">
          <text class="nickname">{{ profile.nickname || '点击登录/注册' }}</text>
          <text class="user-id-small" v-if="profile.nickname && profile.userId">ID:{{ profile.userId }}</text>
        </view>
        <view class="edit-link">
          <text>编辑资料</text>
          <text class="ri-arrow-right-s-line"></text>
        </view>
      </view>
    </view>

    <!-- VIP Banner -->
    <view class="vip-banner" @click="goVipCenter">
      <view class="vip-left">
        <view class="vip-title">开通VIP，解锁16项会员特权</view>
        <view class="vip-sub-row">
          <view class="vip-sub-item"><text class="ri-heart-3-fill"></text><text>免费查看心动</text></view>
          <view class="vip-sub-item"><text class="ri-eye-fill"></text><text>免费查看访客</text></view>
          <view class="vip-sub-item"><text class="ri-calendar-check-fill"></text><text>签到翻倍</text></view>
        </view>
      </view>
      <view class="vip-right">
        <view class="vip-v-icon">V</view>
        <view class="vip-btn">立即开通</view>
      </view>
    </view>

    <!-- 两张卡片：充值中心 & 签到 -->
    <view class="two-cards">
      <view class="card-item" @click="goEggRecharge">
        <view class="card-info">
          <text class="card-num">{{ wallet.balance }}</text>
          <text class="card-sub">充值中心 <text class="ri-arrow-right-s-line"></text></text>
        </view>
        <view class="card-icon-wrap bg-yellow">
          <text class="card-emoji-icon">🥚</text>
        </view>
      </view>
      <view class="card-item" @click="handleSignIn">
        <view class="card-info">
          <text class="card-title">每日签到</text>
          <text class="card-sub highlight-sub">赚取鸭蛋 <text class="ri-arrow-right-s-line"></text></text>
        </view>
        <view class="card-icon-wrap bg-purple">
          <text class="ri-calendar-check-fill"></text>
        </view>
      </view>
    </view>

    <!-- 任务 Banner (红娘专属通道) -->
    <view class="task-banner" @click="handleMatchmaker">
      <view class="task-top">
        <view class="task-title-wrap">
          <text class="ri-file-list-3-line task-icon"></text>
          <text class="task-text">红娘专属通道</text>
        </view>
        <view class="task-btn">进入申请</view>
      </view>
      <text class="task-sub">红娘老师组织线上相亲活动，享30%起活动报名费用佣金分成</text>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goActivityJoined">
        <view class="menu-left">
          <text class="ri-calendar-event-line menu-icon"></text>
          <text class="menu-text">已报名的活动</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goWalletDetail">
        <view class="menu-left">
          <text class="ri-money-cny-box-line menu-icon"></text>
          <text class="menu-text">我的钱包</text>
        </view>
        <view class="menu-right">
          <text class="menu-status highlight-status">红娘可提现</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goVerify">
        <view class="menu-left">
          <text class="ri-shield-user-line menu-icon"></text>
          <text class="menu-text">实名认证</text>
        </view>
        <view class="menu-right">
          <text class="menu-status" :class="{ 'status-ok': profile.isRealAuth === 1 }">{{ profile.isRealAuth === 1 ? '已认证' : '去认证' }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goEduVerify">
        <view class="menu-left">
          <text class="ri-graduation-cap-line menu-icon"></text>
          <text class="menu-text">学历认证</text>
        </view>
        <view class="menu-right">
          <text class="menu-status">去认证</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goAvatarVerify">
        <view class="menu-left">
          <text class="ri-camera-3-line menu-icon"></text>
          <text class="menu-text">头像认证</text>
        </view>
        <view class="menu-right">
          <text class="menu-status">去认证</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goBottle">
        <view class="menu-left">
          <text class="ri-message-3-line menu-icon"></text>
          <text class="menu-text">漂流瓶</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="handleInvite">
        <view class="menu-left">
          <text class="ri-user-add-line menu-icon"></text>
          <text class="menu-text">邀请好友注册</text>
        </view>
        <view class="menu-right">
          <text class="menu-status highlight-status">赚鸭蛋</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="handleCustomerService">
        <view class="menu-left">
          <text class="ri-customer-service-2-line menu-icon"></text>
          <text class="menu-text">联系客服</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="handleBlockList">
        <view class="menu-left">
          <text class="ri-forbid-line menu-icon"></text>
          <text class="menu-text">屏蔽列表</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="goMyPosts">
        <view class="menu-left">
          <text class="ri-image-edit-line menu-icon"></text>
          <text class="menu-text">动态管理</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
      <view class="menu-item" @click="handleSettings">
        <view class="menu-left">
          <text class="ri-settings-4-line menu-icon"></text>
          <text class="menu-text">设置</text>
        </view>
        <view class="menu-right">
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { request } from '../../utils/request'
import { onShow } from '@dcloudio/uni-app'
import { globalAuthGuard, requireLogin } from '../../utils/auth'
import { getRandomAvatar, DEFAULT_AVATAR } from '../../utils/mockData'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
const wallet = ref({
  balance: 0,
  tempBalance: 0,
  permanentBalance: 0
})

const profile = ref({
  nickname: '',
  avatar: '',
  userId: 999,
  isRealAuth: 0,
  isVip: false,
  isMatchmaker: false
})

const handleSettings = () => {
  if (!requireLogin('进入设置')) return
  uni.navigateTo({ url: '/pages/settings/index' })
}

const goMyPosts = () => {
  if (!requireLogin('管理动态')) return
  uni.navigateTo({ url: '/pages/my-posts/index' })
}

const handleBlockList = () => {
  if (!requireLogin('查看屏蔽列表')) return
  uni.navigateTo({ url: '/pages/block-list/index' })
}

const fetchProfile = async () => {
  try {
    const res = await request({ url: '/profile/info', method: 'GET' })
    if (res) {
      let nickname = res.nickname || '神秘鸭鸭'
      const defaultPattern = /^(红小鸭|微信用户|红小鸭用户)\d+$/
      if (defaultPattern.test(nickname)) {
        nickname = nickname.replace(/\d+$/, '')
      }
      profile.value.nickname = nickname
      profile.value.avatar = res.avatar || DEFAULT_AVATAR
      profile.value.isRealAuth = res.isRealAuth || 0
      profile.value.isVip = Boolean(res.isVip)
      profile.value.isMatchmaker = Boolean(res.isMatchmaker)
      profile.value.userId = res.userId || 999
    }
  } catch (e) {}
}

const fetchWallet = async () => {
  try {
    const res = await request({ url: '/wallet/info', method: 'GET' })
    wallet.value = res || wallet.value
  } catch (e) {}
}

const handleSignIn = () => {
  if (!requireLogin('每日签到')) return
  uni.navigateTo({ url: '/pages/sign-in/index' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#6A61F8',
    success: (res) => {
      if(res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('isProfileComplete')
        profile.value = { nickname: '', avatar: '', userId: 0, isRealAuth: 0, isVip: false, isMatchmaker: false }
        wallet.value = { balance: 0, tempBalance: 0, permanentBalance: 0 }
        uni.showToast({ title: '已退出登录', icon: 'none' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 500)
      }
    }
  })
}

const goBottle = () => {
  if (!requireLogin('使用漂流瓶')) return
  uni.navigateTo({ url: '/pages/bottle/index' })
}

const goVipCenter = () => {
  if (!requireLogin('开通VIP')) return
  uni.navigateTo({ url: '/pages/vip-recharge/index' })
}

const goEggRecharge = () => {
  if (!requireLogin('充值鸭蛋')) return
  uni.navigateTo({ url: '/pages/egg-recharge/index' })
}

const goWalletDetail = () => {
  if (!requireLogin('查看钱包')) return
  uni.navigateTo({ url: '/pages/wallet-detail/index' })
}

const goActivityJoined = () => {
  if (!requireLogin('查看报名的活动')) return
  uni.navigateTo({ url: '/pages/activity-joined/index' })
}

const goVerify = () => {
  if (!requireLogin('进行实名认证')) return
  uni.navigateTo({ url: '/pages/auth-verify/index' })
}

const goProfileEdit = () => {
  if (!requireLogin('修改资料')) return
  uni.navigateTo({ url: '/pages/profile-edit/index' })
}

const handleInvite = () => {
  if (!requireLogin('邀请好友')) return
  uni.navigateTo({ url: '/pages/invite-friends/index' })
}

const handleMatchmaker = () => {
  if (!requireLogin('使用红娘通道')) return
  if (profile.value.isMatchmaker) {
    uni.navigateTo({ url: '/pages/matchmaker-center/index' })
  } else {
    uni.navigateTo({ url: '/pages/matchmaker-apply/index' })
  }
}

const handleCustomerService = () => {
  if (!requireLogin('联系客服')) return
  uni.navigateTo({ url: '/pages/customer-service/index' })
}

const goEduVerify = () => {
  if (!requireLogin('进行学历认证')) return
  uni.navigateTo({ url: '/pages/edu-verify/index' })
}

const goAvatarVerify = () => {
  if (!requireLogin('进行头像认证')) return
  uni.navigateTo({ url: '/pages/avatar-verify/index' })
}

onShow(() => {
  if (!globalAuthGuard()) return
  if (uni.getStorageSync('token')) {
    fetchWallet()
    fetchProfile()
  } else {
    profile.value = { nickname: '', avatar: '', userId: 0, isRealAuth: 0, isVip: false, isMatchmaker: false }
    wallet.value = { balance: 0, tempBalance: 0, permanentBalance: 0 }
  }
})
</script>

<style lang="scss" scoped>
.profile-container {
  background-color: #F4F5F9;
  min-height: calc(100vh - var(--window-bottom));
  padding-bottom: 40rpx;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 450rpx;
  background: linear-gradient(180deg, #E8EAFF 0%, #F4F5F9 100%);
  z-index: 0;
}

.user-card {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 0 40rpx 40rpx;
}

.avatar-wrap {
  position: relative;
  margin-right: 30rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid #FFF;
  background-color: #EEE;
}

.auth-badge {
  position: absolute;
  bottom: -10rpx;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(90deg, #B37FEB 0%, #6A61F8 100%);
  color: #FFF;
  font-size: 18rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  white-space: nowrap;
  display: flex;
  align-items: center;
  border: 2rpx solid #FFF;
}

.auth-badge text {
  margin-right: 4rpx;
  font-size: 20rpx;
}

.info {
  flex: 1;
}

.nickname-wrap {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.nickname {
  font-size: 44rpx;
  font-weight: bold;
  color: #333;
}

.user-id-small {
  font-size: 36rpx;
  color: #333;
  font-weight: normal;
  margin-left: 12rpx;
}

.tags {
  display: flex;
  align-items: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
  font-weight: 500;
}

.id-tag {
  background-color: #F5F5F5;
  color: #666;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.edit-link {
  display: inline-flex;
  align-items: center;
  color: #999;
  font-size: 24rpx;
  margin-top: 8rpx;
  background-color: rgba(0,0,0,0.03);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.edit-link text:last-child {
  font-size: 28rpx;
  margin-left: 4rpx;
}

.vip-banner {
  position: relative;
  z-index: 1;
  margin: 0 30rpx 30rpx;
  background: linear-gradient(90deg, #241E38 0%, #1D1730 100%);
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(36, 30, 56, 0.2);
}

.vip-left {
  flex: 1;
}

.vip-title {
  color: #F2D3A8;
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  white-space: nowrap;
}

.vip-sub-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.vip-sub-item {
  display: flex;
  align-items: center;
  color: rgba(242, 211, 168, 0.8);
  font-size: 20rpx;
  white-space: nowrap;
}

.vip-sub-item text:first-child {
  margin-right: 6rpx;
}

.vip-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  position: relative;
}

.vip-v-icon {
  position: absolute;
  top: -40rpx;
  right: 0;
  font-size: 80rpx;
  font-weight: 900;
  color: rgba(242, 211, 168, 0.8);
  font-style: italic;
  text-shadow: 2rpx 4rpx 8rpx rgba(0,0,0,0.5);
}

.vip-btn {
  background: linear-gradient(90deg, #F9E6C9 0%, #F4DEBB 100%);
  color: #241E38;
  font-size: 26rpx;
  font-weight: bold;
  padding: 12rpx 28rpx;
  border-radius: 30rpx;
  margin-top: 40rpx;
}

.two-cards {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;
}

.card-item {
  flex: 1;
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.card-info {
  display: flex;
  flex-direction: column;
}

.card-num {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.card-sub {
  font-size: 24rpx;
  color: #999;
  display: flex;
  align-items: center;
}

.card-sub text {
  font-size: 28rpx;
}

.highlight-sub {
  color: #6A61F8;
}

.card-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon-wrap text {
  font-size: 40rpx;
}

.card-emoji-icon {
  font-size: 44rpx !important;
}

.bg-yellow {
  background: linear-gradient(135deg, #FFF7E8 0%, #FFE8C2 100%);
  color: #FA8C16;
}

.bg-purple {
  background: linear-gradient(135deg, #F2F3FF 0%, #E0E2FF 100%);
  color: #6A61F8;
}

.task-banner {
  position: relative;
  z-index: 1;
  margin: 0 30rpx 30rpx;
  background-color: #FFF;
  border-radius: 24rpx;
  padding: 24rpx 30rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.task-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.task-title-wrap {
  display: flex;
  align-items: center;
}

.task-sub {
  font-size: 20rpx;
  color: #999;
  margin-top: 12rpx;
  line-height: 1.4;
  align-self: flex-start;
}

.task-icon {
  font-size: 36rpx;
  color: #6A61F8;
  margin-right: 16rpx;
}

.task-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.task-btn {
  background: #6A61F8;
  color: #FFF;
  font-size: 24rpx;
  padding: 12rpx 30rpx;
  border-radius: 30rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.quick-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30rpx 12rpx;
  margin: 0 30rpx 30rpx;
  padding: 30rpx 20rpx;
  background: #FFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.quick-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

.quick-icon text {
  font-size: 48rpx;
}

.quick-title {
  font-size: 24rpx;
  color: #666;
}

.menu-list {
  position: relative;
  z-index: 1;
  background-color: #FFF;
  margin: 0 30rpx 30rpx;
  border-radius: 24rpx;
  padding: 10rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  position: relative;
}

.menu-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 80rpx;
  right: 30rpx;
  height: 1rpx;
  background-color: #F5F5F5;
}

.menu-item:last-child::after {
  display: none;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 40rpx;
  color: #333;
  margin-right: 20rpx;
}

.menu-text {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.menu-right {
  display: flex;
  align-items: center;
}

.menu-status {
  font-size: 24rpx;
  color: #999;
  margin-right: 10rpx;
}

.status-ok {
  color: #52C41A;
}

.highlight-status {
  color: #FA8C16;
}

.arrow {
  color: #CCC;
  font-size: 36rpx;
}

.logout-btn {
  margin: 0 30rpx 40rpx;
  background-color: #FFF;
  border-radius: 24rpx;
  height: 100rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.logout-btn text {
  color: #FF4D4F;
  font-size: 32rpx;
  font-weight: 500;
}
</style>
