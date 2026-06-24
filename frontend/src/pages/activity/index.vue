<template>
  <view class="activity-container">
    <view class="custom-nav-spacer" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-left">
        <text class="page-title">相亲</text>
        <view class="filter-wrap" @click="handleFilter">
          <text class="ri-map-pin-line icon-btn"></text>
          <text class="filter-text">{{ currentFilter === '全部' ? '全部' : (currentFilter === '同城' ? userCity || '定位中' : currentFilter) }}</text>
        </view>
      </view>
      <view class="header-right">
        <view class="enrolled-btn" @click="goActivityJoined">
          <text class="ri-calendar-check-line enrolled-icon"></text>
          <text class="enrolled-text">已报名</text>
        </view>
        <text class="ri-search-line icon-btn" @click="toggleSearch"></text>
      </view>
    </view>

    <!-- 搜索框区域 -->
    <view class="search-bar-wrap" v-if="showSearch">
      <view class="search-bar">
        <text class="ri-search-line search-icon"></text>
        <input type="text" v-model="searchQuery" placeholder="搜索活动名称或红娘" class="search-input" />
        <text class="ri-close-circle-fill clear-icon" v-if="searchQuery" @click="searchQuery = ''"></text>
      </view>
    </view>

    <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
      <view class="tabs-container">
        <view class="tab-item" 
              v-for="(tab, index) in tabs" 
              :key="index"
              :class="{ active: currentTab === index }"
              @click="currentTab = index">
          <text class="tab-text">{{ tab }}</text>
          <view class="active-line" v-if="currentTab === index"></view>
        </view>
      </view>
    </scroll-view>

    <scroll-view 
      scroll-y 
      class="activity-list"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <template v-if="filteredActivities.length > 0">
        <view class="activity-card" v-for="act in filteredActivities" :key="act.id">
          <!-- Cover Image -->
          <view class="card-cover-wrap">
            <image class="card-cover" :src="act.coverImg || act.cover || '/static/logo.png'" mode="aspectFill"></image>
            <view class="status-badge" :class="'status-' + act.status">
              {{ getStatusText(act.status) }}
            </view>
          </view>

          <!-- Matchmaker Info -->
          <view class="matchmaker-info">
            <image class="mm-avatar" :src="act.matchmaker?.avatar || '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg'" mode="aspectFill"></image>
            <text class="mm-name">{{ act.matchmaker?.name || '资深红娘' }}ID{{ act.matchmaker?.id || act.creatorId || '123456' }}</text>
            <view class="joined-pill" @click="showJoinedUsers(act)">
              已报{{ act.joinedCount || 0 }}
              <text class="ri-arrow-right-s-line"></text>
            </view>
          </view>

          <view class="act-title">{{ act.name || '神秘活动' }}</view>

          <!-- Tags -->
          <view class="tags-row" v-if="act.tags && act.tags.length > 0">
            <view class="tag-item" v-for="(tag, idx) in act.tags" :key="idx">{{ tag }}</view>
          </view>

          <!-- Enrollment Conditions -->
          <view class="conditions-box">
            <view class="condition-content">
              <text class="condition-text" :class="{ 'expanded': act.isExpanded }">
                <text class="condition-label">报名条件：</text>{{ act.conditions || act.requirements || '京户京房专场/体制内专场/硕博专场，有京房/户|名校| 硕博学历 | 互联网、金融、国央企、名企精英 | 年收入30W+' }}
              </text>
              <view class="toggle-btn-wrap" :class="{ 'expanded': act.isExpanded }" @click.stop="toggleExpand(act)" v-if="getConditionLength(act) > 40">
                <text class="toggle-btn">{{ act.isExpanded ? '收起' : '展开' }}</text>
                <text :class="act.isExpanded ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'" class="toggle-icon"></text>
              </view>
            </view>
          </view>

          <view class="meta">
            <view class="meta-item">
              <text class="meta-icon ri-time-line"></text>
              <text class="meta-text">{{ formatTime(act.startTime) }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon ri-money-cny-circle-line"></text>
              <text class="meta-text price">¥{{ act.price }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon ri-team-line"></text>
              <text class="meta-text">男 {{ act.maleJoined || 0 }}/{{ act.maleQuota }} · 女 {{ act.femaleJoined || 0 }}/{{ act.femaleQuota }}</text>
            </view>
          </view>

          <view class="progress-row">
            <progress :percent="getPercent(act)" activeColor="#6B5EF7" stroke-width="4" border-radius="4" />
            <text class="percent">{{ getPercent(act) }}%</text>
          </view>

          <button
            v-if="act.status === 1"
            class="primary-btn enter-btn"
            @click="handleEnterRoom(act)"
          >进入房间</button>
          <button
            v-else-if="act.status === 0"
            class="primary-btn"
            @click="handleJoin(act)"
          >立即报名</button>
        </view>
        <view class="loading-more" v-if="isLoadingMore">
          <text>加载中...</text>
        </view>
      </template>

      <!-- Empty State -->
      <view class="empty-state" v-else>
        <text class="ri-calendar-event-line empty-icon"></text>
        <text class="empty-text">暂无相关活动</text>
      </view>
    </scroll-view>

    <!-- 自定义筛选弹窗 -->
    <view class="action-sheet-mask" v-if="showFilterPopup" @click="showFilterPopup = false"></view>
    <view class="filter-popup-panel" :class="{'show': showFilterPopup}">
      <view class="filter-popup-header">
        <text class="title">选择城市</text>
        <text class="ri-close-line close-icon" @click="showFilterPopup = false"></text>
      </view>
      
      <scroll-view scroll-y class="filter-popup-content">
        <view class="filter-section">
          <view class="filter-grid">
            <view class="filter-btn" :class="{'active': currentFilter === '全部'}" @click="selectFilter('全部')">全部</view>
            <view class="filter-btn" :class="{'active': currentFilter === '同城'}" @click="selectFilter('同城')">同城</view>
          </view>
        </view>
        
        <view class="filter-section">
          <view class="section-title">热门城市 (一线)</view>
          <view class="filter-grid">
            <view class="filter-btn" 
                  v-for="city in ['北京', '上海', '广州', '深圳']" 
                  :key="city"
                  :class="{'active': currentFilter === city}"
                  @click="selectFilter(city)">
              {{ city }}
            </view>
          </view>
        </view>
        
        <view class="filter-section">
          <view class="section-title">新一线城市</view>
          <view class="filter-grid">
            <view class="filter-btn" 
                  v-for="city in ['成都', '重庆', '杭州', '武汉', '西安', '郑州', '青岛', '长沙', '天津', '苏州', '南京', '东莞', '沈阳', '合肥', '佛山']" 
                  :key="city"
                  :class="{'active': currentFilter === city}"
                  @click="selectFilter(city)">
              {{ city }}
            </view>
          </view>
        </view>

        <view class="filter-section">
          <view class="section-title">二线城市</view>
          <view class="filter-grid">
            <view class="filter-btn" 
                  v-for="city in ['昆明', '福州', '无锡', '厦门', '哈尔滨', '长春', '南昌', '济南', '宁波', '大连', '贵阳', '温州', '石家庄', '泉州', '南宁', '金华', '常州', '珠海', '惠州', '嘉兴', '南通', '中山', '保定', '兰州', '台州', '徐州', '太原', '绍兴', '烟台', '廊坊']" 
                  :key="city"
                  :class="{'active': currentFilter === city}"
                  @click="selectFilter(city)">
              {{ city }}
            </view>
          </view>
        </view>
        
        <view class="filter-section">
          <view class="section-title">三四五线城市</view>
          <view class="filter-grid">
            <view class="filter-btn" 
                  v-for="city in ['海口', '三亚', '洛阳', '潍坊', '临沂', '唐山', '扬州', '盐城', '芜湖', '赣州', '九江', '宜昌', '襄阳', '绵阳', '柳州', '威海', '吉林', '大庆', '呼和浩特', '包头', '银川', '西宁', '乌鲁木齐', '拉萨', '桂林', '遵义', '曲靖', '宝鸡', '咸阳', '南阳']" 
                  :key="city"
                  :class="{'active': currentFilter === city}"
                  @click="selectFilter(city)">
              {{ city }}
            </view>
          </view>
        </view>
        <view style="height: 40rpx;"></view>
      </scroll-view>
    </view>

      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { request } from '../../utils/request'
import { onShow } from '@dcloudio/uni-app'
import { checkAuth, globalAuthGuard, requireLogin } from '../../utils/auth'

const tabs = ['即将开始', '正在进行']
const currentTab = ref(0)
const activityList = ref<any[]>([])
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0
const searchQuery = ref('')
const isRefreshing = ref(false)
const isLoadingMore = ref(false)



const showSearch = ref(false)
const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (!showSearch.value) searchQuery.value = ''
}

// 模拟当前用户的城市 (可从用户资料中获取)
const userCity = ref('')

const getUserCity = async () => {
  const loggedIn = !!uni.getStorageSync('token')
  if (loggedIn) {
    try {
      const myProfile: any = await request({ url: '/profile/info', method: 'GET' })
      if (myProfile?.location) {
        userCity.value = myProfile.location
      } else {
        userCity.value = '北京'
      }
    } catch (e) {
      console.error('获取用户资料失败', e)
      userCity.value = '北京'
    }
  } else {
    userCity.value = '北京'
  }
}

const getMockActivities = () => {
    return [
      {
        id: 1,
        name: '周末剧本杀交友派对',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
        status: 1,
        matchmaker: { id: 10001, avatar: '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg', name: '资深红娘-小雅' },
        joinedCount: 12,
        city: '上海',
        tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 99,
        maleJoined: 6,
        maleQuota: 10,
        femaleJoined: 6,
        femaleQuota: 10,
        conditions: '京户京房专场/体制内专场/硕博专场，有京房/户|名校| 硕博学历 | 互联网、金融、国央企、名企精英 | 年收入30W+',
        requirements: '1. 男士需本科以上学历，年收入20w+\n2. 女士需90后，形象气质佳\n3. 报名需上传实名认证信息'
      },
      {
        id: 2,
        name: '硕博高学历精英见面会',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A802.jpg',
        status: 1,
        matchmaker: { avatar: '/static/avatars/stefan-stefancik-QXevDflbl8A-unsplash.jpg', name: '红娘老师-张姐' },
        joinedCount: 15,
        city: '上海',
        tags: ['上海', '高学历', '精英场'],
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 199,
        maleJoined: 8,
        maleQuota: 15,
        femaleJoined: 7,
        femaleQuota: 15,
        conditions: '名校硕博学历 | 互联网、金融、国央企精英 | 年收入40W+',
        requirements: '1. 必须拥有硕士及以上学历\n2. 需在上海有稳定工作\n3. 诚实守信，真诚脱单'
      },
      {
        id: 3,
        name: '京户京房专场相亲派对',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A803.jpg',
        status: 1,
        matchmaker: { avatar: '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg', name: '资深红娘-小雅' },
        joinedCount: 30,
        city: '北京',
        tags: ['北京', '户京房专场', '体制内专场', '硕博专场'],
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 128,
        maleJoined: 15,
        maleQuota: 20,
        femaleJoined: 15,
        femaleQuota: 20,
        conditions: '京户京房专场，有京房/户 | 体制内工作 | 年收入30W+',
        requirements: '1. 年龄在25-35岁之间\n2. 诚心交友，拒绝婚托\n3. 现场需配合红娘老师互动'
      },
      {
        id: 4,
        name: '上海单身白领互助晚宴',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A804.jpg',
        status: 0,
        matchmaker: { avatar: '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg', name: '资深红娘-小雅' },
        joinedCount: 11,
        city: '北京',
        tags: ['北京', '户京房专场', '体制内专场', '硕博专场'],
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 168,
        maleJoined: 3,
        maleQuota: 12,
        femaleJoined: 8,
        femaleQuota: 12,
        requirements: '1. 仅限在沪工作的白领阶层\n2. 需提供工作证或名片\n3. 穿着得体，准时参加'
      },
      {
        id: 5,
        name: '冬日围炉煮茶',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
        status: 2,
        matchmaker: { avatar: '/static/avatars/stefan-stefancik-QXevDflbl8A-unsplash.jpg', name: '温婉红娘·赵姐' },
        joinedCount: 20,
        city: '广州',
        tags: ['广州', '户京房专场', '体制内专场', '硕博专场'],
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 88,
        maleJoined: 10,
        maleQuota: 10,
        femaleJoined: 10,
        femaleQuota: 10,
        requirements: '1. 喜欢茶文化或户外活动\n2. 性格开朗，乐于分享\n3. 活动已结束，仅供回顾'
      },
      {
        id: 6,
        name: '城市周边露营交友',
        status: 0,
        city: '杭州',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
        tags: ['杭州', '户外露营', '周末放松'],
        matchmaker: {
          name: '户外达人·阿强',
          avatar: '/static/avatars/charlie-green-3JmfENcL24M-unsplash.jpg'
        },
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 158,
        maleQuota: 20,
        femaleQuota: 20,
        maleJoined: 15,
        femaleJoined: 12,
        joinedCount: 27
      },
      {
        id: 7,
        name: '8分钟心动相亲速配',
        status: 1,
        city: '广州',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A802.jpg',
        tags: ['广州', '高效速配', '真实社交'],
        matchmaker: {
          name: '王牌红娘·陈阿姨',
          avatar: '/static/avatars/ayo-ogunseinde-6W4F62sN_yI-unsplash.jpg'
        },
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 69,
        maleQuota: 30,
        femaleQuota: 30,
        maleJoined: 28,
        femaleJoined: 30,
        joinedCount: 58
      },
      {
        id: 8,
        name: '宠物主题单身派对',
        status: 0,
        city: '深圳',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A803.jpg',
        tags: ['深圳', '萌宠相伴', '爱心交友'],
        matchmaker: {
          name: '爱心红娘·喵喵',
          avatar: '/static/avatars/gabriel-silverio-K_b41GaWC5Y-unsplash.jpg'
        },
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 128,
        maleQuota: 15,
        femaleQuota: 15,
        maleJoined: 8,
        femaleJoined: 14,
        joinedCount: 22
      },
      {
        id: 9,
        name: '大厂青年专场相亲',
        status: 0,
        city: '北京',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A804.jpg',
        tags: ['北京', '名企单身', '高知圈层'],
        matchmaker: {
          name: '资深红娘·张姐',
          avatar: '/static/avatars/aiony-haust-3TLl_97HNJo-unsplash.jpg'
        },
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 99,
        maleQuota: 40,
        femaleQuota: 40,
        maleJoined: 38,
        femaleJoined: 32,
        joinedCount: 70
      },
      {
        id: 10,
        name: '音乐节看展同城聚会',
        status: 2,
        city: '成都',
        coverImg: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
        tags: ['成都', '文艺青年', '音乐现场'],
        matchmaker: {
          name: '潮流红娘·Kiki',
          avatar: '/static/avatars/christina-wocintechchat-com-m-0Zx1bDv5BNY-unsplash.jpg'
        },
        startTime: new Date('2026/08/10 20:00:00').getTime(),
        price: 188,
        maleQuota: 25,
        femaleQuota: 25,
        maleJoined: 25,
        femaleJoined: 25,
        joinedCount: 50
      }
    ]
}

const showJoinedUsers = async (act: any) => {
  if (!(await checkAuth('查看报名列表'))) return
  uni.showModal({
    title: '报名列表',
    content: '这里仅展示小头像，查看会员主页需扣除 20 鸭蛋',
    confirmText: '去看看',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/activity-joined/index' })
      }
    }
  })
}

