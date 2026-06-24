<template>
  <view class="detail-container">
    <scroll-view scroll-y class="scroll-view" :show-scrollbar="false">
      <!-- 顶部大图 -->
      <view class="header-image-wrapper">
        <swiper
          class="header-swiper"
          circular
          autoplay
          :interval="2000"
          :duration="520"
          @change="handlePhotoChange"
        >
          <swiper-item v-for="(photo, index) in user.photos" :key="`${user.userId}-${index}`">
            <image class="header-image" :src="photo" mode="aspectFill" @click="previewPhoto(index)"></image>
          </swiper-item>
        </swiper>
        
        <view class="photo-indicators" v-if="user.photos && user.photos.length > 1">
          <view
            v-for="(_, index) in user.photos"
            :key="`indicator-${user.userId}-${index}`"
            class="photo-indicator"
            :class="{ active: currentPhotoIndex === index }"
          ></view>
        </view>
      </view>

      <!-- 资料详情区 -->
      <view class="profile-content">
        <!-- 基础信息卡片 -->
        <view class="info-card main-card">
          <view class="name-row">
            <text class="name">{{ user.nickname || '神秘鸭鸭' }}</text>
            <text class="user-id-small">ID:{{ user.userId }}</text>
          </view>
          
          <view class="auth-row">
            <view class="id-card-tag" v-if="user.idCard">
              身份证：{{ String(user.idCard).substring(0, 3) }}***
            </view>
            <view class="id-card-tag unauth" v-else>
              未实名认证
            </view>
            <view class="unlock-wechat-btn" v-if="!user.wechatUnlocked" @click.stop="unlockWechat">
              <text class="ri-wechat-line"></text> 解锁微信号
            </view>
            <view class="unlocked-wechat-text" v-else @click.stop="copyWechat">
              <text class="ri-wechat-line"></text> 微信号：{{ user.wechat }}
            </view>
          </view>
          
          <view class="sub-tags">
            <view class="s-tag highlight">
              <text class="ri-flashlight-line icon"></text> {{ user.onlineText || '正在线' }}
            </view>
            <view class="s-tag">
              <text class="ri-map-pin-line icon"></text> 距你<text>{{ user.distance || '0.5km' }}</text> {{ user.location || '同城' }}
            </view>
            <view class="s-tag">
              <text class="ri-search-heart-line icon"></text> 匹配度<text>{{ user.matchScore || 90 }}</text>%
            </view>
          </view>

          <view class="grid-info">
            <view class="g-item">
              <text class="ri-cake-2-line icon"></text> <text>{{ user.age || 25 }}</text>岁 · {{ user.constellation || '狮子座' }}
            </view>
            <view class="g-item">
              <text class="ri-ruler-line icon"></text> <text>{{ user.height || 170 }}</text>cm · <text>{{ user.weight || 55 }}</text>kg
            </view>
            <view class="g-item" style="width: 58%">
              <text class="ri-graduation-cap-line icon"></text> <text class="ellipsis-text">{{ user.education || '本科' }} · {{ user.school || '中国科学技术大学' }}</text>
            </view>
            <view class="g-item" style="width: 42%">
              <text class="ri-hearts-line icon"></text> {{ user.maritalStatus || '未婚' }}
            </view>
            <view class="g-item full-width">
              <text class="ri-briefcase-4-line icon"></text> {{ user.profession || '程序员' }} · {{ user.income || '年入20w' }}
            </view>
            <view class="g-item full-width">
              <text class="ri-home-4-line icon"></text> 现居 {{ formatCity(user.location || '北京') }} · {{ formatCity(user.hometown || '河北石家庄') }}人 · 户籍 {{ formatCity(user.household || '北京') }}
            </view>
          </view>
        </view>

        <view class="info-card moment-card" @click="goUserPosts">
          <view class="card-header compact">
            <text class="ri-image-circle-fill header-icon"></text>
            <text class="title">最新动态</text>
          </view>
          <view class="post-item">
            <view class="post-content-area">
              <text class="post-text">{{ user.firstPost || '最近常来，说不定正在等你。' }}</text>
              <view class="bio-photos" v-if="user.photos && user.photos.length > 0">
                <image 
                  class="bio-photo" 
                  v-for="(img, i) in (user.photos || []).slice(0, 3)" 
                  :key="'post-img-'+i" 
                  :src="img" 
                  mode="aspectFill"
                  @click.stop="previewPhoto(i)"
                ></image>
              </view>
            </view>
          </view>
        </view>

        <!-- 自我介绍及家庭成员卡片 -->
        <view class="info-card bio-card">
          <view class="card-header">
            <text class="ri-quill-pen-fill header-icon"></text>
            <text class="title">自我介绍及家庭成员</text>
          </view>
          <view class="bio-content-wrap">
            <text class="bio-text" v-if="user.bio">{{ user.bio }}</text>
            <text class="bio-text" v-if="user.family">{{ user.family }}</text>
            <text class="bio-text" v-if="!user.bio && !user.family">211本 985硕 京户 程序员 INFP\n性格：自信阳光开朗，性情中人，慕强；\n喜欢漂亮精致美好，也可以大大咧咧不拘小节；\n家庭关系稳定，重视沟通和边界感。\n真诚找对象，欢迎真诚靠谱的请详聊。</text>
          </view>
          <view class="bio-photos" v-if="user.photos && user.photos.length > 0">
            <image v-for="(photo, index) in (user.photos || []).slice(0, 6)" :key="'bio-photo-'+index" :src="photo" class="bio-photo" mode="aspectFill" @click="previewPhoto(index)"></image>
          </view>
        </view>

        <view class="info-card bio-card">
          <view class="card-header">
            <text class="ri-heart-pulse-fill header-icon"></text>
            <text class="title">期待的伴侣及生活</text>
          </view>
          <view class="bio-content-wrap">
            <text class="bio-text" v-if="user.idealPartner">{{ user.idealPartner }}</text>
            <text class="bio-text" v-if="user.expectedLife">{{ user.expectedLife }}</text>
            <text class="bio-text" v-if="!user.idealPartner && !user.expectedLife">情绪稳定，有责任感，愿意沟通。\n一起把普通日子过得有温度。</text>
          </view>
        </view>

        <!-- 兴趣爱好及爱情宣言卡片 -->
        <view class="info-card bio-card">
          <view class="card-header">
            <text class="ri-star-smile-fill header-icon"></text>
            <text class="title">兴趣爱好及爱情宣言</text>
          </view>
          <view class="bio-content-wrap">
            <text class="bio-text" v-if="user.hobbies">{{ user.hobbies }}</text>
            <text class="bio-text" v-if="user.loveDeclaration">{{ user.loveDeclaration }}</text>
            <text class="bio-text" v-if="!user.hobbies && !user.loveDeclaration">旅行、阅读、电影和城市散步。\n真诚找对象，慢慢了解也可以很浪漫。</text>
          </view>
        </view>

        <!-- 我的标签卡片 -->
        <view class="info-card tags-card">
          <view class="card-header">
            <text class="ri-price-tag-3-fill header-icon"></text>
            <text class="title">我的标签</text>
          </view>
          <view class="tags-list">
            <view class="t-pill" :class="'c' + ((Number(index) % 6) + 1)" v-for="(tag, index) in (user.myTags && user.myTags.length > 0 ? user.myTags : ['💖 INFP 调停者', '☀️ 开朗积极', '😤 傲娇', '🤗 喜欢被照顾', '🏄‍♀️ 爽快', '👍 真诚靠谱'])" :key="index">
              {{ tag }}
            </view>
          </view>
        </view>

        <!-- 三重认证卡片（折叠式） -->
        <view class="info-card auth-card-new">
          <view class="card-header auth-toggle" @click="authExpanded = !authExpanded">
            <view class="auth-toggle-left">
              <text class="ri-shield-check-fill header-icon"></text>
              <text class="title">三重认证</text>
            </view>
            <view class="auth-toggle-right">
              <text class="auth-percent" v-show="!authExpanded">真实度95%</text>
              <text :class="authExpanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'" class="toggle-arrow"></text>
            </view>
          </view>
          <view class="auth-list" v-show="authExpanded">
            <view class="auth-item">
              <text class="ri-face-recognition-line a-icon"></text>
              <view class="a-info">
                <text class="a-title">头像认证</text>
                <text class="a-desc">完成人脸比对</text>
              </view>
              <text class="a-status success">真实度 95%</text>
            </view>
            <view class="auth-item">
              <text class="ri-profile-line a-icon"></text>
              <view class="a-info">
                <text class="a-title">实名认证</text>
                <text class="a-desc">居民身份证</text>
              </view>
              <text class="a-status success" v-if="user.idCard">真实度 95%</text>
              <text class="a-status pending" v-else>真实度 95%</text>
            </view>
            <view class="auth-item">
              <text class="ri-graduation-cap-line a-icon"></text>
              <view class="a-info">
                <text class="a-title">学历认证</text>
                <text class="a-desc">提交毕业证/毕业证编号</text>
              </view>
              <text class="a-status success" v-if="user.educationAuth">真实度 95%</text>
              <text class="a-status pending" v-else>真实度 95%</text>
            </view>
          </view>
          <!-- 收起样式 -->
          <view class="auth-collapsed" v-show="!authExpanded" @click="authExpanded = true">
            <view class="auth-c-item success">
              <text class="ri-face-recognition-line c-icon"></text>
              <text class="c-text">头像认证</text>
            </view>
            <view class="auth-c-item" :class="user.idCard ? 'success' : 'pending'">
              <text class="ri-profile-line c-icon"></text>
              <text class="c-text">实名认证</text>
            </view>
            <view class="auth-c-item" :class="user.educationAuth ? 'success' : 'pending'">
              <text class="ri-graduation-cap-line c-icon"></text>
              <text class="c-text">学历认证</text>
            </view>
          </view>
        </view>

        <view class="safety-actions">
          <view class="safety-btn" @click="reportUser">
            <text class="ri-alarm-warning-line"></text>
            <text>举报</text>
          </view>
          <view class="safety-btn" @click="handleBlock">
            <text :class="isBlocked ? 'ri-checkbox-circle-line' : 'ri-forbid-line'"></text>
            <text>{{ isBlocked ? '解除屏蔽' : '屏蔽' }}</text>
          </view>
        </view>

        <!-- 底部安全区，防止被操作栏遮挡 -->
        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <!-- 固定在右侧的操作栏 -->
    <view class="fixed-action-bar">
      <view class="action-btn like-btn" @click.stop="handleLike($event)">
        <text class="icon ri-heart-fill"></text>
      </view>
      <view class="action-btn super-btn" @click.stop="handleSendRose($event)">
        <text class="icon">🌹</text>
      </view>
    </view>
    <!-- 统一自定义弹窗 -->
    <custom-popup />

    <!-- 漂浮特效容器 -->
    <view class="floating-container">
      <view
        v-for="item in floatingItems"
        :key="item.id"
        class="floating-item"
        :class="[item.type, item.animationClass]"
        :style="{ left: item.x + 'px', top: item.y + 'px' }"
      >
        <text v-if="item.type === 'like'" class="icon ri-heart-fill"></text>
        <text v-else class="icon">🌹</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { request } from '../../utils/request'
