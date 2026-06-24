<template>
  <view class="home-container">
    <!-- 自定义导航栏（悬浮在图片上，顶部渐变遮罩保证文字可读性） -->
    <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }" :class="{ 'nav-scrolled': isScrolled || sortMode !== 'recommend' }">
      <view class="nav-content">
        <view class="nav-left">
          <view class="sort-tabs">
            <view
              class="sort-tab"
              :class="{ active: sortMode === 'recommend' }"
              @click="changeSortMode('recommend')"
            >
              推荐
            </view>
            <view
              class="sort-tab"
              :class="{ active: sortMode === 'active' }"
              @click="changeSortMode('active')"
            >
              喜欢
            </view>
            <view
              class="sort-tab"
              :class="{ active: sortMode === 'match' }"
              @click="changeSortMode('match')"
            >
              喜欢我的
            </view>
          </view>
        </view>
        <view class="nav-actions">
          <view class="action-icon share-icon" @click="handleShare">
            <text class="icon-text ri-share-forward-line"></text>
          </view>
          <view class="action-icon" @click="goVisitorHistory">
            <text class="icon-text ri-footprint-line"></text>
          </view>
          <view class="action-icon" @click="handleFilter">
            <text class="icon-text ri-sound-module-line"></text>
          </view>
        </view>
      </view>
    </view>

    <block v-if="!isProfileComplete">
      <!-- 完善资料默认页面(二狗参考风格) -->
      <view class="ergou-default-page">
        <!-- 顶部装饰背景 -->
        <view class="ergou-bg-decoration">
          <view class="circle-blur top-left"></view>
          <view class="circle-blur bottom-right"></view>
        </view>
        
        <view class="ergou-content">
          <view class="mascot-wrapper">
            <image class="ergou-mascot" src="/static/hongxiaoya-logo.png" mode="aspectFill"></image>
            <view class="mascot-badge">Hi~</view>
          </view>
          
          <view class="text-center">
            <text class="ergou-title">完善资料 开启缘分</text>
            <text class="ergou-subtitle">靠谱单身⻘年脱单平台</text>
          </view>
          
          <view class="ergou-feature-card">
            <view class="feature-item">
              <view class="icon-box purple">
                <text class="ri-shield-user-fill"></text>
              </view>
              <view class="feature-text-box">
                <text class="f-title">真实身份认证</text>
                <text class="f-desc">严格审核把关，交友更安心</text>
              </view>
            </view>
            <view class="feature-divider"></view>
            <view class="feature-item">
              <view class="icon-box pink">
                <text class="ri-heart-pulse-fill"></text>
              </view>
              <view class="feature-text-box">
                <text class="f-title">每日精准推荐</text>
                <text class="f-desc">多维度算法，匹配高契合度异性</text>
              </view>
            </view>
            <view class="feature-divider"></view>
            <view class="feature-item">
              <view class="icon-box orange">
                <text class="ri-vip-crown-fill"></text>
              </view>
              <view class="feature-text-box">
                <text class="f-title">海量优质用户</text>
                <text class="f-desc">名企大厂/海归/公职，圈子更纯粹</text>
              </view>
            </view>
          </view>

          <view class="btn-wrapper">
            <button class="ergou-btn" @click="goProfileEdit">
              <text>立即完善资料</text>
              <text class="ri-arrow-right-s-line arrow-icon"></text>
            </button>
            <text class="safe-tip"><text class="ri-lock-fill"></text> 信息仅用于匹配，平台严格保密</text>
          </view>
        </view>
      </view>
    </block>

    <block v-else-if="sortMode === 'recommend'">
      <block v-if="currentUser">
        <scroll-view scroll-y class="scroll-view" :class="swipeClass" :show-scrollbar="false" :bounces="false" @scroll="onScroll">
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
            <swiper-item v-for="(photo, index) in currentUser.photos" :key="`${currentUser.userId}-${index}`">
              <image class="header-image" :src="photo" mode="aspectFill" @click="previewPhoto(index)"></image>
            </swiper-item>
          </swiper>
          
          <view class="photo-indicators" v-if="currentUser.photos && currentUser.photos.length > 1">
            <view
              v-for="(_, index) in currentUser.photos"
              :key="`indicator-${currentUser.userId}-${index}`"
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
              <text class="name">{{ currentUser.nickname || '神秘鸭鸭' }}</text>
              <text class="user-id-small">ID:{{ currentUser.userId }}</text>
            </view>
            
            <view class="auth-row">
              <view class="id-card-tag" v-if="currentUser.idCard">
                身份证：{{ String(currentUser.idCard).substring(0, 3) }}***
              </view>
              <view class="id-card-tag unauth" v-else>
                未实名认证
              </view>
              <view class="unlock-wechat-btn" v-if="!currentUser.wechatUnlocked" @click.stop="unlockWechat(currentUser)">
                <text class="ri-wechat-line"></text> 解锁微信号
              </view>
              <view class="unlocked-wechat-text" v-else @click.stop="copyWechat(currentUser)">
                <text class="ri-wechat-line"></text> 微信号：{{ currentUser.wechat }}
              </view>
            </view>
            
            <view class="sub-tags">
              <view class="s-tag highlight">
                <text class="ri-flashlight-line icon"></text> {{ currentUser.onlineText || '正在线' }}
              </view>
              <view class="s-tag">
                <text class="ri-map-pin-line icon"></text> 距你<text>{{ currentUser.distance || '28km' }}</text> {{ currentUser.location || '北京' }}
              </view>
              <view class="s-tag">
                <text class="ri-search-heart-line icon"></text> 匹配度<text>{{ currentUser.matchScore || 90 }}</text>%
              </view>
            </view>

            <view class="grid-info">
              <view class="g-item">
                <text class="ri-cake-2-line icon"></text> <text>{{ currentUser.age || 25 }}</text>岁 · {{ currentUser.constellation || '狮子座' }}
              </view>
              <view class="g-item">
                <text class="ri-ruler-line icon"></text> <text>{{ currentUser.height || 170 }}</text>cm · <text>{{ currentUser.weight || 55 }}</text>kg
              </view>
              <view class="g-item" style="width: 58%">
                <text class="ri-graduation-cap-line icon"></text> <text class="ellipsis-text">{{ currentUser.education || '本科' }} · {{ currentUser.school || '中国科学技术大学' }}</text>
              </view>
              <view class="g-item" style="width: 42%">
                <text class="ri-hearts-line icon"></text> {{ currentUser.maritalStatus || '未婚' }}
              </view>
              <view class="g-item full-width">
                <text class="ri-briefcase-4-line icon"></text> {{ currentUser.profession || '程序员' }} · {{ currentUser.income || '年入20w' }}
              </view>
              <view class="g-item full-width">
                <text class="ri-home-4-line icon"></text> 现居 {{ formatCity(currentUser.location || '北京') }} · {{ formatCity(currentUser.hometown || '河北石家庄') }}人 · 户籍 {{ formatCity(currentUser.household || '北京') }}
              </view>
            </view>
          </view>

          <view class="info-card moment-card" @click="goUserPosts(currentUser)">
            <view class="card-header compact">
              <text class="ri-image-circle-fill header-icon"></text>
              <text class="title">最新动态</text>
            </view>
            <view class="post-item">
              <view class="post-content-area">
                <text class="post-text">{{ currentUser.firstPost || '最近常来，说不定正在等你。' }}</text>
                <view class="bio-photos" v-if="currentUser.photos && currentUser.photos.length > 0">
                  <image 
                    class="bio-photo" 
                    v-for="(img, i) in (currentUser.photos || []).slice(0, 3)" 
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
              <text class="bio-text" v-if="currentUser.bio">{{ currentUser.bio }}</text>
              <text class="bio-text" v-if="currentUser.family">{{ currentUser.family }}</text>
              <text class="bio-text" v-if="!currentUser.bio && !currentUser.family">211本 985硕 京户 程序员 INFP\n性格：自信阳光开朗，性情中人，慕强；\n喜欢漂亮精致美好，也可以大大咧咧不拘小节；\n家庭关系稳定，重视沟通和边界感。\n真诚找对象，欢迎真诚靠谱的请详聊。</text>
            </view>
            <view class="bio-photos" v-if="currentUser.photos && currentUser.photos.length > 0">
              <image v-for="(photo, index) in (currentUser.photos || []).slice(0, 6)" :key="'bio-photo-'+index" :src="photo" class="bio-photo" mode="aspectFill" @click="goUserDetail(currentUser)"></image>
            </view>
          </view>

          <view class="info-card bio-card">
            <view class="card-header">
              <text class="ri-heart-pulse-fill header-icon"></text>
              <text class="title">期待的伴侣及生活</text>
            </view>
            <view class="bio-content-wrap">
              <text class="bio-text" v-if="currentUser.idealPartner">{{ currentUser.idealPartner }}</text>
              <text class="bio-text" v-if="currentUser.expectedLife">{{ currentUser.expectedLife }}</text>
              <text class="bio-text" v-if="!currentUser.idealPartner && !currentUser.expectedLife">情绪稳定，有责任感，愿意沟通。\n一起把普通日子过得有温度。</text>
            </view>
          </view>

          <!-- 兴趣爱好及爱情宣言卡片 -->
          <view class="info-card bio-card">
            <view class="card-header">
              <text class="ri-star-smile-fill header-icon"></text>
              <text class="title">兴趣爱好及爱情宣言</text>
            </view>
            <view class="bio-content-wrap">
              <text class="bio-text" v-if="currentUser.hobbies">{{ currentUser.hobbies }}</text>
              <text class="bio-text" v-if="currentUser.loveDeclaration">{{ currentUser.loveDeclaration }}</text>
              <text class="bio-text" v-if="!currentUser.hobbies && !currentUser.loveDeclaration">旅行、阅读、电影和城市散步。\n真诚找对象，慢慢了解也可以很浪漫。</text>
            </view>
          </view>

          <!-- 我的标签卡片 -->
          <view class="info-card tags-card">
            <view class="card-header">
              <text class="ri-price-tag-3-fill header-icon"></text>
              <text class="title">我的标签</text>
            </view>
            <view class="tags-list">
              <view class="t-pill" :class="'c' + ((Number(index) % 6) + 1)" v-for="(tag, index) in (currentUser.tags || ['💖 INFP 调停者', '☀️ 开朗积极', '😤 傲娇', '🤗 喜欢被照顾', '🏄‍♀️ 爽快', '👍 真诚靠谱'])" :key="index">
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
                <text class="a-status success" v-if="currentUser.idCard">真实度 95%</text>
                <text class="a-status pending" v-else>真实度 95%</text>
              </view>
              <view class="auth-item">
                <text class="ri-graduation-cap-line a-icon"></text>
                <view class="a-info">
                  <text class="a-title">学历认证</text>
                  <text class="a-desc">提交毕业证/毕业证编号</text>
                </view>
                <text class="a-status success" v-if="currentUser.education">真实度 95%</text>
                <text class="a-status pending" v-else>真实度 95%</text>
              </view>
            </view>
            <!-- 收起样式 -->
            <view class="auth-collapsed" v-show="!authExpanded" @click="authExpanded = true">
              <view class="auth-c-item success">
                <text class="ri-face-recognition-line c-icon"></text>
                <text class="c-text">头像认证</text>
              </view>
              <view class="auth-c-item" :class="currentUser.idCard ? 'success' : 'pending'">
                <text class="ri-profile-line c-icon"></text>
                <text class="c-text">实名认证</text>
              </view>
              <view class="auth-c-item" :class="currentUser.education ? 'success' : 'pending'">
                <text class="ri-graduation-cap-line c-icon"></text>
                <text class="c-text">学历认证</text>
              </view>
            </view>
          </view>

          <view class="safety-actions">
            <view class="safety-btn" @click="reportUser(currentUser)">
              <text class="ri-alarm-warning-line"></text>
              <text>举报</text>
            </view>
            <view class="safety-btn" @click="toggleBlock(currentUser)">
              <text :class="currentUser.isBlocked ? 'ri-checkbox-circle-line' : 'ri-forbid-line'"></text>
              <text>{{ currentUser.isBlocked ? '解除屏蔽' : '屏蔽' }}</text>
            </view>
          </view>

          <!-- 底部安全区，防止被操作栏遮挡 -->
          <view class="bottom-spacer"></view>
        </view>
      </scroll-view>

      <!-- 固定在右侧的操作栏 -->
      <view class="fixed-action-bar">
        <view class="action-btn like-btn" @click.stop="handleLike(currentUser, $event)">
          <text class="icon ri-heart-fill"></text>
        </view>
        <view class="action-btn super-btn" @click.stop="handleSendRose(currentUser, $event)">
          <text class="icon">🌹</text>
        </view>
        <view class="action-btn pass-btn" @click.stop="handleDislike(currentUser)">
          <text class="icon ri-close-line"></text>
        </view>
      </view>
      </block>

      <!-- 空状态 -->
      <view class="empty-state" v-else>
        <image class="empty-logo" src="/static/hongxiaoya-logo.png" mode="aspectFit" style="border-radius: 12px; overflow: hidden;"></image>
        <text class="empty-text">附近暂无推荐用户</text>
        <button class="empty-btn" @click="handleFilter">放宽筛选条件</button>
      </view>
    </block>

    <LikeList v-else-if="sortMode === 'active'" />
    <WhoLikesMe v-else-if="sortMode === 'match'" />

    <!-- 筛选弹窗 -->
    <view class="modal-mask" v-if="showFilter" @click="showFilter = false"></view>
    <view class="filter-panel" :class="{'show': showFilter}">
      <view class="filter-header">
        <text>高级筛选</text>
        <text class="vip-badge">VIP</text>
      </view>
      <scroll-view scroll-y class="filter-scroll">
        <picker mode="multiSelector" :range="cityOptions" @change="(e:any) => filterForm.location = cityOptions[0][e.detail.value[0]] + '-' + cityOptions[1][e.detail.value[1]]" @columnchange="handleCityColumnChange">
          <view class="filter-item">
            <text class="label">居住地</text>
            <view class="picker-val" :class="{'has-val': filterForm.location}">{{ filterForm.location || '不限' }}</view>
          </view>
        </picker>
        <view class="filter-item">
          <text class="label">年龄段</text>
          <input class="input" v-model="filterForm.age" placeholder="例如：20-30" placeholder-class="ph-color"/>
        </view>
        <view class="filter-item">
          <text class="label">身高</text>
          <input class="input" v-model="filterForm.height" placeholder="例如：160-180" placeholder-class="ph-color"/>
        </view>
        <picker :range="['大专', '本科', '硕士', '博士']" @change="(e:any) => filterForm.education = ['大专', '本科', '硕士', '博士'][e.detail.value]">
          <view class="filter-item">
            <text class="label">最低学历</text>
            <view class="picker-val" :class="{'has-val': filterForm.education}">{{ filterForm.education || '不限' }}</view>
          </view>
        </picker>
        <picker :range="incomeOptions" @change="(e:any) => filterForm.income = incomeOptions[e.detail.value]">
          <view class="filter-item">
            <text class="label">年收入</text>
            <view class="picker-val" :class="{'has-val': filterForm.income}">{{ filterForm.income || '不限' }}</view>
          </view>
        </picker>
        <picker mode="multiSelector" :range="cityOptions2" @change="(e:any) => { filterForm.nativePlace = cityOptions2[0][e.detail.value[0]] + '-' + cityOptions2[1][e.detail.value[1]]; filterForm.household = cityOptions2[0][e.detail.value[0]] + '-' + cityOptions2[1][e.detail.value[1]]; }" @columnchange="handleCityColumnChange2">
          <view class="filter-item">
            <text class="label">籍贯/户口所在地</text>
            <view class="picker-val" :class="{'has-val': filterForm.nativePlace}">{{ filterForm.nativePlace || '不限' }}</view>
          </view>
        </picker>
        <picker :range="['未婚', '离异', '丧偶']" @change="(e:any) => filterForm.maritalStatus = ['未婚', '离异', '丧偶'][e.detail.value]">
          <view class="filter-item">
            <text class="label">婚姻状态</text>
            <view class="picker-val" :class="{'has-val': filterForm.maritalStatus}">{{ filterForm.maritalStatus || '不限' }}</view>
          </view>
        </picker>
      </scroll-view>
      <view class="filter-btns">
        <button class="reset-btn" @click="resetFilter">重置</button>
        <button class="confirm-btn" @click="applyFilter">确认</button>
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
  </view></template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { request } from '../../utils/request'
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { requireLogin, checkAuth, globalAuthGuard } from '../../utils/auth'
import { getMockPhotosByGender, getRandomAvatar, DEFAULT_AVATAR } from '../../utils/mockData'
import LikeList from './components/LikeList.vue'
import WhoLikesMe from './components/WhoLikesMe.vue'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
const recommendList = ref<any[]>([])
const sortMode = ref<'recommend' | 'active' | 'match'>('recommend')
const currentPhotoIndex = ref(0)
const isScrolled = ref(false)
const isProfileComplete = ref(Boolean(uni.getStorageSync('isProfileComplete')))
const authExpanded = ref(false)
const swipeClass = ref('')

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
  // 随机左右偏移和动画类
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

