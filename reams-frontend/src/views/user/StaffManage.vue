<template>
  <div class="staff-manage-page page-shell">
    <!-- 顶部标题区 -->
    <section class="manage-hero page-shell-hero">
      <div class="manage-hero__content page-shell-hero__content">
        <p class="eyebrow">House Studio</p>
        <h1>人员管理</h1>
        <p class="hero-text">统一管理中介和客户信息、账号状态，确保平台用户质量</p>
        <div class="manage-hero__metrics">
          <div class="manage-hero__chip">
            <span>中介</span>
            <strong>{{ agentPagination.total }} 人</strong>
          </div>
          <div class="manage-hero__chip">
            <span>客户</span>
            <strong>{{ customerPagination.total }} 人</strong>
          </div>
        </div>
      </div>
    </section>

    <!-- 内容区域 -->
    <div class="manage-content page-shell-panel">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
        <!-- 中介管理 -->
        <el-tab-pane label="中介管理" name="agent">
          <div class="tab-toolbar">
            <h2>中介列表</h2>
            <div class="filter-group">
              <el-input
                v-model="agentSearch.keyword"
                placeholder="搜索姓名或手机号"
                clearable
                class="search-input"
                @clear="loadAgentList"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="agentSearch.status"
                placeholder="状态"
                clearable
                class="filter-select"
                @change="loadAgentList"
              >
                <el-option label="正常" :value="1" />
                <el-option label="待审核" :value="2" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <el-button type="primary" @click="loadAgentList">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table
              :data="agentList"
              style="width: 100%"
              v-loading="agentLoading"
              :header-cell-style="headerStyle"
              row-class-name="table-row"
            >
              <el-table-column label="用户信息" min-width="200">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="40" :src="row.avatar || ''" class="user-avatar">
                      {{ row.name?.charAt(0) }}
                    </el-avatar>
                    <div class="user-detail">
                      <div class="user-name">{{ row.name }}</div>
                      <div class="user-phone">
                        <el-icon><Phone /></el-icon>
                        {{ row.phone }}
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="邮箱" min-width="180">
                <template #default="{ row }">
                  <div class="email-text">
                    <el-icon><Message /></el-icon>
                    {{ row.email || '-' }}
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <div class="status-tag" :class="`status-${row.status}`">
                    <span class="status-dot"></span>
                    {{ row.status === 1 ? '正常' : row.status === 2 ? '待审核' : '禁用' }}
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="注册时间" width="160">
                <template #default="{ row }">
                  <div class="time-info">
                    <div>{{ formatDate(row.createTime) }}</div>
                    <div class="time-ago">{{ timeAgo(row.createTime) }}</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <div class="action-btns">
                    <template v-if="row.status === 2">
                      <el-button class="btn-pass" size="small" @click="approveAgent(row)">
                        <el-icon><Check /></el-icon>
                        通过
                      </el-button>
                      <el-button class="btn-reject" size="small" @click="rejectAgent(row)">
                        <el-icon><Close /></el-icon>
                        拒绝
                      </el-button>
                    </template>
                    <template v-else>
                      <el-button
                        :class="row.status === 1 ? 'btn-ban' : 'btn-enable'"
                        size="small"
                        @click="updateAgentStatus(row, row.status === 1 ? 0 : 1)"
                      >
                        <el-icon><Lock v-if="row.status === 1" /><Unlock v-else /></el-icon>
                        {{ row.status === 1 ? '禁用' : '解禁' }}
                      </el-button>
                    </template>
                    <el-button class="btn-delete" size="small" @click="deleteAgent(row)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-table-column>

              <template #empty>
                <div class="empty-state">
                  <el-icon class="empty-icon"><User /></el-icon>
                  <p>暂无中介数据</p>
                </div>
              </template>
            </el-table>

            <el-pagination
              v-model:current-page="agentPagination.pageNum"
              v-model:page-size="agentPagination.pageSize"
              :total="agentPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, prev, pager, next, sizes"
              @size-change="loadAgentList"
              @current-change="loadAgentList"
              class="pagination-bar"
            />
          </div>
        </el-tab-pane>

        <!-- 客户管理 -->
        <el-tab-pane label="客户管理" name="customer">
          <div class="tab-toolbar">
            <h2>客户列表</h2>
            <div class="filter-group">
              <el-input
                v-model="customerSearch.keyword"
                placeholder="搜索昵称或手机号"
                clearable
                class="search-input"
                @clear="loadCustomerList"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="customerSearch.status"
                placeholder="状态"
                clearable
                class="filter-select"
                @change="loadCustomerList"
              >
                <el-option label="正常" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <el-button type="primary" @click="loadCustomerList">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table
              :data="customerList"
              style="width: 100%"
              v-loading="customerLoading"
              :header-cell-style="headerStyle"
              row-class-name="table-row"
            >
              <el-table-column label="用户信息" min-width="200">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="40" :src="row.avatar || ''" class="user-avatar">
                      {{ row.nickname?.charAt(0) }}
                    </el-avatar>
                    <div class="user-detail">
                      <div class="user-name">{{ row.nickname }}</div>
                      <div class="user-phone">
                        <el-icon><Phone /></el-icon>
                        {{ row.phone }}
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="邮箱" min-width="180">
                <template #default="{ row }">
                  <div class="email-text">
                    <el-icon><Message /></el-icon>
                    {{ row.email || '-' }}
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <div class="status-tag" :class="`status-${row.status}`">
                    <span class="status-dot"></span>
                    {{ row.status === 1 ? '正常' : '禁用' }}
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="注册时间" width="160">
                <template #default="{ row }">
                  <div class="time-info">
                    <div>{{ formatDate(row.createTime) }}</div>
                    <div class="time-ago">{{ timeAgo(row.createTime) }}</div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <div class="action-btns">
                    <el-button
                      :class="row.status === 1 ? 'btn-ban' : 'btn-enable'"
                      size="small"
                      @click="updateCustomerStatus(row, row.status === 1 ? 0 : 1)"
                    >
                      <el-icon><Lock v-if="row.status === 1" /><Unlock v-else /></el-icon>
                      {{ row.status === 1 ? '禁用' : '解禁' }}
                    </el-button>
                    <el-button class="btn-delete" size="small" @click="deleteCustomer(row)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-table-column>

              <template #empty>
                <div class="empty-state">
                  <el-icon class="empty-icon"><User /></el-icon>
                  <p>暂无客户数据</p>
                </div>
              </template>
            </el-table>

            <el-pagination
              v-model:current-page="customerPagination.pageNum"
              v-model:page-size="customerPagination.pageSize"
              :total="customerPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, prev, pager, next, sizes"
              @size-change="loadCustomerList"
              @current-change="loadCustomerList"
              class="pagination-bar"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Phone,
  Message,
  Check,
  Close,
  Lock,
  Unlock,
  Delete,
  User
} from '@element-plus/icons-vue'
import request from '@/api'

