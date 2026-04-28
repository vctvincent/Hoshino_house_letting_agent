<template>
  <div class="home-container">
    <!-- ============ 顶部导航栏 ============ -->
    <header class="top-navbar">
      <div class="navbar-content">
        <div class="navbar-logo" @click="router.push(getRoutePath('/layout/dashboard'))">
          <span class="logo-icon">🏠</span>
          <span class="logo-text">房屋中介管理系统</span>
        </div>

        <div class="city-selector">
          <el-select
              v-model="selectedCity"
              placeholder="选择城市"
              size="large"
              :loading="cityLoading"
              @change="handleCityChange"
          >
            <el-option
                v-for="city in cityList"
                :key="city.value"
                :label="city.label"
                :value="city.value"
            />
          </el-select>
        </div>

        <div class="navbar-right">
          <template v-if="isAuthenticated">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
              <el-icon class="bell-icon" @click="router.push(getRoutePath('/layout/message'))">
                <Bell />
              </el-icon>
            </el-badge>

            <el-dropdown trigger="hover">
              <div class="user-nickname">
                <span>{{ displayName }}</span>
                <el-icon><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                      v-if="role === 'ROLE_ADMIN'"
                      @click="router.push(getRoutePath('/layout/admin'))"
                  >
                    <span class="dropdown-icon">⚙️</span>系统管理
                  </el-dropdown-item>
                  <el-dropdown-item
                      v-if="role === 'ROLE_AGENT'"
                      @click="router.push(getRoutePath('/layout/agent/workspace'))"
                  >
                    <span class="dropdown-icon">💼</span>我的工作台
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push(getRoutePath('/layout/profile'))">
                    <span class="dropdown-icon">👤</span>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <span class="dropdown-icon">🚪</span>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <el-button v-else type="primary" @click="router.push(getRoutePath('/login'))">
            点击登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- ============ 主内容区 ============ -->
    <main class="main-content">
      <!-- 搜索区域 -->
      <section class="search-section">
        <div class="search-wrapper">
          <h1 class="page-title">
            <span class="title-icon">🏠</span>
            星野房屋中介管理系统
          </h1>

          <div class="search-container">
            <div class="search-box">
              <div class="search-icon">🔍</div>
              <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="请输入小区名称、区域、地段等关键词"
                  class="search-input"
                  @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">
                <span>搜索</span>
                <span class="btn-icon">→</span>
              </button>
            </div>

            <!-- 区域筛选 -->
            <div v-if="selectedCity && districtList.length" class="district-filter">
              <span class="filter-label">📍 区域：</span>
              <el-tag
                  v-for="district in districtList"
                  :key="district.value"
                  :type="selectedDistrict === district.value ? '' : 'info'"
                  :effect="selectedDistrict === district.value ? 'dark' : 'plain'"
                  class="district-tag"
                  round
                  @click="selectDistrict(district.value)"
              >
                {{ district.label }}
              </el-tag>
              <el-tag
                  v-if="selectedDistrict"
                  type="info"
                  effect="plain"
                  class="district-tag clear-tag"
                  round
                  @click="clearDistrict"
              >
                全部区域 ✕
              </el-tag>
            </div>
          </div>
        </div>
      </section>

      <!-- 状态统计卡片 -->
      <section v-if="isAdmin" class="status-stats-bar">
        <div
            v-for="stat in statusStats"
            :key="stat.value"
            class="status-stat-card"
            :class="{ active: filterForm.houseStatus === stat.value }"
            @click="quickFilterByStatus(stat.value)"
        >
          <span class="stat-num" :style="{ color: stat.color }">{{ stat.count }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </section>

      <!-- 筛选区域 -->
      <section class="filter-section">
        <el-card class="filter-card" shadow="never">
          <!-- 交易状态筛选行 -->
          <div v-if="isAdmin" class="status-filter-row">
            <span class="status-filter-label">房源状态：</span>
            <el-radio-group
                v-model="filterForm.houseStatus"
                size="default"
                @change="handleStatusFilterChange"
            >
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button
                  v-for="status in houseStatusOptions"
                  :key="status.value"
                  :value="status.value"
              >
                {{ status.label }}
              </el-radio-button>
            </el-radio-group>
          </div>

          <!-- 通用筛选 -->
          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item label="💰 价格">
              <el-input v-model="filterForm.minPrice" placeholder="最低价" class="filter-input-short" />
              <span class="filter-separator">-</span>
              <el-input v-model="filterForm.maxPrice" placeholder="最高价" class="filter-input-short" />
              <span class="filter-unit">万</span>
            </el-form-item>

            <el-form-item label="🏠 户型">
              <el-select v-model="filterForm.houseType" placeholder="请选择" clearable class="filter-select">
                <el-option v-for="n in 5" :key="n" :label="n === 5 ? '5 室及以上' : `${n} 室`" :value="`${n}室`" />
              </el-select>
            </el-form-item>

            <el-form-item label="📐 面积">
              <el-input v-model="filterForm.minArea" placeholder="最小" class="filter-input-xs" />
              <span class="filter-separator">-</span>
              <el-input v-model="filterForm.maxArea" placeholder="最大" class="filter-input-xs" />
              <span class="filter-unit">㎡</span>
            </el-form-item>

            <el-form-item label="🏢 楼层">
              <el-select v-model="filterForm.floor" placeholder="请选择" clearable class="filter-select">
                <el-option label="低楼层" value="低" />
                <el-option label="中楼层" value="中" />
                <el-option label="高楼层" value="高" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleFilterSearch">🔍 搜索</el-button>
              <el-button @click="handleFilterReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </section>

      <!-- 房源列表 -->
      <section class="section">
        <div class="section-header">
          <h3>
            🏘️
            {{ currentStatusLabel ? currentStatusLabel + '房源' : '全部房源' }}
            <span class="sort-tip">（按热度排序）</span>
          </h3>
          <span class="result-count">共 {{ pagination.total }} 套房源</span>
        </div>

        <el-skeleton :loading="houseLoading" animated :count="8">
          <template #template>
            <el-row :gutter="20">
              <el-col v-for="n in 8" :key="n" :xs="24" :sm="12" :md="8" :lg="6">
                <div class="skeleton-card">
                  <el-skeleton-item variant="image" style="height: 220px" />
                  <div style="padding: 16px">
                    <el-skeleton-item variant="text" style="width: 80%" />
                    <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px" />
                    <el-skeleton-item variant="text" style="width: 40%; margin-top: 12px" />
                  </div>
                </div>
              </el-col>
            </el-row>
          </template>

          <template #default>
            <el-empty v-if="!houseList.length" description="暂无符合条件的房源" />
            <el-row v-else :gutter="20">
              <el-col
                  v-for="house in houseList"
                  :key="house.id"
                  :xs="24"
                  :sm="12"
                  :md="8"
                  :lg="6"
              >
                <el-card
                    class="house-card"
                    shadow="hover"
                    @click="router.push(getRoutePath(`/layout/house/detail/${house.id}`))"
                >
                  <div class="house-image-wrapper">
                    <img
                        :src="getHouseImage(house.images)"
                        :alt="house.title"
                        loading="lazy"
                        @error="handleImageError"
                    />
                    <div class="house-overlay">
                      <span class="view-count">👁 {{ house.viewCount || 0 }}</span>
                    </div>
                    <el-tag
                        v-if="house.houseStatus !== null && house.houseStatus !== undefined && house.houseStatus !== 1"
                        :type="getStatusTagType(house.houseStatus)"
                        size="small"
                        class="status-badge"
                        effect="dark"
                    >
                      {{ getStatusLabel(house.houseStatus) }}
                    </el-tag>
                  </div>
                  <div class="house-info">
                    <h3 class="house-title">{{ house.title }}</h3>
                    <p class="house-address">📍 {{ house.address }}</p>
                    <div class="house-tags">
                      <el-tag size="small" effect="plain">{{ house.houseType }}</el-tag>
                      <el-tag size="small" type="info" effect="plain">{{ house.area }}㎡</el-tag>
                      <el-tag v-if="house.floor" size="small" type="success" effect="plain">
                        {{ house.floor }}
                      </el-tag>
                    </div>
                    <div class="house-price-wrapper">
                      <div class="house-price">
                        <span class="price-symbol">￥</span>
                        <span class="price">{{ formatPrice(house.price) }}</span>
                        <span class="price-unit">万元</span>
                      </div>
                      <div v-if="house.unitPrice" class="house-unit-price">
                        {{ Math.round(house.unitPrice) }}元/㎡
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </template>
        </el-skeleton>

        <el-pagination
            v-if="pagination.total > 0"
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[8, 12, 16, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            class="pagination-container"
            @size-change="loadHouseList"
            @current-change="loadHouseList"
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api'

const router = useRouter()
const route = useRoute()
const store = useStore()

const role = computed(() => store.getters.role)
const userInfo = computed(() => store.getters.userInfo)
const isAuthenticated = computed(() => !!sessionStorage.getItem('token'))

const isAdmin = computed(() => role.value === 'ROLE_ADMIN')
const agentId = computed(() => userInfo.value?.userId || userInfo.value?.id)

const displayName = computed(() => {
  const info = userInfo.value
  if (!info) return ''
  const nameMap = {
    ROLE_ADMIN: info.name || info.realName || '管理员',
    ROLE_AGENT: info.name || info.agentName || info.phone || '中介',
    ROLE_CUSTOMER: info.nickname || info.customerNickname || info.phone || '用户',
  }
  return nameMap[role.value] || info.name || info.nickname || info.phone || ''
})

// 根据角色和路径生成正确的路由
const getRoutePath = (path) => {
  if (role.value === 'ROLE_ADMIN') {
    return path.replace('/layout/', '/admin-layout/')
  } else if (role.value === 'ROLE_AGENT') {
    return path.replace('/layout/', '/agent-layout/')
  }
  return path
}

const houseStatusOptions = [
  { value: 1, label: '已发布', color: '#67c23a' },
  { value: 2, label: '已成交', color: '#f56c6c' },
  { value: 3, label: '已下架', color: '#909399' },
]

const statusTagTypeMap = {
  0: 'info',
  1: 'success',
  2: 'danger',
  3: 'warning',
}

function normalizeStatus(status) {
  if (status === null || status === undefined) return NaN
  return Number(status)
}

function getStatusLabel(status) {
  const n = normalizeStatus(status)
  const found = houseStatusOptions.find((s) => s.value === n)
  if (found) return found.label
  if (n === 0) return '未发布'
  return '未知'
}

function getStatusTagType(status) {
  const n = normalizeStatus(status)
  return statusTagTypeMap[n] || 'info'
}

const statusStats = ref([
  { value: '', label: '全部房源', count: 0, color: '#303133' },
  { value: 1, label: '已发布', count: 0, color: '#67c23a' },
  { value: 2, label: '已成交', count: 0, color: '#f56c6c' },
  { value: 3, label: '已下架', count: 0, color: '#909399' },
])

const currentStatusLabel = computed(() => {
  if (filterForm.houseStatus === '') return ''
  return getStatusLabel(filterForm.houseStatus)
})

async function loadStatusStats() {
  for (const stat of statusStats.value) {
    try {
      const params = { pageSize: 1 }
      if (selectedCity.value) params.city = selectedCity.value
      if (selectedDistrict.value) params.district = selectedDistrict.value
      if (stat.value !== '') params.houseStatus = stat.value
      const res = await request.get('/house/list', { params })
      stat.count = res.data?.total || 0
    } catch { stat.count = 0 }
  }
}

function quickFilterByStatus(status) {
  filterForm.houseStatus = status
  pagination.pageNum = 1
  loadHouseList()
}

function handleStatusFilterChange() {
  pagination.pageNum = 1
  loadHouseList()
}

const searchKeyword = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')

const filterForm = reactive({
  houseStatus: '',
  minPrice: '',
  maxPrice: '',
  houseType: '',
  minArea: '',
  maxArea: '',
  floor: '',
})

function buildQueryParams() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (role.value === 'ROLE_ADMIN') {
    if (filterForm.houseStatus !== '') params.houseStatus = filterForm.houseStatus
  } else {
    params.houseStatus = 1
  }
  const fieldMap = {
    city: selectedCity.value, district: selectedDistrict.value, keyword: searchKeyword.value.trim(),
    minPrice: filterForm.minPrice, maxPrice: filterForm.maxPrice, houseType: filterForm.houseType,
    minArea: filterForm.minArea, maxArea: filterForm.maxArea, floor: filterForm.floor,
  }
  for (const [key, val] of Object.entries(fieldMap)) {
    if (val !== '' && val !== null && val !== undefined) params[key] = val
  }
  return params
}

const cityList = ref([])
const districtList = ref([])
const cityLoading = ref(false)

async function loadCityList() {
  cityLoading.value = true
  try {
    const res = await request.get('/house/cities')
    cityList.value = (res.data || []).map((city) => ({ label: city, value: city }))
  } catch (err) { console.error('加载城市列表失败:', err) }
  finally { cityLoading.value = false }
}

async function loadDistrictList(city) {
  try {
    const res = await request.get('/house/districts', { params: { city } })
    districtList.value = (res.data || []).map((d) => ({ label: d, value: d }))
  } catch (err) { console.error('加载区域列表失败:', err); districtList.value = [] }
}

function handleCityChange(city) {
  selectedDistrict.value = ''
  loadDistrictList(city)
  pagination.pageNum = 1
  loadHouseList()
  loadStatusStats()
  const cityName = cityList.value.find((c) => c.value === city)?.label
  ElMessage.success(`已切换到：${cityName}`)
}

function selectDistrict(district) {
  selectedDistrict.value = district
  pagination.pageNum = 1
  loadHouseList()
  loadStatusStats()
}

function clearDistrict() {
  selectedDistrict.value = ''
  pagination.pageNum = 1
  loadHouseList()
  loadStatusStats()
}

const houseList = ref([])
const houseLoading = ref(false)
const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

async function loadHouseList() {
  houseLoading.value = true
  try {
    const params = buildQueryParams()
    const res = await request.get('/house/list', { params })
    houseList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (err) { console.error('加载房源列表失败:', err) }
  finally { houseLoading.value = false }
}

function handleSearch() { pagination.pageNum = 1; loadHouseList() }
function handleFilterSearch() { pagination.pageNum = 1; loadHouseList() }
function handleFilterReset() {
  Object.assign(filterForm, { houseStatus: '', minPrice: '', maxPrice: '', houseType: '', minArea: '', maxArea: '', floor: '' })
  pagination.pageNum = 1
  loadHouseList()
}

function getHouseImage(imagesStr) {
  try {
    if (!imagesStr) return '/default-house.jpg'
    const images = JSON.parse(imagesStr)
    const first = images?.[0] || '/default-house.jpg'
    if (first && !first.startsWith('/') && !first.startsWith('http')) return '/' + first
    return first
  } catch { return '/default-house.jpg' }
}

function handleImageError(e) { e.target.src = '/default-house.jpg' }

function formatPrice(price) {
  if (!price || price === 0) return '面议'
  const num = parseFloat(price)
  return isNaN(num) ? '面议' : num.toFixed(2)
}

const unreadCount = ref(0)

async function loadUnreadCount() {
  try {
    const userId = userInfo.value?.userId || userInfo.value?.id
    const typeMap = { ROLE_ADMIN: 1, ROLE_AGENT: 2, ROLE_CUSTOMER: 3 }
    const userType = typeMap[role.value] || 0
    if (!userId || !userType) { unreadCount.value = 0; return }
    const res = await request.get('/message/unread/count', { params: { userId, userType } })
    if (res.code === 200) unreadCount.value = res.data?.count || 0
  } catch { unreadCount.value = 0 }
}

function handleLogout() { store.dispatch('clearAuth'); router.push('/login') }

watch(() => route.name, (newName, oldName) => {
  if (newName === 'Dashboard' && oldName && oldName !== 'Dashboard') loadUnreadCount()
})

onMounted(() => {
  loadHouseList()
  loadCityList()
  loadUnreadCount()
  if (isAdmin.value) loadStatusStats()
})
</script>

<style scoped>
/* ==================== CSS 变量 — 星野·深林翡翠 ==================== */
.home-container {
  --primary-start: #064e3b;
  --primary-end: #059669;
  --primary-gradient: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  --primary-dark: #064e3b;
  --primary-light: #a7f3d0;
  --primary-bg: #ecfdf5;
  --text-primary: #1f2937;
  --text-secondary: #374151;
  --text-muted: #6b7280;
  --border-color: #e5e7eb;
  --bg-page: #f0fdf4;
  --bg-white: #ffffff;
  --price-color: #059669;
  --shadow-card: 0 2px 12px rgba(6, 78, 59, 0.08);
  --shadow-hover: 0 12px 30px rgba(6, 78, 59, 0.18);
  --radius-lg: 12px;
  --radius-md: 8px;
  --transition-base: 0.3s ease;

  min-height: 100vh;
  background-color: var(--bg-page);
}

/* ==================== 顶部导航 ==================== */
.top-navbar {
  background: var(--primary-gradient);
  box-shadow: 0 2px 8px rgba(6, 78, 59, 0.2);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform var(--transition-base);
}

.navbar-logo:hover {
  transform: translateY(-2px);
}

.logo-icon {
  font-size: 26px;
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.city-selector {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 20px;
}

.city-selector :deep(.el-select) {
  width: 220px;
}

.city-selector :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15);
  box-shadow: none;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-md);
  padding: 0 15px;
  transition: all var(--transition-base);
}

