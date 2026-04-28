<template>
  <div class="admin-page">
    <!-- 顶部 Hero 区域 -->
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">Transaction Flow Detail</p>
        <h1>交易流程详情</h1>
        <p class="hero-text">
          {{ detail.transactionNo || '当前交易' }} · {{ detail.houseTitle || '未关联房源' }}
        </p>
        <div class="hero-meta-inline">
          <span class="mini-tag ok">客户：{{ detail.customerName || detail.customerPhone || '未填写' }}</span>
          <span class="mini-tag muted">中介：{{ detail.agentName || '未填写' }}</span>
          <span class="mini-tag warn">总价：{{ formatPrice(detail.finalPrice) }}万元</span>
        </div>
      </div>
      <div class="quick-actions">
        <button type="button" class="quick-card" @click="goBack">
          <span class="quick-mark">←</span>
          <div>
            <strong>返回列表</strong>
            <p>返回交易列表页面</p>
          </div>
        </button>
        <button type="button" class="quick-card" @click="loadDetail">
          <span class="quick-mark">↻</span>
          <div>
            <strong>刷新数据</strong>
            <p>重新加载交易信息</p>
          </div>
        </button>
      </div>
    </section>

    <!-- 操作条 -->
    <div v-if="showActionBar" class="action-bar">
      <div class="action-bar-inner">
        <span class="action-label">快捷操作</span>
        <div class="action-buttons-compact">
          <el-button
            v-if="detail.status === 0 && userRole === 'ROLE_CUSTOMER'"
            type="success"
            size="small"
            @click="confirmTransaction"
          >
            确认交易
          </el-button>

          <el-button
            v-if="detail.status === 0 && userRole === 'ROLE_AGENT'"
            type="warning"
            size="small"
            @click="startNegotiation"
          >
            开始谈判
          </el-button>

          <el-button
            v-if="detail.status === 1 && !isAdmin"
            type="primary"
            size="small"
            @click="openNegotiateDialog"
          >
            {{ userRole === 'ROLE_CUSTOMER' ? '提出还价' : '协商价格' }}
          </el-button>

          <el-button
            v-if="detail.status === 1 && userRole === 'ROLE_CUSTOMER'"
            type="success"
            size="small"
            @click="acceptPrice"
          >
            同意当前价格
          </el-button>

          <el-button
            v-if="detail.status === 1 && userRole === 'ROLE_AGENT'"
            type="success"
            size="small"
            @click="acceptCustomerOffer"
          >
            接受客户报价
          </el-button>

          <el-button
            v-if="detail.status === 2 && userRole === 'ROLE_AGENT' && !detail.contractUrl"
            type="primary"
            size="small"
            @click="openSignDialog"
          >
            上传合同
          </el-button>

          <el-button
            v-if="detail.status === 2 && userRole === 'ROLE_AGENT'"
            type="success"
            size="small"
            @click="completeTransaction"
          >
            完成交易
          </el-button>

          <el-button
            v-if="detail.status !== null && detail.status < 3 && !isAdmin"
            type="danger"
            size="small"
            plain
            @click="cancelTransaction"
          >
            取消交易
          </el-button>
        </div>
      </div>
    </div>

    <section class="flow-panel">
      <div class="panel-head">
        <div>
          <p class="panel-kicker">Current Stage</p>
          <h2>交易当前步骤</h2>
        </div>
        <div class="stage-pill">
          当前阶段：<strong>{{ currentStageLabel }}</strong>
        </div>
      </div>

      <div v-if="detail.status === 4" class="cancel-banner">
        这笔交易已取消，时间线中仍保留完整的处理记录，方便后续回看。
      </div>

      <!-- 流程轨道 -->
      <div ref="flowTrackViewportRef" class="flow-track-viewport">
        <div class="flow-track">
          <div
            v-for="(step, index) in flowSteps"
            :key="step.key"
            :ref="element => setFlowNodeRef(element, index)"
            class="flow-node-wrapper"
            :class="getFlowWrapperState(index)"
          >
          <div v-if="index > 0" class="flow-connector" :class="getConnectorState(index)"></div>

          <div class="flow-node" :class="getFlowNodeState(index)">
            <div class="node-badge">{{ index + 1 }}</div>
            <div class="node-content">
              <strong>{{ step.label }}</strong>
              <span>{{ step.note }}</span>
            </div>
          </div>
        </div>
        </div>
      </div>

      <p class="flow-tip">{{ currentStageDescription }}</p>
    </section>

    <div class="content-grid" v-loading="loading">
      <div class="main-column">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>基础信息</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="交易单号">{{ detail.transactionNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易状态">{{ getStatusLabel(detail.status) }}</el-descriptions-item>
            <el-descriptions-item label="房源标题">{{ detail.houseTitle || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房源地址">{{ detail.houseAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ detail.customerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客户电话">{{ detail.customerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="中介">{{ detail.agentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>交易信息</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="成交价格">{{ formatPrice(detail.finalPrice) }} 万元</el-descriptions-item>
            <el-descriptions-item label="定金">{{ formatPrice(detail.deposit) }} 万元</el-descriptions-item>
            <el-descriptions-item label="首付">{{ formatPrice(detail.downPayment) }} 万元</el-descriptions-item>
            <el-descriptions-item label="贷款金额">{{ formatPrice(detail.loanAmount) }} 万元</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ detail.paymentMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成交日期">{{ formatDate(detail.dealDate) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>合同与附件</span>
            </div>
          </template>

          <div class="contract-panel">
            <div v-if="detail.contractUrl" class="contract-ok">
              <strong>已上传合同</strong>
              <p>可以在线查看或下载当前交易合同。</p>
              <el-link type="primary" :href="detail.contractUrl" target="_blank">查看合同</el-link>
            </div>
            <div v-else class="contract-empty">
              <strong>暂未上传合同</strong>
              <p>交易还未进入上传合同阶段，或中介尚未完成合同上传。</p>
            </div>
          </div>
        </el-card>
      </div>

      <div class="side-column">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>流程时间线</span>
            </div>
          </template>

          <div v-if="timelineEvents.length" class="timeline-list is-scrollable">
            <article v-for="(item, index) in timelineEvents" :key="index" class="timeline-item">
              <span class="timeline-dot" :class="item.tone"></span>
              <div class="timeline-content">
                <strong>{{ item.title }}</strong>
                <small>{{ item.time }}</small>
                <p>{{ item.description }}</p>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无流程记录" />
        </el-card>

        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>价格协商记录</span>
            </div>
          </template>

          <div v-if="negotiationHistory.length" class="negotiation-list is-scrollable">
            <article v-for="(item, index) in negotiationHistory" :key="index" class="negotiation-item">
              <div class="negotiation-top">
                <strong>{{ item.label }}</strong>
                <small>{{ item.time }}</small>
              </div>
              <p class="price-change amount-text">{{ item.oldPrice }} 万元 -> {{ item.newPrice }} 万元</p>
              <p v-if="item.remark" class="negotiation-remark">{{ item.remark }}</p>
            </article>
          </div>
          <el-empty v-else description="暂无协商记录" />
        </el-card>
      </div>
    </div>

    <el-dialog v-model="showNegotiateDialog" title="价格协商" width="640px">
      <el-alert
        title="双方可以多次协商报价，系统会自动记录每次价格变动。"
        type="info"
        :closable="false"
        class="dialog-alert"
      />

      <el-form :model="negotiateForm" label-width="110px">
        <el-form-item label="当前价格">
          <el-input :model-value="formatPrice(detail.finalPrice)" readonly>
            <template #append>万元</template>
          </el-input>
        </el-form-item>

        <el-form-item :label="userRole === 'ROLE_CUSTOMER' ? '我的报价' : '新报价'" required>
          <el-input-number
            v-model="negotiateForm.newPrice"
            :min="0.01"
            :precision="2"
            :step="0.5"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="negotiateForm.remark"
            type="textarea"
            :rows="3"
            :placeholder="
              userRole === 'ROLE_CUSTOMER'
                ? '可以说明预算、付款节奏或还价原因'
                : '可以说明调价原因、市场变化或沟通结果'
            "
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showNegotiateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleNegotiate">
          {{ userRole === 'ROLE_CUSTOMER' ? '提交还价' : '更新报价' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showSignDialog" title="上传合同" width="620px">
      <el-alert
        title="支持 PDF、JPG、PNG，文件大小不超过 10MB。"
        type="info"
        :closable="false"
        class="dialog-alert"
      />

      <el-form :model="signForm" label-width="110px">
        <el-form-item label="合同文件" required>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            accept=".pdf,.jpg,.jpeg,.png"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到这里，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">PDF / JPG / PNG，单个文件不超过 10MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="合同备注">
          <el-input
            v-model="signForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可以补充合同编号、签署时间或其他说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showSignDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!signForm.contractUrl" @click="handleSignContract">
          确认上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/api'
import store from '@/store'
import { getActiveFlowIndex, getConnectorStateByStatus, getFlowNodeStateByStatus } from '../../utils/flowState'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const showSignDialog = ref(false)
const showNegotiateDialog = ref(false)
const detail = ref({})
const flowTrackViewportRef = ref(null)
const flowNodeRefs = ref([])

const signForm = ref({
  contractUrl: '',
  remark: ''
})

const negotiateForm = ref({
  newPrice: 0,
  remark: ''
})

const userRole = computed(() => store.getters.role || '')
const isAdmin = computed(() => userRole.value === 'ROLE_ADMIN')
const showActionBar = computed(() => !isAdmin.value && !!detail.value?.id)

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/api/file/upload/file`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${store.getters.token}`
}))

const flowSteps = [
  { key: 'pending', label: '待确认', note: '等待客户确认交易' },
  { key: 'negotiating', label: '谈判中', note: '双方沟通价格与条件' },
  { key: 'signed', label: '已签约', note: '合同上传并锁定价格' },
  { key: 'completed', label: '已完成', note: '交流程全部结束' }
]

const currentStageLabel = computed(() => getStatusLabel(detail.value?.status))
const currentFlowIndex = computed(() => getActiveFlowIndex(detail.value?.status))

const currentStageDescription = computed(() => {
  const status = Number(detail.value?.status)
  if (status === 0) return '当前还在确认阶段，客户确认后会进入谈判流程。'
  if (status === 1) return '当前处于谈判中，可以继续协商报价或推进签约。'
  if (status === 2) return '当前已签约，等待上传合同或办理后续成交手续。'
  if (status === 3) return '当前交易已经完成，可回看完整处理记录。'
  if (status === 4) return '当前交易已取消，流程已经终止。'
  return '当前交易状态未知，请刷新后重试。'
})

const timelineEvents = computed(() => {
  const events = []
  if (detail.value?.createTime) {
    events.push({
      title: '创建交易',
      time: formatDateTime(detail.value.createTime),
      rawTime: detail.value.createTime,
      description: `已创建交易单 ${detail.value.transactionNo || ''}`.trim(),
      tone: 'success'
    })
  }

  parseHistory(detail.value?.statusHistory).forEach(item => {
    events.push(item)
  })

  if (detail.value?.status === 3 && detail.value?.dealDate) {
    events.push({
      title: '完成交易',
      time: formatDateTime(detail.value.dealDate),
      rawTime: detail.value.dealDate,
      description: '系统记录成交日期，交易进入完成状态。',
      tone: 'success'
    })
  }

  return events.sort((a, b) => new Date(a.rawTime || a.time) - new Date(b.rawTime || b.time))
})

const negotiationHistory = computed(() =>
  parseHistory(detail.value?.statusHistory)
    .filter(item => item.kind === 'negotiation')
    .map(item => ({
      label: item.title,
      time: item.time,
      oldPrice: item.oldPrice,
      newPrice: item.newPrice,
      remark: item.description
    }))
    .reverse()
)

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/transaction/detail/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data || {}
      await nextTick()
      centerCurrentFlowStep('auto')
    } else {
      ElMessage.error(res.message || '加载交易详情失败')
    }
  } catch (error) {
    console.error('加载交易详情失败:', error)
    ElMessage.error('加载交易详情失败')
  } finally {
    loading.value = false
  }
}

const openNegotiateDialog = async () => {
  await loadDetail()
  negotiateForm.value = {
    newPrice: Number(detail.value?.finalPrice || 0),
    remark: ''
  }
  showNegotiateDialog.value = true
}

const startNegotiation = async () => {
  try {
    await ElMessageBox.confirm('确认开始谈判吗？', '开始谈判', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: { status: 1 }
    })

    if (res.code === 200) {
      ElMessage.success('已进入谈判阶段')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始谈判失败:', error)
      ElMessage.error('开始谈判失败')
    }
  }
}

const confirmTransaction = async () => {
  try {
    await ElMessageBox.confirm('确认接受这笔交易并进入谈判吗？', '确认交易', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: { status: 1, remark: '客户确认交易' }
    })

    if (res.code === 200) {
      ElMessage.success('交易已确认，已进入谈判阶段')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认交易失败:', error)
      ElMessage.error('确认交易失败')
    }
  }
}

const handleNegotiate = async () => {
  if (!negotiateForm.value.newPrice || negotiateForm.value.newPrice <= 0) {
    ElMessage.warning('请输入有效的报价')
    return
  }

  try {
    const res = await request.post(`/transaction/negotiate/${detail.value.id}`, null, {
      params: {
        newPrice: negotiateForm.value.newPrice,
        remark: negotiateForm.value.remark
      }
    })

    if (res.code === 200) {
      ElMessage.success(userRole.value === 'ROLE_CUSTOMER' ? '还价已提交' : '报价已更新')
      showNegotiateDialog.value = false
      loadDetail()
    } else {
      ElMessage.error(res.message || '协商失败')
    }
  } catch (error) {
    console.error('协商价格失败:', error)
    ElMessage.error(error.response?.data?.message || '协商价格失败')
  }
}

const acceptPrice = async () => {
  try {
    await ElMessageBox.confirm(
      `确认同意当前价格 ${formatPrice(detail.value.finalPrice)} 万元，并进入签约阶段吗？`,
      '同意价格',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: {
        status: 2,
        remark: '客户同意当前价格'
      }
    })

    if (res.code === 200) {
      ElMessage.success('已进入签约阶段')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('同意价格失败:', error)
      ElMessage.error('同意价格失败')
    }
  }
}

const acceptCustomerOffer = async () => {
  try {
    await ElMessageBox.confirm(
      `确认接受客户报价 ${formatPrice(detail.value.finalPrice)} 万元，并进入签约阶段吗？`,
      '接受客户报价',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: {
        status: 2,
        remark: '中介同意客户报价'
      }
    })

    if (res.code === 200) {
      ElMessage.success('已进入签约阶段')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接受客户报价失败:', error)
      ElMessage.error('接受客户报价失败')
    }
  }
}

