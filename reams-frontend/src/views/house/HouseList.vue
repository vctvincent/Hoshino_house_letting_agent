<template>
  <div :class="[isBackoffice ? 'house-list-page' : 'customer-page', 'page-shell', 'page-shell--has-summary']">
    <section :class="['hero', 'page-shell-hero', { 'list-hero': isBackoffice }]">
      <div :class="[isBackoffice ? 'list-hero__content' : 'hero-content', 'page-shell-hero__content']">
        <p class="eyebrow">House Listing Center</p>
        <h1>房源列表</h1>
        <p class="hero-text">浏览和搜索所有可用房源，找到您理想的居所。</p>
      </div>
    </section>

    <section v-if="isAdmin" class="summary-grid page-shell-summary page-shell-summary--4">
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">总房源数</span>
          <span class="summary-badge ok">活跃</span>
        </div>
        <div class="summary-main">
          <strong>{{ totalCount }}</strong>
        </div>
        <small>平台所有房源</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">已发布</span>
          <span class="summary-badge ok">正常</span>
        </div>
        <div class="summary-main">
          <strong>{{ publishedCount }}</strong>
        </div>
        <small>正在展示的房源</small>
      </article>
      <article class="summary-card muted">
        <div class="summary-top">
          <span class="summary-label">平均价格</span>
          <span class="summary-badge muted">统计</span>
        </div>
        <div class="summary-main">
          <strong>{{ averagePrice }}万</strong>
        </div>
        <small>房源平均售价</small>
      </article>
      <article class="summary-card ok">
        <div class="summary-top">
          <span class="summary-label">今日新增</span>
          <span class="summary-badge ok">新增</span>
        </div>
        <div class="summary-main">
          <strong>{{ todayAdded }}</strong>
        </div>
        <small>今日新发布房源</small>
      </article>
    </section>

    <section class="layout-grid page-shell-stack">
      <div class="main-column">
        <article class="panel page-shell-panel">
          <header class="panel-header page-shell-panel__header">
            <div>
              <p class="panel-kicker page-shell-panel__kicker">House Gallery</p>
              <h2>房源展示</h2>
            </div>
          </header>

          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="城市">
              <el-select v-model="searchForm.city" placeholder="请选择城市" clearable style="width: 140px;" @change="handleCityChange">
                <el-option
                  v-for="city in cityList"
                  :key="city.value"
                  :label="city.label"
                  :value="city.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="区域">
              <el-select v-model="searchForm.district" placeholder="请选择区域" clearable style="width: 140px;" @change="handleSearch">
                <el-option
                  v-for="district in districtList"
                  :key="district.value"
                  :label="district.label"
                  :value="district.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="价格">
              <el-input v-model="searchForm.minPrice" placeholder="最低价" style="width: 100px;" />
              <span style="margin: 0 5px;">-</span>
              <el-input v-model="searchForm.maxPrice" placeholder="最高价" style="width: 100px;" />
            </el-form-item>
            <el-form-item label="户型">
              <el-select v-model="searchForm.houseType" placeholder="请选择" clearable class="filter-select">
                <el-option
                  v-for="option in houseTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="isAdmin" label="交易状态">
              <el-select v-model="searchForm.houseStatus" placeholder="请选择" clearable class="filter-select" @change="handleSearch">
                <el-option
                  v-for="option in houseStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="20" class="house-grid">
            <el-col v-for="house in houseList" :key="house.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
              <el-card class="house-card" shadow="hover" @click="goToDetail(house.id)">
                <div class="house-image">
                  <div v-if="isAdmin" class="admin-status-stack">
                    <el-tag class="admin-status-badge" effect="dark" :type="getHouseStatusTagType(house.houseStatus)">
                      {{ getHouseStatusLabel(house.houseStatus) }}
                    </el-tag>
                    <el-tag
                      v-if="house.auditStatus !== null && house.auditStatus !== undefined"
                      class="admin-status-badge"
                      effect="plain"
                      :type="getAuditStatusTagType(house.auditStatus)"
                    >
                      {{ getAuditStatusLabel(house.auditStatus) }}
                    </el-tag>
                  </div>
                  <img
                    :src="getHouseImage(house.images)"
                    alt="房源图片"
                    @error="handleImageError"
                  />
                  <div class="image-overlay">
                    <i class="el-icon-view"></i> 查看详情
                  </div>
                </div>
                <div class="house-info">
                  <h3 class="house-title" @click.stop="goToDetail(house.id)">{{ house.title || '暂无标题' }}</h3>
                  <p class="house-address">{{ house.province }}{{ house.city }}{{ house.district }}{{ house.address }}</p>
                  <div class="house-tags">
                    <el-tag size="small">{{ house.houseType || '未知户型' }}</el-tag>
                    <el-tag size="small" type="info">{{ house.area || '--' }}㎡</el-tag>
                    <el-tag size="small" type="success">{{ house.decoration || '未知装修' }}</el-tag>
                  </div>
                  <div class="house-price">
                    <span class="price">{{ house.price || '--' }}</span>
                    <span class="unit">万元</span>
                  </div>
                  <div class="house-agent">
                    <span>中介：{{ house.agentName || '未知' }}</span>
                  </div>

                  <div v-if="isSelectMode" class="select-action">
                    <el-button type="success" @click.stop="selectHouse(house)">
                      <el-icon><Check /></el-icon>
                      选择此房源
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 15, 20]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadData"
              @current-change="loadData"
            />
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import request from '@/api'
import { summarizeHouseListStats } from '../../utils/houseListStats'

