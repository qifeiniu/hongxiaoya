import { getRandomAvatar, getMockPhotos, getMockPhotosByGender, DEFAULT_AVATAR } from './mockData'

export const BASE_URL = 'http://localhost:8080/api/v1'

const USE_MOCK = true
const MOCK_STATE_KEY = 'hxy_mock_state_v5'

type LedgerItem = {
  id: number
  title: string
  time: string
  amount: number
  rmbAmount?: number
  type: 'income' | 'expense' | 'commission' | 'recharge_rmb'
  expireTime?: string
}

type MockState = {
  registeredPhones: string[]
  profile: Record<string, any>
  wallet: {
    balance: number
    tempBalance: number
    permanentBalance: number
    commissionBalance: number
  }
  ledger: LedgerItem[]
  signInDate: string
  likedUserIds: number[]
  dislikedUserIds: number[]
  unlockedWechatIds: number[]
  blockedUserIds: number[]
  vipExpireAt: string
  activities: Record<string, any>[]
  posts: Record<string, any>[]
  bottleUseDate: string
  bottlePickCount: number
  bottleThrowCount: number
}

const pad = (value: number) => String(value).padStart(2, '0')

const formatDateTime = (date = new Date()) => {
  return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatDate = (date = new Date()) => {
  return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()}`
}

const addDays = (days: number) => {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return formatDate(date)
}

const makeLedger = (title: string, amount: number, expireTime?: string, rmbAmount?: number, type?: LedgerItem['type']): LedgerItem => ({
  id: Date.now() + Math.floor(Math.random() * 1000),
  title,
  amount,
  rmbAmount,
  expireTime,
  time: formatDateTime(),
  type: type || (amount >= 0 ? 'income' : 'expense')
})

const createInitialState = (): MockState => ({
  registeredPhones: [],
  profile: {
    userId: 999,
    phone: '',
    nickname: '',
    avatar: '',
    gender: -1,
    birthday: '',
    height: '',
    profession: '',
    bio: '',
    wechatId: '',
    education: '',
    school: '',
    income: '',
    location: '',
    hometown: '',
    household: '',
    weight: '',
    isRealAuth: 0,
    isVip: false,
    isMatchmaker: false
  },
  wallet: {
    balance: 2040,
    tempBalance: 0,
    permanentBalance: 2040,
    commissionBalance: 1000
  },
  ledger: [
    {
      id: 2,
      title: '充值2200个鸭蛋',
      time: '2026.6.13 14:30:00',
      amount: 2200,
      rmbAmount: -198,
      type: 'recharge_rmb'
    },
    {
      id: 3,
      title: '开通12个月会员',
      time: '2026.6.13 15:00:00',
      amount: 0,
      rmbAmount: -128,
      type: 'recharge_rmb'
    },
    {
      id: 4,
      title: '红娘分成佣金',
      time: '2026.6.14 10:00:00',
      amount: 0,
      rmbAmount: 50,
      type: 'commission'
    },
    {
      id: 5,
      title: '送出玫瑰花',
      time: '2026.6.15 20:30:00',
      amount: -20,
      type: 'expense'
    },
    {
      id: 6,
      title: '解锁微信号',
      time: '2026.6.15 21:00:00',
      amount: -50,
      type: 'expense'
    },
    {
      id: 7,
      title: '视频通话 2分钟',
      time: '2026.6.16 10:15:00',
      amount: -60,
      type: 'expense'
    },
    {
      id: 8,
      title: '捞取漂流瓶',
      time: '2026.6.16 14:20:00',
      amount: -10,
      type: 'expense'
    },
    {
      id: 9,
      title: '查看最近访客',
      time: '2026.6.17 09:10:00',
      amount: -20,
      type: 'expense'
    }
  ],
  signInDate: '',
  likedUserIds: [],
  dislikedUserIds: [],
  unlockedWechatIds: [],
  blockedUserIds: [],
  vipExpireAt: '',
  activities: [],
  posts: [],
  bottleUseDate: '',
  bottlePickCount: 0,
  bottleThrowCount: 0
})

const readState = (): MockState => {
  const stored = uni.getStorageSync(MOCK_STATE_KEY)
  if (!stored) return createInitialState()

  try {
    return {
      ...createInitialState(),
      ...JSON.parse(stored)
    }
  } catch (e) {
    return createInitialState()
  }
}

const writeState = (state: MockState) => {
  uni.setStorageSync(MOCK_STATE_KEY, JSON.stringify(state))
}

const syncDerivedState = (state: MockState) => {
  state.wallet.balance = state.wallet.tempBalance + state.wallet.permanentBalance
  state.profile.isVip = Boolean(state.vipExpireAt && new Date(state.vipExpireAt).getTime() > Date.now())
}

const earnEggs = (state: MockState, amount: number, title: string, bucket: 'temp' | 'permanent' = 'temp') => {
  if (bucket === 'permanent') {
    state.wallet.permanentBalance += amount
  } else {
    state.wallet.tempBalance += amount
  }
  state.ledger.unshift(makeLedger(title, amount, bucket === 'temp' ? addDays(30) : undefined))
  syncDerivedState(state)
}

const spendEggs = (state: MockState, amount: number, title: string) => {
  syncDerivedState(state)
  if (state.wallet.balance < amount) {
    throw new Error('鸭蛋余额不足，请先充值')
  }

  const tempSpend = Math.min(state.wallet.tempBalance, amount)
  state.wallet.tempBalance -= tempSpend
  state.wallet.permanentBalance -= amount - tempSpend
  state.ledger.unshift(makeLedger(title, -amount))
  syncDerivedState(state)
}

const normalizeUrl = (url?: string) => {
  const raw = url || ''
  const [path, queryString = ''] = raw.split('?')
  
  const queryObj: Record<string, string> = {}
  if (queryString) {
    queryString.split('&').forEach(pair => {
      const [key, value] = pair.split('=')
      if (key) {
        queryObj[decodeURIComponent(key)] = decodeURIComponent(value || '')
      }
    })
  }

  const query = {
    get: (key: string) => queryObj[key] !== undefined ? queryObj[key] : null
  }
  
  return { raw, path, query }
}

const requireLogin = () => {
  if (!uni.getStorageSync('token')) {
    throw new Error('请先登录后再使用此功能')
  }
}

const mockRecommendUsers = (state: MockState, sortMode: string = 'active') => {
  const users = [
    { userId: 101, nickname: '温柔可人', age: 24, gender: 2, bio: '希望能遇到一个懂我的人，一起看世界。', avatar: getRandomAvatar(101, 'female'), photos: getMockPhotosByGender(101, 'female', 4), idCard: '110105199001011234', education: '硕士', school: '中国科学技术大学', income: '年入30w', maritalStatus: '未婚', location: '上海', hometown: '山西', household: '上海户籍', height: 168, weight: 52, profession: '产品经理', distance: '2.8km', onlineText: '刚刚(1-5分钟)', onlineRank: 1, activeScore: 96, matchScore: 88, trustScore: 94, tags: ['认真找对象', '性格开朗', '喜欢旅行'], family: '独生女，父母事业单位，家庭关系稳定。', hobbies: '徒步、摄影、做饭、周末看展。', loveDeclaration: '希望我们都真诚一点，慢慢了解也可以很浪漫。', idealPartner: '情绪稳定，有责任感，愿意沟通。', expectedLife: '两个人一起把日子过得松弛、有计划。', firstPost: '周末去了滨江散步，风很舒服。' },
    { userId: 102, nickname: '甜心萝莉', age: 22, gender: 2, bio: '喜欢二次元，想找个能陪我一起看漫展的小哥哥。', avatar: getRandomAvatar(102, 'female'), photos: getMockPhotosByGender(102, 'female', 4), idCard: '440106199505055678', education: '本科', school: '华南理工大学', income: '年入15w', maritalStatus: '未婚', location: '广州', hometown: '广东', household: '广州户籍', height: 160, weight: 45, profession: '画师', distance: '12km', onlineText: '刚刚(1-5分钟)', onlineRank: 2, activeScore: 91, matchScore: 93, trustScore: 91, tags: ['二次元', '漫展', '萌妹'], family: '家庭氛围轻松，父母已退休。', hobbies: '画画、看漫、Cosplay。', loveDeclaration: '想找一个能宠我的男孩子。', idealPartner: '高大、体贴、有耐心。', expectedLife: '每天都开开心心的。', firstPost: '今天画了一张超满意的画！' },
    { userId: 103, nickname: '春风十里', age: 26, gender: 2, bio: '真诚、稳定，也保留一点浪漫。', avatar: getRandomAvatar(103, 'female'), photos: getMockPhotosByGender(103, 'female', 4), idCard: '310101199803031234', education: '本科', school: '北京师范大学', income: '年入20w', maritalStatus: '未婚', location: '北京', hometown: '河北', household: '北京户籍', height: 166, weight: 50, profession: '教师', distance: '5.6km', onlineText: '五分钟前', onlineRank: 0, activeScore: 86, matchScore: 97, trustScore: 89, tags: ['温柔稳定', '爱读书', '慢热'], family: '普通三口之家，家庭观念比较强。', hobbies: '读书、音乐、烘焙、短途旅行。', loveDeclaration: '相信好的关系是互相滋养，而不是互相消耗。', idealPartner: '善良、有边界感，愿意认真经营关系。', expectedLife: '安稳但不无聊，平凡也能有很多小期待。', firstPost: '今天读到一句很喜欢的话，爱是长期主义。' },
    { userId: 104, nickname: '知性学姐', age: 27, gender: 2, bio: '喜欢安静的夜晚，仰望星空。', avatar: getRandomAvatar(104, 'female'), photos: getMockPhotosByGender(104, 'female', 4), idCard: '310101199403031234', education: '硕士', school: '清华大学', income: '年入40w', maritalStatus: '未婚', location: '北京', hometown: '北京', household: '北京户籍', height: 168, weight: 50, profession: '算法工程师', distance: '1.2km', onlineText: '半小时前', onlineRank: 3, activeScore: 88, matchScore: 90, trustScore: 95, tags: ['知性', '摄影', '安静'], family: '家庭和睦', hobbies: '看书、旅游', loveDeclaration: '星河璀璨，不如你', idealPartner: '懂我', expectedLife: '一起看星星', firstPost: '今晚月色真美' },
    { userId: 105, nickname: '美食探险家', age: 25, gender: 2, bio: '吃货一枚，目标是吃遍全城。', avatar: getRandomAvatar(105, 'female'), photos: getMockPhotosByGender(105, 'female', 4), idCard: '310101199803031235', education: '本科', school: '复旦大学', income: '年入25w', maritalStatus: '未婚', location: '上海', hometown: '上海', household: '上海户籍', height: 162, weight: 48, profession: '运营', distance: '3.5km', onlineText: '1小时前', onlineRank: 4, activeScore: 95, matchScore: 85, trustScore: 90, tags: ['探店', '吃货', '看电影'], family: '和睦', hobbies: '吃', loveDeclaration: '唯有爱与美食不可辜负', idealPartner: '带我吃', expectedLife: '吃遍世界', firstPost: '今天发现一家超好吃的店' },
    { userId: 106, nickname: '高冷御姐', age: 29, gender: 2, bio: '事业第一，但也期待那个能让我卸下防备的人。', avatar: getRandomAvatar(106, 'female'), photos: getMockPhotosByGender(106, 'female', 4), idCard: '310101199303031234', education: '硕士', school: '浙江大学', income: '年入60w', maritalStatus: '未婚', location: '杭州', hometown: '杭州', household: '杭州户籍', height: 172, weight: 55, profession: 'HRBP', distance: '8km', onlineText: '1天前', onlineRank: 5, activeScore: 82, matchScore: 92, trustScore: 96, tags: ['御姐', '事业心', '独立'], family: '父母退休', hobbies: '健身、高尔夫', loveDeclaration: '强强联手，或者你够温柔', idealPartner: '优秀、自信', expectedLife: '有质感的生活', firstPost: '刚开完会，累并快乐着' },
    { userId: 107, nickname: '晨跑爱好者', age: 27, gender: 2, bio: '每天迎着朝阳奔跑。', avatar: getRandomAvatar(107, 'female'), photos: getMockPhotosByGender(107, 'female', 4), idCard: '310101199603031234', education: '本科', school: '南京大学', income: '年入35w', maritalStatus: '未婚', location: '南京', hometown: '南京', household: '南京户籍', height: 169, weight: 51, profession: 'HR', distance: '4.2km', onlineText: '1个月前', onlineRank: 6, activeScore: 98, matchScore: 89, trustScore: 92, tags: ['自律', '晨跑', '健康'], family: '健康快乐', hobbies: '跑步', loveDeclaration: '一起跑向未来', idealPartner: '阳光', expectedLife: '充满活力', firstPost: '今天又跑了10公里' },
    { userId: 108, nickname: '文艺猫少女', age: 24, gender: 2, bio: '吉他与猫，生活的一半。', avatar: getRandomAvatar(108, 'female'), photos: getMockPhotosByGender(108, 'female', 4), idCard: '310101199703031234', education: '本科', school: '武汉大学', income: '年入18w', maritalStatus: '未婚', location: '武汉', hometown: '武汉', household: '武汉户籍', height: 165, weight: 48, profession: '设计师', distance: '6.5km', onlineText: '正在线', onlineRank: 7, activeScore: 85, matchScore: 94, trustScore: 88, tags: ['吉他', '吸猫', '文艺'], family: '幸福', hobbies: '弹琴', loveDeclaration: '为你弹奏一曲', idealPartner: '懂音乐', expectedLife: '充满诗意', firstPost: '今天猫咪很乖' },
    { userId: 109, nickname: '独立摄影师', age: 28, gender: 2, bio: '记录生活中的每一个瞬间。', avatar: getRandomAvatar(109, 'female'), photos: getMockPhotosByGender(109, 'female', 4), idCard: '310101199503031234', education: '本科', school: '中山大学', income: '年入45w', maritalStatus: '未婚', location: '广州', hometown: '广州', household: '广州户籍', height: 165, weight: 49, profession: '摄影师', distance: '9km', onlineText: '正在线', onlineRank: 8, activeScore: 92, matchScore: 87, trustScore: 93, tags: ['摄影', '旅行', '自由'], family: '开明', hobbies: '拍照', loveDeclaration: '你是我的最佳模特', idealPartner: '配合拍照', expectedLife: '环游世界', firstPost: '新入手的镜头真不错' },
    { userId: 110, nickname: '金融精英', age: 30, gender: 2, bio: '投资自己，也投资未来。', avatar: getRandomAvatar(110, 'female'), photos: getMockPhotosByGender(110, 'female', 4), idCard: '310101199203031234', education: '硕士', school: '上海财经大学', income: '年入80w', maritalStatus: '未婚', location: '上海', hometown: '上海', household: '上海户籍', height: 170, weight: 52, profession: '投行分析师', distance: '2.1km', onlineText: '正在线', onlineRank: 9, activeScore: 89, matchScore: 91, trustScore: 97, tags: ['理财', '健身', '品酒'], family: '父母经商', hobbies: '赚钱', loveDeclaration: '最好的投资是选对人', idealPartner: '独立', expectedLife: '财富自由', firstPost: '今天 market不错' },
    { userId: 201, nickname: '小梨涡', age: 23, gender: 2, bio: '爱笑的女孩运气不会太差。', avatar: getRandomAvatar(201, 'female'), photos: getMockPhotosByGender(201, 'female', 4), education: '本科', school: '武汉大学', income: '年入15w', maritalStatus: '未婚', location: '武汉', height: 165, profession: '小学老师', distance: '3.2km', onlineText: '刚刚(1-5分钟)', onlineRank: 10, activeScore: 92, matchScore: 85, trustScore: 90, tags: ['甜美', '老师', '汉服'], loveDeclaration: '想找一个能陪我一起逛街的人。' },
    { userId: 202, nickname: '浅笑安然', age: 26, gender: 2, bio: '生活平淡，但心怀诗意。', avatar: getRandomAvatar(202, 'female'), photos: getMockPhotosByGender(202, 'female', 4), education: '本科', school: '苏州大学', income: '年入20w', maritalStatus: '未婚', location: '苏州', height: 162, profession: '会计', distance: '1.5km', onlineText: '正在线', onlineRank: 11, activeScore: 88, matchScore: 92, trustScore: 94, tags: ['安静', '刺绣', '江南'], loveDeclaration: '期待一份细水长流的爱情。' },
    { userId: 203, nickname: '墨染青衣', age: 28, gender: 2, bio: '喜欢书香，也喜欢远方。', avatar: getRandomAvatar(203, 'female'), photos: getMockPhotosByGender(203, 'female', 4), education: '硕士', school: '南京大学', income: '年入30w', maritalStatus: '未婚', location: '南京', height: 168, profession: '编辑', distance: '4.8km', onlineText: '五分钟前', onlineRank: 12, activeScore: 85, matchScore: 90, trustScore: 92, tags: ['文艺', '阅读', '古风'], loveDeclaration: '希望能遇到灵魂契合的你。' },
    { userId: 204, nickname: '夏日微风', age: 24, gender: 2, bio: '活在当下，享受每一天。', avatar: getRandomAvatar(204, 'female'), photos: getMockPhotosByGender(204, 'female', 4), education: '本科', school: '厦门大学', income: '年入18w', maritalStatus: '未婚', location: '厦门', height: 166, profession: '导游', distance: '2.3km', onlineText: '刚刚(1-5分钟)', onlineRank: 13, activeScore: 95, matchScore: 88, trustScore: 91, tags: ['阳光', '旅行', '开朗'], loveDeclaration: '带你去看最美的海。' },
    { userId: 205, nickname: '星语心愿', age: 22, gender: 2, bio: '对世界充满好奇，也对自己充满信心。', avatar: getRandomAvatar(205, 'female'), photos: getMockPhotosByGender(205, 'female', 4), education: '本科', school: '四川大学', income: '年入12w', maritalStatus: '未婚', location: '成都', height: 160, profession: '学生', distance: '5.1km', onlineText: '正在线', onlineRank: 14, activeScore: 90, matchScore: 85, trustScore: 88, tags: ['纯真', '美食', '大熊猫'], loveDeclaration: '想和你一起吃火锅。' },
    { userId: 206, nickname: '紫陌红尘', age: 29, gender: 2, bio: '阅尽繁华，归于平淡。', avatar: getRandomAvatar(206, 'female'), photos: getMockPhotosByGender(206, 'female', 4), education: '硕士', school: '中山大学', income: '年入45w', maritalStatus: '未婚', location: '广州', height: 170, profession: '律师', distance: '1.2km', onlineText: '半小时前', onlineRank: 15, activeScore: 82, matchScore: 95, trustScore: 97, tags: ['知性', '干练', '法律'], loveDeclaration: '理性生活，感性爱。' },
    { userId: 207, nickname: '雪落无痕', age: 27, gender: 2, bio: '喜欢冬天的雪，也喜欢温暖的你。', avatar: getRandomAvatar(207, 'female'), photos: getMockPhotosByGender(207, 'female', 4), education: '本科', school: '哈尔滨工业大学', income: '年入25w', maritalStatus: '未婚', location: '哈尔滨', height: 172, profession: '工程师', distance: '10km', onlineText: '正在线', onlineRank: 16, activeScore: 87, matchScore: 89, trustScore: 93, tags: ['高挑', '运动', '滑雪'], loveDeclaration: '在这个寒冷的城市，寻找一丝温暖。' },
    { userId: 208, nickname: '云淡风轻', age: 31, gender: 2, bio: '不争不抢，岁月静好。', avatar: getRandomAvatar(208, 'female'), photos: getMockPhotosByGender(208, 'female', 4), education: '本科', school: '南开大学', income: '年入35w', maritalStatus: '未婚', location: '天津', height: 164, profession: '公务员', distance: '3.7km', onlineText: '半小时前', onlineRank: 17, activeScore: 80, matchScore: 91, trustScore: 95, tags: ['稳重', '茶艺', '插花'], loveDeclaration: '找一个懂我的人，共度余生。' },
    { userId: 209, nickname: '灵动雨蝶', age: 25, gender: 2, bio: '生活就像一场舞会，我要跳出最美的舞姿。', avatar: getRandomAvatar(209, 'female'), photos: getMockPhotosByGender(209, 'female', 4), education: '本科', school: '北京舞蹈学院', income: '年入22w', maritalStatus: '未婚', location: '北京', height: 168, profession: '舞蹈老师', distance: '6.2km', onlineText: '刚刚(1-5分钟)', onlineRank: 18, activeScore: 96, matchScore: 87, trustScore: 90, tags: ['优雅', '舞蹈', '气质'], loveDeclaration: '我的世界因舞动而精彩，你愿意加入吗？' },
    { userId: 210, nickname: '晨曦微露', age: 24, gender: 2, bio: '每天都是新的开始。', avatar: getRandomAvatar(210, 'female'), photos: getMockPhotosByGender(210, 'female', 4), education: '本科', school: '华中师范大学', income: '年入16w', maritalStatus: '未婚', location: '武汉', height: 163, profession: '幼师', distance: '2.8km', onlineText: '正在线', onlineRank: 19, activeScore: 91, matchScore: 86, trustScore: 89, tags: ['爱心', '孩子王', '手工'], loveDeclaration: '想找一个同样温柔的你。' },
    { userId: 211, nickname: '静水流深', age: 30, gender: 2, bio: '内心丰富，外表宁静。', avatar: getRandomAvatar(211, 'female'), photos: getMockPhotosByGender(211, 'female', 4), education: '硕士', school: '西安交通大学', income: '年入40w', maritalStatus: '未婚', location: '西安', height: 165, profession: '研究员', distance: '4.5km', onlineText: '正在线', onlineRank: 20, activeScore: 84, matchScore: 93, trustScore: 96, tags: ['博学', '深沉', '古琴'], loveDeclaration: '期待一次思想的碰撞。' },
    { userId: 212, nickname: '暖心大白', age: 26, gender: 2, bio: '希望能成为你生命中的那一抹亮色。', avatar: getRandomAvatar(212, 'female'), photos: getMockPhotosByGender(212, 'female', 4), education: '本科', school: '重庆医科大学', income: '年入24w', maritalStatus: '未婚', location: '重庆', height: 162, profession: '护士', distance: '1.9km', onlineText: '刚刚(1-5分钟)', onlineRank: 21, activeScore: 93, matchScore: 88, trustScore: 92, tags: ['治愈', '温柔', '美食'], loveDeclaration: '我会照顾好你，也会照顾好我们的生活。' },
    { userId: 213, nickname: '幻影女神', age: 28, gender: 2, bio: '生活是彩色的，我爱这绚烂的世界。', avatar: getRandomAvatar(213, 'female'), photos: getMockPhotosByGender(213, 'female', 4), education: '本科', school: '中国美术学院', income: '年入32w', maritalStatus: '未婚', location: '杭州', height: 170, profession: '插画师', distance: '5.5km', onlineText: '正在线', onlineRank: 22, activeScore: 89, matchScore: 91, trustScore: 93, tags: ['个性', '绘画', '色彩'], loveDeclaration: '画出我们的未来。' },
    { userId: 214, nickname: '冰清玉洁', age: 27, gender: 2, bio: '宁静以致远，淡泊以明志。', avatar: getRandomAvatar(214, 'female'), photos: getMockPhotosByGender(214, 'female', 4), education: '本科', school: '山东大学', income: '年入26w', maritalStatus: '未婚', location: '济南', height: 167, profession: '行政', distance: '3.1km', onlineText: '半小时前', onlineRank: 23, activeScore: 86, matchScore: 90, trustScore: 94, tags: ['端庄', '稳重', '书法'], loveDeclaration: '寻找一份真挚的感情。' },
    { userId: 215, nickname: '梦幻甜心', age: 21, gender: 2, bio: '想做你的小公主。', avatar: getRandomAvatar(215, 'female'), photos: getMockPhotosByGender(215, 'female', 4), education: '大专', school: '上海戏剧学院', income: '年入10w', maritalStatus: '未婚', location: '上海', height: 158, profession: '模特', distance: '8.2km', onlineText: '正在线', onlineRank: 24, activeScore: 97, matchScore: 82, trustScore: 85, tags: ['萝莉', '二次元', '可爱'], loveDeclaration: '你会宠我吗？' },
    { userId: 216, nickname: '知更鸟', age: 25, gender: 2, bio: '唱响生活的每一刻。', avatar: getRandomAvatar(216, 'female'), photos: getMockPhotosByGender(216, 'female', 4), education: '本科', school: '中央音乐学院', income: '年入28w', maritalStatus: '未婚', location: '北京', height: 164, profession: '歌手', distance: '4.2km', onlineText: '刚刚(1-5分钟)', onlineRank: 25, activeScore: 94, matchScore: 89, trustScore: 91, tags: ['声乐', '动听', '开朗'], loveDeclaration: '想唱歌给你听。' },
    { userId: 217, nickname: '流云飒飒', age: 29, gender: 2, bio: '生活要有态度。', avatar: getRandomAvatar(217, 'female'), photos: getMockPhotosByGender(217, 'female', 4), education: '本科', school: '同济大学', income: '年入50w', maritalStatus: '未婚', location: '上海', height: 172, profession: '建筑师', distance: '2.5km', onlineText: '正在线', onlineRank: 26, activeScore: 83, matchScore: 94, trustScore: 96, tags: ['帅气', '独立', '设计'], loveDeclaration: '一起构建我们的家。' },
    { userId: 218, nickname: '糖果色', age: 23, gender: 2, bio: '生活要加点糖。', avatar: getRandomAvatar(218, 'female'), photos: getMockPhotosByGender(218, 'female', 4), education: '本科', school: '华南师范大学', income: '年入14w', maritalStatus: '未婚', location: '广州', height: 161, profession: '策划', distance: '6.8km', onlineText: '刚刚(1-5分钟)', onlineRank: 27, activeScore: 92, matchScore: 84, trustScore: 87, tags: ['活泼', '烘焙', '创意'], loveDeclaration: '每天都甜甜的。' },
    { userId: 219, nickname: '静谧森林', age: 32, gender: 2, bio: '在大自然中寻找宁静。', avatar: getRandomAvatar(219, 'female'), photos: getMockPhotosByGender(219, 'female', 4), education: '硕士', school: '北京林业大学', income: '年入38w', maritalStatus: '未婚', location: '北京', height: 166, profession: '园艺师', distance: '12km', onlineText: '半小时前', onlineRank: 28, activeScore: 78, matchScore: 92, trustScore: 95, tags: ['自然', '植物', '平和'], loveDeclaration: '一起种下一棵树。' },
    { userId: 220, nickname: '灵感火花', age: 26, gender: 2, bio: '创意无限，生活精彩。', avatar: getRandomAvatar(220, 'female'), photos: getMockPhotosByGender(220, 'female', 4), education: '本科', school: '江南大学', income: '年入28w', maritalStatus: '未婚', location: '无锡', height: 164, profession: '广告人', distance: '3.5km', onlineText: '正在线', onlineRank: 29, activeScore: 91, matchScore: 87, trustScore: 90, tags: ['点子多', '有趣', '看展'], loveDeclaration: '让生活不再枯燥。' },
    { userId: 221, nickname: '蓝调小情歌', age: 24, gender: 2, bio: '温柔的旋律，动人的爱情。', avatar: getRandomAvatar(221, 'female'), photos: getMockPhotosByGender(221, 'female', 4), education: '本科', school: '星海音乐学院', income: '年入18w', maritalStatus: '未婚', location: '广州', height: 165, profession: '钢琴老师', distance: '4.1km', onlineText: '刚刚(1-5分钟)', onlineRank: 30, activeScore: 93, matchScore: 86, trustScore: 89, tags: ['气质', '音乐', '浪漫'], loveDeclaration: '期待与你合奏一曲。' },
    { userId: 222, nickname: '极光之旅', age: 27, gender: 2, bio: '追逐梦想，永不止步。', avatar: getRandomAvatar(222, 'female'), photos: getMockPhotosByGender(222, 'female', 4), education: '硕士', school: '四川外国语大学', income: '年入30w', maritalStatus: '未婚', location: '重庆', height: 168, profession: '翻译', distance: '5.2km', onlineText: '正在线', onlineRank: 31, activeScore: 88, matchScore: 91, trustScore: 93, tags: ['语言', '旅行', '坚韧'], loveDeclaration: '带你看遍世界各地的极光。' },
    { userId: 223, nickname: '书画人生', age: 33, gender: 2, bio: '一笔一划，书写精彩。', avatar: getRandomAvatar(223, 'female'), photos: getMockPhotosByGender(223, 'female', 4), education: '本科', school: '西安美术学院', income: '年入42w', maritalStatus: '未婚', location: '西安', height: 166, profession: '画家', distance: '2.9km', onlineText: '五分钟前', onlineRank: 32, activeScore: 75, matchScore: 93, trustScore: 96, tags: ['艺术', '沉静', '国画'], loveDeclaration: '你是我的画中人。' },
    { userId: 224, nickname: '风中百合', age: 28, gender: 2, bio: '坚韧而优雅。', avatar: getRandomAvatar(224, 'female'), photos: getMockPhotosByGender(224, 'female', 4), education: '本科', school: '湖南大学', income: '年入28w', maritalStatus: '未婚', location: '长沙', height: 169, profession: 'HR', distance: '6.1km', onlineText: '正在线', onlineRank: 33, activeScore: 86, matchScore: 89, trustScore: 92, tags: ['知性', '辣妹子', '自律'], loveDeclaration: '在长沙的街头，期待遇见你。' },
    { userId: 225, nickname: '小布丁', age: 22, gender: 2, bio: '软软糯糯，需要保护。', avatar: getRandomAvatar(225, 'female'), photos: getMockPhotosByGender(225, 'female', 4), education: '本科', school: '南京师范大学', income: '年入12w', maritalStatus: '未婚', location: '南京', height: 159, profession: '学生', distance: '3.3km', onlineText: '刚刚(1-5分钟)', onlineRank: 34, activeScore: 95, matchScore: 81, trustScore: 86, tags: ['可爱', '吃货', '呆萌'], loveDeclaration: '你愿意做我的大树吗？' },
    { userId: 226, nickname: '职业女性', age: 34, gender: 2, bio: '独立自强，也要温柔。', avatar: getRandomAvatar(226, 'female'), photos: getMockPhotosByGender(226, 'female', 4), education: '硕士', school: '光华管理学院', income: '年入100w+', maritalStatus: '离异', location: '北京', height: 171, profession: '高管', distance: '1.5km', onlineText: '正在线', onlineRank: 35, activeScore: 81, matchScore: 96, trustScore: 98, tags: ['职场', '干练', '精致'], loveDeclaration: '寻找一个可以依靠的港湾。' },
    { userId: 227, nickname: '古灵精怪', age: 25, gender: 2, bio: '永远有出其不意的点子。', avatar: getRandomAvatar(227, 'female'), photos: getMockPhotosByGender(227, 'female', 4), education: '本科', school: '深圳大学', income: '年入24w', maritalStatus: '未婚', location: '深圳', height: 163, profession: '产品经理', distance: '4.7km', onlineText: '刚刚(1-5分钟)', onlineRank: 36, activeScore: 94, matchScore: 85, trustScore: 88, tags: ['有趣', '数码控', '科技'], loveDeclaration: '让生活充满惊喜。' },
    { userId: 228, nickname: '茉莉花开', age: 26, gender: 2, bio: '清香淡雅，不随波逐流。', avatar: getRandomAvatar(228, 'female'), photos: getMockPhotosByGender(228, 'female', 4), education: '本科', school: '郑州大学', income: '年入18w', maritalStatus: '未婚', location: '郑州', height: 164, profession: '公务员', distance: '5.5km', onlineText: '正在线', onlineRank: 37, activeScore: 89, matchScore: 90, trustScore: 94, tags: ['朴素', '传统', '孝顺'], loveDeclaration: '愿得一人心，白首不相离。' },
    { userId: 229, nickname: '自由之魂', age: 29, gender: 2, bio: '世界那么大，我想去看看。', avatar: getRandomAvatar(229, 'female'), photos: getMockPhotosByGender(229, 'female', 4), education: '本科', school: '云南大学', income: '年入30w', maritalStatus: '未婚', location: '昆明', height: 167, profession: '自由职业', distance: '15km', onlineText: '正在线', onlineRank: 38, activeScore: 85, matchScore: 92, trustScore: 91, tags: ['旅行', '背包客', '独立'], loveDeclaration: '跟我一起去流浪。' },
    { userId: 230, nickname: '清晨的雨', age: 27, gender: 2, bio: '洗净铅华，回归真实。', avatar: getRandomAvatar(230, 'female'), photos: getMockPhotosByGender(230, 'female', 4), education: '硕士', school: '兰州大学', income: '年入22w', maritalStatus: '未婚', location: '兰州', height: 166, profession: '老师', distance: '3.9km', onlineText: '正在线', onlineRank: 39, activeScore: 87, matchScore: 88, trustScore: 93, tags: ['淳朴', '认真', '踏实'], loveDeclaration: '寻找一个真诚的你。' },
    { userId: 231, nickname: '灵感火花2', age: 24, gender: 2, bio: '生活是我的素材。', avatar: getRandomAvatar(231, 'female'), photos: getMockPhotosByGender(231, 'female', 4), education: '本科', school: '北京服装学院', income: '年入18w', maritalStatus: '未婚', location: '北京', height: 165, profession: '服装设计师', distance: '2.2km', onlineText: '刚刚(1-5分钟)', onlineRank: 40, activeScore: 92, matchScore: 86, trustScore: 89, tags: ['时尚', '穿搭', '审美'], loveDeclaration: '为你设计独一无二的衣服。' },
    { userId: 232, nickname: '冰山雪莲', age: 30, gender: 2, bio: '高傲而纯洁。', avatar: getRandomAvatar(232, 'female'), photos: getMockPhotosByGender(232, 'female', 4), education: '博士', school: '中国科学院大学', income: '年入50w', maritalStatus: '未婚', location: '北京', height: 172, profession: '科学家', distance: '6.5km', onlineText: '正在线', onlineRank: 41, activeScore: 72, matchScore: 95, trustScore: 97, tags: ['学霸', '高冷', '智慧'], loveDeclaration: '只为对的人绽放。' },
    { userId: 233, nickname: '小太阳', age: 23, gender: 2, bio: '温暖自己，也温暖他人。', avatar: getRandomAvatar(233, 'female'), photos: getMockPhotosByGender(233, 'female', 4), education: '本科', school: '华中科技大学', income: '年入20w', maritalStatus: '未婚', location: '武汉', height: 163, profession: '运营', distance: '4.4km', onlineText: '刚刚(1-5分钟)', onlineRank: 42, activeScore: 96, matchScore: 84, trustScore: 90, tags: ['正能量', '爱运动', '笑容'], loveDeclaration: '你的世界会有我这颗小太阳。' },
    { userId: 234, nickname: '静谧时光', age: 31, gender: 2, bio: '享受独处的时光。', avatar: getRandomAvatar(234, 'female'), photos: getMockPhotosByGender(234, 'female', 4), education: '本科', school: '四川大学', income: '年入28w', maritalStatus: '未婚', location: '成都', height: 162, profession: '公务员', distance: '3.1km', onlineText: '正在线', onlineRank: 43, activeScore: 79, matchScore: 91, trustScore: 95, tags: ['恬静', '手工', '养生'], loveDeclaration: '寻找一个同样安静的灵魂。' },
    { userId: 235, nickname: '都市丽人', age: 28, gender: 2, bio: '在繁忙的都市中寻找真爱。', avatar: getRandomAvatar(235, 'female'), photos: getMockPhotosByGender(235, 'female', 4), education: '本科', school: '上海外国语大学', income: '年入40w', maritalStatus: '未婚', location: '上海', height: 168, profession: '市场总监', distance: '1.1km', onlineText: '刚刚(1-5分钟)', onlineRank: 44, activeScore: 88, matchScore: 93, trustScore: 96, tags: ['精致', '社交', '红酒'], loveDeclaration: '生活不仅有工作，还有爱情。' },
    { userId: 236, nickname: '星空下的梦', age: 25, gender: 2, bio: '追逐星辰大海。', avatar: getRandomAvatar(236, 'female'), photos: getMockPhotosByGender(236, 'female', 4), education: '本科', school: '吉林大学', income: '年入15w', maritalStatus: '未婚', location: '长春', height: 166, profession: '行政', distance: '8.8km', onlineText: '正在线', onlineRank: 45, activeScore: 91, matchScore: 85, trustScore: 88, tags: ['浪漫', '观星', '温柔'], loveDeclaration: '想和你一起看星星。' },
    { userId: 237, nickname: '活力元气', age: 21, gender: 2, bio: '永远年轻，永远热泪盈眶。', avatar: getRandomAvatar(237, 'female'), photos: getMockPhotosByGender(237, 'female', 4), education: '本科', school: '北京体育大学', income: '年入10w', maritalStatus: '未婚', location: '北京', height: 170, profession: '教练', distance: '12km', onlineText: '正在线', onlineRank: 46, activeScore: 98, matchScore: 80, trustScore: 87, tags: ['腹肌', '阳光', '力量'], loveDeclaration: '一起流汗，一起变强。' },
    { userId: 238, nickname: '文艺之星', age: 26, gender: 2, bio: '爱电影，爱艺术。', avatar: getRandomAvatar(238, 'female'), photos: getMockPhotosByGender(238, 'female', 4), education: '本科', school: '北京电影学院', income: '年入25w', maritalStatus: '未婚', location: '北京', height: 165, profession: '剪辑师', distance: '3.6km', onlineText: '刚刚(1-5分钟)', onlineRank: 47, activeScore: 93, matchScore: 87, trustScore: 90, tags: ['电影', '审美', '有趣'], loveDeclaration: '我们的生活就是一部电影。' },
    { userId: 239, nickname: '温柔刀', age: 29, gender: 2, bio: '外柔内刚。', avatar: getRandomAvatar(239, 'female'), photos: getMockPhotosByGender(239, 'female', 4), education: '硕士', school: '武汉大学', income: '年入45w', maritalStatus: '未婚', location: '深圳', height: 167, profession: '分析师', distance: '2.4km', onlineText: '正在线', onlineRank: 48, activeScore: 84, matchScore: 94, trustScore: 96, tags: ['果敢', '温柔', '理智'], loveDeclaration: '寻找一个势均力敌的你。' },
    { userId: 240, nickname: '小确幸', age: 24, gender: 2, bio: '寻找生活中的小惊喜。', avatar: getRandomAvatar(240, 'female'), photos: getMockPhotosByGender(240, 'female', 4), education: '本科', school: '厦门大学', income: '年入16w', maritalStatus: '未婚', location: '厦门', height: 161, profession: 'HR', distance: '4.5km', onlineText: '刚刚(1-5分钟)', onlineRank: 49, activeScore: 90, matchScore: 86, trustScore: 89, tags: ['知足', '美食', '摄影'], loveDeclaration: '和你在一起，就是最大的幸运。' },
    { userId: 241, nickname: '极简主义', age: 30, gender: 2, bio: '化繁为简。', avatar: getRandomAvatar(241, 'female'), photos: getMockPhotosByGender(241, 'female', 4), education: '本科', school: '中国传媒大学', income: '年入35w', maritalStatus: '未婚', location: '北京', height: 166, profession: '制片人', distance: '5.2km', onlineText: '正在线', onlineRank: 50, activeScore: 82, matchScore: 91, trustScore: 95, tags: ['利落', '简单', '品质'], loveDeclaration: '简单爱，才长久。' },
    { userId: 242, nickname: '海洋之歌', age: 25, gender: 2, bio: '爱自由，爱大海。', avatar: getRandomAvatar(242, 'female'), photos: getMockPhotosByGender(242, 'female', 4), education: '本科', school: '中国海洋大学', income: '年入20w', maritalStatus: '未婚', location: '青岛', height: 168, profession: '潜水教练', distance: '10km', onlineText: '正在线', onlineRank: 51, activeScore: 94, matchScore: 83, trustScore: 88, tags: ['潜水', '勇敢', '开朗'], loveDeclaration: '带你下潜到蔚蓝深处。' },
    { userId: 243, nickname: '静读少女', age: 22, gender: 2, bio: '书中有我想要的一切。', avatar: getRandomAvatar(243, 'female'), photos: getMockPhotosByGender(243, 'female', 4), education: '本科', school: '华东师范大学', income: '年入12w', maritalStatus: '未婚', location: '上海', height: 162, profession: '学生', distance: '2.1km', onlineText: '刚刚(1-5分钟)', onlineRank: 52, activeScore: 91, matchScore: 85, trustScore: 87, tags: ['文静', '阅读', '图书馆'], loveDeclaration: '寻找一个能陪我一起看书的人。' },
    { userId: 244, nickname: '律政佳人', age: 27, gender: 2, bio: '公平与正义的守护者。', avatar: getRandomAvatar(244, 'female'), photos: getMockPhotosByGender(244, 'female', 4), education: '硕士', school: '中国政法大学', income: '年入30w', maritalStatus: '未婚', location: '北京', height: 169, profession: '律师', distance: '1.8km', onlineText: '正在线', onlineRank: 53, activeScore: 86, matchScore: 92, trustScore: 95, tags: ['专业', '逻辑', '正义'], loveDeclaration: '在法律的世界里，我也期待温情。' },
    { userId: 245, nickname: '甜心主厨', age: 26, gender: 2, bio: '用美食治愈灵魂。', avatar: getRandomAvatar(245, 'female'), photos: getMockPhotosByGender(245, 'female', 4), education: '本科', school: '扬州大学', income: '年入22w', maritalStatus: '未婚', location: '扬州', height: 163, profession: '厨师', distance: '3.4km', onlineText: '刚刚(1-5分钟)', onlineRank: 54, activeScore: 95, matchScore: 88, trustScore: 92, tags: ['美食', '温暖', '贤惠'], loveDeclaration: '抓住你的胃，也抓住你的心。' },
    { userId: 246, nickname: '灵感火花3', age: 28, gender: 2, bio: '世界是我的调色盘。', avatar: getRandomAvatar(246, 'female'), photos: getMockPhotosByGender(246, 'female', 4), education: '本科', school: '广州美术学院', income: '年入26w', maritalStatus: '未婚', location: '广州', height: 165, profession: 'UI设计师', distance: '4.2km', onlineText: '正在线', onlineRank: 55, activeScore: 89, matchScore: 90, trustScore: 91, tags: ['审美', '细节控', '二次元'], loveDeclaration: '一起设计我们的生活。' },
    { userId: 247, nickname: '晨跑女孩', age: 23, gender: 2, bio: '汗水是最好的化妆品。', avatar: getRandomAvatar(247, 'female'), photos: getMockPhotosByGender(247, 'female', 4), education: '本科', school: '暨南大学', income: '年入14w', maritalStatus: '未婚', location: '广州', height: 166, profession: '行政', distance: '5.1km', onlineText: '刚刚(1-5分钟)', onlineRank: 56, activeScore: 97, matchScore: 84, trustScore: 89, tags: ['自律', '阳光', '活力'], loveDeclaration: '一起奔跑在人生的赛道上。' },
    { userId: 248, nickname: '知性学姐2', age: 31, gender: 2, bio: '阅历是最好的财富。', avatar: getRandomAvatar(248, 'female'), photos: getMockPhotosByGender(248, 'female', 4), education: '硕士', school: '南开大学', income: '年入50w', maritalStatus: '未婚', location: '天津', height: 168, profession: '经理', distance: '6.5km', onlineText: '正在线', onlineRank: 57, activeScore: 80, matchScore: 94, trustScore: 97, tags: ['成熟', '内涵', '品味'], loveDeclaration: '寻找一个成熟稳重的你。' },
    { userId: 249, nickname: '萌动之心', age: 20, gender: 2, bio: '永远保持少女心。', avatar: getRandomAvatar(249, 'female'), photos: getMockPhotosByGender(249, 'female', 4), education: '专科', school: '上海工艺美术职业学院', income: '年入8w', maritalStatus: '未婚', location: '上海', height: 157, profession: '学生', distance: '9.2km', onlineText: '刚刚(1-5分钟)', onlineRank: 58, activeScore: 98, matchScore: 78, trustScore: 85, tags: ['萝莉', '可爱', '爱笑'], loveDeclaration: '带我一起去吃糖。' },
    { userId: 250, nickname: '静谧港湾', age: 32, gender: 2, bio: '希望能成为你的港湾。', avatar: getRandomAvatar(250, 'female'), photos: getMockPhotosByGender(250, 'female', 4), education: '本科', school: '大连理工大学', income: '年入32w', maritalStatus: '未婚', location: '大连', height: 164, profession: '会计', distance: '4.7km', onlineText: '正在线', onlineRank: 59, activeScore: 76, matchScore: 92, trustScore: 95, tags: ['贤惠', '稳重', '居家'], loveDeclaration: '平平淡淡才是真。' },
  ]
  
  // 强制过滤逻辑：男性用户只看女性(gender:2)
  const filteredUsers = users.filter(item => {
    const isNotDisliked = !state.dislikedUserIds.includes(Number(item.userId))
    const isNotBlocked = !state.blockedUserIds.includes(Number(item.userId))
    // 如果当前用户是男性(gender:1)或未设置，默认只看女性(gender:2)
    const genderMatch = (state.profile.gender === 1 || state.profile.gender === -1) ? item.gender === 2 : item.gender === 1
    return isNotDisliked && isNotBlocked && genderMatch
  })

  return filteredUsers
    .sort((a, b) => {
      if (a.onlineRank !== b.onlineRank) return a.onlineRank - b.onlineRank
      if (sortMode === 'recommend') return (b.activeScore + b.matchScore) - (a.activeScore + a.matchScore)
      if (sortMode === 'match') return b.matchScore - a.matchScore
      return b.activeScore - a.activeScore
    })
}

const mockOnlineUsers = (state: MockState) => {
  return mockRecommendUsers(state, 'match').map(item => ({
    id: Number(item.userId),
    userId: Number(item.userId),
    nickname: item.nickname,
    avatar: item.avatar,
    age: item.age,
    gender: item.gender,
    distance: item.distance,
    onlineText: item.onlineText,
    tags: item.tags.slice(0, 3),
    height: item.height,
    education: item.education
  }))
}

const mockProfileDetail = (targetUserId: number, state: MockState) => {
  const isUnlocked = state.unlockedWechatIds.includes(targetUserId)
  
  // 尝试从 mockRecommendUsers 中查找该用户，以获取更完整的信息
  const allMockUsers = mockRecommendUsers(state)
  const foundUser = allMockUsers.find(u => Number(u.userId) === Number(targetUserId))
  
  if (foundUser) {
    return {
      ...foundUser,
      photos: JSON.stringify(foundUser.photos || getMockPhotos(3)),
      isRealAuth: 1,
      isEduAuth: 1,
      incomeLevel: 3,
      wechatId: isUnlocked ? `hxy_${targetUserId}` : '',
      tags: JSON.stringify(foundUser.tags || ['性格开朗', '独立', '积极生活']),
      isBlocked: state.blockedUserIds.includes(Number(targetUserId))
    }
  }

  return {
    userId: targetUserId || 999,
    nickname: targetUserId ? `红小鸭用户${targetUserId}` : (state.profile.nickname || '神秘鸭鸭'),
    photos: JSON.stringify(getMockPhotos(3)),
    isRealAuth: 1,
    isEduAuth: 1,
    age: 26,
    height: 168,
    education: '本科',
    school: '北京大学',
    incomeLevel: 3,
    location: '北京市',
    bio: '我是一个热爱生活的人，喜欢旅游和美食。希望能在这里遇到懂我的人。',
    wechatId: isUnlocked ? `hxy_${targetUserId || 999}` : '',
    tags: JSON.stringify(['性格开朗', '独立', '积极生活']),
    distance: '0.5km',
    isBlocked: state.blockedUserIds.includes(Number(targetUserId))
  }
}

const mockActivities = (state: MockState) => {
  const seedActivities = [
    {
      id: 1,
      name: '京户京房单身派对',
      status: 1,
      city: '北京',
      cover: '/static/activities/%E6%B4%BB%E5%8A%A801.jpg',
      tags: ['北京', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '金牌红娘·李姐',
        avatar: '/static/avatars/aiony-haust-3TLl_97HNJo-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 50,
      maleQuota: 50,
      femaleQuota: 50,
      maleJoined: 35,
      femaleJoined: 30,
      joinedCount: 65
    },
    {
      id: 2,
      name: '程序员与教师专场',
      status: 0,
      city: '上海',
      cover: '/static/activities/%E6%B4%BB%E5%8A%A802.jpg',
      tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '红娘小鸭',
        avatar: '/static/avatars/janko-ferlic-G-jo31ESuRE-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 88,
      maleQuota: 20,
      femaleQuota: 20,
      maleJoined: 5,
      femaleJoined: 7,
      joinedCount: 12
    },
    {
      id: 3,
      name: '春季脱单游园会',
      status: 2,
      city: '上海',
      cover: '/static/activities/%E6%B4%BB%E5%8A%A803.jpg',
      tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '资深红娘·张阿姨',
        avatar: '/static/avatars/ian-dooley-d1UPkiFd04A-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 120,
      maleQuota: 30,
      femaleQuota: 30,
      maleJoined: 30,
      femaleJoined: 30,
      joinedCount: 60
    },
    {
      id: 4,
      name: '都市夜跑寻缘',
      status: 1,
      city: '北京',
      cover: '/static/activities/%E6%B4%BB%E5%8A%A804.jpg',
      tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '活力红娘·小王',
        avatar: '/static/avatars/gabriel-silverio-K_b41GaWC5Y-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 29,
      maleQuota: 15,
      femaleQuota: 15,
      maleJoined: 12,
      femaleJoined: 14,
      joinedCount: 26
    },
    {
      id: 5,
      name: '硕博专场剧本杀局',
      status: 0,
      city: '上海',
      cover: '/static/activities/活动01.jpg',
      tags: ['上海', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '推理红娘·大脸猫',
        avatar: '/static/avatars/nate-J5U-22o1ubw-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 118,
      maleQuota: 8,
      femaleQuota: 8,
      maleJoined: 4,
      femaleJoined: 6,
      joinedCount: 10
    },
    {
      id: 6,
      name: '体制内文艺青年读书会',
      status: 0,
      city: '北京',
      cover: '/static/activities/活动02.jpg',
      tags: ['北京', '户京房专场', '体制内专场', '硕博专场'],
      matchmaker: {
        name: '知性红娘·林林',
        avatar: '/static/avatars/christina-wocintechchat-com-m-0Zx1bDv5BNY-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 45,
      maleQuota: 12,
      femaleQuota: 12,
      maleJoined: 3,
      femaleJoined: 8,
      joinedCount: 11
    },
    {
      id: 7,
      name: '冬日围炉煮茶',
      status: 2,
      cover: '/static/activities/活动03.jpg',
      tags: ['茶艺', '慢生活', '小资'],
      matchmaker: {
        name: '温婉红娘·赵姐',
        avatar: '/static/avatars/stefan-stefancik-QXevDflbl8A-unsplash.jpg'
      },
      startTime: new Date('2026/08/10 20:00:00').getTime(),
      price: 88,
      maleQuota: 10,
      femaleQuota: 10,
      maleJoined: 10,
      femaleJoined: 10,
      joinedCount: 20
    },
    {
      id: 8,
      name: '城市周边露营交友',
      status: 0,
      city: '杭州',
      cover: '/static/activities/活动01.jpg',
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
      id: 9,
      name: '8分钟心动相亲速配',
      status: 1,
      city: '广州',
      cover: '/static/activities/活动02.jpg',
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
      id: 10,
      name: '宠物主题单身派对',
      status: 0,
      city: '深圳',
      cover: '/static/activities/活动03.jpg',
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
      id: 11,
      name: '大厂青年专场相亲',
      status: 0,
      city: '北京',
      cover: '/static/activities/活动04.jpg',
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
      id: 12,
      name: '音乐节看展同城聚会',
      status: 2,
      city: '成都',
      cover: '/static/activities/活动01.jpg',
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
  
  // Update state activities with mock covers if they don't have one
  const updatedStateActivities = state.activities.map(act => ({
    ...act,
    cover: act.cover || '/static/activities/活动04.jpg',
    tags: act.tags || ['热门活动'],
    matchmaker: act.matchmaker || {
      name: '红小鸭官方红娘',
      avatar: '/static/logo.png'
    }
  }))

  return [...updatedStateActivities, ...seedActivities]
}

const mockPosts = (state: MockState) => {
  const seedPosts = [
    { id: 1, nickname: '旅行的青蛙', avatar: getRandomAvatar(10, 'female'), isVip: true, publishTime: '10分钟前', location: '北京·朝阳区', hometown: '四川成都', age: 24, education: '本科', height: 165, content: '今天天气真好，去公园溜达了一圈，拍了一些好看的照片。', images: getMockPhotos(1), topic: '周末去哪儿', likeCount: 12, commentCount: 3, isLiked: false },
    { id: 2, nickname: '美食家', avatar: getRandomAvatar(11, 'male'), isVip: false, publishTime: '1小时前', location: '上海·徐汇区', hometown: '江苏南京', age: 28, education: '硕士', height: 178, content: '新发现的一家日料店，味道太棒了！强烈推荐给大家。', images: getMockPhotos(3), topic: '探店打卡', likeCount: 45, commentCount: 18, isLiked: true },
    { id: 3, nickname: '代码诗人', avatar: getRandomAvatar(12, 'male'), isVip: true, publishTime: '2小时前', location: '杭州·西湖区', hometown: '浙江杭州', age: 26, education: '本科', height: 175, content: '周末加班改bug，终于上线了！给自己加个鸡腿。', images: getMockPhotos(2), topic: '程序猿日常', likeCount: 8, commentCount: 5, isLiked: false },
    { id: 4, nickname: '爱猫人士', avatar: getRandomAvatar(13, 'female'), isVip: false, publishTime: '3小时前', location: '广州·天河区', hometown: '广东广州', age: 23, education: '大专', height: 160, content: '我家主子今天特别黏人，给大家看看这盛世美颜。', images: getMockPhotos(4), topic: '萌宠时刻', likeCount: 120, commentCount: 34, isLiked: true },
    { id: 5, nickname: '健身狂魔', avatar: getRandomAvatar(14, 'male'), isVip: true, publishTime: '5小时前', location: '深圳·南山区', hometown: '湖南长沙', age: 29, education: '本科', height: 182, content: '今天的卧推突破了个人记录！继续努力！💪', images: getMockPhotos(1), topic: '运动打卡', likeCount: 65, commentCount: 12, isLiked: false },
    { id: 6, nickname: '独立摄影师', avatar: getRandomAvatar(15, 'female'), isVip: false, publishTime: '6小时前', location: '成都·海珠区', hometown: '四川成都', age: 25, education: '本科', height: 168, content: '街头抓拍，有时候不经意的瞬间最动人。', images: getMockPhotos(3), topic: '摄影日记', likeCount: 88, commentCount: 20, isLiked: true },
    { id: 7, nickname: '金融达人', avatar: getRandomAvatar(16, 'male'), isVip: true, publishTime: '8小时前', location: '上海·浦东新区', hometown: '上海', age: 30, education: '硕士', height: 180, content: '最近的市场波动很大，大家要注意控制风险啊。', images: [], topic: '理财心得', likeCount: 42, commentCount: 15, isLiked: false },
    { id: 8, nickname: '音乐剧迷', avatar: getRandomAvatar(17, 'female'), isVip: false, publishTime: '昨天', location: '北京·东城区', hometown: '北京', age: 24, education: '本科', height: 162, content: '昨晚看的这部音乐剧真的太震撼了，演员的唱功绝了！', images: getMockPhotos(2), topic: '文艺生活', likeCount: 56, commentCount: 8, isLiked: true }
  ]
  return [...state.posts, ...seedPosts]
}

const resetBottleQuotaIfNeeded = (state: MockState) => {
  const today = formatDate()
  if (state.bottleUseDate !== today) {
    state.bottleUseDate = today
    state.bottlePickCount = 0
    state.bottleThrowCount = 0
  }
}

const chargeBottleIfNeeded = (state: MockState, action: 'pick' | 'throw') => {
  resetBottleQuotaIfNeeded(state)
  const freeLimit = state.profile.isVip ? 20 : 5
  const key = action === 'pick' ? 'bottlePickCount' : 'bottleThrowCount'
  state[key] += 1
  if (state[key] > freeLimit) {
    spendEggs(state, 10, action === 'pick' ? '捞取漂流瓶' : '丢出漂流瓶')
  }
}

const handleMockRequest = <T = any>(options: UniApp.RequestOptions): T => {
  const { raw, path, query } = normalizeUrl(options.url)
  const state = readState()

  if (path === '/auth/sms/send') {
    return '验证码已发送' as T
  }

  if (path === '/auth/login' || path === '/auth/login/wechat') {
    const phone = String((options.data as any)?.phone || 'wechat_user')
    const isNewUser = !state.registeredPhones.includes(phone)

    if (isNewUser) {
      state.registeredPhones.push(phone)
      state.profile.nickname = path === '/auth/login/wechat' ? `微信用户${state.profile.userId}` : `红小鸭${state.profile.userId}`
      state.profile.phone = phone
      state.profile.avatar = DEFAULT_AVATAR
      earnEggs(state, 100, '新用户注册赠送', 'temp')
    }

    syncDerivedState(state)
    writeState(state)
    return { token: 'mock_token_123', userId: 999, isNewUser, bonusEggs: isNewUser ? 100 : 0 } as T
  }

  if (path === '/home/recommend') {
    return mockRecommendUsers(state, query.get('sort') || 'active') as T
  }

  if (path === '/discover/online-users') {
    return {
      onlineCount: 1286,
      list: mockOnlineUsers(state)
    } as T
  }

  if (path === '/profile/detail') {
    return mockProfileDetail(Number(query.get('targetUserId') || 0), state) as T
  }

  if (path === '/discover/posts' || path === '/post/list') {
    return mockPosts(state) as T
  }

  requireLogin()

  if (path === '/profile/info') {
    syncDerivedState(state)
    return {
      ...state.profile,
      avatar: state.profile.avatar || DEFAULT_AVATAR
    } as T
  }

  if (path === '/post/create') {
    const data = options.data as Record<string, any>
    state.posts.unshift({
      id: Date.now(),
      userId: state.profile.userId,
      nickname: state.profile.nickname || '红小鸭用户',
      avatar: state.profile.avatar || DEFAULT_AVATAR,
      isVip: state.profile.isVip,
      content: data.content,
      images: data.images || '[]',
      topic: data.topic,
      location: data.location || '同城',
      likeCount: 0,
      commentCount: 0,
      isLiked: false,
      createdAt: new Date().toISOString()
    })
    writeState(state)
    return '发布成功' as T
  }

  if (path === '/post/like') {
    return '点赞成功' as T
  }

  if (path === '/profile/apply-matchmaker') {
    state.profile.isMatchmaker = true
    writeState(state)
    return '申请成功' as T
  }

  if (path === '/profile/update') {
    state.profile = {
      ...state.profile,
      ...(options.data as Record<string, any>)
    }
    writeState(state)
    return '更新成功' as T
  }

  if (path === '/profile/realNameAuth' || path === '/auth/verify') {
    state.profile.isRealAuth = 1
    writeState(state)
    return '认证成功' as T
  }

  if (path === '/wallet/withdraw') {
    const amount = Number((options.data as any)?.amount || 0)
    if (amount <= 0 || amount > state.wallet.commissionBalance) {
      throw new Error('提现金额无效')
    }
    state.wallet.commissionBalance -= amount
    state.ledger.unshift(makeLedger(`佣金提现`, 0, undefined, -amount, 'commission'))
    writeState(state)
    return '提现成功' as T
  }

  if (path === '/wallet/info') {
    syncDerivedState(state)
    return state.wallet as T
  }

  if (path === '/wallet/ledger') {
    syncDerivedState(state)
    return { balance: state.wallet.balance, commissionBalance: state.wallet.commissionBalance, list: state.ledger } as T
  }

  if (path === '/wallet/sign-in') {
    const today = formatDate()
    if (state.signInDate === today) {
      throw new Error('今天已经签到过了')
    }
    const amount = state.profile.isVip ? 20 : 10
    state.signInDate = today
    earnEggs(state, amount, '每日签到', 'temp')
    writeState(state)
    return { amount, balance: state.wallet.balance } as T
  }

  if (path === '/wallet/recharge') {
    const amount = Number(query.get('amount') || 0)
    const rmbAmount = -Math.floor(amount * 0.09) // Just an example, e.g. 2200 -> -198
    state.wallet.permanentBalance += amount
    state.ledger.unshift(makeLedger(`充值${amount}个鸭蛋`, amount, undefined, rmbAmount, 'recharge_rmb'))
    syncDerivedState(state)
    writeState(state)
    return { amount, balance: state.wallet.balance } as T
  }

  if (path === '/vip/buy') {
    const months = Number(query.get('months') || 1)
    const expireAt = new Date()
    expireAt.setMonth(expireAt.getMonth() + months)
    state.vipExpireAt = expireAt.toISOString()
    const rmbAmount = months === 12 ? -128 : -30
    state.ledger.unshift(makeLedger(`开通${months}个月会员`, 0, undefined, rmbAmount, 'recharge_rmb'))
    syncDerivedState(state)
    writeState(state)
    return { level: 1, expireAt: state.vipExpireAt } as T
  }

  if (path === '/vip/info') {
    syncDerivedState(state)
    return { level: state.profile.isVip ? 1 : 0, expireAt: state.vipExpireAt } as T
  }

  if (path === '/home/send-rose' || path === '/chat/send-gift') {
    const amount = Number((options.data as any)?.amount || 20)
    spendEggs(state, amount, amount === 20 ? '送出玫瑰花' : `送出礼物 ${amount}鸭蛋`)
    writeState(state)
    return { balance: state.wallet.balance } as T
  }

  if (path === '/home/like') {
    const targetUserId = Number(query.get('targetUserId') || 0)
    if (targetUserId && !state.likedUserIds.includes(targetUserId)) state.likedUserIds.push(targetUserId)
    writeState(state)
    return '已喜欢' as T
  }

  if (path === '/home/dislike') {
    const targetUserId = Number(query.get('targetUserId') || 0)
    if (targetUserId && !state.dislikedUserIds.includes(targetUserId)) state.dislikedUserIds.push(targetUserId)
    writeState(state)
    return '已不喜欢' as T
  }

  if (path === '/relation/unlock-wechat') {
    const targetId = Number(query.get('targetId') || 0)
    const cost = state.profile.isVip ? 50 : 100
    if (!state.unlockedWechatIds.includes(targetId)) {
      spendEggs(state, cost, '解锁微信号')
      state.unlockedWechatIds.push(targetId)
      writeState(state)
    }
    return `hxy_${targetId}` as T
  }

  if (path === '/relation/block') {
    const targetId = Number(query.get('targetId') || 0)
    if (targetId && !state.blockedUserIds.includes(targetId)) state.blockedUserIds.push(targetId)
    writeState(state)
    return '屏蔽成功' as T
  }

  if (path === '/relation/unblock') {
    const targetId = Number(query.get('targetId') || 0)
    if (targetId) {
      state.blockedUserIds = state.blockedUserIds.filter(id => id !== targetId)
      writeState(state)
    }
    return '解除屏蔽成功' as T
  }

  if (path === '/relation/block-list') {
    return state.blockedUserIds.map(id => {
      const isMale = id % 2 === 0
      return {
        userId: id,
        nickname: `用户_${id}`,
        avatar: getRandomAvatar(id, isMale ? 'male' : 'female'),
        gender: isMale ? 1 : 2,
        age: 20 + (id % 15),
        height: 160 + (id % 20),
        education: id % 2 === 0 ? '本科' : '硕士',
        profession: id % 2 === 0 ? '程序员' : '设计师'
      }
    }) as T
  }

  if (path === '/interaction/who-likes-me' || path === '/relation/who-likes-me') {
    return [
      { id: 1, createdAt: new Date(Date.now() - 600000).toISOString(), userProfile: { userId: 301, nickname: '春风十里', avatar: getRandomAvatar(301, 'female'), bio: '想找个一起看世界的人' }, isUnlocked: true },
      { id: 2, createdAt: new Date(Date.now() - 7200000).toISOString(), userProfile: { userId: 302, nickname: '夜空中的星', avatar: getRandomAvatar(302, 'male'), bio: '爱好音乐、电影和美食' }, isUnlocked: false }
    ] as T
  }

  if (path === '/relation/visitors') {
    return [
      { id: 1, visitTime: new Date().toISOString(), visitorProfile: { userId: 401, nickname: '神秘访客', avatar: DEFAULT_AVATAR }, isUnlocked: false },
      { id: 2, visitTime: new Date(Date.now() - 3600000).toISOString(), visitorProfile: { userId: 402, nickname: '风一样的男子', avatar: getRandomAvatar(402, 'male') }, isUnlocked: true }
    ] as T
  }

  if (path === '/relation/unlock-visitor') {
    spendEggs(state, 20, '查看最近访客')
    writeState(state)
    return '解锁成功' as T
  }

  if (path === '/activity/list') {
    return mockActivities(state) as T
  }

  if (path === '/activity/create') {
    const data = options.data as Record<string, any>
    state.activities.unshift({
      ...data,
      id: Date.now(),
      status: data.startTime <= Date.now() ? 1 : 0,
      joinedCount: 0
    })
    writeState(state)
    return '创建成功' as T
  }

  if (path === '/activity/join') {
    const activityId = Number(query.get('activityId') || 0)
    const activity = state.activities.find(item => Number(item.id) === activityId)
    if (activity) activity.joinedCount = Number(activity.joinedCount || 0) + 1
    writeState(state)
    return '报名成功' as T
  }

  if (path === '/bottle/pick') {
    chargeBottleIfNeeded(state, 'pick')
    writeState(state)
    return {
      id: Date.now(),
      userId: 502,
      nickname: '海边来信',
      avatar: getRandomAvatar(502, 'female'),
      content: '今天想把一点点温柔投递给陌生人，希望你也刚好收到。'
    } as T
  }

  if (path === '/bottle/throw') {
    chargeBottleIfNeeded(state, 'throw')
    writeState(state)
    return '扔出成功' as T
  }

  if (path === '/bottle/records') {
    const type = Number(query.get('type') || 1)
    if (type === 1) {
      return [
        { id: 1, content: '今天天气真好，希望能遇到一个有趣的人。', status: 1, createdAt: new Date(Date.now() - 86400000).toISOString(), pickedAt: new Date(Date.now() - 80000000).toISOString(), targetUserId: 202, targetNickname: '风一样的人', targetAvatar: getRandomAvatar(202, 'male') },
        { id: 2, content: '想找个一起看海的灵魂伴侣。', status: 0, createdAt: new Date(Date.now() - 3600000).toISOString() }
      ] as T
    } else {
      return [
        { id: 3, content: '生活很苦，但你要甜。', status: 1, pickedAt: new Date(Date.now() - 7200000).toISOString(), targetUserId: 305, targetNickname: '甜心女孩', targetAvatar: getRandomAvatar(305, 'female') },
        { id: 4, content: '谁懂啊，今天加班到现在...', status: 1, pickedAt: new Date(Date.now() - 86400000 * 2).toISOString(), targetUserId: 408, targetNickname: '打工人', targetAvatar: getRandomAvatar(408, 'male') }
      ] as T
    }
  }

  if (path === '/message/conversations') {
    return [
      { id: 1, targetId: 101, targetProfile: { nickname: '温柔可人', avatar: getRandomAvatar(101, 'female') }, lastMsgContent: '谢谢你的玫瑰花', lastMsgTime: new Date().toISOString(), unreadCount: 1, isTop: 0 },
      { id: 2, targetId: 102, targetProfile: { nickname: '阳光大男孩', avatar: getRandomAvatar(102, 'male') }, lastMsgContent: '周末有空一起看电影吗？', lastMsgTime: new Date(Date.now() - 86400000).toISOString(), unreadCount: 0, isTop: 0 },
      { id: 3, targetId: 103, targetProfile: { nickname: '文艺青年', avatar: getRandomAvatar(103, 'male') }, lastMsgContent: '昨天的展览很不错', lastMsgTime: new Date(Date.now() - 86400000 * 2).toISOString(), unreadCount: 3, isTop: 0 },
      { id: 4, targetId: 104, targetProfile: { nickname: '甜心女孩', avatar: getRandomAvatar(104, 'female') }, lastMsgContent: '好呀好呀', lastMsgTime: new Date(Date.now() - 86400000 * 3).toISOString(), unreadCount: 0, isTop: 0 },
      { id: 5, targetId: 105, targetProfile: { nickname: '户外达人', avatar: getRandomAvatar(105, 'male') }, lastMsgContent: '下周一起去爬山吗？', lastMsgTime: new Date(Date.now() - 86400000 * 4).toISOString(), unreadCount: 0, isTop: 0 }
    ] as T
  }

  if (path === '/message/history') {
    return [
      { id: 1, senderId: 101, receiverId: 999, msgType: 1, content: '你好呀', createdAt: new Date(Date.now() - 3600000).toISOString() },
      { id: 2, senderId: 999, receiverId: 101, msgType: 1, content: '你好，很高兴认识你', createdAt: new Date(Date.now() - 3500000).toISOString() }
    ] as T
  }

  if (path === '/message/send') {
    if (Number(query.get('msgType')) === 5) {
      const giftName = decodeURIComponent(query.get('content') || '礼物')
      const priceMap: Record<string, number> = { '玫瑰花': 20, '跑车': 50, '火箭': 100 }
      spendEggs(state, priceMap[giftName] || 20, `送出${giftName}`)
      writeState(state)
    }
    return { id: Date.now() } as T
  }

  if (path === '/message/video-call/start') {
    spendEggs(state, 60, '视频通话 2分钟')
    writeState(state)
    return { balance: state.wallet.balance } as T
  }

  console.log('Mock Request:', raw, options.data)
  return {} as T
}

export const request = <T = any>(options: UniApp.RequestOptions): Promise<T> => {
  return new Promise((resolve, reject) => {
    if (USE_MOCK) {
      setTimeout(() => {
        try {
          resolve(handleMockRequest<T>(options))
        } catch (error: any) {
          uni.showToast({
            title: error?.message || '请求失败',
            icon: 'none'
          })
          if (error?.message?.includes('请先登录')) {
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/login/index' })
            }, 600)
          }
          reject(error)
        }
      }, 250)
      return
    }

    uni.request({
      ...options,
      url: BASE_URL + options.url,
      header: {
        ...options.header,
        Authorization: uni.getStorageSync('token') || ''
      },
      success: (res: any) => {
        const data = res.data
        if (data.code === 0) {
          resolve(data.data as T)
        } else {
          uni.showToast({
            title: data.msg || '请求失败',
            icon: 'none'
          })
          if (data.code === 401 || data.code === 1001) {
            uni.navigateTo({ url: '/pages/login/index' })
          }
          reject(new Error(data.msg))
        }
      },
      fail: (err) => {
        console.error('Request Error:', err)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
