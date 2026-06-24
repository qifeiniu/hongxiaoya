<template>
  <view class="chat-container">
    <!-- 消息流 -->
    <scroll-view scroll-y class="msg-list" :scroll-into-view="scrollToId">
      <view v-for="(msg, index) in msgList" :key="index" :id="'msg-' + index" class="msg-wrapper">
        <!-- 系统消息单独一行居中显示 -->
        <view v-if="msg.type === 'system'" class="system-msg-row">
          <text class="system-msg">{{ msg.content }}</text>
        </view>
        
        <!-- 普通消息 -->
        <view v-else class="msg-row" :class="msg.isMe ? 'msg-right' : 'msg-left'">
          <image v-if="!msg.isMe" class="avatar" :src="targetAvatar" mode="aspectFill" @click="goTargetProfile"></image>
          <view class="msg-content" :class="msg.type === 'gift' ? 'gift-content' : ''" @longpress="handleLongPress(msg, index)">
            <text v-if="msg.type === 'text'" class="text-bubble">{{ msg.content }}</text>
            <view v-else-if="msg.type === 'gift'" class="gift-bubble">
              <text class="gift-icon">{{ msg.giftIcon }}</text>
              <text class="gift-text">送出了 {{ msg.giftName }}</text>
            </view>
            <image v-else-if="msg.type === 'image'" class="msg-image" :src="msg.content" mode="widthFix" @click="previewImage(msg.content)"></image>
            <view v-else-if="msg.type === 'voice'" class="voice-bubble" @click="playVoice(msg)">
              <text class="ri-volume-up-line voice-icon"></text>
              <text class="voice-duration">{{ msg.duration || 3 }}''</text>
            </view>
            
            <!-- 已读回执 -->
            <view v-if="msg.isMe && msg.type !== 'system'" class="read-status" :class="{ 'is-read': msg.isRead }">
              {{ msg.isRead ? '已读' : '未读' }}
            </view>
          </view>
          <image v-if="msg.isMe" class="avatar" :src="myAvatar" mode="aspectFill"></image>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作区 -->
    <view class="input-panel">
      <view class="input-wrap">
        <input class="input-box" v-model="inputText" placeholder="发消息..." confirm-type="send" @confirm="sendText" @focus="hidePanels" />
        <view class="send-btn-small" @click="sendText">发送</view>
      </view>
      <view class="action-btn ri-emotion-happy-line" @click="openEmoji"></view>
      <view class="action-btn" :class="showActionPanel ? 'ri-close-circle-line' : 'ri-add-circle-line'" @click="toggleActionPanel"></view>
    </view>

    <!-- 更多功能面板 -->
    <view class="action-panel" :class="{ 'show': showActionPanel }">
      <view class="action-grid">
        <view class="action-item" @click="openGift">
          <view class="icon-wrap"><text class="ri-gift-line"></text></view>
          <text class="action-text">礼物</text>
        </view>
        <view class="action-item" @click="handleChooseImage">
          <view class="icon-wrap"><text class="ri-image-line"></text></view>
          <text class="action-text">照片</text>
        </view>
        <view class="action-item" @click="handleCall">
          <view class="icon-wrap"><text class="ri-phone-line"></text></view>
          <text class="action-text">通话</text>
        </view>
      </view>
    </view>

    <!-- 表情面板 -->
    <view class="emoji-panel" :class="{ 'show': showEmojiPanel }">
      <view class="emoji-grid">
        <view class="emoji-item" v-for="emoji in emojis" :key="emoji" @click="insertEmoji(emoji)">
          <text>{{ emoji }}</text>
        </view>
      </view>
    </view>

    <!-- 礼物面板弹窗 -->
    <view class="gift-mask" v-if="showGiftPanel" @click="showGiftPanel = false"></view>
    <view class="gift-panel" :class="{ 'show': showGiftPanel }">
      <view class="panel-header">
        <text class="title">送礼物</text>
        <text class="balance">鸭蛋余额: {{ balance }}</text>
      </view>
      <view class="gift-list">
        <view class="gift-item" v-for="g in gifts" :key="g.id" @click="sendGift(g)">
          <text class="gift-emoji">{{ g.icon }}</text>
          <text class="gift-name">{{ g.name }}</text>
          <text class="gift-price">{{ g.price }}鸭蛋</text>
        </view>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { getRandomAvatar } from '../../utils/mockData'

