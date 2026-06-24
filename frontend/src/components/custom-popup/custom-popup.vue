<template>
  <view class="custom-popup-wrapper" v-if="showModal || showActionSheet">
    <!-- 背景遮罩层 -->
    <view 
      class="popup-mask" 
      :class="{ 'mask-show': maskVisible }"
      @click="handleMaskClick"
    ></view>
    
    <!-- 1. 确认/提示弹窗 (居中卡片) -->
    <view 
      v-if="showModal" 
      class="modal-card" 
      :class="{ 'modal-show': cardVisible }"
    >
      <view class="modal-body">
        <!-- 动态场景化图标 -->
        <view class="modal-icon-wrap" :class="iconThemeClass" v-if="hasIcon">
          <text :class="modalIconClass" class="modal-icon"></text>
        </view>
        
        <view class="modal-title" v-if="modalTitle">{{ modalTitle }}</view>
        
        <scroll-view scroll-y class="modal-content-scroll">
          <text class="modal-content">{{ modalContent }}</text>
        </scroll-view>
      </view>
      
      <!-- 底部操作按钮 -->
      <view class="modal-footer" :class="{ 'single-btn': !modalShowCancel }">
        <button 
          v-if="modalShowCancel"
          class="modal-btn cancel-btn" 
          :style="{ color: modalCancelColor }"
          hover-class="modal-btn-hover"
          @click="onCancel"
        >
          {{ modalCancelText }}
        </button>
        <button 
          class="modal-btn confirm-btn" 
          :style="{ background: modalConfirmBg }"
          hover-class="confirm-btn-hover"
          @click="onConfirm"
        >
          {{ modalConfirmText }}
        </button>
      </view>
    </view>
    
    <!-- 2. 操作菜单 (底部滑动面板) -->
    <view 
      v-if="showActionSheet" 
      class="action-sheet-card" 
      :class="{ 'sheet-show': cardVisible }"
    >
      <view class="sheet-header" v-if="actionSheetTitle">
        <text class="sheet-title">{{ actionSheetTitle }}</text>
      </view>
      
      <scroll-view scroll-y class="sheet-list">
        <view 
          v-for="(item, index) in actionSheetItems" 
          :key="index"
          class="sheet-item"
          hover-class="sheet-item-hover"
          :hover-stay-time="100"
          @click="onActionSheetItemClick(index)"
        >
          <text class="sheet-item-text" :style="{ color: actionSheetItemColor }">{{ item }}</text>
        </view>
      </scroll-view>
      
      <view class="sheet-cancel" hover-class="sheet-item-hover" @click="onActionSheetCancel">
        <text class="sheet-cancel-text">取消</text>
      </view>
      
      <!-- 安全区底部占位 -->
      <view class="sheet-safe-area"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick } from 'vue'
import { popupState } from '../../utils/customPopup'

const showModal = ref(false)
const showActionSheet = ref(false)

const maskVisible = ref(false)
const cardVisible = ref(false)

// 映射全局 Modal 属性
const modalTitle = computed(() => popupState.modal.title)
const modalContent = computed(() => popupState.modal.content)
const modalShowCancel = computed(() => popupState.modal.showCancel)
const modalCancelText = computed(() => popupState.modal.cancelText)
const modalConfirmText = computed(() => popupState.modal.confirmText)
const modalConfirmBg = computed(() => popupState.modal.confirmColor || '#6B5EF7')
const modalCancelColor = computed(() => popupState.modal.cancelColor || '#8A8A99')

// 映射全局 Action Sheet 属性
const actionSheetTitle = computed(() => popupState.actionSheet.title)
const actionSheetItems = computed(() => popupState.actionSheet.itemList)
const actionSheetItemColor = computed(() => popupState.actionSheet.itemColor)

// 监听全局弹窗显示状态以触发过渡动画
watch(() => popupState.modal.show, (newVal) => {
  if (newVal) {
    showModal.value = true
    nextTick(() => {
      setTimeout(() => {
        maskVisible.value = true
        cardVisible.value = true
      }, 30)
    })
  } else {
    cardVisible.value = false
    maskVisible.value = false
    setTimeout(() => {
      showModal.value = false
    }, 240)
  }
})

watch(() => popupState.actionSheet.show, (newVal) => {
  if (newVal) {
    showActionSheet.value = true
    nextTick(() => {
      setTimeout(() => {
        maskVisible.value = true
        cardVisible.value = true
      }, 30)
    })
  } else {
    cardVisible.value = false
    maskVisible.value = false
    setTimeout(() => {
      showActionSheet.value = false
    }, 240)
  }
})

// Modal 事件响应
const onConfirm = () => {
  if (popupState.modal.resolve) {
    popupState.modal.resolve({ confirm: true, cancel: false })
  }
  popupState.modal.show = false
}

const onCancel = () => {
  if (popupState.modal.resolve) {
    popupState.modal.resolve({ confirm: false, cancel: true })
  }
  popupState.modal.show = false
}

// Action Sheet 事件响应
const onActionSheetItemClick = (index: number) => {
  if (popupState.actionSheet.resolve) {
    popupState.actionSheet.resolve({ tapIndex: index })
  }
  popupState.actionSheet.show = false
}

const onActionSheetCancel = () => {
  if (popupState.actionSheet.reject) {
    popupState.actionSheet.reject({ errMsg: 'showActionSheet:fail cancel' })
  }
  popupState.actionSheet.show = false
}

// 点击蒙层：若有取消逻辑，视为取消
const handleMaskClick = () => {
  if (showModal.value) {
    if (modalShowCancel.value) {
      onCancel()
    }
  } else if (showActionSheet.value) {
    onActionSheetCancel()
  }
}

