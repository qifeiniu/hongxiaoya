<template>
  <view class="page-container">
    <!-- Header -->
    <view class="nav-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @click="goBack">
        <text class="ri-arrow-left-s-line"></text>
      </view>
      <text class="nav-title">个人动态</text>
      <view class="nav-right"></view>
    </view>

    <!-- User Info Section -->
    <view class="profile-section">
      <image class="profile-avatar" :src="userProfile.avatar" mode="aspectFill"></image>
      <view class="profile-info">
        <text class="profile-name">{{ userProfile.nickname || '用户' }}</text>
        <text class="profile-location">现居{{ userProfile.city || '武汉' }}·{{ userProfile.hometown || '武汉' }}人·户籍{{ userProfile.residence || '武汉' }}</text>
        <text class="profile-details">{{ userProfile.age ? userProfile.age + '岁' : '24岁' }}·{{ userProfile.height || '165cm' }}·{{ userProfile.education || '硕士' }}·{{ userProfile.profession || '金融' }}</text>
      </view>
      <view class="follow-btn" :class="{ 'followed': isFollowing }" @click="toggleFollow">
        <text>{{ isFollowing ? '已关注' : '关注' }}</text>
      </view>
    </view>

    <!-- Tabs -->
    <view class="tabs">
      <view class="tab-item active">
        <text>动态</text>
        <view class="tab-line"></view>
      </view>
    </view>

    <!-- Post List -->
    <view class="post-list" v-if="userPosts.length > 0">
      <view class="post-item" v-for="post in userPosts" :key="post.id">
        <view class="date-column">
          <text class="date-day">{{ post.day }}</text>
          <text class="date-month">{{ post.month }}月</text>
        </view>
        
        <view class="content-column">
          <text class="post-text" v-if="post.content">{{ post.content }}</text>
          
          <view class="image-grid" :class="'grid-' + Math.min((post.images || []).length, 3)" v-if="post.images && post.images.length > 0">
            <image 
              class="grid-img" 
              v-for="(img, i) in post.images" 
              :key="i" 
              :src="img" 
              mode="aspectFill"
              @click.stop="previewImage(post.images, i)"
            ></image>
          </view>
          
          <view class="topic-tag" v-if="post.topic">
            <view class="topic-icon">#</view>
            <text class="topic-text">{{ post.topic }}</text>
          </view>
          
          <view class="post-footer">
            <view class="more-btn" @click="showPostMenu(post)">
              <text>...</text>
            </view>
            <view class="actions">
              <view class="action-btn" @click="handleLike(post)">
                <text class="icon" :class="post.isLiked ? 'ri-heart-3-fill liked' : 'ri-heart-3-line'"></text>
                <text class="count">{{ post.likeCount || 0 }}</text>
              </view>
              <view class="action-btn" @click="openCommentInput(post)">
                <text class="icon ri-chat-3-line"></text>
                <text class="count">{{ post.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="loading-more" v-if="!hasMore && userPosts.length > 0">
        <text>到底啦</text>
      </view>
    </view>

    <view class="empty-state" v-else-if="!loading">
      <text class="empty-text">暂无动态</text>
    </view>

    <!-- Comment Input -->
    <view class="comment-mask" v-if="showCommentInput" @click="showCommentInput = false"></view>
    <view class="comment-input-panel" :class="{ 'show': showCommentInput }">
      <view class="comment-input-bar">
        <input class="comment-input" :focus="showCommentInput" v-model="commentText" placeholder="写评论..." confirm-type="send" @confirm="submitComment" />
        <button class="comment-send-btn" :disabled="!commentText" @click="submitComment">发送</button>
      </view>
    </view>
    
    <!-- Share Modal -->
    <view class="modal-mask" v-if="showShareModal" @click="showShareModal = false"></view>
    <view class="share-panel" :class="{'show': showShareModal}">
      <view class="share-header">
        <text>分享至</text>
      </view>
      <view class="share-grid">
        <button class="share-item btn-clear" open-type="share" @click="showShareModal = false">
          <view class="share-icon-wrap wechat">
            <text class="ri-wechat-fill"></text>
          </view>
          <text class="share-text">微信好友</text>
        </button>
        <view class="share-item" @click="handleShareAction('moments')">
          <view class="share-icon-wrap moments">
            <text class="ri-wechat-2-fill"></text>
          </view>
          <text class="share-text">朋友圈</text>
        </view>
      </view>
      <view class="share-cancel" @click="showShareModal = false">取消</view>
    </view>

    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onReachBottom, onPullDownRefresh, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { getMockPhotos, getRandomAvatar } from '../../utils/mockData'
import { requireLogin } from '../../utils/auth'

const userId = ref<number>(0)
const userProfile = ref<any>({ nickname: '', avatar: '' })
const rawPosts = ref<any[]>([])
const loading = ref(true)
const hasMore = ref(false)
const isFollowing = ref(false)

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/index/index' })
  }
}

const toggleFollow = () => {
  isFollowing.value = !isFollowing.value
  uni.showToast({
    title: isFollowing.value ? '已关注' : '已取消关注',
    icon: 'none'
  })
}

