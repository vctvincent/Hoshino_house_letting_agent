<template>
  <div class="transaction-manage-page page-shell page-shell--has-summary">
    <section class="manage-hero page-shell-hero">
      <div class="manage-hero__content page-shell-hero__content">
        <p class="eyebrow">Deal Flow</p>
        <h1>{{ isAdmin ? '交易记录中心' : '我的交易管理' }}</h1>
        <p class="manage-hero__text hero-text">
          {{
            isAdmin
              ? '从全局视角查看交易进度、筛选记录并进入流程详情，管理员当前仅查看不干预。'
              : '基于已完成带看的客户和房源创建交易，并持续跟进谈判、签约和成交状态。'
          }}
        </p>
        <div v-if="isAdmin" class="manage-hero__readonly">当前为管理员只读模式</div>
      </div>
      <div v-if="!isAdmin" class="manage-hero__actions">
        <el-button
          type="primary"
          size="large"
          class="manage-hero__button"
          @click="openCreateDialog"
        >
          创建交易
        </el-button>
      </div>
    </section>

    <section class="summary-grid page-shell-summary page-shell-summary--6">
      <button
        v-for="item in summaryCards"
        :key="item.key"
        type="button"
        class="summary-card"
        :class="{ active: activeQuickFilter === item.key }"
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
          placeholder="搜索交易单号、房源标题、客户名或中介名"
          clearable
          class="filter-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.status" placeholder="交易状态" clearable class="filter-select">
          <el-option label="待确认" :value="0" />
          <el-option label="谈判中" :value="1" />
          <el-option label="已签约" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-skeleton :loading="loading" animated :count="6">
      <template #template>
        <div class="transaction-grid">
          <div v-for="n in 6" :key="n" class="transaction-card-skeleton">
            <el-skeleton-item variant="image" style="height: 200px" />
            <div class="skeleton-body">
              <el-skeleton-item variant="h3" style="width: 60%" />
              <el-skeleton-item variant="text" style="width: 90%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 56%; margin-top: 12px" />
            </div>
          </div>
        </div>
      </template>

      <template #default>
        <el-empty v-if="!displayedTransactions.length" description="当前筛选条件下暂无交易记录" />

        <div v-else class="transaction-grid">
          <article v-for="transaction in displayedTransactions" :key="transaction.id" class="transaction-card">
            <div class="transaction-image" @click="viewDetail(transaction)">
              <img :src="getHouseImage(transaction.houseId)" :alt="transaction.houseTitle" @error="handleImageError" />
              <div class="badge-row">
                <el-tag size="small" effect="dark" :type="getStatusType(transaction.status)">
                  {{ getStatusLabel(transaction.status) }}
                </el-tag>
              </div>
              <div class="image-overlay">
                <span>{{ transaction.transactionNo }}</span>
              </div>
            </div>

            <div class="transaction-body">
              <div class="title-row">
                <div>
                  <h3 @click="viewDetail(transaction)">{{ transaction.houseTitle || '未命名房源' }}</h3>
                  <span class="sub-line">{{ transaction.houseAddress || '未填写房源地址' }}</span>
                </div>
                <span class="customer-name">
                  {{
                    isAdmin
                      ? transaction.agentName || '未填写中介'
                      : transaction.customerName || transaction.customerPhone || '未填写客户'
                  }}
                </span>
              </div>

              <div class="meta-list">
                <div class="meta-item">
                  <span>成交价格</span>
                  <strong class="amount-text">{{ formatPrice(transaction.finalPrice) }} 万元</strong>
                </div>
                <div class="meta-item">
                  <span>支付方式</span>
                  <strong>{{ transaction.paymentMethod || '未填写' }}</strong>
                </div>
                <div class="meta-item">
                  <span>客户</span>
                  <strong>{{ transaction.customerName || transaction.customerPhone || '未填写' }}</strong>
                </div>
                <div class="meta-item">
                  <span>{{ isAdmin ? '中介' : '定金' }}</span>
                  <strong :class="{ 'amount-text': !isAdmin }">
                    {{
                      isAdmin
                        ? transaction.agentName || '未填写'
                        : `${formatPrice(transaction.deposit)} 万元`
                    }}
                  </strong>
                </div>
              </div>

              <p v-if="transaction.remark" class="remark-line">备注：{{ transaction.remark }}</p>

              <div class="action-row">
                <el-button plain @click="viewDetail(transaction)">查看流程</el-button>
                <el-button type="primary" plain @click="viewDetail(transaction)">交易详情</el-button>
              </div>
            </div>
          </article>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="displayedPaginationTotal"
            :page-sizes="[10, 15, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="handlePageSizeChange"
            @current-change="handleCurrentPageChange"
          />
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="showCreateDialog" title="创建交易" width="920px" destroy-on-close>
      <el-alert
        title="请选择与当前客户已完成带看的房源，系统会自动绑定对应的带看记录。"
        type="info"
        :closable="false"
        class="dialog-alert"
      />

      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="选择客户" prop="customerId">
          <el-select
            v-model="createForm.customerId"
            placeholder="请先选择客户"
            filterable
            style="width: 100%"
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in agentCustomers"
              :key="customer.id"
              :label="`${customer.nickname || customer.phone} - ${customer.phone}`"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择房源" prop="viewingId">
          <div v-if="!createForm.customerId" class="empty-tip">请先选择客户，再从已完成带看的房源中进行选择。</div>
          <el-skeleton v-else :loading="candidateLoading" animated>
            <template #default>
              <div v-if="!candidateViewings.length" class="empty-tip">
                当前客户和该中介之间还没有可用于创建交易的已完成带看房源。
              </div>
              <div v-else>
                <div v-if="selectedViewing" class="selected-viewing-tip">
                  <span>已选择房源，可在下方查看详情或重新选择。</span>
                  <el-button plain size="small" @click="clearSelectedViewing">重新选择</el-button>
                </div>
                <div v-else class="candidate-grid">
                  <button
                    v-for="item in candidateViewings"
                    :key="item.viewingId"
                    type="button"
                    class="candidate-card"
                    :class="{ active: Number(createForm.viewingId) === Number(item.viewingId) }"
                    @click="selectViewing(item)"
                  >
                    <img
                      :src="item.imageUrl"
                      :alt="item.houseTitle"
                      class="candidate-cover"
                      @error="handleImageError"
                    />
                    <div class="candidate-body">
                      <h4>{{ item.houseTitle }}</h4>
                      <p>{{ item.houseAddress || '未填写房源地址' }}</p>
                      <div class="candidate-meta">
                        <span class="amount-text">{{ formatPrice(item.price) }} 万元</span>
                        <span>{{ item.houseType || '待补充户型' }}</span>
                        <span>{{ item.area || '--' }} ㎡</span>
                      </div>
                      <small>完成带看：{{ formatTime(item.actualTime || item.appointTime) }}</small>
                    </div>
                    <div class="candidate-actions">
                      <el-button plain size="small" @click.stop="viewHouseDetail(item.houseId)">房源详情</el-button>
                    </div>
                  </button>
                </div>
              </div>
            </template>
          </el-skeleton>
        </el-form-item>

        <div class="create-grid">
          <el-form-item label="预计成交价（万元）" prop="finalPrice">
            <el-input-number
              v-model="createForm.finalPrice"
              :min="0.01"
              :precision="2"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="定金（万元）">
            <el-input-number
              v-model="createForm.deposit"
              :min="0"
              :precision="2"
              :step="0.5"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <div class="create-grid">
          <el-form-item label="支付方式">
            <el-radio-group v-model="createForm.paymentMethod">
              <el-radio value="全款">全款</el-radio>
              <el-radio value="分期付款">分期付款</el-radio>
              <el-radio value="银行贷款">银行贷款</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <div v-if="selectedViewing" class="selected-house-card">
          <img
            :src="selectedViewing.imageUrl"
            :alt="selectedViewing.houseTitle"
            class="selected-house-cover"
            @error="handleImageError"
          />
          <div class="selected-house-copy">
            <strong>{{ selectedViewing.houseTitle }}</strong>
            <p>{{ selectedViewing.houseAddress || '未填写房源地址' }}</p>
            <div class="selected-house-tags">
              <span class="amount-text">{{ formatPrice(selectedViewing.price) }} 万元</span>
              <span>{{ selectedViewing.houseType || '待补充户型' }}</span>
              <span>{{ selectedViewing.area || '--' }} ㎡</span>
            </div>
          </div>
          <div class="selected-house-actions">
            <el-button plain size="small" @click="viewHouseDetail(selectedViewing.houseId)">房源详情</el-button>
            <el-button plain size="small" type="danger" @click="clearSelectedViewing">移除当前房源</el-button>
          </div>
        </div>

        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可以填写谈判背景、付款节奏或其他说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api'