export default {
  name: 'HouseList',
  components: {
    Check
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const isSelectMode = computed(() => route.query.selectForViewing === '1')
    const selectListingAgentId = computed(() => route.query.listingAgentId || route.query.agentId)
    const isBackoffice = computed(() => {
      const currentPath = route.path || ''
      return currentPath.startsWith('/agent-layout/') || currentPath.startsWith('/admin-layout/')
    })

    const searchForm = reactive({
      city: '',
      district: '',
      minPrice: '',
      maxPrice: '',
      houseType: '',
      houseStatus: ''
    })

    const cityList = ref([])
    const districtList = ref([])
    const pagination = reactive({
      pageNum: 1,
      pageSize: 10,
      total: 0
    })
    const houseList = ref([])

    const isAdmin = computed(() => sessionStorage.getItem('role') === 'ROLE_ADMIN')
    const totalCount = ref(0)
    const publishedCount = ref(0)
    const averagePrice = ref('0.0')
    const todayAdded = ref(0)
    const houseTypeOptions = [
      { value: '1 室', label: '1 室' },
      { value: '2 室', label: '2 室' },
      { value: '3 室', label: '3 室' },
      { value: '4 室', label: '4 室' },
      { value: '5 室', label: '5 室及以上' }
    ]
    const houseStatusOptions = [
      { value: 0, label: '未发布' },
      { value: 1, label: '已发布' },
      { value: 2, label: '已成交' },
      { value: 3, label: '已下架' }
    ]
    const houseStatusTagTypes = {
      0: 'info',
      1: 'success',
      2: 'danger',
      3: 'warning'
    }
    const auditStatusTagTypes = {
      0: 'warning',
      1: 'info',
      2: 'success',
      3: 'danger'
    }

    const normalizeStatus = status => {
      if (status === '' || status === null || status === undefined) {
        return NaN
      }
      return Number(status)
    }

    const getHouseStatusLabel = status => {
      const normalizedStatus = normalizeStatus(status)
      const matchedOption = houseStatusOptions.find(option => option.value === normalizedStatus)
      return matchedOption?.label || '状态未知'
    }

    const getHouseStatusTagType = status => houseStatusTagTypes[normalizeStatus(status)] || 'info'

    const getAuditStatusLabel = status => {
      const normalizedStatus = normalizeStatus(status)
      return ({
        0: '待审核',
        1: '审核中',
        2: '已通过',
        3: '已拒绝'
      })[normalizedStatus] || '审核未知'
    }

    const getAuditStatusTagType = status => auditStatusTagTypes[normalizeStatus(status)] || 'info'

    const loadCityList = async () => {
      try {
        const res = await request.get('/house/cities')
        const cities = res.data || []
        cityList.value = cities.map(city => ({ label: city, value: city }))
      } catch (error) {
        console.error('加载城市列表失败:', error)
      }
    }

    const loadDistrictList = async city => {
      try {
        const res = await request.get('/house/districts', { params: { city } })
        const districts = res.data || []
        districtList.value = districts.map(district => ({ label: district, value: district }))
      } catch (error) {
        console.error('加载区域列表失败:', error)
        districtList.value = []
      }
    }

    const handleCityChange = city => {
      searchForm.district = ''
      if (city) {
        loadDistrictList(city)
      } else {
        districtList.value = []
      }
      handleSearch()
    }

    const applyAdminStats = stats => {
      totalCount.value = stats.totalCount
      publishedCount.value = stats.publishedCount
      averagePrice.value = stats.averagePrice
      todayAdded.value = stats.todayAdded
    }

    const loadAdminStats = async () => {
      try {
        const totalRes = await request.get('/house/list', {
          params: { pageNum: 1, pageSize: 1 }
        })
        const total = totalRes.data?.total || 0

        const fullListRes = await request.get('/house/list', {
          params: { pageNum: 1, pageSize: Math.max(total, 1) }
        })

        applyAdminStats(
          summarizeHouseListStats(fullListRes.data?.list || [], { totalCount: total })
        )
      } catch (error) {
        console.error('加载房源统计失败:', error)
      }
    }

    const loadData = async () => {
      try {
        const params = {
          pageNum: pagination.pageNum,
          pageSize: pagination.pageSize
        }

        if (isSelectMode.value && selectListingAgentId.value) {
          params.agentId = selectListingAgentId.value
          params.auditStatus = 2
          params.houseStatus = 1
        }

        if (searchForm.city) params.city = searchForm.city
        if (searchForm.district) params.district = searchForm.district
        if (searchForm.minPrice) params.minPrice = searchForm.minPrice
        if (searchForm.maxPrice) params.maxPrice = searchForm.maxPrice
        if (searchForm.houseType) params.houseType = searchForm.houseType
        if (searchForm.houseStatus !== '' && searchForm.houseStatus !== null && searchForm.houseStatus !== undefined) {
          params.houseStatus = searchForm.houseStatus
        }

        if ((params.houseStatus === undefined || params.houseStatus === null || params.houseStatus === '') && !isSelectMode.value) {
          params.auditStatus = 2
        }

        const res = await request.get('/house/list', { params })
        houseList.value = res.data.list || []
        pagination.total = res.data.total || 0

      } catch (error) {
        console.error('加载房源列表失败:', error)
      }
    }

    const handleSearch = () => {
      pagination.pageNum = 1
      loadData()
    }

    const handleReset = () => {
      Object.assign(searchForm, {
        city: '',
        district: '',
        minPrice: '',
        maxPrice: '',
        houseType: '',
        houseStatus: ''
      })
      districtList.value = []
      handleSearch()
    }

    const goToDetail = id => {
      const currentPath = route.path || ''
      if (currentPath.startsWith('/agent-layout/')) {
        router.push(`/agent-layout/house/detail/${id}`)
        return
      }
      if (currentPath.startsWith('/admin-layout/')) {
        router.push(`/admin-layout/house/detail/${id}`)
        return
      }
      router.push(`/layout/house/detail/${id}`)
    }

    const selectHouse = house => {
      ElMessage.success(`已选择房源：${house.title}`)
      setTimeout(() => {
        router.push({
          path: route.query.returnTo || '/layout/message',
          query: {
            selectedHouseId: house.id,
            targetId: route.query.targetId || selectListingAgentId.value,
            targetType: route.query.targetType || 'AGENT'
          }
        })
      }, 500)
    }

    const getHouseImage = imagesStr => {
      try {
        if (!imagesStr) return '/default-house.jpg'
        const images = JSON.parse(imagesStr)
        let firstImage = images && images.length > 0 ? images[0] : '/default-house.jpg'
        if (firstImage && !firstImage.startsWith('/') && !firstImage.startsWith('http')) {
          firstImage = '/' + firstImage
        }
        return firstImage
      } catch {
        return '/default-house.jpg'
      }
    }

    const handleImageError = event => {
      event.target.src = '/default-house.jpg'
    }

    const parseUrlParams = () => {
      const params = new URLSearchParams(window.location.search)
      const type = params.get('type')
      const city = params.get('city')
      const district = params.get('district')

      if (type) {
        searchForm.houseType = type
      }
      if (city) {
        searchForm.city = city
        loadDistrictList(city)
      }
      if (district) {
        searchForm.district = district
      }
    }

    watch(
      () => [route.query.selectForViewing, route.query.agentId],
      ([newSelectMode, newAgentId]) => {
        if (newSelectMode === '1' && newAgentId) {
          pagination.pageNum = 1
          loadData()
        }
      },
      { immediate: false }
    )

    onMounted(() => {
      loadCityList()
      parseUrlParams()
      loadData()
      if (isAdmin.value) {
        loadAdminStats()
      }
    })

    return {
      searchForm,
      pagination,
      houseList,
      houseTypeOptions,
      houseStatusOptions,
      isAdmin,
      isBackoffice,
      totalCount,
      publishedCount,
      averagePrice,
      todayAdded,
      cityList,
      districtList,
      handleCityChange,
      isSelectMode,
      loadData,
      handleSearch,
      handleReset,
      goToDetail,
      selectHouse,
      getHouseImage,
      handleImageError,
      getHouseStatusLabel,
      getHouseStatusTagType,
      getAuditStatusLabel,
      getAuditStatusTagType
    }
  }
}
</script>
<style scoped>
.house-list-page {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
  min-height: 100%;
}