const doSwipe = (direction: 'left' | 'right', callback: () => void) => {
  swipeClass.value = `swipe-${direction}`
  setTimeout(() => {
    callback()
    swipeClass.value = 'swipe-reset'
    setTimeout(() => {
      swipeClass.value = ''
    }, 50)
  }, 300)
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

const onScroll = (e: any) => {
  isScrolled.value = e.detail.scrollTop > 50
}

// 计算当前展示的用户
const currentUser = computed(() => recommendList.value[0])

const showShareModal = ref(false)
const showFilter = ref(false)
const incomeOptions = ['5万以下', '5-10w', '10-20w', '20-30w', '30-40w', '40-50w', '50-60w', '60-80w', '80-100w', '100w以上']

// 模拟省市数据
const cityData: Record<string, string[]> = {
  '北京': ['北京市'],
  '上海': ['上海市'],
  '天津': ['天津市'],
  '重庆': ['重庆市'],
  '广东': ['广州市', '深圳市', '珠海市', '汕头市', '东莞市', '佛山市'],
  '江苏': ['南京市', '苏州市', '无锡市', '常州市', '南通市', '徐州市'],
  '浙江': ['杭州市', '宁波市', '温州市', '嘉兴市', '绍兴市', '金华市'],
  '山东': ['济南市', '青岛市', '烟台市', '威海市', '潍坊市', '淄博市'],
  '四川': ['成都市', '绵阳市', '德阳市', '宜宾市', '南充市'],
  '湖北': ['武汉市', '宜昌市', '襄阳市', '荆州市'],
  '湖南': ['长沙市', '株洲市', '湘潭市', '衡阳市'],
  '河南': ['郑州市', '青岛市', '洛阳市', '南阳市', '新乡市'],
  '河北': ['石家庄市', '唐山市', '保定市', '邯郸市'],
  '安徽': ['合肥市', '芜湖市', '蚌埠市', '马鞍山市'],
  '福建': ['福州市', '厦门市', '泉州市', '莆田市'],
  '江西': ['南昌市', '赣州市', '九江市', '宜春市'],
  '辽宁': ['沈阳市', '大连市', '大连市', '鞍山市'],
  '吉林': ['长春市', '吉林市', '四平市'],
  '黑龙江': ['哈尔滨市', '齐齐哈尔市', '大庆市'],
  '陕西': ['西安市', '宝鸡市', '咸阳市'],
  '山西': ['太原市', '大同市', '长治市'],
  '云南': ['昆明市', '曲靖市', '玉溪市'],
  '贵州': ['贵阳市', '遵义市', '安顺市'],
  '广西': ['南宁市', '柳州市', '桂林市'],
  '海南': ['海口市', '三亚市', '三沙市'],
  '内蒙古': ['呼和浩特市', '包头市', '鄂尔多斯市'],
  '宁夏': ['银川市', '柳州市', '吴忠市'],
  '甘肃': ['兰州市', '天水市', '白银市'],
  '青海': ['西宁市', '海东市'],
  '新疆': ['乌鲁木齐市', '克拉玛依市'],
  '西藏': ['拉萨市', '日喀则市'],
  '台湾': ['台北市', '新北市', '高雄市'],
  '香港': ['香港特别行政区'],
  '澳门': ['澳门特别行政区'],
  '海外': ['海外']
}
const provinceList = Object.keys(cityData)

const cityOptions = ref([provinceList, cityData[provinceList[0]]])
const handleCityColumnChange = (e: any) => {
  const { column, value } = e.detail
  if (column === 0) {
    const selectedProvince = provinceList[value]
    cityOptions.value = [provinceList, cityData[selectedProvince] || []]
  }
}

const cityOptions2 = ref([provinceList, cityData[provinceList[0]]])
const handleCityColumnChange2 = (e: any) => {
  const { column, value } = e.detail
  if (column === 0) {
    const selectedProvince = provinceList[value]
    cityOptions2.value = [provinceList, cityData[selectedProvince] || []]
  }
}

const filterForm = ref({
  location: '',
  age: '',
  height: '',
  education: '',
  income: '',
  nativePlace: '',
  household: '',
  maritalStatus: ''
})

const showVipGuide = () => {
  uni.showModal({
    title: '开通VIP',
    content: '开通VIP解锁首页高级筛选',
    confirmText: '去开通',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/vip-recharge/index' })
      }
    }
  })
}

