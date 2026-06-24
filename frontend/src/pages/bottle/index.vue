<template>
  <view class="bottle-container">
    <!-- 背景层 -->
    <image class="bg-image" src="/static/bottle-bg.jpg" mode="aspectFill"></image>

    <!-- 自定义导航栏 -->
    <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <text class="ri-arrow-left-s-line back-icon"></text>
      </view>
      <view class="nav-right" @click="goRecord">
        <text class="record-text">记录</text>
      </view>
    </view>

    <!-- 交互区域 -->
    <view class="action-area">
      <text class="title">漂流瓶</text>
      <text class="sub-title">捞起一段缘分，或者扔出你的心事</text>

      <view class="bottle-actions">
        <view class="btn-wrap" @click="handlePick">
          <view class="circle-btn pick-btn">
            <image 
              src="/static/bottle-net.png" 
              class="custom-icon net-icon" 
              mode="aspectFit"
            ></image>
            <!-- 用于飞出的幽灵捞网 -->
            <image 
              v-if="isPicking"
              src="/static/bottle-net.png" 
              class="custom-icon net-icon ghost-icon animating-pick" 
              mode="aspectFit"
            ></image>
          </view>
          <text class="btn-text">捞一个</text>
          <text class="limit-text">超出扣10鸭蛋</text>
        </view>
        
        <view class="btn-wrap" @click="openThrowModal">
          <view class="circle-btn throw-btn">
            <image 
              src="/static/bottle-throw.png" 
              class="custom-icon bottle-icon" 
              mode="aspectFit"
            ></image>
            <!-- 用于飞出的幽灵瓶子 -->
            <image 
              v-if="isThrowing"
              src="/static/bottle-throw.png" 
              class="custom-icon bottle-icon ghost-icon animating-throw" 
              mode="aspectFit"
            ></image>
          </view>
          <text class="btn-text">扔一个</text>
          <text class="limit-text">超出扣10鸭蛋</text>
        </view>
      </view>
    </view>

    <!-- 扔瓶子弹窗 -->
    <view class="modal-mask" v-if="showThrowModal" @click="showThrowModal = false"></view>
    <view class="modal-content throw-modal" v-if="showThrowModal">
      <view class="modal-header">写下你的心事</view>
      <textarea 
        class="bottle-input" 
        v-model="bottleText" 
        placeholder="想对陌生的TA说点什么？" 
        maxlength="100"
      ></textarea>
      <text class="word-count">{{ bottleText.length }}/100</text>
      <view class="modal-btns">
        <button class="cancel" @click="showThrowModal = false">取消</button>
        <button class="confirm" @click="submitThrow">扔进海里</button>
      </view>
    </view>

    <!-- 捞到瓶子弹窗 -->
    <view class="modal-mask" v-if="showPickedModal" @click="showPickedModal = false"></view>
    <view class="modal-content picked-modal" v-if="showPickedModal">
      <view class="modal-header">捞到了一个漂流瓶 <text class="ri-drop-line" style="color: #00BFFF; margin-left: 10rpx;"></text></view>
      <view class="picked-info">
        <image class="avatar" :src="pickedBottle.avatar" mode="aspectFill"></image>
        <text class="nickname">{{ pickedBottle.nickname }}</text>
        <text class="user-id-small" v-if="pickedBottle.userId">{{ pickedBottle.userId }}</text>
      </view>
      <view class="picked-text">{{ pickedBottle.content }}</view>
      <view class="modal-btns">
        <button class="cancel" @click="showPickedModal = false">扔回海里</button>
        <button class="confirm" @click="replyBottle">回复TA</button>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { request } from '../../utils/request'
import { checkAuth } from '../../utils/auth'

const showThrowModal = ref(false)
const showPickedModal = ref(false)
const bottleText = ref('')
const pickedBottle = ref<any>({})

const isPicking = ref(false)
const isThrowing = ref(false)

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0

const goBack = () => {
  uni.navigateBack()
}

const goRecord = () => {
  uni.navigateTo({ url: '/pages/bottle/record' })
}

const openThrowModal = async () => {
  if (!(await checkAuth('扔漂流瓶'))) return
  showThrowModal.value = true
}