/* 娑堥櫎 page-shell 甯︽潵鐨勫弻灞傝瑙夋晥鏋?*/
.house-list-page :deep(.page-shell-disabled),
.house-list-page :deep(.page-shell-hero-disabled),
.house-list-page :deep(.page-shell-hero--split-disabled) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
  border: none !important;
}

/* 纭繚鏁翠釜椤甸潰鑳屾櫙缁熶竴 */
.house-list-page {
  background: #f0f2f5;
}

.house-list-page .list-hero--legacy {
  position: relative;
  margin-bottom: 24px;
  padding: 0;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
  overflow: hidden;
}

.house-list-page .list-hero--legacy::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 6px;
  background: linear-gradient(90deg, #064e3b 0%, #059669 50%, #10b981 100%);
}

.house-list-page .list-hero__content--legacy {
  flex: 1;
  padding: 38px 32px 32px;
}

.layout-grid {
  display: block;
}

.main-column {
  min-width: 0;
}

.search-form {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

:deep(.search-form .el-form-item) {
  margin-right: 12px;
  margin-bottom: 14px;
}

.filter-select {
  width: 140px;
}

.house-card {
  margin-bottom: 20px;
  overflow: hidden;
  border: 1px solid rgba(5, 150, 105, 0.08);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 16px 40px rgba(6, 78, 59, 0.1);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.house-card:hover {
  transform: translateY(-4px);
  border-color: rgba(5, 150, 105, 0.2);
  box-shadow: 0 20px 50px rgba(6, 78, 59, 0.16);
}

.house-card :deep(.el-card__body) {
  padding: 0 18px 18px;
}

.house-image {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  cursor: pointer;
  background: #e5e7eb;
  border-radius: 18px 18px 0 0;
}

.house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.admin-status-stack {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}

.admin-status-badge {
  width: fit-content;
  padding: 0 10px;
  font-weight: 600;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.16);
}

.house-image:hover img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.44);
  color: #fff;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.house-image:hover .image-overlay {
  opacity: 1;
}

