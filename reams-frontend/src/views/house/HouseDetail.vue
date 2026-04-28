<template>
  <div class="house-detail-page">
    <el-skeleton v-if="pageLoading" animated :rows="10" class="detail-skeleton" />

    <template v-else-if="house">
      <section class="listing-shell">
        <div class="hero-topbar">
          <el-button class="back-button" :icon="ArrowLeft" @click="router.back()">返回列表</el-button>
          <div class="hero-topbar-right">
            <span class="view-pill">{{ `${house.viewCount || 0} 次浏览` }}</span>
            <el-tag v-if="house.houseStatus != null" :type="statusTagType" effect="dark">
              {{ statusLabel }}
            </el-tag>
          </div>
        </div>

        <div class="listing-body">
          <div class="listing-left">
            <article class="gallery-card">
              <div class="card-head">
                <div>
                  <p class="section-kicker">Gallery</p>
                  <h2>房源图片</h2>
                </div>
                <span class="card-tip">点击主图查看大图</span>
              </div>

              <div class="main-image" @click="showPreview = true">
                <img :src="currentImage" :alt="house.title" />
                <span v-if="imageList.length > 1" class="image-order">{{ currentIndex + 1 }} / {{ imageList.length }}</span>
                <span class="image-tip">查看大图</span>
              </div>

              <div v-if="imageList.length > 1" class="thumb-row">
                <button
                  v-for="(image, index) in imageList"
                  :key="`${image}-${index}`"
                  type="button"
                  class="thumb-button"
                  :class="{ active: currentIndex === index }"
                  @click="switchImage(index)"
                >
                  <img :src="image" :alt="`${house.title}-${index + 1}`" />
                </button>
              </div>
            </article>

            <article class="description-card">
              <div class="card-head">
                <div>
                  <p class="section-kicker">Listing Story</p>
                  <h2>房源描述</h2>
                </div>
              </div>

              <div class="description-content">
                <p class="description-text">{{ house.description || '暂无房源描述信息。' }}</p>
              </div>
            </article>
          </div>

          <div class="listing-right">
            <article class="info-card info-overview-card">
              <div class="price-block">
                <p class="eyebrow">House Profile</p>
                <h1 class="house-title">{{ house.title }}</h1>

                <div class="price-line">
                  <span class="price-value">{{ formatPrice(house.price) }}</span>
                  <span class="price-unit">万元</span>
                </div>

                <p class="price-subline">
                  {{ house.unitPrice ? `约 ${Math.round(house.unitPrice)} 元/㎡` : '单价信息暂未完善' }}
                </p>

                <p v-if="fullAddress" class="price-location">{{ fullAddress }}</p>
              </div>

              <div class="house-facts-panel">
                <div v-for="item in primaryFacts" :key="item.label" class="house-fact-card">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>

              <div class="detail-sections">
                <section class="detail-block detail-property">
                  <div class="detail-block-head">
                    <strong>房源信息</strong>
                  </div>
                  <div v-if="propertyItems.length" class="detail-inline-list">
                    <div
                      v-for="item in propertyItems"
                      :key="item.label"
                      class="detail-inline-item"
                      :class="{ 'is-full': item.fullRow }"
                    >
                      <span>{{ item.label }}</span>
                      <strong>{{ item.value }}</strong>
                    </div>
                  </div>
                  <div v-else class="detail-empty">当前暂无更多可展示的房源信息。</div>
                </section>

                <section v-if="facilityChips.length" class="detail-block detail-facility">
                  <div class="detail-block-head">
                    <strong>配套与亮点</strong>
                    <span>用标签方式展示配套和亮点</span>
                  </div>
                  <div class="detail-chip-list">
                    <span v-for="chip in facilityChips" :key="chip" class="detail-chip">{{ chip }}</span>
                  </div>
                </section>
              </div>

              <div class="agent-panel">
                <div class="agent-card-head">
                  <div>
                    <p class="agent-kicker">专属带看顾问</p>
                    <strong class="agent-name">{{ house.agentName || '待分配中介' }}</strong>
                  </div>

                  <el-button
                    v-if="house.agentId"
                    type="primary"
                    plain
                    @click="router.push(`/layout/agent/profile/${house.agentId}`)"
                  >
                    查看主页
                  </el-button>
                </div>

                <p class="agent-phone">{{ house.agentPhone || '暂无联系电话' }}</p>

                <div class="action-row">
                  <el-button type="success" size="large" class="action-button" @click="handleContact">
                    发送消息
                  </el-button>
                  <el-button
                    size="large"
                    class="action-button"
                    :class="{ 'favorite-active': isFavorited }"
                    @click="toggleFavorite"
                  >
                    <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
                    {{ isFavorited ? '已收藏' : '收藏房源' }}
                  </el-button>
                </div>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section class="review-shell">
        <article class="review-card-shell">
          <div class="section-head review-head">
            <div class="review-copy">
              <p class="section-kicker">Customer Reviews</p>
              <h2>客户评价</h2>
              <p class="review-count-text">{{ evaluations.length }} 条评价</p>
            </div>

            <div class="review-score-box">
              <strong>{{ averageRating.toFixed(1) }}</strong>
              <small>{{ reviewLabel }}</small>
            </div>
          </div>

          <div v-if="canReview" class="compose-card">
            <div class="compose-top">
              <div>
                <h3>发表房源评价</h3>
                <p>支持文字、表情和图片，分享你的真实看房感受。</p>
              </div>
              <div class="compose-rating">
                <span>评分</span>
                <el-rate v-model="evalForm.rating" show-text :texts="ratingTexts" />
              </div>
            </div>

            <el-input
              ref="reviewInputRef"
              v-model="evalForm.content"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              resize="none"
              placeholder="可以聊聊房源真实感受、户型采光、周边环境等。"
              @paste="handleReviewPaste"
            />

            <div v-if="emojiPickerVisible" class="emoji-panel">
              <button
                v-for="emoji in emojiList"
                :key="emoji"
                type="button"
                class="emoji-chip"
                @click="appendEmoji(emoji)"
              >
                {{ emoji }}
              </button>
            </div>

            <input
              ref="reviewFileInputRef"
              class="hidden-file-input"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              multiple
              @change="handleReviewImageChange"
            />

            <div v-if="reviewImages.length" class="review-image-grid">
              <div v-for="image in reviewImages" :key="image.id" class="review-image-card">
                <img :src="image.previewUrl" :alt="image.name" />
                <div class="review-image-meta">
                  <span>{{ image.name }}</span>
                  <small>{{ formatFileSize(image.size) }}</small>
                </div>
                <button type="button" class="remove-image-button" @click="removeReviewImage(image.id)">移除</button>
              </div>
            </div>

            <div class="compose-toolbar">
              <div class="toolbar-left">
                <el-button plain @click="toggleEmojiPicker">表情</el-button>
                <el-button plain @click="openReviewImagePicker">图片</el-button>
                <span class="toolbar-hint">最多 3 张图片，单张不超过 2MB，支持 JPG / PNG / GIF / WebP，也支持直接粘贴图片。</span>
              </div>
              <el-button type="primary" :loading="submitting" @click="submitEvaluation">发布评价</el-button>
            </div>
          </div>

          <div class="evaluation-list">
            <article v-for="item in evaluations" :key="item.id" class="evaluation-card">
              <div class="evaluation-head">
                <div class="evaluation-user">
                  <el-avatar :size="42" :src="item.avatarUrl || undefined" class="evaluation-avatar">
                    {{ (item.userName || '匿')[0] }}
                  </el-avatar>
                  <div>
                    <strong>{{ item.userName || '匿名用户' }}</strong>
                    <p>{{ formatDate(item.createTime) }}</p>
                  </div>
                </div>
                <div class="evaluation-score">
                  <el-rate :model-value="item.rating" disabled />
                  <span>{{ ratingText(item.rating) }}</span>
                </div>
              </div>

              <p class="evaluation-text">{{ item.content || '该用户没有留下更多文字。' }}</p>

              <div v-if="item.imageList.length" class="evaluation-images">
                <el-image
                  v-for="image in item.imageList"
                  :key="image"
                  class="evaluation-image"
                  :src="image"
                  :preview-src-list="item.imageList"
                  fit="cover"
                  preview-teleported
                />
              </div>
            </article>

            <el-empty v-if="!evaluations.length" description="还没有房源评价，欢迎成为第一个分享感受的人。" :image-size="92" />
          </div>
        </article>
      </section>

      <el-image-viewer
        v-if="showPreview"
        :url-list="imageList"
        :initial-index="currentIndex"
        @close="showPreview = false"
      />
    </template>

    <el-empty v-else description="房源不存在或已下架">
      <el-button type="primary" @click="router.push('/layout/dashboard')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ArrowLeft, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'