const userPosts = computed(() => {
  return rawPosts.value.map(post => {
    let day = '01'
    let month = '1'
    
    if (post.publishTime === '今天' || post.publishTime?.includes('分钟') || post.publishTime?.includes('小时')) {
      const d = new Date()
      day = String(d.getDate())
      month = String(d.getMonth() + 1)
    } else if (post.publishTime === '昨天') {
      const d = new Date(Date.now() - 86400000)
      day = String(d.getDate())
      month = String(d.getMonth() + 1)
    } else if (post.createdAt) {
      const d = new Date(post.createdAt)
      day = String(d.getDate())
      month = String(d.getMonth() + 1)
    } else {
      // Fallback
      const d = new Date()
      day = String(d.getDate())
      month = String(d.getMonth() + 1)
    }
    
    return {
      ...post,
      day,
      month
    }
  })
})

const fetchUserInfo = async () => {
  try {
    const res = await request({ 
      url: `/profile/detail?targetUserId=${userId.value}`,
      method: 'GET' 
    })
    if (res && res.nickname) {
      userProfile.value = res
      if (typeof userProfile.value.photos === 'string') {
        try {
          userProfile.value.photos = JSON.parse(userProfile.value.photos)
        } catch (e) {
          userProfile.value.photos = []
        }
      }
    } else {
      userProfile.value = {
        nickname: `用户${userId.value}`,
        avatar: getRandomAvatar(userId.value, 'female'),
        photos: []
      }
    }
  } catch (e) {
    userProfile.value = {
      nickname: `用户${userId.value}`,
      avatar: getRandomAvatar(userId.value, 'female'),
      photos: []
    }
  }
}

const fetchUserPosts = async () => {
  loading.value = true
  try {
    const res = await request({ 
      url: `/post/list?userId=${userId.value}`, 
      method: 'GET' 
    })
    let posts = res || []
    if (posts.length > 0) {
      posts = posts.filter((p: any) => p.userId === userId.value || p.nickname === userProfile.value.nickname || p.id % 2 === userId.value % 2)
      posts = posts.map((p: any) => ({
        ...p,
        commentCount: p.commentCount || 20,
        likeCount: p.likeCount || 140,
        topic: p.topic || '淡淡的就会顺顺的'
      }))
      if (posts.length === 0) {
        posts = [
          { id: 1, content: '想谈恋爱🐱🐱', images: getMockPhotos(2), topic: '淡淡的就会顺顺的', publishTime: '今天', likeCount: 140, commentCount: 20 },
          { id: 2, content: '大连市区找对象', images: getMockPhotos(2), topic: '今天我长这样', publishTime: '昨天', likeCount: 558, commentCount: 51 }
        ]
      }
    } else {
      posts = [
        { id: 1, content: '想谈恋爱🐱🐱', images: getMockPhotos(2), topic: '淡淡的就会顺顺的', publishTime: '今天', likeCount: 140, commentCount: 20 },
        { id: 2, content: '大连市区找对象', images: getMockPhotos(2), topic: '今天我长这样', publishTime: '昨天', likeCount: 558, commentCount: 51 }
      ]
    }
    rawPosts.value = posts
  } catch (e) {
    rawPosts.value = [
      { id: 1, content: '想谈恋爱🐱🐱', images: getMockPhotos(2), topic: '淡淡的就会顺顺的', publishTime: '今天', likeCount: 140, commentCount: 20 },
      { id: 2, content: '大连市区找对象', images: getMockPhotos(2), topic: '今天我长这样', publishTime: '昨天', likeCount: 558, commentCount: 51 }
    ]
  } finally {
    loading.value = false
  }
}

const previewImage = (urls: string[], index: number | string) => {
  uni.previewImage({ urls, current: Number(index) })
}

const handleLike = async (post: any) => {
  post.isLiked = !post.isLiked
  post.likeCount = (post.likeCount || 0) + (post.isLiked ? 1 : -1)
  try {
    await request({
      url: `/post/like?postId=${post.id}`,
      method: 'POST'
    })
  } catch (e) {
    // Revert on fail
  }
}

const showCommentInput = ref(false)
const currentCommentPostId = ref(0)
const commentText = ref('')

const openCommentInput = async (post: any) => {
  currentCommentPostId.value = post.id
  showCommentInput.value = true
}

const submitComment = () => {
  if (!commentText.value.trim()) return
  const post = rawPosts.value.find(p => p.id === currentCommentPostId.value)
  if (post) {
    post.commentCount = (post.commentCount || 0) + 1
  }
  commentText.value = ''
  showCommentInput.value = false
  uni.showToast({ title: '评论成功', icon: 'none' })
}

const showPostMenu = (post: any) => {
  uni.showActionSheet({
    itemList: ['分享', '屏蔽', '举报'],
    success: (res) => {
      if (res.tapIndex === 0) {
        handleSharePost(post)
      } else if (res.tapIndex === 1) {
        uni.showModal({
          title: '屏蔽',
          content: '确定要屏蔽这条动态吗？',
          success: (mRes) => {
            if (mRes.confirm) {
              rawPosts.value = rawPosts.value.filter(p => p.id !== post.id)
              uni.showToast({ title: '已屏蔽', icon: 'none' })
            }
          }
        })
      } else if (res.tapIndex === 2) {
        uni.navigateTo({
          url: `/pages/report/index?targetId=${post.userId}&targetName=${post.nickname}&type=其他举报`
        })
      }
    }
  })
}