const handleFilter = async () => {
  if (!requireLogin('高级筛选')) return
  try {
    const res: any = await request({ url: '/vip/info', method: 'GET' })
    let vipInfo = res;
    if (res && res.code === 200) {
      vipInfo = res.data;
    }
    
    if (vipInfo && vipInfo.expireAt && new Date(vipInfo.expireAt).getTime() > Date.now()) {
      showFilter.value = true
    } else {
      showVipGuide()
    }
  } catch(e) {
    showVipGuide()
  }
}

const resetFilter = () => {
  filterForm.value = {
    location: '',
    age: '',
    height: '',
    education: '',
    income: '',
    nativePlace: '',
    household: '',
    maritalStatus: ''
  }
}

const applyFilter = () => {
  showFilter.value = false
  uni.showToast({ title: '已应用筛选', icon: 'success' })
  fetchRecommendList()
}

const goUserDetail = (user: any) => {
  if (!requireLogin('查看主页')) return
  uni.navigateTo({ url: `/pages/user-detail/index?id=${user.userId}` })
}

const goUserPosts = (user: any) => {
  if (!requireLogin('查看动态')) return
  uni.navigateTo({ url: `/pages/user-posts/index?userId=${user.userId}` })
}

const changeSortMode = (mode: 'recommend' | 'active' | 'match') => {
  if (mode !== 'recommend' && !requireLogin('查看此列表')) return
  if (sortMode.value === mode) return
  sortMode.value = mode
  currentPhotoIndex.value = 0
  if (mode === 'recommend') {
    fetchRecommendList()
  }
}

