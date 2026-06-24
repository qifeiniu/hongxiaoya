<template>
  <view class="message-container">
    <view class="custom-nav-spacer" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 顶部导航与搜索 -->
    <view class="header">
      <text class="page-title">消息</text>
      <view class="header-right">
        <view class="search-bar">
          <text class="ri-search-line search-icon"></text>
          <input class="search-input" v-model="searchKeyword" placeholder="搜索ID/昵称" />
          <text class="ri-close-circle-fill clear-icon" v-if="searchKeyword" @click="searchKeyword = ''"></text>
        </view>
        <text class="ri-brush-line icon-btn" @click="clearUnread"></text>
      </view>
    </view>

    <!-- 消息分类 Tabs -->
    <view class="tabs-scroll">
      <view class="tabs-container">
        <view class="tab-item" 
              v-for="(tab, index) in tabs" 
              :key="index"
              :class="{ active: currentTab === index }"
              @click="currentTab = index">
          <view class="tab-content-wrap">
            <text class="tab-text">{{ tab }}</text>
            <view class="tab-badge" v-if="unreadCounts[index] > 0">{{ unreadCounts[index] > 99 ? '99+' : unreadCounts[index] }}</view>
          </view>
          <view class="active-line" v-if="currentTab === index"></view>
        </view>
      </view>
    </view>

    <!-- 会话列表 - 私聊 -->
    <scroll-view scroll-y class="chat-list" v-if="currentTab === 0">
      <view class="chat-item-wrap" v-for="chat in filteredChatList" :key="chat.id">
        <view class="chat-item-content"
              :style="{ transform: `translateX(${swipeState[chat.id]?.translateX || 0}px)`, transition: swipeState[chat.id]?.isMoving ? 'none' : 'transform 0.3s' }"
              @touchstart="touchStart($event, chat)"
              @touchmove="touchMove($event, chat)"
              @touchend="touchEnd($event, chat)"
              @click="goChat(chat)"
              @longpress="showChatMenu(chat)">
            <view class="chat-item" :class="{ 'is-top': chat.isTop === 1 }">
              <view class="avatar-wrap">
                <image class="avatar" :src="chat.targetProfile?.avatar || '/static/logo.png'" mode="aspectFill"></image>
                <view class="badge" v-if="chat.unreadCount > 0">{{ chat.unreadCount }}</view>
              </view>
            
            <view class="chat-info">
              <view class="chat-header">
                <view class="name-wrap">
                  <text class="nickname">{{ chat.targetProfile?.nickname || '用户' }}</text>
                  <view class="official-tag" v-if="chat.isOfficial">
                    <text class="ri-verified-badge-fill tag-icon"></text>
                    <text>官方</text>
                  </view>
                </view>
                <text class="time">{{ formatTime(chat.lastMsgTime) }}</text>
              </view>
              <view class="chat-content">
                <text class="msg-preview" :class="{ 'unread-text': chat.unreadCount > 0 }">{{ chat.lastMsgContent || '...' }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="chat-actions">
          <view class="action-btn top-btn" @click.stop="toggleTop(chat)">
            <text>{{ chat.isTop === 1 ? '取消置顶' : '置顶' }}</text>
          </view>
          <view class="action-btn del-btn" @click.stop="deleteChat(chat)">
            <text>删除</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="filteredChatList.length === 0">
        <text class="empty-text">暂无相关消息</text>
      </view>
    </scroll-view>

    <!-- 系统通知列表 -->
    <scroll-view scroll-y class="notification-list" v-if="currentTab === 1">
      <view class="noti-item" v-for="item in systemNotifications" :key="item.id">
        <view class="noti-icon-wrap" :class="item.iconBg">
          <text :class="item.icon"></text>
        </view>
        <view class="noti-content">
          <view class="noti-header">
            <text class="noti-title">{{ item.title }}</text>
            <text class="noti-time">{{ item.time }}</text>
          </view>
          <text class="noti-desc">{{ item.desc }}</text>
        </view>
      </view>

      <view class="empty-state" v-if="systemNotifications.length === 0">
        <text class="ri-notification-off-line empty-icon"></text>
        <text class="empty-text">暂无通知</text>
      </view>
    </scroll-view>

    <!-- 收到的花列表 -->
    <scroll-view scroll-y class="chat-list" v-if="currentTab === 2">
      <view class="chat-item-wrap" v-for="item in flowerNotifications" :key="item.id">
        <view class="chat-item-content"
              :style="{ transform: `translateX(${swipeState[item.id]?.translateX || 0}px)`, transition: swipeState[item.id]?.isMoving ? 'none' : 'transform 0.3s' }"
              @touchstart="touchStart($event, item)"
              @touchmove="touchMove($event, item)"
              @touchend="touchEnd($event, item)"
              @click="markFlowerRead(item)">
          <view class="chat-item">
            <view class="avatar-wrap">
              <image class="avatar" :src="item.user?.avatar || '/static/logo.png'" mode="aspectFill"></image>
              <view class="badge" v-if="item.unread">1</view>
            </view>
          
            <view class="chat-info">
              <view class="chat-header">
                <view class="name-wrap">
                  <text class="nickname">{{ item.user?.nickname || '用户' }}</text>
                </view>
                <text class="time">{{ item.time }}</text>
              </view>
              <view class="chat-content">
                <text class="msg-preview" :class="{ 'unread-text': item.unread }">对方送了花</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="chat-actions">
          <view class="action-btn del-btn" @click.stop="deleteFlower(item)">
            <text>删除</text>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="flowerNotifications.length === 0">
        <text class="ri-heart-3-line empty-icon"></text>
        <text class="empty-text">暂无收到的花</text>
      </view>
    </scroll-view>

    <!-- 筛选面板 -->
    <view class="mask" v-if="showFilterPanel" @click="showFilterPanel = false"></view>
    <view class="filter-panel" :class="{ 'show': showFilterPanel }">
      <view class="panel-header">
        <text>筛选消息</text>
        <text class="ri-close-line close-icon" @click="showFilterPanel = false"></text>
      </view>
      
      <view class="filter-section">
        <view class="section-title">消息状态</view>
        <view class="tags-wrap">
          <view class="filter-tag" :class="{ active: filterForm.status === 'all' }" @click="filterForm.status = 'all'">全部</view>
          <view class="filter-tag" :class="{ active: filterForm.status === 'unread' }" @click="filterForm.status = 'unread'">未读消息</view>
          <view class="filter-tag" :class="{ active: filterForm.status === 'read' }" @click="filterForm.status = 'read'">已读消息</view>
        </view>
      </view>

      <view class="filter-section">
        <view class="section-title">时间范围</view>
        <view class="tags-wrap">
          <view class="filter-tag" :class="{ active: filterForm.time === 'all' }" @click="filterForm.time = 'all'">全部时间</view>
          <view class="filter-tag" :class="{ active: filterForm.time === 'today' }" @click="filterForm.time = 'today'">今天</view>
          <view class="filter-tag" :class="{ active: filterForm.time === 'week' }" @click="filterForm.time = 'week'">最近一周</view>
          <view class="filter-tag" :class="{ active: filterForm.time === 'month' }" @click="filterForm.time = 'month'">最近一月</view>
        </view>
      </view>

      <view class="panel-bottom">
        <button class="reset-btn" @click="resetFilter">重置</button>
        <button class="confirm-btn" @click="applyFilter">确定</button>
      </view>
    </view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { request } from '../../utils/request'
import { onShow } from '@dcloudio/uni-app'
import { checkAuth, globalAuthGuard, requireLogin } from '../../utils/auth'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0

const tabs = ['私聊', '系统通知', '收到的花']
const currentTab = ref(0)

const searchKeyword = ref('')

const showFilterPanel = ref(false)
const openFilterPanel = () => {
  if (!requireLogin('筛选消息')) return
  showFilterPanel.value = true
}
const filterForm = ref({
  status: 'all', // all, unread, read
  time: 'all' // all, today, week, month
})

// 保存应用了的筛选条件，只在点击确定时生效
const appliedFilter = ref({
  status: 'all',
  time: 'all'
})

const resetFilter = () => {
  filterForm.value = {
    status: 'all',
    time: 'all'
  }
}

const applyFilter = () => {
  appliedFilter.value = { ...filterForm.value }
  showFilterPanel.value = false
}

const chatList = ref<any[]>([])

// 系统通知数据
const systemNotifications = ref([
  { id: 4, type: 'system', icon: 'ri-shield-check-fill', iconBg: 'bg-blue', title: '认证通过', desc: '你的实名认证已通过审核', time: '2小时前' },
  { id: 5, type: 'system', icon: 'ri-coin-fill', iconBg: 'bg-gold', title: '签到奖励', desc: '签到成功，获得10鸭蛋', time: '今天 08:00' },
  { id: 7, type: 'system', icon: 'ri-message-3-fill', iconBg: 'bg-primary', title: '系统通知', desc: '欢迎来到单身青年自救平台，期待你找到心仪的另一半。', time: '昨天' }
])

// 收到的花数据
const flowerNotifications = ref([
  { 
    id: 1, 
    type: 'flower', 
    icon: 'ri-seedling-line', 
    iconBg: 'bg-red', 
    title: '收到玫瑰花', 
    desc: '“星空下的猫”送了你1朵玫瑰花', 
    time: '10:42', 
    unread: true,
    user: {
      avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=face',
      nickname: '星空下的猫'
    },
    flower: {
      name: '玫瑰花',
      count: 1,
      emoji: '🌹'
    }
  },
  { 
    id: 2, 
    type: 'flower', 
    icon: 'ri-seedling-line', 
    iconBg: 'bg-pink', 
    title: '收到郁金香', 
    desc: '“阳光男孩”送了你3朵郁金香', 
    time: '昨天', 
    unread: true,
    user: {
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face',
      nickname: '阳光男孩'
    },
    flower: {
      name: '郁金香',
      count: 3,
      emoji: '🌷'
    }
  },
  { 
    id: 3, 
    type: 'flower', 
    icon: 'ri-seedling-line', 
    iconBg: 'bg-gold', 
    title: '收到向日葵', 
    desc: '“小仙女”送了你2朵向日葵', 
    time: '3天前', 
    unread: true,
    user: {
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=face',
      nickname: '小仙女'
    },
    flower: {
      name: '向日葵',
      count: 2,
      emoji: '🌻'
    }
  }
])

// 左滑相关状态与逻辑
const swipeState = ref<Record<string, { translateX: number, isMoving: boolean }>>({})
let startX = 0
let startY = 0
let currentSwipingId = ''
const actionWidthPx = uni.upx2px(300) // 两个按钮，每个150rpx，共300rpx
let isSwiping = false

const touchStart = (e: any, chat: any) => {
  startX = e.touches[0].clientX
  startY = e.touches[0].clientY
  
  if (currentSwipingId && currentSwipingId !== chat.id) {
    if (swipeState.value[currentSwipingId]) {
      swipeState.value[currentSwipingId].translateX = 0
    }
    currentSwipingId = ''
  }
  
  if (!swipeState.value[chat.id]) {
    swipeState.value[chat.id] = { translateX: 0, isMoving: false }
  }
  swipeState.value[chat.id].isMoving = true
}

const touchMove = (e: any, chat: any) => {
  const currentX = e.touches[0].clientX
  const currentY = e.touches[0].clientY
  const dx = currentX - startX
  const dy = currentY - startY
  
  if (Math.abs(dx) > Math.abs(dy)) {
    if (Math.abs(dx) > 5) isSwiping = true
    const state = swipeState.value[chat.id]
    const baseTranslateX = currentSwipingId === chat.id ? -actionWidthPx : 0
    let newTranslateX = baseTranslateX + dx
    
    if (newTranslateX > 0) newTranslateX = 0
    if (newTranslateX < -actionWidthPx) newTranslateX = -actionWidthPx
    
    state.translateX = newTranslateX
  }
}

const touchEnd = (e: any, chat: any) => {
  const state = swipeState.value[chat.id]
  if (!state) return
  state.isMoving = false
  
  if (state.translateX < -actionWidthPx / 2) {
    state.translateX = -actionWidthPx
    currentSwipingId = chat.id
  } else {
    state.translateX = 0
    if (currentSwipingId === chat.id) {
      currentSwipingId = ''
    }
  }
  
  setTimeout(() => {
    isSwiping = false
  }, 50)
}

const toggleTop = async (chat: any) => {
  if (!requireLogin('会话管理')) return
  const isTop = chat.isTop === 1;
  try {
    // 乐观更新 UI
    chat.isTop = isTop ? 0 : 1;
    
    // 手动触发计算属性重新排序（如果是 proxy 数组，修改属性可能不足以触发深度排序，这里强制触发）
    chatList.value = [...chatList.value];
    
    uni.showToast({ title: '操作成功', icon: 'success' });
    
    if (swipeState.value[chat.id]) {
      swipeState.value[chat.id].translateX = 0;
    }
    currentSwipingId = '';
    
    // 如果是前端 mock，就不等接口返回了
    await request({ 
      url: `/message/top?targetId=${chat.targetId}&isTop=${!isTop}`, 
      method: 'POST' 
    }).catch(e => {
      // 失败则回滚
      chat.isTop = isTop ? 1 : 0;
      chatList.value = [...chatList.value];
    });
    
  } catch(e) {}
}

const deleteChat = (chat: any) => {
  if (!requireLogin('会话管理')) return
  uni.showModal({
    title: '提示',
    content: '确定要删除该会话吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已删除会话', icon: 'none' });
        chatList.value = chatList.value.filter(c => c.id !== chat.id);
      }
      if (swipeState.value[chat.id]) {
        swipeState.value[chat.id].translateX = 0;
      }
      currentSwipingId = '';
    }
  });
}

