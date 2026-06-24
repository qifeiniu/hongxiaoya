<template>
  <view class="profile-edit-container">
    <view class="avatar-section">
      <view class="avatar-wrap" @click="chooseAvatar">
        <image class="avatar" :src="form.avatar" mode="aspectFill"></image>
        <view class="camera-icon"><text class="ri-camera-3-fill"></text></view>
      </view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <view class="form-group">
      <view class="form-title">基础资料</view>
      <view class="form-list">
        <view class="form-item">
          <text class="label">昵称</text>
          <input class="input" v-model="form.nickname" placeholder="请输入昵称" placeholder-class="ph-color"/>
        </view>
        
        <view class="form-item">
          <text class="label">ID</text>
          <text class="value-readonly">{{ form.userId }}</text>
        </view>
        
        <view class="form-item">
          <text class="label">微信号</text>
          <input class="input" v-model="form.wechatId" placeholder="仅互通心意或付费后可见" placeholder-class="ph-color"/>
        </view>
        
        <picker @change="bindGenderChange" :value="form.genderIndex" :range="genderOptions">
          <view class="form-item">
            <text class="label">性别</text>
            <text class="value" :class="{ 'placeholder': form.genderIndex === -1 }">
              {{ form.genderIndex === -1 ? '请选择性别' : genderOptions[form.genderIndex] }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <picker mode="date" :value="form.birthday" @change="bindDateChange">
          <view class="form-item">
            <text class="label">生日</text>
            <text class="value" :class="{ 'placeholder': !form.birthday }">
              {{ form.birthday || '请选择出生日期' }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <picker @change="bindConstellationChange" :value="form.constellationIndex" :range="constellationOptions">
          <view class="form-item">
            <text class="label">星座</text>
            <text class="value" :class="{ 'placeholder': form.constellationIndex === -1 }">
              {{ form.constellationIndex === -1 ? '请选择星座' : constellationOptions[form.constellationIndex] }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <view class="form-item">
          <text class="label">身高(cm)</text>
          <input class="input" type="number" v-model="form.height" placeholder="请输入身高" placeholder-class="ph-color"/>
        </view>
        
        <view class="form-item">
          <text class="label">体重(kg)</text>
          <input class="input" type="number" v-model="form.weight" placeholder="请输入体重" placeholder-class="ph-color"/>
        </view>
        
        <picker @change="bindEduChange" :value="form.educationIndex" :range="eduOptions">
          <view class="form-item">
            <text class="label">学历</text>
            <text class="value" :class="{ 'placeholder': form.educationIndex === -1 }">
              {{ form.educationIndex === -1 ? '请选择学历' : eduOptions[form.educationIndex] }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>
        
        <view class="form-item">
          <text class="label">毕业院校</text>
          <input class="input" v-model="form.school" placeholder="请输入毕业院校" placeholder-class="ph-color"/>
        </view>

        <picker @change="bindIncomeChange" :value="form.incomeIndex" :range="incomeOptions">
          <view class="form-item">
            <text class="label">年收入</text>
            <text class="value" :class="{ 'placeholder': form.incomeIndex === -1 }">
              {{ form.incomeIndex === -1 ? '请选择年收入' : incomeOptions[form.incomeIndex] }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <view class="form-item">
          <text class="label">职业</text>
          <input class="input" v-model="form.profession" placeholder="请输入职业" placeholder-class="ph-color"/>
        </view>

        <picker mode="multiSelector" :range="cityOptions" @change="(e:any) => form.city = cityOptions[0][e.detail.value[0]] + '-' + cityOptions[1][e.detail.value[1]]" @columnchange="handleCityColumnChange">
          <view class="form-item">
            <text class="label">居住地</text>
            <text class="value" :class="{ 'placeholder': !form.city }">{{ form.city || '请选择居住地' }}</text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <picker mode="multiSelector" :range="cityOptions2" @change="(e:any) => { form.hometown = cityOptions2[0][e.detail.value[0]] + '-' + cityOptions2[1][e.detail.value[1]]; form.household = cityOptions2[0][e.detail.value[0]] + '-' + cityOptions2[1][e.detail.value[1]]; }" @columnchange="handleCityColumnChange2">
          <view class="form-item">
            <text class="label">籍贯/户口所在地</text>
            <text class="value" :class="{ 'placeholder': !form.hometown }">{{ form.hometown || '请选择' }}</text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>

        <picker @change="bindMaritalChange" :value="form.maritalIndex" :range="maritalOptions">
          <view class="form-item">
            <text class="label">婚姻状态</text>
            <text class="value" :class="{ 'placeholder': form.maritalIndex === -1 }">
              {{ form.maritalIndex === -1 ? '请选择婚姻状态' : maritalOptions[form.maritalIndex] }}
            </text>
            <text class="ri-arrow-right-s-line arrow"></text>
          </view>
        </picker>
      </view>
    </view>

    <view class="form-group">
      <view class="form-title">自我介绍及家庭成员</view>
      <view class="form-list">
        <view class="form-item bio-item">
          <textarea class="textarea large-textarea" v-model="form.bio" placeholder="介绍一下自己、家庭情况及交友期待...（文字介绍可上传图片六张内）" maxlength="300"></textarea>
        </view>
      </view>
      <view class="photo-list">
        <view class="photo-item" v-for="(photo, index) in form.photos" :key="index">
          <image :src="photo" mode="aspectFill" class="photo-img"></image>
          <view class="del-btn" @click="removePhoto(index)"><text class="ri-close-line"></text></view>
        </view>
        <view class="photo-upload" @click="choosePhotos" v-if="form.photos.length < 6">
          <text class="ri-add-line add-icon"></text>
        </view>
      </view>
    </view>

    <view class="form-group">
      <view class="form-title">期待的伴侣及生活</view>
      <view class="form-list">
        <view class="form-item bio-item">
          <textarea class="textarea large-textarea" v-model="form.idealPartner" placeholder="你期待Ta是什么样的人？两人在一起后向往的生活状态是？（纯文字介绍）" maxlength="200"></textarea>
        </view>
      </view>
    </view>

    <view class="form-group">
      <view class="form-title">兴趣爱好及爱情宣言</view>
      <view class="form-list">
        <view class="form-item bio-item">
          <textarea class="textarea large-textarea" v-model="form.hobbies" placeholder="平时喜欢做些什么？对爱情有什么宣言？（纯文字介绍）" maxlength="200"></textarea>
        </view>
      </view>
    </view>

    <view class="form-group">
      <view class="form-title">我的标签</view>
      <view class="tags-container">
        <view class="tag-item" v-for="(tag, index) in form.tags" :key="index">
          {{ tag }}
          <text class="ri-close-line del-tag" @click="removeTag(index)"></text>
        </view>
        <view class="add-tag-wrap" v-if="form.tags.length < 6">
          <input class="tag-input" v-model="newTag" placeholder="添加标签" @confirm="addTag" confirm-type="done" />
          <view class="add-tag-btn" @click="addTag">添加</view>
        </view>
      </view>
    </view>

    <button class="save-btn" @click="handleSave">保存资料</button>
    <view class="logout-link" v-if="isFromLogin" @click="handleLogout">
      <text>退出登录 / 切换账号</text>
    </view>
    <view class="safe-bottom"></view>
      <!-- 统一自定义弹窗 -->
    <custom-popup />
  </view></template>

<script setup lang="ts">
import { ref } from 'vue'
import { request } from '../../utils/request'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { getRandomAvatar, DEFAULT_AVATAR } from '../../utils/mockData'

const genderOptions = ['未知', '男', '女']
const eduOptions = ['大专', '本科', '硕士', '博士']
const incomeOptions = ['5万以下', '5-10w', '10-20w', '20-30w', '30-40w', '40-50w', '50-60w', '60-80w', '80-100w', '100w以上']
const maritalOptions = ['未婚', '离异', '丧偶']
const constellationOptions = ['白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座', '天秤座', '天蝎座', '射手座', '摩羯座', '水瓶座', '双鱼座']

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

const newTag = ref('')
const isFromLogin = ref(false)

const form = ref({
  userId: 0,
  avatar: DEFAULT_AVATAR,
  nickname: '红小鸭用户',
  wechatId: '',
  genderIndex: -1,
  birthday: '',
  constellationIndex: -1,
  height: '',
  weight: '',
  educationIndex: -1,
  school: '',
  incomeIndex: -1,
  profession: '',
  city: '',
  hometown: '',
  household: '',
  maritalIndex: -1,
  bio: '',
  hobbies: '',
  idealPartner: '',
  photos: [] as string[],
  tags: [] as string[]
})

onLoad((options: any) => {
  if (options.from === 'login') {
    isFromLogin.value = true
  }
})

const loadProfile = async () => {
  try {
    const res: any = await request({
      url: '/profile/info',
      method: 'GET'
    })
    if (res) {
      form.value.userId = res.userId || uni.getStorageSync('userId') || 0
      let nickname = res.nickname || ''
      const defaultPattern = /^(红小鸭|微信用户|红小鸭用户)\d+$/
      if (defaultPattern.test(nickname)) {
        nickname = nickname.replace(/\d+$/, '')
      }
      form.value.nickname = nickname
      form.value.avatar = res.avatar || DEFAULT_AVATAR
      form.value.wechatId = res.wechatId || ''
      form.value.genderIndex = res.gender !== undefined ? res.gender : -1
      if (res.birthday) {
        const d = new Date(res.birthday)
        form.value.birthday = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      }
      if (res.constellation) {
        form.value.constellationIndex = constellationOptions.indexOf(res.constellation)
      }
      form.value.height = res.height ? String(res.height) : ''
      form.value.weight = res.weight ? String(res.weight) : ''
      form.value.educationIndex = res.education !== undefined ? res.education - 1 : -1
      form.value.school = res.school || ''
      form.value.incomeIndex = res.incomeLevel !== undefined ? res.incomeLevel - 1 : -1
      form.value.profession = res.profession || ''
      if (res.cityCode) {
        form.value.city = res.cityCode
      }
      if (res.hometownCode) {
        form.value.hometown = res.hometownCode
      }
      if (res.householdCode) {
        form.value.household = res.householdCode
      }
      form.value.maritalIndex = res.maritalStatus !== undefined ? res.maritalStatus - 1 : -1
      
      form.value.bio = res.bio || ''
      if (res.familyBackground) form.value.bio += (form.value.bio ? '\n' : '') + res.familyBackground
      
      form.value.idealPartner = res.idealPartner || ''
      if (res.expectedLife) form.value.idealPartner += (form.value.idealPartner ? '\n' : '') + res.expectedLife
      
      form.value.hobbies = res.hobbies || ''
      
      if (res.photos) {
        try {
          form.value.photos = typeof res.photos === 'string' ? JSON.parse(res.photos) : res.photos
        } catch(e) { form.value.photos = [] }
      }
      if (res.tags) {
        try {
          form.value.tags = typeof res.tags === 'string' ? JSON.parse(res.tags) : res.tags
        } catch(e) { form.value.tags = [] }
      }
    }
  } catch (e) {
    console.error(e)
  }
}

onShow(() => {
  loadProfile()
})

const bindGenderChange = (e: any) => { form.value.genderIndex = e.detail.value }
const bindDateChange = (e: any) => { form.value.birthday = e.detail.value }
const bindConstellationChange = (e: any) => { form.value.constellationIndex = e.detail.value }
const bindEduChange = (e: any) => { form.value.educationIndex = e.detail.value }
const bindIncomeChange = (e: any) => { form.value.incomeIndex = e.detail.value }
const bindMaritalChange = (e: any) => { form.value.maritalIndex = e.detail.value }

const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.value.avatar = res.tempFilePaths[0] as string
      uni.showToast({ title: '已选择新头像', icon: 'none' })
    }
  })
}

const choosePhotos = () => {
  const remain = 6 - form.value.photos.length
  if (remain <= 0) return
  uni.chooseImage({
    count: remain,
    success: (res) => {
      form.value.photos = [...form.value.photos, ...(res.tempFilePaths as string[])]
    }
  })
}

const removePhoto = (index: number) => {
  form.value.photos.splice(index, 1)
}

const addTag = () => {
  if (newTag.value.trim() && form.value.tags.length < 6) {
    form.value.tags.push(newTag.value.trim())
    newTag.value = ''
  }
}

const removeTag = (index: number) => {
  form.value.tags.splice(index, 1)
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '尚未完善资料，确定要退出登录吗？',
    confirmColor: '#6A61F8',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('isProfileComplete')
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}

const handleSave = async () => {
  // 校验必填项
  if (!form.value.avatar) {
    uni.showToast({ title: '请上传头像', icon: 'none' })
    return
  }
  if (!form.value.nickname.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  if (form.value.genderIndex === -1 || form.value.genderIndex === 0) {
    uni.showToast({ title: '请选择性别', icon: 'none' })
    return
  }
  if (!form.value.birthday) {
    uni.showToast({ title: '请选择生日以计算年龄', icon: 'none' })
    return
  }

  uni.showLoading({ title: '保存中...' })
  try {
    const payload = {
      ...form.value,
      gender: form.value.genderIndex > -1 ? form.value.genderIndex : undefined,
      constellation: form.value.constellationIndex > -1 ? constellationOptions[form.value.constellationIndex] : undefined,
      education: form.value.educationIndex > -1 ? form.value.educationIndex + 1 : undefined,
      incomeLevel: form.value.incomeIndex > -1 ? form.value.incomeIndex + 1 : undefined,
      maritalStatus: form.value.maritalIndex > -1 ? form.value.maritalIndex + 1 : undefined,
      cityCode: form.value.city,
      hometownCode: form.value.hometown,
      householdCode: form.value.household,
      familyBackground: '',
      expectedLife: '',
      photos: JSON.stringify(form.value.photos),
      tags: JSON.stringify(form.value.tags)
    }
    
    await request({
      url: '/profile/update',
      method: 'POST',
      data: payload
    })
    uni.hideLoading()
    // 保存完善状态
    uni.setStorageSync('isProfileComplete', true)
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      if (isFromLogin.value) {
        uni.switchTab({ url: '/pages/index/index' })
      } else {
        uni.navigateBack()
      }
    }, 1500)
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.profile-edit-container {
  background-color: #F4F5F9;
  min-height: 100vh;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 0;
  background-color: #FFF;
  margin-bottom: 20rpx;
}

.avatar-wrap {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 16rpx;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #EEE;
  border: 4rpx solid #FFF;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.camera-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  background-color: #6A61F8;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 4rpx solid #FFF;
}

.camera-icon text {
  color: #FFF;
  font-size: 24rpx;
}

.avatar-tip {
  font-size: 24rpx;
  color: #999;
}

.form-group {
  background-color: #FFF;
  margin-bottom: 20rpx;
  padding: 0 30rpx;
}

.form-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  padding: 30rpx 0 10rpx;
}

.form-list {
  display: flex;
  flex-direction: column;
}

.form-item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #F5F5F5;
  padding: 30rpx 0;
  font-size: 30rpx;
}