import { formatImageUrl } from '@/utils/imageUtils'

const route = useRoute()
const router = useRouter()
const store = useStore()

const REVIEW_IMAGE_LIMIT = 3
const REVIEW_IMAGE_MAX_SIZE = 2 * 1024 * 1024
const REVIEW_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const ratingTexts = ['很差', '较差', '一般', '满意', '非常满意']
const emojiList = [
  '😀', '😁', '😂', '🤣', '😊', '😄', '😎', '🥰', '😍', '😘',
  '🤗', '🤩', '🥳', '😌', '😮', '👏', '👍', '🙏', '💯', '🎉',
  '🏠', '🛋️', '🛏️', '🪟', '🌇', '🌳', '🚇', '🚗', '📍', '📷',
  '💡', '🔑', '🧡', '❤️', '🤝'
]

const routeId = computed(() => Number(route.params.id))
const role = computed(() => store.getters.role)
const userInfo = computed(() => store.getters.userInfo || {})
const normalizedRole = computed(() => role.value || sessionStorage.getItem('role') || '')
const isCustomer = computed(() => normalizedRole.value === 'ROLE_CUSTOMER')
const canReview = computed(() => {
  if (isCustomer.value) return true

  return Boolean(userInfo.value?.nickname || userInfo.value?.customerNickname)
})