const handlePhotoChange = (event: any) => {
  currentPhotoIndex.value = event.detail.current || 0
}

const previewPhoto = (index: number | string) => {
  const idx = Number(index)
  if (currentUser.value && currentUser.value.photos) {
    uni.previewImage({
      urls: currentUser.value.photos,
      current: currentUser.value.photos[idx]
    })
  }
}

const handleShare = () => {
  if (!requireLogin('分享')) return
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
    uni.showToast({ title: '准备分享给微信好友（需接入SDK）', icon: 'none' })
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

onShareAppMessage(() => {
  if (currentUser.value) {
    return {
      title: `为你推荐 ${currentUser.value.nickname} 的个人主页`,
      path: `/pages/user-detail/index?id=${currentUser.value.userId}`,
      imageUrl: currentUser.value.photos && currentUser.value.photos.length > 0 ? currentUser.value.photos[0] : ''
    }
  }
  return {
    title: '红小鸭相亲交友',
    path: '/pages/index/index'
  }
})

onShareTimeline(() => {
  if (currentUser.value) {
    return {
      title: `为你推荐 ${currentUser.value.nickname} 的个人主页`,
      query: `id=${currentUser.value.userId}`,
      imageUrl: currentUser.value.photos && currentUser.value.photos.length > 0 ? currentUser.value.photos[0] : ''
    }
  }
  return {
    title: '红小鸭相亲交友',
    query: ''
  }
})

const fetchRecommendList = async () => {
  try {
    let url = `/home/recommend?sort=${sortMode.value}`
    if (filterForm.value.location && filterForm.value.location !== '不限') url += `&location=${encodeURIComponent(filterForm.value.location)}`
    if (filterForm.value.age) url += `&age=${encodeURIComponent(filterForm.value.age)}`
    if (filterForm.value.height) url += `&height=${encodeURIComponent(filterForm.value.height)}`
    if (filterForm.value.education && filterForm.value.education !== '不限') url += `&education=${encodeURIComponent(filterForm.value.education)}`
    if (filterForm.value.income && filterForm.value.income !== '不限') url += `&income=${encodeURIComponent(filterForm.value.income)}`
    if (filterForm.value.nativePlace && filterForm.value.nativePlace !== '不限') url += `&nativePlace=${encodeURIComponent(filterForm.value.nativePlace)}`
    if (filterForm.value.household && filterForm.value.household !== '不限') url += `&household=${encodeURIComponent(filterForm.value.household)}`
    if (filterForm.value.maritalStatus && filterForm.value.maritalStatus !== '不限') url += `&maritalStatus=${encodeURIComponent(filterForm.value.maritalStatus)}`

    const res = await request({ url, method: 'GET' })
    if (res && res.length > 0) {
      let list = res.map(normalizeRecommendUser)
      
      // 二次过滤，确保只展示异性（前端兜底）
      const myGender = uni.getStorageSync('userGender')
      if (myGender === 1 || myGender === 2) {
        const targetGender = myGender === 1 ? 2 : 1
        list = list.filter((u: any) => u.gender === targetGender)
      }

      // 仅展示完成三重认证（实名认证+学历认证，头像认证默认已完成）的用户
      list = list.filter((u: any) => u.idCard && u.education)

      if (list.length > 0) {
        // 推荐机制：按上线时间优先推荐实时在线人员，其次活跃度、匹配度
        if (sortMode.value === 'recommend') {
          list.sort((a: any, b: any) => {
            const getOnlinePriority = (text: string) => {
              if (!text) return 0
              if (text.includes('正在线')) return 100
              if (text.includes('刚刚')) return 90
              if (text.includes('五分钟前')) return 80
              if (text.includes('半小时前')) return 70
              if (text.includes('小时前')) return 60
              if (text.includes('天前')) return 50
              if (text.includes('个月前')) return 40
              return 30
            }
            const aPriority = getOnlinePriority(a.onlineText)
            const bPriority = getOnlinePriority(b.onlineText)
            
            if (aPriority !== bPriority) {
              return bPriority - aPriority
            }
            // 在线时间相似时，按匹配度排序
            return (b.matchScore || 0) - (a.matchScore || 0)
          })
        }
        
        recommendList.value = list
      } else {
        loadMockData()
      }
    } else {
      loadMockData()
    }
  } catch (e) {
    loadMockData()
  }
}

const normalizePhotos = (item: any) => {
  const gender = item.gender === 1 ? 'male' : 'female'
  const avatar = item.avatar || DEFAULT_AVATAR
  let photos = item.photos

  if (typeof photos === 'string') {
    try {
      photos = JSON.parse(photos)
    } catch (e) {
      photos = []
    }
  }

  const getMockPhotosByGenderSafely = (userId: string | number, gender: 'male' | 'female', count: number) => {
    return getMockPhotosByGender(Number(userId), gender, count)
  }

  if (!Array.isArray(photos) || photos.length === 0) {
    photos = getMockPhotosByGenderSafely(item.userId, gender, 4)
  }

  if (!photos.includes(avatar)) {
    photos = [avatar, ...photos]
  }

  return photos.slice(0, 5)
}

const normalizeRecommendUser = (item: any) => {
  const gender = item.gender === 1 ? 'male' : 'female'
  const avatar = item.avatar || DEFAULT_AVATAR

  // 模拟在线时间和距离、匹配度（如果后端没有返回）
  const mockOnlineTimes = ['正在线', '刚刚(1-5分钟)', '五分钟前', '半小时前', '1小时前', '1天前', '1个月前']
  const onlineText = item.onlineText || mockOnlineTimes[Number(item.userId || 0) % mockOnlineTimes.length] || '刚刚(1-5分钟)'
  const distance = item.distance || `${(Number(item.userId || 0) % 20) + 0.5}km`
  const matchScore = item.matchScore || (80 + (Number(item.userId || 0) % 20))

  return {
    ...item,
    avatar,
    onlineText,
    distance,
    matchScore,
    photos: normalizePhotos({ ...item, avatar })
  }
}

const loadMockData = () => {
  // 获取当前用户性别（假设在本地缓存或默认）
  const myGender = uni.getStorageSync('userGender') || 1 // 默认当前用户为男
  const targetGender = myGender === 1 ? 2 : 1

  const allMockUsers = [
    { userId: 101, nickname: '温柔学姐', age: 24, gender: 2, height: 165, bio: '希望能遇到一个懂我的人，一起看世界。', avatar: getRandomAvatar(101, 'female'), idCard: '110105199001011234', education: '硕士', income: '30-40w', maritalStatus: '未婚', location: '上海' },
    { userId: 103, nickname: '甜心少女', age: 22, gender: 2, height: 160, bio: '喜欢美食、旅游，想找个能一起探索城市的人。', avatar: getRandomAvatar(103, 'female'), idCard: '310101199803031234', education: '本科', income: '20-30w', maritalStatus: '未婚', location: '北京' },
    { userId: 105, nickname: '神秘女孩', age: 23, gender: 2, height: 168, bio: '暂未认证，但我很真实。', avatar: getRandomAvatar(105, 'female'), idCard: '', education: '', income: '10-20w', maritalStatus: '未婚', location: '广州' },
    { userId: 107, nickname: '知性佳人', age: 27, gender: 2, height: 166, bio: '从事设计工作，热爱艺术与生活。', avatar: getRandomAvatar(107, 'female'), idCard: '110105199001011236', education: '本科', income: '20-30w', maritalStatus: '离异', location: '杭州' },
    { userId: 109, nickname: '活泼小仙女', age: 21, gender: 2, height: 158, bio: '刚毕业，喜欢小动物和拍照。', avatar: getRandomAvatar(109, 'female'), idCard: '310101199803031236', education: '大专', income: '10-20w', maritalStatus: '未婚', location: '成都' },
    { userId: 111, nickname: '优雅女士', age: 31, gender: 2, height: 170, bio: '事业小有成就，希望能遇到灵魂契合的伴侣。', avatar: getRandomAvatar(111, 'female'), idCard: '110105199001011238', education: '硕士', income: '50-60w', maritalStatus: '未婚', location: '深圳' },
    { userId: 113, nickname: '文艺女青', age: 25, gender: 2, height: 163, bio: '喜欢音乐节和话剧，周末常泡在书店。', avatar: getRandomAvatar(113, 'female'), idCard: '310101199803031238', education: '本科', income: '10-20w', maritalStatus: '未婚', location: '南京' },
    { userId: 102, nickname: '阳光学长', age: 26, gender: 1, height: 180, bio: '热爱运动和编程，期待一起进步。', avatar: getRandomAvatar(102, 'male'), idCard: '110105199001011235', education: '硕士', income: '30-40w', maritalStatus: '未婚', location: '上海' },
    { userId: 104, nickname: '成熟大叔', age: 30, gender: 1, height: 178, bio: '事业稳定，寻找真诚的另一半。', avatar: getRandomAvatar(104, 'male'), idCard: '310101199803031235', education: '本科', income: '40-50w', maritalStatus: '离异', location: '北京' },
    { userId: 106, nickname: '路过小哥', age: 25, gender: 1, height: 175, bio: '还没有认证呢。', avatar: getRandomAvatar(106, 'male'), idCard: '', education: '', income: '10-20w', maritalStatus: '未婚', location: '深圳' },
    { userId: 108, nickname: '理科暖男', age: 28, gender: 1, height: 176, bio: 'IT从业者，会做饭，情绪稳定。', avatar: getRandomAvatar(108, 'male'), idCard: '110105199001011237', education: '本科', income: '30-40w', maritalStatus: '未婚', location: '杭州' },
    { userId: 110, nickname: '幽默男神', age: 27, gender: 1, height: 182, bio: '喜欢讲段子，带给你每天的好心情。', avatar: getRandomAvatar(110, 'male'), idCard: '310101199803031237', education: '本科', income: '20-30w', maritalStatus: '未婚', location: '广州' },
    { userId: 112, nickname: '精英男士', age: 33, gender: 1, height: 185, bio: '金融行业，喜欢高尔夫和自驾游。', avatar: getRandomAvatar(112, 'male'), idCard: '110105199001011239', education: '硕士', income: '80-100w', maritalStatus: '未婚', location: '上海' },
    { userId: 114, nickname: '运动达人', age: 24, gender: 1, height: 179, bio: '健身教练，身材好，喜欢户外运动。', avatar: getRandomAvatar(114, 'male'), idCard: '310101199803031239', education: '大专', income: '10-20w', maritalStatus: '未婚', location: '成都' }
  ]

  let list = allMockUsers.filter(u => u.gender === targetGender && u.idCard && u.education)

  // 模拟应用前端筛选条件
  if (filterForm.value.location && filterForm.value.location !== '不限') {
    const [prov, city] = filterForm.value.location.split('-')
    if (city) {
      list = list.filter(u => u.location.includes(city) || u.location.includes(prov))
    }
  }
  if (filterForm.value.age) {
    const [min, max] = filterForm.value.age.split('-').map(Number)
    if (min && max) list = list.filter(u => u.age >= min && u.age <= max)
  }
  if (filterForm.value.height) {
    const [min, max] = filterForm.value.height.split('-').map(Number)
    if (min && max) list = list.filter(u => u.height >= min && u.height <= max)
  }
  if (filterForm.value.education && filterForm.value.education !== '不限') {
    const eduLevels = ['大专', '本科', '硕士', '博士']
    const reqLevel = eduLevels.indexOf(filterForm.value.education)
    if (reqLevel !== -1) {
      list = list.filter(u => eduLevels.indexOf(u.education) >= reqLevel)
    }
  }
  if (filterForm.value.maritalStatus && filterForm.value.maritalStatus !== '不限') {
    list = list.filter(u => u.maritalStatus === filterForm.value.maritalStatus)
  }

  recommendList.value = list.map(normalizeRecommendUser)
}

const goVisitorHistory = () => {
  if (!requireLogin('查看浏览记录')) return
  uni.navigateTo({ url: '/pages/visitor-history/index' })
}

const goProfileEdit = () => {
  uni.navigateTo({ url: '/pages/profile-edit/index' })
}

const handleSendRose = async (user: any, event?: any) => {
  if (!(await checkAuth('送玫瑰花'))) return
  try {
    if (event) createFloatingEffect('rose', event)
    await request({
      url: `/home/send-rose?targetUserId=${user.userId}`,
      method: 'POST'
    })
    doSwipe('right', () => {
      currentPhotoIndex.value = 0
      recommendList.value.shift()
    })
  } catch (e: any) {
    // 鸭蛋不足时的提示通常由后端返回特定错误码，如果后端直接抛出错误信息，这里可以捕获
    // 或者在全局 request 拦截器中已经处理。如果需要特定处理，可以在此处增加逻辑。
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

const handleLike = async (user: any, event?: any) => {
  if (!(await checkAuth('喜欢'))) return
  try {
    if (event) createFloatingEffect('like', event)
    await request({
      url: `/home/like?targetUserId=${user.userId}`,
      method: 'POST'
    })
    doSwipe('right', () => {
      currentPhotoIndex.value = 0
      recommendList.value.shift()
    })
  } catch (e) {}
}

const handleDislike = async (user: any) => {
  if (!requireLogin('操作')) return
  try {
    await request({
      url: `/home/dislike?targetUserId=${user.userId}`,
      method: 'POST'
    })
    doSwipe('left', () => {
      currentPhotoIndex.value = 0
      recommendList.value.shift()
    })
  } catch (e) {}
}

const unlockWechat = async (user: any) => {
  if (!(await checkAuth('解锁微信号'))) return
  const cost = user.isVip ? 50 : 100
  uni.showModal({
    title: '解锁微信号',
    content: `查看 ${user.nickname} 的微信号需支付 ${cost} 鸭蛋`,
    confirmColor: '#6A61F8',
    success: async (res) => {
      if (!res.confirm) return
      try {
        const wechatId: any = await request({ url: `/relation/unlock-wechat?targetId=${user.userId}`, method: 'POST' })
        user.wechatUnlocked = true
        user.wechat = wechatId || '微信号未填写'
        uni.showToast({ title: '解锁成功', icon: 'success' })
      } catch (e) {}
    }
  })
}

const copyWechat = (user: any) => {
  if (user.wechat) {
    uni.setClipboardData({
      data: String(user.wechat),
      success: () => {
        uni.showToast({ title: '微信号已复制', icon: 'success' })
      }
    })
  }
}

const reportUser = (user: any) => {
  if (!requireLogin('举报用户')) return
  uni.navigateTo({
    url: `/pages/report/index?targetId=${user.userId}&targetName=${user.nickname}&type=其他举报`
  })
}

const toggleBlock = (user: any) => {
  if (!requireLogin('屏蔽用户')) return
  if (user.isBlocked) {
    uni.showModal({
      title: '解除屏蔽',
      content: `确定要解除对 ${user.nickname} 的屏蔽吗？`,
      confirmColor: '#6A61F8',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ url: `/relation/unblock?targetId=${user.userId}`, method: 'POST' })
          user.isBlocked = false
          uni.showToast({ title: '已解除屏蔽', icon: 'none' })
        } catch (e) {}
      }
    })
  } else {
    uni.showModal({
      title: '屏蔽用户',
      content: `屏蔽后首页不再推荐 ${user.nickname}`,
      confirmColor: '#6A61F8',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ url: `/relation/block?targetId=${user.userId}`, method: 'POST' })
          user.isBlocked = true
          uni.showToast({ title: '已屏蔽', icon: 'none' })
          
          // 新增逻辑：屏蔽后将该用户移出首页展示列表
          doSwipe('left', () => {
            currentPhotoIndex.value = 0
            recommendList.value.shift()
          })
        } catch (e) {}
      }
    })
  }
}

