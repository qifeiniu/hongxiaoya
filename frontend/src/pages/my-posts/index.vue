<template>
  <view class="page-container">
    <!-- Custom Back Button -->
    <view class="back-btn" :style="{ top: (statusBarHeight + 10) + 'px' }" @click="goBack">
      <text class="ri-arrow-left-s-line"></text>
    </view>
    
    <!-- Custom Publish Button -->
    <view class="publish-top-btn" :style="{ top: (statusBarHeight + 10) + 'px' }" @click="goPublish">
      <text class="ri-camera-fill"></text>
    </view>

    <!-- Header like WeChat Moments -->
    <view class="header-section">
      <image class="cover-img" :src="coverImage" mode="aspectFill" @click="previewCover"></image>
      <view class="change-cover-btn" @click="changeCover">
        <text class="ri-image-edit-line"></text>
        <text class="btn-text">更换背景</text>
      </view>
      <view class="user-info">
        <text class="nickname">{{ userProfile.nickname }}</text>
        <image class="avatar" :src="userProfile.avatar" mode="aspectFill"></image>
      </view>
    </view>

    <!-- Post List -->
    <view class="post-list" v-if="userPosts.length > 0">
      <view class="post-item" v-for="post in userPosts" :key="post.id">
        <view class="post-date">
          <view v-if="post.isToday" class="today">今天</view>
          <view v-else class="date-group">
            <text class="day">{{ post.day }}</text>
            <text class="month">{{ post.month }}月</text>
          </view>
        </view>
        <view class="post-content-area">
          <view class="post-header-action">
            <text class="ri-more-2-fill menu-icon" @click="showMenu(post)"></text>
          </view>
          <text class="post-text" v-if="post.content">{{ post.content }}</text>
          <view class="post-images" :class="{'single-img': post.images && post.images.length === 1}" v-if="post.images && post.images.length > 0">
            <image 
              class="post-img" 
              v-for="(img, i) in post.images.slice(0, 9)" 
              :key="i" 
              :src="img" 
              mode="aspectFill"
              @click="previewImage(post.images, Number(i))"
            ></image>
          </view>
          
          <!-- 互动数据与评论区 -->
          <view class="post-interactions" v-if="post.likeCount || post.commentCount">
            <view class="interaction-stats">
              <text v-if="post.likeCount" class="stat-item"><text class="ri-heart-3-line"></text> {{ post.likeCount }}</text>
              <text v-if="post.commentCount" class="stat-item"><text class="ri-message-3-line"></text> {{ post.commentCount }}</text>
            </view>
          </view>
          <view class="inline-comments" v-if="post.comments && post.comments.length > 0">
            <view class="inline-comment-item" v-for="(c, i) in (post.isExpanded ? post.comments : post.comments.slice(0, 2))" :key="i">
              <text class="c-name">{{ c.nickname }}：</text>
              <text class="c-content">{{ c.content }}</text>
            </view>
            <view class="more-comments" v-if="!post.isExpanded && (post.commentCount || post.comments.length) > 2" @click="post.isExpanded = true">
              <text>查看全部评论</text>
            </view>
            <view class="more-comments" v-if="post.isExpanded && (post.commentCount || post.comments.length) > 2" @click="post.isExpanded = false">
              <text>收起评论</text>
            </view>
          </view>
          
          <!-- 互动操作区 -->
          <view class="post-actions">
            <view class="actions-right">
              <view class="action-item" @click="handleLike(post)" :class="{ active: post.isLiked }">
                <text class="icon" :class="post.isLiked ? 'ri-heart-3-fill liked' : 'ri-heart-3-line'"></text>
                <text class="count">{{ post.likeCount || '赞' }}</text>
              </view>
              <view class="action-item" @click="openCommentInput(post)">
                <text class="icon ri-message-3-line"></text>
                <text class="count">{{ post.commentCount || '评论' }}</text>
              </view>
            </view>
          </view>

        </view>
      </view>
      <view class="loading-more">
        <text>到底啦</text>
      </view>
    </view>

    <view class="empty-state" v-else>
      <text class="ri-quill-pen-line empty-icon"></text>
      <text class="empty-text">还没有发布动态</text>
      <button class="publish-btn" @click="goPublish">去发布</button>
    </view>
    
    <!-- 评论输入弹窗 -->
    <view class="comment-mask" v-if="showCommentInput" @click="showCommentInput = false"></view>
    <view class="comment-input-panel" :class="{ 'show': showCommentInput }">
      <view class="comment-input-bar">
        <input class="comment-input" :focus="showCommentInput" v-model="commentText" placeholder="写评论..." confirm-type="send" @confirm="submitComment" />
        <button class="comment-send-btn" :disabled="!commentText" @click="submitComment">发送</button>
      </view>
    </view>
    
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { getMockPhotos, getRandomAvatar } from '../../utils/mockData'

