<template>
  <view class="post-publish-container">
    <view class="header-actions">
      <text class="cancel-btn" @click="handleCancel">取消</text>
      <button class="publish-btn" :disabled="!content && mediaList.length === 0" @click="handlePublish">发布</button>
    </view>

    <view class="input-area">
      <textarea 
        class="textarea" 
        v-model="content" 
        placeholder="分享此刻的心情、新鲜事..." 
        maxlength="500"
        :show-confirm-bar="false"
      ></textarea>
      <text class="word-count">{{ content.length }}/500</text>
    </view>

    <view class="image-uploader">
      <view class="image-list">
        <view class="image-item" v-for="(media, index) in mediaList" :key="index">
          <image v-if="media.type === 'image'" :src="media.url" mode="aspectFill" class="uploaded-img"></image>
          <video v-else-if="media.type === 'video'" :src="media.url" class="uploaded-video" object-fit="cover" :controls="false" :show-play-btn="false"></video>
          <view class="play-icon-overlay" v-if="media.type === 'video'">
            <text class="ri-play-circle-fill"></text>
          </view>
          <view class="delete-btn" @click="removeMedia(index)">
            <text class="ri-close-line"></text>
          </view>
        </view>
        <view class="upload-btn" @click="chooseMedia" v-if="mediaList.length < 9 && !hasVideo">
          <text class="ri-camera-3-line upload-icon"></text>
          <text class="upload-text">照片/视频</text>
        </view>
      </view>
    </view>

    <view class="options-list">
      <view class="option-item" @click="chooseTopic">
        <text class="ri-hashtag icon"></text>
        <text class="label">参与话题</text>
        <text class="value">{{ topic || '添加合适的话题' }}</text>
        <text class="ri-arrow-right-s-line arrow"></text>
      </view>
      <view class="option-item" @click="chooseLocation">
        <text class="ri-map-pin-line icon"></text>
        <text class="label">所在位置</text>
        <text class="value">{{ location || '添加位置' }}</text>
        <text class="ri-arrow-right-s-line arrow"></text>
      </view>
      <view class="option-item" @click="chooseVisibility">
        <text class="ri-lock-line icon"></text>
        <text class="label">谁可以看</text>
        <text class="value">{{ visibilityLabel }}</text>
        <text class="ri-arrow-right-s-line arrow"></text>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request'

const content = ref('')
const mediaList = ref<{url: string, type: 'image'|'video'}[]>([])
const topic = ref('')
const location = ref('')
const visibility = ref('public') // 'public', 'friends', 'private'
const postId = ref('')

const hasVideo = computed(() => mediaList.value.some(m => m.type === 'video'))

const visibilityLabel = computed(() => {
  const map: Record<string, string> = {
    'public': '公开',
    'friends': '好友',
    'private': '仅自己'
  }
  return map[visibility.value] || '公开'
})

onLoad((options: any) => {
  if (options.id) {
    postId.value = options.id
    uni.setNavigationBarTitle({ title: '编辑动态' })
    fetchPostDetail(options.id)
  }
})