onShow(async () => {
  if (!globalAuthGuard()) return
  
  const loggedIn = !!uni.getStorageSync('token')
  if (loggedIn) {
    isProfileComplete.value = Boolean(uni.getStorageSync('isProfileComplete'))
    
    if (!uni.getStorageSync('userGender') || !isProfileComplete.value) {
      try {
        const profile: any = await request({ url: '/profile/info', method: 'GET' })
        if (profile && profile.gender) {
          uni.setStorageSync('userGender', profile.gender)
        }
        if (profile && profile.nickname && profile.avatar && profile.gender !== -1) {
          uni.setStorageSync('isProfileComplete', true)
          isProfileComplete.value = true
        }
      } catch(e) {}
    }
    
    if (isProfileComplete.value) {
      fetchRecommendList()
    }
  } else {
    // 未登录时允许浏览推荐列表
    isProfileComplete.value = true
    fetchRecommendList()
  }
})
</script>

<style lang="scss" scoped>
.home-container {
  background-color: $uni-bg-color-page;
  height: calc(100vh - var(--window-bottom));
  display: flex;
  flex-direction: column;
  position: relative;
}

.custom-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: transparent;
  transition: background-color 0.3s ease;
}

.custom-nav.nav-scrolled {
  background-color: #FFFFFF;
}

