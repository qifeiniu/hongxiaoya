<template>
  <view class="activity-create-container">
    <view class="header-tips">
      <text class="ri-information-line icon"></text>
      <text class="text">仅限白银及以上红娘创建活动，平台将收取一定服务费</text>
    </view>

    <!-- 封面图上传 -->
    <view class="cover-upload-section">
      <view class="cover-card" @click="chooseCover">
        <image v-if="form.cover" class="cover-image" :src="form.cover" mode="aspectFill"></image>
        <view v-else class="cover-placeholder">
          <text class="ri-camera-lens-line icon"></text>
          <text class="tips">上传活动封面图</text>
          <text class="sub-tips">建议尺寸 16:9，格式为 JPG/PNG</text>
        </view>
      </view>
    </view>

    <view class="form-list">
      <view class="form-item">
        <text class="label">活动名称</text>
        <input class="input" v-model="form.name" placeholder="例如：周末同城单身派对" />
      </view>

      <view class="form-item">
        <text class="label">报名费用(元)</text>
        <input class="input" type="number" v-model="form.price" placeholder="最低10元" />
      </view>

      <view class="form-item">
        <text class="label">男女人数限制</text>
        <view class="quota-wrap">
          <text>男</text>
          <input class="quota-input" type="number" v-model="form.maleQuota" />
          <text style="margin-left: 20rpx;">女</text>
          <input class="quota-input" type="number" v-model="form.femaleQuota" />
        </view>
      </view>

      <picker mode="date" @change="bindDateChange">
        <view class="form-item">
          <text class="label">开始日期</text>
          <text class="value" :class="{ 'placeholder': !form.date }">{{ form.date || '请选择' }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </picker>

      <picker mode="time" @change="bindTimeChange">
        <view class="form-item">
          <text class="label">开始时间</text>
          <text class="value" :class="{ 'placeholder': !form.time }">{{ form.time || '请选择' }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </picker>



      <!-- 循环举办选项 -->
      <picker :range="recurringTypeOptions" @change="bindRecurringChange">
        <view class="form-item">
          <text class="label">举办频次</text>
          <text class="value">{{ recurringTypeOptions[form.recurringTypeIndex] }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </picker>

      <!-- 退款规则选项 -->
      <picker :range="refundOptions" @change="bindRefundChange">
        <view class="form-item">
          <text class="label">退款规则</text>
          <text class="value text-truncate">{{ refundOptions[form.refundIndex] }}</text>
          <text class="ri-arrow-right-s-line arrow"></text>
        </view>
      </picker>

      <view class="form-item bio-item">
        <text class="label">活动简介</text>
        <textarea class="textarea" v-model="form.desc" placeholder="请详细描述活动流程等..." maxlength="200"></textarea>
      </view>

      <view class="form-item bio-item">
        <text class="label">报名要求</text>
        <textarea class="textarea" v-model="form.requirements" placeholder="请填写报名要求（如：男士需本科以上、女士需90后等）" maxlength="200"></textarea>
      </view>
    </view>

    <view class="switch-list">
      <view class="switch-item">
        <text class="switch-label">是否需要人工审核</text>
        <switch :checked="form.needAudit" @change="(e: any) => form.needAudit = e.detail.value" color="#6A61F8" />
      </view>
    </view>

    <view class="footer">
      <button class="submit-btn" :disabled="!isFormValid" @click="handleSubmit">发布活动</button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { request } from '../../utils/request'

const durationOptions = ['30', '60', '90', '120']
const recurringTypeOptions = ['单次活动', '每日举办', '每周举办']
const refundOptions = [
  '活动能否正常举办有红娘老师决定，如果活动无法正常举办可申请退款/自动退款，活动一旦正常举办参不参加均不支持退款',
  '活动开始前24小时内可申请退款'
]

const form = ref({
  name: '',
  price: '',
  maleQuota: '50',
  femaleQuota: '50',
  date: '',
  time: '',
  durationIndex: 1,
  desc: '',
  requirements: '',
  needAudit: false,
  cover: '',
  recurringTypeIndex: 0,
  refundIndex: 0
})

const isFormValid = computed(() => {
  return form.value.name && 
    form.value.price && 
    form.value.date && 
    form.value.time && 
    form.value.cover
})

const chooseCover = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.value.cover = res.tempFilePaths[0]
    }
  })
}