const targetId = ref('')
const targetName = ref('')
const targetAvatar = ref(getRandomAvatar(Math.floor(Math.random() * 100), 'female'))
const myAvatar = ref(getRandomAvatar(100, 'male'))

const msgList = ref<any[]>([])
const inputText = ref('')
const scrollToId = ref('')
const showGiftPanel = ref(false)
const showEmojiPanel = ref(false)
const showActionPanel = ref(false)
const balance = ref(0)

const gifts = [
  { id: 1, name: '玫瑰花', icon: '🌹', price: 20 },
  { id: 2, name: '跑车', icon: '🏎️', price: 50 },
  { id: 3, name: '火箭', icon: '🚀', price: 100 }
]

const emojis = [
  '😊', '😂', '🥰', '😍', '😘', '😜', '😎', '🤗',
  '😢', '😭', '😱', '😳', '🥺', '😤', '😡', '🤔',
  '👍', '👎', '👏', '🙏', '💪', '🤝', '❤️', '💔',
  '🌹', '🎉', '🎂', '🍰', '☀️', '🌙', '⭐', '🔥',
  '✈️', '🏠', '💰', '📱', '🎵', '🎬', '📷', '🎯'
]

const isFirstFree = ref(false)
const chatSource = ref('private_chat')
const receivedFlowerName = ref('玫瑰花')
const receivedFlowerIcon = ref('🌹')

onLoad(async (options) => {
  if (options && options.id) {
    targetId.value = options.id
    targetName.value = options.name || '聊天'
    uni.setNavigationBarTitle({ title: targetName.value })
    targetAvatar.value = getRandomAvatar(Number(options.id), 'female')
  }
  
  if (options && options.source) {
    chatSource.value = options.source
  }
  if (options && options.flowerName) {
    receivedFlowerName.value = options.flowerName
  }
  if (options && options.flowerIcon) {
    receivedFlowerIcon.value = options.flowerIcon
  }
  
  await fetchHistory()

  if (options && (options.autoRose === '1' || options.type === 'rose')) {
    isFirstFree.value = true
    // 模拟插入一条赠送玫瑰花的消息
    msgList.value.push({
      id: Date.now(),
      type: 'gift',
      content: '玫瑰花',
      isMe: true,
      giftName: '玫瑰花',
      giftIcon: '🌹',
      isRead: false
    })
    scrollToBottom()
  }

  fetchBalance()
  initWebSocket()
})

let wsTask: any = null

const initWebSocket = () => {
  const token = uni.getStorageSync('token')
  if (!token) return

  wsTask = uni.connectSocket({
    url: `ws://localhost:8080/ws/chat?token=${token}`,
    success: () => {
      console.log('WebSocket connect success')
    }
  })

  uni.onSocketMessage((res) => {
    try {
      const data = JSON.parse(res.data)
      if (data.senderId == targetId.value) {
        msgList.value.push({
          id: data.id,
          type: data.msgType === 1 ? 'text' : data.msgType === 5 ? 'gift' : 'system',
          content: data.content,
          isMe: false,
          giftName: data.msgType === 5 ? data.content : '',
          giftIcon: data.msgType === 5 ? '🎁' : ''
        })
        scrollToBottom()
        // Send read receipt to backend
        request({ url: `/message/read?targetId=${targetId.value}`, method: 'POST' }).catch(()=>{})
      }
    } catch(e) {
      console.error('WS msg error', e)
    }
  })
}

