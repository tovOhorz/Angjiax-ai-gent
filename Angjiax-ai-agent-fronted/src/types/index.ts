export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface LoginUserVO {
  id: string
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  vip_level: number
  points: number
  phone: string
  editTime?: string
  createTime?: string
  updateTime?: string
}

export interface UserVO {
  id: string
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  vip_level: number
  points: number
  phone: string
  createTime: string
}

export interface ProductVO {
  id: number
  productName: string
  categoryName: string
  brand: string
  originalPrice: number
  currentPrice: number
  stock: number
  salesVolume: number
  rating: number
  platform: string
  platformUrl?: string
  imageUrl?: string
  productDesc?: string
  status: number
  createTime: string
}

export interface CouponEntity {
  id: string
  couponName: string
  couponType: number
  conditionAmount: number
  discountAmount: number
  discountRate: number
  startTime: string
  endTime: string
  applicableProducts?: string
  platform: string
  totalQuantity: number
  usedQuantity: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface UserCouponVO {
  id: string
  userId: string
  coupon: {
    id: string
    couponName: string
    couponType: number
    conditionAmount: number
    discountAmount: number
    discountRate: number
    startTime: string
    endTime: string
    platform: string
    status: number
    description?: string
  }
  status: number
  statusText: string
  receivedAt: string
  usedAt: string
  orderId: string
}

export interface CartVO {
  id: string
  /** 管理员全量列表接口返回 */
  userId?: string
  productId: string
  productName: string
  imageUrl: string
  currentPrice: number
  quantity: number
  subtotal: number
  selected: number
  stock: number
  status: number
}

export interface OrderItemVO {
  id: string
  orderId: string
  productId: string
  productName: string
  productImage: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface OrderVO {
  id: string
  orderId: string
  userId: string
  totalAmount: number
  discountAmount: number
  actualPaid: number
  couponId?: string
  orderStatus: number
  orderStatusText?: string
  paidTime?: string
  address?: string
  createTime: string
  orderItems?: OrderItemVO[]
}