const myPosts = ref<any[]>([])
const userProfile = ref<any>({ nickname: '', avatar: '' })

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/profile/index' })
  }
}

const coverImage = computed(() => {
  if (userProfile.value.photos && userProfile.value.photos.length > 0) {
    return userProfile.value.photos[0]
  }
  return 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop'
})

const userPosts = computed(() => {
  return myPosts.value.map(post => {
    let isToday = false
    let day = ''
    let month = ''
    
    if (post.publishTime === '今天' || post.publishTime?.includes('分钟') || post.publishTime?.includes('小时') || post.publishTime?.includes('刚刚')) {
      isToday = true
    } else if (post.publishTime === '昨天') {
      const d = new Date(Date.now() - 86400000)
      day = String(d.getDate()).padStart(2, '0')
      month = String(d.getMonth() + 1)
    } else {
      const d = new Date(post.createdAt || Date.now())
      day = String(d.getDate()).padStart(2, '0')
      month = String(d.getMonth() + 1)
    }
    
    return {
      ...post,
      isToday,
      day,
      month
    }
  })
})

const fetchProfile = async () => {
  try {
    const res: any = await request({ url: '/profile/info', method: 'GET' })
    if (res) {
      userProfile.value = res
      if (typeof userProfile.value.photos === 'string') {
        try {
          userProfile.value.photos = JSON.parse(userProfile.value.photos)
        } catch (e) {
          userProfile.value.photos = []
        }
      }
    }
  } catch (e) {}
}

const fetchMyPosts = async () => {
  try {
    const res = await request({ url: '/post/list', method: 'GET' })
    myPosts.value = res || []
    if (myPosts.value.length === 0) {
      myPosts.value = [
        { id: 1, content: '周末阳光很好，出去走了走 🌞', images: getMockPhotos(2), publishTime: '2小时前', likeCount: 5, commentCount: 2, comments: [{ nickname: '小太阳', content: '拍得真好看！' }, { nickname: '阿喵', content: '一起呀~' }] },
        { id: 2, content: '新发现的一家咖啡店，氛围感拉满 ☕', images: getMockPhotos(1), publishTime: '昨天', likeCount: 12, commentCount: 4, comments: [{ nickname: '吃货', content: '求地址！' }, { nickname: '咖啡控', content: '看着不错。' }] }
      ]
    }
  } catch (e) {
    myPosts.value = [
      { id: 1, content: '周末阳光很好，出去走了走 🌞', images: getMockPhotos(2), publishTime: '2小时前', likeCount: 5, commentCount: 2, comments: [{ nickname: '小太阳', content: '拍得真好看！' }, { nickname: '阿喵', content: '一起呀~' }] }
    ]
  }
}