.nav-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36rpx;
}

.nav-left {
  display: flex;
  align-items: center;
  position: relative;
  min-width: 0;
  flex: 1;
}

.sort-tabs {
  display: flex;
  align-items: center;
  gap: 36rpx;
}

.sort-tab {
  position: relative;
  height: 74rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 600;
  color: #8f8da0;
  line-height: 1;
  transition: all 0.2s ease;
}

.sort-tab.active {
  color: $uni-text-color;
}

.sort-tab.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 6rpx;
  width: 36rpx;
  height: 6rpx;
  border-radius: $uni-border-radius-pill;
  background: $uni-color-primary;
  transform: translateX(-50%);
}

.nav-title {
  font-size: 38rpx;
  font-weight: 800;
  color: $uni-text-color;
  margin-right: 30rpx;
  position: relative;
}
.nav-title::after {
  content: '';
  position: absolute;
  bottom: -6rpx;
  left: 10%;
  width: 80%;
  height: 6rpx;
  background-color: $uni-color-primary;
  border-radius: $uni-border-radius-pill;
}

.nav-subtitle {
  font-size: 32rpx;
  color: $uni-text-color-regular;
  margin-right: 30rpx;
  font-weight: 500;
}

.online-dot {
  width: 12rpx;
  height: 12rpx;
  background-color: $uni-color-accent;
  border-radius: 50%;
  position: absolute;
  right: -16rpx;
  top: 4rpx;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}
