<template>
  <div class="admin-page page-shell page-shell--has-summary">
    <section class="hero page-shell-hero">
      <div class="audit-hero__content page-shell-hero__content">
        <p class="eyebrow">House Audit Studio</p>
        <h1>房源审核</h1>
        <p class="hero-text">统一处理待审核房源，让外壳、背景和下方业务面板保持同一套看板风格。</p>
      </div>
    </section>

    <section class="summary-grid page-shell-summary page-shell-summary--4">
      <article class="summary-card warn">
        <div class="summary-top">
          <span class="summary-label">待审核</span>
          <span class="summary-badge warn">处理中</span>
        </div>
        <div class="summary-main">
          <strong>{{ pendingCount }}</strong>
        </div>
        <small>当前仍待管理员处理的房源数量</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">今日通过</span>
          <span class="summary-badge ok">通过</span>
        </div>
        <div class="summary-main">
          <strong>{{ todayApproved }}</strong>
        </div>
        <small>本次会话内已确认通过的房源</small>
      </article>
      <article class="summary-card muted">
        <div class="summary-top">
          <span class="summary-label">今日驳回</span>
          <span class="summary-badge muted">已处理</span>
        </div>
        <div class="summary-main">
          <strong>{{ todayRejected }}</strong>
        </div>
        <small>本次会话内已驳回的房源</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">通过率</span>
          <span class="summary-badge ok">参考值</span>
        </div>
        <div class="summary-main">
          <strong>{{ approvalRate }}%</strong>
        </div>
        <small>根据当前处理记录估算的通过比例</small>
      </article>
    </section>

    <section class="layout-grid">
      <div class="main-column">
        <article class="panel page-shell-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Pending Review</p>
              <h2>待审核列表</h2>
            </div>
            <div class="header-actions">
              <el-button @click="loadData" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </header>

          <div v-if="tableData.length === 0" class="empty-state">
            <el-icon class="empty-icon"><DocumentChecked /></el-icon>
            <p>暂无待审核房源</p>
            <span>所有房源已审核完毕</span>
          </div>

          <div v-else class="table-wrapper">
            <el-table
              :data="tableData"
              style="width: 100%"
              :header-cell-style="headerStyle"
              row-class-name="table-row"
            >
              <el-table-column label="房源信息" min-width="280">
                <template #default="{ row }">
                  <div class="house-info">
                    <button class="house-title house-link" type="button" @click="openHousePreview(row)">
                      {{ row.title }}
                    </button>
                    <div class="house-address">
                      <el-icon><Location /></el-icon>
                      {{ row.address }}
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="价格/面积" width="150">
                <template #default="{ row }">
                  <div class="price-info">
                    <div class="price">{{ row.price }}<span>万</span></div>
                    <div class="area" v-if="row.area">{{ row.area }}㎡</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="提交人" width="120">
                <template #default="{ row }">
                  <div class="agent-info">
                    <el-avatar :size="32" :src="row.agentAvatar || ''">
                      {{ row.agentName?.charAt(0) }}
                    </el-avatar>
                    <span>{{ row.agentName }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="提交时间" width="160">
                <template #default="{ row }">
                  <div class="time-info">
                    <div>{{ formatDate(row.createTime) }}</div>
                    <div class="time-ago">{{ timeAgo(row.createTime) }}</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <div class="action-btns">
                    <el-button class="btn-pass" type="primary" @click="handleAudit(row, 2)">
                      <el-icon><Check /></el-icon>
                      通过
                    </el-button>
                    <el-button class="btn-reject" @click="handleReject(row)">
                      <el-icon><Close /></el-icon>
                      拒绝
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Location, Check, Close, DocumentChecked } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/api'

const tableData = ref([])
const loading = ref(false)
const router = useRouter()

const pendingCount = computed(() => tableData.value.length)
const todayApproved = ref(0)
const todayRejected = ref(0)
const approvalRate = computed(() => {
  const total = todayApproved.value + todayRejected.value
  return total > 0 ? ((todayApproved.value / total) * 100).toFixed(1) : 100
})

const headerStyle = () => ({
  background: '#f5f7fa',
  color: '#132238',
  fontWeight: 600,
  fontSize: '13px'
})