.form-item:last-child {
  border-bottom: none;
}

.bio-item {
  flex-direction: column;
  align-items: flex-start;
}

.form-item.bio-item {
  border-bottom: none;
  padding-top: 0;
}

.label {
  width: 180rpx;
  color: #333;
  font-weight: 500;
}

.block-label {
  width: 100%;
  margin-bottom: 16rpx;
}

.tip {
  font-size: 24rpx;
  color: #FF4D4F;
  font-weight: normal;
}

.input {
  flex: 1;
  text-align: right;
  color: #333;
  font-size: 30rpx;
}

.ph-color {
  color: #CCC;
}

.value {
  flex: 1;
  text-align: right;
  color: #333;
}

.value-readonly {
  flex: 1;
  text-align: right;
  color: #999;
}

.placeholder {
  color: #CCC;
}

.arrow {
  color: #CCC;
  margin-left: 10rpx;
  font-size: 36rpx;
}

.textarea {
  width: 100%;
  height: 160rpx;
  background-color: #F9F9F9;
  padding: 20rpx;
  border-radius: 16rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #333;
}

.large-textarea {
  height: 240rpx;
}

/* 相册 */
.photo-list {
  display: flex;
  flex-wrap: wrap;
  padding: 0 0 30rpx;
  gap: 20rpx;
}