import { getMockPhotos } from '../../utils/mockData'
import { checkAuth } from '../../utils/auth'

const isVip = ref(false)
const authExpanded = ref(false)
const isBlocked = ref(false)
const currentPhotoIndex = ref(0)

const floatingItems = ref<any[]>([])
let floatId = 0

const createFloatingEffect = (type: 'like' | 'rose', e: any) => {
  let x = uni.getSystemInfoSync().windowWidth / 2 - 30
  let y = uni.getSystemInfoSync().windowHeight / 2 - 30

  if (e && e.touches && e.touches.length > 0) {
    x = e.touches[0].clientX - 30
    y = e.touches[0].clientY - 30
  } else if (e && e.clientX) {
    x = e.clientX - 30
    y = e.clientY - 30
  }

  const id = floatId++
  const randomAnim = `float-anim-${Math.floor(Math.random() * 3) + 1}`
  
  floatingItems.value.push({
    id,
    type,
    x,
    y,
    animationClass: randomAnim
  })

  setTimeout(() => {
    floatingItems.value = floatingItems.value.filter(item => item.id !== id)
  }, 1000)
}

const user = ref<any>({
  userId: 0,
  nickname: '加载中...',
  photos: [],
  idCard: '',
  educationAuth: false,
  age: 0,
  height: 0,
  weight: 0,
  constellation: '',
  education: '',
  school: '',
  income: '',
  location: '',
  hometown: '',
  household: '',
  profession: '',
  bio: '',
  family: '',
  hobbies: '',
  loveDeclaration: '',
  idealPartner: '',
  expectedLife: '',
  wechat: '',
  wechatUnlocked: false,
  myTags: [],
  distance: '0.5km',
  maritalStatus: '',
  onlineText: '正在线',
  matchScore: 90,
  firstPost: '最近常来，说不定正在等你。'
})