import { formatImageUrls } from '@/utils/imageUtils'

const store = useStore()
const router = useRouter()
const route = useRoute()

const userRole = computed(() => store.getters.role || '')
const currentUserId = computed(() => store.getters.userInfo?.userId || store.getters.userInfo?.id)
const isAdmin = computed(() => userRole.value === 'ROLE_ADMIN')

const loading = ref(false)
const creating = ref(false)
const showCreateDialog = ref(false)
const candidateLoading = ref(false)
const activeQuickFilter = ref('all')

const rawTransactions = ref([])
const allTransactions = ref([])
const houseImageMap = ref({})
const agentCustomers = ref([])
const candidateViewings = ref([])
const selectedViewing = ref(null)
const createFormRef = ref()

const flowStages = [
  { key: 'pending', label: '待确认' },
  { key: 'negotiating', label: '谈判中' },
  { key: 'signed', label: '已签约' },
  { key: 'completed', label: '已完成' }
]

const filters = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const createForm = reactive({
  houseId: null,
  customerId: null,
  viewingId: null,
  finalPrice: null,
  deposit: 0,
  paymentMethod: '全款',
  remark: ''
})

const createRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  viewingId: [{ required: true, message: '请选择已完成带看的房源', trigger: 'change' }],
  finalPrice: [{ required: true, message: '请输入预计成交价', trigger: 'change' }]
}

