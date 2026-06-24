<template>
  <view class="who-likes-me-container">
    <view class="header">
      <text class="title">喜欢我的人</text>
      <text class="sub-title">他们都在偷偷关注你</text>
    </view>

    <!-- 列表展示 -->
    <scroll-view scroll-y class="list-view">
      <view class="grid-container">
        <view class="user-card" v-for="item in list" :key="item.id">
          <image class="bg-image" :class="{'blur': !isVip && !item.isUnlocked}" :src="item.userProfile?.avatar" mode="aspectFill"></image>
          <view class="card-content" @click="item.isUnlocked || isVip ? goUserProfile(item.userProfile) : showUnlockPopup(item)">
            <view class="user-info">
              <view class="name-wrap">
                <text class="user-name" :class="{ 'blur-text': !isVip && !item.isUnlocked }">
                  {{ isVip || item.isUnlocked ? item.userProfile?.nickname : '神秘人' }}
                </text>
                <text class="user-id-small" v-if="(isVip || item.isUnlocked) && item.userProfile?.userId">{{ item.userProfile.userId }}</text>
              </view>
              <text class="user-meta">{{ item.userProfile?.height || 165 }}cm · {{ item.userProfile?.occupation || '自由职业' }} · {{ item.userProfile?.education || '本科' }}</text>
              <text class="user-meta">{{ formatTime(item.createdAt) }}来访</text>
              <text class="user-meta highlight">刚刚在线</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="list.length === 0">
        <image class="empty-img" src="/static/hongxiaoya-logo.png" mode="aspectFit"></image>
        <text class="empty-text">暂时还没有人喜欢你，去多发发动态吧~</text>
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
            <image class="locked-avatar blur" :src="currentUnlockTarget?.userProfile?.avatar || '/static/avatars/default-avatar.png'" mode="aspectFill"></image>
            <view class="lock-icon-overlay">
              <text class="ri-lock-fill"></text>
            </view>
          </view>
          <view class="info-text-col">
            <view class="info-row">{{ currentUnlockTarget?.userProfile?.height || 165 }}cm·{{ currentUnlockTarget?.userProfile?.occupation || '自由职业' }}·{{ currentUnlockTarget?.userProfile?.education || '本科' }}</view>
            <view class="info-row-time">{{ formatTime(currentUnlockTarget?.createdAt) }}来访</view>
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
        <text class="btn-main-text">解锁全部喜欢我的人</text>
      </button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { request } from '../../utils/request'
import { checkAuth } from '../../utils/auth'

const isVip = ref(false) 
const list = ref<any[]>([])
const showPopup = ref(false)
const currentUnlockTarget = ref<any>(null)

const showUnlockPopup = (item: any) => {
  currentUnlockTarget.value = item
  showPopup.value = true
}

const handleSingleUnlock = async () => {
  showPopup.value = false
  const item = currentUnlockTarget.value
  if (!item) return
  
  try {
    uni.showLoading({ title: '解锁中...' })
    await request({
      url: `/relation/unlock-who-likes-me?targetId=${item.userProfile.userId}`,
      method: 'POST'
    })
    uni.hideLoading()
    uni.showToast({ title: '解锁成功', icon: 'success' })
    fetchList()
  } catch (e: any) {
    uni.hideLoading()
    // 假设后端余额不足时返回特定的状态码或错误信息，这里统一处理为提示充值鸭蛋
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
    }
  }
}

const handleUnlockAll = () => {
  showPopup.value = false
  goVipRecharge()
}

onMounted(async () => {
  fetchVipInfo()
  fetchList()
})

const fetchVipInfo = async () => {
  try {
    const res: any = await request({ url: '/vip/info', method: 'GET' })
    if (res && res.expireAt && new Date(res.expireAt).getTime() > Date.now()) {
      isVip.value = true
    }
  } catch (e) {}
}

