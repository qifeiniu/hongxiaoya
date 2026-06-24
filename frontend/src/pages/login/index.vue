<template>
  <view class="login-container">
    <view class="header">
      <text class="title">欢迎来到红小鸭</text>
      <text class="subtitle">靠谱单身⻘年脱单平台</text>
    </view>

    <view class="form">
      <view class="input-group">
        <text class="label">+86</text>
        <input class="input" type="number" v-model="phone" placeholder="请输入手机号" maxlength="11" />
      </view>

      <view class="input-group code-group">
        <input class="input" type="number" v-model="code" placeholder="请输入验证码" maxlength="6" />
        <button class="code-btn" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
        </button>
      </view>

      <view class="agreement">
        <radio :checked="isAgree" @click="isAgree = !isAgree" color="#6A61F8" style="transform:scale(0.7)" />
        <text class="text">我已阅读并同意</text>
        <text class="link" @click="navigateTo('/pages/document/index?type=user_agreement')">《用户协议》</text>
        <text class="text">和</text>
        <text class="link" @click="navigateTo('/pages/document/index?type=privacy_policy')">《隐私政策》</text>
      </view>

      <button class="submit-btn" @click="handleLogin">
        登录 / 注册
      </button>

      <view class="wechat-login" @click="handleWechatLogin">
        <text class="ri-wechat-fill wechat-icon"></text>
        <text class="wechat-text">微信快捷登录</text>
      </view>

      <view class="guest-login" @click="handleGuestBrowse">
        <text class="guest-text">游客模式</text>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { request } from '../../utils/request'

const phone = ref('')
const code = ref('123456') // 测试默认验证码
const isAgree = ref(false)
const countdown = ref(0)

const navigateTo = (url: string) => {
  uni.navigateTo({ url })
}

const sendCode = async () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '发送中...' })
    await request({
      url: `/auth/sms/send?phone=${phone.value}`,
      method: 'POST'
    })
    uni.hideLoading()
    uni.showToast({ title: '验证码已发送', icon: 'success' })

    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    uni.hideLoading()
  }
}

const handleWechatLogin = () => {
  if (!isAgree.value) {
    uni.showToast({ title: '请先勾选同意协议', icon: 'none' })
    return
  }
  uni.showLoading({ title: '拉起微信授权...' })
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      try {
        const res = await request({
          url: '/auth/login/wechat',
          method: 'POST',
          data: { code: loginRes.code }
        })
        uni.hideLoading()
        
        // 微信登录成功后，强制绑定手机号
        if (!res.phone) {
          uni.showModal({
            title: '绑定手机号',
            content: '根据国家相关法律法规要求，请先绑定手机号。',
            showCancel: false,
            confirmText: '去绑定',
            success: () => {
              // 实际开发中应该跳转到专门的绑定手机号页面，这里为了演示简化，直接提示
              uni.showToast({ title: '请在上方输入手机号和验证码', icon: 'none' })
            }
          })
          return;
        }

        uni.setStorageSync('token', res.token)
        uni.setStorageSync('userId', res.userId)
        uni.setStorageSync('isProfileComplete', res.isProfileComplete)
        uni.showToast({ title: res.isNewUser ? '注册成功，送100鸭蛋' : '微信登录成功', icon: 'none' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' })
        }, 1500)
      } catch (e) {
        uni.hideLoading()
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '微信登录失败', icon: 'none' })
    }
  })
}

const handleLogin = async () => {
  if (!isAgree.value) {
    uni.showToast({ title: '请先勾选同意协议', icon: 'none' })
    return
  }
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '登录中...' })
    const res = await request({
      url: '/auth/login',
      method: 'POST',
      data: {
        phone: phone.value,
        code: code.value
      }
    })
    
    uni.hideLoading()
    // 保存 token
    uni.setStorageSync('token', res.token)
    uni.setStorageSync('userId', res.userId)
    uni.setStorageSync('isProfileComplete', res.isProfileComplete)
    
    uni.showToast({
      title: res.isNewUser ? '注册成功，送100鸭蛋' : '登录成功',
      icon: 'none'
    })
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
    
  } catch (error) {
    uni.hideLoading()
    // 错误在 request 拦截器中已经提示过了
  }
}
const handleGuestBrowse = () => {
  uni.setStorageSync('isGuest', true)
  uni.removeStorageSync('token')
  uni.removeStorageSync('userId')
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.login-container {
  padding: 60rpx 40rpx;
  background-color: $uni-bg-color;
  min-height: 100vh;
}

.header {
  margin-bottom: 80rpx;
  margin-top: 40rpx;
}

.title {
  font-size: 56rpx;
  font-weight: bold;
  color: $uni-text-color;
  display: block;
  margin-bottom: 20rpx;
}

.subtitle {
  font-size: 28rpx;
  color: $uni-text-color-grey;
}

.form {
  margin-top: 40rpx;
}

.input-group {
  display: flex;
  align-items: center;
  border-bottom: 1px solid $uni-border-color;
  padding: 30rpx 0;
  margin-bottom: 40rpx;
}

.label {
  font-size: 32rpx;
  color: $uni-text-color;
  margin-right: 30rpx;
  font-weight: bold;
}

.input {
  flex: 1;
  font-size: 32rpx;
}

.code-group {
  justify-content: space-between;
}

.code-btn {
  margin: 0;
  padding: 0 20rpx;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 26rpx;
  color: $uni-color-primary;
  background: transparent;
  border: 1px solid $uni-color-primary;
  border-radius: $uni-border-radius-lg;
}

.code-btn::after {
  border: none;
}

.code-btn[disabled] {
  color: $uni-text-color-grey;
  border-color: $uni-border-color;
}

.agreement {
  display: flex;
  align-items: center;
  margin-bottom: 60rpx;
  font-size: 24rpx;
}

.text {
  color: $uni-text-color-grey;
}

.link {
  color: #576B95;
}

.submit-btn {
  background-color: $uni-color-primary;
  color: $uni-bg-color;
  border-radius: $uni-border-radius-pill;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
}

.submit-btn::after {
  border: none;
}

.submit-btn[disabled] {
  background-color: $uni-color-primary-light;
  color: $uni-color-primary;
}

.wechat-login {
  margin-top: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.wechat-icon {
  font-size: 80rpx;
  color: #07C160;
  margin-bottom: 20rpx;
}

.wechat-text {
  font-size: 24rpx;
  color: $uni-text-color-grey;
}

.guest-login {
  margin-top: 40rpx;
  text-align: center;
}

.guest-text {
  font-size: 28rpx;
  color: $uni-text-color-grey;
  text-decoration: underline;
  letter-spacing: 1rpx;
}
</style>