const filteredTransactions = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return rawTransactions.value.filter(item => {
    const matchStatus = filters.status === null || item.status === filters.status
    const matchKeyword =
      !keyword ||
      [item.transactionNo, item.houseTitle, item.customerName, item.customerPhone, item.agentName]
        .filter(Boolean)
        .some(value => String(value).toLowerCase().includes(keyword))

    return matchStatus && matchKeyword
  })
})

const displayedTransactions = computed(() => {
  if (isAdmin.value) {
    return filteredTransactions.value
  }

  const start = (pagination.pageNum - 1) * pagination.pageSize
  return filteredTransactions.value.slice(start, start + pagination.pageSize)
})

const displayedPaginationTotal = computed(() => {
  return isAdmin.value ? pagination.total : filteredTransactions.value.length
})

const summaryCards = computed(() => {
  const list = allTransactions.value
  const countByStatus = status => list.filter(item => item.status === status).length

  return [
    { key: 'all', label: '全部交易', value: list.length, tip: '系统中的全部交易' },
    { key: 'pending', label: '待确认', value: countByStatus(0), tip: '等待客户确认的交易' },
    { key: 'negotiating', label: '谈判中', value: countByStatus(1), tip: '正在协商中的交易' },
    { key: 'signed', label: '已签约', value: countByStatus(2), tip: '已经签约待完成的交易' },
    { key: 'completed', label: '已完成', value: countByStatus(3), tip: '已经成交完成的交易' },
    { key: 'cancelled', label: '已取消', value: countByStatus(4), tip: '已取消或已关闭的交易' }
  ]
})