const unreadCounts = computed(() => {
  const counts = [0, 0, 0] // 私聊, 系统通知, 收到的花

  // 私聊未读
  chatList.value.forEach(chat => {
    if ((!chat.type || chat.type === 'private') && chat.unreadCount > 0) {
      counts[0] += chat.unreadCount
    }
  })

  // 收到的花未读
  flowerNotifications.value.forEach(noti => {
    if (noti.unread) {
      counts[2]++
    }
  })

  // 系统通知暂时没有未读逻辑
  counts[1] = 0

  return counts
})

// 模拟获取数据
const fetchChats = async () => {
  try {
    const res: any = await request({ url: '/message/conversations', method: 'GET' })
    chatList.value = res || [];
  } catch (e) {
    console.error(e)
  }
}

const filteredChatList = computed(() => {
  let list = chatList.value
  const now = Date.now()
  const dayMs = 86400000
  
  // 1. Tabs 筛选
  if (currentTab.value === 0) {
    // 私聊
    list = list.filter(c => !c.type || c.type === 'private')
  } else if (currentTab.value === 1) {
    // 系统通知 - 非私聊、非like/gift的消息
    list = list.filter(c => c.type && c.type !== 'private' && c.type !== 'like' && c.type !== 'gift')
  } else if (currentTab.value === 2) {
    // 收到的花 - like/gift消息
    list = list.filter(c => c.type === 'like' || c.type === 'gift')
  }

  // 2. 状态筛选
  if (appliedFilter.value.status === 'unread') {
    list = list.filter(chat => (chat.unreadCount || 0) > 0)
  } else if (appliedFilter.value.status === 'read') {
    list = list.filter(chat => !(chat.unreadCount || 0))
  }

  // 3. 时间筛选
  if (appliedFilter.value.time !== 'all') {
    list = list.filter(chat => {
      if (!chat.lastMsgTime) return false
      const msgTime = new Date(chat.lastMsgTime).getTime()
      if (appliedFilter.value.time === 'today') {
        return now - msgTime < dayMs
      } else if (appliedFilter.value.time === 'week') {
        return now - msgTime < dayMs * 7
      } else if (appliedFilter.value.time === 'month') {
        return now - msgTime < dayMs * 30
      }
      return true
    })
  }

  // 4. 关键字搜索
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((chat: any) => {
      const nickname = (chat.targetProfile?.nickname || '').toLowerCase()
      const id = String(chat.targetId || '')
      return nickname.includes(kw) || id.includes(kw)
    })
  }
  
  // PRD: 2.4.1 按时间倒序
  list.sort((a, b) => {
    const timeA = new Date(a.lastMsgTime || 0).getTime()
    const timeB = new Date(b.lastMsgTime || 0).getTime()
    return timeB - timeA
  })

  // 置顶优先
  list.sort((a, b) => (b.isTop || 0) - (a.isTop || 0))

  return list
})