const getEduText = (edu: number) => {
  const map = { 1: '大专', 2: '本科', 3: '硕士', 4: '博士' }
  return (map as any)[edu] || '未知'
}

const getIncomeText = (income: number) => {
  const map = { 1: '5万以下', 2: '5-10w', 3: '10-20w', 4: '20-30w', 5: '30-40w', 6: '40-50w', 7: '50-60w', 8: '60-80w', 9: '80-100w', 10: '100w以上' }
  return (map as any)[income] || '未知'
}

const formatCity = (val: string) => {
  if (!val) return ''
  val = val.replace('户籍', '')
  let [province, city] = val.split('-')
  if (!city) return val
  if (province === city || province + '市' === city || province === city.replace(/市$/, '')) {
    return province
  }
  return province + city.replace(/市$/, '')
}

const handlePhotoChange = (event: any) => {
  currentPhotoIndex.value = event.detail.current || 0
}

const previewPhoto = (index: number | string) => {
  const idx = Number(index)
  if (user.value && user.value.photos) {
    uni.previewImage({
      urls: user.value.photos,
      current: user.value.photos[idx]
    })
  }
}

const goUserPosts = () => {
  uni.navigateTo({ url: `/pages/user-posts/index?userId=${user.value.userId}` })
}

const fetchUserDetail = async (id: number) => {
  try {
    const res: any = await request({ url: `/profile/detail?targetUserId=${id}`, method: 'GET' })
    if (res) {
      let data = res;
      if (res.code === 200) data = res.data;
      
      user.value = {
        userId: data.userId,
        nickname: data.nickname || '神秘鸭鸭',
        photos: typeof data.photos === 'string' ? JSON.parse(data.photos) : (data.photos || getMockPhotos(3)),
        idCard: data.isRealAuth ? '110105199001011234' : '', // Mock idcard for display if auth
        educationAuth: data.isEduAuth === 1,
        age: data.age || 26,
        height: data.height || 168,
        weight: data.weight || 55,
        constellation: data.constellation || '',
        education: getEduText(data.education),
        school: data.school || '中国科学技术大学',
        income: getIncomeText(data.incomeLevel),
        location: data.location || '同城',
        hometown: data.hometown || '',
        household: data.household || '',
        profession: data.profession || '保密',
        bio: data.bio || '这个人很懒，什么都没留下~',
        family: data.family || '家庭关系稳定，重视沟通和边界感。',
        hobbies: data.hobbies || '旅行、阅读、电影和城市散步。',
        loveDeclaration: data.loveDeclaration || '真诚找对象，慢慢了解也可以很浪漫。',
        idealPartner: data.idealPartner || '情绪稳定，有责任感，愿意沟通。',
        expectedLife: data.expectedLife || '一起把普通日子过得有温度。',
        wechat: data.wechatId, // might be null if not unlocked
        wechatUnlocked: false,
        myTags: typeof data.tags === 'string' ? JSON.parse(data.tags) : (data.tags || ['💖 INFP 调停者', '☀️ 开朗积极', '😤 傲娇', '🤗 喜欢被照顾', '🏄‍♀️ 爽快', '👍 真诚靠谱']),
        distance: data.distance || '0.5km',
        maritalStatus: data.maritalStatus === 2 ? '离异' : (data.maritalStatus === 3 ? '丧偶' : '未婚'),
        onlineText: data.onlineText || '刚刚',
        matchScore: data.matchScore || 95,
        firstPost: data.firstPost || '最近常来，说不定正在等你。'
      }
      
      isBlocked.value = data.isBlocked || false;
    }
  } catch (e) {
    console.error(e)
  }
}