const loadHouseImages = async houseIds => {
  const missingIds = houseIds.filter(id => id && !houseImageMap.value[id])
  if (!missingIds.length) return

  const responses = await Promise.all(
    missingIds.map(async id => {
      try {
        const res = await request.get(`/house/detail/${id}`)
        return [id, formatImageUrls(res.data?.images || [])[0] || '/default-house.jpg']
      } catch {
        return [id, '/default-house.jpg']
      }
    })
  )

  houseImageMap.value = {
    ...houseImageMap.value,
    ...Object.fromEntries(responses)
  }
}

const loadTransactions = async () => {
  loading.value = true
  try {
    if (isAdmin.value) {
      const [res, allRes] = await Promise.all([
        request.get('/transaction/list', {
          params: {
            status: filters.status ?? undefined,
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize
          }
        }),
        request.get('/transaction/list', {
          params: {
            pageNum: 1,
            pageSize: 1000
          }
        })
      ])

      rawTransactions.value = res.data?.list || []
      pagination.total = res.data?.total || 0
      allTransactions.value = allRes.data?.list || []
    } else {
      const [res, allRes] = await Promise.all([
        request.get('/transaction/my/agent', {
          params: {
            status: filters.status ?? undefined
          }
        }),
        request.get('/transaction/my/agent')
      ])

      rawTransactions.value = Array.isArray(res.data) ? res.data : []
      pagination.total = rawTransactions.value.length
      allTransactions.value = Array.isArray(allRes.data) ? allRes.data : []
    }

    await loadHouseImages(rawTransactions.value.map(item => item.houseId))
  } catch (error) {
    console.error('加载交易列表失败:', error)
    ElMessage.error('加载交易列表失败')
  } finally {
    loading.value = false
  }
}