const house = ref(null)
const pageLoading = ref(true)
const isFavorited = ref(false)
const showPreview = ref(false)
const currentIndex = ref(0)
const evaluations = ref([])
const submitting = ref(false)
const emojiPickerVisible = ref(false)
const reviewImages = ref([])
const reviewFileInputRef = ref(null)
const reviewInputRef = ref(null)

const evalForm = reactive({
  rating: 0,
  content: ''
})

const statusMap = {
  0: { label: '未发布', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已成交', type: 'danger' },
  3: { label: '已下架', type: 'warning' }
}

const statusLabel = computed(() => statusMap[house.value?.houseStatus]?.label || '状态未知')
const statusTagType = computed(() => statusMap[house.value?.houseStatus]?.type || 'info')

const imageList = computed(() => {
  const rawImages = parseImageArray(house.value?.images)
  return rawImages.length ? rawImages : ['/default-house.jpg']
})

const currentImage = computed(() => imageList.value[currentIndex.value] || imageList.value[0])

const primaryFacts = computed(() => {
  const currentHouse = house.value
  if (!currentHouse) return []

  return [
    { label: '户型', value: formatOptional(currentHouse.houseType) },
    { label: '面积', value: currentHouse.area ? `${currentHouse.area}㎡` : '' },
    { label: '朝向', value: formatOptional(currentHouse.orientation) },
    { label: '楼层', value: formatFloor(currentHouse.floor, currentHouse.totalFloor) },
    { label: '房屋类型', value: formatOptional(currentHouse.propertyType) },
    { label: '装修', value: formatOptional(currentHouse.decoration) }
  ].filter(item => item.value)
})

const fullAddress = computed(() => {
  const currentHouse = house.value
  if (!currentHouse) return ''

  return buildJoinedText([
    currentHouse.province,
    currentHouse.city,
    currentHouse.district,
    currentHouse.community,
    currentHouse.address
  ])
})

const propertyItems = computed(() => {
  const currentHouse = house.value
  if (!currentHouse) return []

  return [
    { label: '户型结构', value: formatOptional(currentHouse.layout), fullRow: true },
    { label: '建筑年代', value: currentHouse.buildingYear ? `${currentHouse.buildingYear}年` : '' },
    { label: '物业费', value: currentHouse.propertyFee ? `${currentHouse.propertyFee} 元/㎡/月` : '' },
    { label: '电梯', value: formatBooleanDetail(currentHouse.elevator) },
    { label: '供暖', value: formatBooleanDetail(currentHouse.heating) }
  ].filter(item => item.value)
})

const facilityChips = computed(() => {
  const currentHouse = house.value
  if (!currentHouse) return []

  const chips = [
    ...parseTagArray(currentHouse.tags),
    ...parseTagArray(currentHouse.facilities)
  ].filter(Boolean)

  return Array.from(new Set(chips))
})

const averageRating = computed(() => {
  if (!evaluations.value.length) return 0
  const total = evaluations.value.reduce((sum, item) => sum + Number(item.rating || 0), 0)
  return Number((total / evaluations.value.length).toFixed(1))
})

const reviewLabel = computed(() => {
  if (!evaluations.value.length) return '暂无评价'
  return ratingText(Math.round(averageRating.value) || 0)
})

function parseImageArray(value) {
  if (!value) return []

  if (Array.isArray(value)) {
    return value.map(normalizeImageUrl).filter(Boolean)
  }

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) {
        return parsed.map(normalizeImageUrl).filter(Boolean)
      }
    } catch {
      return value.split(',').map(item => normalizeImageUrl(item.trim())).filter(Boolean)
    }
  }

  return []
}

