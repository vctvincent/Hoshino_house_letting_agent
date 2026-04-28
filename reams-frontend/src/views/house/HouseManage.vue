<template>
  <div class="house-manage-page page-shell page-shell--has-summary">
    <section class="hero page-shell-hero page-shell-hero--split manage-hero">
      <div class="manage-hero__content page-shell-hero__content">
        <p class="eyebrow">Agent Inventory Studio</p>
        <h1>我的房源管理</h1>
        <p class="hero-text">沿用首页的图卡浏览方式，同时把编辑、审核状态和成交进度都放到同一屏里。</p>
      </div>
      <div class="manage-hero__actions page-shell-hero__aside page-shell-hero__aside--action">
        <el-button
          type="primary"
          size="large"
          class="manage-hero__button"
          @click="handleCreate"
        >
          新增房源
        </el-button>
      </div>
    </section>
    <section class="summary-grid page-shell-summary manage-summary">
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

    <section class="main-content page-shell-stack">
      <!-- 筛选卡片 -->
      <el-card shadow="never" class="filter-card page-shell-filter">
        <div class="filter-row">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题、小区、地址"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filters.houseStatus" placeholder="房源状态" clearable class="filter-select">
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已成交" :value="2" />
            <el-option label="已下架" :value="3" />
          </el-select>
          <el-select v-model="filters.auditStatus" placeholder="审核状态" clearable class="filter-select">
            <el-option label="待审核" :value="0" />
            <el-option label="审核中" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </el-card>

      <!-- 房源列表 -->
      <el-skeleton :loading="loading" animated :count="6">
        <template #template>
          <div class="house-grid">
            <div v-for="n in 6" :key="n" class="house-card-skeleton">
              <el-skeleton-item variant="image" style="height: 210px" />
              <div class="skeleton-body">
                <el-skeleton-item variant="h3" style="width: 68%" />
                <el-skeleton-item variant="text" style="width: 92%; margin-top: 10px" />
                <el-skeleton-item variant="text" style="width: 56%; margin-top: 12px" />
              </div>
            </div>
          </div>
        </template>

        <template #default>
          <el-empty v-if="!houseList.length" description="当前筛选条件下没有房源" />

          <div v-else class="house-grid">
            <article v-for="house in houseList" :key="house.id" class="house-card">
              <div class="house-image" @click="goToDetail(house.id)">
                <img :src="getHouseImage(house.images)" :alt="house.title" @error="handleImageError" />
                <div class="house-image-overlay">
                  <span>{{ house.viewCount || 0 }} 次浏览</span>
                  <span>{{ house.favoriteCount || 0 }} 次收藏</span>
                </div>
                <div class="badge-row">
                  <el-tag size="small" effect="dark" :type="getHouseStatusType(house.houseStatus)">
                    {{ getHouseStatusLabel(house.houseStatus) }}
                  </el-tag>
                  <el-tag size="small" effect="plain" :type="getAuditStatusType(house.auditStatus)">
                    {{ getAuditStatusLabel(house.auditStatus) }}
                  </el-tag>
                </div>
              </div>

              <div class="house-body">
                <div class="house-topline">
                  <h3 @click="goToDetail(house.id)">{{ house.title }}</h3>
                  <span class="unit-price">{{ formatUnitPrice(house) }}</span>
                </div>
                <p class="house-address">{{ formatAddress(house) }}</p>

                <div class="house-tags">
                  <el-tag size="small" effect="plain">{{ house.houseType || '待补充户型' }}</el-tag>
                  <el-tag size="small" type="info" effect="plain">{{ house.area || '--' }} ㎡</el-tag>
                  <el-tag v-if="house.floor" size="small" type="success" effect="plain">{{ house.floor }}</el-tag>
                  <el-tag v-if="house.decoration" size="small" type="warning" effect="plain">
                    {{ house.decoration }}
                  </el-tag>
                </div>

                <div class="price-row">
                  <div class="total-price">
                    <span class="amount">{{ formatPrice(house.price) }}</span>
                    <span class="unit">万元</span>
                  </div>
                  <span class="community">{{ house.community || '未填写小区' }}</span>
                </div>

                <p v-if="house.rejectReason && house.auditStatus === 3" class="reject-reason">
                  驳回原因：{{ house.rejectReason }}
                </p>

                <div class="action-row">
                  <el-button plain @click="goToDetail(house.id)">查看详情</el-button>
                  <el-button type="primary" plain @click="handleEdit(house)">编辑</el-button>
                  <el-button type="danger" plain @click="handleDelete(house)">删除</el-button>
                </div>
              </div>
            </article>
          </div>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 15, 20]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadData"
              @size-change="handlePageSizeChange"
            />
          </div>
        </template>
      </el-skeleton>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'
