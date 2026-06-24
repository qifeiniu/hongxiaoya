<template>
  <view class="page-container">
    <!-- Header Area -->
    <view class="header-bg">
      <view class="header-content">
        <text class="header-title">帮助中心</text>
        <text class="header-subtitle">我们能为您提供什么帮助？</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll" :show-scrollbar="false">
      <view class="content-wrapper">
        <!-- 搜索框 -->
        <view class="search-box">
          <text class="ri-search-line search-icon"></text>
          <input class="search-input" type="text" placeholder="搜索问题..." v-model="searchQuery" />
        </view>

        <!-- 操作指引视频 -->
        <view class="section mt-40">
          <view class="section-header">
            <text class="section-title">操作指引视频</text>
            <text class="section-more">查看全部 <text class="ri-arrow-right-s-line"></text></text>
          </view>
          <scroll-view scroll-x class="video-scroll" :show-scrollbar="false">
            <view class="video-list">
              <view class="video-card" v-for="(video, index) in videos" :key="index" @click="playVideo(video)">
                <view class="video-cover">
                  <image :src="video.cover" mode="aspectFill" class="cover-img"></image>
                  <view class="play-btn">
                    <text class="ri-play-fill"></text>
                  </view>
                  <text class="duration">{{ video.duration }}</text>
                </view>
                <text class="video-title">{{ video.title }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 新手引导 -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">新手引导</text>
          </view>
          <view class="guide-grid">
            <view class="guide-card" v-for="(item, index) in quickStart" :key="index" @click="showGuideDetail(item)">
              <view class="guide-icon-wrapper" :class="'bg-' + (index % 4)">
                <text :class="item.icon" class="guide-icon"></text>
              </view>
              <text class="guide-name">{{ item.title }}</text>
            </view>
          </view>
        </view>

        <!-- 常见问题 -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">常见问题</text>
          </view>
          <view class="faq-list">
            <view class="faq-item" v-for="item in filteredFaqs" :key="item.q" @click="item.open = !item.open">
              <view class="faq-header">
                <text class="faq-q">{{ item.q }}</text>
                <text class="faq-arrow" :class="item.open ? 'ri-arrow-up-s-line open' : 'ri-arrow-down-s-line'"></text>
              </view>
              <view class="faq-answer" v-if="item.open">
                <text class="answer-text">{{ item.a }}</text>
              </view>
            </view>
          </view>
          
          <view v-if="filteredFaqs.length === 0" class="empty-state">
            <text class="empty-text">没有找到相关问题</text>
          </view>
        </view>

        <!-- 联系客服 -->
        <view class="contact-banner" @click="goService">
          <view class="contact-left">
            <view class="cs-icon-wrapper">
              <text class="ri-customer-service-2-fill cs-icon"></text>
            </view>
            <view class="cs-info">
              <text class="cs-title">没找到答案？</text>
              <text class="cs-desc">在线客服随时为您解答疑问</text>
            </view>
          </view>
          <view class="cs-right">
            <button class="cs-btn">去咨询</button>
          </view>
        </view>
        
        <view class="safe-area-bottom"></view>
      </view>
    </scroll-view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'

const searchQuery = ref('')

// 模拟视频数据
const videos = reactive([
  { title: '如何完善资料提升曝光率？', cover: '/static/activities/活动01.jpg', duration: '01:25', url: '' },
  { title: '手把手教你开通红娘', cover: '/static/activities/活动02.jpg', duration: '02:10', url: '' },
  { title: '相亲活动报名流程演示', cover: '/static/activities/活动01.jpg', duration: '01:45', url: '' }
])

// 新手指引
const quickStart = reactive([
  { title: '完善资料', icon: 'ri-user-settings-line', content: '进入"我的"页面，点击"编辑资料"，填写基本信息、上传照片，完善度越高越容易获得推荐。' },
  { title: '实名认证', icon: 'ri-profile-line', content: '进入"我的"→"实名认证"，上传身份证正反面照片，系统自动识别验证。' },
  { title: '充值鸭蛋', icon: 'ri-copper-coin-line', content: '进入"我的"→"我的钱包"→"充值"，选择合适的充值档位进行支付。' },
  { title: '发布动态', icon: 'ri-camera-lens-line', content: '在"发现"页面点击右上角发布按钮，可以分享你的生活日常。' }
])

// 常见问题
const faqs = reactive([
  { q: '送玫瑰花需要多少鸭蛋？', a: '送一朵玫瑰花需要20鸭蛋，对方会收到通知并开启对话。', open: false },
  { q: '如何解锁微信号？', a: 'VIP用户花费50鸭蛋、普通用户花费100鸭蛋即可解锁对方微信号。', open: false },
  { q: '视频通话怎么收费？', a: '视频/语音通话 60鸭蛋/2分钟，先扣后聊。', open: false },
  { q: '签到有什么奖励？', a: '普通用户每日签到获得10鸭蛋，VIP用户获得20鸭蛋。签到获得的鸭蛋有效期为1个月。', open: false },
  { q: '鸭蛋会过期吗？', a: '充值获得的鸭蛋不会过期，签到、活动赠送的鸭蛋30天有效。', open: false },
  { q: '如何成为VIP？', a: '进入"我的"→"办理会员"，选择合适的会员时长进行开通。', open: false },
  { q: '消息可以撤回吗？', a: '发送后3分钟内可以长按消息进行撤回。', open: false }
])

// 搜索过滤
const filteredFaqs = computed(() => {
  if (!searchQuery.value) return faqs
  const lowerQuery = searchQuery.value.toLowerCase()
  return faqs.filter(faq => 
    faq.q.toLowerCase().includes(lowerQuery) || 
    faq.a.toLowerCase().includes(lowerQuery)
  )
})

const playVideo = (video: any) => {
  uni.showToast({
    title: '视频播放功能开发中',
    icon: 'none'
  })
}

const showGuideDetail = (item: any) => {
  uni.showModal({
    title: item.title,
    content: item.content,
    showCancel: false,
    confirmText: '知道了',
    confirmColor: '#6A61F8'
  })
}

const goService = () => {
  uni.navigateTo({ url: '/pages/customer-service/index' })
}
</script>

<style lang="scss" scoped>
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F7F8FA;
}

