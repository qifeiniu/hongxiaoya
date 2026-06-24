<template>
  <view class="report-container">
    <view class="header">
      <text class="title">举报 {{ targetName }}</text>
    </view>

    <view class="form-section">
      <text class="section-title">举报类型</text>
      <view class="type-grid">
        <view 
          class="type-btn" 
          :class="{ active: reportType === type }"
          v-for="type in reportTypes" 
          :key="type"
          @click="reportType = type"
        >
          {{ type }}
        </view>
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">问题描述</text>
      <view class="textarea-wrap">
        <textarea
          class="desc-input"
          v-model="description"
          placeholder="请详细描述举报内容（500字以内），我们将为您严格保密。"
          maxlength="500"
          @input="handleInput"
        />
        <text class="word-count">{{ description.length }}/500</text>
      </view>
    </view>

    <view class="form-section">
      <text class="section-title">证据截图 <text class="sub-text">(最多9张)</text></text>
      <view class="image-grid">
        <view class="img-wrap" v-for="(img, index) in images" :key="index">
          <image :src="img" class="evidence-img" mode="aspectFill" @click="previewImage(index)"></image>
          <view class="del-btn" @click.stop="deleteImage(index)">
            <text class="ri-close-line"></text>
          </view>
        </view>
        <view class="upload-btn" @click="chooseImage" v-if="images.length < 9">
          <text class="ri-camera-3-line upload-icon"></text>
          <text class="upload-text">上传照片</text>
        </view>
      </view>
    </view>

    <view class="tips">
      <text>恶意举报将受到处罚，请如实提供证据。</text>
    </view>

    <view class="bottom-action">
      <button class="submit-btn" @click="submitReport">提交举报</button>
    </view>
    <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request'

const targetId = ref('')
const targetName = ref('')
const reportType = ref('')
const description = ref('')
const images = ref<string[]>([])
const reportTypes = ['资料虚假', '疑似诈骗', '涉黄', '其他举报']

onLoad((options) => {
  if (options && options.targetId) {
    targetId.value = options.targetId
    targetName.value = options.targetName || '该用户'
    reportType.value = options.type || '其他举报'
  }
})

const handleInput = (e: any) => {
  description.value = e.detail.value
}

const isValid = computed(() => {
  return description.value.trim().length > 0 || images.value.length > 0
})

const chooseImage = () => {
  const count = 9 - images.value.length
  if (count <= 0) return
  
  uni.chooseImage({
    count,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths]
    }
  })
}

const deleteImage = (index: number) => {
  images.value.splice(index, 1)
}

const previewImage = (index: number) => {
  uni.previewImage({
    urls: images.value,
    current: index
  })
}

const submitReport = async () => {
  if (!isValid.value) {
    uni.showToast({ title: '请填写问题描述或上传截图', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '提交中...' })
    
    // In a real app, you would upload images to OSS first, then submit the report.
    // For now, we simulate a successful API call.
    // await request({
    //   url: '/report/submit',
    //   method: 'POST',
    //   data: {
    //     targetId: targetId.value,
    //     type: reportType.value,
    //     description: description.value,
    //     images: images.value // Should be URLs
    //   }
    // })
    
    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({ title: '举报已提交', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }, 1000)
    
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.report-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  padding: 30rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

.header {
  margin-bottom: 40rpx;
  
  .title {
    font-size: 40rpx;
    font-weight: bold;
    color: $uni-text-color;
    display: block;
  }
}

.type-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 20rpx 0;
}

.type-btn {
  width: 22%;
  padding: 12rpx 0;
  text-align: center;
  background: #F4F5FF;
  color: $uni-text-color-regular;
  border-radius: 100rpx;
  font-size: 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  box-sizing: border-box;
}

.type-btn.active {
  background: $uni-color-primary;
  color: #FFFFFF;
  border-color: $uni-color-primary;
  font-weight: bold;
}

.form-section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: $uni-text-color;
    margin-bottom: 24rpx;
    display: block;
    
    .sub-text {
      font-size: 24rpx;
      color: $uni-text-color-placeholder;
      font-weight: normal;
      margin-left: 12rpx;
    }
  }
}

.textarea-wrap {
  position: relative;
  background: #F8F9FA;
  border-radius: 16rpx;
  padding: 24rpx;
  
  .desc-input {
    width: 100%;
    height: 240rpx;
    font-size: 28rpx;
    line-height: 1.5;
    color: $uni-text-color;
  }
  
  .word-count {
    position: absolute;
    right: 24rpx;
    bottom: 24rpx;
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
  }
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.img-wrap {
  width: calc((100vw - 120rpx - 40rpx) / 3);
  height: calc((100vw - 120rpx - 40rpx) / 3);
  border-radius: 16rpx;
  position: relative;
  
  .evidence-img {
    width: 100%;
    height: 100%;
    border-radius: 16rpx;
  }
  
  .del-btn {
    position: absolute;
    top: -12rpx;
    right: -12rpx;
    width: 40rpx;
    height: 40rpx;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      color: #FFF;
      font-size: 28rpx;
    }
  }
}

.upload-btn {
  width: calc((100vw - 120rpx - 40rpx) / 3);
  height: calc((100vw - 120rpx - 40rpx) / 3);
  background: #F8F9FA;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .upload-icon {
    font-size: 60rpx;
    color: #C0C4CC;
    margin-bottom: 12rpx;
  }
  
  .upload-text {
    font-size: 24rpx;
    color: #C0C4CC;
  }
}

.tips {
  padding: 0 10rpx;
  margin-bottom: 60rpx;
  
  text {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
  }
}

.bottom-action {
  margin-top: auto;
  
  .submit-btn {
    background: $uni-color-primary;
    color: #FFFFFF;
    border-radius: 100rpx;
    font-size: 32rpx;
    font-weight: bold;
    height: 90rpx;
    line-height: 90rpx;
  }
}
</style>