.action-icon .icon-text {
  font-size: 40rpx;
  color: $uni-text-color;
  font-weight: 500;
}

.share-icon .icon-text {
  font-size: 42rpx;
}

.scroll-view {
  flex: 1;
  height: 0;
  transition: transform 0.3s ease-out, opacity 0.3s ease-out;
}

.scroll-view.swipe-right {
  transform: translateX(100%);
  opacity: 0;
}

.scroll-view.swipe-left {
  transform: translateX(-100%);
  opacity: 0;
}

.scroll-view.swipe-reset {
  transition: none !important;
  transform: translateX(0);
  opacity: 0;
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
  0% {
    transform: translateY(0) scale(0.5) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: translateY(-200rpx) scale(1.2) rotate(-15deg);
    opacity: 0.8;
  }
  100% {
    transform: translateY(-400rpx) scale(1.5) rotate(-30deg);
    opacity: 0;
  }
}

@keyframes floatUp2 {
  0% {
    transform: translateY(0) scale(0.5) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: translateY(-220rpx) scale(1.3) rotate(15deg);
    opacity: 0.8;
  }
  100% {
    transform: translateY(-450rpx) scale(1.6) rotate(30deg);
    opacity: 0;
  }
}

@keyframes floatUp3 {
  0% {
    transform: translateY(0) scale(0.5) rotate(0deg);
    opacity: 1;
  }
  50% {
    transform: translateY(-180rpx) scale(1.1) rotate(5deg);
    opacity: 0.9;
  }
  100% {
    transform: translateY(-380rpx) scale(1.4) rotate(10deg);
    opacity: 0;
  }
}

.float-anim-1 {
  animation: floatUp1 1s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}
.float-anim-2 {
  animation: floatUp2 1s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}
.float-anim-3 {
  animation: floatUp3 1s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}

.header-image-wrapper {
  position: relative;
  width: auto;
  height: 850rpx;
  margin: calc(44px + var(--status-bar-height, 0px) + 20rpx) 20rpx 0 20rpx;
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
  right: calc(30rpx - 10px); /* 再次向右移动5px，总计10px */
  top: calc(48% + 110px); /* 在之前+130px的基础上向上移动20px，总计110px */
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 52rpx; /* 稍微拉开间距，显得不紧凑 */
  z-index: 100;
  pointer-events: none;
}