function normalizeImageUrl(url) {
  if (!url) return ''
  return formatImageUrl(url)
}

function formatFloor(floor, totalFloor) {
  if (floor && totalFloor) return `${floor}/${totalFloor}层`
  if (floor) return `${floor}层`
  return ''
}

function formatOptional(value) {
  if (value === null || value === undefined) return ''
  const text = String(value).trim()
  if (!text) return ''
  return text
}

function buildJoinedText(parts) {
  const normalized = parts.map(item => formatOptional(item)).filter(Boolean)
  if (!normalized.length) return ''

  const deduped = normalized.filter((item, index) => index === 0 || item !== normalized[index - 1])
  return deduped.join(' ')
}

function formatBooleanDetail(value) {
  if (value === 1) return '有'
  if (value === 0) return '无'
  return ''
}

function parseTagArray(value) {
  if (!value) return []

  if (Array.isArray(value)) {
    return value.map(item => formatOptional(item)).filter(Boolean)
  }

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) {
        return parsed.map(item => formatOptional(item)).filter(Boolean)
      }
    } catch {
      return value.split(',').map(item => formatOptional(item)).filter(Boolean)
    }
  }

  return []
}

function switchImage(index) {
  currentIndex.value = index
}

function ratingText(score) {
  if (score >= 5) return '非常满意'
  if (score >= 4) return '满意'
  if (score >= 3) return '一般'
  if (score >= 2) return '不太满意'
  return '暂无'
}

