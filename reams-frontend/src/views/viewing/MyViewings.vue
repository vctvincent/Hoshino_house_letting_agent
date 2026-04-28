<template>
  <div class="customer-page">
    <!-- 顶部 Hero 区域 -->
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">My Viewing Schedule</p>
        <h1>我的带看</h1>
        <p class="hero-text">查看和管理您的带看预约，跟踪看房进度。</p>
      </div>
    </section>

    <!-- 统计卡片 -->
    <section class="summary-grid">
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">总带看数</span>
          <span class="summary-badge ok">活跃</span>
        </div>
        <div class="summary-main">
          <strong>{{ totalCount }}</strong>
        </div>
        <small>所有带看记录</small>
      </article>
      <article class="summary-card warn">
        <div class="summary-top">
          <span class="summary-label">待确认</span>
          <span class="summary-badge warn">待处理</span>
        </div>
        <div class="summary-main">
          <strong>{{ pendingCount }}</strong>
        </div>
        <small>等待中介确认</small>
      </article>
      <article class="summary-card muted">
        <div class="summary-top">
          <span class="summary-label">已完成</span>
          <span class="summary-badge muted">已完成</span>
        </div>
        <div class="summary-main">
          <strong>{{ completedCount }}</strong>
        </div>
        <small>已完成的带看</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">本月带看</span>
          <span class="summary-badge ok">本月</span>
        </div>
        <div class="summary-main">
          <strong>{{ monthCount }}</strong>
        </div>
        <small>本月预约带看</small>
      </article>
    </section>

    <!-- 主内容区 -->
    <section class="layout-grid">
      <div class="main-column">
        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Viewing List</p>
              <h2>带看列表</h2>
            </div>
          </header>

          <div class="table-wrapper">
            <el-table
              :data="tableData"
              border
              style="width: 100%"
              v-loading="loading"
            >
              <el-table-column prop="houseTitle" label="房源" min-width="200" show-overflow-tooltip />
              <el-table-column prop="houseAddress" label="地址" min-width="180" show-overflow-tooltip />
              <el-table-column prop="appointTime" label="预约时间" width="170">
                <template #default="{ row }">
                  {{ formatTime(row.appointTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="agentName" label="中介" width="120" />
              <el-table-column prop="agentPhone" label="中介电话" width="130" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">
                    {{ statusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <div class="action-btns">
                    <el-button size="small" type="primary" @click="handleViewDetail(row)">
                      详情
                    </el-button>
                    <el-button
                      v-if="row.status === 0 || row.status === 1"
                      size="small"
                      type="danger"
                      @click="handleCancel(row)"
                    >
                      取消
                    </el-button>
                    <el-tag v-else size="small">-</el-tag>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadMyViewings"
              @current-change="loadMyViewings"
            />
          </div>
        </article>
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="带看详情" width="700px">
      <el-descriptions v-if="currentDetail" :column="2" border>
        <el-descriptions-item label="带看 ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentDetail.status)">
            {{ statusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房源标题" :span="2">
          {{ currentDetail.houseTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="房源地址" :span="2">
          {{ currentDetail.houseAddress }}
        </el-descriptions-item>
        <el-descriptions-item label="中介姓名">{{ currentDetail.agentName }}</el-descriptions-item>
        <el-descriptions-item label="中介电话">{{ currentDetail.agentPhone }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatTime(currentDetail.appointTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.actualTime" label="实际时间">
          {{ formatTime(currentDetail.actualTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.remark" label="备注" :span="2">
          {{ currentDetail.remark }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatTime(currentDetail.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const STATUS_TEXT_MAP = Object.freeze({
  0: '待确认',
  1: '已确认',
  2: '已完成',
  3: '已取消',
  4: '已过期'
})

const STATUS_TAG_MAP = Object.freeze({
  0: 'warning',
  1: 'success',
  2: 'info',
  3: 'danger',
  4: 'info'
})

async function loadMyViewings() {
  loading.value = true
  try {
    const res = await request.get('/viewing/my/customer', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    })

    if (res.code === 200) {
      if (Array.isArray(res.data)) {
        tableData.value = res.data
        pagination.total = res.data.length
      } else {
        tableData.value = res.data?.list || []
        pagination.total = res.data?.total || 0
      }
    }
  } catch (error) {
    console.error('加载带看列表失败:', error)
    ElMessage.error('加载带看列表失败')
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row) {
  try {
    const res = await request.get(`/viewing/detail/${row.id}`)
    if (res.code === 200) {
      currentDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    console.error('加载带看详情失败:', error)
    ElMessage.error('加载带看详情失败')
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确认取消这次带看吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post(`/viewing/cancel/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('已取消带看')
      loadMyViewings()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消带看失败:', error)
      ElMessage.error('取消带看失败')
    }
  }
}

function statusText(status) {
  return STATUS_TEXT_MAP[status] || '未知'
}

function statusTagType(status) {
  return STATUS_TAG_MAP[status] || 'info'
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadMyViewings()
})
</script>

<style scoped>
.customer-page {
  padding: 20px;
}

.hero {
  background-color: #f0f2f5;
  padding: 40px 20px;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-content .eyebrow {
  font-size: 1.25rem;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
}

.hero-content h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-content .hero-text {
  font-size: 1.125rem;
  color: #909399;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.summary-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  text-align: center;
}

.summary-card .summary-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.summary-card .summary-label {
  font-size: 1.125rem;
  font-weight: 600;
  color: #303133;
}

.summary-card .summary-badge {
  font-size: 0.875rem;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  color: #fff;
}

.summary-card.ok .summary-badge {
  background-color: #67c23a;
}

.summary-card.warn .summary-badge {
  background-color: #e6a23c;
}

.summary-card.muted .summary-badge {
  background-color: #909399;
}

.summary-card .summary-main {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 10px;
}

.summary-card small {
  font-size: 0.875rem;
  color: #909399;
}

.layout-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  margin-top: 20px;
}

.main-column {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header .panel-kicker {
  font-size: 1.125rem;
  font-weight: 600;
  color: #606266;
}

.panel-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.table-wrapper {
  margin-bottom: 20px;
}

.table-wrapper .action-btns {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

</style>
