<template>
  <view class="discover-container">
    <!-- 顶部状态栏占位 -->
    <view class="status-bar-spacer" :style="{ height: statusBarHeight + 'px' }"></view>

    <scroll-view scroll-y class="scroll-view">
      
      <!-- 上半屏：在线用户推荐 (3D网络球形) -->
      <view class="online-recommend" v-if="onlineUsers.length > 0">
        <view class="section-title">
          <view class="title-left">
            <view class="title-main" style="margin-bottom: 0;">
              <text>同城在线</text>
              <view class="live-tag">
                <view class="live-dot"></view>
                <text>{{ onlineCount }}人在线</text>
              </view>
            </view>
          </view>
          <view class="refresh-btn" @click="shuffleOnlineUsers">
            <text class="ri-refresh-line refresh-icon" :class="{ 'is-spinning': isRefreshing }"></text>
            <text>换一批</text>
          </view>
        </view>
        
        <view class="online-carousel-container">
          <view class="ripple-bg-container">
            <!-- 核心点 -->
            <view class="ripple-core"></view>
            <!-- 扩散线条 -->
            <view class="ripple-line r-1"></view>
            <view class="ripple-line r-2"></view>
            <view class="ripple-line r-3"></view>
            <view class="ripple-line r-4"></view>
          </view>
          <scroll-view 
            scroll-x 
            class="online-scroll-view" 
            :show-scrollbar="false"
            :scroll-left="scrollLeft"
            scroll-with-animation="false"
            @touchstart="handleScrollTouchStart"
            @touchend="handleScrollTouchEnd"
            @scroll="handleScroll"
          >
            <view class="online-list" :style="{ width: onlineListWidth + 'rpx' }">
              <view 
                class="online-avatar-item" 
                v-for="(user, index) in onlineUsers" 
                :key="user.id"
                :style="getScatterStyle(index)"
                @click="goUserDetail(user)"
              >
                <view class="avatar-wrap">
                  <image class="avatar-img" :src="user.avatar" mode="aspectFill"></image>
                  <view class="online-status">
                    <view class="status-pulse"></view>
                  </view>
                </view>
                <view class="nickname-tag">
                  <text class="tag-name">{{ user.nickname }}</text>
                  <text class="tag-info" v-if="user.age && user.height">{{ user.age }}岁 · {{ user.height }}cm{{ user.education ? ' · ' + user.education : '' }}</text>
                  <text class="tag-info" v-else-if="user.age">{{ user.age }}岁</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
      <view class="online-recommend empty" v-else>
        <view class="section-title">
          <text>同城在线</text>
        </view>
        <view class="empty-online">
          <text>暂无在线用户</text>
        </view>
      </view>

      <view class="post-list">
        <view class="post-card" v-for="post in postList" :key="post.id">
          <!-- 用户信息区 -->
          <view class="post-header">
            <image class="avatar" :src="post.avatar" mode="aspectFill" @click="goUserDetail(post)"></image>
            <view class="user-info" @click="goUserDetail(post)">
              <view class="name-wrap">
                <text class="nickname">{{ post.nickname }}</text>
                <view class="vip-icon" v-if="post.isVip">
                  <text class="ri-vip-crown-fill"></text>
                </view>
              </view>
              <view class="meta">
                <template v-for="(item, index) in [
                  post.age ? post.age + '岁' : '',
                  post.height ? post.height + 'cm' : '',
                  post.location ? formatLocation(post.location) : '',
                  post.education ? post.education : '',
                  post.hometown ? '籍贯：' + formatHometown(post.hometown) : ''
                ].filter(Boolean)" :key="index">
                  <text class="dot" v-if="index > 0">·</text>
                  <text class="user-extra">{{ item }}</text>
                </template>
              </view>
            </view>
            <view class="right-actions">
              <view class="more-action" @click.stop="showMoreMenu(post)">
                <text class="ri-more-2-fill"></text>
              </view>
            </view>
          </view>

          <!-- 动态内容区 -->
          <view class="post-content">
            <text class="text">{{ post.content }}</text>
            
            <view class="video-container" v-if="post.videoUrl">
              <video 
                :src="post.videoUrl" 
                class="post-video" 
                object-fit="cover" 
                :poster="post.videoUrl + '?x-oss-process=video/snapshot,t_1000,f_jpg'"
                @click.stop=""
              ></video>
            </view>
            <view class="image-grid" :class="'grid-' + Math.min(post.images.length, 3)" v-else-if="post.images && post.images.length > 0">
              <image 
                class="grid-img" 
                v-for="(img, index) in post.images" 
                :key="index" 
                :src="img" 
                mode="aspectFill"
                @click.stop="previewImage(post.images, index)"
              ></image>
            </view>
            
            <view class="tags-line">
              <view class="topic-tag" v-if="post.topic">
                <text class="ri-hashtag tag-icon"></text>
                <text>{{ post.topic }}</text>
              </view>
            </view>
            
            <view class="time-location-line">
              <text class="time">{{ post.publishTime }}</text>
              <view class="location-tag" v-if="post.location">
                <text class="ri-map-pin-line tag-icon"></text>
                <text>{{ formatLocation(post.location) }}</text>
              </view>
            </view>

            <!-- 评论展示区 (内嵌在内容区底部) -->
            <view class="inline-comments" v-if="post.comments && post.comments.length > 0">
              <view class="inline-comment-item" v-for="(c, i) in (post.isExpanded ? post.comments : post.comments.slice(0, 2))" :key="i">
                <text class="c-name">{{ c.nickname }}：</text>
                <text class="c-content">{{ c.content }}</text>
              </view>
              <view class="more-comments" v-if="!post.isExpanded && (post.commentCount || post.comments.length) > 2" @click="post.isExpanded = true">
              <text>查看全部评论</text>
            </view>
            <view class="more-comments" v-if="post.isExpanded && (post.commentCount || post.comments.length) > 2" @click="post.isExpanded = false">
              <text>收起评论</text>
            </view>
            </view>
          </view>

          <!-- 互动操作区 -->
          <view class="post-actions">
            <view class="actions-right">
              <view class="action-item" @click="handleLike(post)" :class="{ active: post.isLiked }">
                <text class="icon" :class="post.isLiked ? 'ri-heart-3-fill liked' : 'ri-heart-3-line'"></text>
                <text class="count">{{ post.likeCount || '赞' }}</text>
              </view>
              <view class="action-item" @click="openCommentInput(post)">
                <text class="icon ri-message-3-line"></text>
                <text class="count">{{ post.commentCount || '评论' }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 底部提示 -->
        <view class="bottom-tips">
          <text>没有更多动态了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮发布按钮 -->
    <view class="publish-actions">
      <view class="action-item main-publish" @click="handlePublish">
        <text class="ri-add-line publish-icon"></text>
      </view>
    </view>

    <!-- 评论输入弹窗 -->
    <view class="comment-mask" v-if="showCommentInput" @click="showCommentInput = false"></view>
    <view class="comment-input-panel" :class="{ 'show': showCommentInput }">
      <view class="comment-input-bar">
        <input class="comment-input" :focus="showCommentInput" v-model="commentText" placeholder="写评论..." confirm-type="send" @confirm="submitComment" />
        <button class="comment-send-btn" :disabled="!commentText" @click="submitComment">发送</button>
      </view>
    </view>
      <!-- 分享自定义弹窗 -->
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

    <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { request } from '../../utils/request'
import { onShow, onHide, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getMockPhotos, getRandomAvatar, DEFAULT_AVATAR } from '../../utils/mockData'
import { checkAuth, requireLogin, globalAuthGuard } from '../../utils/auth'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
const postList = ref<any[]>([])
const onlineCount = ref(0)
const scrollLeft = ref(0)
let autoScrollTimer: any = null
let isTouching = false
let currentActualScroll = 0
const isRefreshing = ref(false)

const formatLocation = (loc: string) => {
  if (!loc) return ''
  const parts = loc.split('·')
  return parts.length > 1 ? parts[parts.length - 1] : loc
}

const formatHometown = (hometown: string) => {
  if (!hometown) return ''
  const provinces = ['黑龙江', '内蒙古', '北京', '天津', '上海', '重庆', '河北', '山西', '辽宁', '吉林', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南', '广东', '海南', '四川', '贵州', '云南', '陕西', '甘肃', '青海', '台湾', '广西', '西藏', '宁夏', '新疆', '香港', '澳门']
  let city = hometown
  for (const p of provinces) {
    if (city.startsWith(p)) {
      city = city.substring(p.length)
      if (!city) return hometown
      break
    }
  }
  return city
}

const onlineListWidth = computed(() => {
  const cols = Math.ceil(onlineUsers.value.length / 4)
  // 进一步增加基础列宽，确保拉开横向间距后有足够的滚动空间
  return cols * 320 + 400 
})

const startAutoScroll = () => {
  stopAutoScroll()
  autoScrollTimer = setInterval(() => {
    if (!isTouching) {
      currentActualScroll += 1 
      const maxScroll = (onlineListWidth.value * 0.7) 
      if (currentActualScroll > maxScroll) {
        currentActualScroll = 0
      }
      scrollLeft.value = currentActualScroll
    }
  }, 40)
}

const stopAutoScroll = () => {
  if (autoScrollTimer) {
    clearInterval(autoScrollTimer)
    autoScrollTimer = null
  }
}

const handleScrollTouchStart = () => {
  isTouching = true
  stopAutoScroll()
}

const handleScrollTouchEnd = () => {
  isTouching = false
  setTimeout(() => {
    if (!isTouching) {
      scrollLeft.value = currentActualScroll
      startAutoScroll()
    }
  }, 1000)
}

const handleScroll = (e: any) => {
  currentActualScroll = e.detail.scrollLeft
  if (!isTouching && !autoScrollTimer) {
     scrollLeft.value = currentActualScroll
  }
}

const getScatterStyle = (index: number) => {
  // 分布式排版逻辑：4行错位分布
  const row = index % 4
  const col = Math.floor(index / 4)
  
  // 显著拉大列间距，避免横向挤压
  const baseLeft = col * 320 + 60
  // 每行设定固定的 top，4行紧凑穿插以适应360rpx的容器高度
  const rowTops = [10, 90, 170, 250]
  const baseTop = rowTops[row]
  
  // 偶数行横向错位，拉开错位距离
  const rowOffsetLeft = (row % 2 === 1) ? 160 : 0
  
  // 微弱的随机偏移，打破死板，但控制在安全范围内避免重叠
  const offsetLeft = Math.sin(index * 13.5) * 20
  const offsetTop = Math.cos(index * 7.1) * 10
  
  // 动效参数
  const duration = 3 + (index % 4)
  const delay = (index % 5) * 0.5
  
  return `position: absolute; left: ${baseLeft + rowOffsetLeft + offsetLeft}rpx; top: ${baseTop + offsetTop}rpx; animation: avatar-float ${duration}s ease-in-out ${delay}s infinite alternate;`
}

const baseOnlineUsers = [
  { id: 1, nickname: '可爱小猫', distance: '1.2km', avatar: getRandomAvatar(1, 'female'), tags: ['爱宠物'], age: 22, height: 165, education: '本科', school: '南京师范大学', gender: 'female' },
  { id: 2, nickname: '健身达人', distance: '2.5km', avatar: getRandomAvatar(2, 'female'), tags: ['爱健身'], age: 25, height: 180, education: '硕士', school: '北京体育大学', gender: 'female' },
  { id: 3, nickname: '旅行家', distance: '3.0km', avatar: getRandomAvatar(3, 'female'), tags: ['爱旅行'], age: 24, height: 168, education: '本科', school: '厦门大学', gender: 'female' },
  { id: 4, nickname: '极客少女', distance: '5.1km', avatar: getRandomAvatar(4, 'female'), tags: ['极客'], age: 28, height: 175, education: '硕士', school: '电子科技大学', gender: 'female' },
  { id: 5, nickname: '美食家', distance: '同城', avatar: getRandomAvatar(5, 'female'), tags: ['吃货'], age: 26, height: 160, education: '本科', school: '四川大学', gender: 'female' },
  { id: 6, nickname: '星空漫步', distance: '1.5km', avatar: getRandomAvatar(104, 'female'), tags: ['天文'], age: 27, height: 178, education: '本科', school: '北京航空航天大学', gender: 'female' },
  { id: 7, nickname: '晨跑爱好者', distance: '4.2km', avatar: getRandomAvatar(107, 'female'), tags: ['晨跑'], age: 23, height: 162, education: '本科', school: '南京大学', gender: 'female' },
  { id: 8, nickname: '文艺青年', distance: '6.5km', avatar: getRandomAvatar(108, 'female'), tags: ['吉他'], age: 29, height: 176, education: '本科', school: '中央音乐学院', gender: 'female' },
  { id: 9, nickname: '独立摄影师', distance: '9.0km', avatar: getRandomAvatar(109, 'female'), tags: ['摄影'], age: 25, height: 166, education: '本科', school: '中国美术学院', gender: 'female' },
  { id: 10, nickname: '金融达人', distance: '2.1km', avatar: getRandomAvatar(110, 'female'), tags: ['理财'], age: 30, height: 182, education: '硕士', school: '上海财经大学', gender: 'female' },
  { id: 11, nickname: '滑雪爱好者', distance: '10km', avatar: getRandomAvatar(111, 'female'), tags: ['滑雪'], age: 26, height: 180, education: '本科', school: '哈尔滨工业大学', gender: 'female' },
  { id: 12, nickname: '音乐剧控', distance: '3.5km', avatar: getRandomAvatar(112, 'female'), tags: ['音乐剧'], age: 24, height: 165, education: '本科', school: '上海戏剧学院', gender: 'female' },
  { id: 13, nickname: '户外探险', distance: '15km', avatar: getRandomAvatar(113, 'female'), tags: ['徒步'], age: 31, height: 178, education: '本科', school: '中国地质大学', gender: 'female' },
  { id: 14, nickname: '瑜伽导师', distance: '2.0km', avatar: getRandomAvatar(114, 'female'), tags: ['瑜伽'], age: 27, height: 170, education: '本科', school: '北京体育大学', gender: 'female' },
  { id: 15, nickname: '电竞选手', distance: '4.5km', avatar: getRandomAvatar(115, 'female'), tags: ['游戏'], age: 22, height: 175, education: '大专', school: '广州电竞学院', gender: 'female' },
  { id: 16, nickname: '喵星人', distance: '0.8km', avatar: getRandomAvatar(116, 'female'), tags: ['猫控'], age: 21, height: 163, education: '本科', school: '华南农业大学', gender: 'female' },
  { id: 17, nickname: '书香门第', distance: '1.8km', avatar: getRandomAvatar(117, 'female'), tags: ['阅读'], age: 26, height: 170, education: '硕士', school: '北京师范大学', gender: 'female' },
  { id: 18, nickname: '钢琴公主', distance: '2.2km', avatar: getRandomAvatar(118, 'female'), tags: ['钢琴'], age: 23, height: 167, education: '本科', school: '星海音乐学院', gender: 'female' },
  { id: 19, nickname: '职场精英', distance: '5.5km', avatar: getRandomAvatar(119, 'female'), tags: ['事业'], age: 28, height: 172, education: '本科', school: '对外经济贸易大学', gender: 'female' },
  { id: 20, nickname: '林间小鹿', distance: '3.1km', avatar: getRandomAvatar(120, 'female'), tags: ['自然'], age: 22, height: 161, education: '本科', school: '北京林业大学', gender: 'female' },
  { id: 21, nickname: '花艺师', distance: '0.5km', avatar: getRandomAvatar(121, 'female'), tags: ['插花'], age: 25, height: 163, education: '本科', school: '云南大学', gender: 'female' },
  { id: 22, nickname: '街舞少女', distance: '4.0km', avatar: getRandomAvatar(122, 'female'), tags: ['街舞'], age: 20, height: 166, education: '大专', school: '北京现代音乐研修学院', gender: 'female' },
  { id: 23, nickname: '咖啡达人', distance: '2.8km', avatar: getRandomAvatar(123, 'female'), tags: ['咖啡'], age: 27, height: 165, education: '本科', school: '江南大学', gender: 'female' },
  { id: 24, nickname: '滑板酷女孩', distance: '1.2km', avatar: getRandomAvatar(124, 'female'), tags: ['滑板'], age: 21, height: 168, education: '本科', school: '深圳大学', gender: 'female' },
  { id: 25, nickname: '温柔学姐', distance: '0.9km', avatar: getRandomAvatar(125, 'female'), tags: ['治愈'], age: 24, height: 162, education: '本科', school: '华中师范大学', gender: 'female' },
  { id: 26, nickname: '动漫迷', distance: '6.0km', avatar: getRandomAvatar(126, 'female'), tags: ['二次元'], age: 19, height: 158, education: '大专', school: '上海工艺美术职业学院', gender: 'female' },
  { id: 27, nickname: '汉服同好', distance: '3.4km', avatar: getRandomAvatar(127, 'female'), tags: ['古风'], age: 26, height: 165, education: '本科', school: '西安美术学院', gender: 'female' },
  { id: 28, nickname: '登山勇士', distance: '12km', avatar: getRandomAvatar(128, 'female'), tags: ['登山'], age: 30, height: 170, education: '本科', school: '中国地质大学', gender: 'female' },
  { id: 29, nickname: '萌系博主', distance: '1.5km', avatar: getRandomAvatar(129, 'female'), tags: ['分享'], age: 23, height: 160, education: '本科', school: '暨南大学', gender: 'female' },
  { id: 30, nickname: '法律之光', distance: '2.6km', avatar: getRandomAvatar(130, 'female'), tags: ['知性'], age: 29, height: 171, education: '硕士', school: '中国政法大学', gender: 'female' },
  { id: 31, nickname: '甜品屋主', distance: '0.3km', avatar: getRandomAvatar(131, 'female'), tags: ['烘焙'], age: 25, height: 162, education: '大专', school: '扬州大学', gender: 'female' },
  { id: 32, nickname: '古琴韵', distance: '4.8km', avatar: getRandomAvatar(132, 'female'), tags: ['民乐'], age: 27, height: 166, education: '本科', school: '天津音乐学院', gender: 'female' },
  { id: 33, nickname: '马术名媛', distance: '15km', avatar: getRandomAvatar(133, 'female'), tags: ['马术'], age: 28, height: 175, education: '硕士', school: '英国皇家马术学院', gender: 'female' },
  { id: 34, nickname: '画室女孩', distance: '1.1km', avatar: getRandomAvatar(134, 'female'), tags: ['绘画'], age: 24, height: 164, education: '本科', school: '广州美术学院', gender: 'female' },
  { id: 35, nickname: '网球达人', distance: '3.7km', avatar: getRandomAvatar(135, 'female'), tags: ['运动'], age: 26, height: 173, education: '本科', school: '北京体育大学', gender: 'female' },
  { id: 36, nickname: '茶艺仙子', distance: '2.5km', avatar: getRandomAvatar(136, 'female'), tags: ['品茶'], age: 25, height: 165, education: '本科', school: '福建农林大学', gender: 'female' }
]

// 额外生成120个活跃用户，统一设置为女性，满足“男性用户看女性”的需求
const extraOnlineUsers = Array.from({ length: 120 }).map((_, i) => {
  const educations = ['大专', '本科', '硕士', '博士']
  const schools = ['清华大学', '北京大学', '复旦大学', '上海交通大学', '浙江大学', '南京大学', '武汉大学', '中山大学', '四川大学', '南开大学']
  const gender = 'female' // 统一女性
  return {
    id: 200 + i,
    nickname: `活跃女生${i + 1}`,
    distance: `${(Math.random() * 8).toFixed(1)}km`,
    avatar: getRandomAvatar(200 + i, gender),
    tags: ['活跃'],
    age: 18 + Math.floor(Math.random() * 12),
    height: 155 + Math.floor(Math.random() * 20),
    education: educations[Math.floor(Math.random() * educations.length)],
    school: schools[Math.floor(Math.random() * schools.length)],
    gender: gender
  }
})

const onlineUsers = ref<any[]>([...baseOnlineUsers, ...extraOnlineUsers])

const displayUsers = computed(() => {
  return onlineUsers.value
})

const fetchOnlineUsers = async () => {
  try {
    const res: any = await request({ url: '/discover/online-users', method: 'GET' })
    
    // 获取我的资料以确定异性（仅在登录时获取，避免未登录抛错）
    const loggedIn = !!uni.getStorageSync('token')
    let myGender = 1
    let myCity = '北京'
    if (loggedIn) {
      const myProfile: any = await request({ url: '/profile/info', method: 'GET' }).catch(() => ({ gender: 1 }))
      myGender = myProfile?.gender || 1
      myCity = myProfile?.location || '北京'
    }
    const targetGender = myGender === 1 ? 2 : 1

    if (res && res.list && res.list.length > 0) {
      // 推荐机制：仅展示异性，优先同城用户
      let apiUsers = res.list.filter((u: any) => u.gender === targetGender || u.gender === (targetGender === 1 ? 'male' : 'female'))
      
      apiUsers = apiUsers.map((u: any) => {
        if (!u.height) u.height = 158 + Math.floor(Math.random() * 12)
        if (!u.age) u.age = 20 + Math.floor(Math.random() * 10)
        return u
      })
      
      // 排序：同城优先
      apiUsers.sort((a: any, b: any) => {
        const aIsLocal = (a.location || a.distance || '').includes(myCity) ? 1 : 0
        const bIsLocal = (b.location || b.distance || '').includes(myCity) ? 1 : 0
        return bIsLocal - aIsLocal
      })
      
      // 保持基数庞大，将API返回的数据与Mock数据合并
      const existingIds = new Set(apiUsers.map((u: any) => u.id || u.userId))
      const targetGenderStr = targetGender === 1 ? 'male' : 'female'
      const remainingMockUsers = [...baseOnlineUsers, ...extraOnlineUsers]
        .filter(u => !existingIds.has(u.id))
        .map(u => ({
          ...u,
          gender: targetGenderStr,
          avatar: u.gender !== targetGenderStr ? getRandomAvatar(u.id, targetGenderStr) : u.avatar
        }))
      
      onlineUsers.value = [...apiUsers, ...remainingMockUsers].slice(0, 100)
      onlineCount.value = Math.max(res.onlineCount || 0, onlineUsers.value.length)
      
    } else {
      // 如果 API 失败，Mock 数据也保证数量并转换为异性
      const targetGenderStr = targetGender === 1 ? 'male' : 'female'
      onlineUsers.value = [...baseOnlineUsers, ...extraOnlineUsers].map(u => ({
        ...u,
        gender: targetGenderStr,
        avatar: u.gender !== targetGenderStr ? getRandomAvatar(u.id, targetGenderStr) : u.avatar
      })).slice(0, 100)
      onlineCount.value = onlineUsers.value.length
    }
  } catch (e) {
    console.error('fetchOnlineUsers error:', e)
    onlineCount.value = onlineUsers.value.length
  }
}

const loadMoreOnlineUsers = async () => {
  if (onlineUsers.value.length >= 200) return // 增加上限到200个
  // Simulate loading more users
  const startIndex = onlineUsers.value.length + 100
  const moreUsers = Array.from({ length: 20 }).map((_, i) => {
    const educations = ['专科', '本科', '硕士', '博士']
    const schools = ['华中科技大学', '西安交通大学', '哈尔滨工业大学', '吉林大学', '山东大学', '中南大学', '湖南大学', '华南理工大学', '重庆大学', '兰州大学']
    return {
      id: startIndex + i,
      nickname: `新晋用户${startIndex + i}`,
      distance: `${(Math.random() * 5).toFixed(1)}km`,
      avatar: getRandomAvatar(startIndex + i, 'female'),
      tags: ['活跃'],
      age: 20 + Math.floor(Math.random() * 10),
      height: 160 + Math.floor(Math.random() * 20),
      education: educations[Math.floor(Math.random() * educations.length)],
      school: schools[Math.floor(Math.random() * schools.length)],
      gender: 'female'
    }
  })
  onlineUsers.value.push(...moreUsers)
}

const shuffleOnlineUsers = () => {
  if (!requireLogin('更新在线会员')) return
  if (isRefreshing.value) return
  isRefreshing.value = true
  uni.showLoading({ title: '正在寻找缘分...', mask: true })
  
  setTimeout(() => {
    // 随机打乱当前列表，营造“更新”感
    onlineUsers.value = [...onlineUsers.value].sort(() => Math.random() - 0.5)
    uni.hideLoading()
    isRefreshing.value = false
    uni.showToast({ title: '已更新在线会员', icon: 'none' })
  }, 800)
}

const goUserDetail = (user: any) => {
  if (!requireLogin('查看主页')) return
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.id || user.userId}` })
}

const getMockPosts = () => {
  return [
    {
      id: 1,
      userId: 101,
      nickname: '阳光大男孩',
      avatar: getRandomAvatar(101, 'male'),
      isVip: true,
      publishTime: '10分钟前',
      location: '北京南站',
      hometown: '山东济南',
      age: 26,
      education: '本科',
      height: 180,
      content: '今天天气真不错，适合出去跑跑步！有没有一起的小伙伴？🏃‍♂️🏃‍♂️',
      images: getMockPhotos(2),
      topic: '运动打卡',
      isLiked: false,
      isFollowing: false,
      likeCount: 12,
      commentCount: 5,
      comments: [
        { nickname: '运动达人', content: '加油！' },
        { nickname: '健身女孩', content: '我也想跑步' },
        { nickname: '懒虫', content: '太辛苦了' }
      ]
    },
    {
      id: 2,
      userId: 102,
      nickname: '温柔可人',
      avatar: getRandomAvatar(102, 'female'),
      isVip: false,
      publishTime: '1小时前',
      location: '上海迪士尼乐园',
      hometown: '江苏苏州',
      age: 24,
      education: '硕士',
      height: 165,
      content: '新做了一个蛋糕，味道超级好，感觉自己可以开店啦 🍰✨ 记录美好的周末午后时光。',
      images: [],
      videoUrl: 'https://media.w3.org/2010/05/sintel/trailer.mp4',
      topic: '美食日常',
      isLiked: true,
      isFollowing: true,
      likeCount: 45,
      commentCount: 18,
      comments: [
        { nickname: '吃货一号', content: '看起来好好吃！' },
        { nickname: '甜品控', content: '求教程！' },
        { nickname: '烘焙小白', content: '太厉害了！' }
      ]
    },
    {
      id: 3,
      userId: 103,
      nickname: '代码诗人',
      avatar: getRandomAvatar(103, 'male'),
      isVip: true,
      publishTime: '2小时前',
      location: '西湖畔星巴克',
      hometown: '浙江杭州',
      age: 28,
      education: '本科',
      height: 175,
      content: '周末加班改bug，终于上线了！给自己加个鸡腿。',
      images: getMockPhotos(2),
      topic: '程序猿日常',
      isLiked: false,
      isFollowing: false,
      likeCount: 8,
      commentCount: 5,
      comments: [
        { nickname: '程序员同事', content: '辛苦了' },
        { nickname: '产品经理', content: '辛苦了，辛苦了' }
      ]
    },
    {
      id: 4,
      userId: 104,
      nickname: '爱猫人士',
      avatar: getRandomAvatar(104, 'female'),
      isVip: false,
      publishTime: '3小时前',
      location: '广州塔',
      hometown: '广东广州',
      age: 22,
      education: '大专',
      height: 160,
      content: '我家主子今天特别黏人，给大家看看这盛世美颜。',
      images: getMockPhotos(4),
      topic: '萌宠时刻',
      isLiked: true,
      isFollowing: false,
      likeCount: 120,
      commentCount: 34,
      comments: [
        { nickname: '猫奴甲', content: '好可爱啊！' },
        { nickname: '猫奴乙', content: '羡慕！' },
        { nickname: '云吸猫', content: '好漂亮的猫' },
        { nickname: '宠物医生', content: '照顾得很好哦' }
      ]
    },
    {
      id: 5,
      userId: 105,
      nickname: '健身狂魔',
      avatar: getRandomAvatar(105, 'male'),
      isVip: true,
      publishTime: '5小时前',
      location: '深圳市民中心',
      hometown: '湖南长沙',
      age: 29,
      education: '本科',
      height: 185,
      content: '今天的卧推突破了个人记录！继续努力！💪',
      images: getMockPhotos(1),
      topic: '运动打卡',
      isLiked: false,
      isFollowing: false,
      likeCount: 65,
      commentCount: 12,
      comments: [
        { nickname: '健身爱好者', content: '太厉害了！' },
        { nickname: '运动达人', content: '向你学习！' },
        { nickname: '健身房常客', content: '恭喜恭喜！' },
        { nickname: '增肌小能手', content: '厉害厉害！' }
      ]
    }
  ]
}

const fetchPosts = async () => {
  try {
    const res = await request({ url: '/post/list', method: 'GET' })
    if (res && res.length > 0) {
      postList.value = res.map((item: any) => ({
        ...item,
        nickname: item.nickname || '神秘鸭鸭',
        avatar: item.avatar || getRandomAvatar(item.userId, 'female'),
        images: typeof item.images === 'string' ? JSON.parse(item.images) : (item.images || []),
        publishTime: item.createdAt ? new Date(item.createdAt).toLocaleString() : '4小时前',
        commentCount: item.commentCount || 2,
        comments: item.comments || [
          { nickname: '热心网友', content: '很棒的动态！' },
          { nickname: '路人甲', content: '支持一下~' }
        ],
        isFollowing: item.isFollowing || false
      }))
    } else {
      postList.value = getMockPosts()
    }
  } catch (e) {
    postList.value = getMockPosts()
  }
}

const handleLike = async (post: any) => {
  if (!(await checkAuth('点赞'))) return

  post.isLiked = !post.isLiked
  post.likeCount += post.isLiked ? 1 : -1
  try {
    await request({
      url: `/post/like?postId=${post.id}`,
      method: 'POST'
    })
  } catch (e) {
    post.isLiked = !post.isLiked
    post.likeCount += post.isLiked ? 1 : -1
  }
}

const toggleFollow = async (post: any) => {
  if (!requireLogin('关注')) return
  post.isFollowing = !post.isFollowing
  try {
    const url = post.isFollowing ? `/relation/follow?targetId=${post.userId}` : `/relation/unfollow?targetId=${post.userId}`
    await request({ url, method: 'POST' })
    uni.showToast({ title: post.isFollowing ? '已关注' : '已取消关注', icon: 'none' })
  } catch (e) {
    post.isFollowing = !post.isFollowing
  }
}

const handlePublish = async () => {
  // 发布动态只需要基础注册和完善资料，不需要实名认证
  if (!(await checkAuth('发布动态', false))) return
  uni.navigateTo({ url: '/pages/post-publish/index' })
}

const previewImage = (urls: string[], current: any) => {
  uni.previewImage({ urls, current: Number(current) })
}

const showCommentInput = ref(false)
const currentCommentPostId = ref(0)
const commentText = ref('')

const openCommentInput = async (post: any) => {
  if (!(await checkAuth('评论'))) return
  currentCommentPostId.value = post.id
  showCommentInput.value = true
}

const submitComment = () => {
  if (!commentText.value.trim()) return
  const post = postList.value.find(p => p.id === currentCommentPostId.value)
  if (post) {
    if (!post.comments) post.comments = []
    post.comments.unshift({
      id: Date.now(),
      nickname: '我',
      avatar: getRandomAvatar(999, 'female'),
      content: commentText.value,
      time: '刚刚'
    })
    post.commentCount = (post.commentCount || 0) + 1
  }
  commentText.value = ''
  showCommentInput.value = false
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
  const shareUrl = window.location.href
  
  // #ifdef H5
  if (type === 'wechat') {
    uni.setClipboardData({
      data: shareUrl,
      success: () => uni.showToast({ title: '链接已复制，可转发给好友', icon: 'none' })
    })
  } else if (type === 'moments') {
    uni.setClipboardData({
      data: shareUrl,
      success: () => uni.showToast({ title: '链接已复制，可分享到朋友圈', icon: 'none' })
    })
  }
  // #endif
  
  // #ifndef H5
  if (type === 'wechat') {
    uni.showToast({ title: '请点击右上角...发送给朋友', icon: 'none' })
  } else if (type === 'moments') {
    // #ifdef MP-WEIXIN
    uni.showToast({ title: '请点击右上角...分享到朋友圈', icon: 'none' })
    // #endif
    // #ifndef MP-WEIXIN
    uni.showToast({ title: '准备分享到朋友圈（需接入SDK）', icon: 'none' })
    // #endif
  }
  // #endif
}

const showMoreMenu = (post: any) => {
  const followAction = post.isFollowing ? '取消关注' : '关注'
  uni.showActionSheet({
    itemList: [followAction, '分享', '屏蔽', '举报'],
    success: (res) => {
      if (res.tapIndex === 0) {
        toggleFollow(post)
      } else if (res.tapIndex === 1) {
        handleSharePost(post)
      } else if (res.tapIndex === 2) {
        uni.showToast({ title: '已屏蔽该用户', icon: 'none' })
      } else if (res.tapIndex === 3) {
        uni.navigateTo({
          url: `/pages/report/index?targetId=${post.userId}&targetName=${post.nickname}&type=其他举报`
        })
      }
    }
  })
}

onShow(() => {
  if (!globalAuthGuard()) return
  fetchOnlineUsers()
  fetchPosts()
  startAutoScroll()
})

onHide(() => {
  stopAutoScroll()
})

onUnmounted(() => {
  stopAutoScroll()
})

onShareAppMessage(() => {
  if (currentSharePost.value) {
    return {
      title: `${currentSharePost.value.nickname}的动态`,
      path: `/pages/discover/index`, // Actually it should point to post detail if it exists, for now discover
      imageUrl: currentSharePost.value.images && currentSharePost.value.images.length > 0 ? currentSharePost.value.images[0] : ''
    }
  }
  return {
    title: '红小鸭相亲交友 - 发现',
    path: '/pages/discover/index'
  }
})

onShareTimeline(() => {
  if (currentSharePost.value) {
    return {
      title: `${currentSharePost.value.nickname}的动态`,
      query: ``,
      imageUrl: currentSharePost.value.images && currentSharePost.value.images.length > 0 ? currentSharePost.value.images[0] : ''
    }
  }
  return {
    title: '红小鸭相亲交友 - 发现',
    query: ''
  }
})
</script>

<style lang="scss" scoped>
.discover-container {
  background-color: $uni-bg-color-grey;
  height: calc(100vh - var(--window-bottom));
  position: relative;
  display: flex;
  flex-direction: column;
}

.status-bar-spacer {
  background-color: $uni-bg-color;
  z-index: 99;
}

.scroll-view {
  flex: 1;
  height: 0;
}

.online-recommend {
  background-color: $uni-bg-color;
  padding: 20rpx 0 30rpx 0;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.section-title {
  padding: 0 40rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.title-main {
  display: flex;
  align-items: center;
  margin-bottom: 4rpx;
}

.section-title > text:first-child,
.title-main > text:first-child {
  font-size: 34rpx;
  font-weight: bold;
  color: $uni-text-color;
}

.live-tag {
  display: flex;
  align-items: center;
  background-color: $uni-color-primary-light;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-left: 16rpx;
}

.live-dot {
  width: 10rpx;
  height: 10rpx;
  background-color: $uni-color-primary;
  border-radius: 50%;
  margin-right: 8rpx;
  animation: live-blink 1.2s infinite;
}

.live-tag text {
  font-size: 18rpx;
  color: $uni-color-primary;
  font-weight: bold;
  letter-spacing: 1rpx;
}

@keyframes live-blink {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}

.subtitle {
  font-size: 24rpx;
  color: $uni-text-color-grey;
}

.refresh-icon.is-spinning {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.online-carousel-container {
  width: 100%;
  height: 360rpx;
  position: relative;
  overflow: hidden;
  /* background: linear-gradient(to bottom, rgba(160, 160, 160, 0.08) 0%, transparent 100%); */
}

.ripple-bg-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 核心呼吸点 */
.ripple-core {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 14rpx;
  height: 14rpx;
  transform: translate(-50%, -50%);
  background-color: rgba(160, 160, 160, 0.45);
  border-radius: 50%;
  box-shadow: 0 0 24rpx rgba(160, 160, 160, 0.3);
  animation: core-pulse 2s ease-in-out infinite alternate;
}

@keyframes core-pulse {
  0% { transform: translate(-50%, -50%) scale(0.8); opacity: 0.25; }
  100% { transform: translate(-50%, -50%) scale(1.4); opacity: 0.5; }
}

/* 扩散线条水波纹 */
.ripple-line {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  border: 3rpx solid rgba(180, 180, 180, 0.3);
  transform: translate(-50%, -50%);
  opacity: 0;
}

.r-1 { animation: ripple-expand 4.5s linear infinite; }
.r-2 { animation: ripple-expand 4.5s linear infinite 1.125s; }
.r-3 { animation: ripple-expand 4.5s linear infinite 2.25s; }
.r-4 { animation: ripple-expand 4.5s linear infinite 3.375s; }

@keyframes ripple-expand {
  0% {
    width: 20rpx;
    height: 20rpx;
    opacity: 0.4;
    border-width: 4rpx;
  }
  100% {
    width: 800rpx;
    height: 800rpx;
    opacity: 0;
    border-width: 1.5rpx;
  }
}

.online-carousel-container::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100rpx;
  height: 100%;
  background: linear-gradient(to left, $uni-bg-color 0%, transparent 100%);
  pointer-events: none;
  z-index: 5;
}

.online-scroll-view {
  width: 100%;
  height: 100%;
  white-space: nowrap;
  position: relative;
  z-index: 10; /* 提高层级，确保在遮罩和背景动画之上，能够接收触摸事件 */
  /* 确保内部能够滚动 */
  overflow-x: scroll;
  -webkit-overflow-scrolling: touch;
}

.online-list {
  position: relative;
  height: 100%;
  /* 确保在绝对定位子元素下，容器宽度能够被识别以支持滚动 */
  min-width: 100%;
  padding-bottom: 20rpx; /* 防止底部被截断影响滑动 */
}

.online-avatar-item {
  display: inline-flex; /* 修改为 inline-flex 辅助解决部分滚动问题，尽管仍使用 absolute */
  flex-direction: column;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 3;
}

.online-avatar-item:active {
  transform: scale(1.1) !important;
  z-index: 10;
}

.avatar-wrap {
  position: relative;
  width: 48rpx;
  height: 48rpx;
  margin-bottom: 8rpx;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2rpx solid #fff;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.online-status {
  position: absolute;
  right: -2rpx;
  bottom: -2rpx;
  width: 12rpx;
  height: 12rpx;
  background-color: $uni-color-success;
  border: 2rpx solid #fff;
  border-radius: 50%;
  z-index: 2;
}

.status-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: $uni-color-success;
  border-radius: 50%;
  opacity: 0.6;
  animation: pulse 1.5s infinite;
}

.nickname-tag {
  white-space: nowrap;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 4rpx;
}

.tag-name {
  font-size: 18rpx;
  color: $uni-text-color;
  font-weight: 600;
  line-height: 1.2;
  text-shadow: 0 2rpx 4rpx rgba(255,255,255,0.8);
}

.tag-info {
  font-size: 18rpx;
  color: $uni-text-color-grey;
  margin-top: 4rpx;
  line-height: 1.2;
  text-shadow: 0 2rpx 4rpx rgba(255,255,255,0.8);
}

@keyframes avatar-float {
  0% { transform: translate(0, 0); }
  33% { transform: translate(6rpx, -10rpx); }
  66% { transform: translate(-4rpx, 6rpx); }
  100% { transform: translate(0, 0); }
}

.scroll-spacer {
  display: none;
}

.dot-pulse {
  display: none;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(2.5); opacity: 0; }
}

.refresh-btn {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: $uni-color-primary;
  background-color: $uni-color-primary-light;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  transition: all 0.2s;
}

.refresh-btn:active {
  transform: scale(0.95);
  opacity: 0.8;
}

.refresh-btn text:first-child {
  margin-right: 6rpx;
  font-size: 28rpx;
}

.post-card {
  background-color: $uni-bg-color;
  padding: 30rpx 24rpx;
  margin-bottom: 20rpx;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: $uni-border-color;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.more-action {
  display: flex;
  align-items: center;
  padding: 10rpx;
  font-size: 40rpx;
  color: #999;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.follow-btn {
  padding: 12rpx 24rpx;
  background-color: #6A61F8;
  color: #ffffff;
  font-size: 24rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
  transition: all 0.2s;
  white-space: nowrap;
}

.follow-btn.followed {
  background-color: #f0f0f0;
  color: #999;
}

.follow-btn:active {
  transform: scale(0.95);
  opacity: 0.8;
}

.add-icon {
  font-size: 28rpx;
  line-height: 1;
}

.name-wrap {
  display: flex;
  align-items: center;
  margin-bottom: 6rpx;
}

.nickname {
  font-size: 30rpx;
  font-weight: bold;
  color: $uni-text-color;
  margin-right: 12rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.vip-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32rpx;
  height: 32rpx;
  background: linear-gradient(135deg, #4A4A4A 0%, #1A1A1A 100%);
  border-radius: 50%;
  flex-shrink: 0;
}

.vip-icon text {
  font-size: 20rpx;
  color: #F0D09B;
}

.meta {
  font-size: 22rpx;
  color: $uni-text-color-placeholder;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  line-height: 1.4;
}

.dot {
  margin: 0 8rpx;
}

.post-content {
  margin-bottom: 20rpx;
  padding-left: 100rpx; /* Align with text (80rpx avatar + 20rpx margin) */
}

.text {
  font-size: 30rpx;
  color: $uni-text-color;
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: block;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.video-container {
  margin-bottom: 24rpx;
  width: 100%;
  max-width: 500rpx;
  height: 400rpx;
  border-radius: $uni-border-radius-base;
  overflow: hidden;
  background-color: $uni-bg-color-page;
}

.post-video {
  width: 100%;
  height: 100%;
}

.grid-img {
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-base;
}

.grid-1 .grid-img {
  width: 400rpx;
  height: 400rpx;
  border-radius: $uni-border-radius-base;
}

.grid-2 .grid-img, .grid-3 .grid-img {
  width: 180rpx;
  height: 180rpx;
}

.tags-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.topic-tag {
  display: inline-flex;
  align-items: center;
  background-color: $uni-color-primary-light;
  color: $uni-color-primary;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: $uni-border-radius-lg;
  font-weight: 500;
}

.location-tag {
  display: inline-flex;
  align-items: center;
  background-color: #f0f0f0;
  color: #999;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  font-weight: 500;
}

.location-tag .tag-icon {
  margin-right: 6rpx;
  font-size: 26rpx;
}

.tag-icon {
  margin-right: 6rpx;
  font-size: 26rpx;
}

.time-location-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.time-location-line .time {
  font-size: 24rpx;
  color: $uni-text-color-placeholder;
}

.post-actions {
  display: flex;
  justify-content: flex-end;
  padding-left: 100rpx;
}

.actions-right {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.action-item {
  display: flex;
  align-items: center;
  color: $uni-text-color-grey;
  transition: all 0.2s;
}

.action-item.active {
  color: $uni-color-primary;
}

.icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.liked {
  color: $uni-color-primary;
}

.count {
  font-size: 26rpx;
  font-weight: 500;
}

.inline-comments {
  margin-top: 12rpx;
  background-color: #f8f8f8;
  padding: 16rpx 20rpx;
  border-radius: 8rpx;
}

.inline-comment-item {
  margin-bottom: 8rpx;
}

.inline-comment-item:last-child {
  margin-bottom: 0;
}

.inline-comment-item .c-name {
  color: #333333;
  font-size: 28rpx;
  font-weight: bold;
}

.inline-comment-item .c-content {
  color: #333;
  font-size: 28rpx;
}

.more-comments {
  margin-top: 12rpx;
}

.more-comments text {
  color: $uni-color-primary;
  font-size: 26rpx;
  cursor: pointer;
  font-weight: 500;
}

.bottom-tips {
  text-align: center;
  padding: 40rpx 0;
  color: $uni-text-color-placeholder;
  font-size: 24rpx;
}

.publish-actions {
  position: fixed;
  right: 40rpx;
  bottom: 180rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  z-index: 99;
}

.publish-actions .action-item {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(107, 94, 247, 0.3);
  transition: all 0.2s;
}

.publish-actions .action-item:active {
  transform: scale(0.9);
}

.main-publish {
  background: $uni-color-primary-gradient;
}

.publish-icon {
  font-size: 56rpx;
  color: #fff;
}

/* Comment Panel */
.comment-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0,0,0,0.5);
  z-index: 200;
}

/* Share Panel */
.share-panel {
  position: fixed;
  bottom: var(--window-bottom, 0); left: 0; right: 0;
  background-color: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  z-index: 201;
  padding-bottom: 0;
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
  color: $uni-text-color;
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
  background-color: #07C160;
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
  color: $uni-text-color-regular;
}

.share-cancel {
  background-color: #FFFFFF;
  text-align: center;
  padding: 30rpx 0;
  font-size: 32rpx;
  color: $uni-text-color;
}

.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 200;
}

/* 评论输入弹窗 */
.comment-input-panel {
  position: fixed;
  bottom: -100vh;
  left: 0; right: 0;
  background-color: $uni-bg-color;
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
  border-top: 1px solid $uni-bg-color-page;
}

.comment-input {
  flex: 1;
  background-color: $uni-bg-color-page;
  height: 72rpx;
  border-radius: $uni-border-radius-pill;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.comment-send-btn {
  margin-left: 16rpx;
  height: 64rpx;
  line-height: 64rpx;
  background-color: $uni-color-primary;
  color: $uni-bg-color;
  font-size: 26rpx;
  border-radius: $uni-border-radius-lg;
  padding: 0 30rpx;
}

.comment-send-btn[disabled] {
  background-color: #ccc;
}

.user-extra {
  font-size: 22rpx;
  color: $uni-text-color-placeholder;
}
</style>