const showMenu = (post: any) => {
  uni.showActionSheet({
    itemList: ['编辑', '分享给微信好友', '分享到朋友圈', '删除', '隐藏'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: `/pages/post-publish/index?id=${post.id}` })
      } else if (res.tapIndex === 1) {
        uni.showToast({ title: '请点击右上角...发送给朋友', icon: 'none' })
      } else if (res.tapIndex === 2) {
        uni.showToast({ title: '请点击右上角...分享到朋友圈', icon: 'none' })
      } else if (res.tapIndex === 3) {
        uni.showModal({
          title: '确认删除',
          content: '删除后不可恢复，确定删除吗？',
          confirmColor: '#FF4D4F',
          success: async (modalRes) => {
            if (modalRes.confirm) {
              uni.showLoading({ title: '删除中...' })
              try {
                await request({ url: '/post/delete', method: 'POST', data: { id: post.id } })
                myPosts.value = myPosts.value.filter(p => p.id !== post.id)
                uni.hideLoading()
                uni.showToast({ title: '已删除', icon: 'success' })
              } catch (e) {
                uni.hideLoading()
                // For mock, still remove from list
                myPosts.value = myPosts.value.filter(p => p.id !== post.id)
                uni.showToast({ title: '已删除', icon: 'success' })
              }
            }
          }
        })
      } else if (res.tapIndex === 4) {
        myPosts.value = myPosts.value.filter(p => p.id !== post.id)
        uni.showToast({ title: '已隐藏', icon: 'none' })
      }
    }
  })
}

const goPublish = () => {
  uni.navigateTo({ url: '/pages/post-publish/index' })
}

const previewCover = () => {
  uni.previewImage({ urls: [coverImage.value], current: 0 })
}