const loadAgentCustomers = async () => {
  if (isAdmin.value) return

  try {
    const res = await request.get('/viewing/my/customers')
    agentCustomers.value = res.data || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const handleCustomerChange = async customerId => {
  createForm.viewingId = null
  createForm.houseId = null
  createForm.finalPrice = null
  selectedViewing.value = null
  candidateViewings.value = []

  if (!customerId) return

  candidateLoading.value = true
  try {
    const res = await request.get('/viewing/list', {
      params: {
        agentId: currentUserId.value,
        customerId,
        status: 2,
        pageNum: 1,
        pageSize: 100
      }
    })

    const list = res.data?.list || []
    const latestByHouse = new Map()
    list.forEach(item => {
      const currentTime = new Date(item.actualTime || item.appointTime || 0).getTime()
      const existing = latestByHouse.get(item.houseId)
      const existingTime = new Date(existing?.actualTime || existing?.appointTime || 0).getTime()
      if (!existing || currentTime > existingTime) {
        latestByHouse.set(item.houseId, item)
      }
    })

    const candidates = Array.from(latestByHouse.values())
    await loadHouseImages(candidates.map(item => item.houseId))

    const houseDetails = await Promise.all(
      candidates.map(async item => {
        try {
          const res = await request.get(`/house/detail/${item.houseId}`)
          return [item.houseId, res.data]
        } catch {
          return [item.houseId, null]
        }
      })
    )
    const detailMap = Object.fromEntries(houseDetails)

    const availableCandidates = candidates.filter(item => {
      const house = detailMap[item.houseId]
      return house && house.houseStatus === 1 && house.auditStatus === 2
    })

    candidateViewings.value = availableCandidates.map(item => {
      const house = detailMap[item.houseId] || {}
      return {
        viewingId: item.id,
        houseId: item.houseId,
        houseTitle: item.houseTitle,
        houseAddress: item.houseAddress,
        houseType: house.houseType,
        actualTime: item.actualTime,
        appointTime: item.appointTime,
        price: house.price,
        area: house.area,
        imageUrl: houseImageMap.value[item.houseId] || '/default-house.jpg'
      }
    })

    if (!candidateViewings.value.length) {
      ElMessage.warning('该客户暂无可创建交易的已完成带看房源')
    }
  } catch (error) {
    console.error('加载候选房源失败:', error)
    ElMessage.error('加载候选房源失败')
  } finally {
    candidateLoading.value = false
  }
}

const selectViewing = item => {
  selectedViewing.value = item
  createForm.viewingId = item.viewingId
  createForm.houseId = item.houseId
  createForm.finalPrice = item.price || createForm.finalPrice
  if (!createForm.deposit && createForm.finalPrice) {
    createForm.deposit = Number((createForm.finalPrice * 0.1).toFixed(2))
  }
}

const clearSelectedViewing = () => {
  selectedViewing.value = null
  createForm.viewingId = null
  createForm.houseId = null
}

const openCreateDialog = () => {
  resetCreateForm()
  showCreateDialog.value = true
}

const resetCreateForm = () => {
  createForm.houseId = null
  createForm.customerId = null
  createForm.viewingId = null
  createForm.finalPrice = null
  createForm.deposit = 0
  createForm.paymentMethod = '全款'
  createForm.remark = ''
  candidateViewings.value = []
  selectedViewing.value = null
  createFormRef.value?.clearValidate?.()
}

const handleCreate = async () => {
  const valid = await createFormRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  try {
    creating.value = true
    await request.post('/transaction/create', {
      houseId: Number(createForm.houseId),
      customerId: Number(createForm.customerId),
      viewingId: Number(createForm.viewingId),
      finalPrice: createForm.finalPrice,
      deposit: createForm.deposit,
      paymentMethod: createForm.paymentMethod,
      remark: createForm.remark
    })

    ElMessage.success('交易创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadTransactions()
  } catch (error) {
    console.error('创建交易失败:', error)
    ElMessage.error(error.message || '创建交易失败')
  } finally {
    creating.value = false
  }
}

const applyQuickFilter = key => {
  activeQuickFilter.value = key
  if (key === 'all') {
    filters.status = null
  } else if (key === 'pending') {
    filters.status = 0
  } else if (key === 'negotiating') {
    filters.status = 1
  } else if (key === 'signed') {
    filters.status = 2
  } else if (key === 'completed') {
    filters.status = 3
  } else if (key === 'cancelled') {
    filters.status = 4
  }

  pagination.pageNum = 1

  if (isAdmin.value) {
    loadTransactions()
  }
}

const handleSearch = async () => {
  activeQuickFilter.value = 'custom'
  pagination.pageNum = 1

  if (isAdmin.value) {
    await loadTransactions()
  }
}

const resetFilters = async () => {
  activeQuickFilter.value = 'all'
  filters.keyword = ''
  filters.status = null
  pagination.pageNum = 1

  if (isAdmin.value) {
    await loadTransactions()
  }
}

const handlePageSizeChange = () => {
  pagination.pageNum = 1
  if (isAdmin.value) {
    loadTransactions()
  }
}

const handleCurrentPageChange = () => {
  if (isAdmin.value) {
    loadTransactions()
  }
}

const viewDetail = row => {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    router.push(`/agent-layout/transaction/detail/${row.id}`)
    return
  }
  if (currentPath.startsWith('/admin-layout/')) {
    router.push(`/admin-layout/transaction/detail/${row.id}`)
    return
  }
  router.push(`/layout/transaction/detail/${row.id}`)
}

const viewHouseDetail = houseId => {
  if (!houseId) return
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    router.push(`/agent-layout/house/detail/${houseId}`)
    return
  }
  if (currentPath.startsWith('/admin-layout/')) {
    router.push(`/admin-layout/house/detail/${houseId}`)
    return
  }
  router.push(`/layout/house/detail/${houseId}`)
}

const getStatusLabel = status =>
  ({
    0: '待确认',
    1: '谈判中',
    2: '已签约',
    3: '已完成',
    4: '已取消'
  }[status] || '未知状态')

const getStatusType = status =>
  ({
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'success',
    4: 'info'
  }[status] || 'info')

const getCardStepState = (status, index) => {
  if (status >= 3) return 'done'
  if (status === 2) return index <= 2 ? 'done' : 'pending'
  if (status === 1) return index === 0 ? 'done' : index === 1 ? 'active' : 'pending'
  return index === 0 ? 'active' : 'pending'
}

const formatPrice = value => {
  if (value === null || value === undefined || value === '') return '--'
  const num = Number(value)
  if (Number.isNaN(num)) return '--'
  return num.toLocaleString()
}

const formatTime = time => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getHouseImage = houseId => houseImageMap.value[houseId] || '/default-house.jpg'

const handleImageError = event => {
  event.target.src = '/default-house.jpg'
}

const applyRouteFilters = () => {
  const routeStatus = route.query.status
  if (routeStatus !== undefined && routeStatus !== '') {
    filters.status = Number(route.query.status)
    activeQuickFilter.value =
      Number(route.query.status) === 0 ? 'pending'
        : Number(route.query.status) === 1 ? 'negotiating'
          : Number(route.query.status) === 2 ? 'signed'
            : Number(route.query.status) === 3 ? 'completed'
              : Number(route.query.status) === 4 ? 'cancelled'
                : 'custom'
  } else {
    filters.status = null
    activeQuickFilter.value = 'all'
  }
}

watch(
  () => createForm.finalPrice,
  newValue => {
    if (newValue && createForm.deposit === 0) {
      createForm.deposit = Number((newValue * 0.1).toFixed(2))
    }
  }
)

watch(
  [() => filteredTransactions.value.length, () => pagination.pageSize],
  () => {
    if (isAdmin.value) {
      return
    }

    const maxPage = Math.max(Math.ceil(filteredTransactions.value.length / pagination.pageSize), 1)
    if (pagination.pageNum > maxPage) {
      pagination.pageNum = maxPage
    }
  }
)

onMounted(async () => {
  applyRouteFilters()
  await Promise.all([loadTransactions(), loadAgentCustomers()])
})
</script>
<style scoped>
.transaction-manage-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
}

