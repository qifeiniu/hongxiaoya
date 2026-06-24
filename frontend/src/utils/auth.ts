/**
 * Global authentication guard utility.
 * Checks login status and redirects to login page when needed.
 */
import { request } from './request'

// Pages that can be accessed without login
const WHITE_LIST = [
  '/pages/splash/index',
  '/pages/login/index',
  '/pages/privacy/index',
  '/pages/help/index',
  '/pages/index/index',
  '/pages/discover/index',
  '/pages/activity/index',
  '/pages/message/index',
  '/pages/profile/index'
]

/**
 * Check if user is logged in
 */
export const isLoggedIn = (): boolean => {
  return !!uni.getStorageSync('token')
}

/**
 * 全局路由守卫（同步检查）。
 * 检查登录状态和资料完善状态，如果不满足则直接重定向。
 * 建议在应用主页面的 onShow 中调用。
 */
export const globalAuthGuard = (): boolean => {
  const token = uni.getStorageSync('token')
  if (!token) {
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      const path = '/' + currentPage.route
      if (isWhiteListPage(path)) {
        return true
      }
    }
    uni.reLaunch({ url: '/pages/login/index' })
    return false
  }
  
  return true
}

/**
 * Require login for interactive actions.
 * Shows a modal and redirects to login page if not logged in.
 * Returns true if user is logged in, false otherwise.
 */
export const requireLogin = (actionText: string = '使用此功能'): boolean => {
  if (isLoggedIn()) return true

  uni.showModal({
    title: '请先登录',
    content: `登录后才能${actionText}`,
    confirmText: '去登录',
    confirmColor: '#6A61F8',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/login/index' })
      }
    }
  })
  return false
}

/**
 * 完整权限检查：包含登录、完善资料、实名认证。
 * 用于核心互动功能（聊天、送礼物、报名相亲活动、使用漂流瓶等）。
 */
export const checkAuth = async (actionText: string = '使用此功能', requireRealName: boolean = true): Promise<boolean> => {
  // 1. 检查是否登录
  if (!isLoggedIn()) {
    uni.showModal({
      title: '请先登录',
      content: `登录后才能${actionText}`,
      confirmText: '去登录',
      confirmColor: '#6A61F8',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/index' })
        }
      }
    })
    return false
  }

  try {
    // 移除“检查权限中...”的loading提示，避免打断操作流程
    const profile: any = await request({ url: '/profile/info', method: 'GET' })

    // 2. 检查资料是否完善（头像、昵称、性别）
    if (!profile.nickname || !profile.avatar || profile.gender === -1) {
      uni.showModal({
        title: '完善个人资料',
        content: `请先完善基本资料（头像、昵称、性别等）才能${actionText}`,
        confirmText: '去完善',
        confirmColor: '#6A61F8',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/profile-edit/index' })
          }
        }
      })
      return false
    }

    // 3. 检查是否实名认证 (仅针对需要实名的功能)
    if (requireRealName && profile.isRealAuth !== 1) {
      uni.showModal({
        title: '实名认证',
        content: `实名注册会员才能${actionText}，请先完成实名认证（OCR识别+活体检测）`,
        confirmText: '去认证',
        confirmColor: '#6A61F8',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/auth-verify/index' })
          }
        }
      })
      return false
    }

    return true
  } catch (e) {
    return false
  }
}

/**
 * Navigation guard - wrap navigateTo with login check
 */
export const navigateWithAuth = (url: string, actionText?: string) => {
  if (!requireLogin(actionText || '查看此页面')) return
  uni.navigateTo({ url })
}

/**
 * Check if a page path is in the whitelist
 */
export const isWhiteListPage = (path: string): boolean => {
  return WHITE_LIST.some(p => path.startsWith(p))
}