const handlePick = async () => {
  if (!(await checkAuth('捞漂流瓶'))) return
  
  if (isPicking.value) return;
  isPicking.value = true;
  
  const animationDuration = 1500;
  const startTime = Date.now();

  try {
    const res: any = await request({ url: '/bottle/pick', method: 'GET' })
    let bottleData = res;
    if (res && res.code) {
      bottleData = res.data;
    }
    
    // 计算剩余等待时间，确保动画播放完毕
    const elapsedTime = Date.now() - startTime;
    const remainingTime = Math.max(0, animationDuration - elapsedTime);
    
    setTimeout(() => {
      isPicking.value = false;
      if (bottleData && bottleData.id) {
        pickedBottle.value = bottleData
        showPickedModal.value = true
      } else {
        uni.showToast({ title: '这次什么也没捞到，换个姿势试试~', icon: 'none' })
      }
    }, remainingTime);
    
  } catch (e) {
    // 请求失败也需要重置状态
    const elapsedTime = Date.now() - startTime;
    const remainingTime = Math.max(0, animationDuration - elapsedTime);
    setTimeout(() => {
      isPicking.value = false;
    }, remainingTime);
  }
}

const submitThrow = async () => {
  if (!bottleText.value.trim()) {
    return uni.showToast({ title: '内容不能为空', icon: 'none' })
  }
  
  // 1. 先关闭弹窗
  showThrowModal.value = false
  
  // 2. 稍微延迟一点点，等弹窗关闭动画执行，再触发扔瓶子动画
  setTimeout(async () => {
    isThrowing.value = true
    
    const animationDuration = 1500;
    const startTime = Date.now();

    try {
      await request({ 
        url: '/bottle/throw', 
        method: 'POST', 
        data: { content: bottleText.value } 
      });
      
      bottleText.value = '';
      
      const elapsedTime = Date.now() - startTime;
      const remainingTime = Math.max(0, animationDuration - elapsedTime);

      // 4. 动画结束后重置状态并提示
      setTimeout(() => {
        isThrowing.value = false;
        uni.showToast({ title: '扔出成功！', icon: 'success' })
      }, remainingTime);

    } catch (e) {
      // 如果请求失败，等动画结束恢复状态
      const elapsedTime = Date.now() - startTime;
      const remainingTime = Math.max(0, animationDuration - elapsedTime);
      setTimeout(() => {
        isThrowing.value = false;
      }, remainingTime);
    }

  }, 300);
}

const replyBottle = async () => {
  if (!(await checkAuth('聊天'))) return
  showPickedModal.value = false
  uni.navigateTo({
    url: `/pages/chat/index?id=${pickedBottle.value.userId}&name=${pickedBottle.value.nickname}`
  })
}
</script>

<style lang="scss" scoped>
.bottle-container {
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: #0F172A;
  display: flex;
  flex-direction: column;
}

.bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 导航栏 */
.custom-nav {
  position: relative;
  z-index: 20;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 88rpx;
  padding: 0 30rpx;
}

.nav-left {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.back-icon {
  font-size: 40rpx;
  color: #FFFFFF;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 10rpx 28rpx;
  border-radius: 100rpx;
  backdrop-filter: blur(10px);
}

.record-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 交互区域 */
.action-area {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 15vh;
}

.title {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: bold;
  margin-bottom: 24rpx;
  letter-spacing: 6rpx;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.sub-title {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 160rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
  font-weight: normal;
  letter-spacing: 2rpx;
}

.bottle-actions {
  display: flex;
  width: 100%;
  justify-content: center;
  gap: 160rpx;
}

.btn-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.circle-btn {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 4rpx solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  position: relative;
}

/* 浮动动画 */
@keyframes float1 {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
  100% { transform: translateY(0px); }
}

@keyframes float2 {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-12px); }
  100% { transform: translateY(0px); }
}

.pick-btn {
  background: linear-gradient(135deg, rgba(142, 134, 255, 0.9) 0%, rgba(106, 97, 248, 0.9) 100%);
  box-shadow: 0 16rpx 32rpx rgba(106, 97, 248, 0.3);
  animation: float1 4s ease-in-out infinite;
}

