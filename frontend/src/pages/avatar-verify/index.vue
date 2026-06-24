<template>
  <view class="page-container">
    <view class="header-card">
      <text class="ri-shield-user-fill header-icon"></text>
      <text class="header-title">头像认证</text>
      <text class="header-desc">通过真人拍照比对，验证你的头像真实性</text>
    </view>

    <!-- 步骤说明 -->
    <view class="steps-card">
      <view class="step" v-for="(step, i) in steps" :key="i">
        <view class="step-num">{{ i + 1 }}</view>
        <view class="step-info">
          <text class="step-title">{{ step.title }}</text>
          <text class="step-desc">{{ step.desc }}</text>
        </view>
      </view>
    </view>

    <!-- 拍照区域 -->
    <view class="photo-section">
      <view class="photo-area" @click="takePhoto">
        <image v-if="photoUrl" class="photo-preview" :src="photoUrl" mode="aspectFill"></image>
        <view v-else class="photo-placeholder">
          <text class="ri-camera-3-line photo-icon"></text>
          <text class="photo-text">点击拍照</text>
        </view>
      </view>
      <text class="photo-tip">请确保光线充足，正脸面向镜头</text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-area">
      <button class="submit-btn" :disabled="!photoUrl" @click="handleSubmit">开始认证</button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'

const photoUrl = ref('')
const steps = [
  { title: '拍摄正面照片', desc: '请在光线充足的环境下拍摄正面照' },
  { title: '系统自动比对', desc: '将拍摄照片与您上传的头像进行比对' },
  { title: '获得认证标识', desc: '通过后将获得"头像已认证"标识' }
]

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    sizeType: ['compressed'],
    success: (res) => {
      photoUrl.value = res.tempFilePaths[0]
    }
  })
}

const handleSubmit = () => {
  if (!photoUrl.value) return
  uni.showLoading({ title: '认证中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showModal({
      title: '认证成功',
      content: '恭喜！您的头像认证已通过',
      showCancel: false,
      success: () => uni.navigateBack()
    })
  }, 2000)
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $uni-bg-color-page;
  padding: 40rpx;
}

.header-card {
  background: $uni-color-primary-gradient;
  border-radius: $uni-border-radius-lg;
  padding: 50rpx 40rpx;
  text-align: center;
  margin-bottom: 40rpx;
  box-shadow: $uni-shadow-primary;
}

.header-icon { font-size: 80rpx; color: #fff; display: block; margin-bottom: 20rpx; }
.header-title { font-size: 36rpx; font-weight: bold; color: #fff; display: block; margin-bottom: 16rpx; }
.header-desc { font-size: 24rpx; color: rgba(255,255,255,0.8); display: block; }

.steps-card {
  background: $uni-bg-color;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.step {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
}

.step + .step {
  border-top: 1px solid $uni-bg-color-page;
}

.step-num {
  width: 48rpx;
  height: 48rpx;
  background: $uni-color-primary-light;
  color: $uni-color-primary;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.step-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $uni-text-color;
  display: block;
}

.step-desc {
  font-size: 24rpx;
  color: $uni-text-color-grey;
  display: block;
  margin-top: 4rpx;
}

.photo-section {
  text-align: center;
  margin-bottom: 40rpx;
}

.photo-area {
  width: 400rpx;
  height: 400rpx;
  border-radius: 50%;
  margin: 0 auto 24rpx;
  overflow: hidden;
  border: 4rpx dashed $uni-color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $uni-bg-color;
  box-shadow: $uni-shadow-sm;
}

.photo-preview {
  width: 100%;
  height: 100%;
}

.photo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $uni-text-color-placeholder;
}

.photo-icon { font-size: 100rpx; margin-bottom: 16rpx; }
.photo-text { font-size: 28rpx; }

.photo-tip {
  font-size: 24rpx;
  color: $uni-text-color-placeholder;
}

.submit-area {
  padding: 0 30rpx;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: $uni-color-primary-gradient;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  border: none;
  box-shadow: $uni-shadow-primary;
}

.submit-btn[disabled] {
  opacity: 0.5;
}
</style>
