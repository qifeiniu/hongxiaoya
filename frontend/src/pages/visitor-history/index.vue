<template>
  <view class="page-container">
    <!-- 访客列表 -->
    <scroll-view scroll-y class="user-list">
      <view class="grid-container">
        <view class="user-card" v-for="user in visitorList" :key="user.id">
          <image class="bg-image" :class="{'blur': !isVip && !user.isUnlocked}" :src="user.visitorProfile?.avatar" mode="aspectFill"></image>
          <view class="card-content" @click="isVip || user.isUnlocked ? goDetail(user) : showUnlockPopup(user)">
            <view class="user-info">
              <view class="name-wrap">
                <text class="user-name" :class="{ 'blur-text': !isVip && !user.isUnlocked }">
                  {{ isVip || user.isUnlocked ? user.visitorProfile?.nickname : '神秘访客' }}
                </text>
                <text class="user-id-small" v-if="(isVip || user.isUnlocked) && user.visitorProfile?.userId">{{ user.visitorProfile.userId }}</text>
              </view>
              <text class="user-meta">{{ user.visitorProfile?.height || 165 }}cm · {{ user.visitorProfile?.occupation || '自由职业' }} · {{ user.visitorProfile?.education || '本科' }}</text>
              <text class="user-meta">{{ formatTime(user.visitTime) }}来访</text>
              <text class="user-meta highlight">刚刚在线</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="visitorList.length === 0">
        <text class="ri-user-search-line empty-icon"></text>
        <text class="empty-text">最近还没有人看过你，多发动态增加曝光吧</text>
      </view>
    </scroll-view>

    <!-- 解锁弹窗 -->
    <view class="unlock-popup-mask" v-if="showPopup" @click="showPopup = false">
      <view class="unlock-popup-content" @click.stop>
        <view class="popup-header">
          <view class="header-text">
            <view class="popup-title">查看对方资料</view>
            <view class="popup-subtitle">送出喜欢，即可开聊</view>
          </view>
          <view class="close-btn" @click="showPopup = false">
            <text class="ri-close-line"></text>
          </view>
        </view>
        
        <view class="popup-user-info">
          <view class="avatar-lock-col">
            <image class="locked-avatar blur" :src="currentUnlockTarget?.visitorProfile?.avatar || '/static/avatars/default-avatar.png'" mode="aspectFill"></image>
            <view class="lock-icon-overlay">
              <text class="ri-lock-fill"></text>
            </view>
          </view>
          <view class="info-text-col">
            <view class="info-row">{{ currentUnlockTarget?.visitorProfile?.height || 165 }}cm·{{ currentUnlockTarget?.visitorProfile?.occupation || '自由职业' }}·{{ currentUnlockTarget?.visitorProfile?.education || '本科' }}</view>
            <view class="info-row-time">{{ formatTime(currentUnlockTarget?.visitTime) }}来访</view>
          </view>
        </view>
        
        <view class="popup-buttons">
          <view class="btn-half btn-unlock-single" @click="handleSingleUnlock">
            只看ta(50🥚)
          </view>
          <view class="btn-half btn-unlock-vip" @click="handleUnlockAll">
            VIP解锁全部
          </view>
        </view>
      </view>
    </view>
    <!-- 底部固定开通会员按钮 -->
    <view class="fixed-bottom-btn" v-if="!isVip">
      <button class="unlock-all-btn" @click="goVipRecharge">
        <text class="btn-main-text">解锁全部访客</text>
      </button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { request } from '../../utils/request'
import { getRandomAvatar } from '../../utils/mockData'

const isVip = ref(false)
const visitorList = ref<any[]>([])
const showPopup = ref(false)
const currentUnlockTarget = ref<any>(null)

const showUnlockPopup = (user: any) => {
  currentUnlockTarget.value = user
  showPopup.value = true
}