.city-selector :deep(.el-select:hover .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.6);
}

.city-selector :deep(.el-input__inner) {
  color: white;
  font-weight: 600;
  font-size: 15px;
}

.city-selector :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.8);
}

.city-selector :deep(.el-select__caret) {
  color: white;
  font-size: 16px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.navbar-right :deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border-color: transparent;
  color: white;
  font-weight: 600;
  font-size: 15px;
  transition: all var(--transition-base);
}

.navbar-right :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.message-badge {
  cursor: pointer;
}

.bell-icon {
  font-size: 22px;
  color: white;
  transition: opacity var(--transition-base);
}

.bell-icon:hover {
  opacity: 0.8;
}

.user-nickname {
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.user-nickname:hover {
  background: rgba(255, 255, 255, 0.15);
}

.user-nickname:hover .el-icon {
  transform: rotate(180deg);
}

.user-nickname .el-icon {
  font-size: 14px;
  transition: transform var(--transition-base);
}

.dropdown-icon {
  margin-right: 8px;
}

/* ==================== 主内容 ==================== */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* ==================== 搜索区域 ==================== */
.search-section {
  background: var(--primary-gradient);
  padding: 80px 20px 60px;
  margin-bottom: 30px;
  position: relative;
  overflow: hidden;
}

.search-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 500px;
  height: 500px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  pointer-events: none;
}