const changeCover = () => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uni.showLoading({ title: '上传中...' })
      try {
        // Here you would typically upload the file to your server first
        // For now, we'll just simulate a successful upload and update the profile
        
        // Mocking the update process
        let photos: string[] = []
        if (userProfile.value.photos && Array.isArray(userProfile.value.photos)) {
          photos = [...userProfile.value.photos]
        }
        
        // Put the new image at the beginning so it becomes the cover
        photos.unshift(tempFilePath)
        
        // Optional: you might want to call an API to save this change permanently
        // await request({ url: '/profile/update', method: 'POST', data: { photos: JSON.stringify(photos) } })
        
        userProfile.value.photos = photos
        uni.hideLoading()
        uni.showToast({ title: '背景已更新', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}

const previewImage = (urls: string[], index: number) => {
  uni.previewImage({ urls, current: index })
}

const showCommentInput = ref(false)
const currentCommentPostId = ref(0)
const commentText = ref('')

const openCommentInput = async (post: any) => {
  currentCommentPostId.value = post.id
  showCommentInput.value = true
}

const handleLike = async (post: any) => {
  post.isLiked = !post.isLiked
  try {
    await request({
      url: `/post/like?postId=${post.id}`,
      method: 'POST'
    })
  } catch (e) {
    post.isLiked = !post.isLiked
    post.likeCount = (post.likeCount || 0) + (post.isLiked ? 1 : -1)
  }
}

const submitComment = () => {
  if (!commentText.value.trim()) return
  const post = myPosts.value.find(p => p.id === currentCommentPostId.value)
  if (post) {
    if (!post.comments) post.comments = []
    post.comments.unshift({
      id: Date.now(),
      nickname: '我',
      avatar: getRandomAvatar(999, 'female'),
      content: commentText.value,
      time: '刚刚'
    })
    post.commentCount = (post.commentCount || 0) + 1
  }
  commentText.value = ''
  showCommentInput.value = false
}

onShow(() => {
  fetchProfile()
  fetchMyPosts()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #ffffff;
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
}

.back-btn {
  position: fixed;
  left: 30rpx;
  z-index: 100;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  color: #ffffff;
}

.back-btn text {
  font-size: 44rpx;
}

.publish-top-btn {
  position: fixed;
  right: 30rpx;
  z-index: 100;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  color: #ffffff;
}

.publish-top-btn text {
  font-size: 36rpx;
}

.header-section {
  position: relative;
  width: 100%;
  height: 500rpx;
  margin-bottom: 80rpx;
}

.cover-img {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
}

.user-info {
  position: absolute;
  right: 30rpx;
  bottom: -40rpx;
  display: flex;
  align-items: flex-end;
}

.change-cover-btn {
  position: absolute;
  left: 30rpx;
  bottom: 30rpx;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.5);
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  color: #ffffff;
  z-index: 10;
}

.change-cover-btn text {
  font-size: 24rpx;
}

.change-cover-btn .btn-text {
  margin-left: 8rpx;
}

.nickname {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  margin-right: 30rpx;
  margin-bottom: 50rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 20rpx;
  border: 4rpx solid #ffffff;
  background: #ffffff;
}

.post-list {
  padding: 0 30rpx 40rpx;
  box-sizing: border-box;
  width: 100%;
}

.post-item {
  display: flex;
  margin-bottom: 60rpx;
}

.post-date {
  width: 140rpx;
  flex-shrink: 0;
}

.today {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
}

.date-group {
  display: flex;
  align-items: baseline;
}

.day {
  font-size: 44rpx;
  font-weight: bold;
  color: #333333;
  margin-right: 8rpx;
}

.month {
  font-size: 24rpx;
  color: #333333;
}

.post-content-area {
  flex: 1;
  min-width: 0;
  position: relative;
}

.post-header-action {
  position: absolute;
  right: -20rpx;
  top: -10rpx;
  padding: 10rpx;
  z-index: 10;
}

.menu-icon {
  font-size: 36rpx;
  color: #999999;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
  margin-bottom: 12rpx;
}

.post-img {
  width: 176rpx;
  height: 176rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.single-img .post-img {
  width: 360rpx;
  height: 360rpx;
  border-radius: 12rpx;
  max-width: 100%;
}

.post-text {
  font-size: 30rpx;
  color: #1a1a1a;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-all;
  padding-right: 60rpx; /* Increased padding to prevent text overlapping with menu icon */
  display: block;
}

.post-interactions {
  margin-top: 20rpx;
}

.interaction-stats {
  display: flex;
  gap: 30rpx;
}

.stat-item {
  font-size: 26rpx;
  color: #999;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.inline-comments {
  background: #f8f8f8;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  margin-top: 16rpx;
}

.inline-comment-item {
  margin-bottom: 8rpx;
  font-size: 26rpx;
  line-height: 1.5;
}

.inline-comment-item:last-child {
  margin-bottom: 0;
}

.inline-comment-item .c-name {
  color: #333333;
  font-weight: bold;
}

.inline-comment-item .c-content {
  color: #333;
}

.more-comments {
  margin-top: 8rpx;
}

.more-comments text {
  color: #6A61F8;
  font-size: 24rpx;
  font-weight: 500;
}

.loading-more {
  text-align: center;
  padding: 30rpx 0;
  color: #999999;
  font-size: 24rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon { font-size: 120rpx; color: #cccccc; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999999; display: block; margin-bottom: 40rpx; }

.publish-btn {
  width: 300rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #6A61F8 0%, #8A81F8 100%);
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  border: none;
}

/* 互动操作区 */
.post-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20rpx;
}

.actions-right {
  display: flex;
  align-items: center;
  gap: 40rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-item .icon {
  font-size: 40rpx;
  color: #333333;
}

.action-item .icon.liked {
  color: #ff4757;
}

.action-item .count {
  font-size: 26rpx;
  color: #666666;
}

/* 评论输入弹窗 */
.comment-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 200;
}

.comment-input-panel {
  position: fixed;
  bottom: -100vh;
  left: 0; right: 0;
  background-color: #ffffff;
  border-top-left-radius: 30rpx;
  border-top-right-radius: 30rpx;
  z-index: 201;
  transition: all 0.3s ease;
  padding-bottom: env(safe-area-inset-bottom);
}

.comment-input-panel.show {
  bottom: 0;
}

.comment-input-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  border-top: 1px solid #f5f5f5;
}

.comment-input {
  flex: 1;
  background-color: #f5f5f5;
  height: 72rpx;
  border-radius: 36rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.comment-send-btn {
  margin-left: 16rpx;
  height: 64rpx;
  line-height: 64rpx;
  background-color: #6A61F8;
  color: #ffffff;
  font-size: 26rpx;
  border-radius: 12rpx;
  padding: 0 30rpx;
}

.comment-send-btn[disabled] {
  background-color: #cccccc;
}
</style>