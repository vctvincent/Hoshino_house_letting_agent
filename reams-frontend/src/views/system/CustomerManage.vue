<template>
  <div class="admin-page page-shell page-shell--has-summary">
    <section class="hero page-shell-hero">
      <div class="customer-hero__content page-shell-hero__content">
        <p class="eyebrow">Customer Management</p>
        <h1>客户管理</h1>
        <p class="hero-text">统一查看客户账号状态与预算信息，保留和业务看板一致的外壳、背景与统计卡片布局。</p>
      </div>
    </section>

    <section class="summary-grid page-shell-summary page-shell-summary--4">
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">客户总数</span>
          <span class="summary-badge ok">总览</span>
        </div>
        <div class="summary-main">
          <strong>{{ totalCount }}</strong>
        </div>
        <small>当前系统内已加载的客户账号数量</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">正常账号</span>
          <span class="summary-badge ok">可用</span>
        </div>
        <div class="summary-main">
          <strong>{{ normalCount }}</strong>
        </div>
        <small>当前处于正常状态的客户账号</small>
      </article>
      <article class="summary-card warn">
        <div class="summary-top">
          <span class="summary-label">禁用账号</span>
          <span class="summary-badge warn">关注</span>
        </div>
        <div class="summary-main">
          <strong>{{ disabledCount }}</strong>
        </div>
        <small>需要后续处理或复核的受限账号</small>
      </article>
      <article class="summary-card muted">
        <div class="summary-top">
          <span class="summary-label">平均预算</span>
          <span class="summary-badge muted">参考</span>
        </div>
        <div class="summary-main">
          <strong>{{ averageBudget }}</strong>
        </div>
        <small>按客户预算区间均值计算出的参考价格</small>
      </article>
    </section>

    <section class="layout-grid">
      <div class="main-column">
        <article class="panel page-shell-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Customer List</p>
              <h2>客户列表</h2>
            </div>
          </header>

          <div class="table-wrapper">
            <el-table :data="tableData" border style="width: 100%">
              <el-table-column prop="nickname" label="昵称" />
              <el-table-column prop="phone" label="手机号" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="budgetMin" label="预算下限(万)" />
              <el-table-column prop="budgetMax" label="预算上限(万)" />
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0" type="danger">禁用</el-tag>
                  <el-tag v-else type="success">正常</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <div class="action-btns">
                    <el-button
                      v-if="row.status === 1"
                      size="small"
                      type="warning"
                      @click="showDisableDialog(row)"
                    >
                      禁用
                    </el-button>
                    <el-button
                      v-else-if="row.status === 0"
                      size="small"
                      type="primary"
                      @click="handleEnable(row)"
                    >
                      解禁
                    </el-button>
                    <el-button size="small" type="danger" @click="handleDelete(row)">
                      删除
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
import request from '@/api'

const tableData = ref([])

const totalCount = computed(() => tableData.value.length)
const normalCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const disabledCount = computed(() => tableData.value.filter(item => item.status === 0).length)
const averageBudget = computed(() => {
  if (tableData.value.length === 0) return 0
  const total = tableData.value.reduce((sum, item) => {
    const min = Number(item.budgetMin) || 0
    const max = Number(item.budgetMax) || 0
    return sum + (min + max) / 2
  }, 0)
  return (total / tableData.value.length).toFixed(1)
})

const loadData = async () => {
  try {
    const res = await request.get('/customer/list')
    tableData.value = res.data?.list || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  }
}

const showDisableDialog = row => {
  ElMessageBox.confirm(`确定要禁用客户“${row.nickname}”吗？`, '确认禁用', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      handleDisable(row)
    })
    .catch(() => {})
}

const handleDisable = async row => {
  try {
    await request.put(`/customer/${row.id}/status`, { status: 0 })
    ElMessage.success('已禁用')
    loadData()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const handleEnable = async row => {
  try {
    await ElMessageBox.confirm(`确定要解禁客户“${row.nickname}”吗？`, '确认解禁', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await request.put(`/customer/${row.id}/status`, { status: 1 })
    ElMessage.success('已解禁')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(`确定要删除客户“${row.nickname}”吗？此操作不可恢复。`, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'danger'
    })

    await request.delete(`/customer/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
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

.customer-hero__content {
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
  padding: 16px 24px 24px;
}

.action-btns {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .hero,
  .panel-header,
  .action-btns {
    flex-direction: column;
    align-items: flex-start;
  }

  .customer-hero__content {
    padding: 28px 20px 24px;
  }

  .hero h1 {
    font-size: 28px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .action-btns .el-button {
    width: 100%;
  }
}
</style>