.search-section::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -5%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
  pointer-events: none;
}

.search-wrapper {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 40px;
  font-weight: 900;
  margin-bottom: 40px;
  text-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  animation: fadeInDown 0.8s ease;
}

.title-icon {
  font-size: 48px;
  animation: bounce 2s infinite;
}

.search-container {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  border-radius: var(--radius-lg);
  padding: 25px;
  box-shadow: 0 20px 60px rgba(6, 78, 59, 0.25);
  animation: fadeInUp 0.8s ease 0.4s both;
}

.search-box {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.search-icon {
  width: 60px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  background: var(--primary-gradient);
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  height: 56px;
  border: 2px solid var(--border-color);
  border-left: none;
  border-right: none;
  outline: none;
  font-size: 16px;
  padding: 0 20px;
  transition: all var(--transition-base);
}

.search-input:focus {
  border-color: var(--primary-start);
  background: #f0fdf4;
}

.search-btn {
  width: 160px;
  height: 56px;
  background: var(--primary-gradient);
  border: none;
  color: white;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-base);
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(6, 78, 59, 0.4);
}

.search-btn:hover {
  transform: translateX(3px);
  box-shadow: 0 6px 20px rgba(6, 78, 59, 0.6);
}

.btn-icon {
  font-size: 20px;
  transition: transform var(--transition-base);
}

