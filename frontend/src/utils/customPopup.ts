import { reactive } from 'vue'

export interface ModalOptions {
  title?: string
  content?: string
  showCancel?: boolean
  cancelText?: string
  confirmText?: string
  confirmColor?: string
  cancelColor?: string
  success?: (res: { confirm: boolean; cancel: boolean }) => void
  fail?: (err: any) => void
  complete?: (res: any) => void
}

export interface ActionSheetOptions {
  title?: string
  itemList: string[]
  itemColor?: string
  success?: (res: { tapIndex: number }) => void
  fail?: (err: any) => void
  complete?: (res: any) => void
}

export const popupState = reactive({
  // Modal dialog state
  modal: {
    show: false,
    title: '提示',
    content: '',
    showCancel: true,
    cancelText: '取消',
    confirmText: '确定',
    confirmColor: '#6B5EF7',
    cancelColor: '#8A8A99',
    resolve: null as ((value: { confirm: boolean; cancel: boolean }) => void) | null,
  },
  // Action sheet menu state
  actionSheet: {
    show: false,
    title: '',
    itemList: [] as string[],
    itemColor: '#2A2938',
    resolve: null as ((value: { tapIndex: number }) => void) | null,
    reject: null as ((err: any) => void) | null,
  }
})

export function customShowModal(options: ModalOptions) {
  popupState.modal.title = options.title !== undefined ? options.title : '提示'
  popupState.modal.content = options.content || ''
  popupState.modal.showCancel = options.showCancel !== false
  popupState.modal.cancelText = options.cancelText || '取消'
  popupState.modal.confirmText = options.confirmText || '确定'
  popupState.modal.confirmColor = options.confirmColor || '#6B5EF7'
  popupState.modal.cancelColor = options.cancelColor || '#8A8A99'
  
  popupState.modal.show = true
  
  return new Promise<{ confirm: boolean; cancel: boolean }>((resolve) => {
    popupState.modal.resolve = (res) => {
      if (options.success) options.success(res)
      if (options.complete) options.complete(res)
      resolve(res)
    }
  })
}

export function customShowActionSheet(options: ActionSheetOptions) {
  popupState.actionSheet.title = options.title || ''
  popupState.actionSheet.itemList = options.itemList || []
  popupState.actionSheet.itemColor = options.itemColor || '#2A2938'
  
  popupState.actionSheet.show = true
  
  return new Promise<{ tapIndex: number }>((resolve, reject) => {
    popupState.actionSheet.resolve = (res) => {
      if (options.success) options.success(res)
      if (options.complete) options.complete(res)
      resolve(res)
    }
    popupState.actionSheet.reject = (err) => {
      if (options.fail) options.fail(err)
      if (options.complete) options.complete(err)
      reject(err)
    }
  })
}

// Global initialization helper
export function initCustomPopupGlobal() {
  uni.showModal = customShowModal as any
  uni.showActionSheet = customShowActionSheet as any
}