const openSignDialog = () => {
  signForm.value = {
    contractUrl: '',
    remark: ''
  }
  showSignDialog.value = true
}

const beforeUpload = file => {
  const isValidType = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('仅支持 PDF、JPG、PNG 格式')
    return false
  }

  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }

  return true
}

const handleUploadSuccess = response => {
  if (response.code === 200) {
    signForm.value.contractUrl = response.data.url
    ElMessage.success('合同上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = error => {
  console.error('合同上传失败:', error)
  ElMessage.error('合同上传失败，请重试')
}

const handleSignContract = async () => {
  if (!signForm.value.contractUrl) {
    ElMessage.warning('请先上传合同文件')
    return
  }

  try {
    const res = await request.put('/transaction/update', {
      id: detail.value.id,
      contractUrl: signForm.value.contractUrl,
      status: 2
    })

    if (res.code === 200) {
      ElMessage.success('合同已保存')
      showSignDialog.value = false
      loadDetail()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存合同失败:', error)
    ElMessage.error('保存合同失败')
  }
}

const completeTransaction = async () => {
  try {
    await ElMessageBox.confirm('确认将这笔交易标记为已完成吗？', '完成交易', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: { status: 3, remark: '中介完成交易' }
    })

    if (res.code === 200) {
      ElMessage.success('交易已完成')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成交易失败:', error)
      ElMessage.error('完成交易失败')
    }
  }
}

const cancelTransaction = async () => {
  try {
    await ElMessageBox.confirm('确认取消这笔交易吗？', '取消交易', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post(`/transaction/status/${detail.value.id}`, null, {
      params: { status: 4, remark: '手动取消交易' }
    })

    if (res.code === 200) {
      ElMessage.success('交易已取消')
      loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消交易失败:', error)
      ElMessage.error('取消交易失败')
    }
  }
}

const goBack = () => {
  if (userRole.value === 'ROLE_CUSTOMER') {
    router.push('/layout/transaction/customer')
    return
  }
  router.push('/layout/transaction/manage')
}

function getStatusLabel(status) {
  return (
    {
      0: '待确认',
      1: '谈判中',
      2: '已签约',
      3: '已完成',
      4: '已取消'
    }[status] || '未知状态'
  )
}

function getStatusType(status) {
  return (
    {
      0: 'warning',
      1: 'primary',
      2: 'success',
      3: 'success',
      4: 'info'
    }[status] || 'info'
  )
}

function getFlowNodeState(index) {
  return getFlowNodeStateByStatus(detail.value?.status, index)
}

function getConnectorState(index) {
  return getConnectorStateByStatus(detail.value?.status, index)
}

function getFlowWrapperState(index) {
  if (index === currentFlowIndex.value) return 'is-current'
  if (index === currentFlowIndex.value - 1) return 'is-before-current'
  if (index === currentFlowIndex.value + 1) return 'is-after-current'
  return 'is-secondary'
}

function setFlowNodeRef(element, index) {
  flowNodeRefs.value[index] = element || null
}

function centerCurrentFlowStep(behavior = 'smooth') {
  if (typeof window === 'undefined' || window.innerWidth <= 1200) return
  if (!flowTrackViewportRef.value) return

  const activeNode = flowNodeRefs.value[currentFlowIndex.value]
  if (!activeNode || typeof activeNode.scrollIntoView !== 'function') return

  activeNode.scrollIntoView({
    behavior,
    block: 'nearest',
    inline: 'center'
  })
}

function parseHistory(raw) {
  if (!raw) return []

  try {
    const items = JSON.parse(raw)
    if (!Array.isArray(items)) return []

    return items.map(item => {
      const time = formatDateTime(item.time)
      const targetStatus = Number(item.to_status)
      const remark = String(item.remark || '').trim()
      const negotiationMatch = remark.match(/价格协商[:：]\s*([\d.]+).*?(?:->|→)\s*([\d.]+).*?\|\s*(.*)/)

      if (negotiationMatch) {
        return {
          kind: 'negotiation',
          title: '价格协商',
          time,
          rawTime: item.time,
          description: negotiationMatch[3] || '双方进行了一次价格沟通',
          oldPrice: negotiationMatch[1],
          newPrice: negotiationMatch[2],
          tone: 'warning'
        }
      }

      return {
        kind: 'status',
        title: `进入${getStatusLabel(targetStatus)}阶段`,
        time,
        rawTime: item.time,
        description: remark || defaultStatusDescription(targetStatus),
        tone: targetStatus >= 2 ? 'success' : targetStatus === 4 ? 'muted' : 'primary'
      }
    })
  } catch (error) {
    console.warn('解析交易历史失败:', error)
    return []
  }
}

function defaultStatusDescription(status) {
  if (status === 1) return '交易已进入谈判阶段。'
  if (status === 2) return '交易已进入签约阶段。'
  if (status === 3) return '交易已完成。'
  if (status === 4) return '交易已取消。'
  return '状态已更新。'
}

function formatPrice(value) {
  if (value === null || value === undefined || value === '') {
    return '--'
  }
  const num = Number(value)
  if (Number.isNaN(num)) return '--'
  return num.toLocaleString()
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleDateString('zh-CN')
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadDetail()
})

watch(currentFlowIndex, async () => {
  await nextTick()
  centerCurrentFlowStep()
})
</script>

<style scoped>
.transaction-detail-page {
  min-height: 100%;
  padding: 24px;
  background: #ffffff;
}

.detail-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(6, 78, 59, 0.96) 0%, rgba(5, 150, 105, 0.92) 55%, rgba(16, 185, 129, 0.86) 100%);
  color: #ffffff;
  box-shadow: 0 18px 40px rgba(6, 78, 59, 0.22);
}