const clearUnread = () => {
  if (!requireLogin('清除未读')) return
  uni.showToast({ title: '已清除未读', icon: 'none' })
  chatList.value.forEach(chat => chat.unreadCount = 0)
}

const goSystemNotice = () => {
  if (!requireLogin('查看通知')) return
  uni.navigateTo({ url: '/pages/notification/index' })
}

// 标记收到的花为已读并跳转到聊天页面
const markFlowerRead = async (item: any) => {
  if (isSwiping) return
  
  if (currentSwipingId) {
    if (swipeState.value[currentSwipingId]) {
      swipeState.value[currentSwipingId].translateX = 0
    }
    currentSwipingId = ''
    return
  }

  if (item.unread) {
    item.unread = false
  }
  
  if (!(await checkAuth('聊天'))) return

  const targetId = item.userId || item.id
  const targetName = item.user?.nickname || '聊天'
  
  uni.navigateTo({
    url: `/pages/chat/index?id=${targetId}&name=${targetName}&source=received_flower&flowerName=${item.flower?.name || '花'}&flowerIcon=${item.flower?.emoji || '🌹'}`,
    events: {
      // 监听聊天页面触发的“开启私聊”事件
      onChatUpgraded: function() {
        // 从“收到的花”列表中移除该消息
        flowerNotifications.value = flowerNotifications.value.filter(f => f.id !== item.id)
        
        // 并在私聊列表中增加该消息的记录 (模拟数据同步)
        const isExist = chatList.value.find(c => c.targetId === targetId)
        if (!isExist) {
          chatList.value.unshift({
            id: Date.now(),
            targetId: targetId,
            type: 'private',
            targetProfile: {
              nickname: targetName,
              avatar: item.user?.avatar
            },
            lastMsgContent: '你回复了对方',
            lastMsgTime: new Date().toISOString(),
            unreadCount: 0,
            isTop: 0
          })
        }
      }
    }
  })
}