.house-info {
  display: grid;
  gap: 12px;
  padding-top: 16px;
}

.house-title {
  margin: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.house-address {
  min-height: 42px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.house-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.house-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.price {
  color: #dc2626;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.unit {
  color: #94a3b8;
  font-size: 14px;
}

.house-agent {
  color: #64748b;
  font-size: 13px;
}

.select-action {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.select-action .el-button {
  width: 100%;
  border-radius: 10px;
}

/* 5鍒楀竷灞€鐨勫搷搴斿紡鏂偣 */
.house-grid :deep(.el-col) {
  margin-bottom: 20px;
}

@media (min-width: 1600px) {
  /* 妗岄潰绔? 5鍒?*/
  .house-grid :deep(.el-col) {
    flex: 0 0 25%;
    max-width: 25%;
  }
}

@media (min-width: 1200px) and (max-width: 1599px) {
  /* 涓ぇ灞? 4鍒?*/
  .house-grid :deep(.el-col) {
    flex: 0 0 25%;
    max-width: 25%;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  /* 骞虫澘: 2鍒?*/
  .house-grid :deep(.el-col) {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media (max-width: 767px) {
  /* 鎵嬫満: 1鍒?*/
  .house-grid :deep(.el-col) {
    flex: 0 0 100%;
    max-width: 100%;
  }
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

/* 浼樺寲5鍒楀竷灞€涓嬬殑鍗＄墖鏍峰紡 */
.house-grid .house-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.house-grid .house-info {
  flex: 1;
}

.house-grid .house-image {
  height: 180px;
}

@media (min-width: 1600px) {
  .house-grid .house-image {
    height: 160px;
  }
}

@media (max-width: 1200px) {
  :deep(.el-row > .el-col) {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media (max-width: 768px) {
  :deep(.el-row > .el-col) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .pagination-wrap {
    padding: 14px 12px;
  }
}
</style>