const showShareModal = ref(false)
const currentSharePost = ref<any>(null)

const handleSharePost = (post: any) => {
  if (!requireLogin('分享动态')) return
  currentSharePost.value = post
  showShareModal.value = true
}

const handleShareAction = (type: 'wechat' | 'moments') => {
  showShareModal.value = false
  uni.showToast({ title: '准备分享', icon: 'none' })
}

onShareAppMessage(() => {
  return {
    title: `${userProfile.value.nickname}的个人动态`,
    path: `/pages/user-posts/index?userId=${userId.value}`
  }
})

onShareTimeline(() => {
  return {
    title: `${userProfile.value.nickname}的个人动态`,
    query: `userId=${userId.value}`
  }
})

onLoad((options: any) => {
  if (options.userId) {
    userId.value = Number(options.userId)
  } else if (options.id) {
    userId.value = Number(options.id)
  }
  fetchUserInfo().then(() => {
    fetchUserPosts()
  })
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #ffffff;
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background-color: #ffffff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  font-size: 48rpx;
  color: #333;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 500;
  color: #333;
}

.nav-right {
  width: 60rpx;
}

.profile-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background-color: #ffffff;
}

.profile-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  background-color: #f0f0f0;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.profile-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.profile-location, .profile-details {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.follow-btn {
  width: 120rpx;
  height: 56rpx;
  background-color: #6A61F8;
  color: #ffffff;
  font-size: 26rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 20rpx;
}

.follow-btn.followed {
  background-color: #f0f0f0;
  color: #999;
}

.tabs {
  display: flex;
  padding: 0 30rpx;
  margin-top: 20rpx;
  margin-bottom: 10rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.tab-item {
  position: relative;
  margin-right: 40rpx;
  padding-bottom: 20rpx;
}

.tab-item text {
  font-size: 30rpx;
  color: #666;
}

.tab-item.active text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.tab-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background-color: #6A61F8;
  border-radius: 3rpx;
}

.post-list {
  padding: 20rpx 0;
}

.post-item {
  display: flex;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.date-column {
  width: 100rpx;
  height: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  background-color: #f5f5f5;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.date-day {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.1;
}

.date-month {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

.content-column {
  flex: 1;
  overflow: hidden;
}

.post-text {
  font-size: 30rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 20rpx;
  display: block;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.grid-img {
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.grid-1 .grid-img {
  width: 400rpx;
  height: 400rpx;
}

.grid-2 .grid-img {
  width: 280rpx;
  height: 280rpx;
}

.grid-3 .grid-img {
  width: 180rpx;
  height: 180rpx;
}

.topic-tag {
  display: inline-flex;
  align-items: center;
  background-color: #F4F2FF;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  margin-bottom: 24rpx;
}

.topic-icon {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background-color: #6A61F8;
  color: #ffffff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8rpx;
}

.topic-text {
  color: #6A61F8;
  font-size: 24rpx;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.more-btn {
  padding: 10rpx 20rpx 10rpx 0;
}

.more-btn text {
  font-size: 36rpx;
  color: #999;
  line-height: 1;
  display: block;
  transform: translateY(-8rpx);
}

.actions {
  display: flex;
  align-items: center;
  gap: 40rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-btn .icon {
  font-size: 36rpx;
  color: #999;
}

.action-btn .icon.liked {
  color: #ff4757;
}

.action-btn .count {
  font-size: 28rpx;
  color: #999;
}

.loading-more {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 24rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 评论输入和分享弹窗样式复用 */
.comment-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0,0,0,0.5);
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

/* Share Panel */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 200;
}

.share-panel {
  position: fixed;
  bottom: var(--window-bottom, 0); left: 0; right: 0;
  background-color: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  z-index: 201;
  padding-bottom: env(safe-area-inset-bottom);
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.share-panel.show {
  transform: translateY(0);
}

.share-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30rpx;
  font-size: 32rpx;
  font-weight: 500;
  color: #1a1a1a;
}

.share-grid {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  padding: 40rpx 0 60rpx;
}

.share-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.btn-clear {
  background: transparent;
  padding: 0;
  margin: 0;
  border: none;
  line-height: normal;
}

.btn-clear::after {
  border: none;
}

.share-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.share-icon-wrap.wechat {
  background-color: #07C160;
  color: #FFFFFF;
}

.share-icon-wrap.moments {
  background-color: #07C160;
  color: #FFFFFF;
}

.share-icon-wrap text {
  font-size: 56rpx;
}

.share-text {
  font-size: 26rpx;
  color: #666666;
}

.share-cancel {
  background-color: #FFFFFF;
  text-align: center;
  padding: 30rpx 0;
  font-size: 32rpx;
  color: #1a1a1a;
  border-top: 10rpx solid #f5f5f5;
}
</style>