const handleEnterRoom = async (act: any) => {
  if (!(await checkAuth('进入相亲房间'))) return
  uni.navigateTo({ url: '/pages/activity-room/index?id=' + act.id })
}

const formatTime = (t: number) => {
  if (!t) return ''
  const d = new Date(t)
  return `开始时间 ${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}

const getPercent = (act: any) => {
  const total = Number(act?.maleQuota || 0) + Number(act?.femaleQuota || 0)
  const joined = Number(act?.joinedCount || 0)
  if (!total) return 0
  const p = Math.round((joined / total) * 100)
  if (p < 0) return 0
  if (p > 100) return 100
  return p
}

const getStatusText = (status: number) => {
  if (status === 1) return '正在进行'
  if (status === 0) return '即将开始'
  if (status === 2) return '已结束'
  return '未知状态'
}

const getConditionLength = (act: any) => {
  const text = act.conditions || act.requirements || '京户京房专场/体制内专场/硕博专场，有京房/户|名校| 硕博学历 | 互联网、金融、国央企、名企精英 | 年收入30W+'
  return text.length
}

const toggleExpand = (act: any) => {
  act.isExpanded = !act.isExpanded
}

const currentFilter = ref('同城')

const filteredActivities = computed(() => {
  let list = activityList.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(a => 
      (a.name && a.name.toLowerCase().includes(query)) || 
      (a.matchmaker && a.matchmaker.name && a.matchmaker.name.toLowerCase().includes(query))
    )
  }

  // 筛选逻辑
  if (currentFilter.value === '同城') {
    list = list.filter(a => userCity.value && a.city && (userCity.value.includes(a.city) || a.city.includes(userCity.value)))
  } else if (currentFilter.value !== '全部') {
    // 选了具体城市
    list = list.filter(a => a.city && a.city.includes(currentFilter.value))
  }

  // 按状态过滤
  if (tabs[currentTab.value] === '正在进行') list = list.filter(a => a?.status === 1)
  else if (tabs[currentTab.value] === '即将开始') list = list.filter(a => a?.status === 0)

  // 排序：优先推荐同城活动
  list.sort((a, b) => {
    const aIsLocal = (userCity.value && a.city && (userCity.value.includes(a.city) || a.city.includes(userCity.value))) ? 1 : 0
    const bIsLocal = (userCity.value && b.city && (userCity.value.includes(b.city) || b.city.includes(userCity.value))) ? 1 : 0
    if (aIsLocal !== bIsLocal) {
      return bIsLocal - aIsLocal // 同城优先排在前面
    }
    return 0 // 其他维持原顺序或按时间等其他条件排序
  })

  return list
})

const fetchActivities = async () => {
  try {
    const res = await request({ url: '/activity/list', method: 'GET' })
    if (res && res.length > 0) {
      activityList.value = res
    } else {
      activityList.value = getMockActivities()
    }
  } catch (e) {
    activityList.value = getMockActivities()
  }
}

const showFilterPopup = ref(false)

const selectFilter = (val: string) => {
  currentFilter.value = val
  showFilterPopup.value = false
  uni.showToast({ title: `已切换至：${val === '同城' ? userCity.value || '同城' : val}`, icon: 'none' })
}

const handleFilter = () => {
  if (!requireLogin('筛选活动')) return
  showFilterPopup.value = true
}

const goHistory = () => {
  if (!requireLogin('查看往期活动')) return
  uni.navigateTo({ url: '/pages/activity-history/index' })
}

const handleJoin = async (act: any) => {
  if (!(await checkAuth('报名相亲活动'))) return
  uni.navigateTo({ url: `/pages/activity-apply/index?id=${act.id}` })
}

const goActivityJoined = () => {
  if (!requireLogin('查看已报名活动')) return
  uni.navigateTo({ url: '/pages/activity-joined/index' })
}

const onRefresh = async () => {
  isRefreshing.value = true
  await fetchActivities()
  setTimeout(() => {
    isRefreshing.value = false
  }, 500)
}

const onLoadMore = () => {
  if (isLoadingMore.value) return
  isLoadingMore.value = true
  setTimeout(() => {
    isLoadingMore.value = false
    uni.showToast({ title: '没有更多活动了', icon: 'none' })
  }, 1000)
}

onShow(async () => {
  if (!globalAuthGuard()) return
  await getUserCity()
  fetchActivities()
})
</script>

<style lang="scss" scoped>
.activity-container {
  background-color: $uni-bg-color-page;
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
  padding: 20rpx 32rpx;
  background-color: $uni-bg-color;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.page-title {
  font-size: 48rpx;
  font-weight: 600;
  color: $uni-text-color;
  letter-spacing: 1rpx;
}

.filter-wrap {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 16rpx;
  background: #f5f6f8;
  border-radius: 24rpx;
  
  .icon-btn {
    font-size: 32rpx;
    color: $uni-text-color-regular;
  }
  
  .filter-text {
    font-size: 26rpx;
    color: $uni-text-color-regular;
  }
}

.enrolled-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 16rpx;
  background: #f5f6f8;
  border-radius: 24rpx;

  .enrolled-icon {
    font-size: 32rpx;
    color: $uni-text-color-regular;
  }

  .enrolled-text {
    font-size: 26rpx;
    color: $uni-text-color-regular;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 32rpx;
  
  .icon-btn {
    font-size: 44rpx;
    color: $uni-text-color-regular;
    font-weight: 500;
  }
}

/* 搜索框区域 */
.search-bar-wrap {
  padding: 0 32rpx 20rpx;
  background-color: $uni-bg-color;
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: $uni-bg-color-page;
  height: 72rpx;
  border-radius: $uni-border-radius-pill;
  padding: 0 24rpx;
  
  .search-icon {
    font-size: 32rpx;
    color: $uni-text-color-placeholder;
    margin-right: 12rpx;
  }
  
  .search-input {
    flex: 1;
    font-size: 28rpx;
    height: 100%;
  }
  
  .clear-icon {
    font-size: 32rpx;
    color: $uni-text-color-placeholder;
    margin-left: 12rpx;
  }
}

/* Tabs */
.tabs-scroll {
  width: 100%;
  white-space: nowrap;
  padding: 10rpx 0 20rpx;
  border-bottom: 1px solid rgba(0,0,0,0.02);
  background-color: $uni-bg-color;
}

.tabs-container {
  display: inline-flex;
  padding: 0 32rpx;
  gap: 48rpx;
}

.tab-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 12rpx;
  
  .tab-text {
    font-size: 30rpx;
    color: $uni-text-color-grey;
    transition: all 0.3s;
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

.activity-list {
  flex: 1;
  height: 0;
  padding-top: 24rpx;
  padding-bottom: 24rpx;
}

.activity-card {
  background-color: $uni-bg-color;
  border-radius: $uni-border-radius-lg;
  padding: 24rpx;
  margin: 0 24rpx 18rpx;
  box-shadow: $uni-shadow-base;
  display: flex;
  flex-direction: column;
}

.card-cover-wrap {
  position: relative;
  width: 100%;
  height: 280rpx;
  border-radius: $uni-border-radius-base;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 22rpx;
  font-weight: bold;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.status-1 {
  background-color: $uni-color-accent;
}

.status-0 {
  background-color: $uni-color-primary;
}

.status-2 {
  background-color: $uni-text-color-grey;
}

.matchmaker-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.mm-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.mm-name {
  font-size: 26rpx;
  color: $uni-text-color-regular;
  flex: 1;
}

.joined-pill {
  background-color: $uni-bg-color-page;
  padding: 8rpx 16rpx;
  border-radius: $uni-border-radius-pill;
  color: $uni-text-color-regular;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.act-title {
  font-size: 32rpx;
  font-weight: 800;
  color: $uni-text-color;
  margin-bottom: 16rpx;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tag-item {
  font-size: 22rpx;
  color: $uni-color-primary;
  background-color: $uni-color-primary-light;
  padding: 4rpx 16rpx;
  border-radius: $uni-border-radius-sm;
}

.conditions-box {
  margin-bottom: 20rpx;
  padding: 0 4rpx;
}

.condition-label {
  font-size: 26rpx;
  font-weight: bold;
  color: $uni-text-color;
}

.condition-content {
  position: relative;
  margin-top: 6rpx;
  line-height: 40rpx;
}

.condition-text {
  font-size: 26rpx;
  color: $uni-text-color-regular;
  line-height: 40rpx;
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

.meta {
  background-color: $uni-bg-color-page;
  border-radius: $uni-border-radius-base;
  padding: 16rpx 18rpx;
  margin-bottom: 18rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: $uni-text-color-regular;
}

.meta-item + .meta-item {
  margin-top: 12rpx;
}

.meta-icon {
  width: 44rpx;
  color: $uni-text-color-grey;
  font-size: 28rpx;
}

.meta-text {
  flex: 1;
  color: $uni-text-color-regular;
}

.price {
  color: $uni-color-primary;
  font-weight: 800;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 18rpx;
  
  progress, uni-progress {
    flex: 1;
  }
}

.percent {
  font-size: 22rpx;
  color: $uni-text-color-grey;
  width: 72rpx;
  text-align: right;
}

.primary-btn {
  width: 100%;
  margin: 0;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: $uni-border-radius-pill;
  font-size: 28rpx;
  font-weight: 800;
  background: $uni-color-primary-gradient;
  color: $uni-bg-color;
  box-shadow: $uni-shadow-primary;
}

.primary-btn::after {
  border: none;
}

.enter-btn {
  background: $uni-color-primary-gradient;
  box-shadow: $uni-shadow-primary;
}

.ended-btn {
  background: $uni-text-color-disable;
  box-shadow: none;
  color: #fff;
}



.loading-more {
  text-align: center;
  padding: 24rpx;
  color: $uni-text-color-grey;
  font-size: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  color: $uni-text-color-disable;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: $uni-text-color-grey;
}

/* 自定义筛选弹窗 */
.action-sheet-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 999;
}

.filter-popup-panel {
  position: fixed;
  bottom: -100%;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 1000;
  border-radius: 32rpx 32rpx 0 0;
  transition: bottom 0.3s ease-out;
  padding-bottom: env(safe-area-inset-bottom);
  max-height: 80vh;
  display: flex;
  flex-direction: column;

  &.show {
    bottom: 0;
  }
}

.filter-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .close-icon {
    font-size: 40rpx;
    color: #999;
  }
}

.filter-popup-content {
  padding: 30rpx 40rpx;
  max-height: 60vh;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.filter-section {
  margin-bottom: 40rpx;
  
  .section-title {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 24rpx;
    font-weight: 500;
  }
}

.filter-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
}

.filter-btn {
  width: calc((100vw - 80rpx - 48rpx) / 3);
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: #f5f6f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  
  &.active {
    background: rgba(107, 94, 247, 0.1);
    color: #6B5EF7;
    font-weight: bold;
    border: 2rpx solid #6B5EF7;
  }
}


</style>