.search-btn:hover .btn-icon {
  transform: translateX(5px);
}

.district-filter {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  animation: fadeInUp 0.5s ease;
}

.filter-label {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 600;
  margin-right: 8px;
}

.district-tag {
  cursor: pointer;
  padding: 8px 18px;
  font-size: 15px;
  border-radius: 20px;
  transition: all var(--transition-base);
}

.district-tag:hover {
  transform: translateY(-2px);
}

/* ==================== 状态统计卡片 ==================== */
.status-stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding: 4px 0;
}

.status-stat-card {
  flex: 1;
  min-width: 120px;
  text-align: center;
  padding: 20px 16px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 2px solid transparent;
}

.status-stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(6, 78, 59, 0.12);
}

.status-stat-card.active {
  border-color: var(--primary-start);
  background: var(--primary-bg);
  box-shadow: 0 4px 16px rgba(6, 78, 59, 0.2);
}

.status-stat-card .stat-num {
  display: block;
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
}

.status-stat-card .stat-label {
  display: block;
  font-size: 15px;
  color: var(--text-muted);
  margin-top: 8px;
}

/* ==================== 筛选区域 ==================== */
.filter-section {
  margin-bottom: 20px;
}

.filter-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.status-filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.status-filter-label {
  font-size: 15px;
  color: var(--text-secondary);
  font-weight: 600;
  white-space: nowrap;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}