.eyebrow,
.panel-kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.74);
}

.detail-hero h1 {
  margin: 0;
  font-size: 34px;
}

.hero-text {
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 16px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
  align-items: baseline;
}

.hero-chip {
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 13px;
}

/* 总价样式 - 加大字号，醒目红色 */
.hero-amount {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  margin-left: 8px;
}

.amount-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.amount-value {
  font-size: 32px;
  font-weight: 800;
  color: #ff6b6b;
  text-shadow: 0 2px 8px rgba(220, 38, 38, 0.4);
  letter-spacing: -0.02em;
}

.amount-unit {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  margin-left: 2px;
}

.hero-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  min-width: 220px;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.readonly-note {
  padding: 8px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
}

.action-bar {
  margin-top: 16px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  border: 1px solid rgba(5, 150, 105, 0.15);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(6, 78, 59, 0.06);
}

.action-bar-inner {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-label {
  font-size: 14px;
  font-weight: 600;
  color: #065f46;
  white-space: nowrap;
}

.action-buttons-compact {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.action-buttons-compact .el-button {
  border-radius: 8px;
}

.flow-panel,
.info-card {
  margin-top: 20px;
  border: 0;
  border-radius: 24px;
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.09);
}

.flow-panel {
  padding: 24px;
  background: #ffffff;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.panel-head h2 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.stage-pill {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(5, 150, 105, 0.1);
  color: #065f46;
  font-size: 14px;
}

.cancel-banner {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f3f4f6;
  color: #475569;
}

.flow-track-viewport {
  margin-top: 32px;
  overflow-x: auto;
  overflow-y: visible;
  padding: 18px max(24px, calc(50% - 118px)) 10px;
  scroll-behavior: smooth;
  scrollbar-width: none;
}

.flow-track-viewport::-webkit-scrollbar {
  display: none;
}

.flow-track {
  display: flex;
  align-items: flex-start;
  position: relative;
  width: max-content;
}

.flow-node-wrapper {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  position: relative;
  z-index: 1;
}

.flow-node-wrapper.is-current {
  z-index: 3;
}

.flow-node-wrapper.is-before-current,
.flow-node-wrapper.is-after-current {
  z-index: 2;
}

.flow-connector {
  flex: 0 0 92px;
  width: 92px;
  height: 3px;
  margin: 0 14px;
  background: #e2e8f0;
  border-radius: 2px;
  position: relative;
  top: 20px;
  transition: all 0.3s ease;
}

.flow-node-wrapper.is-current .flow-connector,
.flow-node-wrapper.is-after-current .flow-connector {
  flex-basis: 164px;
  width: 164px;
  margin: 0 22px;
}

.flow-connector.active {
  background: linear-gradient(90deg, #10b981, #059669);
}

.flow-connector.inactive {
  background: #e2e8f0;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  min-width: 118px;
  position: relative;
  z-index: 1;
  transition: transform 0.28s ease, filter 0.28s ease, opacity 0.28s ease;
}

.flow-node-wrapper:not(.is-current) .flow-node {
  transform: scale(0.82);
  opacity: 0.66;
  filter: saturate(0.72);
}

.flow-node-wrapper.is-before-current .flow-node,
.flow-node-wrapper.is-after-current .flow-node {
  transform: scale(0.86);
  opacity: 0.76;
}

.node-badge {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  background: #f1f5f9;
  color: #94a3b8;
  border: 3px solid #e2e8f0;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.node-content {
  padding: 6px 8px;
  border-radius: 16px;
  transition: all 0.28s ease;
}

.node-content strong {
  display: block;
  font-size: 15px;
  color: #64748b;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.node-content span {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
  max-width: 120px;
}

.flow-node.done .node-badge {
  background: #10b981;
  color: #ffffff;
  border-color: #10b981;
}

.flow-node.done .node-content strong {
  color: #059669;
}

.flow-node.active .node-badge {
  background: linear-gradient(135deg, #064e3b, #059669);
  color: #ffffff;
  border-color: #059669;
  box-shadow: 0 8px 20px rgba(5, 150, 105, 0.35);
  transform: scale(1.18);
}

.flow-node.active {
  transform: translateY(-10px) scale(1.04);
  opacity: 1;
  filter: none;
}

.flow-node.active .node-content {
  padding: 12px 16px;
  background: linear-gradient(180deg, rgba(240, 253, 244, 0.98), rgba(220, 252, 231, 0.82));
  box-shadow: 0 16px 30px rgba(5, 150, 105, 0.14);
}

.flow-node.active .node-content strong {
  color: #065f46;
  font-weight: 700;
  font-size: 16px;
}

.flow-node.active .node-content span {
  color: #047857;
}

.flow-node.pending .node-badge {
  background: #f8fafc;
  color: #cbd5e1;
  border-color: #e2e8f0;
}

.flow-node.disabled {
  opacity: 0.5;
}

.flow-node.disabled .node-badge {
  background: #f1f5f9;
  color: #cbd5e1;
  border-color: #e2e8f0;
}

.flow-tip {
  margin: 24px 0 0;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  color: #475569;
  font-size: 14px;
  border-left: 4px solid #059669;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.4fr 0.95fr;
  gap: 20px;
  align-items: start;
}

.main-column,
.side-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.contract-panel {
  padding: 8px 0 4px;
}

.contract-ok,
.contract-empty {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  border: 1px solid rgba(5, 150, 105, 0.1);
}

.contract-empty {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.contract-panel strong {
  display: block;
  font-size: 16px;
  color: #0f172a;
}

.contract-panel p {
  margin: 10px 0 0;
  color: #607086;
  line-height: 1.6;
}

.timeline-list,
.negotiation-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.timeline-list.is-scrollable {
  max-height: 418px;
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.65) transparent;
}

.timeline-list.is-scrollable::-webkit-scrollbar {
  width: 6px;
}

.timeline-list.is-scrollable::-webkit-scrollbar-thumb,
.negotiation-list.is-scrollable::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.65);
}

.negotiation-list.is-scrollable {
  max-height: 418px;
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.65) transparent;
}