.header-bg {
  background: linear-gradient(135deg, #6A61F8 0%, #8A81FA 100%);
  padding: 80rpx 40rpx 100rpx;
  border-radius: 0 0 40rpx 40rpx;
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 44rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.header-subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.main-scroll {
  flex: 1;
  margin-top: -60rpx;
  z-index: 2;
  position: relative;
}

.content-wrapper {
  padding: 0 30rpx;
}

/* 搜索框 */
.search-box {
  background-color: #FFFFFF;
  border-radius: 100rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(106, 97, 248, 0.08);
  margin-bottom: 40rpx;
}

.search-icon {
  font-size: 36rpx;
  color: #999;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.mt-40 {
  margin-top: 40rpx;
}

.section {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #999;
  display: flex;
  align-items: center;
}

/* 视频列表 */
.video-scroll {
  width: 100%;
}

.video-list {
  display: flex;
  padding-bottom: 10rpx;
}

.video-card {
  width: 280rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.video-cover {
  width: 280rpx;
  height: 160rpx;
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.cover-img {
  width: 100%;
  height: 100%;
  background-color: #eee;
}

.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60rpx;
  height: 60rpx;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
  
  text {
    color: #FFF;
    font-size: 32rpx;
    margin-left: 4rpx;
  }
}

.duration {
  position: absolute;
  right: 12rpx;
  bottom: 12rpx;
  background: rgba(0, 0, 0, 0.6);
  color: #FFF;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 100rpx;
}

.video-title {
  font-size: 26rpx;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* 新手引导 */
.guide-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.guide-card {
  width: 48%;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 30rpx 20rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-sizing: border-box;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.guide-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  
  &.bg-0 { background: rgba(106, 97, 248, 0.1); color: #6A61F8; }
  &.bg-1 { background: rgba(255, 107, 107, 0.1); color: #FF6B6B; }
  &.bg-2 { background: rgba(72, 219, 251, 0.1); color: #48DBFB; }
  &.bg-3 { background: rgba(29, 209, 161, 0.1); color: #1DD1A1; }
}

.guide-icon {
  font-size: 36rpx;
}

.guide-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

/* 常见问题 */
.faq-list {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.faq-item {
  border-bottom: 1rpx solid #F0F0F0;
  transition: all 0.3s;
  
  &:last-child {
    border-bottom: none;
  }
}

.faq-header {
  padding: 32rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.faq-q {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  flex: 1;
  line-height: 1.4;
}

.faq-arrow {
  font-size: 36rpx;
  color: #999;
  margin-left: 20rpx;
  transition: transform 0.3s;
  
  &.open {
    color: #6A61F8;
  }
}

.faq-answer {
  padding: 0 30rpx 32rpx;
  background: #FAFAFC;
}

.answer-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.empty-state {
  padding: 60rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 联系客服 */
.contact-banner {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 40rpx;
  margin-bottom: 60rpx;
  box-shadow: 0 8rpx 24rpx rgba(106, 97, 248, 0.08);
}

.contact-left {
  display: flex;
  align-items: center;
}

.cs-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #6A61F8 0%, #8A81FA 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.cs-icon {
  font-size: 40rpx;
  color: #FFF;
}

.cs-info {
  display: flex;
  flex-direction: column;
}

.cs-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 6rpx;
}

.cs-desc {
  font-size: 24rpx;
  color: #999;
}

.cs-btn {
  background: #F0EFFF;
  color: #6A61F8;
  font-size: 26rpx;
  font-weight: 500;
  padding: 0 32rpx;
  height: 60rpx;
  line-height: 60rpx;
  border-radius: 100rpx;
  margin: 0;
  
  &::after {
    border: none;
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
  padding-bottom: 40rpx;
}
</style>