const handleSingleUnlock = async () => {
  showPopup.value = false
  const user = currentUnlockTarget.value
  if (!user) return
  
  try {
    uni.showLoading({ title: '解锁中...' })
    await request({ url: '/relation/unlock-visitor', method: 'POST', data: { visitorId: user.visitorProfile.userId } })
    uni.hideLoading()
    uni.showToast({ title: '解锁成功', icon: 'success' })
    user.isUnlocked = true
  } catch (e: any) {
    uni.hideLoading()
    if (e.message && e.message.includes('余额不足') || e.code === 4002) {
      uni.showModal({
        title: '鸭蛋不足',
        content: '您的鸭蛋余额不足，是否前往充值？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/egg-recharge/index' })
          }
        }
      })
    } else {
      uni.showToast({ title: '解锁失败', icon: 'none' })
    }
  }
}

const handleUnlockAll = () => {
  showPopup.value = false
  goVipRecharge()
}

onMounted(async () => {
  // 获取 VIP 状态
  try {
    const profile: any = await request({ url: '/profile/info', method: 'GET' })
    isVip.value = profile?.isVip === 1
  } catch (e) {
    // Mock
    isVip.value = false
  }
  
  // 无论是否VIP都获取访客列表
  fetchVisitors()
})

const fetchVisitors = async () => {
  try {
    const res: any = await request({ url: '/relation/visitors', method: 'GET' })
    if (res && res.length > 0) {
      visitorList.value = res
    } else {
      setMockData()
    }
  } catch (e) {
    setMockData()
  }
}