const formatDate = time => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(
    date.getMinutes()
  ).padStart(2, '0')}`
}

const timeAgo = time => {
  if (!time) return ''
  const diff = Date.now() - new Date(time).getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/house/pending/audit')
    tableData.value = res.data?.list || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const openHousePreview = row => {
  router.push({
    path: `/admin-layout/house/editor/${row.id}`,
    query: { readonly: '1', from: 'audit' }
  })
}

const handleAudit = async (row, status) => {
  try {
    await ElMessageBox.confirm(`确定通过房源“${row.title}”的审核吗？`, '确认通过', {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      type: 'success'
    })

    await request.post(`/house/audit/${row.id}`, null, {
      params: { auditStatus: status, reason: '' }
    })
    ElMessage.success('审核通过')
    todayApproved.value++
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReject = async row => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因，将反馈给提交人', '确认拒绝', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：图片不清晰、价格虚高、信息不完整...',
      inputValidator: value => {
        if (!value || value.trim().length < 5) {
          return '拒绝原因至少需要5个字'
        }
        return true
      }
    })

    await request.post(`/house/audit/${row.id}`, null, {
      params: { auditStatus: 3, reason }
    })
    ElMessage.success('已拒绝并通知提交人')
    todayRejected.value++
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
  min-height: 100%;
}

.hero {
  margin-bottom: 18px;
  padding: 0;
  border-radius: 16px;
}

.audit-hero__content {
  padding: 38px 32px 32px;
}

.summary-grid {
  margin-bottom: 18px;
}

.summary-card.ok::before {
  background: linear-gradient(90deg, #047857 0%, #10b981 100%);
}

.summary-card.warn::before {
  background: linear-gradient(90deg, #d97706 0%, #f59e0b 100%);
}

.summary-card.muted::before {
  background: linear-gradient(90deg, #64748b 0%, #94a3b8 100%);
}

.summary-badge.warn {
  background: rgba(245, 158, 11, 0.14);
  color: #b45309;
}

.summary-badge.muted {
  background: rgba(148, 163, 184, 0.16);
  color: #64748b;
}

.layout-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
}

.main-column {
  min-width: 0;
}

.panel {
  padding: 0;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 24px 0;
}

.panel-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #059669;
}

.panel-header h2 {
  margin: 0;
  color: #132238;
}

.table-wrapper {
  padding: 0 4px 4px;
}

:deep(.el-table) {
  border-radius: 0 0 20px 20px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper th) {
  border-bottom: 1px solid #e4e7ed !important;
}

:deep(.table-row) {
  transition: all 0.2s;
}

:deep(.table-row:hover) {
  background: rgba(236, 253, 245, 0.45);
}

.house-info {
  padding: 8px 0;
}

.house-title {
  font-size: 14px;
  font-weight: 600;
  color: #132238;
  margin-bottom: 6px;
  line-height: 1.4;
}

.house-link {
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: color 0.2s ease;
}

.house-link:hover {
  color: #059669;
}

.house-address {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.house-address .el-icon {
  font-size: 14px;
}

.price-info {
  text-align: center;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
}

.price span {
  font-size: 12px;
  font-weight: 400;
  margin-left: 2px;
}

.area {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-info span {
  font-size: 13px;
  color: #606266;
}

.time-info {
  font-size: 13px;
  color: #606266;
}

.time-ago {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-pass {
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(5, 150, 105, 0.3);
  transition: all 0.2s;
}

.btn-pass:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.4);
}

.btn-reject {
  border-radius: 999px;
  padding: 8px 16px;
  font-weight: 500;
  color: #dc2626;
  border: 1px solid #fecaca;
  background: #fef2f2;
  transition: all 0.2s;
}

.btn-reject:hover {
  color: #fff;
  background: #dc2626;
  border-color: #dc2626;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #606266;
  margin: 0 0 8px;
}

.empty-state span {
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero,
  .panel-header,
  .action-btns {
    flex-direction: column;
    align-items: flex-start;
  }

  .audit-hero__content {
    padding: 28px 20px 24px;
  }

  .hero h1 {
    font-size: 28px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .btn-pass,
  .btn-reject {
    width: 100%;
    justify-content: center;
  }
}
</style>