const fetchHistory = async () => {
  if (chatSource.value === 'received_flower') {
    msgList.value = [{
      id: 'mock-flower-first',
      type: 'gift',
      content: receivedFlowerName.value,
      isMe: false,
      isRead: true,
      giftName: receivedFlowerName.value,
      giftIcon: receivedFlowerIcon.value,
      createdAt: new Date(Date.now() - 86400000).toISOString()
    }]
    scrollToBottom()
    return
  }

  try {
    const res: any = await request({ url: `/message/history?targetId=${targetId.value}`, method: 'GET' })
    const myId = Number(uni.getStorageSync('userId'))
    
    let history = res || []
    
    msgList.value = history.map((m: any) => ({
      id: m.id,
      type: m.msgType === 1 ? 'text' : m.msgType === 5 ? 'gift' : m.msgType === 6 ? 'system' : m.msgType === 3 ? 'image' : m.msgType === 4 ? 'voice' : 'text',
      content: m.content,
      isMe: m.senderId === myId,
      isRead: m.isRead === 1,
      giftName: m.msgType === 5 ? m.content : '',
      giftIcon: m.msgType === 5 ? (m.content === '玫瑰花' ? '🌹' : '🎁') : '',
      duration: m.duration,
      createdAt: m.createdAt
    }))
    
    // 强制插入首条记录：根据来源决定是谁送出的玫瑰花
    if (msgList.value.length === 0 || msgList.value[0].type !== 'gift' || msgList.value[0].giftName !== '玫瑰花') {
      const isMeSend = chatSource.value === 'private_chat'
      
      msgList.value.unshift({
        id: 'mock-rose-first',
        type: 'gift',
        content: '玫瑰花',
        isMe: isMeSend,
        isRead: true,
        giftName: '玫瑰花',
        giftIcon: '🌹',
        createdAt: new Date(Date.now() - 86400000).toISOString()
      })
    }
    
    // 如果只有送花记录，模拟回复
    if (msgList.value.length === 1) {
      if (chatSource.value === 'private_chat') {
        // 我先送花，对方自动回复
        msgList.value.push({
          id: 'mock-reply-first',
          type: 'text',
          content: '谢谢你的玫瑰花呀，很高兴认识你~ 😊',
          isMe: false,
          isRead: true,
          createdAt: new Date(Date.now() - 86000000).toISOString()
        })
      }
    }
    
    scrollToBottom()
  } catch (e) {
    console.error(e)
    // 接口失败时的默认兜底数据
    if (msgList.value.length === 0) {
      msgList.value = [{
        id: 'mock-rose-first',
        type: 'gift',
        content: '玫瑰花',
        isMe: true,
        isRead: true,
        giftName: '玫瑰花',
        giftIcon: '🌹',
        createdAt: new Date(Date.now() - 86400000).toISOString()
      }]
    }
    scrollToBottom()
  }
}

const fetchBalance = async () => {
  try {
    const res = await request({ url: '/wallet/info', method: 'GET' })
    balance.value = res ? res.balance : 100
  } catch (e) {
    balance.value = 100 // mock default
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollToId.value = 'msg-' + (msgList.value.length - 1)
  })
}

const goTargetProfile = () => {
  if (targetId.value) {
    uni.navigateTo({ url: `/pages/user-detail/index?id=${targetId.value}` })
  }
}

const insertEmoji = (emoji: string) => {
  inputText.value += emoji
}

const toggleActionPanel = () => {
  if (showActionPanel.value) {
    showActionPanel.value = false
  } else {
    hidePanels()
    showActionPanel.value = true
  }
}

const openEmoji = () => {
  showActionPanel.value = false
  showEmojiPanel.value = true
}

const openGift = () => {
  showActionPanel.value = false
  showGiftPanel.value = true
}

const hidePanels = () => {
  showActionPanel.value = false
  showEmojiPanel.value = false
  showGiftPanel.value = false
}

const handleChooseImage = () => {
  hidePanels()
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFile = res.tempFilePaths[0]
      // In production, upload to server first. For mock, use local path.
      msgList.value.push({
        type: 'image',
        content: tempFile,
        isMe: true
      })
      scrollToBottom()
      // Mock send to server
      request({
        url: `/message/send?receiverId=${targetId.value}&msgType=3&content=${encodeURIComponent(tempFile)}`,
        method: 'POST'
      }).catch(() => {})
    }
  })
}

const previewImage = (url: string) => {
  uni.previewImage({ urls: [url], current: url })
}

const playVoice = (msg: any) => {
  uni.showToast({ title: `播放语音 ${msg.duration || 3}秒`, icon: 'none' })
}