.action-btn {
  pointer-events: auto; /* 按钮恢复可点击 */
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

.pass-btn {
  width: 100rpx;
  height: 100rpx;
  color: #B2B2C1;
}
.pass-btn .icon { font-size: 58rpx; font-weight: bold; }

.super-btn {
  width: 100rpx;
  height: 100rpx;
}
.super-btn .icon { font-size: 58rpx; font-style: normal; }

.like-btn {
  width: 100rpx;
  height: 100rpx;
}

/* 利用渐变、阴影等 CSS 技巧实现 3D 立体感红心 */
.like-btn .icon { 
  font-size: 58rpx; 
  background: linear-gradient(135deg, #FF416C 0%, #FF4B2B 100%);
  -webkit-background-clip: text;
  color: transparent;
  filter: drop-shadow(0 4rpx 8rpx rgba(255, 65, 108, 0.4));
}

.ergou-default-page {
  flex: 1;
  height: 100%;
  background-color: #F7F8FA;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ergou-bg-decoration {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 0;
  pointer-events: none;
  background: linear-gradient(180deg, #F0F2FF 0%, #F7F8FA 40%);
}

.circle-blur {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
}
.circle-blur.top-left {
  width: 400rpx;
  height: 400rpx;
  background: rgba(106, 97, 248, 0.4);
  top: -100rpx;
  left: -100rpx;
}
.circle-blur.bottom-right {
  width: 500rpx;
  height: 500rpx;
  background: rgba(255, 94, 125, 0.15);
  bottom: 20%;
  right: -150rpx;
}

.ergou-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 260rpx 50rpx 0;
  height: 100%;
}

.mascot-wrapper {
  position: relative;
  margin-bottom: 40rpx;
}

.ergou-mascot {
  width: 200rpx;
  height: 200rpx;
  border-radius: 40rpx;
  box-shadow: 0 20rpx 40rpx rgba(106, 97, 248, 0.15);
  overflow: hidden;
}

.mascot-badge {
  position: absolute;
  top: -10rpx;
  right: -20rpx;
  background: $uni-color-primary;
  color: #FFFFFF;
  font-size: 24rpx;
  font-weight: bold;
  padding: 4rpx 16rpx;
  border-radius: 20rpx 20rpx 20rpx 4rpx;
  box-shadow: 0 4rpx 12rpx rgba(107, 94, 247, 0.3);
}

.text-center {
  text-align: center;
  margin-bottom: 60rpx;
}

.ergou-title {
  font-size: 48rpx;
  font-weight: 800;
  color: #1A1A24;
  display: block;
  margin-bottom: 16rpx;
  letter-spacing: 1rpx;
}

.ergou-subtitle {
  font-size: 28rpx;
  color: #8A8A99;
  display: block;
}

.ergou-feature-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 80rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
}

.feature-item {
  display: flex;
  align-items: center;
}

.feature-divider {
  height: 1rpx;
  background: #F0F0F5;
  margin: 30rpx 0 30rpx 110rpx;
}

.icon-box {
  width: 84rpx;
  height: 84rpx;
  border-radius: 24rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 26rpx;
  flex-shrink: 0;
}
.icon-box text {
  font-size: 44rpx;
}
.icon-box.purple {
  background: #F2F3FF;
  color: #6A61F8;
}
.icon-box.pink {
  background: #FFF0F3;
  color: #FF5E7D;
}
.icon-box.orange {
  background: #FFF5E6;
  color: #FFB03A;
}

.feature-text-box {
  display: flex;
  flex-direction: column;
}
.f-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1A1A24;
  margin-bottom: 8rpx;
}
.f-desc {
  font-size: 24rpx;
  color: #8A8A99;
}

.btn-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ergou-btn {
  width: 100%;
  height: 100rpx;
  border-radius: 100rpx;
  background: linear-gradient(135deg, #9287FF 0%, #6A61F8 100%);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 12rpx 32rpx rgba(106, 97, 248, 0.3);
  transition: all 0.2s;
  margin-bottom: 24rpx;
}
.ergou-btn::after {
  border: none;
}
.ergou-btn:active {
  transform: scale(0.96);
  box-shadow: 0 6rpx 16rpx rgba(106, 97, 248, 0.2);
}
.arrow-icon {
  margin-left: 8rpx;
  font-size: 40rpx;
}

.safe-tip {
  font-size: 22rpx;
  color: #B0B0C0;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.safe-tip .ri-lock-fill {
  font-size: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.empty-logo {
  width: 168rpx;
  height: 168rpx;
  margin-bottom: 30rpx;
  opacity: 0.8;
}

.empty-text {
  color: $uni-text-color-grey;
  font-size: 30rpx;
  margin-bottom: 40rpx;
}

.empty-btn {
  background: $uni-color-primary-gradient;
  color: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 0 60rpx;
  font-size: 28rpx;
  height: 80rpx;
  line-height: 80rpx;
}
.empty-btn::after { border: none; }

/* 筛选面板 */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: $uni-bg-color-mask;
  z-index: 200;
}

.filter-panel {
  position: fixed;
  bottom: var(--window-bottom, 0); left: 0; right: 0;
  background-color: $uni-bg-color;
  border-radius: $uni-border-radius-lg 40rpx 0 0;
  z-index: 201;
  padding-bottom: 20rpx;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

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
  border-top: 1rpx solid #EAEAEA;
}

.filter-panel.show {
  transform: translateY(0);
}

.filter-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0 30rpx;
  font-size: 34rpx;
  font-weight: bold;
}

.vip-badge {
  background: $uni-text-color;
  color: #FFE0B2;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: $uni-border-radius-sm;
  margin-left: 12rpx;
  font-style: italic;
}

.filter-scroll {
  max-height: 60vh;
  padding: 0 40rpx;
  box-sizing: border-box;
}

.filter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36rpx 0;
  border-bottom: 1rpx solid $uni-border-color;
}

.filter-item .label {
  font-size: 30rpx;
  color: $uni-text-color;
  font-weight: 500;
}

.filter-item .input, .filter-item .picker-val {
  font-size: 30rpx;
  text-align: right;
  flex: 1;
}

.ph-color {
  color: $uni-text-color-placeholder;
}

.picker-val {
  flex: 1;
  text-align: right;
  color: #CCC;
}
.picker-val.has-val {
  color: $uni-text-color;
}

.filter-btns {
  display: flex;
  padding: 40rpx;
  gap: 30rpx;
}

.reset-btn {
  flex: 1;
  background-color: $uni-bg-color-grey;
  color: $uni-text-color-grey;
  border-radius: $uni-border-radius-pill;
  font-size: 32rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-weight: bold;
}
.reset-btn::after { border: none; }

.confirm-btn {
  flex: 2;
  background: $uni-color-primary-gradient;
  color: $uni-bg-color;
  border-radius: $uni-border-radius-pill;
  font-size: 32rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-weight: bold;
  box-shadow: $uni-shadow-primary;
}
.confirm-btn::after { border: none; }
</style>