const activeTab = ref('agent')
const route = useRoute()

const headerStyle = () => ({
  background: '#f5f7fa',
  color: '#132238',
  fontWeight: 600,
  fontSize: '13px'
})

const formatDate = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const timeAgo = (time) => {
  if (!time) return ''
  const diff = Date.now() - new Date(time).getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const months = Math.floor(days / 30)
  return `${months}个月前`
}

// ========== 中介管理 ==========
const agentList = ref([])
const agentLoading = ref(false)
const agentSearch = reactive({
  keyword: '',
  status: null
})
const agentPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadAgentList = async () => {
  agentLoading.value = true
  try {
    const res = await request.get('/user/agent/list', {
      params: {
        pageNum: agentPagination.pageNum,
        pageSize: agentPagination.pageSize,
        keyword: agentSearch.keyword || undefined,
        status: agentSearch.status
      }
    })
    if (res.code === 200) {
      agentList.value = res.data.list || []
      agentPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载中介列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    agentLoading.value = false
  }
}

const updateAgentStatus = async (row, newStatus) => {
  const actionText = newStatus === 1 ? '解禁' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}中介"${row.name}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await request.put(`/user/agent/status/${row.id}`, null, {
      params: { status: newStatus }
    })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadAgentList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(`${actionText}失败`)
    }
  }
}

const approveAgent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定通过中介"${row.name}"的审核吗？`,
      '确认通过',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const res = await request.put(`/user/agent/approve/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('审核通过')
      loadAgentList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('审核失败')
    }
  }
}

const rejectAgent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定拒绝中介"${row.name}"的审核吗？`,
      '确认拒绝',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const res = await request.put(`/user/agent/reject/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      loadAgentList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('拒绝失败')
    }
  }
}

const deleteAgent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除中介"${row.name}"？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const res = await request.delete(`/user/agent/delete/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAgentList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

// ========== 客户管理 ==========
const customerList = ref([])
const customerLoading = ref(false)
const customerSearch = reactive({
  keyword: '',
  status: null
})
const customerPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadCustomerList = async () => {
  customerLoading.value = true
  try {
    const res = await request.get('/user/customer/list', {
      params: {
        pageNum: customerPagination.pageNum,
        pageSize: customerPagination.pageSize,
        keyword: customerSearch.keyword || undefined,
        status: customerSearch.status
      }
    })
    if (res.code === 200) {
      customerList.value = res.data.list || []
      customerPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    customerLoading.value = false
  }
}

const updateCustomerStatus = async (row, newStatus) => {
  const actionText = newStatus === 1 ? '解禁' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}客户"${row.nickname}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await request.put(`/user/customer/status/${row.id}`, null, {
      params: { status: newStatus }
    })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadCustomerList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(`${actionText}失败`)
    }
  }
}

const deleteCustomer = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除客户"${row.nickname}"？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const res = await request.delete(`/user/customer/delete/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadCustomerList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const handleTabChange = (tab) => {
  if (tab === 'agent' && agentList.value.length === 0) {
    loadAgentList()
  } else if (tab === 'customer' && customerList.value.length === 0) {
    loadCustomerList()
  }
}

const applyRouteFilters = () => {
  if (route.query.tab === 'agent') {
    activeTab.value = 'agent'
  } else if (route.query.tab === 'customer') {
    activeTab.value = 'customer'
  }

  if (activeTab.value === 'agent') {
    agentPagination.pageNum = 1
    if (route.query.status !== undefined && route.query.status !== '') {
      agentSearch.status = Number(route.query.status)
    } else {
      agentSearch.status = null
    }
  }
}

onMounted(() => {
  applyRouteFilters()
  loadAgentList()
})
</script>

<style scoped>
.staff-manage-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
  min-height: 100%;
}