const sendText = async () => {
  if (!inputText.value.trim()) return
  
  const content = inputText.value
  inputText.value = ''
  hidePanels()
  
  // 发送后取消首条免费标记
  isFirstFree.value = false
  
  try {
    const res: any = await request({
      url: `/message/send?receiverId=${targetId.value}&msgType=1&content=${encodeURIComponent(content)}`,
      method: 'POST'
    })
    
    // 如果存在引导信息，则移除
    msgList.value = msgList.value.filter(msg => msg.type !== 'guide')
    
    msgList.value.push({ type: 'text', content: content, isMe: true, isRead: false })
    scrollToBottom()

    // 如果是从收到的花进入，发送消息后将此会话转移到私聊列表的模拟逻辑
    if (chatSource.value === 'received_flower') {
      chatSource.value = 'private_chat'
      
      // 触发上一页（消息列表）的事件，通知其“已升级为私聊”
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1] as any
      const eventChannel = currentPage.getOpenerEventChannel ? currentPage.getOpenerEventChannel() : null
      if (eventChannel && eventChannel.emit) {
        eventChannel.emit('onChatUpgraded')
      }
      
      uni.showToast({ title: '已开启私聊', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

const sendGift = (gift: any) => {
  uni.showModal({
    title: '确认送礼',
    content: `确认花费 ${gift.price} 鸭蛋赠送${gift.name}吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '赠送中...' })
          
          await request({
            url: `/message/send?receiverId=${targetId.value}&msgType=5&content=${encodeURIComponent(gift.name)}`,
            method: 'POST'
          })
          uni.hideLoading()
          
          fetchBalance()
          showGiftPanel.value = false
          
          msgList.value.push({
            type: 'gift',
            giftName: gift.name,
            giftIcon: gift.icon,
            isMe: true
          })
          scrollToBottom()
          
        } catch (e) {
          uni.hideLoading()
        }
      }
    }
  })
}

const handleCall = () => {
  hidePanels()
  uni.showActionSheet({
    itemList: ['视频通话 (60鸭蛋/2分钟)', '语音通话 (60鸭蛋/2分钟)'],
    success: (res) => {
      const callType = res.tapIndex === 0 ? '视频通话' : '语音通话'
      uni.showModal({
        title: callType,
        content: `发起${callType}需要 60鸭蛋/2分钟，先扣后聊（仅扣发起方鸭蛋），是否继续？`,
        success: async (modalRes) => {
          if (modalRes.confirm) {
            try {
              uni.showLoading({ title: '发起中...' })
              await request({
                url: `/message/video-call/start?targetUserId=${targetId.value}`,
                method: 'POST'
              })
              uni.hideLoading()
              fetchBalance()
              uni.showToast({ title: '等待对方接听...', icon: 'none' })
            } catch (e) {
              uni.hideLoading()
            }
          }
        }
      })
    }
  })
}

const handleLongPress = (msg: any, index: number) => {
  if (!msg.isMe || msg.type === 'system') return
  
  // Check 3-minute time limit for recall
  const msgTime = msg.createdAt ? new Date(msg.createdAt).getTime() : 0
  const now = Date.now()
  const threeMinutes = 3 * 60 * 1000

  if (msgTime && (now - msgTime) > threeMinutes) {
    uni.showToast({ title: '发送已超过3分钟，无法撤回', icon: 'none' })
    return
  }
  
  uni.showActionSheet({
    itemList: ['撤回消息'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        try {
          uni.showLoading({ title: '撤回中...' })
          await request({
            url: `/message/recall?msgId=${msg.id}`,
            method: 'POST'
          })
          uni.hideLoading()
          msgList.value[index] = { type: 'system', content: '你撤回了一条消息', isMe: true }
        } catch (e) {
          uni.hideLoading()
          uni.showToast({ title: '撤回失败', icon: 'none' })
        }
      }
    }
  })
}
onUnmounted(() => {
  if (wsTask) {
    uni.closeSocket()
  }
})
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $uni-bg-color-page;
}

.msg-list {
  flex: 1;
  padding: 40rpx 30rpx;
  padding-bottom: 180rpx; /* 为固定的底部输入框留出更多空间 */
  box-sizing: border-box;
}

.msg-row {
  display: flex;
  margin-bottom: 48rpx;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-left {
  flex-direction: row;
}

.msg-right {
  flex-direction: row;
  justify-content: flex-end;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: $uni-border-radius-circle;
  background-color: $uni-border-color;
  box-shadow: $uni-shadow-sm;
  border: 4rpx solid #ffffff;
}

.msg-left .avatar {
  margin-right: 24rpx;
}

.msg-right .avatar {
  margin-left: 24rpx;
}

.msg-content {
  max-width: 65%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.read-status {
  position: absolute;
  bottom: 0;
  left: -70rpx;
  font-size: 22rpx;
  color: $uni-text-color-placeholder;
}

.read-status.is-read {
  color: $uni-color-primary;
  font-weight: 500;
}

.text-bubble {
  display: inline-block;
  padding: 24rpx 36rpx;
  border-radius: $uni-border-radius-lg;
  font-size: 30rpx;
  line-height: 1.5;
  word-wrap: break-word;
  box-shadow: $uni-shadow-base;
}

.msg-left .text-bubble {
  background-color: $uni-bg-color;
  color: $uni-text-color;
  border-top-left-radius: 8rpx;
}

.msg-right .text-bubble {
  background: $uni-color-primary-gradient;
  color: $uni-text-color-inverse;
  border-top-right-radius: 8rpx;
  box-shadow: $uni-shadow-primary;
}

.system-msg-row {
  display: flex;
  justify-content: center;
  width: 100%;
}

.system-msg {
  align-self: center;
  font-size: 24rpx;
  color: $uni-text-color-grey;
  background-color: rgba(0, 0, 0, 0.04);
  padding: 8rpx 24rpx;
  border-radius: $uni-border-radius-pill;
  margin: 16rpx 0 32rpx;
}

.guide-msg {
  color: $uni-color-primary;
  background-color: rgba($uni-color-primary, 0.1);
}

.msg-image {
  max-width: 400rpx;
  border-radius: $uni-border-radius-lg;
  background-color: $uni-bg-color-page;
  box-shadow: $uni-shadow-base;
}

.msg-left .msg-image {
  border-top-left-radius: 8rpx;
}

.msg-right .msg-image {
  border-top-right-radius: 8rpx;
}

.voice-bubble {
  display: flex;
  align-items: center;
  padding: 24rpx 36rpx;
  border-radius: $uni-border-radius-lg;
  background-color: $uni-bg-color;
  min-width: 180rpx;
  box-shadow: $uni-shadow-base;
}

.msg-left .voice-bubble {
  border-top-left-radius: 8rpx;
}

.msg-right .voice-bubble {
  background: $uni-color-primary-gradient;
  box-shadow: $uni-shadow-primary;
  border-top-right-radius: 8rpx;
}

.voice-icon {
  font-size: 40rpx;
  color: $uni-text-color-regular;
  margin-right: 16rpx;
}

.msg-right .voice-icon {
  color: $uni-bg-color;
}

.voice-duration {
  font-size: 28rpx;
  color: $uni-text-color-regular;
  font-weight: 500;
}

.msg-right .voice-duration {
  color: $uni-bg-color;
}

.gift-bubble {
  background-color: $uni-color-accent-light;
  padding: 24rpx 48rpx;
  border-radius: $uni-border-radius-lg;
  display: flex;
  align-items: center;
  border: 2rpx solid rgba(255, 87, 119, 0.15);
  box-shadow: $uni-shadow-accent;
}

.msg-left .gift-bubble {
  border-top-left-radius: 8rpx;
}

.msg-right .gift-bubble {
  border-top-right-radius: 8rpx;
}

.gift-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.gift-text {
  color: $uni-color-accent;
  font-size: 30rpx;
  font-weight: bold;
}

.input-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.04);
  padding-bottom: calc(env(safe-area-inset-bottom) + 24rpx);
  z-index: 100;
}

.input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-pill;
  height: 80rpx;
}

.input-box {
  flex: 1;
  height: 100%;
  padding: 0 100rpx 0 36rpx;
  font-size: 30rpx;
  color: $uni-text-color;
  background: transparent;
}

.send-btn-small {
  position: absolute;
  right: 12rpx;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 24rpx;
  background: $uni-color-primary-gradient;
  color: $uni-text-color-inverse;
  font-size: 24rpx;
  font-weight: bold;
  border-radius: $uni-border-radius-pill;
  transition: all 0.2s;
}

.send-btn-small:active {
  transform: scale(0.95);
}

.action-btn {
  margin-left: 20rpx;
  font-size: 56rpx;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: $uni-text-color-regular;
  background-color: transparent;
  transition: all 0.2s;
}

.action-btn:active {
  transform: scale(0.9);
  background-color: transparent;
  color: $uni-color-primary;
}

.send-btn {
  margin-left: 20rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: $uni-color-primary-gradient;
  box-shadow: $uni-shadow-primary;
  color: $uni-text-color-inverse;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: $uni-border-radius-pill;
  padding: 0 40rpx;
  border: none;
  transition: all 0.2s;
}

.send-btn:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 12rpx rgba(106, 97, 248, 0.2);
}

/* Action Panel */
.action-panel {
  position: fixed;
  bottom: -600rpx;
  left: 0;
  right: 0;
  background-color: $uni-bg-color;
  z-index: 99;
  transition: all 0.35s cubic-bezier(0.25, 1, 0.5, 1);
  padding-bottom: env(safe-area-inset-bottom);
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  box-shadow: 0 -12rpx 40rpx rgba(0, 0, 0, 0.08);
}

.action-panel.show {
  bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 50rpx 40rpx;
}

.action-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

.icon-wrap {
  width: 100rpx;
  height: 100rpx;
  background-color: $uni-bg-color-page;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  color: $uni-text-color;
  margin-bottom: 16rpx;
  transition: all 0.2s;
}

.action-item:active .icon-wrap {
  background-color: $uni-border-color;
  transform: scale(0.9);
}

.action-text {
  font-size: 24rpx;
  color: $uni-text-color-regular;
}

/* Emoji Panel */
.emoji-panel {
  position: fixed;
  bottom: -600rpx;
  left: 0;
  right: 0;
  background-color: $uni-bg-color;
  z-index: 99;
  transition: all 0.35s cubic-bezier(0.25, 1, 0.5, 1);
  padding-bottom: env(safe-area-inset-bottom);
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  box-shadow: 0 -12rpx 40rpx rgba(0, 0, 0, 0.08);
}

.emoji-panel.show {
  bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 40rpx 20rpx;
  max-height: 480rpx;
  overflow-y: auto;
}

.emoji-item {
  width: 12.5%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  transition: transform 0.1s;
}

.emoji-item:active {
  transform: scale(1.2);
  background-color: transparent;
}

/* Gift Panel */
.gift-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: $uni-bg-color-mask;
  backdrop-filter: blur(4px);
  z-index: 100;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.gift-panel {
  position: fixed;
  bottom: -600rpx;
  left: 0;
  right: 0;
  background-color: $uni-bg-color;
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  z-index: 101;
  transition: all 0.35s cubic-bezier(0.25, 1, 0.5, 1);
  padding-bottom: calc(env(safe-area-inset-bottom) + 30rpx);
  box-shadow: 0 -12rpx 40rpx rgba(0, 0, 0, 0.08);
}

.gift-panel.show {
  bottom: var(--window-bottom, 0);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 50rpx;
  position: relative;
}

.panel-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 40rpx;
  right: 40rpx;
  height: 2rpx;
  background-color: $uni-bg-color-page;
}

.panel-header .title {
  font-size: 36rpx;
  font-weight: bold;
  color: $uni-text-color;
}

.panel-header .balance {
  font-size: 28rpx;
  color: $uni-color-warning;
  font-weight: 500;
  background-color: rgba(255, 176, 58, 0.1);
  padding: 8rpx 24rpx;
  border-radius: $uni-border-radius-pill;
}

.gift-list {
  display: flex;
  justify-content: space-around;
  padding: 50rpx 20rpx;
}

.gift-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: $uni-border-radius-lg;
  transition: all 0.2s;
}

.gift-item:active {
  background-color: $uni-bg-color-page;
  transform: scale(0.95);
}

.gift-emoji {
  font-size: 96rpx;
  margin-bottom: 20rpx;
  filter: drop-shadow(0 8rpx 16rpx rgba(0,0,0,0.1));
}

.gift-name {
  font-size: 28rpx;
  color: $uni-text-color;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.gift-price {
  font-size: 24rpx;
  color: $uni-color-warning;
  font-weight: bold;
}
</style>