function formatPrice(price) {
  if (price === null || price === undefined || price === '') return '暂无'
  const value = Number(price)
  return Number.isNaN(value) ? '暂无' : value.toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return '未知时间'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatFileSize(size) {
  if (!size) return '0 KB'
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${Math.max(1, Math.round(size / 1024))} KB`
}

function resetReviewForm() {
  evalForm.rating = 0
  evalForm.content = ''
  emojiPickerVisible.value = false
  clearReviewImages()
}

function clearReviewImages() {
  reviewImages.value.forEach(image => {
    if (image.previewUrl?.startsWith('blob:')) URL.revokeObjectURL(image.previewUrl)
  })
  reviewImages.value = []
  if (reviewFileInputRef.value) {
    reviewFileInputRef.value.value = ''
  }
}

function appendEmoji(emoji) {
  evalForm.content += emoji
  emojiPickerVisible.value = false
  nextTick(() => {
    reviewInputRef.value?.textarea?.focus()
  })
}

function toggleEmojiPicker() {
  emojiPickerVisible.value = !emojiPickerVisible.value
}

function openReviewImagePicker() {
  reviewFileInputRef.value?.click()
}

function removeReviewImage(id) {
  const target = reviewImages.value.find(item => item.id === id)
  if (target?.previewUrl?.startsWith('blob:')) URL.revokeObjectURL(target.previewUrl)
  reviewImages.value = reviewImages.value.filter(item => item.id !== id)
}

function validateReviewImage(file, nextCount) {
  if (!REVIEW_IMAGE_TYPES.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 图片')
    return false
  }
  if (file.size > REVIEW_IMAGE_MAX_SIZE) {
    ElMessage.warning('单张图片不能超过 2MB')
    return false
  }
  if (nextCount > REVIEW_IMAGE_LIMIT) {
    ElMessage.warning('最多上传 3 张图片')
    return false
  }
  return true
}

function pushReviewFile(file, fallbackName = '') {
  const nextCount = reviewImages.value.length + 1
  if (!validateReviewImage(file, nextCount)) return

  reviewImages.value.push({
    id: `${file.name || fallbackName || 'image'}-${file.size}-${Date.now()}-${nextCount}`,
    name: file.name || fallbackName || '图片',
    size: file.size,
    file,
    previewUrl: URL.createObjectURL(file)
  })
}

function handleReviewImageChange(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) return

  files.forEach(file => {
    pushReviewFile(file)
  })

  if (reviewFileInputRef.value) {
    reviewFileInputRef.value.value = ''
  }
}

function handleReviewPaste(event) {
  const items = Array.from(event.clipboardData?.items || [])
  const imageItems = items.filter(item => item.type?.startsWith('image/'))
  if (!imageItems.length) return

  imageItems.forEach(item => {
    const file = item.getAsFile()
    if (file) pushReviewFile(file, '粘贴图片')
  })
}

async function uploadReviewImages() {
  if (!reviewImages.value.length) return []

  const uploadedUrls = []
  for (const image of reviewImages.value) {
    const formData = new FormData()
    formData.append('file', image.file)
    const res = await request.post('/file/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    const imageUrl = normalizeImageUrl(res.data?.url || res.url)
    if (imageUrl) uploadedUrls.push(imageUrl)
  }

  return uploadedUrls
}

async function loadHouse() {
  if (!routeId.value) {
    pageLoading.value = false
    return
  }

  try {
    const res = await request.get(`/house/detail/${routeId.value}`)
    house.value = res.data
    currentIndex.value = 0
  } catch (error) {
    console.error('Failed to load house detail:', error)
    house.value = null
  } finally {
    pageLoading.value = false
  }
}

async function loadEvaluations() {
  try {
    const res = await request.get('/review/list', {
      params: {
        houseId: routeId.value,
        targetType: 1,
        pageNum: 1,
        pageSize: 20
      }
    })

    const list = res.data?.list || res.data || []
    evaluations.value = list.map(item => ({
      id: item.id,
      userName: item.customerName || item.customerNickname || '匿名用户',
      avatarUrl: normalizeImageUrl(item.customerAvatar),
      rating: Number(item.rating || 0),
      content: item.content || '',
      createTime: item.createTime,
      imageList: parseImageArray(item.images)
    }))
  } catch (error) {
    console.error('Failed to load house reviews:', error)
    evaluations.value = []
  }
}

async function submitEvaluation() {
  if (!sessionStorage.getItem('token')) {
    ElMessage.warning('请先登录后再评价')
    router.push('/login')
    return
  }
  if (!evalForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  if (!evalForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!house.value?.agentId) {
    ElMessage.warning('当前房源暂无关联中介，暂时无法提交评价')
    return
  }

  submitting.value = true
  try {
    const uploadedImages = await uploadReviewImages()
    await request.post('/review/submit', {
      houseId: routeId.value,
      agentId: house.value.agentId,
      targetType: 1,
      rating: Number(evalForm.rating),
      content: evalForm.content.trim(),
      images: JSON.stringify(uploadedImages)
    })

    ElMessage.success('房源评价发布成功')
    resetReviewForm()
    await loadEvaluations()
  } catch (error) {
    console.error('Failed to submit review:', error)
    ElMessage.error(error.response?.data?.message || error.message || '评价提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function checkFavorite() {
  const customerId = userInfo.value?.userId || userInfo.value?.id
  if (!sessionStorage.getItem('token') || !isCustomer.value || !customerId || !routeId.value) {
    isFavorited.value = false
    return
  }

  try {
    const res = await request.get('/favorite/check', {
      params: {
        customerId: Number(customerId),
        houseId: routeId.value
      }
    })
    isFavorited.value = res.data === true || res.data === 'true'
  } catch {
    isFavorited.value = false
  }
}

async function toggleFavorite() {
  const customerId = userInfo.value?.userId || userInfo.value?.id
  if (!sessionStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!isCustomer.value) {
    ElMessage.warning('只有客户账号可以收藏房源')
    return
  }
  if (!customerId) {
    ElMessage.error('未获取到当前客户信息')
    return
  }

  try {
    if (isFavorited.value) {
      await ElMessageBox.confirm('确认取消收藏这套房源吗？', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await request.delete('/favorite/remove', {
        params: {
          customerId: Number(customerId),
          houseId: routeId.value
        }
      })
      isFavorited.value = false
      ElMessage.success('已取消收藏')
      return
    }

    await request.post('/favorite/add', null, {
      params: {
        customerId: Number(customerId),
        houseId: routeId.value
      }
    })
    isFavorited.value = true
    ElMessage.success('收藏成功')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('Favorite action failed:', error)
      ElMessage.error('收藏操作失败，请稍后重试')
    }
  }
}

function handleContact() {
  if (!sessionStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!house.value?.agentId) {
    ElMessage.warning('暂无中介联系方式')
    return
  }

  router.push({
    path: '/layout/message',
    query: {
      targetId: house.value.agentId,
      targetType: 'agent',
      targetName: house.value.agentName,
      currentHouseId: house.value.id,
      currentHouseTitle: house.value.title
    }
  })
}

async function initPage() {
  pageLoading.value = true
  house.value = null
  evaluations.value = []
  currentIndex.value = 0
  showPreview.value = false
  resetReviewForm()

  await loadHouse()
  await Promise.all([loadEvaluations(), checkFavorite()])
}

watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      initPage()
    }
  }
)

onMounted(() => {
  initPage()
})

onBeforeUnmount(() => {
  clearReviewImages()
})
</script>

<style scoped>
.house-detail-page {
  max-width: 1360px;
  margin: 0 auto;
  padding: 28px 24px 40px;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.14), transparent 22%),
    linear-gradient(180deg, #f5fbf7 0%, #eef7f1 100%);
}

.detail-skeleton {
  border-radius: 28px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.92);
}

.listing-shell,
.review-card-shell {
  border-radius: 32px;
  border: 1px solid rgba(5, 150, 105, 0.12);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 40px rgba(6, 78, 59, 0.08);
}

.listing-shell {
  padding: 24px;
}

.review-shell {
  margin-top: 22px;
}

.hero-topbar,
.hero-topbar-right,
.section-head,
.card-head,
.agent-card-head,
.action-row,
.compose-top,
.compose-toolbar,
.toolbar-left,
.evaluation-head,
.evaluation-user,
.evaluation-score {
  display: flex;
  align-items: center;
}

.hero-topbar,
.section-head,
.card-head,
.compose-toolbar,
.evaluation-head {
  justify-content: space-between;
}

.hero-topbar {
  margin-bottom: 18px;
}

.hero-topbar-right {
  gap: 12px;
}

.back-button {
  border-radius: 999px;
}

.view-pill,
.card-tip {
  padding: 8px 14px;
  border-radius: 999px;
  color: #0f766e;
  background: rgba(236, 253, 245, 0.95);
  font-size: 12px;
  font-weight: 600;
}

.listing-body {
  display: grid;
  grid-template-columns: minmax(0, 1.16fr) minmax(380px, 0.84fr);
  gap: 28px;
  align-items: stretch;
}

.listing-left {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 100%;
}

.listing-right {
  min-width: 0;
  min-height: 100%;
}

.gallery-card,
.description-card,
.info-card,
.compose-card {
  border-radius: 28px;
  border: 1px solid rgba(5, 150, 105, 0.12);
}

.gallery-card {
  padding: 18px;
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.96), rgba(236, 253, 245, 0.68));
}

.description-card {
  padding: 22px 24px;
  min-height: 236px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 244, 0.86));
}

.address-value {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.7;
}

.standalone-address {
  margin-bottom: 0;
}

.description-content {
  margin-top: 14px;
  height: 128px;
  overflow: auto;
  padding-right: 6px;
}

.info-card {
  padding: 26px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 244, 0.84));
  box-shadow: 0 16px 32px rgba(6, 78, 59, 0.06);
}

.info-overview-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 100%;
  justify-content: space-between;
}

.price-block {
  padding: 2px 0 0;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  padding-bottom: 16px;
}

.price-location {
  margin: 14px 0 0;
  color: #0f766e;
  font-size: 18px;
  line-height: 1.75;
  font-weight: 600;
}

.agent-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
  border-radius: 24px;
  border: 1px solid rgba(5, 150, 105, 0.1);
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.92), rgba(255, 255, 255, 0.98));
  margin-top: auto;
}

.main-image {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  min-height: 480px;
  cursor: pointer;
  background: #f0fdf4;
}

.main-image img {
  width: 100%;
  height: 100%;
  min-height: 480px;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.main-image:hover img {
  transform: scale(1.03);
}

.image-order,
.image-tip {
  position: absolute;
  padding: 6px 12px;
  border-radius: 999px;
  color: #fff;
  background: rgba(15, 23, 42, 0.58);
  font-size: 12px;
  font-weight: 600;
}

.image-order {
  top: 16px;
  left: 16px;
}

.image-tip {
  right: 16px;
  bottom: 16px;
}

.thumb-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(92px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.thumb-button {
  padding: 0;
  border: 0;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  background: transparent;
  box-shadow: inset 0 0 0 2px transparent;
}

.thumb-button.active {
  box-shadow: inset 0 0 0 2px #10b981;
}

.thumb-button img {
  width: 100%;
  height: 76px;
  object-fit: cover;
  display: block;
}

.eyebrow,
.section-kicker,
.agent-kicker {
  margin: 0 0 10px;
  color: #059669;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.house-title,
.card-head h2,
.review-copy h2,
.compose-top h3,
.agent-name {
  margin: 0;
  color: #0f172a;
}

.house-title {
  font-size: 30px;
  line-height: 1.35;
}

.price-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-value {
  font-size: 42px;
  line-height: 1;
  font-weight: 800;
  color: #dc2626;
}

.price-unit {
  color: #64748b;
  font-size: 16px;
}

.price-subline,
.agent-phone,
.section-desc,
.compose-top p,
.description-text,
.evaluation-text,
.detail-address {
  color: #64748b;
  line-height: 1.85;
}

.price-subline,
.agent-phone,
.section-desc,
.compose-top p,
.description-text,
.detail-address {
  margin: 0;
}

.description-text,
.detail-address {
  white-space: pre-wrap;
  word-break: break-word;
}

.house-facts-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding: 2px 0;
}

.house-fact-card {
  min-height: 84px;
  padding: 12px 12px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(244, 252, 247, 0.96), rgba(237, 250, 243, 0.82));
  border: 1px solid rgba(5, 150, 105, 0.08);
}

.house-fact-card span {
  display: block;
  color: #64748b;
  font-size: 15px;
}

.house-fact-card strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.35;
}

.detail-sections,
.evaluation-images,
.review-image-grid {
  display: grid;
  gap: 12px;
}

.detail-sections {
  gap: 12px;
}

.detail-block {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(5, 150, 105, 0.08);
}

.detail-property,
.detail-facility {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(240, 253, 244, 0.72));
}

.detail-block-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.detail-block-head strong {
  color: #0f172a;
  font-size: 18px;
}

.detail-block-head span {
  display: block;
  color: #64748b;
  font-size: 14px;
}

.detail-inline-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
}

.detail-inline-item {
  min-width: 120px;
  flex: 1 1 44%;
}

.detail-inline-item.is-full {
  flex-basis: 100%;
}

.detail-inline-item span {
  display: block;
  color: #64748b;
  font-size: 15px;
}

.detail-empty {
  padding: 14px 0 2px;
  color: #64748b;
  line-height: 1.8;
}

.detail-inline-item strong {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 17px;
  line-height: 1.45;
  word-break: break-word;
  white-space: pre-wrap;
}

.detail-inline-list-compact .detail-inline-item {
  min-width: 96px;
  flex-basis: 30%;
}

.detail-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-chip {
  display: inline-flex;
  align-items: center;
  padding: 11px 16px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 0.9);
  color: #047857;
  font-size: 14px;
  font-weight: 700;
}

.agent-card-head {
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.action-row {
  gap: 12px;
}

.action-button {
  flex: 1;
  min-height: 48px;
  border-radius: 16px;
  font-weight: 700;
}

.favorite-active {
  color: #fff !important;
  border-color: #f59e0b !important;
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
}

.review-card-shell {
  padding: 20px;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top right, rgba(16, 185, 129, 0.08), transparent 16%),
    rgba(255, 255, 255, 0.95);
}

.review-head {
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.review-copy {
  flex: 1;
  min-width: 0;
}

.review-score-box {
  flex-shrink: 0;
  width: 140px;
  padding: 18px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  text-align: center;
}

.review-score-box strong {
  display: block;
  color: #065f46;
  font-size: 36px;
  line-height: 1;
  font-weight: 800;
}

.review-score-box small {
  display: block;
  margin-top: 6px;
  color: #059669;
  font-size: 13px;
  font-weight: 600;
}

.review-count-text {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
}

.compose-card {
  margin-top: 20px;
  padding: 22px;
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.88), rgba(255, 255, 255, 0.98));
}

.compose-top {
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}

.compose-rating {
  min-width: 180px;
}

.compose-rating span {
  display: block;
  color: #475569;
  font-size: 13px;
  margin-bottom: 8px;
}

.emoji-panel {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.emoji-chip {
  border: 0;
  border-radius: 14px;
  padding: 8px 0;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  font-size: 20px;
  box-shadow: inset 0 0 0 1px rgba(16, 185, 129, 0.12);
}

.review-image-card {
  overflow: hidden;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 10px 22px rgba(6, 78, 59, 0.08);
}

.review-image-card img {
  width: 100%;
  height: 128px;
  object-fit: cover;
  display: block;
}

.review-image-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px 0;
  color: #475569;
  font-size: 12px;
}

.review-image-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-image-button {
  width: calc(100% - 24px);
  margin: 12px;
  border: 0;
  border-radius: 12px;
  padding: 10px 0;
  background: rgba(254, 226, 226, 0.92);
  color: #dc2626;
  cursor: pointer;
  font-weight: 700;
}

.compose-toolbar {
  margin-top: 18px;
  gap: 14px;
}

.toolbar-left {
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-hint {
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 18px;
}

.evaluation-card {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.94));
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.evaluation-user {
  gap: 12px;
}

.evaluation-avatar {
  background: linear-gradient(135deg, #10b981, #0f766e);
  color: #fff;
}

.evaluation-user strong {
  color: #0f172a;
}

.evaluation-user p,
.evaluation-score span {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.evaluation-score {
  gap: 10px;
}

.evaluation-text {
  margin: 14px 0 0;
  white-space: pre-wrap;
}

.evaluation-images {
  grid-template-columns: repeat(auto-fit, minmax(110px, 140px));
  margin-top: 14px;
}

.evaluation-image {
  width: 100%;
  height: 110px;
  border-radius: 16px;
  overflow: hidden;
}

.hidden-file-input {
  display: none;
}

@media (max-width: 1180px) {
  .listing-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .house-detail-page {
    padding: 16px 12px 28px;
  }

  .listing-shell,
  .review-card-shell {
    padding: 18px;
    border-radius: 24px;
  }

  .hero-topbar,
  .hero-topbar-right,
  .card-head,
  .agent-card-head,
  .action-row,
  .section-head,
  .compose-top,
  .compose-toolbar,
  .evaluation-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .main-image,
  .main-image img {
    min-height: 320px;
  }

  .house-title {
    font-size: 24px;
  }

  .price-value {
    font-size: 34px;
  }

  .house-facts-panel {
    display: grid;
    grid-template-columns: 1fr;
  }

  .description-card {
    min-height: 220px;
  }

  .description-content {
    height: auto;
    max-height: 220px;
  }

  .detail-inline-list,
  .detail-chip-list {
    display: grid;
    grid-template-columns: 1fr;
  }

  .emoji-panel,
  .review-image-grid,
  .evaluation-images {
    grid-template-columns: 1fr;
  }

  .evaluation-score {
    margin-top: 10px;
  }
}
</style>