// 判断当前弹窗内容是否需要显示图标
const hasIcon = computed(() => {
  const title = modalTitle.value || ''
  const content = modalContent.value || ''
  const text = title + content
  return text.includes('屏蔽') || 
         text.includes('举报') || 
         text.includes('解除') || 
         text.includes('充值') || 
         text.includes('支付') || 
         text.includes('解锁') || 
         text.includes('退出') || 
         text.includes('登录') || 
         text.includes('注销') || 
         text.includes('删除')
})

// 计算图标的分类主题类名
const iconThemeClass = computed(() => {
  const title = modalTitle.value || ''
  const content = modalContent.value || ''
  const text = title + content
  if (text.includes('屏蔽') || text.includes('举报') || text.includes('退出') || text.includes('注销') || text.includes('删除')) {
    return 'danger-theme'
  }
  if (text.includes('支付') || text.includes('解锁') || text.includes('充值')) {
    return 'warning-theme'
  }
  return 'info-theme'
})

// 计算具体的 RemixIcon 类名
const modalIconClass = computed(() => {
  const title = modalTitle.value || ''
  const content = modalContent.value || ''
  const text = title + content
  if (text.includes('屏蔽') || text.includes('解除')) {
    return 'ri-forbid-2-line'
  }
  if (text.includes('举报')) {
    return 'ri-alarm-warning-line'
  }
  if (text.includes('支付') || text.includes('解锁') || text.includes('充值')) {
    return 'ri-wallet-3-line'
  }
  if (text.includes('退出') || text.includes('注销') || text.includes('删除')) {
    return 'ri-error-warning-line'
  }
  if (text.includes('登录')) {
    return 'ri-user-shared-line'
  }
  return 'ri-information-line'
})
</script>

<style lang="scss" scoped>
.custom-popup-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999999;
}

/* 蒙层动画 */
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999999;
  background-color: rgba(18, 17, 28, 0);
  backdrop-filter: blur(0px);
  -webkit-backdrop-filter: blur(0px);
  transition: all 0.24s ease-out;
}

.popup-mask.mask-show {
  background-color: rgba(18, 17, 28, 0.6);
}

/* 1. Modal 居中弹窗样式 */
.modal-card {
  position: fixed;
  top: 50%;
  left: 50%;
  z-index: 1000000;
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 40rpx;
  padding: 48rpx 40rpx 40rpx;
  box-sizing: border-box;
  box-shadow: 0 24rpx 64rpx rgba(18, 17, 28, 0.12);
  
  /* 初始态：微缩、透明 */
  opacity: 0;
  transform: translate(-50%, -46%) scale(0.9);
  transition: all 0.24s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-card.modal-show {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

.modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

/* 图标大容器 */
.modal-icon-wrap {
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  
  .modal-icon {
    font-size: 52rpx;
  }
}

/* 图标主题色彩 */
.info-theme {
  background-color: rgba(107, 94, 247, 0.08);
  color: #6B5EF7;
}

.warning-theme {
  background-color: rgba(255, 176, 58, 0.08);
  color: #FFB03A;
}

.danger-theme {
  background-color: rgba(255, 87, 119, 0.08);
  color: #FF5777;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #2A2938;
  margin-bottom: 16rpx;
  text-align: center;
}

.modal-content-scroll {
  max-height: 360rpx;
  width: 100%;
}

.modal-content {
  font-size: 28rpx;
  color: #555460;
  line-height: 1.6;
  text-align: center;
  word-break: break-all;
  display: block;
}

/* 按钮操作区 */
.modal-footer {
  display: flex;
  gap: 20rpx;
  width: 100%;
}

.modal-footer.single-btn {
  .confirm-btn {
    flex: 1;
  }
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 100rpx;
  font-size: 30rpx;
  font-weight: 800;
  text-align: center;
  border: none;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.modal-btn::after {
  border: none;
}

.cancel-btn {
  background-color: #F5F6F8;
  transition: background-color 0.15s ease;
}

.modal-btn-hover {
  background-color: #EAEBF0 !important;
}

.confirm-btn {
  color: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(106, 97, 248, 0.25);
  transition: all 0.15s ease;
}

.confirm-btn-hover {
  opacity: 0.9;
  transform: scale(0.98);
}

/* 2. Action Sheet 底部滑出面板样式 */
.action-sheet-card {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000000;
  background-color: #ffffff;
  border-radius: 40rpx 40rpx 0 0;
  padding: 36rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  box-shadow: 0 -12rpx 40rpx rgba(18, 17, 28, 0.08);
  
  /* 初始态：在屏幕下方 */
  transform: translateY(100%);
  transition: transform 0.24s cubic-bezier(0.25, 1, 0.5, 1);
}

.action-sheet-card.sheet-show {
  transform: translateY(0);
}

.sheet-header {
  text-align: center;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #F2F3F7;
  margin-bottom: 16rpx;
}

.sheet-title {
  font-size: 26rpx;
  color: #9594A4;
  font-weight: 500;
}

.sheet-list {
  max-height: 540rpx;
  width: 100%;
}

.sheet-item {
  height: 98rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F6F8;
  border-radius: 28rpx;
  margin-bottom: 16rpx;
  transition: background-color 0.15s ease;
}

.sheet-item-hover {
  background-color: #EAEBF0 !important;
}

.sheet-item-text {
  font-size: 30rpx;
  font-weight: 800;
}

.sheet-cancel {
  height: 98rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F5F6F8;
  border-radius: 100rpx;
  margin-top: 24rpx;
  transition: background-color 0.15s ease;
}

.sheet-cancel-text {
  font-size: 30rpx;
  font-weight: 800;
  color: #9594A4;
}

.sheet-safe-area {
  height: env(safe-area-inset-bottom);
}
</style>
