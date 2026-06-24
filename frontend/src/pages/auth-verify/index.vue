<template>
  <view class="verify-container">
    <view class="header">
      <text class="title">为了您的交友安全</text>
      <text class="sub-title">请完成实名认证与真人活体检测</text>
    </view>

    <view class="form-area">
      <view class="input-group">
        <text class="label">真实姓名</text>
        <input class="input" v-model="form.name" placeholder="请输入您的真实姓名" />
      </view>
      <view class="input-group">
        <text class="label">身份证号</text>
        <input class="input" v-model="form.idCard" type="idcard" placeholder="请输入您的18位身份证号" />
      </view>
    </view>

    <view class="upload-area">
      <text class="section-title">上传身份证照片</text>
      <view class="upload-row">
        <view class="upload-box" @click="chooseImage('front')">
          <image v-if="form.frontImageUrl" class="preview-img" :src="form.frontImageUrl" mode="aspectFill"></image>
          <view v-else class="upload-placeholder">
            <text class="ri-id-card-line upload-icon"></text>
            <text class="upload-text">点击上传人像面</text>
          </view>
        </view>
        <view class="upload-box" @click="chooseImage('back')">
          <image v-if="form.backImageUrl" class="preview-img" :src="form.backImageUrl" mode="aspectFill"></image>
          <view v-else class="upload-placeholder">
            <text class="ri-bank-card-line upload-icon"></text>
            <text class="upload-text">点击上传国徽面</text>
          </view>
        </view>
      </view>
    </view>

    <view class="tips">
      <text class="tip-text">· 您的身份信息将严格保密，仅用于平台安全审核。</text>
      <text class="tip-text">· 请确保身份证照片清晰、无反光、无遮挡。</text>
      <text class="tip-text">· 提交后将拉起摄像头进行人脸活体比对。</text>
    </view>

    <button class="submit-btn" :disabled="!isFormValid" @click="handleVerify">
      开始人脸活体认证
    </button>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { request } from '../../utils/request'

const form = ref({
  name: '',
  idCard: '',
  frontImageUrl: '',
  backImageUrl: ''
})

const isFormValid = computed(() => {
  return form.value.name.length >= 2 && 
         form.value.idCard.length === 18 &&
         form.value.frontImageUrl &&
         form.value.backImageUrl
})

const chooseImage = (type: 'front' | 'back') => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      if (type === 'front') {
        form.value.frontImageUrl = res.tempFilePaths[0]
      } else {
        form.value.backImageUrl = res.tempFilePaths[0]
      }
    }
  })
}

const handleVerify = async () => {
  // 从身份证号中提取出生年月日计算年龄
  if (form.value.idCard.length === 18) {
    const birthYear = parseInt(form.value.idCard.substring(6, 10))
    const birthMonth = parseInt(form.value.idCard.substring(10, 12))
    const birthDay = parseInt(form.value.idCard.substring(12, 14))
    
    const today = new Date()
    let age = today.getFullYear() - birthYear
    if (today.getMonth() + 1 < birthMonth || (today.getMonth() + 1 === birthMonth && today.getDate() < birthDay)) {
      age--
    }
    
    if (age < 18) {
      uni.showModal({
        title: '认证失败',
        content: '抱歉，根据平台规定，未满18周岁的用户禁止注册和使用本应用。',
        showCancel: false
      })
      return
    }
  }

  uni.showLoading({ title: '正在调起人脸识别...' })
  
  // 模拟活体检测过程获取图片
  setTimeout(async () => {
    try {
      await request({
        url: '/profile/realNameAuth',
        method: 'POST',
        data: {
          frontImageUrl: form.value.frontImageUrl,
          backImageUrl: form.value.backImageUrl,
          faceImageUrl: 'https://mock.url/face.jpg'
        }
      })
      uni.hideLoading()
      uni.showToast({ title: '认证成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } catch (e) {
      uni.hideLoading()
      // 错误由 request 拦截器处理
    }
  }, 1500)
}
</script>

<style lang="scss" scoped>
.verify-container {
  padding: 40rpx;
  background-color: $uni-bg-color;
  min-height: 100vh;
  box-sizing: border-box;
}

.header {
  margin-bottom: 60rpx;
  margin-top: 40rpx;
}

.title {
  font-size: 44rpx;
  font-weight: bold;
  color: $uni-text-color;
  display: block;
  margin-bottom: 16rpx;
}

.sub-title {
  font-size: 28rpx;
  color: $uni-text-color-grey;
}

.form-area {
  margin-bottom: 40rpx;
}

.input-group {
  margin-bottom: 40rpx;
  border-bottom: 1px solid $uni-border-color;
  padding-bottom: 20rpx;
}

.label {
  font-size: 28rpx;
  color: $uni-text-color;
  margin-bottom: 20rpx;
  display: block;
}

.input {
  font-size: 32rpx;
  height: 60rpx;
}

.upload-area {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 28rpx;
  color: $uni-text-color;
  margin-bottom: 20rpx;
  display: block;
}

.upload-row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.upload-box {
  flex: 1;
  height: 220rpx;
  background-color: $uni-bg-color-page;
  border-radius: 16rpx;
  border: 2rpx dashed $uni-border-color;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $uni-text-color-grey;
}

.upload-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
  color: $uni-color-primary;
}

.upload-text {
  font-size: 24rpx;
}

.tips {
  margin-bottom: 80rpx;
  background-color: $uni-bg-color-page;
  padding: 30rpx;
  border-radius: $uni-border-radius-base;
}

.tip-text {
  font-size: 24rpx;
  color: $uni-text-color-regular;
  line-height: 1.6;
  display: block;
}

.submit-btn {
  background: $uni-color-primary-gradient;
  color: #fff;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
}

.submit-btn[disabled] {
  opacity: 0.5;
}

.submit-btn::after {
  border: none;
}
</style>