const setMockData = () => {
  visitorList.value = [
    { id: 1, visitTime: new Date().toISOString(), visitorProfile: { userId: 401, nickname: '清风', avatar: getRandomAvatar(1, 'female'), age: 24, location: '北京' }, isUnlocked: false },
    { id: 2, visitTime: new Date(Date.now() - 3600000).toISOString(), visitorProfile: { userId: 402, nickname: '小鱼', avatar: getRandomAvatar(2, 'female'), age: 22, location: '上海' }, isUnlocked: true },
    { id: 3, visitTime: new Date(Date.now() - 86400000).toISOString(), visitorProfile: { userId: 403, nickname: '阳光男孩', avatar: getRandomAvatar(3, 'male'), age: 26, location: '深圳' }, isUnlocked: false },
    { id: 4, visitTime: new Date(Date.now() - 172800000).toISOString(), visitorProfile: { userId: 404, nickname: '冬雪', avatar: getRandomAvatar(4, 'female'), age: 23, location: '广州' }, isUnlocked: false },
    { id: 5, visitTime: new Date(Date.now() - 259200000).toISOString(), visitorProfile: { userId: 405, nickname: '秋叶', avatar: getRandomAvatar(5, 'male'), age: 28, location: '成都' }, isUnlocked: false },
    { id: 6, visitTime: new Date(Date.now() - 345600000).toISOString(), visitorProfile: { userId: 406, nickname: '星辰', avatar: getRandomAvatar(6, 'female'), age: 25, location: '杭州' }, isUnlocked: false },
    { id: 7, visitTime: new Date(Date.now() - 432000000).toISOString(), visitorProfile: { userId: 407, nickname: '大海', avatar: getRandomAvatar(7, 'male'), age: 29, location: '武汉' }, isUnlocked: false },
    { id: 8, visitTime: new Date(Date.now() - 518400000).toISOString(), visitorProfile: { userId: 408, nickname: '微光', avatar: getRandomAvatar(8, 'female'), age: 21, location: '南京' }, isUnlocked: false },
    { id: 9, visitTime: new Date(Date.now() - 604800000).toISOString(), visitorProfile: { userId: 409, nickname: '远方', avatar: getRandomAvatar(9, 'male'), age: 27, location: '西安' }, isUnlocked: false },
    { id: 10, visitTime: new Date(Date.now() - 691200000).toISOString(), visitorProfile: { userId: 410, nickname: '清音', avatar: getRandomAvatar(10, 'female'), age: 24, location: '苏州' }, isUnlocked: false }
  ]
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  // 如果有非ISO格式直接返回
  if (!timeStr.includes('T')) return timeStr
  
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 3600000) {
    return Math.max(1, Math.floor(diff / 60000)) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else if (diff < 172800000) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

const goVipRecharge = () => {
  uni.navigateTo({ url: '/pages/vip-recharge/index' })
}

const goDetail = (user: any) => {
  if (!isVip.value && !user.isUnlocked) {
    showUnlockPopup(user)
    return
  }
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.visitorProfile.userId}` })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $uni-bg-color-page;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.user-list {
  flex: 1;
  padding: 20rpx;
  padding-bottom: 180rpx; /* 留出底部按钮空间 */
  box-sizing: border-box;
  width: 100%;
}

.grid-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.user-card {
  width: calc(50% - 10rpx);
  height: 440rpx;
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}
.bg-image.blur {
  filter: blur(10px);
  transform: scale(1.1);
}

.card-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 40%, rgba(0,0,0,0.8) 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  padding: 30rpx 10rpx;
  box-sizing: border-box;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.name-wrap {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
  justify-content: center;
}

.user-name { font-size: 28rpx; font-weight: 600; color: #fff; }
.blur-text { filter: blur(4px); user-select: none; }
.user-id-small { font-size: 24rpx; color: rgba(255, 255, 255, 0.8); margin-left: 12rpx; font-weight: normal; }
.user-meta { font-size: 22rpx; color: rgba(255, 255, 255, 0.8); margin-bottom: 6rpx; text-align: center; }
.user-meta.highlight { color: #FFD700; }

.empty-state { text-align: center; padding: 120rpx 0; width: 100%; }
.empty-icon { font-size: 100rpx; color: $uni-text-color-placeholder; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: $uni-text-color-placeholder; padding: 0 60rpx; }

/* 解锁弹窗样式 */
.unlock-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.unlock-popup-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  padding: 60rpx 40rpx calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  animation: slideUp 0.3s ease-out forwards;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;
}

.header-text {
  display: flex;
  flex-direction: column;
}

.popup-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 8rpx;
}

.popup-subtitle {
  font-size: 26rpx;
  color: #999;
}

.close-btn {
  padding: 10rpx;
  margin-right: -10rpx;
  margin-top: -10rpx;
}

.close-btn .ri-close-line {
  font-size: 44rpx;
  color: #999;
}

.popup-user-info {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  background: #F8F9FA;
  padding: 30rpx;
  border-radius: 24rpx;
  margin-bottom: 40rpx;
  border: 1rpx solid rgba(0,0,0,0.03);
}

.avatar-lock-col {
  position: relative;
  width: 110rpx;
  height: 110rpx;
  margin-right: 30rpx;
  flex-shrink: 0;
}

.info-text-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.info-row {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.info-row-time {
  font-size: 24rpx;
  color: #999;
}

.locked-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  filter: blur(4px);
}

.lock-icon-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
}

.lock-icon-overlay .ri-lock-fill {
  color: #fff;
  font-size: 40rpx;
}

.popup-buttons {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.btn-half {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 44rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.btn-unlock-single {
  background: #F0EFFF;
  color: #6A61F8;
}

.btn-unlock-vip {
  background: linear-gradient(90deg, #6A61F8 0%, #8E87F9 100%);
  color: #fff;
}

.fixed-bottom-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20rpx 40rpx calc(20rpx + env(safe-area-inset-bottom));
  background: transparent;
  box-sizing: border-box;
  z-index: 100;
  pointer-events: none; /* 让事件穿透外层容器 */
}

.unlock-all-btn {
  background: linear-gradient(90deg, #6A61F8 0%, #8E87F9 100%);
  border-radius: 60rpx;
  height: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(106, 97, 248, 0.3);
  pointer-events: auto; /* 恢复按钮本身的点击事件 */
}

.unlock-all-btn .btn-main-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
}
</style>