const fetchPostDetail = async (id: string) => {
  uni.showLoading({ title: '加载中...' })
  try {
    const res: any = await request({ url: `/post/detail`, method: 'GET', data: { id } })
    if (res) {
      content.value = res.content || ''
      topic.value = res.topic || ''
      location.value = res.location || ''
      visibility.value = res.visibility || 'public'
      
      if (res.images && res.images.length > 0) {
        mediaList.value = res.images.map((url: string) => ({ url, type: 'image' as const }))
      } else if (res.videoUrl) {
        mediaList.value = [{ url: res.videoUrl, type: 'video' as const }]
      }
    }
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    // For mock if API fails
    content.value = '周末阳光很好，出去走了走 🌞'
    mediaList.value = [
      { url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop', type: 'image' as const }
    ]
  }
}

const handleCancel = () => {
  if (content.value || mediaList.value.length > 0) {
    uni.showModal({
      title: '提示',
      content: '确定要放弃编辑吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

const chooseMedia = () => {
  uni.showActionSheet({
    itemList: ['拍摄', '从手机相册选择照片', '从手机相册选择视频'],
    success: (res) => {
      if (res.tapIndex === 2) {
        uni.chooseVideo({
          maxDuration: 60,
          sourceType: ['album'],
          success: (videoRes) => {
            mediaList.value.push({ url: videoRes.tempFilePath, type: 'video' })
          }
        })
      } else {
        const sourceType = res.tapIndex === 0 ? ['camera'] : ['album']
        uni.chooseImage({
          count: 9 - mediaList.value.length,
          sourceType,
          success: (imgRes) => {
            const tempFilePaths = imgRes.tempFilePaths as string[];
            const newImgs = tempFilePaths.map((path: string) => ({ url: path, type: 'image' as const }))
            mediaList.value.push(...newImgs)
          }
        })
      }
    }
  })
}

const removeMedia = (index: number) => {
  mediaList.value.splice(index, 1)
}

const chooseTopic = () => {
  uni.showActionSheet({
    itemList: ['#2026我要脱单', '#周末去哪儿', '#日常分享'],
    success: (res) => {
      const topics = ['2026我要脱单', '周末去哪儿', '日常分享']
      topic.value = topics[res.tapIndex]
    }
  })
}

const chooseLocation = () => {
  uni.showToast({ title: '定位获取中...', icon: 'none' })
  setTimeout(() => {
    location.value = '北京市 朝阳区'
  }, 1000)
}

const chooseVisibility = () => {
  uni.showActionSheet({
    itemList: ['公开', '好友', '仅自己'],
    success: (res) => {
      const visibilities = ['public', 'friends', 'private']
      visibility.value = visibilities[res.tapIndex]
    }
  })
}

const handlePublish = async () => {
  uni.showLoading({ title: postId.value ? '保存中...' : '发布中...' })
  try {
    // 这里将上传的本地图片/视频路径直接转为JSON存储，真实环境应该先上传OSS
    const mediaUrls = mediaList.value.map(m => m.url)
    const mediaType = hasVideo.value ? 'video' : 'image'
    
    const url = postId.value ? '/post/update' : '/post/create'
    const data: any = {
      content: content.value,
      images: mediaType === 'image' ? JSON.stringify(mediaUrls) : '[]',
      videoUrl: mediaType === 'video' ? mediaUrls[0] : null, // 仅支持单视频
      topic: topic.value,
      location: location.value,
      visibility: visibility.value
    }
    
    if (postId.value) {
      data.id = postId.value
    }

    await request({
      url,
      method: 'POST',
      data
    })
    
    uni.hideLoading()
    uni.showToast({ title: postId.value ? '保存成功' : '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.post-publish-container {
  min-height: 100vh;
  background-color: $uni-bg-color;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 40rpx;
}

.cancel-btn {
  font-size: 32rpx;
  color: $uni-text-color-regular;
}

.publish-btn {
  margin: 0;
  padding: 0 40rpx;
  height: 60rpx;
  line-height: 60rpx;
  background-color: $uni-color-primary;
  color: $uni-bg-color;
  font-size: 28rpx;
  border-radius: $uni-border-radius-lg;
}

.publish-btn::after {
  border: none;
}

.publish-btn[disabled] {
  opacity: 0.5;
}

.input-area {
  padding: 30rpx 40rpx;
  position: relative;
}

.textarea {
  width: 100%;
  height: 240rpx;
  font-size: 32rpx;
  line-height: 1.5;
}

.word-count {
  position: absolute;
  bottom: 10rpx;
  right: 40rpx;
  font-size: 24rpx;
  color: $uni-text-color-grey;
}

.image-uploader {
  padding: 0 40rpx 40rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -10rpx;
}

.image-item, .upload-btn {
  width: calc(33.33% - 20rpx);
  height: 210rpx;
  margin: 10rpx;
  position: relative;
}

.uploaded-img, .uploaded-video {
  width: 100%;
  height: 100%;
  border-radius: $uni-border-radius-base;
}

.play-icon-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60rpx;
  height: 60rpx;
  background-color: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #FFF;
  font-size: 36rpx;
}

.delete-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0,0,0,0.5);
  border-radius: $uni-border-radius-base;
  display: flex;
  justify-content: center;
  align-items: center;
  color: $uni-bg-color;
  font-size: 24rpx;
}

.upload-btn {
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-base;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.upload-icon {
  font-size: 60rpx;
  color: $uni-text-color-placeholder;
  margin-bottom: 10rpx;
}

.upload-text {
  font-size: 24rpx;
  color: $uni-text-color-grey;
}

.options-list {
  border-top: 1px solid $uni-bg-color-page;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 30rpx 40rpx;
  border-bottom: 1px solid $uni-bg-color-page;
}

.icon {
  font-size: 40rpx;
  color: $uni-text-color;
  margin-right: 20rpx;
}

.label {
  font-size: 30rpx;
  color: $uni-text-color;
  flex: 1;
}

.value {
  font-size: 28rpx;
  color: $uni-text-color-grey;
  margin-right: 10rpx;
}

.arrow {
  font-size: 32rpx;
  color: $uni-text-color-placeholder;
}
</style>