.negotiation-list.is-scrollable::-webkit-scrollbar {
  width: 6px;
}

.timeline-item,
.negotiation-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
}

.timeline-dot {
  flex-shrink: 0;
  width: 12px;
  height: 12px;
  margin-top: 8px;
  border-radius: 50%;
  background: #94a3b8;
}

.timeline-dot.success {
  background: #10b981;
}

.timeline-dot.primary {
  background: #3b82f6;
}

.timeline-dot.warning {
  background: #f59e0b;
}

.timeline-dot.muted {
  background: #94a3b8;
}

.timeline-content,
.negotiation-item {
  flex: 1;
}

.timeline-content strong,
.negotiation-top strong {
  display: block;
  color: #0f172a;
  font-size: 15px;
}

.timeline-content small,
.negotiation-top small {
  display: block;
  margin-top: 4px;
  color: #94a3b8;
}

.timeline-content p,
.negotiation-remark {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.6;
}

.negotiation-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.price-change {
  margin: 10px 0 0;
  color: #c2410c;
  font-weight: 700;
}

.dialog-alert {
  margin-bottom: 18px;
}

@media (max-width: 1200px) {
  .flow-track-viewport {
    overflow: visible;
    padding: 0;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .flow-track {
    padding: 0;
    gap: 8px;
    width: 100%;
  }

  .flow-node-wrapper {
    flex-direction: column;
    align-items: center;
    width: 100%;
  }

  .flow-connector {
    flex: 0 0 auto;
    width: 3px;
    height: 30px;
    margin: 8px 0;
    top: 0;
    background: #e2e8f0;
  }

  .flow-node-wrapper.is-current .flow-connector,
  .flow-node-wrapper.is-after-current .flow-connector {
    width: 3px;
    height: 42px;
    margin: 12px 0;
  }

  .flow-node-wrapper:not(.is-current) .flow-node,
  .flow-node-wrapper.is-before-current .flow-node,
  .flow-node-wrapper.is-after-current .flow-node {
    transform: none;
    opacity: 1;
    filter: none;
  }

  .flow-connector.active {
    background: linear-gradient(180deg, #10b981, #059669);
  }

  .flow-track {
    flex-direction: column;
    align-items: center;
  }
}

@media (max-width: 768px) {
  .transaction-detail-page {
    padding: 16px;
  }

  .detail-hero,
  .panel-head {
    flex-direction: column;
  }

  .hero-side {
    align-items: stretch;
    min-width: 0;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-actions .el-button {
    flex: 1;
  }

  .hero-amount {
    margin-left: 0;
    margin-top: 12px;
    display: flex;
    width: 100%;
  }

  .amount-value {
    font-size: 28px;
  }

  .action-bar-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .action-buttons-compact {
    width: 100%;
  }

  .flow-track {
    flex-direction: column;
    gap: 8px;
  }

  .flow-node-wrapper {
    width: 100%;
  }

  .flow-connector {
    width: 3px;
    height: 24px;
    margin: 4px auto;
    top: 0;
  }

  .flow-node {
    flex-direction: row;
    text-align: left;
    width: 100%;
    padding: 12px;
    background: #f8fafc;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
  }

  .flow-node.active {
    transform: none;
    border-color: rgba(5, 150, 105, 0.22);
    box-shadow: 0 10px 24px rgba(5, 150, 105, 0.12);
  }

  .node-badge {
    margin-bottom: 0;
    margin-right: 16px;
    flex-shrink: 0;
  }

  .node-content {
    text-align: left;
  }

  .node-content span {
    max-width: none;
  }
}
</style>
