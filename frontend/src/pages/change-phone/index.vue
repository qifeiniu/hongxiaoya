<template>
  <view class="container">
    <view class="form-group">
      <text class="label">新手机号</text>
      <input class="input" type="number" v-model="phone" placeholder="请输入新手机号" maxlength="11" />
    </view>
    <view class="form-group">
      <text class="label">验证码</text>
      <view class="code-wrap">
        <input class="input code-input" type="number" v-model="code" placeholder="请输入验证码" maxlength="6" />
        <view class="get-code-btn" :class="{ disabled: countdown > 0 }" @click="getCode">
          {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
        </view>
      </view>
    </view>
    <view class="submit-btn" @click="handleSubmit">
      <text>确认更换</text>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
let timer: any = null

const getCode = () => {
  if (countdown.value > 0) return
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  uni.showLoading({ title: '发送中...' })
  // 模拟发送验证码
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '发送成功', icon: 'success' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  }, 1000)
}

const handleSubmit = () => {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  uni.showLoading({ title: '验证中...' })
  // 模拟提交
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '手机号更换成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1000)
}
</script>

<style lang="scss" scoped>
.container {
  padding: 40rpx 30rpx;
  background-color: #F4F5F9;
  min-height: 100vh;
}
.form-group {
  margin-bottom: 40rpx;
}
.label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}
.input {
  height: 88rpx;
  background-color: #fff;
  border-radius: 12rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}
.code-wrap {
  display: flex;
  align-items: center;
}
.code-input {
  flex: 1;
  margin-right: 20rpx;
}
.get-code-btn {
  width: 220rpx;
  height: 88rpx;
  background-color: #6A61F8;
  color: #fff;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26rpx;
}
.get-code-btn.disabled {
  background-color: #ccc;
}
.submit-btn {
  margin-top: 80rpx;
  height: 88rpx;
  background-color: #6A61F8;
  border-radius: 44rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}
.submit-btn text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
}
</style>