/* 消除 page-shell 带来的双层视觉效果 */
.staff-manage-page :deep(.page-shell-shell-disabled),
.staff-manage-page :deep(.page-shell-hero-disabled),
.staff-manage-page :deep(.page-shell-hero--split-disabled) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
  border: none !important;
}

/* 确保整个页面背景统一 */
.staff-manage-page--legacy {
  background: #f0f2f5;
  padding: 0;
}

.manage-hero {
  margin-bottom: 18px;
  padding: 0;
  border-radius: 16px;
}

.manage-hero__content {
  padding: 38px 32px 32px;
  min-width: 0;
}

.manage-hero__metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.manage-hero__chip {
  padding: 16px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.9) 0%, rgba(209, 250, 229, 0.7) 100%);
  border: 1px solid rgba(5, 150, 105, 0.12);
}

.manage-hero__chip span {
  display: block;
  margin-bottom: 8px;
  color: #607086;
  font-size: 13px;
}

.manage-hero__chip strong {
  color: #064e3b;
  font-size: 20px;
  font-weight: 700;
}

.summary-grid {
  margin-top: 0;
  margin-bottom: 18px;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 0 0 18px 18px;
  box-shadow: none;
}

.summary-grid .summary-card:first-child {
  border-top: 1px solid rgba(5, 150, 105, 0.08);
  margin-top: -4px;
  padding-top: 22px;
}

/* 内容区域 */
.manage-content {
  padding: 0;
  overflow: hidden;
}

/* 自定义标签页 */
:deep(.custom-tabs) {
  --el-tabs-header-height: 56px;
}

:deep(.custom-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: linear-gradient(180deg, rgba(240, 253, 244, 0.85), rgba(236, 253, 245, 0.65));
  border-bottom: 1px solid rgba(5, 150, 105, 0.08);
}

:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.custom-tabs .el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  padding: 0 24px;
}

:deep(.custom-tabs .el-tabs__item.is-active) {
  color: #059669;
  font-weight: 600;
}

:deep(.custom-tabs .el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
}

/* 工具栏 */
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(5, 150, 105, 0.08);
}

.tab-toolbar h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #132238;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 120px;
}

/* 表格区域 */
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

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.user-avatar {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #132238;
}

.user-phone {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 邮箱 */
.email-text {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.email-text .el-icon {
  color: #909399;
}

/* 状态标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-1 {
  background: #f0f9eb;
  color: #67c23a;
}

.status-1 .status-dot {
  background: #67c23a;
}

.status-2 {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-2 .status-dot {
  background: #e6a23c;
}

.status-0 {
  background: #fef0f0;
  color: #f56c6c;
}

.status-0 .status-dot {
  background: #f56c6c;
}

/* 时间 */
.time-info {
  font-size: 13px;
  color: #606266;
}

.time-ago {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 8px;
  align-items: center;
}

.btn-pass {
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 500;
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  border: none;
  color: #fff;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
  transition: all 0.2s;
}

.btn-pass:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.btn-reject {
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 500;
  color: #f56c6c;
  border: 1px solid #fcd3d3;
  background: #fef0f0;
  transition: all 0.2s;
}

.btn-reject:hover {
  color: #fff;
  background: #f56c6c;
  border-color: #f56c6c;
}

.btn-enable {
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 500;
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  border: none;
  color: #fff;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.btn-ban {
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 500;
  color: #e6a23c;
  border: 1px solid #f5dab1;
  background: #fdf6ec;
  transition: all 0.2s;
}

.btn-ban:hover {
  color: #fff;
  background: #e6a23c;
  border-color: #e6a23c;
}

.btn-delete {
  border-radius: 8px;
  padding: 6px 10px;
  color: #909399;
  border: 1px solid #d9d9d9;
  background: #fff;
  transition: all 0.2s;
}

.btn-delete:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background: #fef0f0;
}

/* 分页 */
.pagination-bar {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .manage-hero {
    flex-direction: column;
  }

  .manage-hero__metrics {
    width: 100%;
    min-width: 0;
  }

  .tab-toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .filter-group {
    width: 100%;
  }

  .search-input {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .staff-manage-page {
    padding: 16px;
  }

  .manage-hero {
    padding: 0;
  }

  .manage-hero__content {
    padding: 28px 20px 24px;
  }

  .manage-hero__metrics {
    grid-template-columns: 1fr;
  }

  .filter-group {
    flex-wrap: wrap;
  }

  .action-btns {
    flex-wrap: wrap;
  }
}
</style>