// 删除收到的花
const deleteFlower = (item: any) => {
  if (!requireLogin('会话管理')) return
  uni.showModal({
    title: '提示',
    content: '确定要删除这条消息吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已删除', icon: 'none' });
        flowerNotifications.value = flowerNotifications.value.filter(f => f.id !== item.id);
      }
      if (swipeState.value[item.id]) {
        swipeState.value[item.id].translateX = 0;
      }
      currentSwipingId = '';
    }
  });
}

const goChat = async (chat: any) => {
  if (isSwiping) return
  
  if (currentSwipingId) {
    if (swipeState.value[currentSwipingId]) {
      swipeState.value[currentSwipingId].translateX = 0
    }
    currentSwipingId = ''
    return
  }

  // 如果是系统类通知，跳转到通知页面
  if (chat.type && chat.type !== 'private') {
    chat.unreadCount = 0
    goSystemNotice()
    return
  }

  if (!(await checkAuth('聊天'))) return

  chat.unreadCount = 0
  // Mark as read in backend
  request({ url: `/message/read?targetId=${chat.targetId}`, method: 'POST' }).catch(()=>{})
  uni.navigateTo({
    url: `/pages/chat/index?id=${chat.targetId}&name=${chat.targetProfile?.nickname || '聊天'}&source=private_chat`
  })
}

