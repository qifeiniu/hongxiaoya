<template>
  <view class="apply-container">
    <!-- Activity Info Card -->
    <view class="card info-card">
      <view class="act-brief">
        <view class="brief-cover-wrap">
          <image :src="actInfo?.coverImg || actInfo?.cover || '/static/logo.png'" mode="aspectFill" class="brief-cover"></image>
          <view class="status-badge" v-if="actInfo?.status !== undefined">
            {{ actInfo?.status === 0 ? '即将开始' : actInfo?.status === 1 ? '正在进行' : '已结束' }}
          </view>
        </view>
        <view class="brief-details">
          <text class="brief-name">{{ actInfo?.name || '活动加载中...' }}</text>
          <view class="meta-item">
            <text class="ri-time-line meta-icon"></text>
            <text class="meta-text">{{ formatTime(actInfo?.startTime) }}</text>
          </view>
          <view class="meta-item" v-if="actInfo?.duration">
            <text class="ri-timer-line meta-icon"></text>
            <text class="meta-text">时长：{{ actInfo?.duration }} 分钟</text>
          </view>
          <view class="meta-item">
            <text class="ri-map-pin-line meta-icon"></text>
            <text class="meta-text">{{ actInfo?.city || '未知地点' }}</text>
          </view>
        </view>
      </view>

      <!-- Tags -->
      <view class="tags-row" v-if="actInfo?.tags && actInfo?.tags.length > 0">
        <view class="tag-item" v-for="(tag, idx) in actInfo.tags" :key="idx">{{ tag }}</view>
      </view>

      <!-- Matchmaker Info -->
      <view class="matchmaker-info">
        <image class="mm-avatar" :src="actInfo?.matchmaker?.avatar || '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg'" mode="aspectFill"></image>
        <view class="mm-details">
          <text class="mm-name">主办红娘：{{ actInfo?.matchmaker?.name || '资深红娘' }}</text>
        </view>
      </view>

      <!-- Quota Info -->
      <view class="quota-info">
        <view class="quota-item">
          <text class="quota-label">男嘉宾</text>
          <text class="quota-val">{{ actInfo?.maleJoined || 0 }} / {{ actInfo?.maleQuota || 0 }}</text>
        </view>
        <view class="quota-divider"></view>
        <view class="quota-item">
          <text class="quota-label">女嘉宾</text>
          <text class="quota-val">{{ actInfo?.femaleJoined || 0 }} / {{ actInfo?.femaleQuota || 0 }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="description-box" v-if="actInfo?.description">
        <text class="box-title">活动简介</text>
        <text class="description-text">{{ actInfo?.description }}</text>
      </view>
    </view>

    <!-- Payment Section -->
    <view class="card payment-card">
      <view class="payment-header">
        <text class="payment-title">报名费用</text>
        <text class="payment-price">¥{{ actInfo?.price || '0' }}</text>
      </view>
      
      <view class="payment-methods">
        <view 
          class="method-item" 
          :class="{ active: payMethod === 'wechat' }"
          @click="payMethod = 'wechat'"
        >
          <view class="method-left">
            <text class="ri-wechat-pay-fill wechat-icon"></text>
            <text class="method-name">微信支付</text>
          </view>
          <view class="radio-circle">
            <view class="radio-inner" v-if="payMethod === 'wechat'"></view>
          </view>
        </view>
        
        <view 
          class="method-item" 
          :class="{ active: payMethod === 'alipay' }"
          @click="payMethod = 'alipay'"
        >
          <view class="method-left">
            <text class="ri-alipay-fill alipay-icon"></text>
            <text class="method-name">支付宝支付</text>
          </view>
          <view class="radio-circle">
            <view class="radio-inner" v-if="payMethod === 'alipay'"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- Requirements Section -->
    <view class="card requirements-card">
      <view class="requirements-box">
        <text class="box-title">报名条件与要求</text>
        <view class="condition-content">
          <text class="requirements-text" :class="{ 'expanded': isExpanded }">{{ actInfo?.conditions || actInfo?.requirements || '1. 必须拥有硕士及以上学历\n2. 需在上海有稳定工作\n3. 诚实守信，真诚脱单\n4. 名校硕博学历 | 互联网、金融、国央企精英 | 年收入40W+' }}</text>
          <view class="toggle-btn-wrap" :class="{ 'expanded': isExpanded }" @click.stop="toggleExpand" v-if="getConditionLength() > 40">
            <text class="toggle-btn">{{ isExpanded ? '收起' : '展开' }}</text>
            <text :class="isExpanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'" class="toggle-icon"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- Tips -->
    <view class="tips">
      <text class="ri-information-line"></text>
      <text>活动能否正常举办有红娘老师决定，如果活动无法正常举办可申请退款/自动退款，活动一旦正常举办参不参加均不支持退款。</text>
    </view>

    <!-- Bottom Action Bar -->
    <view class="action-bar-placeholder"></view>
    <view class="action-bar">
      <view class="total-price">
        <text class="label">合计：</text>
        <text class="amount">¥{{ actInfo?.price || '0' }}</text>
      </view>
      <button class="submit-btn" :loading="isSubmitting" @click="handlePay">立即支付报名</button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { request } from '../../utils/request'
import { onLoad } from '@dcloudio/uni-app'

const actId = ref<string | null>(null)
const actInfo = ref<any>(null)
const payMethod = ref<'wechat' | 'alipay'>('wechat')
const isSubmitting = ref(false)
const isExpanded = ref(false)

const getConditionLength = () => {
  const text = actInfo.value?.conditions || actInfo.value?.requirements || '1. 必须拥有硕士及以上学历\n2. 需在上海有稳定工作\n3. 诚实守信，真诚脱单\n4. 名校硕博学历 | 互联网、金融、国央企精英 | 年收入40W+'
  return text.length
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const formatTime = (t: number) => {
  if (!t) return '待定'
  const d = new Date(t)
  return `开始时间 ${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

onLoad((options: any) => {
  if (options.id) {
    actId.value = options.id
    fetchActInfo()
  } else {
    uni.showToast({ title: '参数错误', icon: 'none' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
})

const fetchActInfo = async () => {
  try {
    uni.showLoading({ title: '加载中' })
    // 如果有详情接口可以调用，目前用mock或通用接口处理
    // 假设从全局或用通用列表查
    const res = await request({ url: '/activity/list', method: 'GET' })
    if (res && res.length > 0) {
      actInfo.value = res.find((a: any) => String(a.id) === actId.value)
    }
    
    if (!actInfo.value) {
      // Mock data fallback
      actInfo.value = {
        id: actId.value,
        name: '周末剧本杀交友派对',
        status: 0,
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
        city: '上海',
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        duration: 120,
        price: 99,
        maleQuota: 20,
        femaleQuota: 20,
        maleJoined: 15,
        femaleJoined: 18,
        matchmaker: {
          name: '资深红娘·李姐',
          avatar: '/static/avatars/christina-wocintechchat-com-m-0Zx1bDv5BNY-unsplash.jpg'
        },
        description: '一场充满趣味和悬疑的剧本杀交友派对，让你在推理和合作中结识志同道合的优质单身青年。',
        conditions: '京户京房专场/体制内专场/硕博专场，有京房/户|名校| 硕博学历 | 互联网、金融、国央企、名企精英 | 年收入30W+'
      }
    }
    // 如果获取到的活动数据中没有 conditions，则尝试使用 requirements，或者使用统一默认文本
    if (!actInfo.value.conditions && !actInfo.value.requirements) {
      actInfo.value.conditions = '1. 必须拥有硕士及以上学历\n2. 需在上海有稳定工作\n3. 诚实守信，真诚脱单\n4. 名校硕博学历 | 互联网、金融、国央企精英 | 年收入40W+'
    }
  } catch (e) {
    console.error(e)
  } finally {
    uni.hideLoading()
  }
}

const handlePay = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  
  uni.showLoading({ title: '正在支付...' })
  
  // 模拟支付请求
  setTimeout(() => {
    uni.hideLoading()
    isSubmitting.value = false
    
    uni.showToast({
      title: '支付并报名成功',
      icon: 'success'
    })
    
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/activity-joined/index' })
    }, 1500)
  }, 1500)
}
</script>

<style lang="scss" scoped>
.apply-container {
  min-height: 100vh;
  background-color: $uni-bg-color-page;
  padding: 24rpx;
  box-sizing: border-box;
}

.card {
  background-color: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: $uni-shadow-base;
}

.info-card {
  .act-brief {
    display: flex;
    align-items: stretch;
    margin-bottom: 24rpx;
  }

  .brief-cover-wrap {
    position: relative;
    margin-right: 24rpx;
    display: flex;
    align-items: center;
  }

  .brief-cover {
    width: 200rpx;
    height: 150rpx;
    border-radius: $uni-border-radius-base;
    background-color: #f0f0f0;
  }

  .status-badge {
    position: absolute;
    top: 10rpx;
    left: 10rpx;
    font-size: 20rpx;
    color: #fff;
    background-color: rgba(0, 0, 0, 0.6);
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }

  .brief-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 8rpx;
    padding: 4rpx 0;
  }

  .brief-name {
    font-size: 30rpx;
    font-weight: 800;
    color: $uni-text-color;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    .meta-icon {
      color: $uni-text-color-grey;
      font-size: 26rpx;
    }
    
    .meta-text {
      font-size: 24rpx;
      color: $uni-text-color-regular;
    }
  }

  .tags-row {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-top: 12rpx;
    margin-bottom: 16rpx;
  }

  .tag-item {
    font-size: 22rpx;
    color: $uni-color-primary;
    background-color: rgba(106, 97, 248, 0.1);
    padding: 6rpx 16rpx;
    border-radius: $uni-border-radius-sm;
  }

  .matchmaker-info {
    display: flex;
    align-items: center;
    margin-top: 32rpx;
    padding-top: 32rpx;
    border-top: 2rpx solid $uni-bg-color-grey;
    
    .mm-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 16rpx;
      background-color: #f0f0f0;
    }
    
    .mm-details {
      display: flex;
      flex-direction: column;
      
      .mm-name {
        font-size: 28rpx;
        font-weight: 500;
        color: $uni-text-color;
      }
    }
  }

  .quota-info {
    display: flex;
    align-items: center;
    background-color: #F8F9FA;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-top: 32rpx;

    .quota-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      
      .quota-label {
        font-size: 24rpx;
        color: $uni-text-color-grey;
      }
      
      .quota-val {
        font-size: 32rpx;
        font-weight: bold;
        color: $uni-text-color;
      }
    }

    .quota-divider {
      width: 2rpx;
      height: 40rpx;
      background-color: $uni-text-color-disable;
      opacity: 0.3;
    }
  }

  .description-box {
    margin-top: 32rpx;
    padding-top: 32rpx;
    border-top: 2rpx solid $uni-bg-color-grey;
    
    .box-title {
      font-size: 28rpx;
      font-weight: bold;
      color: $uni-text-color;
      margin-bottom: 16rpx;
      display: block;
    }
    
    .description-text {
      font-size: 26rpx;
      color: $uni-text-color-regular;
      line-height: 1.6;
    }
  }
}

.requirements-card {
  .requirements-box {
    .box-title {
      font-size: 32rpx;
      font-weight: bold;
      color: $uni-text-color;
      margin-bottom: 20rpx;
      display: block;
    }
    
    .condition-content {
      position: relative;
      margin-top: 6rpx;
      line-height: 44rpx;
    }

    .requirements-text {
      font-size: 28rpx;
      color: $uni-text-color-regular;
      line-height: 44rpx;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
      word-break: break-all;
      transition: all 0.3s;
      
      &.expanded {
        -webkit-line-clamp: unset;
        display: block;
      }
    }

    .toggle-btn-wrap {
      position: absolute;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      padding-left: 60rpx;
      background: linear-gradient(90deg, rgba(255,255,255,0) 0%, #ffffff 30%, #ffffff 100%);
      
      &.expanded {
        position: relative;
        background: transparent;
        justify-content: flex-end;
        padding-left: 0;
        margin-top: 4rpx;
      }
    }

    .toggle-btn {
      font-size: 24rpx;
      color: $uni-color-primary;
    }

    .toggle-icon {
      font-size: 26rpx;
      color: $uni-color-primary;
      margin-left: 4rpx;
    }
  }
}

.payment-card {
  .payment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
    
    .payment-title {
      font-size: 30rpx;
      font-weight: bold;
      color: $uni-text-color;
    }
    
    .payment-price {
      font-size: 36rpx;
      font-weight: 800;
      color: $uni-color-primary;
    }
  }
  
  .payment-methods {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
  }
  
  .method-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx;
    border-radius: $uni-border-radius-base;
    background-color: $uni-bg-color-page;
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    
    &.active {
      border-color: rgba(106, 97, 248, 0.3);
      background-color: rgba(106, 97, 248, 0.05);
    }
    
    .method-left {
      display: flex;
      align-items: center;
      gap: 16rpx;
    }
    
    .wechat-icon {
      font-size: 48rpx;
      color: #09B83E;
    }
    
    .alipay-icon {
      font-size: 48rpx;
      color: #1677FF;
    }
    
    .method-name {
      font-size: 28rpx;
      font-weight: 500;
      color: $uni-text-color;
    }
    
    .radio-circle {
      width: 36rpx;
      height: 36rpx;
      border-radius: 50%;
      border: 2rpx solid $uni-text-color-disable;
      display: flex;
      align-items: center;
      justify-content: center;
      box-sizing: border-box;
    }
    
    &.active .radio-circle {
      border-color: $uni-color-primary;
    }
    
    .radio-inner {
      width: 20rpx;
      height: 20rpx;
      border-radius: 50%;
      background-color: $uni-color-primary;
    }
  }
}

.tips {
  display: flex;
  align-items: flex-start;
  padding: 0 12rpx;
  gap: 12rpx;
  margin-bottom: 40rpx;
  
  text {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    line-height: 1.5;
  }
  
  .ri-information-line {
    font-size: 28rpx;
    color: $uni-color-warning;
    margin-top: 2rpx;
  }
}

.action-bar-placeholder {
  height: 140rpx;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: $uni-bg-color;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  z-index: 100;
  
  .total-price {
    display: flex;
    align-items: baseline;
    
    .label {
      font-size: 28rpx;
      color: $uni-text-color;
    }
    
    .amount {
      font-size: 40rpx;
      font-weight: 800;
      color: $uni-color-primary;
    }
  }
  
  .submit-btn {
    margin: 0;
    width: 320rpx;
    height: 84rpx;
    line-height: 84rpx;
    border-radius: $uni-border-radius-pill;
    background: $uni-color-primary-gradient;
    color: #fff;
    font-size: 30rpx;
    font-weight: bold;
    box-shadow: $uni-shadow-primary;
    
    &::after {
      border: none;
    }
  }
}
</style>