import { formatImageUrls } from '@/utils/imageUtils'

const router = useRouter()
const store = useStore()
const userId = store.getters.userInfo.userId

const loading = ref(false)
const houseList = ref([])

const filters = reactive({
  keyword: '',
  houseStatus: null,
  auditStatus: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const activeQuickFilter = ref('all')

const summary = reactive({
  total: 0,
  published: 0,
  pending: 0,
  sold: 0,
  other: 0
})

const summaryCards = computed(() => [
  { key: 'all', label: '全部房源', value: summary.total, tip: '当前账号名下的全部房源' },
  { key: 'published', label: '在售房源', value: summary.published, tip: '已上架且审核通过的房源' },
  { key: 'pending', label: '待审核', value: summary.pending, tip: '等待管理员处理的房源' },
  { key: 'sold', label: '已成交', value: summary.sold, tip: '已经完成交易的房源' },
  {
    key: 'other',
    label: '其他状态',
    value: summary.other,
    tip: '审核中、已驳回、未发布或已下架的房源'
  }
])

const matchesOtherStatus = house => {
  const isPublished = house.houseStatus === 1 && house.auditStatus === 2
  const isPending = house.auditStatus === 0
  const isSold = house.houseStatus === 2
  return !isPublished && !isPending && !isSold
}

const buildQuery = () => ({
  agentId: userId,
  pageNum: pagination.pageNum,
  pageSize: pagination.pageSize,
  keyword: filters.keyword || undefined,
  houseStatus: filters.houseStatus ?? undefined,
  auditStatus: filters.auditStatus ?? undefined
})

const loadData = async () => {
  loading.value = true
  try {
    if (activeQuickFilter.value === 'other') {
      const totalRes = await request.get('/house/list', {
        params: {
          agentId: userId,
          pageNum: 1,
          pageSize: 1,
          keyword: filters.keyword || undefined
        }
      })

      const allRes = await request.get('/house/list', {
        params: {
          agentId: userId,
          pageNum: 1,
          pageSize: Math.max(totalRes.data?.total || 0, 1),
          keyword: filters.keyword || undefined
        }
      })

      const otherHouses = (allRes.data?.list || []).filter(matchesOtherStatus)
      const start = (pagination.pageNum - 1) * pagination.pageSize
      houseList.value = otherHouses.slice(start, start + pagination.pageSize)
      pagination.total = otherHouses.length
      return
    }

    const res = await request.get('/house/list', {
      params: buildQuery()
    })
    houseList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载房源失败:', error)
  } finally {
    loading.value = false
  }
}

const loadSummary = async () => {
  try {
    const [allRes, publishedRes, soldRes, pendingRes] = await Promise.all([
      request.get('/house/list', { params: { agentId: userId, pageNum: 1, pageSize: 1 } }),
      request.get('/house/list', {
        params: { agentId: userId, pageNum: 1, pageSize: 1, houseStatus: 1, auditStatus: 2 }
      }),
      request.get('/house/list', {
        params: { agentId: userId, pageNum: 1, pageSize: 1, houseStatus: 2 }
      }),
      request.get('/house/list', {
        params: { agentId: userId, pageNum: 1, pageSize: 1, auditStatus: 0 }
      })
    ])

    summary.total = allRes.data.total || 0
    summary.published = publishedRes.data.total || 0
    summary.sold = soldRes.data.total || 0
    summary.pending = pendingRes.data.total || 0
    summary.other = Math.max(summary.total - summary.published - summary.pending - summary.sold, 0)
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const applyQuickFilter = key => {
  activeQuickFilter.value = key
  filters.keyword = ''

  if (key === 'all') {
    filters.houseStatus = null
    filters.auditStatus = null
  } else if (key === 'published') {
    filters.houseStatus = 1
    filters.auditStatus = 2
  } else if (key === 'pending') {
    filters.houseStatus = null
    filters.auditStatus = 0
  } else if (key === 'sold') {
    filters.houseStatus = 2
    filters.auditStatus = null
  } else if (key === 'other') {
    filters.houseStatus = null
    filters.auditStatus = null
  }

  pagination.pageNum = 1
  loadData()
}

const handleSearch = () => {
  activeQuickFilter.value = 'custom'
  pagination.pageNum = 1
  loadData()
}

const resetFilters = () => {
  activeQuickFilter.value = 'all'
  filters.keyword = ''
  filters.houseStatus = null
  filters.auditStatus = null
  pagination.pageNum = 1
  loadData()
}

const handlePageSizeChange = () => {
  pagination.pageNum = 1
  loadData()
}

const handleCreate = () => {
  router.push('/agent-layout/house/editor')
}

const goToDetail = id => {
  router.push(`/agent-layout/house/detail/${id}`)
}

const handleEdit = house => {
  router.push(`/agent-layout/house/editor/${house.id}`)
}

const handleDelete = async house => {
  try {
    await ElMessageBox.confirm(`确认删除房源「${house.title}」吗？`, '删除房源', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/house/delete/${house.id}`)
    ElMessage.success('房源已删除')
    await Promise.all([loadData(), loadSummary()])
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除房源失败:', error)
    }
  }
}

const getHouseImage = images => {
  const imageList = formatImageUrls(images)
  return imageList[0] || '/default-house.jpg'
}

const handleImageError = event => {
  event.target.src = '/default-house.jpg'
}

const formatPrice = value => {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  return Number(value).toLocaleString()
}

const formatUnitPrice = house => {
  const unitPrice = Number(house.unitPrice || 0)
  if (unitPrice > 0) {
    return `${Math.round(unitPrice).toLocaleString()} 元/㎡`
  }

  const price = Number(house.price || 0)
  const area = Number(house.area || 0)
  if (!price || !area) {
    return '暂无单价'
  }

  return `${Math.round((price * 10000) / area).toLocaleString()} 元/㎡`
}

const formatAddress = house => [house.province, house.city, house.district, house.address].filter(Boolean).join(' ')

const getHouseStatusLabel = status =>
  ({
    0: '未发布',
    1: '已发布',
    2: '已成交',
    3: '已下架'
  }[status] || '未知状态')

const getHouseStatusType = status =>
  ({
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning'
  }[status] || 'info')

const getAuditStatusLabel = status =>
  ({
    0: '待审核',
    1: '审核中',
    2: '已通过',
    3: '已驳回'
  }[status] || '未知审核状态')

const getAuditStatusType = status =>
  ({
    0: 'warning',
    1: '',
    2: 'success',
    3: 'danger'
  }[status] || 'info')

onMounted(async () => {
  await Promise.all([loadData(), loadSummary()])
})
</script>

<style scoped>
.house-manage-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
  background: #f0f2f5;
  min-height: 100%;
}

/* 消除 page-shell 带来的双层视觉效果 */
.house-manage-page :deep(.page-shell-disabled),
.house-manage-page :deep(.page-shell-hero-disabled),
.house-manage-page :deep(.page-shell-hero--split-disabled) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
  border: none !important;
}

.manage-hero {
  margin-bottom: 24px;
  display: flex;
  gap: 20px;
  padding: 0;
  border-radius: 16px;
  align-items: stretch;
}

.manage-hero__content {
  padding: 38px 32px 32px;
}

.manage-hero .hero-text {
  max-width: 600px;
  margin-top: 8px;
  line-height: 1.6;
  color: #64748b;
}

.manage-hero__actions {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 108px 32px 24px 0;
  min-width: 180px;
  align-self: stretch;
}

.manage-hero.page-shell-hero--split .page-shell-hero__aside::before {
  display: none;
}

.manage-hero__button {
  min-width: 160px;
  height: 48px;
  font-size: 15px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #047857 0%, #059669 100%);
  box-shadow: 0 12px 26px rgba(5, 150, 105, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.manage-hero__button:hover,
.manage-hero__button:focus {
  background: linear-gradient(135deg, #065f46 0%, #047857 100%);
  box-shadow: 0 16px 30px rgba(5, 150, 105, 0.26);
  transform: translateY(-1px);
}

/* 统计卡片区域: 与Hero合并为一个整体 */
.manage-summary {
  margin-top: 0;
  margin-bottom: 18px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  padding: 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.summary-card {
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  text-align: left;
}

.summary-card:hover {
  background: #fff;
  border-color: var(--el-color-primary-light-7);
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.08);
}

.summary-card.active {
  background: #fff;
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.15);
}

.summary-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.summary-card strong {
  font-size: 24px;
  color: #0f172a;
  font-weight: 700;
}

.summary-card small {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.summary-card.is-disabled,
.summary-card:disabled {
  cursor: default;
  opacity: 1;
}

.summary-card.is-disabled:hover,
.summary-card:disabled:hover {
  background: #f8fafc;
  border-color: transparent;
  box-shadow: none;
  transform: none;
}

.main-content {
  display: grid;
  gap: 18px;
}

.filter-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-row {
  display: grid;
  grid-template-columns: minmax(240px, 1.4fr) repeat(2, minmax(170px, 0.8fr)) auto auto;
  gap: 14px;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.house-card,
.house-card-skeleton {
  overflow: hidden;
  border: 1px solid rgba(5, 150, 105, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 4px 12px rgba(6, 78, 59, 0.04);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.house-card:hover {
  transform: translateY(-4px);
  border-color: rgba(5, 150, 105, 0.2);
  box-shadow: 0 12px 24px rgba(6, 78, 59, 0.08);
}

.house-card {
  display: flex;
  flex-direction: column;
}

.house-image {
  position: relative;
  height: 210px;
  overflow: hidden;
  cursor: pointer;
  background: #e5e7eb;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.house-card:hover .house-image img {
  transform: scale(1.05);
}

.house-image::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 88px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0) 0%, rgba(15, 23, 42, 0.55) 100%);
  pointer-events: none;
}

.house-image-overlay,
.badge-row {
  position: absolute;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  z-index: 1;
}

.badge-row {
  top: 16px;
  gap: 8px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.house-image-overlay {
  bottom: 14px;
  justify-content: space-between;
  color: #fff;
  font-size: 12px;
}

.house-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 18px 20px;
}

.house-topline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  min-height: 56px;
}

.house-topline h3 {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.45;
  cursor: pointer;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.house-address {
  min-height: 48px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.65;
}

.house-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-content: flex-start;
  min-height: 64px;
}

.price-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  min-height: 56px;
}

.total-price {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.amount {
  color: #dc2626;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.unit {
  color: #94a3b8;
  font-size: 14px;
}

.unit-price,
.community {
  color: #64748b;
  font-size: 13px;
}

.reject-reason {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.6;
}

.action-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: auto;
}

.action-row .el-button {
  margin: 0;
}

.skeleton-body {
  display: grid;
  gap: 10px;
  padding: 18px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 8px;
  padding: 18px 20px;
  border: 1px solid rgba(5, 150, 105, 0.08);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.82);
}

:deep(.pagination-wrap .el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px 14px;
}

@media (max-width: 1600px) {
  .manage-summary {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .house-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .manage-summary {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .house-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .manage-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .house-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .manage-hero {
    text-align: left;
  }
  
  .manage-hero__actions {
    justify-content: center;
    padding: 0 20px 24px;
  }

  .manage-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-row,
  .house-grid,
  .action-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .pagination-wrap {
    padding: 14px 12px;
  }
}
</style>