const showChatMenu = (chat: any) => {
  if (!requireLogin('会话管理')) return
  const isTop = chat.isTop === 1;
  uni.showActionSheet({
    itemList: [isTop ? '取消置顶' : '置顶聊天', '解除聊天权限', '删除会话'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        try {
          await request({ 
            url: `/message/top?targetId=${chat.targetId}&isTop=${!isTop}`, 
            method: 'POST' 
          });
          uni.showToast({ title: '操作成功', icon: 'success' });
          fetchChats();
        } catch(e) {}
      } else if (res.tapIndex === 1) {
        uni.showToast({ title: '已解除聊天权限', icon: 'none' });
      } else if (res.tapIndex === 2) {
        uni.showToast({ title: '已删除会话', icon: 'none' });
      }
    }
  });
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  
  // 如果是今天，显示 HH:MM
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  // 否则显示月日
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

onShow(() => {
  if (!globalAuthGuard()) return
  if (uni.getStorageSync('token')) {
    fetchChats()
  } else {
    chatList.value = []
  }
})
</script>

<style lang="scss" scoped>
.message-container {
  background-color: $uni-bg-color;
  height: calc(100vh - var(--window-bottom));
  display: flex;
  flex-direction: column;
}

.custom-nav-spacer {
  background-color: $uni-bg-color;
  width: 100%;
}