.photo-item, .photo-upload {
  width: 216rpx;
  height: 216rpx;
  border-radius: 16rpx;
  position: relative;
}

.photo-img {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
  background-color: #F5F5F5;
}

.del-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 40rpx;
  height: 40rpx;
  background-color: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.del-btn text {
  color: #FFF;
  font-size: 24rpx;
}

.photo-upload {
  background-color: #F9F9F9;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2rpx dashed #E5E5E5;
}

.add-icon {
  font-size: 60rpx;
  color: #CCC;
}

/* 标签 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  padding: 20rpx 0 30rpx;
}

.tag-item {
  background-color: #F0F0FF;
  color: #6A61F8;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
}

.del-tag {
  margin-left: 10rpx;
  font-size: 28rpx;
}

.add-tag-wrap {
  display: flex;
  align-items: center;
  background-color: #F5F5F5;
  border-radius: 30rpx;
  padding: 4rpx 8rpx 4rpx 24rpx;
}

.tag-input {
  width: 140rpx;
  font-size: 26rpx;
  color: #333;
}

.add-tag-btn {
  background-color: #6A61F8;
  color: #FFF;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  margin-left: 10rpx;
}

.save-btn {
  margin: 60rpx 40rpx 20rpx;
  background: linear-gradient(90deg, #8B70FF 0%, #6A61F8 100%);
  color: #FFF;
  height: 90rpx;
  line-height: 90rpx;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 8rpx 20rpx rgba(106, 97, 248, 0.3);
}

.save-btn::after {
  border: none;
}

.logout-link {
  text-align: center;
  padding: 30rpx 0;
  margin-bottom: 20rpx;
  color: #999;
  font-size: 28rpx;
}

.safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
