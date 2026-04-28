<template>
  <div class="customer-page">
    <!-- 顶部 Hero 区域 -->
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">My Transaction Center</p>
        <h1>我的交易</h1>
        <p class="hero-text">查看和管理您的房产交易记录，跟踪交易进度。</p>
      </div>
    </section>

    <!-- 统计卡片 -->
    <section class="summary-grid">
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">总交易数</span>
          <span class="summary-badge ok">活跃</span>
        </div>
        <div class="summary-main">
          <strong>{{ totalCount }}</strong>
        </div>
        <small>所有交易记录</small>
      </article>
      <article class="summary-card warn">
        <div class="summary-top">
          <span class="summary-label">待确认</span>
          <span class="summary-badge warn">待处理</span>
        </div>
        <div class="summary-main">
          <strong>{{ pendingCount }}</strong>
        </div>
        <small>需要您确认</small>
      </article>
      <article class="summary-card muted">
        <div class="summary-top">
          <span class="summary-label">谈判中</span>
          <span class="summary-badge muted">进行中</span>
        </div>
        <div class="summary-main">
          <strong>{{ negotiatingCount }}</strong>
        </div>
        <small>正在协商价格</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">已完成</span>
          <span class="summary-badge ok">已完成</span>
        </div>
        <div class="summary-main">
          <strong>{{ completedCount }}</strong>
        </div>
        <small>已成功完成交易</small>
      </article>
    </section>

    <!-- 主内容区 -->
    <section class="layout-grid">
      <div class="main-column">
        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Transaction List</p>
              <h2>交易列表</h2>
            </div>
          </header>

          <!-- 表格 -->
          <div class="table-wrapper">
            <el-table :data="tableData" border style="width: 100%" v-loading="loading">
              <el-table-column prop="transactionNo" label="交易单号" width="180" />
              <el-table-column prop="houseTitle" label="房源" min-width="200" />
              <el-table-column prop="agentName" label="中介" width="100" />
              <el-table-column prop="finalPrice" label="成交价 (万)" width="100" />
              <el-table-column prop="dealDate" label="成交日期" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0">待确认</el-tag>
                  <el-tag v-else-if="row.status === 1" type="warning">谈判中</el-tag>
                  <el-tag v-else-if="row.status === 2" type="success">已签约</el-tag>
                  <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
                  <el-tag v-else-if="row.status === 4" type="info">已取消</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <div class="action-btns">
                    <el-button size="small" @click="viewDetail(row)">交易详情</el-button>
                    <el-button
                      v-if="row.status === 0"
                      size="small"
                      type="success"
                      @click="confirmTransaction(row)"
                    >
                      确认交易
                    </el-button>
                    <el-button
                      v-if="row.status === 2"
                      size="small"
                      type="primary"
                      @click="uploadContract(row)"
                    >
                      上传合同
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </article>
      </div>
    </section>

    <!-- 上传合同对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传合同" width="600px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="合同 URL" required>
          <el-input v-model="uploadForm.contractUrl" placeholder="/uploads/contracts/xxx.pdf" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadContract">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/api'

export default {
  name: 'CustomerTransaction',
  setup() {
    const router = useRouter()
    const tableData = ref([])
    const loading = ref(false)
    const showUploadDialog = ref(false)

    const uploadForm = ref({
      contractUrl: ''
    })

    const currentTransaction = ref(null)

    const loadData = async () => {
      loading.value = true
      try {
        const res = await request.get('/transaction/my/customer')
        if (res.code === 200) {
          tableData.value = res.data || []
        }
      } catch (error) {
        console.error('加载交易列表失败:', error)
        ElMessage.error('加载交易列表失败')
      } finally {
        loading.value = false
      }
    }

    const viewDetail = (row) => {
      router.push(`/layout/transaction/detail/${row.id}`)
    }

    const confirmTransaction = async (row) => {
      try {
        await ElMessageBox.confirm('确定要确认这笔交易吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })

        const res = await request.post(`/transaction/status/${row.id}`, null, {
          params: { status: 1 }
        })

        if (res.code === 200) {
          ElMessage.success('交易已确认')
          loadData()
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

    const uploadContract = (row) => {
      currentTransaction.value = row
      uploadForm.value.contractUrl = ''
      showUploadDialog.value = true
    }

    const handleUploadContract = async () => {
      if (!uploadForm.value.contractUrl) {
        ElMessage.warning('请填写合同 URL')
        return
      }

      try {
        const res = await request.put('/transaction/update', {
          id: currentTransaction.value.id,
          contractUrl: uploadForm.value.contractUrl,
          status: 2 // 已签约
        })

        if (res.code === 200) {
          ElMessage.success('合同上传成功')
          showUploadDialog.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        console.error('上传合同失败:', error)
        ElMessage.error('上传合同失败')
      }
    }

    onMounted(() => {
      loadData()
    })

    return {
      tableData,
      loading,
      showUploadDialog,
      uploadForm,
      loadData,
      viewDetail,
      confirmTransaction,
      uploadContract,
      handleUploadContract
    }
  }
}
</script>

<style scoped>
.customer-transaction {
  padding: 20px;
}
</style>