const bindDateChange = (e: any) => {
  form.value.date = e.detail.value
}

const bindTimeChange = (e: any) => {
  form.value.time = e.detail.value
}

const bindDurationChange = (e: any) => {
  form.value.durationIndex = e.detail.value
}

const bindRecurringChange = (e: any) => {
  form.value.recurringTypeIndex = e.detail.value
}

const bindRefundChange = (e: any) => {
  form.value.refundIndex = e.detail.value
}

const handleSubmit = async () => {
  if (Number(form.value.price) < 10) {
    uni.showToast({ title: '报名费最低10元', icon: 'none' })
    return
  }

  const startTime = new Date(`${form.value.date} ${form.value.time}`).getTime()
  
  uni.showLoading({ title: '提交中...' })
  try {
    await request({
      url: '/activity/create',
      method: 'POST',
      data: {
        name: form.value.name,
        price: Number(form.value.price),
        maleQuota: Number(form.value.maleQuota),
        femaleQuota: Number(form.value.femaleQuota),
        startTime: startTime,
        duration: Number(durationOptions[form.value.durationIndex]),
        description: form.value.desc,
        requirements: form.value.requirements,
        cover: form.value.cover,
        recurringType: recurringTypeOptions[form.value.recurringTypeIndex],
        refundRule: refundOptions[form.value.refundIndex],
        needAudit: form.value.needAudit
      }
    })
    uni.hideLoading()
    uni.showToast({ title: '创建成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.activity-create-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.header-tips {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: $uni-color-primary-light;
  color: $uni-color-primary;
  font-size: 24rpx;
}

.header-tips .icon {
  margin-right: 10rpx;
}

.cover-upload-section {
  padding: 30rpx;
  background-color: $uni-bg-color;
  margin-bottom: 20rpx;
}

.cover-card {
  width: 100%;
  height: 320rpx;
  border-radius: $uni-border-radius-lg;
  border: 2rpx dashed $uni-border-color;
  overflow: hidden;
  background-color: $uni-bg-color-page;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $uni-text-color-grey;
}

.cover-placeholder .icon {
  font-size: 64rpx;
  color: $uni-color-primary;
  margin-bottom: 12rpx;
}

.cover-placeholder .tips {
  font-size: 28rpx;
  font-weight: bold;
  color: $uni-text-color;
  margin-bottom: 8rpx;
}

.cover-placeholder .sub-tips {
  font-size: 22rpx;
}

.form-list {
  background-color: $uni-bg-color;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid $uni-bg-color-page;
}

.bio-item {
  flex-direction: column;
  align-items: flex-start;
  border-bottom: none;
}

.label {
  width: 200rpx;
  font-size: 30rpx;
  color: $uni-text-color;
}

.input {
  flex: 1;
  text-align: right;
  font-size: 30rpx;
}

.quota-wrap {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 30rpx;
}

.quota-input {
  width: 80rpx;
  text-align: center;
  border-bottom: 1px solid $uni-border-color;
  margin-left: 10rpx;
}

.value {
  flex: 1;
  text-align: right;
  font-size: 30rpx;
  color: $uni-text-color;
}

.text-truncate {
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.placeholder {
  color: $uni-text-color-grey;
}

.arrow {
  color: $uni-text-color-placeholder;
  font-size: 32rpx;
  margin-left: 10rpx;
}

.textarea {
  width: 100%;
  height: 160rpx;
  background-color: #f9f9f9;
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: $uni-border-radius-base;
  font-size: 28rpx;
  box-sizing: border-box;
}

.switch-list {
  background-color: $uni-bg-color;
  padding: 0 30rpx;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
}

.switch-label {
  font-size: 30rpx;
  color: $uni-text-color;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 40rpx;
  background-color: $uni-bg-color;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: $uni-shadow-sm;
  z-index: 10;
}

.submit-btn {
  background-color: $uni-color-primary;
  color: $uni-bg-color;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 32rpx;
}

.submit-btn::after {
  border: none;
}

.submit-btn[disabled] {
  background: $uni-text-color-placeholder;
  color: $uni-text-color-inverse;
  opacity: 0.8;
}
</style>