.manage-hero {
  margin-bottom: 24px;
  display: flex;
  gap: 20px;
  padding: 0;
  border-radius: 16px;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.96);
}

.manage-hero__content {
  flex: 1;
  padding: 38px 32px 32px;
  min-width: 0;
}

.manage-hero__text {
  max-width: 600px;
  margin-top: 8px;
  line-height: 1.6;
}

.manage-hero__readonly {
  display: inline-flex;
  margin-top: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(5, 150, 105, 0.08);
  color: #059669;
  font-size: 13px;
  font-weight: 500;
}

.manage-hero__actions {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 108px 32px 24px 0;
  min-width: 180px;
  align-self: stretch;
}

.manage-hero__button {
  min-width: 160px;
  height: 48px;
  margin-top: 0;
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

.filter-card {
  margin-bottom: 18px;
}

.filter-row {
  display: grid;
  grid-template-columns: minmax(260px, 1.6fr) 200px auto auto;
  gap: 14px;
}

.transaction-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.transaction-card,
.transaction-card-skeleton {
  overflow: hidden;
  border: 1px solid rgba(5, 150, 105, 0.08);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 16px 40px rgba(6, 78, 59, 0.1);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.transaction-card:hover {
  transform: translateY(-4px);
  border-color: rgba(5, 150, 105, 0.2);
  box-shadow: 0 20px 50px rgba(6, 78, 59, 0.16);
}

.transaction-image {
  position: relative;
  height: 172px;
  overflow: hidden;
  cursor: pointer;
  background: #e5e7eb;
}

.transaction-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.transaction-card:hover .transaction-image img {
  transform: scale(1.05);
}

.transaction-image::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 88px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0) 0%, rgba(15, 23, 42, 0.55) 100%);
  pointer-events: none;
}

