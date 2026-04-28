<template>
  <div class="viewing-manage-page page-shell page-shell--has-summary">
    <section class="viewing-hero page-shell-hero">
      <div class="viewing-hero__content page-shell-hero__content">
        <p class="eyebrow">Viewing Management Desk</p>
        <h1>带看管理</h1>
        <p class="viewing-hero__text hero-text">
          统一查看预约、确认与完成进度，快速跟进客户带看安排，并保持和看板一致的页面外壳。
        </p>
      </div>
    </section>

    <section class="summary-grid page-shell-summary page-shell-summary--5 manage-summary">
      <button
        v-for="item in summaryCards"
        :key="item.key"
        type="button"
        class="summary-card"
        :class="{ active: activeQuickFilter === item.key, 'is-disabled': item.disabled }"
        :disabled="item.disabled"
        @click="applyQuickFilter(item.key)"
      >
        <span class="summary-label">{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.tip }}</small>
      </button>
    </section>

    <el-card shadow="never" class="filter-card page-shell-filter">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索客户、房源标题、地址"
          clearable
          class="filter-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="带看状态" clearable class="filter-select">
          <el-option label="待中介确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
          <el-option label="待客户确认" :value="4" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-skeleton :loading="loading" animated :count="6">
      <template #template>
        <div class="viewing-grid">
          <div v-for="n in 6" :key="n" class="viewing-card-skeleton">
            <el-skeleton-item variant="image" style="height: 200px" />
            <div class="skeleton-body">
              <el-skeleton-item variant="h3" style="width: 64%" />
              <el-skeleton-item variant="text" style="width: 88%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 44%; margin-top: 12px" />
            </div>
          </div>
        </div>
      </template>

      <template #default>
        <el-empty v-if="!filteredViewings.length" description="当前筛选条件下没有带看记录" />

        <div v-else class="viewing-grid">
          <article v-for="viewing in filteredViewings" :key="viewing.id" class="viewing-card">
            <div class="viewing-image" @click="handleViewDetail(viewing)">
              <img :src="getHouseImage(viewing.houseId)" :alt="viewing.houseTitle" @error="handleImageError" />
              <div class="badge-row">
                <el-tag size="small" effect="dark" :type="getStatusType(viewing.status)">
                  {{ getStatusLabel(viewing.status) }}
                </el-tag>
                <el-tag v-if="viewing.agentReviewSubmitted" size="small" effect="plain" type="success">
                  已评价
                </el-tag>
              </div>
              <div class="image-overlay">
                <span>{{ formatTime(viewing.appointTime) }}</span>
              </div>
            </div>

            <div class="viewing-body">
              <div class="title-row">
                <h3 @click="handleViewDetail(viewing)">{{ viewing.houseTitle }}</h3>
                <span class="customer-name">{{ viewing.customerName || viewing.customerPhone }}</span>
              </div>

              <p class="viewing-address">{{ viewing.houseAddress || '未填写带看地址' }}</p>

              <div class="meta-list">
                <div class="meta-item">
                  <span>客户电话</span>
                  <strong>{{ viewing.customerPhone || '未填写' }}</strong>
                </div>
                <div class="meta-item">
                  <span>预约时间</span>
                  <strong>{{ formatTime(viewing.appointTime) }}</strong>
                </div>
                <div v-if="viewing.actualTime" class="meta-item">
                  <span>完成时间</span>
                  <strong>{{ formatTime(viewing.actualTime) }}</strong>
                </div>
                <div v-if="viewing.remark" class="meta-item full">
                  <span>备注</span>
                  <strong>{{ viewing.remark }}</strong>
                </div>
                <div v-if="viewing.cancelReason" class="meta-item full danger">
                  <span>取消原因</span>
                  <strong>{{ viewing.cancelReason }}</strong>
                </div>
              </div>

              <div class="action-row">
                <el-button plain @click="handleViewDetail(viewing)">详情</el-button>
                <el-button
                  v-if="viewing.status === 0"
                  type="success"
                  plain
                  @click="handleConfirm(viewing)"
                >
                  确认带看
                </el-button>
                <el-button
                  v-if="viewing.status === 4"
                  type="success"
                  plain
                  @click="handleCustomerConfirm(viewing)"
                >
                  确认带看
                </el-button>
                <el-button
                  v-if="viewing.status === 4"
                  type="danger"
                  plain
                  @click="handleReject(viewing)"
                >
                  拒绝
                </el-button>
                <el-button
                  v-if="viewing.status === 1"
                  type="primary"
                  plain
                  :disabled="!canCompleteViewing(viewing)"
                  :title="completeViewingHint(viewing)"
                  @click="handleComplete(viewing)"
                >
                  完成带看
                </el-button>
                <el-button
                  v-if="viewing.status === 0 || viewing.status === 1 || viewing.status === 4"
                  type="danger"
                  plain
                  @click="handleCancel(viewing)"
                >
                  取消带看
                </el-button>
              </div>
            </div>
          </article>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[8, 12, 16]"
            layout="total, sizes, prev, pager, next"
            @size-change="handlePageSizeChange"
            @current-change="loadViewingList"
          />
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="detailVisible" title="带看详情" width="760px">
      <el-descriptions v-if="currentDetail" :column="2" border>
        <el-descriptions-item label="房源标题" :span="2">{{ currentDetail.houseTitle }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentDetail.customerName || currentDetail.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(currentDetail.status) }}</el-descriptions-item>
        <el-descriptions-item label="客户电话">{{ currentDetail.customerPhone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatTime(currentDetail.appointTime) }}</el-descriptions-item>
        <el-descriptions-item label="房源地址" :span="2">{{ currentDetail.houseAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.actualTime" label="完成时间">
          {{ formatTime(currentDetail.actualTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.cancelReason" label="取消原因">
          {{ currentDetail.cancelReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.remark" label="备注" :span="2">
          {{ currentDetail.remark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import request from '@/api'
import { formatImageUrls } from '@/utils/imageUtils'

const store = useStore()
const COMPLETE_GRACE_PERIOD_MS = 30 * 60 * 1000
const userRole = computed(() => store.getters.role || '')
const currentUserId = computed(() => store.getters.userInfo?.userId || store.getters.userInfo?.id)
const loading = ref(false)
const detailVisible = ref(false)
const currentDetail = ref(null)
const rawViewings = ref([])
const houseImageMap = ref({})

const filters = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 8,
  total: 0
})

const activeQuickFilter = ref('all')
const summary = reactive({
  total: 0,
  pending: 0,
  confirmed: 0,
  completed: 0,
  cancelled: 0
})

const filteredViewings = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return rawViewings.value.filter(item => {
    const matchKeyword =
      !keyword ||
      [item.customerName, item.customerPhone, item.houseTitle, item.houseAddress]
        .filter(Boolean)
        .some(value => String(value).toLowerCase().includes(keyword))

    return matchKeyword
  })
})

const summaryCards = computed(() => [
  { key: 'all', label: '全部带看', value: summary.total, tip: '当前中介名下的全部带看' },
  { key: 'pending', label: '待确认', value: summary.pending, tip: '需要尽快确认的预约' },
  { key: 'confirmed', label: '已确认', value: summary.confirmed, tip: '已确认但未完成的带看' },
  { key: 'completed', label: '已完成', value: summary.completed, tip: '可继续推进交易和评价' },
  { key: 'cancelled', label: '已取消', value: summary.cancelled, tip: '已取消的带看记录' }
])

const buildViewingListParams = () => {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize
  }
  if (userRole.value !== 'ROLE_ADMIN') {
    params.agentId = currentUserId.value
  }
  params.status = filters.status ?? undefined
  return params
}

const buildViewingSummaryParams = status => {
  const params = {
    pageNum: 1,
    pageSize: 1
  }
  if (userRole.value !== 'ROLE_ADMIN') {
    params.agentId = currentUserId.value
  }
  if (status !== undefined) {
    params.status = status
  }
  return params
}

const loadHouseImages = async houseIds => {
  const missingIds = houseIds.filter(id => id && !houseImageMap.value[id])
  if (!missingIds.length) {
    return
  }

  const responses = await Promise.all(
    missingIds.map(async id => {
      try {
        const res = await request.get(`/house/detail/${id}`)
        return [id, formatImageUrls(res.data?.images || [])[0] || '/default-house.jpg']
      } catch (error) {
        return [id, '/default-house.jpg']
      }
    })
  )

  houseImageMap.value = {
    ...houseImageMap.value,
    ...Object.fromEntries(responses)
  }
}

const loadViewingList = async () => {
  loading.value = true
  try {
    const res = await request.get('/viewing/list', { params: buildViewingListParams() })
    rawViewings.value = res.data?.list || []
    pagination.total = res.data?.total || 0
    await loadHouseImages(rawViewings.value.map(item => item.houseId))
  } catch (error) {
    console.error('加载带看列表失败:', error)
    ElMessage.error('加载带看列表失败')
  } finally {
    loading.value = false
  }
}

const loadSummary = async () => {
  try {
    const [allRes, pendingRes, confirmedRes, completedRes, cancelledRes] = await Promise.all([
      request.get('/viewing/list', { params: buildViewingSummaryParams() }),
      request.get('/viewing/list', { params: buildViewingSummaryParams(0) }),
      request.get('/viewing/list', { params: buildViewingSummaryParams(1) }),
      request.get('/viewing/list', { params: buildViewingSummaryParams(2) }),
      request.get('/viewing/list', { params: buildViewingSummaryParams(3) })
    ])

    summary.total = allRes.data?.total || 0
    summary.pending = pendingRes.data?.total || 0
    summary.confirmed = confirmedRes.data?.total || 0
    summary.completed = completedRes.data?.total || 0
    summary.cancelled = cancelledRes.data?.total || 0
  } catch (error) {
    console.error('加载带看统计失败:', error)
  }
}

const refreshViewingPage = async () => {
  await Promise.all([loadViewingList(), loadSummary()])
}

const applyQuickFilter = key => {
  activeQuickFilter.value = key
  filters.keyword = ''

  if (key === 'all') {
    filters.status = null
  } else if (key === 'pending') {
    filters.status = 0
  } else if (key === 'confirmed') {
    filters.status = 1
  } else if (key === 'completed') {
    filters.status = 2
  } else if (key === 'cancelled') {
    filters.status = 3
  }

  pagination.pageNum = 1
  loadViewingList()
}

const handleSearch = () => {
  activeQuickFilter.value = 'custom'
  pagination.pageNum = 1
  loadViewingList()
}

const resetFilters = () => {
  activeQuickFilter.value = 'all'
  filters.keyword = ''
  filters.status = null
  pagination.pageNum = 1
  loadViewingList()
}

const handlePageSizeChange = () => {
  pagination.pageNum = 1
  loadViewingList()
}

const handleViewDetail = async row => {
  try {
    const res = await request.get(`/viewing/detail/${row.id}`)
    currentDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('加载带看详情失败:', error)
    ElMessage.error('加载带看详情失败')
  }
}

const canCompleteViewing = viewing => {
  if (!viewing || viewing.status !== 1 || !viewing.appointTime) {
    return false
  }
  const appointTime = new Date(viewing.appointTime)
  if (Number.isNaN(appointTime.getTime())) {
    return false
  }
  return Date.now() >= appointTime.getTime() + COMPLETE_GRACE_PERIOD_MS
}

const completeViewingHint = viewing =>
  canCompleteViewing(viewing) ? '预约开始30分钟后可完成带看' : '预约开始30分钟后才能完成带看'

const handleConfirm = async row => {
  try {
    await ElMessageBox.confirm(`确认接受“${row.houseTitle}”这次带看吗？`, '确认带看', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/viewing/confirm/${row.id}`)
    ElMessage.success('带看已确认')
    await refreshViewingPage()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认带看失败:', error)
      ElMessage.error('确认带看失败')
    }
  }
}

const handleComplete = async row => {
  if (!canCompleteViewing(row)) {
    ElMessage.warning('预约开始30分钟后才能完成带看')
    return
  }
  try {
    await ElMessageBox.confirm(
      '完成带看后，系统会向客户发送本次服务评价邀请，确认继续吗？',
      '完成带看',
      {
        confirmButtonText: '确认完成',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await request.post(`/viewing/complete/${row.id}`)
    ElMessage.success('带看已完成')
    await refreshViewingPage()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成带看失败:', error)
      ElMessage.error('完成带看失败')
    }
  }
}

const handleCancel = async row => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因，方便后续查看记录。', '取消带看', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '例如：客户临时有事，改约下周',
      inputValidator: val => (val && val.trim() ? true : '请填写取消原因')
    })

    await request.post(`/viewing/cancel/${row.id}`, null, {
      params: {
        reason: value
      }
    })
    ElMessage.success('带看已取消')
    await refreshViewingPage()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消带看失败:', error)
      ElMessage.error('取消带看失败')
    }
  }
}

// 客户确认中介发起的带看
const handleCustomerConfirm = async row => {
  try {
    await ElMessageBox.confirm(`确认接受“${row.houseTitle}”的带看邀请吗？`, '确认带看', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    await request.post(`/viewing/customer/confirm/${row.id}`)
    ElMessage.success('已确认带看')
    await refreshViewingPage()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认带看失败:', error)
      ElMessage.error('确认带看失败')
    }
  }
}

// 客户拒绝中介发起的带看
const handleReject = async row => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（选填）。', '拒绝带看', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：时间不合适，改约其他时间'
    })

    await request.post(`/viewing/cancel/${row.id}`, null, {
      params: {
        reason: value
      }
    })
    ElMessage.success('已拒绝带看')
    await refreshViewingPage()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝带看失败:', error)
      ElMessage.error('拒绝带看失败')
    }
  }
}

const formatTime = time => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusLabel = status =>
  ({
    0: '待中介确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '待客户确认'
  }[status] || '未知状态')

const getStatusType = status =>
  ({
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'info'
  }[status] || 'info')

const getHouseImage = houseId => houseImageMap.value[houseId] || '/default-house.jpg'

const handleImageError = event => {
  event.target.src = '/default-house.jpg'
}

onMounted(async () => {
  await refreshViewingPage()
})
</script>

<style scoped>
.viewing-manage-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
}

.viewing-hero {
  margin-bottom: 18px;
  padding: 0;
  border-radius: 16px;
}

.viewing-hero__content {
  flex: 1;
  padding: 38px 32px 32px;
  min-width: 0;
}

.viewing-hero__text {
  max-width: 620px;
  margin-top: 12px;
}

.summary-grid {
  margin-bottom: 18px;
}

.manage-summary .summary-card.is-disabled,
.manage-summary .summary-card:disabled {
  cursor: default;
  opacity: 1;
}

.manage-summary .summary-card.is-disabled:hover,
.manage-summary .summary-card:disabled:hover {
  transform: none;
  border-color: transparent;
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.1);
  background: var(--page-shell-surface-strong);
}

.filter-card {
  margin-bottom: 18px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-input {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  width: 140px;
}

.viewing-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.viewing-card,
.viewing-card-skeleton {
  overflow: hidden;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.viewing-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(6, 78, 59, 0.08);
  border-color: rgba(5, 150, 105, 0.2);
}

.viewing-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
}

.viewing-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.viewing-card:hover .viewing-image img {
  transform: scale(1.05);
}

.viewing-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0.4) 100%);
}

.badge-row,
.image-overlay {
  position: absolute;
  z-index: 1;
  left: 12px;
  right: 12px;
  display: flex;
}

.badge-row {
  top: 12px;
  justify-content: space-between;
}

.image-overlay {
  bottom: 12px;
  justify-content: flex-end;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
}

.viewing-body {
  padding: 16px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
  margin-bottom: 8px;
}

.title-row h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
  cursor: pointer;
  font-weight: 600;
  line-height: 1.4;
  flex: 1;
}

.title-row h3:hover {
  color: #059669;
}

.customer-name {
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
}

.viewing-address {
  margin: 0 0 12px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}

.meta-item {
  padding: 8px;
  border-radius: 8px;
  background: #f9fafb;
  font-size: 12px;
}

.meta-item.full {
  grid-column: 1 / -1;
}

.meta-item.danger {
  background: #fef2f2;
  color: #ef4444;
}

.meta-item span {
  display: block;
  color: #9ca3af;
  margin-bottom: 2px;
}

.meta-item strong {
  color: #374151;
  font-weight: 500;
}

.meta-item.danger strong {
  color: #ef4444;
}

.action-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-row .el-button {
  flex: 1;
  border-radius: 6px;
  font-size: 13px;
}

.skeleton-body {
  padding: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .viewing-manage-page {
    padding: 16px;
  }

  .viewing-hero {
    padding: 0;
  }

  .viewing-hero__content {
    padding: 28px 20px 24px;
  }

  .summary-grid {
    margin-bottom: 16px;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }

  .viewing-grid {
    grid-template-columns: 1fr;
  }
}
</style>