/* 顶部导航 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 32rpx;
  background-color: $uni-bg-color;
}

.page-title {
  font-size: 48rpx;
  font-weight: 600;
  color: $uni-text-color;
  letter-spacing: 1rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex: 1;
  justify-content: flex-end;
  
  .icon-btn {
    font-size: 44rpx;
    color: $uni-text-color-regular;
    font-weight: 500;
  }
}

/* 搜索框区域 */
.search-bar {
  display: flex;
  align-items: center;
  background-color: $uni-bg-color-page;
  height: 64rpx;
  border-radius: $uni-border-radius-pill;
  padding: 0 24rpx;
  width: 320rpx;
  
  .search-icon {
    font-size: 28rpx;
    color: $uni-text-color-placeholder;
    margin-right: 8rpx;
  }
  
  .search-input {
    flex: 1;
    font-size: 26rpx;
    height: 100%;
  }
  
  .clear-icon {
    font-size: 28rpx;
    color: $uni-text-color-placeholder;
    margin-left: 8rpx;
  }
}

/* 消息分类 Tabs */
.tabs-scroll {
  width: 100%;
  padding: 24rpx 0 20rpx;
  border-bottom: 1px solid rgba(0,0,0,0.02);
  display: flex;
  justify-content: center;
}

.tabs-container {
  display: inline-flex;
  padding: 0 32rpx;
  gap: 48rpx;
  flex: 1;
  justify-content: space-around;
}

.tab-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 12rpx;
  
  .tab-content-wrap {
    display: flex;
    align-items: flex-start;
    position: relative;
  }

  .tab-text {
    font-size: 30rpx;
    color: $uni-text-color-grey;
    transition: all 0.3s;
  }
  
  .tab-badge {
    position: absolute;
    top: -14rpx;
    right: -30rpx;
    background-color: #FF3B30;
    color: #fff;
    font-size: 20rpx;
    font-weight: 500;
    min-width: 28rpx;
    height: 28rpx;
    line-height: 28rpx;
    text-align: center;
    border-radius: 14rpx;
    padding: 0 6rpx;
    box-sizing: border-box;
    transform: scale(0.9);
  }
  
  &.active {
    .tab-text {
      color: $uni-text-color;
      font-weight: 600;
    }
  }
  
  .active-line {
    position: absolute;
    bottom: 0;
    width: 40rpx;
    height: 6rpx;
    background-color: $uni-color-primary;
    border-radius: 6rpx;
  }
}

/* 会话列表 */
.chat-list {
  flex: 1;
  overflow: hidden;
}

.chat-item-wrap {
  position: relative;
  width: 100%;
  overflow: hidden;
  background-color: $uni-bg-color;
}

.chat-item-content {
  width: 100%;
  position: relative;
  z-index: 2;
  background-color: $uni-bg-color;
}

.chat-actions {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  display: flex;
  z-index: 1;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 150rpx;
  color: #fff;
  font-size: 28rpx;
}

.top-btn {
  background-color: #C8C7CC;
}

.del-btn {
  background-color: #FF3B30;
}

.chat-item {
  display: flex;
  padding: 0 32rpx;
  background-color: $uni-bg-color;
  transition: background-color 0.3s;
  
  &.is-top {
    background-color: $uni-bg-color-page;
  }

  &:active {
    background-color: $uni-bg-color-hover;
  }
}

