<template>
  <view class="room-container">
    <!-- 顶部导航和信息 -->
    <view class="room-header-overlay" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 第一行: 主播信息 & 观众人数 -->
      <view class="header-row-1">
        <!-- 1. 左上角小头像是主播会员头像，点击头像可以进入主播会员主页小窗口 -->
        <view class="host-pill" @click="openUserProfile(host)">
          <image class="host-avatar" :src="host.avatar" mode="aspectFill"></image>
          <view class="host-info">
            <text class="host-name">{{ host.name }}</text>
          </view>
        </view>
        <view class="right-actions">
          <!-- 2. 右上角是相亲会员列表 -->
          <view class="viewer-pill" @click="showViewerList = true">
            <view class="viewer-avatars">
              <image class="v-avatar" src="https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=50" mode="aspectFill"></image>
              <image class="v-avatar" src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50" mode="aspectFill"></image>
            </view>
            <text class="viewer-count">相亲列表</text>
          </view>
          <view class="close-btn" @click="handleBack">
            <text class="ri-close-line icon"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 核心区域: 视频布局 -->
    <view class="grid-container" :style="{ paddingTop: (statusBarHeight + 50) + 'px' }">
      <!-- 3. 大窗直播 -->
      <view class="main-live-window" @click="openUserProfile(currentLiveUser)">
        <image v-if="currentLiveUser.videoOn" class="main-video" :src="currentLiveUser.avatar" mode="aspectFill"></image>
        <view v-else class="main-avatar-placeholder">
          <image class="avatar-round" :src="currentLiveUser.avatar" mode="aspectFill"></image>
        </view>
        <view class="user-bottom-info">
          <text class="icon" :class="currentLiveUser.isMuted ? 'ri-mic-off-line' : 'ri-mic-line'"></text>
          <text class="name-text">{{ currentLiveUser.name }}</text>
        </view>
        <view class="live-tag">大窗直播</view>
      </view>

      <!-- 3. 下方四个排队位，及最后是红娘老师麦位 -->
      <view class="queue-row">
        <view class="queue-item" v-for="(u, idx) in queueAndHost" :key="idx" @click="u && !u.isEmpty && openUserProfile(u)">
          <template v-if="u && !u.isEmpty">
            <image v-if="u.videoOn" class="q-video" :src="u.avatar" mode="aspectFill"></image>
            <view v-else class="q-avatar-placeholder">
              <image class="q-avatar" :src="u.avatar" mode="aspectFill"></image>
            </view>
            <view class="q-info">
              <text class="icon" :class="u.isMuted ? 'ri-mic-off-line' : 'ri-mic-line'"></text>
              <text class="name">{{ u.name }}</text>
            </view>
            <view class="role-tag">{{ u.roleName }}</view>
          </template>
          <template v-else>
            <view class="queue-empty">
              <text class="ri-add-line icon"></text>
              <text class="txt">空位</text>
            </view>
          </template>
        </view>
      </view>
    </view>

    <!-- 底部半透明遮罩，为了聊天区和输入框背景 -->
    <view class="bottom-gradient-mask"></view>

    <!-- 公聊区 -->
    <view class="chat-area-wrap">
      <view class="sys-msg-banner" v-if="announcement">
        <text class="ri-volume-up-line" style="margin-right: 8rpx;"></text>
        <text class="announcement-text">{{ announcement }}</text>
      </view>
      
      <scroll-view scroll-y class="chat-area" :scroll-into-view="scrollToId" :scroll-with-animation="true">
        <view class="msg-item" v-for="(msg, index) in messages" :key="index" :id="'msg-'+index">
          <text class="nickname">{{ msg.name }}: </text>
          <text class="content">{{ msg.content }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="input-wrap">
        <input class="chat-input" v-model="inputText" placeholder="说点什么..." placeholder-style="color: rgba(255,255,255,0.6);" confirm-type="send" @confirm="sendMsg" />
        <text class="ri-emotion-line emoji-icon"></text>
      </view>

      <view class="action-btns">
        <view class="icon-btn media-btn" @click="toggleMyAudio">
          <text :class="myAudioOn ? 'ri-mic-line' : 'ri-mic-off-line'"></text>
        </view>
        <view class="icon-btn media-btn" @click="toggleMyVideo">
          <text :class="myVideoOn ? 'ri-camera-line' : 'ri-camera-off-line'"></text>
        </view>
        <view class="icon-btn hand-btn" @click="raiseHand">
          <view class="hand-text">
            <text>举手</text>
            <text>发言</text>
          </view>
        </view>
        <view class="icon-btn link-btn" v-if="role === 'host'" @click="showAdminPanel = true">
          <text class="ri-settings-3-line"></text>
        </view>
      </view>
    </view>
    
    <!-- 相亲会员列表弹窗 -->
    <view class="viewer-list-mask" v-if="showViewerList" @click="showViewerList = false"></view>
    <view class="viewer-list-panel" v-if="showViewerList">
      <view class="v-header">
        <text class="title">相亲会员列表</text>
        <text class="ri-close-line close-btn" @click="showViewerList = false"></text>
      </view>
      <scroll-view scroll-y class="v-list">
        <view class="v-item" v-for="user in viewerList" :key="user.id" @click="handleViewerClick(user)">
          <image :src="user.avatar" class="v-avatar" mode="aspectFill"></image>
          <view class="v-info">
            <view class="v-name-row">
              <text class="v-name">{{ user.name }}</text>
              <text class="v-id">ID: {{ user.displayId || ('1000' + user.id) }}</text>
            </view>
            <view class="v-tags">
              <text class="v-tag">{{ user.age || '25' }}岁</text>
              <text class="v-tag">{{ user.height || '168cm' }}</text>
              <text class="v-tag">{{ user.edu || '本科' }}</text>
              <text class="v-tag">{{ user.job || '设计师' }}</text>
            </view>
          </view>
          <view class="v-action">
            <text class="ri-user-search-line chat-icon"></text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 红娘管理面板 -->
    <view class="admin-mask" v-if="showAdminPanel" @click="showAdminPanel = false"></view>
    <view class="admin-panel" v-if="showAdminPanel">
      <view class="admin-header">
        <text class="title">房间控制台</text>
        <text class="ri-close-line close-btn" @click="showAdminPanel = false"></text>
      </view>
      
      <view class="admin-section">
        <view class="settings-row">
          <input class="announcement-input" v-model="tempAnnouncement" placeholder="请输入置顶公告" />
          <view class="save-btn-wrapper" @click="setAnnouncement">
            <text class="save-btn-text">保存</text>
          </view>
        </view>
      </view>

      <!-- 4. 红娘有限制其他会员视频、麦克风开启/关闭/下播权限 -->
      <view class="admin-section flex-1">
        <scroll-view scroll-y class="guest-manage-list">
          <view class="guest-manage-item" v-for="guest in allUsers" :key="guest.id">
            <view class="guest-info-top">
              <image class="guest-avatar" :src="guest.avatar" mode="aspectFill"></image>
              <view class="guest-info-block">
                 <view class="g-name-row">
                    <text class="name">{{ guest.name }}<template v-if="guest.id !== 'host'"> ID:{{ guest.displayId || ('1000' + guest.id) }}</template></text>
                  </view>
                 <view class="g-tags">
                    <text class="g-tag">{{ guest.age || '25' }}岁</text>
                    <text class="g-tag">{{ guest.height || '168cm' }}</text>
                    <text class="g-tag">{{ guest.edu || '本科' }}</text>
                    <text class="g-tag">{{ guest.job || '红娘' }}</text>
                  </view>
                </view>
              </view>
            <view class="guest-actions">
              <template v-if="guest.id !== 'host'">
                <button class="action-btn-sm" @click="urgeGuest(guest)">催促上播</button>
                <button class="action-btn-sm" @click="removeMainScreen(guest)">移除大屏</button>
                <button class="action-btn-sm" :class="{ active: guest.isMuted }" @click="toggleGuestMute(guest)">
                  {{ guest.isMuted ? '解禁麦' : '禁麦' }}
                </button>
                <button class="action-btn-sm kick-btn" @click="kickGuest(guest)">
                  移除直播间
                </button>
              </template>
            </view>
          </view>
        </scroll-view>
      </view>
      <view class="admin-section footer-actions" v-if="role === 'host'" style="margin-top: 20rpx;">
        <button class="end-activity-btn" @click="confirmEndActivity">结束相亲活动</button>
      </view>
    </view>

    <view class="summary-mask" v-if="showSummary"></view>
    <view class="summary-popup" v-if="showSummary">
      <text class="ri-checkbox-circle-fill success-icon"></text>
      <text class="title">活动已圆满结束</text>
      <button class="confirm-summary-btn" @click="closeSummary">返回活动列表</button>
    </view>
    <custom-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { onLoad, onShow, onHide } from '@dcloudio/uni-app'

const activityId = ref('')
const role = ref('host') // host: 红娘, guest: 嘉宾
const showAdminPanel = ref(false)
const showSummary = ref(false)
const showViewerList = ref(false)
// const showUserProfile = ref(false)
// const selectedUser = ref<any>(null)

const announcement = ref('欢迎来到相亲活动，请大家文明交流。')
const tempAnnouncement = ref('')

// 默认进入：麦克风开启，视频开启
const myAudioOn = ref(true)
const myVideoOn = ref(true)

// 模拟红娘
const host = ref({
  id: 'host',
  name: '红娘小鸭ID123456',
  avatar: 'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=300',
  isMuted: false, // 默认开启
  videoOn: true,  // 默认开启，可自由选择不开视频
  role: 'host',
  age: 28,
  height: '165cm',
  edu: '本科',
  job: '专业红娘'
})

// 模拟当前大窗直播会员 (倒一、倒二等按顺序大窗)
const currentLiveUser = ref({
  id: 1, name: '勇敢牛牛不怕困难', avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300', isMuted: false, videoOn: true, role: 'live',
  age: 24, height: '178cm', edu: '本科', job: '程序员'
})

// 模拟4个排队位会员
const queueUsers = ref([
  { id: 2, name: '只爱喝可乐', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300', isMuted: false, videoOn: true, age: 28, height: '180cm', edu: '硕士', job: '产品经理' },
  { id: 3, name: '✨KN', avatar: 'https://images.unsplash.com/photo-1517365830460-955ce3ccd263?w=300', isMuted: false, videoOn: true, age: 26, height: '175cm', edu: '本科', job: 'UI设计师' },
  { id: 4, name: '小王不睡', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300', isMuted: false, videoOn: true, age: 25, height: '182cm', edu: '专科', job: '销售' },
  { id: 5, name: 'H.阿丰', avatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300', isMuted: false, videoOn: true, age: 29, height: '176cm', edu: '博士', job: '研究员' }
])

// 队列和红娘：倒一，倒二，倒三，倒四，红娘
const queueAndHost = computed(() => {
  const list: any[] = []
  const labels = ['倒一', '倒二', '倒三', '倒四']
  for (let i = 0; i < 4; i++) {
    if (queueUsers.value[i]) {
      list.push({ ...queueUsers.value[i], roleName: labels[i] })
    } else {
      list.push({ isEmpty: true, roleName: labels[i] })
    }
  }
  list.push({ ...host.value, roleName: '红娘老师' })
  return list
})

const allUsers = computed(() => [host.value, currentLiveUser.value, ...queueUsers.value])

// 模拟相亲会员报名列表 (右上角弹窗数据)
const viewerList = ref([
  { id: 101, name: '小李', avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100', age: 24, height: '178cm', edu: '本科', job: '程序员' },
  { id: 102, name: '张三', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100', age: 28, height: '180cm', edu: '硕士', job: '产品经理' },
  { id: 103, name: '王五', avatar: 'https://images.unsplash.com/photo-1517365830460-955ce3ccd263?w=100', age: 26, height: '175cm', edu: '本科', job: 'UI设计师' }
])

onShow(() => { uni.hideTabBar() })
onHide(() => { uni.showTabBar() })

const messages = ref<any[]>([
  { level: 19, vip: 8, name: '初一', content: '红娘老师好！' },
  { level: 15, name: '小李', content: '来了来了，怎么排队上麦？' }
])
const inputText = ref('')

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
const scrollToId = ref('')

let wsTask: any = null

onLoad((options) => {
  if (options && options.id) {
    activityId.value = options.id
  }
  initWebSocket()
  scrollToBottom()
})

const initWebSocket = () => {
  const token = uni.getStorageSync('token')
  if (!token || !activityId.value) return

  wsTask = uni.connectSocket({
    url: `ws://localhost:8080/ws/activity/${activityId.value}?token=${token}`,
    success: () => { console.log('Activity Room WebSocket connect success') }
  })

  uni.onSocketMessage((res) => {
    try {
      const data = JSON.parse(res.data)
      messages.value.push({ level: 10, name: data.name, content: data.content })
      scrollToBottom()
    } catch(e) { console.error('WS msg error', e) }
  })
}

const handleBack = () => {
  uni.showModal({
    title: '确认退出',
    content: '是否退出当前直播间？',
    success: (res) => { if (res.confirm) uni.navigateBack() }
  })
}

const toggleGuestMute = (guest: any) => {
  guest.isMuted = !guest.isMuted
}

const urgeGuest = (guest: any) => {
  uni.showToast({ title: '已发送催促上播提醒', icon: 'none' })
}

const removeMainScreen = (guest: any) => {
  if (currentLiveUser.value.id === guest.id) {
    if (queueUsers.value.length > 0) {
      currentLiveUser.value = { ...queueUsers.value[0] }
      uni.showToast({ title: '已移除大屏直播', icon: 'none' })
    } else {
      uni.showToast({ title: '暂无其他排队会员', icon: 'none' })
    }
  } else {
    uni.showToast({ title: '该会员不在大屏直播中', icon: 'none' })
  }
}

const kickGuest = (guest: any) => {
  queueUsers.value = queueUsers.value.filter(g => g.id !== guest.id)
  if (currentLiveUser.value.id === guest.id) {
    if (queueUsers.value.length > 0) {
      currentLiveUser.value = { ...queueUsers.value[0] } 
    }
  }
  uni.showToast({ title: '已移除出直播间', icon: 'none' })
}

const confirmEndActivity = () => {
  uni.showModal({
    title: '结束活动',
    content: '确定要结束本场视频相亲活动吗？',
    confirmColor: '#e14c4c',
    success: (res) => {
      if (res.confirm) {
        showAdminPanel.value = false
        showSummary.value = true
      }
    }
  })
}

const closeSummary = () => {
  showSummary.value = false
  uni.navigateBack()
}

const scrollToBottom = () => {
  nextTick(() => {
    setTimeout(() => { scrollToId.value = 'msg-' + (messages.value.length - 1) }, 100)
  })
}

const sendMsg = () => {
  if (!inputText.value.trim()) return
  messages.value.push({
    level: 25,
    name: role.value === 'host' ? '我(主播)' : '我',
    content: inputText.value
  })
  scrollToBottom()
  inputText.value = ''
}

const openUserProfile = (user: any) => {
  // 不再使用小窗口，直接跳转到用户详情主页
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.id}` })
}

const handleViewerClick = (user: any) => {
  openUserProfile(user)
}

const raiseHand = () => {
  uni.showToast({ title: '已举手，等待红娘同意上麦', icon: 'none' })
}

const setAnnouncement = () => {
  if (!tempAnnouncement.value.trim()) {
    uni.showToast({ title: '请输入公告内容', icon: 'none' })
    return
  }
  announcement.value = tempAnnouncement.value
  uni.showToast({ title: '公告已更新', icon: 'success' })
  tempAnnouncement.value = ''
}

const toggleMyAudio = () => {
  myAudioOn.value = !myAudioOn.value;
  // 同步更新自己(如果是host)
  if (role.value === 'host') {
    host.value.isMuted = !myAudioOn.value
  }
  uni.showToast({ title: myAudioOn.value ? '已开启麦克风' : '已关闭麦克风', icon: 'none' })
}

const toggleMyVideo = () => {
  myVideoOn.value = !myVideoOn.value;
  if (role.value === 'host') {
    host.value.videoOn = myVideoOn.value
  }
  uni.showToast({ title: myVideoOn.value ? '已开启摄像头' : '已关闭摄像头', icon: 'none' })
}

onUnmounted(() => {
  if (wsTask) uni.closeSocket()
  uni.showTabBar()
})
</script>

<style lang="scss" scoped>
.room-container {
  height: 100vh;
  /* 整体背景色，类似截图的粉紫色 */
  background: linear-gradient(180deg, #CE7D9F 0%, #683863 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  padding-bottom: 0;
}

/* ================= 顶部导航与信息 ================= */
.room-header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  pointer-events: none; /* 让事件穿透 */
  padding: 0 20rpx;
}

.header-row-1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
  height: 80rpx;
  pointer-events: auto; /* 恢复内部元素的点击 */
}

.host-pill {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 40rpx; /* 全圆角 */
  display: flex;
  align-items: center;
  padding: 4rpx;
  padding-right: 16rpx;
  height: 64rpx; /* 强制高度与右侧列表一致 */
  box-sizing: border-box;
}

.host-avatar {
  width: 56rpx; /* 调整头像尺寸以适应容器高度 */
  height: 56rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.host-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-right: 16rpx;
  
  .host-name {
    font-size: 24rpx;
    color: #ffffff;
    font-weight: bold;
    max-width: 400rpx; /* 足够宽以显示完整昵称 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
  position: relative;
  z-index: 200; /* 提升层级，确保能被点击 */
}

.viewer-pill {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 40rpx; /* 全圆角 */
  display: flex;
  align-items: center;
  padding: 4rpx 16rpx 4rpx 6rpx;
  height: 64rpx; /* 强制高度与左侧主播一致 */
  box-sizing: border-box;
}

.viewer-avatars {
  display: flex;
  margin-right: 8rpx;
  
  .v-avatar {
    width: 48rpx; /* 调整头像尺寸以适应容器高度 */
    height: 48rpx;
    border-radius: 50%;
    border: 2rpx solid transparent;
    margin-right: -16rpx;
    
    &:last-child {
      margin-right: 0;
    }
  }
}

.viewer-count {
  font-size: 24rpx; /* 字号与左侧一致 */
  color: #ffffff;
  margin-left: 8rpx;
  font-weight: 500;
}

.close-btn {
  width: 56rpx;
  height: 56rpx;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .icon {
    font-size: 32rpx;
    color: #ffffff;
  }
}

/* ================= 核心区域 ================= */
.grid-container {
  width: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  padding: 0 4rpx;
}

.main-live-window {
  width: 100%;
  aspect-ratio: 1; /* 正方形大窗 */
  background-color: #2c2c2e;
  position: relative;
  overflow: hidden;
  border-radius: 12rpx;
}

.main-video {
  width: 100%;
  height: 100%;
  display: block;
}

.main-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #1e1e20;
  .avatar-round { width: 160rpx; height: 160rpx; border-radius: 50%; opacity: 0.8; }
}

.user-bottom-info {
  position: absolute;
  bottom: 16rpx;
  left: 16rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  z-index: 2;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.8);
  .icon { font-size: 24rpx; color: #ffffff; background-color: rgba(0,0,0,0.3); border-radius: 50%; padding: 4rpx; }
  .name-text { font-size: 26rpx; color: #ffffff; font-weight: bold; }
}

.live-tag {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  background-color: #ff3b30;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  z-index: 2;
}

.queue-row {
  display: flex;
  width: 100%;
  gap: 4rpx;
  height: 160rpx; /* 高度适中 */
}

.queue-item {
  flex: 1;
  background-color: #1e1e20;
  position: relative;
  overflow: hidden;
  border-radius: 8rpx;
}

.q-video { width: 100%; height: 100%; display: block; }
.q-avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; justify-content: center; align-items: center;
  .q-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; }
}

.q-info {
  position: absolute; bottom: 8rpx; left: 8rpx;
  display: flex; align-items: center; gap: 4rpx;
  .icon { font-size: 16rpx; color: #fff; background-color: rgba(0,0,0,0.3); border-radius: 50%; padding: 2rpx; }
  .name { font-size: 18rpx; color: #fff; max-width: 80rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-shadow: 0 1px 2px rgba(0,0,0,0.8); }
}

.role-tag {
  position: absolute; top: 8rpx; right: 8rpx;
  background-color: rgba(0,0,0,0.5); color: #FFB03A; font-size: 16rpx; padding: 2rpx 8rpx; border-radius: 10rpx;
}

.queue-empty {
  width: 100%; height: 100%;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  background-color: rgba(0, 0, 0, 0.1);
  color: rgba(255, 255, 255, 0.3);
  .icon { font-size: 32rpx; margin-bottom: 4rpx; }
  .txt { font-size: 18rpx; }
}

/* ================= 底部渐变遮罩 ================= */
.bottom-gradient-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 600rpx;
  background: linear-gradient(to top, #683863 0%, rgba(104, 56, 99, 0.8) 40%, transparent 100%);
  z-index: 1;
  pointer-events: none;
}

/* ================= 公聊区 ================= */
.chat-area-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 0; /* 挤压空间 */
  margin-top: 30rpx;
}

.chat-area {
  max-height: 260rpx;
  padding: 0 20rpx;
  margin-bottom: 20rpx;
  z-index: 20;
  mask-image: linear-gradient(to bottom, transparent 0%, black 20%);
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 20%);
}

.sys-msg-banner {
  background-color: rgba(0, 0, 0, 0.15);
  color: #FFB03A;
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  margin-left: 20rpx;
  display: flex;
  align-items: center;
  max-width: 80%;
  z-index: 25;
  
  .announcement-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.msg-item {
  margin-bottom: 12rpx;
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  line-height: 1.5;
  background-color: rgba(0, 0, 0, 0.15);
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
  max-width: 85%;
  align-self: flex-start;
}

.nickname {
  color: #A097FF;
  font-size: 26rpx;
  margin-right: 8rpx;
  line-height: 40rpx;
}

.content {
  color: #ffffff;
  font-size: 26rpx;
  line-height: 40rpx;
}


/* ================= 底部操作栏 ================= */
.bottom-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  z-index: 20;
  gap: 16rpx;
  background: transparent;
  padding-bottom: calc(16rpx + 20px);
}

.input-wrap {
  flex: 1;
  height: 72rpx;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  
  .chat-input {
    flex: 1;
    font-size: 26rpx;
    color: #ffffff;
  }
  
  .emoji-icon {
    font-size: 40rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-left: 16rpx;
  }
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 40rpx;
    color: #ffffff;
  }
}

.link-btn {
  background: linear-gradient(135deg, #6A82FB 0%, #A097FF 100%);
}

.media-btn {
  background-color: rgba(0, 0, 0, 0.4);
}

.hand-btn {
  background: linear-gradient(135deg, #6A82FB 0%, #A097FF 100%);
  .hand-text {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    line-height: 1.1;
    text {
      font-size: 20rpx;
      font-weight: bold;
    }
  }
}

/* ================= 观众列表弹框 ================= */
.viewer-list-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 105;
}
.viewer-list-panel {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  height: 60vh;
  background-color: #1c1c1e;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx;
  z-index: 106;
  display: flex;
  flex-direction: column;
}
.v-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  .title { font-size: 32rpx; color: #fff; font-weight: bold; }
  .close-btn { font-size: 40rpx; color: #8e8e93; }
}
.v-list {
  flex: 1;
  height: 0;
}
.v-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.v-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}
.v-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  .v-name-row {
    display: flex;
    align-items: center;
    margin-bottom: 8rpx;
    .v-name { font-size: 28rpx; color: #fff; font-weight: 500; margin-right: 12rpx; }
    .v-id { font-size: 22rpx; color: #A097FF; }
  }
  .v-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    .v-tag { font-size: 20rpx; color: #8e8e93; background-color: rgba(255,255,255,0.08); padding: 2rpx 10rpx; border-radius: 8rpx; }
  }
}
.v-action {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 20rpx;
  .chat-icon { font-size: 32rpx; color: #A097FF; }
}

/* ================= 红娘面板弹框 ================= */
.admin-mask, .summary-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

.admin-panel {
  position: fixed;
  bottom: var(--window-bottom, 0);
  left: 0; right: 0;
  height: 65vh; /* 稍微增加高度以容纳更多间距 */
  background-color: #1c1c1e;
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx 24rpx; /* 减小左右内边距，让内容更宽 */
  padding-bottom: env(safe-area-inset-bottom, 40rpx);
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx; /* 调整标题与下方内容的间距 */
  flex-shrink: 0;
  padding: 0 16rpx; /* 补偿面板左右缩小的 padding */
  
  .title { 
    font-size: 32rpx;
    font-weight: bold;
    color: #ffffff;
  }
  .close-btn { font-size: 40rpx; color: #8e8e93; }
}

.admin-section {
  margin-bottom: 30rpx; /* 调整各个版块之间的上下间距 */
  flex-shrink: 0;
  padding: 0 16rpx; /* 补偿面板左右缩小的 padding */
  
  .section-title { font-size: 28rpx; font-weight: 500; color: #ffffff; margin-bottom: 20rpx; display: block; }
}

.admin-section.flex-1 {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-bottom: 0;
}

.settings-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.announcement-input {
  flex: 1;
  height: 56rpx;
  background-color: rgba(255, 255, 255, 0.08);
  border-radius: 28rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #ffffff;
}

.save-btn-wrapper {
  background: linear-gradient(135deg, #6A82FB 0%, #A097FF 100%);
  width: 90rpx;
  height: 56rpx;
  border-radius: 28rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}
.save-btn-text {
  font-size: 24rpx;
  color: #ffffff;
  line-height: 1;
}

.guest-manage-list {
  flex: 1;
  height: 0;
  background-color: transparent;
  border-radius: 0;
  padding: 0;
}

.guest-manage-item {
  display: flex;
  flex-direction: column;
  padding: 30rpx 16rpx; /* 增加上下间距，并补充左右 padding */
  border-bottom: 1px solid rgba(255,255,255,0.08);
  
  &:last-child {
    border-bottom: none;
  }
}

.guest-info-top {
  display: flex;
  align-items: flex-start;
  width: 100%;
  margin-bottom: 16rpx;
}

.guest-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; margin-right: 20rpx; flex-shrink: 0; }

.guest-info-block {
  flex: 1;
  display: flex; flex-direction: column;
  .g-name-row {
    display: flex; align-items: center; margin-bottom: 8rpx; flex-wrap: wrap;
    .name { font-size: 24rpx; color: #ffffff; font-weight: 500; }
  }
  .g-tags {
    display: flex; flex-wrap: wrap; gap: 8rpx;
    .g-tag { font-size: 20rpx; color: #8e8e93; background-color: rgba(255,255,255,0.08); padding: 2rpx 10rpx; border-radius: 8rpx; }
  }
}

.guest-actions { 
  display: flex; 
  gap: 16rpx; 
  justify-content: flex-end; 
  width: 100%; 
  padding-left: 84rpx; /* 与缩小的头像对齐 (64rpx + 20rpx) */
  box-sizing: border-box;
}

.action-btn-sm { 
  font-size: 20rpx; 
  padding: 0 10rpx; 
  height: 52rpx; 
  line-height: 52rpx; 
  background-color: rgba(255,255,255,0.1); 
  color: #ffffff; 
  border-radius: 26rpx; 
  margin: 0; 
  flex: 1;
  text-align: center;
  white-space: nowrap;
}
.action-btn-sm.active { background-color: #6A82FB; color: #ffffff; }
.kick-btn { background-color: rgba(255,59,48,0.15); color: #ff3b30; }
.end-activity-btn { width: 100%; height: 96rpx; line-height: 96rpx; background: linear-gradient(135deg, #6A82FB 0%, #A097FF 100%); color: #ffffff; font-size: 30rpx; font-weight: bold; border-radius: 48rpx; margin-top: 10rpx; }

/* 结算弹窗 */
.summary-popup {
  position: fixed;
  top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 580rpx;
  background-color: #1c1c1e;
  border-radius: 30rpx;
  padding: 50rpx 40rpx;
  z-index: 100;
  text-align: center;
}
.success-icon { font-size: 100rpx; color: #4cd964; margin-bottom: 24rpx; display: inline-block; }
.summary-popup .title { font-size: 36rpx; color: #ffffff; margin-bottom: 40rpx; display: block; }
.confirm-summary-btn { background-color: #6a61f8; color: #ffffff; height: 84rpx; line-height: 84rpx; border-radius: 42rpx; font-size: 28rpx; }
</style>