.throw-btn {
  background: linear-gradient(135deg, rgba(142, 134, 255, 0.9) 0%, rgba(106, 97, 248, 0.9) 100%);
  box-shadow: 0 16rpx 32rpx rgba(106, 97, 248, 0.3);
  animation: float2 3.5s ease-in-out infinite 0.5s;
}

.circle-btn:active {
  animation-play-state: paused;
  transform: scale(0.9) !important;
}

.custom-icon {
  width: 140rpx;
  height: 140rpx;
  position: absolute;
  top: -20rpx;
}

.net-icon {
  width: 150rpx;
  height: 150rpx;
  left: 50%;
  transform: translateX(-50%);
}

.bottle-icon {
  width: 130rpx;
  height: 130rpx;
  left: 50%;
  transform: translateX(-50%);
}

/* 交互动画 */
@keyframes throwNet {
  0% { transform: translateX(-50%) scale(1) rotate(0deg); opacity: 1; }
  30% { transform: translateX(-50%) translateY(-50vh) scale(2) rotate(-30deg); opacity: 1; }
  60% { transform: translateX(-50%) translateY(-30vh) scale(1.5) rotate(10deg); opacity: 0.8; }
  100% { transform: translateX(-50%) translateY(0) scale(1) rotate(0deg); opacity: 1; }
}

@keyframes throwBottle {
  0% { transform: translateX(-50%) scale(1) rotate(0deg); opacity: 1; }
  100% { transform: translateX(-50%) translateY(-60vh) scale(0) rotate(360deg); opacity: 0; }
}

.ghost-icon {
  position: absolute;
  top: -20rpx;
  z-index: 9999;
  pointer-events: none;
}

.animating-pick {
  animation: throwNet 1.5s ease-in-out forwards !important;
}

.animating-throw {
  animation: throwBottle 1.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards !important;
}

.btn-text {
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.limit-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

/* 弹窗通用样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  z-index: 100;
  animation: fadeIn 0.3s ease;
}

.modal-content {
  position: fixed;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 620rpx;
  background-color: #FFFFFF;
  border-radius: 40rpx;
  padding: 48rpx 40rpx;
  z-index: 101;
  box-sizing: border-box;
  box-shadow: 0 24rpx 48rpx rgba(0, 0, 0, 0.15);
  animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes popIn {
  from { opacity: 0; transform: translate(-50%, -40%) scale(0.9); }
  to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
}

.modal-header {
  text-align: center;
  font-size: 36rpx;
  font-weight: 600;
  margin-bottom: 32rpx;
  color: #1A1A24;
}

.modal-btns {
  display: flex;
  justify-content: space-between;
  margin-top: 48rpx;
  gap: 24rpx;
}

.modal-btns button {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 100rpx;
  margin: 0;
}

.modal-btns .cancel {
  background-color: #F7F8FA;
  color: #4A4A5A;
}

.modal-btns .cancel::after {
  border: none;
}

.modal-btns .confirm {
  background-color: #6A61F8;
  color: #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(106, 97, 248, 0.25);
}

.modal-btns .confirm::after {
  border: none;
}

/* 扔瓶子专属 */
.bottle-input {
  width: 100%;
  height: 240rpx;
  background-color: #F7F8FA;
  border-radius: 24rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #1A1A24;
  box-sizing: border-box;
}

.word-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #8A8A99;
  margin-top: 12rpx;
}

/* 捞瓶子专属 */
.picked-info {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 0 10rpx;
}

.picked-info .avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: #EEEEEE;
  margin-right: 20rpx;
  border: 2rpx solid #F7F8FA;
}

.picked-info .nickname {
  font-size: 30rpx;
  color: #1A1A24;
  font-weight: 600;
}

.picked-info .user-id-small {
  font-size: 24rpx;
  color: #8A8A99;
  margin-left: 12rpx;
  font-weight: normal;
}

.picked-text {
  background-color: #F7F8FA;
  padding: 32rpx;
  border-radius: 24rpx;
  font-size: 30rpx;
  color: #4A4A5A;
  line-height: 1.6;
  min-height: 160rpx;
}
</style>