.avatar-wrap {
  position: relative;
  margin-right: 24rpx;
  padding: 24rpx 0;
  display: flex;
  align-items: center;
  
  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: $uni-border-radius-circle;
    background-color: $uni-bg-color-page;
  }
  
  .system-avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: $uni-border-radius-circle;
    background: $uni-color-primary-gradient;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .system-icon {
      font-size: 48rpx;
      color: #fff;
    }
  }
  
  .badge {
    position: absolute;
    top: 20rpx;
    right: -4rpx;
    background-color: #FF3B30;
    color: #fff;
    font-size: 20rpx;
    font-weight: 500;
    min-width: 32rpx;
    height: 32rpx;
    line-height: 32rpx;
    text-align: center;
    border-radius: 16rpx;
    padding: 0 8rpx;
    box-sizing: border-box;
    border: 2rpx solid #fff;
  }
}

.chat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-bottom: 1px solid rgba(0,0,0,0.03);
  padding: 24rpx 0;
}

.chat-item:last-child .chat-info {
  border-bottom: none;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.name-wrap {
  display: flex;
  align-items: center;
}

.nickname {
  font-size: 32rpx;
  color: $uni-text-color;
  font-weight: 500;
}

.official-tag {
  display: flex;
  align-items: center;
  background-color: rgba($uni-color-warning, 0.15);
  color: #E68A00;
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 8rpx;
  margin-left: 12rpx;
  font-weight: 600;
  
  .tag-icon {
    font-size: 22rpx;
    margin-right: 4rpx;
  }
}

.time {
  font-size: 24rpx;
  color: $uni-text-color-placeholder;
}

.chat-content {
  display: flex;
  align-items: center;
}

.msg-preview {
  font-size: 28rpx;
  color: $uni-text-color-grey;
  width: 480rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-text {
  color: $uni-text-color-regular;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400rpx;
}

.empty-text {
  color: $uni-text-color-placeholder;
  font-size: 28rpx;
}

/* 筛选面板样式 */
.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
}

.filter-panel {
  position: fixed;
  bottom: var(--window-bottom, 0);
  left: 0;
  right: 0;
  background-color: #fff;
  border-radius: 30rpx 30rpx 0 0;
  z-index: 101;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  padding-bottom: 20rpx;
  
  &.show {
    transform: translateY(0);
  }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  font-size: 32rpx;
  font-weight: bold;
  border-bottom: 1px solid $uni-border-color;
  
  .close-icon {
    font-size: 40rpx;
    color: $uni-text-color-grey;
  }
}

.filter-section {
  padding: 30rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: $uni-text-color;
    margin-bottom: 20rpx;
  }
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.filter-tag {
  padding: 12rpx 30rpx;
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-pill;
  font-size: 26rpx;
  color: $uni-text-color-regular;
  border: 1px solid transparent;
  
  &.active {
    background-color: rgba($uni-color-primary, 0.1);
    color: $uni-color-primary;
    border-color: $uni-color-primary;
  }
}

.panel-bottom {
  display: flex;
  padding: 30rpx;
  gap: 30rpx;
  border-top: 1px solid $uni-border-color;
  
  button {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    font-size: 30rpx;
    
    &::after {
      border: none;
    }
  }
  
  .reset-btn {
    background-color: $uni-bg-color-page;
    color: $uni-text-color-regular;
  }
  
  .confirm-btn {
    background: $uni-color-primary-gradient;
    color: #fff;
  }
}

/* 通知列表样式 */
.notification-list {
  flex: 1;
  padding: 24rpx 32rpx;
  box-sizing: border-box;
  width: 100%;
}

.noti-item {
  display: flex;
  background: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: $uni-shadow-base;
  position: relative;
}

.noti-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: $uni-border-radius-circle;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #fff;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.bg-blue { background: linear-gradient(135deg, #6B9DFF, #4081FF); }
.bg-gold { background: linear-gradient(135deg, #FFD700, #FFA500); }
.bg-primary { background: $uni-color-primary-gradient; }
.bg-pink { background: linear-gradient(135deg, #FF758C, #FF7EB3); }
.bg-red { background: linear-gradient(135deg, #FF6B6B, #FF4757); }

.noti-content { flex: 1; }

.noti-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.noti-title { font-size: 32rpx; font-weight: 600; color: $uni-text-color; }
.noti-time { font-size: 24rpx; color: $uni-text-color-placeholder; }
.noti-desc { font-size: 28rpx; color: $uni-text-color-regular; display: block; line-height: 1.5; }
</style>