.filter-input-short { width: 100px; }
.filter-input-xs { width: 80px; }
.filter-select { width: 120px; }

.filter-separator {
  margin: 0 5px;
  color: var(--text-muted);
}

.filter-unit {
  margin-left: 5px;
  color: var(--text-muted);
}

/* ==================== 房源区块 ==================== */
.section {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e5e7eb;
}

.section-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.sort-tip {
  font-size: 15px;
  color: var(--text-muted);
  font-weight: normal;
}

.result-count {
  font-size: 15px;
  color: var(--text-secondary);
}

.skeleton-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 20px;
}

/* ==================== 房源卡片 ==================== */
.house-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all var(--transition-base);
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: none;
}

.house-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
}

.house-image-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: #f0fdf4;
}

.house-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.house-card:hover .house-image-wrapper img {
  transform: scale(1.1);
}

.house-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 10px;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.house-card:hover .house-overlay {
  opacity: 1;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  font-weight: bold;
}

.view-count {
  color: white;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.6);
  padding: 4px 10px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.house-info {
  padding: 16px;
}

.house-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.house-address {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.house-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.house-price-wrapper {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.house-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  font-size: 15px;
  color:  #f56c6c;
  font-weight: 600;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color:  #f56c6c;
  line-height: 1;
}

.price-unit {
  font-size: 14px;
  color: var(--text-muted);
}

.house-unit-price {
  font-size: 14px;
  color: var(--text-muted);
  white-space: nowrap;
}

/* ==================== 分页 ==================== */
.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* ==================== 动画 ==================== */
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .page-title { font-size: 28px; flex-direction: column; gap: 8px; }
  .title-icon { font-size: 40px; }
  .search-section { padding: 40px 15px 30px; }
  .search-container { padding: 15px; }
  .search-box { flex-direction: column; }
  .search-icon { width: 100%; height: 40px; border-radius: var(--radius-md) var(--radius-md) 0 0; }
  .search-input { width: 100%; height: 45px; border: 2px solid var(--border-color); border-radius: 0; }
  .search-btn { width: 100%; border-radius: 0 0 var(--radius-md) var(--radius-md); }
  .navbar-content { padding: 0 10px; }
  .logo-text { font-size: 16px; }
  .filter-form { flex-direction: column; align-items: stretch; }
  .status-stats-bar { gap: 8px; }
  .status-stat-card { min-width: 80px; padding: 12px 8px; }
  .status-stat-card .stat-num { font-size: 24px; }
  .status-filter-row { flex-direction: column; align-items: flex-start; }
}
</style>
