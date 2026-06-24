<template>
  <view class="page-container">
    <!-- 聊天式客服 -->
    <scroll-view scroll-y class="chat-area" :scroll-into-view="scrollToId">
      <view v-for="(msg, idx) in messages" :key="idx" :id="'m-' + idx" class="msg-row" :class="msg.isMe ? 'right' : 'left'">
        <image v-if="!msg.isMe" class="cs-avatar" src="/static/hongxiaoya-logo.png" mode="aspectFill"></image>
        <view class="msg-bubble" :class="msg.isMe ? 'me' : 'cs'">
          <text>{{ msg.content }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 快捷问题 -->
    <view class="quick-questions" v-if="showQuickQ">
      <text class="quick-title">常见问题：</text>
      <view class="quick-tags">
        <view class="quick-tag" v-for="q in quickQuestions" :key="q" @click="askQuestion(q)">{{ q }}</view>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-bar">
      <input class="msg-input" v-model="inputText" placeholder="输入你的问题..." confirm-type="send" @confirm="sendMessage" />
      <button class="send-btn" @click="sendMessage">发送</button>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'

const messages = ref([
  { content: '你好！我是红小鸭智能客服 🐥\n有什么可以帮到你的吗？', isMe: false }
])
const inputText = ref('')
const scrollToId = ref('m-0')
const showQuickQ = ref(true)

const quickQuestions = [
  '如何充值鸭蛋？',
  '如何退款？',
  '举报用户',
  '账号注销',
  'VIP特权说明'
]

const autoReplies: Record<string, string> = {
  '如何充值鸭蛋？': '进入"我的"→点击顶部"充值中心"，选择合适的档位完成支付即可。',
  '如何退款？': '鸭蛋一经使用不可退款。未使用的充值鸭蛋可在7天内联系人工客服申请退款。',
  '举报用户': '在对方主页底部点击"举报"按钮，选择举报原因并提交。我们会在24小时内处理。',
  '账号注销': '进入"我的"→"设置"→"账号注销"，提交注销申请后7天生效。注销后数据不可恢复。',
  'VIP特权说明': 'VIP用户享有：每日签到双倍鸭蛋、查看访客记录、已读回执、更低的微信号解锁费用(50鸭蛋)等特权。'
}

const askQuestion = (q: string) => {
  messages.value.push({ content: q, isMe: true })
  showQuickQ.value = false
  scrollBottom()
  setTimeout(() => {
    messages.value.push({ content: autoReplies[q] || '感谢您的提问，人工客服会尽快回复您。', isMe: false })
    scrollBottom()
  }, 800)
}

const sendMessage = () => {
  if (!inputText.value.trim()) return
  const text = inputText.value
  inputText.value = ''
  messages.value.push({ content: text, isMe: true })
  showQuickQ.value = false
  scrollBottom()
  
  // Auto reply
  setTimeout(() => {
    const matchedKey = Object.keys(autoReplies).find(k => text.includes(k.replace('？', '')))
    const reply = matchedKey ? autoReplies[matchedKey] : '感谢您的反馈！人工客服工作时间为 9:00-21:00，我们会尽快回复您。'
    messages.value.push({ content: reply, isMe: false })
    scrollBottom()
  }, 1000)
}

const scrollBottom = () => {
  nextTick(() => {
    scrollToId.value = 'm-' + (messages.value.length - 1)
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $uni-bg-color-page;
}

.chat-area {
  flex: 1;
  padding: 30rpx;
}

.msg-row {
  display: flex;
  margin-bottom: 30rpx;
}

.right { justify-content: flex-end; }

.cs-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 70%;
  padding: 24rpx 30rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.5;
  white-space: pre-wrap;
}

.msg-bubble.cs {
  background: $uni-bg-color;
  color: $uni-text-color;
  border-top-left-radius: 4rpx;
}

.msg-bubble.me {
  background: $uni-color-primary;
  color: #fff;
  border-top-right-radius: 4rpx;
}

.quick-questions {
  background: $uni-bg-color;
  padding: 24rpx 30rpx;
  border-top: 1px solid $uni-bg-color-page;
}

.quick-title {
  font-size: 24rpx;
  color: $uni-text-color-grey;
  display: block;
  margin-bottom: 16rpx;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.quick-tag {
  padding: 12rpx 24rpx;
  background: $uni-color-primary-light;
  color: $uni-color-primary;
  font-size: 24rpx;
  border-radius: 24rpx;
  font-weight: 500;
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: $uni-bg-color;
  border-top: 1px solid $uni-bg-color-page;
  padding-bottom: env(safe-area-inset-bottom);
}

.msg-input {
  flex: 1;
  background: $uni-bg-color-page;
  height: 72rpx;
  border-radius: $uni-border-radius-pill;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.send-btn {
  margin-left: 16rpx;
  height: 64rpx;
  line-height: 64rpx;
  background: $uni-color-primary;
  color: #fff;
  font-size: 26rpx;
  border-radius: $uni-border-radius-lg;
  padding: 0 30rpx;
  border: none;
}
</style>
