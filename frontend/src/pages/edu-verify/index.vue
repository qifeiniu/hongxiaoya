<template>
  <view class="page-container">
    <view class="header-card">
      <text class="ri-graduation-cap-fill header-icon"></text>
      <text class="header-title">学历认证</text>
      <text class="header-desc">上传毕业证书照片，通过审核后获得学历认证标识</text>
    </view>

    <!-- 上传区域 -->
    <view class="upload-section">
      <text class="section-title">上传毕业证照片</text>
      <view class="upload-area" @click="chooseImage">
        <image v-if="certImage" class="preview-img" :src="certImage" mode="aspectFit"></image>
        <view v-else class="upload-placeholder">
          <text class="ri-camera-line upload-icon"></text>
          <text class="upload-text">点击上传毕业证照片</text>
          <text class="upload-tip">支持 JPG/PNG 格式，≤10MB</text>
        </view>
      </view>
    </view>

    <!-- 毕业证编号 -->
    <view class="form-section">
      <text class="section-title">毕业证编号</text>
      <input class="form-input" v-model="certNumber" placeholder="请输入毕业证编号" />
    </view>

    <!-- 学校名称 -->
    <view class="form-section">
      <text class="section-title">毕业学校</text>
      <input class="form-input" v-model="school" placeholder="请输入毕业学校名称" />
    </view>

    <!-- 学历等级 -->
    <view class="form-section">
      <text class="section-title">学历等级</text>
      <view class="level-options">
        <view 
          class="level-option" 
          v-for="level in levels" 
          :key="level" 
          :class="{ active: education === level }" 
          @click="education = level"
        >
          <text>{{ level }}</text>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-area">
      <button class="submit-btn" :disabled="!canSubmit" @click="handleSubmit">提交认证</button>
      <text class="submit-tip">提交后将由人工审核，1-3个工作日内完成</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const certImage = ref('')
const certNumber = ref('')
const school = ref('')
const education = ref('')
const levels = ['大专', '本科', '硕士', '博士']

const canSubmit = computed(() => certImage.value && certNumber.value && school.value && education.value)

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      certImage.value = res.tempFilePaths[0]
    }
  })
}

const handleSubmit = () => {
  if (!canSubmit.value) return
  uni.showLoading({ title: '提交中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '提交成功，等待审核', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
  }, 1000)
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: $uni-bg-color-page;
  padding: 30rpx;
}

.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  text-align: center;
  margin-bottom: 40rpx;
}

.header-icon {
  font-size: 80rpx;
  color: #fff;
  display: block;
  margin-bottom: 20rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 16rpx;
}

.header-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  display: block;
}

.upload-section, .form-section {
  background: $uni-bg-color;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $uni-text-color;
  display: block;
  margin-bottom: 20rpx;
}

.upload-area {
  border: 2rpx dashed $uni-border-color;
  border-radius: 16rpx;
  min-height: 300rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 300rpx;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $uni-text-color-placeholder;
}

.upload-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.upload-text {
  font-size: 28rpx;
  margin-bottom: 8rpx;
}

.upload-tip {
  font-size: 22rpx;
}

.form-input {
  background: $uni-bg-color-page;
  height: 80rpx;
  border-radius: 12rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.level-options {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.level-option {
  padding: 16rpx 40rpx;
  border-radius: 12rpx;
  background: $uni-bg-color-page;
  font-size: 26rpx;
  color: $uni-text-color-regular;
  transition: all 0.2s;
}

.level-option.active {
  background: $uni-color-primary-light;
  color: $uni-color-primary;
  font-weight: 600;
}

.submit-area {
  margin-top: 40rpx;
  text-align: center;
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
}

.submit-btn[disabled] {
  opacity: 0.5;
}

.submit-tip {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: $uni-text-color-placeholder;
}
</style>