onLoad((options) => {
  if (options && options.id) {
    user.value.userId = Number(options.id)
    fetchUserDetail(user.value.userId)
  }
})

onShareAppMessage(() => {
  return {
    title: `为你推荐 ${user.value.nickname} 的个人主页`,
    path: `/pages/user-detail/index?id=${user.value.userId}`,
    imageUrl: user.value.photos && user.value.photos.length > 0 ? user.value.photos[0] : ''
  }
})

onShareTimeline(() => {
  return {
    title: `为你推荐 ${user.value.nickname} 的个人主页`,
    query: `id=${user.value.userId}`,
    imageUrl: user.value.photos && user.value.photos.length > 0 ? user.value.photos[0] : ''
  }
})

const unlockWechat = async () => {
  if (!(await checkAuth('解锁微信号'))) return
  const isVip = uni.getStorageSync('isVip') === true
  const cost = isVip ? 50 : 100
  uni.showModal({
    title: '解锁微信号',
    content: `查看 ${user.value.nickname} 的微信号需支付 ${cost} 鸭蛋`,
    confirmColor: '#6A61F8',
    success: async (res) => {
      if (!res.confirm) return
      uni.showLoading({ title: '解锁中...' })
      try {
        const wechatId: any = await request({ url: `/relation/unlock-wechat?targetId=${user.value.userId}`, method: 'POST' })
        uni.hideLoading()
        user.value.wechatUnlocked = true
        user.value.wechat = wechatId || '微信号未填写'
        uni.showToast({ title: '解锁成功', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
      }
    }
  })
}

const copyWechat = () => {
  if (user.value.wechat) {
    uni.setClipboardData({
      data: String(user.value.wechat),
      success: () => {
        uni.showToast({ title: '微信号已复制', icon: 'success' })
      }
    })
  }
}

const handleLike = async (event?: any) => {
  if (!(await checkAuth('点赞'))) return
  if (event) createFloatingEffect('like', event)
  // uni.showToast({ title: '已喜欢', icon: 'none' })
}

const handleChat = async () => {
  if (!(await checkAuth('私聊'))) return
  uni.navigateTo({
    url: `/pages/chat/index?id=${user.value.userId}&name=${user.value.nickname}`
  })
}

const handleSendRose = async (event?: any) => {
  if (!(await checkAuth('送礼物'))) return
  try {
    if (event) createFloatingEffect('rose', event)
    await request({
      url: `/chat/send-gift`,
      method: 'POST',
      data: { receiverId: user.value.userId, giftName: '玫瑰花', amount: 20 }
    })
    uni.showToast({ title: '送花成功，已开启私聊', icon: 'none' })
    setTimeout(() => {
      handleChat()
    }, 1000)
  } catch (e: any) {
    if (e && e.code === 400 && e.message && e.message.includes('不足')) {
       uni.showModal({
         title: '鸭蛋不足',
         content: '鸭蛋余额不足，是否前往充值？',
         confirmText: '去充值',
         success: (res) => {
           if (res.confirm) {
             uni.navigateTo({ url: '/pages/egg-recharge/index' })
           }
         }
       })
    }
  }
}

const handleBlock = async () => {
  if (!(await checkAuth('屏蔽'))) return
  if (isBlocked.value) {
    uni.showModal({
      title: '解除屏蔽',
      content: `确定要解除对 ${user.value.nickname} 的屏蔽吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({ url: `/relation/unblock?targetId=${user.value.userId}`, method: 'POST' })
            isBlocked.value = false
            uni.showToast({ title: '已解除屏蔽', icon: 'none' })
          } catch (e) {}
        }
      }
    })
  } else {
    uni.showModal({
      title: '屏蔽用户',
      content: `确定要屏蔽 ${user.value.nickname} 吗？屏蔽后将不再收到对方的消息，并且首页不会再推荐对方。`,
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '处理中...' })
            await request({ url: `/relation/block?targetId=${user.value.userId}`, method: 'POST' })
            uni.hideLoading()
            isBlocked.value = true
            uni.showToast({ title: '屏蔽成功', icon: 'success' })
          } catch (e) {
            uni.hideLoading()
          }
        }
      }
    })
  }
}

const reportUser = async () => {
  if (!(await checkAuth('举报用户'))) return
  uni.navigateTo({
    url: `/pages/report/index?targetId=${user.value.userId}&targetName=${user.value.nickname}&type=其他举报`
  })
}
</script>

<style lang="scss" scoped>
.detail-container {
  background-color: $uni-bg-color-page;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.scroll-view {
  flex: 1;
  height: 0;
}

/* 漂浮特效容器和动画 */
.floating-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 99999;
  overflow: hidden;
}

.floating-item {
  position: absolute;
  font-size: 60rpx;
  opacity: 1;
  transform-origin: center center;
}

.floating-item.like {
  color: #FF5777;
  text-shadow: 0 4rpx 12rpx rgba(255, 87, 119, 0.4);
}

.floating-item.rose {
  filter: drop-shadow(0 4rpx 12rpx rgba(255, 105, 180, 0.4));
}

@keyframes floatUp1 {
  0% { transform: translateY(0) scale(0.5) rotate(0deg); opacity: 1; }
  50% { transform: translateY(-200rpx) scale(1.2) rotate(-15deg); opacity: 0.8; }
  100% { transform: translateY(-400rpx) scale(1.5) rotate(-30deg); opacity: 0; }
}

@keyframes floatUp2 {
  0% { transform: translateY(0) scale(0.5) rotate(0deg); opacity: 1; }
  50% { transform: translateY(-220rpx) scale(1.3) rotate(15deg); opacity: 0.8; }
  100% { transform: translateY(-450rpx) scale(1.6) rotate(30deg); opacity: 0; }
}

@keyframes floatUp3 {
  0% { transform: translateY(0) scale(0.5) rotate(0deg); opacity: 1; }
  50% { transform: translateY(-180rpx) scale(1.1) rotate(5deg); opacity: 0.9; }
  100% { transform: translateY(-380rpx) scale(1.4) rotate(10deg); opacity: 0; }
}

.float-anim-1 { animation: floatUp1 1s cubic-bezier(0.25, 1, 0.5, 1) forwards; }
.float-anim-2 { animation: floatUp2 1s cubic-bezier(0.25, 1, 0.5, 1) forwards; }
.float-anim-3 { animation: floatUp3 1s cubic-bezier(0.25, 1, 0.5, 1) forwards; }

.header-image-wrapper {
  position: relative;
  width: auto;
  height: 850rpx;
  margin: 20rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
}

.header-swiper {
  width: 100%;
  height: 100%;
}

.header-image {
  width: 100%;
  height: 100%;
  background-color: $uni-bg-color-page;
}

.photo-indicators {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 40rpx;
  z-index: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
}

.photo-indicator {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1rpx 8rpx rgba(0, 0, 0, 0.10);
  flex: none;
  transition: all 0.3s ease;
}

.photo-indicator.active {
  background: rgba(255, 255, 255, 0.96);
  width: 14rpx;
  height: 14rpx;
}

.profile-content {
  background-color: transparent;
  position: relative;
  z-index: 2;
  padding: 0 20rpx 30rpx 20rpx;
  margin-top: 20rpx;
}

.info-card {
  background: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 32rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: $uni-shadow-base;
}

.main-card .name-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 12rpx;
}

.main-card .name {
  font-size: 48rpx;
  font-weight: 800;
  color: $uni-text-color;
  max-width: 450rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1;
}

.user-id-small {
  font-size: 30rpx;
  color: $uni-text-color-grey;
  margin-left: 12rpx;
  flex-shrink: 0;
  font-weight: normal;
  line-height: 1;
  margin-bottom: 2rpx;
}

.auth-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0 !important;
}
.auth-toggle-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.auth-toggle-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.auth-percent {
  font-size: 24rpx;
  color: #07C160;
  font-weight: bold;
  background: rgba(7, 193, 96, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
}
.toggle-arrow {
  font-size: 32rpx;
  color: $uni-text-color-grey;
}

.unlock-wechat-btn {
  background: rgba(7, 193, 96, 0.1);
  color: #07C160;
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: $uni-border-radius-pill;
  font-weight: normal;
  font-style: normal;
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}
.unlocked-wechat-text {
  background: rgba(7, 193, 96, 0.1);
  color: #07C160;
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: $uni-border-radius-pill;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.auth-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  gap: 12rpx;
}

.main-card .vip-badge {
  background: $uni-color-primary-light;
  color: $uni-color-primary;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: $uni-border-radius-base;
  font-weight: bold;
  font-style: normal;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.main-card .id-card-tag {
  background: #F0F9FF;
  color: #1890FF;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: $uni-border-radius-base;
  font-weight: normal;
  font-style: normal;
}

.main-card .id-card-tag.unauth {
  background: #F6F6F6;
  color: #999999;
}

.main-card .sub-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 30rpx;
}

.main-card .s-tag {
  font-size: 24rpx;
  color: $uni-text-color-regular;
  background: #F6F6F6;
  padding: 8rpx 20rpx;
  border-radius: $uni-border-radius-pill;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.main-card .s-tag.highlight {
  background: #FFF0F0;
  color: $uni-color-primary;
}

.grid-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx 0;
}

.g-item {
  width: 50%;
  font-size: 28rpx;
  color: $uni-text-color;
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.g-item.full-width {
  width: 100%;
}
.g-item .icon {
  color: $uni-text-color-grey;
  font-size: 32rpx;
  flex-shrink: 0;
}
.ellipsis-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 三重认证卡片 */
.auth-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auth-left .card-header {
  margin-bottom: 16rpx;
}
.auth-left .title {
  font-size: 32rpx;
  font-weight: bold;
  color: $uni-text-color;
}

.auth-tags {
  display: flex;
  gap: 16rpx;
}

.a-tag {
  font-size: 24rpx;
  color: $uni-text-color;
  background: #F6F6F6;
  padding: 8rpx 20rpx;
  border-radius: $uni-border-radius-pill;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.auth-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.score-label {
  font-size: 24rpx;
  color: $uni-text-color-grey;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.score-num {
  font-size: 64rpx;
  font-weight: 800;
  color: $uni-color-primary;
  line-height: 1.1;
}
.score-num .pct {
  font-size: 32rpx;
}

/* 我的认证 */
.auth-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-top: 24rpx;
}
.auth-item {
  display: flex;
  align-items: center;
  background: #F8F9FA;
  padding: 20rpx 24rpx;
  border-radius: $uni-border-radius-base;
}
.auth-item .a-icon {
  font-size: 40rpx;
  color: $uni-color-primary;
  margin-right: 20rpx;
}
.auth-item .a-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.auth-item .a-title {
  font-size: 28rpx;
  font-weight: bold;
  color: $uni-text-color;
  margin-bottom: 6rpx;
}
.auth-item .a-desc {
  font-size: 24rpx;
  color: $uni-text-color-grey;
}
.auth-item .a-status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: $uni-border-radius-pill;
}
.auth-item .a-status.success {
  background: rgba(7, 193, 96, 0.1);
  color: #07C160;
}
.auth-item .a-status.pending {
  background: rgba(255, 151, 106, 0.1);
  color: #FF976A;
}

/* 认证收起样式 */
.auth-collapsed {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}
.auth-c-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: $uni-border-radius-pill;
}
.auth-c-item .c-icon {
  font-size: 28rpx;
}
.auth-c-item.success {
  color: #07C160;
  background: rgba(7, 193, 96, 0.1);
}
.auth-c-item.pending {
  color: #FF976A;
  background: rgba(255, 151, 106, 0.1);
}

/* 自我介绍 & 标签通用 */
.card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.card-header .header-icon {
  font-size: 36rpx;
  color: $uni-color-primary;
}
.card-header .title {
  font-size: 32rpx;
  font-weight: bold;
  color: $uni-text-color;
}
.card-header.compact {
  margin-bottom: 18rpx;
}
/* 最新动态（仿动态列表） */
.post-item {
  display: flex;
  margin-top: 10rpx;
}
.post-content-area {
  flex: 1;
  min-width: 0;
}
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
  margin-bottom: 12rpx;
}
.post-img {
  width: 160rpx;
  height: 160rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}
.single-img .post-img {
  width: 320rpx;
  height: 320rpx;
  border-radius: 12rpx;
  max-width: 100%;
}
.post-text {
  font-size: 30rpx;
  color: #1a1a1a;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-all;
}

.bio-text {
  font-size: 30rpx;
  color: $uni-text-color;
  line-height: 1.8;
}
.bio-content-wrap {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.bio-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 24rpx;
}
.bio-photo {
  width: calc((100% - 32rpx) / 3);
  aspect-ratio: 1 / 1;
  height: auto;
  border-radius: 16rpx;
  background-color: $uni-bg-color-page;
}

.detail-sections {
  padding: 8rpx 30rpx;
}

.detail-item {
  padding: 28rpx 0;
  border-bottom: 1rpx solid $uni-border-color;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.detail-item.last {
  border-bottom: none;
}

.detail-title {
  font-size: 28rpx;
  font-weight: 800;
  color: $uni-text-color;
}

.detail-text {
  font-size: 27rpx;
  line-height: 1.7;
  color: $uni-text-color-regular;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.t-pill {
  font-size: 26rpx;
  padding: 12rpx 28rpx;
  border-radius: $uni-border-radius-pill;
  font-weight: 500;
}
.t-pill.c1 { background: #FFF0F5; color: #E83A7E; }
.t-pill.c2 { background: #FFF7E6; color: #FA8C16; }
.t-pill.c3 { background: #F6FFED; color: #52C41A; }
.t-pill.c4 { background: #E6F7FF; color: #1890FF; }
.t-pill.c5 { background: #F9F0FF; color: #722ED1; }
.t-pill.c6 { background: #FFFBE6; color: #D48806; }

.bottom-spacer {
  height: 40rpx;
}

.safety-actions {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  margin: 8rpx 0 24rpx;
}

.safety-btn {
  flex: 1;
  height: 76rpx;
  border-radius: $uni-border-radius-pill;
  background: rgba(255, 255, 255, 0.78);
  color: $uni-text-color-grey;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  box-shadow: $uni-shadow-sm;
}

.safety-btn text:first-child {
  font-size: 30rpx;
}

/* 右侧操作栏悬浮 */
.fixed-action-bar {
  position: fixed;
  right: calc(30rpx - 10px);
  top: calc(48% + 110px);
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 52rpx;
  z-index: 100;
  pointer-events: none;
}

.action-btn {
  pointer-events: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #FFFFFF;
  border-radius: 50%;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.12);
  transition: transform 0.2s;
}

.action-btn:active {
  transform: scale(0.9);
}

.super-btn {
  width: 100rpx;
  height: 100rpx;
}
.super-btn .icon { font-size: 58rpx; font-style: normal; }

.like-btn {
  width: 100rpx;
  height: 100rpx;
}

.like-btn .icon { 
  font-size: 58rpx; 
  background: linear-gradient(135deg, #FF416C 0%, #FF4B2B 100%);
  -webkit-background-clip: text;
  color: transparent;
  filter: drop-shadow(0 4rpx 8rpx rgba(255, 65, 108, 0.4));
}
</style>