const fetchList = async () => {
  try {
    const res: any = await request({ url: '/relation/who-likes-me', method: 'GET' })
    if (res && res.length > 0) {
      list.value = res
      return
    }
  } catch (e) {
    console.error('Fetch liked me list failed', e)
  }
  
  // 模拟多条喜欢我的数据
  import('../../utils/mockData').then(({ getRandomAvatar }) => {
    list.value = [
      { id: 1, createdAt: new Date().toISOString(), userProfile: { userId: 501, nickname: '春风', avatar: getRandomAvatar(1, 'female'), height: 165, occupation: '设计师', education: '本科' }, isUnlocked: false },
      { id: 2, createdAt: new Date(Date.now() - 3600000).toISOString(), userProfile: { userId: 502, nickname: '夏雨', avatar: getRandomAvatar(2, 'female'), height: 168, occupation: '老师', education: '硕士' }, isUnlocked: true },
      { id: 3, createdAt: new Date(Date.now() - 86400000).toISOString(), userProfile: { userId: 503, nickname: '秋叶', avatar: getRandomAvatar(3, 'male'), height: 175, occupation: '工程师', education: '本科' }, isUnlocked: false },
      { id: 4, createdAt: new Date(Date.now() - 172800000).toISOString(), userProfile: { userId: 504, nickname: '冬雪', avatar: getRandomAvatar(4, 'female'), height: 162, occupation: '运营', education: '大专' }, isUnlocked: false },
      { id: 5, createdAt: new Date(Date.now() - 259200000).toISOString(), userProfile: { userId: 505, nickname: '阳光', avatar: getRandomAvatar(5, 'male'), height: 180, occupation: '产品经理', education: '本科' }, isUnlocked: false },
      { id: 6, createdAt: new Date(Date.now() - 345600000).toISOString(), userProfile: { userId: 506, nickname: '星辰', avatar: getRandomAvatar(6, 'female'), height: 166, occupation: '摄影师', education: '本科' }, isUnlocked: false },
      { id: 7, createdAt: new Date(Date.now() - 432000000).toISOString(), userProfile: { userId: 507, nickname: '云海', avatar: getRandomAvatar(7, 'male'), height: 178, occupation: '律师', education: '硕士' }, isUnlocked: false },
      { id: 8, createdAt: new Date(Date.now() - 518400000).toISOString(), userProfile: { userId: 508, nickname: '微光', avatar: getRandomAvatar(8, 'female'), height: 160, occupation: '护士', education: '大专' }, isUnlocked: false },
      { id: 9, createdAt: new Date(Date.now() - 604800000).toISOString(), userProfile: { userId: 509, nickname: '远方', avatar: getRandomAvatar(9, 'male'), height: 182, occupation: '程序员', education: '本科' }, isUnlocked: false },
      { id: 10, createdAt: new Date(Date.now() - 691200000).toISOString(), userProfile: { userId: 510, nickname: '清音', avatar: getRandomAvatar(10, 'female'), height: 164, occupation: '编辑', education: '本科' }, isUnlocked: false }
    ]
  })
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return Math.floor(diff / 86400000) + '天前'
}

const goVipRecharge = () => {
  uni.navigateTo({ url: '/pages/vip-recharge/index' })
}

const goChat = async (user: any) => {
  if (!(await checkAuth('聊天'))) return
  uni.navigateTo({
    url: `/pages/chat/index?id=${user.userId}&name=${user.nickname}`
  })
}

const goUserProfile = (userProfile: any) => {
  if (!userProfile?.userId) return
  uni.navigateTo({
    url: `/pages/user-detail/index?id=${userProfile.userId}`
  })
}
</script>

<style lang="scss" scoped>
.who-likes-me-container {
  background-color: $uni-bg-color-page;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.list-view {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  width: 100%;
  padding-bottom: 180rpx; /* 留出底部按钮空间 */
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

.action-area {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

.chat-btn-small {
  background: $uni-color-primary-gradient;
  color: #fff;
  font-size: 22rpx;
  padding: 0 20rpx;
  height: 48rpx;
  line-height: 48rpx;
  border-radius: 24rpx;
  border: none;
}

.empty-state {
  padding: 100rpx 0;
  text-align: center;
  width: 100%;
}

.empty-img {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: $uni-text-color-placeholder;
}

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
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  line-height: 1.2;
}

.unlock-all-btn .btn-sub-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 22rpx;
  line-height: 1.2;
}
</style>