.badge-row,
.image-overlay {
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

.image-overlay {
  bottom: 14px;
  justify-content: flex-start;
  color: #fff;
  font-size: 12px;
}

.transaction-body {
  display: grid;
  gap: 14px;
  padding: 18px 18px 20px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title-row h3 {
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

.sub-line {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.customer-name {
  flex-shrink: 0;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 0.92);
  color: #065f46;
  font-size: 12px;
  font-weight: 600;
}

.flow-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.flow-node {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 10px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: #f8fafc;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.flow-node.done {
  border-color: rgba(5, 150, 105, 0.24);
  background: rgba(16, 185, 129, 0.14);
  color: #047857;
}

.flow-node.active {
  border-color: rgba(5, 150, 105, 0.3);
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.92) 0%, rgba(209, 250, 229, 0.76) 100%);
  color: #065f46;
}

.flow-node.cancelled {
  grid-column: 1 / -1;
  border-color: rgba(239, 68, 68, 0.18);
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
}

.meta-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid rgba(5, 150, 105, 0.08);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.96) 0%, rgba(241, 245, 249, 0.92) 100%);
}

.meta-item span {
  color: #64748b;
  font-size: 13px;
}

.meta-item strong {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.amount-text {
  color: #dc2626;
}

.remark-line {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.action-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-row .el-button {
  margin: 0;
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

.dialog-alert {
  margin-bottom: 18px;
}

.empty-tip {
  padding: 24px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.96);
  color: #64748b;
  line-height: 1.7;
  text-align: center;
}

.candidate-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.selected-viewing-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(236, 253, 245, 0.92);
  color: #065f46;
}

.selected-house-card {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
  padding: 16px;
  border: 1px solid rgba(5, 150, 105, 0.12);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.98);
}

.selected-house-cover {
  width: 100%;
  height: 148px;
  border-radius: 18px;
  object-fit: cover;
  background: #e5e7eb;
}

.selected-house-copy {
  display: grid;
  align-content: start;
  gap: 12px;
}

.selected-house-copy strong,
.selected-house-copy p {
  margin: 0;
}

.selected-house-copy p {
  color: #64748b;
  line-height: 1.6;
}

.selected-house-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-house-tags span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 0.9);
  color: #047857;
  font-size: 12px;
  font-weight: 600;
}

.selected-house-actions,
.candidate-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.candidate-card {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 16px;
  padding: 14px;
  border: 1px solid rgba(5, 150, 105, 0.1);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 12px 30px rgba(6, 78, 59, 0.08);
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.candidate-card:hover,
.candidate-card.active {
  transform: translateY(-2px);
  border-color: rgba(5, 150, 105, 0.24);
  box-shadow: 0 18px 36px rgba(6, 78, 59, 0.12);
}

.candidate-cover {
  width: 100%;
  height: 132px;
  border-radius: 16px;
  object-fit: cover;
  background: #e5e7eb;
}

.candidate-body {
  display: grid;
  align-content: start;
  gap: 10px;
}

.candidate-body h4 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.45;
}

.candidate-body p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.candidate-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.candidate-body small {
  color: #8b99ab;
}

.create-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.skeleton-body {
  display: grid;
  gap: 10px;
  padding: 18px;
}

@media (max-width: 1600px) {
  .transaction-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .transaction-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .candidate-grid,
  .create-grid,
  .selected-house-card {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 992px) {
  .transaction-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .filter-row,
  .transaction-grid,
  .meta-list,
  .action-row,
  .flow-strip,
  .candidate-grid,
  .create-grid,
  .selected-house-card {
    grid-template-columns: minmax(0, 1fr);
  }

  .title-row,
  .selected-viewing-tip,
  .selected-house-actions,
  .candidate-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .candidate-card {
    grid-template-columns: minmax(0, 1fr);
  }

  .pagination-wrap {
    padding: 14px 12px;
  }
}
</style>
