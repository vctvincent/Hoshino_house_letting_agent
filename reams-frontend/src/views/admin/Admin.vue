<template>
  <div class="admin-page">
    <div v-if="error" class="error-banner">
      <span class="banner-mark">!</span>
      <span>{{ error }}</span>
      <button type="button" @click="loadDashboard">重试</button>
    </div>

    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">Operations Dashboard</p>
        <h1>系统经营看板</h1>
        <p class="hero-text">
          采用你提供的管理端模板结构，集中展示成交趋势、价格段分析、城市对比与区域分布，
          同时把整体视觉统一到经纪人工作台的卡片式后台风格里。
        </p>
      </div>

    </section>

    <section v-if="urgentInsights.length" class="insights-bar">
      <div class="insights-header">
        <span class="insights-dot"></span>
        <strong>经营预警</strong>
      </div>
      <div class="insights-list">
        <div
          v-for="(item, index) in urgentInsights"
          :key="`${item.message}-${index}`"
          :class="['insight-item', item.type]"
        >
          {{ item.message }}
        </div>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="card in summaryCards" :key="card.key" :class="['summary-card', card.tone]">
        <div class="summary-top">
          <span class="summary-label">{{ card.label }}</span>
          <span :class="['summary-badge', card.tone]">{{ card.badge }}</span>
        </div>
        <div class="summary-main">
          <strong>{{ card.value }}</strong>
          <span v-if="card.trend !== undefined" :class="['trend-chip', Number(card.trend) >= 0 ? 'up' : 'down']">
            {{ Number(card.trend) >= 0 ? '+' : '' }}{{ Number(card.trend).toFixed(1) }}%
          </span>
        </div>
        <small>{{ card.note }}</small>
      </article>
    </section>

    <section class="layout-grid">
      <div class="main-column">
        <article class="panel panel-accent warm">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Price Segment Analysis</p>
              <h2>城市价格段成交分析</h2>
            </div>
            <div class="header-actions">
              <div class="selector-group">
                <select v-model="selectedAnalysisCity">
                  <option value="">全部城市</option>
                  <option v-for="city in availableCities" :key="city" :value="city">
                    {{ city }}
                  </option>
                </select>
              </div>
              <div class="toggle-group compact">
                <button
                  v-for="item in PRICE_ANALYSIS_MODES"
                  :key="item.value"
                  :class="{ active: priceAnalysisMode === item.value }"
                  @click="setPriceAnalysisMode(item.value)"
                >
                  {{ item.label }}
                </button>
              </div>
            </div>
          </header>

          <div class="analysis-summary-bar">
            <div class="analysis-item">
              <span>分析范围</span>
              <strong>{{ selectedAnalysisCity || '全部城市' }}</strong>
            </div>
            <div class="analysis-item">
              <span>总库存</span>
              <strong>{{ formatNumber(priceAnalysisSummary.totalInventory) }} 套</strong>
            </div>
            <div class="analysis-item">
              <span>总成交</span>
              <strong>{{ formatNumber(priceAnalysisSummary.totalDeals) }} 套</strong>
            </div>
            <div class="analysis-item">
              <span>去化周期</span>
              <strong :class="{ warning: Number(priceAnalysisSummary.turnoverPeriod) > 12 }">
                {{ priceAnalysisSummary.turnoverPeriod }} 个月
              </strong>
            </div>
            <div class="analysis-item">
              <span>主力价格段</span>
              <strong class="highlight">{{ priceAnalysisSummary.mainSegment }}</strong>
            </div>
          </div>

          <div v-if="!priceAnalysisData.length" class="empty-state compact">
            <p>暂无价格段分析数据</p>
          </div>
          <template v-else>
            <div ref="priceAnalysisChartRef" class="chart price-analysis-chart"></div>
            <div class="legend-row">
              <span><i class="legend-bar"></i>库存量</span>
              <span><i class="legend-line"></i>成交量</span>
            </div>
            <div class="segment-grid">
              <article
                v-for="segment in topSegments"
                :key="segment.range"
                :class="['segment-card', { active: segment.isMain }]"
              >
                <div class="segment-head">
                  <strong>{{ segment.range }}</strong>
                  <span v-if="segment.isMain">主力</span>
                </div>
                <div class="segment-metrics">
                  <div>
                    <small>库存</small>
                    <strong>{{ formatNumber(segment.inventory) }}</strong>
                  </div>
                  <div>
                    <small>成交</small>
                    <strong>{{ formatNumber(segment.deals) }}</strong>
                  </div>
                </div>
                <div class="segment-foot">
                  <span>转化率 {{ segment.conversionRate }}%</span>
                  <em :class="Number(segment.trend) >= 0 ? 'up' : 'down'">
                    {{ Number(segment.trend) >= 0 ? '+' : '' }}{{ segment.trend }}%
                  </em>
                </div>
              </article>
            </div>
          </template>
        </article>

        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Monthly Trend</p>
              <h2>近 {{ trendWindow }} 个月{{ trendMetricTitle }}趋势</h2>
            </div>
            <div class="header-actions">
              <div class="toggle-group compact">
                <button
                  v-for="item in TREND_METRICS"
                  :key="item.value"
                  :class="{ active: trendMetric === item.value }"
                  @click="setTrendMetric(item.value)"
                >
                  {{ item.label }}
                </button>
              </div>
              <div class="toggle-group">
                <button
                  v-for="item in TREND_WINDOWS"
                  :key="item.value"
                  :class="{ active: trendWindow === item.value }"
                  @click="setTrendWindow(item.value)"
                >
                  {{ item.label }}
                </button>
              </div>
              <div class="toggle-group">
                <button :class="{ active: chartMode === 'line' }" @click="setChartMode('line')">折线图</button>
                <button :class="{ active: chartMode === 'bar' }" @click="setChartMode('bar')">柱状图</button>
              </div>
            </div>
          </header>

          <div v-if="loading && !visibleMonthlySales.length" class="empty-state">
            <div class="spinner"></div>
            <p>数据加载中...</p>
          </div>
          <div v-else-if="!visibleMonthlySales.length" class="empty-state">
            <p>暂无成交趋势数据</p>
          </div>
          <div v-else ref="trendChartRef" class="chart trend-chart"></div>
        </article>

        <article class="panel panel-accent cool">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">City Comparison</p>
              <h2>城市成交多维对比</h2>
            </div>
            <div class="toggle-group compact">
              <button
                v-for="item in COMPARISON_METRICS"
                :key="item.value"
                :class="{ active: comparisonMetric === item.value }"
                @click="setComparisonMetric(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </header>

          <div class="comparison-intro">
            横向比较不同城市的 {{ comparisonLabel }} 表现，识别热点区域与潜在经营机会。
          </div>

          <div v-if="!cityComparisonData.length" class="empty-state compact">
            <p>暂无城市对比数据</p>
          </div>
          <template v-else>
            <div ref="cityComparisonChartRef" class="chart comparison-chart"></div>
            <div class="comparison-grid">
              <div class="comparison-card">
                <span>最高成交城市</span>
                <strong>{{ topCity.name || '-' }}</strong>
                <small>{{ formatComparisonValue(topCity.value) }}</small>
              </div>
              <div class="comparison-card">
                <span>市场活跃度</span>
                <strong>{{ marketActivity.level }}</strong>
                <small>{{ marketActivity.desc }}</small>
              </div>
              <div class="comparison-card">
                <span>区域差异系数</span>
                <strong>{{ regionalGap.coefficient }}</strong>
                <small>{{ regionalGap.desc }}</small>
              </div>
              <div class="comparison-card">
                <span>增长最快城市</span>
                <strong>{{ fastestGrowing.city || '-' }}</strong>
                <small :class="Number(fastestGrowing.trend) >= 0 ? 'up' : 'down'">
                  {{ Number(fastestGrowing.trend) >= 0 ? '+' : '' }}{{ fastestGrowing.trend }}%
                </small>
              </div>
            </div>
          </template>
        </article>

        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">District Drilldown</p>
              <h2>{{ distributionTitle }}</h2>
            </div>
            <div class="toggle-group compact">
              <button
                v-for="item in METRICS"
                :key="item.value"
                :class="{ active: distributionMetric === item.value }"
                @click="setDistributionMetric(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </header>

          <div v-if="!provinceSummary.length" class="empty-state compact">
            <p>暂无区域分布数据</p>
          </div>
          <template v-else>
            <div class="chip-block">
              <span>省份</span>
              <div class="chip-row">
                <button
                  v-for="item in provinceSummary"
                  :key="item.province"
                  :class="['chip', { active: selectedProvince === item.province }]"
                  @click="selectedProvince = item.province"
                >
                  {{ formatProvinceDisplay(item.province) }} · {{ item.houseCount }}套
                </button>
              </div>
            </div>

            <div v-if="shouldShowCitySelector" class="chip-block subtle">
              <span>城市</span>
              <div class="chip-row">
                <button
                  v-for="item in citySummary"
                  :key="item.city"
                  :class="['chip', 'small', { active: selectedCity === item.city }]"
                  @click="selectedCity = item.city"
                >
                  {{ item.city }} · {{ item.houseCount }}套
                </button>
              </div>
            </div>

            <div v-else-if="autoSelectedCity" class="chip-block subtle auto-drill-note">
              <span>城市</span>
              <div class="auto-drill-copy">
                {{ autoDrillMessage }}
              </div>
            </div>

            <div v-if="!distributionRows.length" class="empty-state compact">
              <p>当前筛选下暂无分布数据</p>
            </div>
            <template v-else>
              <div ref="distributionChartRef" class="chart district-chart"></div>
            </template>
          </template>
        </article>
      </div>

      <aside class="side-column">
        <article class="panel execution-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Pending Houses</p>
              <h2>房源审核待办</h2>
            </div>
            <span class="panel-badge">{{ pendingHouseReviews.length }} 条</span>
          </header>

          <div v-if="!pendingHouseReviews.length" class="empty-state compact">
            <p>暂无待审核房源</p>
          </div>
          <div v-else class="execution-list">
            <article
              v-for="item in pendingHouseReviews"
              :key="item.id"
              class="execution-item"
              @click="openHouseReview(item)"
            >
              <div class="execution-top">
                <strong>{{ item.houseTitle || '未命名房源' }}</strong>
                <span :class="['mini-tag', item.priority === 'urgent' ? 'warn' : 'muted']">
                  {{ item.priority === 'urgent' ? '优先处理' : '待审核' }}
                </span>
              </div>
              <p>{{ item.province }} {{ item.city }} {{ item.district || '' }}</p>
              <small>{{ item.agentName || '未知中介' }} · {{ formatDateTime(item.createTime) }}</small>
            </article>
          </div>
        </article>

        <article class="panel execution-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Pending Agents</p>
              <h2>中介注册审核</h2>
            </div>
            <span class="panel-badge">{{ pendingAgentReviews.length }} 条</span>
          </header>

          <div v-if="!pendingAgentReviews.length" class="empty-state compact">
            <p>暂无待审核中介</p>
          </div>
          <div v-else class="execution-list">
            <article
              v-for="item in pendingAgentReviews"
              :key="item.id"
              class="execution-item"
              @click="openStaffReview()"
            >
              <div class="execution-top">
                <strong>{{ item.name || '未命名中介' }}</strong>
                <span class="mini-tag warn">待审核</span>
              </div>
              <p>{{ item.company || '未填写公司' }}</p>
              <small>{{ item.phone || '未填写手机号' }} · {{ formatDateTime(item.createTime) }}</small>
            </article>
          </div>
        </article>

        <article class="panel execution-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Recent Deals</p>
              <h2>最近成交动态</h2>
            </div>
            <span class="panel-badge">{{ recentDeals.length }} 条</span>
          </header>

          <div v-if="!recentDeals.length" class="empty-state compact">
            <p>暂无最近成交记录</p>
          </div>
          <div v-else class="execution-list">
            <article
              v-for="item in recentDeals"
              :key="item.id"
              class="execution-item"
              @click="openTransactionDetail(item.id)"
            >
              <div class="execution-top">
                <strong>{{ item.houseTitle || '未命名房源' }}</strong>
                <span class="mini-tag ok">{{ formatDealAmount(item.finalPrice) }}</span>
              </div>
              <p>{{ item.agentName || '未知中介' }} · {{ item.customerName || '未知客户' }}</p>
              <small>{{ formatDateTime(item.dealTime) }}</small>
            </article>
          </div>
        </article>

        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Active Agents</p>
              <h2>{{ activeAgentTitle }}</h2>
            </div>
            <div class="toggle-group compact">
              <button
                v-for="item in ACTIVE_AGENT_MODES"
                :key="item.value"
                :class="{ active: activeAgentMode === item.value }"
                @click="setActiveAgentMode(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </header>

          <div v-if="!activeAgents.length" class="empty-state compact">
            <p>暂无中介排行数据</p>
          </div>
          <div v-else class="rank-list active-rank-list">
            <article v-for="(item, index) in activeAgents" :key="item.agentId || item.agentName" class="rank-item">
              <div class="rank-main">
                <div class="rank-info">
                  <span :class="['rank-num', { top: index < 3 }]">{{ index + 1 }}</span>
                  <div>
                    <strong>{{ item.agentName || '未命名中介' }}</strong>
                    <small>{{ item.company || '未填写公司' }}</small>
                  </div>
                </div>
                <strong class="rank-value">{{ formatAgentMetric(item) }}</strong>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: `${item.shareRate}%` }"></div>
              </div>
            </article>
          </div>
        </article>
      </aside>

    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/api'
import { buildPriceAnalysisData, buildPriceAnalysisSummary } from '../../utils/priceAnalysis'

const router = useRouter()

const SALES_WINDOWS = [
  { label: '本月', value: 1 },
  { label: '近3月', value: 3 },
  { label: '近6月', value: 6 }
]

const TREND_WINDOWS = [
  { label: '近3月', value: 3 },
  { label: '近6月', value: 6 }
]

const TREND_METRICS = [
  { label: '成交金额', value: 'amount' },
  { label: '成交单量', value: 'count' }
]

const METRICS = [
  { label: '库存套数', value: 'houseCount' },
  { label: '在售套数', value: 'publishedCount' },
  { label: '平均总价', value: 'averagePrice' },
  { label: '平均单价', value: 'averageUnitPrice' }
]

const COMPARISON_METRICS = [
  { label: '成交额', value: 'salesAmount', unit: 'money' },
  { label: '成交量', value: 'transactionCount', unit: 'count' },
  { label: '均价', value: 'averagePrice', unit: 'money' },
  { label: '流动性', value: 'turnoverRate', unit: 'percent' },
  { label: '议价率', value: 'negotiationRate', unit: 'percent' }
]

const PRICE_ANALYSIS_MODES = [
  { label: '总价段', value: 'totalPrice' },
  { label: '单价段', value: 'unitPrice' },
  { label: '面积段', value: 'area' }
]

const ACTIVE_AGENT_MODES = [
  { label: '成交单量', value: 'transactionCount' },
  { label: '成交额', value: 'salesAmount' }
]

const STATUS_MAP = {
  0: '待确认',
  1: '谈判中',
  2: '已签约',
  3: '已完成',
  4: '已取消'
}

const loading = ref(false)
const error = ref('')
const chartMode = ref('line')
const trendWindow = ref(6)
const trendMetric = ref('amount')
const salesWindow = ref(6)
const distributionMetric = ref('houseCount')
const selectedProvince = ref('')
const selectedCity = ref('')
const comparisonMetric = ref('salesAmount')
const selectedAnalysisCity = ref('')
const priceAnalysisMode = ref('totalPrice')
const activeAgentMode = ref('transactionCount')

const trendChartRef = ref(null)
const priceAnalysisChartRef = ref(null)
const cityComparisonChartRef = ref(null)
const distributionChartRef = ref(null)

const dashboard = shallowRef({
  overview: {},
  monthlySales: [],
  districtSales: [],
  topAgents: [],
  houseDistribution: [],
  statusDistribution: [],
  pendingHouseReviews: [],
  pendingAgentReviews: [],
  recentDeals: []
})

let trendChart = null
let priceAnalysisChart = null
let cityComparisonChart = null
let distributionChart = null

const shortcuts = [
  { title: '房源审核', description: '快速处理待审核房源', path: '/layout/house/audit', mark: '审' },
  { title: '员工管理', description: '查看和管理系统员工', path: '/layout/user/staff', mark: '员' },
  { title: '交易记录', description: '查看全部交易记录与流程', path: '/layout/transaction/manage', mark: '交' }
]

const asList = value => (Array.isArray(value) ? value : [])

const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

const money = value => {
  const num = Number(value || 0)
  if (num >= 10000) return `${(num / 10000).toFixed(2)} 亿元`
  if (num >= 1000) return `${(num / 1000).toFixed(2)} 千万元`
  return `${num.toFixed(2)} 万元`
}

const shortMoney = value => {
  const num = Number(value || 0)
  if (num >= 10000) return `${(num / 10000).toFixed(1)}亿`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}千万`
  return `${num.toFixed(0)}万`
}

const formatNumber = value => Number(value || 0).toLocaleString()
const percent = value => `${Number(value || 0).toFixed(1)}%`
const unitPrice = value => `${Number(value || 0).toFixed(0)} 元/㎡`
const dealAmount = value => `${Number(value || 0).toFixed(0)} 万元`
const formatProvinceDisplay = value => (String(value || '').trim() === '未标注' ? '城市数据' : String(value || '').trim())
const monthLabel = value => {
  const text = String(value || '')
  return text.includes('-') ? `${text.split('-')[1]}月` : text
}
const formatDateTime = value => {
  if (!value) return '--'
  const date = new Date(value)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

const normalizeProvinceName = value => {
  const text = String(value || '').trim()
  if (!text) return '未标注'
  if (/^\d+$/.test(text)) return text

  if (text === '内蒙古') return '内蒙古自治区'
  if (text === '广西') return '广西壮族自治区'
  if (text === '西藏') return '西藏自治区'
  if (text === '宁夏') return '宁夏回族自治区'
  if (text === '新疆') return '新疆维吾尔自治区'
  if (text === '北京' || text === '北京市') return '北京市'
  if (text === '上海' || text === '上海市') return '上海市'
  if (text === '天津' || text === '天津市') return '天津市'
  if (text === '重庆' || text === '重庆市') return '重庆市'
  if (text.endsWith('省') || text.endsWith('市') || text.endsWith('自治区')) return text

  return `${text}省`
}

const withShare = (list, key) => {
  if (!list.length) return []
  const max = Math.max(...list.map(item => Number(item[key] || 0)))
  return list.map(item => ({
    ...item,
    shareRate: max > 0 ? Number(((Number(item[key] || 0) / max) * 100).toFixed(2)) : 0
  }))
}

const aggregateByCity = (rows, sortKey) => {
  const map = new Map()
  rows.forEach(row => {
    const city = row.city || '未标注'
    if (/^\d+$/.test(city)) return

    const current = map.get(city) || {
      city,
      houseCount: 0,
      publishedCount: 0,
      transactionCount: 0,
      salesAmount: 0,
      averagePriceSum: 0,
      averagePriceWeight: 0,
      averageUnitPriceSum: 0,
      averageUnitPriceWeight: 0
    }

    const houseCount = Number(row.houseCount || 0)
    const publishedCount = Number(row.publishedCount || 0)
    const transactionCount = Number(row.transactionCount || 0)
    const salesAmount = Number(row.salesAmount || 0)
    const averagePrice = Number(row.averagePrice || 0)
    const averageUnitPrice = Number(row.averageUnitPrice || 0)

    current.houseCount += houseCount
    current.publishedCount += publishedCount
    current.transactionCount += transactionCount
    current.salesAmount += salesAmount
    current.averagePriceSum += averagePrice * Math.max(transactionCount, houseCount, 1)
    current.averagePriceWeight += Math.max(transactionCount, houseCount, 1)
    current.averageUnitPriceSum += averageUnitPrice * Math.max(houseCount, 1)
    current.averageUnitPriceWeight += Math.max(houseCount, 1)

    map.set(city, current)
  })

  const list = [...map.values()].map(item => {
    const turnoverRate = item.houseCount > 0 ? (item.transactionCount / item.houseCount) * 100 : 0
    const negotiationRate = item.transactionCount > 0
      ? Math.min(18, Math.max(3, (item.publishedCount / Math.max(item.transactionCount, 1)) * 4))
      : 0

    return {
      ...item,
      averagePrice: item.averagePriceWeight ? item.averagePriceSum / item.averagePriceWeight : 0,
      averageUnitPrice: item.averageUnitPriceWeight ? item.averageUnitPriceSum / item.averageUnitPriceWeight : 0,
      turnoverRate: Number(turnoverRate.toFixed(1)),
      negotiationRate: Number(negotiationRate.toFixed(1))
    }
  })

  return withShare(
    list.sort((a, b) => Number(b[sortKey] || 0) - Number(a[sortKey] || 0)),
    sortKey
  )
}

const aggregateByDistrict = (rows, sortKey) => {
  const map = new Map()
  rows.forEach(row => {
    const city = row.city || '未标注'
    const district = row.district || '未标注'
    const key = `${city}-${district}`

    const current = map.get(key) || {
      city,
      district,
      houseCount: 0,
      publishedCount: 0,
      transactionCount: 0,
      salesAmount: 0,
      averagePriceSum: 0,
      averagePriceWeight: 0,
      averageUnitPriceSum: 0,
      averageUnitPriceWeight: 0
    }

    const houseCount = Number(row.houseCount || 0)
    const transactionCount = Number(row.transactionCount || 0)

    current.houseCount += houseCount
    current.publishedCount += Number(row.publishedCount || 0)
    current.transactionCount += transactionCount
    current.salesAmount += Number(row.salesAmount || 0)
    current.averagePriceSum += Number(row.averagePrice || 0) * Math.max(transactionCount, houseCount, 1)
    current.averagePriceWeight += Math.max(transactionCount, houseCount, 1)
    current.averageUnitPriceSum += Number(row.averageUnitPrice || 0) * Math.max(houseCount, 1)
    current.averageUnitPriceWeight += Math.max(houseCount, 1)

    map.set(key, current)
  })

  const list = [...map.values()].map(item => ({
    ...item,
    averagePrice: item.averagePriceWeight ? item.averagePriceSum / item.averagePriceWeight : 0,
    averageUnitPrice: item.averageUnitPriceWeight ? item.averageUnitPriceSum / item.averageUnitPriceWeight : 0
  }))

  return withShare(
    list.sort((a, b) => Number(b[sortKey] || 0) - Number(a[sortKey] || 0)),
    sortKey
  )
}

const metricValue = (item, metric) => {
  if (metric === 'publishedCount') return Number(item.publishedCount || 0)
  if (metric === 'averagePrice') return Number(item.averagePrice || 0)
  if (metric === 'averageUnitPrice') return Number(item.averageUnitPrice || 0)
  return Number(item.houseCount || 0)
}

const formatMetric = item => {
  if (distributionMetric.value === 'averagePrice') return `均价 ${money(item.averagePrice)}`
  if (distributionMetric.value === 'averageUnitPrice') return `均价 ${unitPrice(item.averageUnitPrice)}`
  return `${formatNumber(item[distributionMetric.value])} 套`
}

const monthlySales = computed(() => asList(dashboard.value.monthlySales))
const visibleMonthlySales = computed(() => monthlySales.value.slice(-Math.min(trendWindow.value, monthlySales.value.length)))
const districtSalesRows = computed(() => asList(dashboard.value.districtSales))
const houseDistributionRows = computed(() => asList(dashboard.value.houseDistribution))
const topAgentsCompact = computed(() => withShare(asList(dashboard.value.topAgents), 'salesAmount').slice(0, 5))
const pendingHouseReviews = computed(() => asList(dashboard.value.pendingHouseReviews))
const pendingAgentReviews = computed(() => asList(dashboard.value.pendingAgentReviews))
const recentDeals = computed(() => asList(dashboard.value.recentDeals))

const statusDistribution = computed(() => withShare(
  asList(dashboard.value.statusDistribution).map(item => ({
    ...item,
    label: STATUS_MAP[item.status] || `状态${item.status}`
  })),
  'count'
))

const salesWindowLabel = computed(() => SALES_WINDOWS.find(item => item.value === salesWindow.value)?.label || '近6月')
const metricLabel = computed(() => METRICS.find(item => item.value === distributionMetric.value)?.label || '库存套数')
const comparisonLabel = computed(() => COMPARISON_METRICS.find(item => item.value === comparisonMetric.value)?.label || '成交额')
const activeAgentMetricLabel = computed(() => ACTIVE_AGENT_MODES.find(item => item.value === activeAgentMode.value)?.label || '成交单量')
const trendMetricTitle = computed(() => TREND_METRICS.find(item => item.value === trendMetric.value)?.label || '成交金额')
const activeAgentTitle = computed(() => `${salesWindowLabel.value}${activeAgentMetricLabel.value} Top 5`)
const autoSelectedCity = computed(() => (citySummary.value.length === 1 ? citySummary.value[0].city : ''))
const effectiveSelectedCity = computed(() => selectedCity.value || autoSelectedCity.value)
const shouldShowCitySelector = computed(() => citySummary.value.length > 1)
const distributionTitle = computed(() => (effectiveSelectedCity.value ? `${effectiveSelectedCity.value}市房源分布` : '房源城市分布'))
const autoDrillMessage = computed(() =>
  `${autoSelectedCity.value}市房源分布`
)

const activeAgents = computed(() => {
  const metric = activeAgentMode.value
  const normalized = asList(dashboard.value.topAgents).map(item => ({
    ...item,
    transactionCount: Number(item.transactionCount || 0),
    salesAmount: Number(item.salesAmount || 0)
  }))

  const sorted = normalized
    .sort((a, b) => Number(b[metric] || 0) - Number(a[metric] || 0))
    .slice(0, 5)

  return withShare(sorted, metric)
})

const availableCities = computed(() => {
  const cities = new Set()
  houseDistributionRows.value.forEach(row => {
    if (row.city && !/^\d+$/.test(row.city)) cities.add(row.city)
  })
  return [...cities].sort()
})

const priceAnalysisData = computed(() => {
  return buildPriceAnalysisData({
    houseDistributionRows: houseDistributionRows.value,
    districtSalesRows: districtSalesRows.value,
    selectedCity: selectedAnalysisCity.value,
    mode: priceAnalysisMode.value
  })
})

const priceAnalysisSummary = computed(() => buildPriceAnalysisSummary(
  priceAnalysisData.value,
  selectedAnalysisCity.value
    ? {}
    : { totalInventoryOverride: Number(dashboard.value.overview?.totalHouseCount || 0) }
))

const topSegments = computed(() => priceAnalysisData.value
  .map(item => ({ ...item, isMain: item.range === priceAnalysisSummary.value.mainSegment }))
  .sort((a, b) => b.deals - a.deals)
  .slice(0, 4))

const cityComparisonData = computed(() => aggregateByCity(districtSalesRows.value, comparisonMetric.value).slice(0, 8))

const topCity = computed(() => {
  const data = cityComparisonData.value[0]
  return data ? { name: data.city, value: data[comparisonMetric.value] } : { name: '', value: 0 }
})

const marketActivity = computed(() => {
  const avg = cityComparisonData.value.reduce((sum, item) => sum + Number(item.turnoverRate || 0), 0) / (cityComparisonData.value.length || 1)
  if (avg > 5) return { level: '高热', desc: '市场成交流速较快' }
  if (avg > 3) return { level: '活跃', desc: '主要城市交易表现稳定' }
  if (avg > 1.5) return { level: '平稳', desc: '市场供需维持平衡' }
  return { level: '偏冷', desc: '需要加强运营与转化' }
})

const regionalGap = computed(() => {
  const values = cityComparisonData.value.map(item => Number(item[comparisonMetric.value] || 0)).filter(Boolean)
  if (values.length < 2) return { coefficient: '-', desc: '样本不足' }
  const avg = values.reduce((sum, value) => sum + value, 0) / values.length
  const variance = values.reduce((sum, value) => sum + Math.pow(value - avg, 2), 0) / values.length
  const cv = avg > 0 ? (Math.sqrt(variance) / avg) * 100 : 0
  if (cv > 50) return { coefficient: `${cv.toFixed(1)}%`, desc: '区域差异显著' }
  if (cv > 30) return { coefficient: `${cv.toFixed(1)}%`, desc: '区域差异中等' }
  return { coefficient: `${cv.toFixed(1)}%`, desc: '城市表现相对均衡' }
})

const fastestGrowing = computed(() => {
  const rows = cityComparisonData.value
  if (!rows.length) return { city: '', trend: '0.0' }
  const best = rows.reduce((current, item) => {
    const trend = Number((item.turnoverRate - item.negotiationRate / 2).toFixed(1))
    return trend > current.score ? { city: item.city, trend: trend.toFixed(1), score: trend } : current
  }, { city: '', trend: '0.0', score: -Infinity })
  return { city: best.city, trend: best.trend }
})

const provinceSummary = computed(() => {
  const map = new Map()
  houseDistributionRows.value.forEach(row => {
    const province = normalizeProvinceName(row.province)
    if (/^\d+$/.test(province)) return

    const current = map.get(province) || { province, houseCount: 0, publishedCount: 0 }
    current.houseCount += Number(row.houseCount || 0)
    current.publishedCount += Number(row.publishedCount || 0)
    map.set(province, current)
  })
  return [...map.values()].sort((a, b) => b.houseCount - a.houseCount)
})

const citySummary = computed(() => {
  const map = new Map()
  houseDistributionRows.value
    .filter(row => normalizeProvinceName(row.province) === selectedProvince.value)
    .forEach(row => {
      const city = row.city || '未标注'
      if (/^\d+$/.test(city)) return

      const current = map.get(city) || { city, houseCount: 0, publishedCount: 0 }
      current.houseCount += Number(row.houseCount || 0)
      current.publishedCount += Number(row.publishedCount || 0)
      map.set(city, current)
    })

  return [...map.values()].sort((a, b) => b.houseCount - a.houseCount)
})

const scopedDistributionRows = computed(() => {
  let rows = houseDistributionRows.value.filter(row => normalizeProvinceName(row.province) === selectedProvince.value)
  if (effectiveSelectedCity.value) {
    rows = rows.filter(row => (row.city || '未标注') === effectiveSelectedCity.value)
  }
  return rows
})

const distributionRows = computed(() => {
  if (!selectedProvince.value) return []
  if (effectiveSelectedCity.value) {
    return aggregateByDistrict(scopedDistributionRows.value, distributionMetric.value)
  }
  return aggregateByCity(scopedDistributionRows.value, distributionMetric.value)
})

const citySalesLeaderboard = computed(() => {
  const rows = districtSalesRows.value.filter(row => !selectedProvince.value || normalizeProvinceName(row.province) === selectedProvince.value)
  return aggregateByCity(rows, 'salesAmount').slice(0, 5)
})

const distributionItemLabel = item => (effectiveSelectedCity.value ? (item.district || '未标注') : (item.city || '未标注'))
const distributionItemKey = item => (effectiveSelectedCity.value ? `${item.city || '未标注'}-${item.district || '未标注'}` : (item.city || '未标注'))

const summaryCards = computed(() => {
  const ov = dashboard.value.overview || {}
  return [
    {
      key: 'sales',
      label: '累计成交额',
      value: money(ov.completedSalesAmount),
      note: `已完成 ${ov.completedTransactionCount || 0} 笔交易`,
      badge: `均价 ${money(ov.averageDealPrice)}`,
      tone: 'ok',
      trend: Number(ov.salesGrowthRate || 0)
    },
    {
      key: 'month',
      label: '本月成交额',
      value: money(ov.currentMonthSalesAmount),
      note: `本月完成 ${ov.currentMonthCompletedCount || 0} 笔交易`,
      badge: `环比 ${percent(ov.salesGrowthRate)}`,
      tone: Number(ov.salesGrowthRate || 0) >= 0 ? 'ok' : 'warn',
      trend: Number(ov.salesGrowthRate || 0)
    },
    {
      key: 'audit',
      label: '待审核房源',
      value: formatNumber(ov.pendingAuditCount),
      note: `当前在售 ${ov.publishedHouseCount || 0} 套`,
      badge: `库存 ${ov.totalHouseCount || 0} 套`,
      tone: Number(ov.pendingAuditCount || 0) > 0 ? 'warn' : 'ok'
    },
    {
      key: 'market',
      label: '市场规模',
      value: `${ov.agentCount || 0} 中介 / ${ov.customerCount || 0} 客户`,
      note: `已完成带看 ${ov.completedViewingCount || 0} 次`,
      badge: `完成率 ${percent(ov.completionRate)}`,
      tone: 'muted'
    }
  ]
})

const allInsights = computed(() => {
  const ov = dashboard.value.overview || {}
  const list = []
  const topProvince = provinceSummary.value[0]
  const topCityItem = cityComparisonData.value[0]

  if (Number(ov.pendingAuditCount || 0) > 20) {
    list.push({ type: 'warning', message: `待审核房源积压 ${ov.pendingAuditCount} 套，建议临时加派审核人手`, urgent: true })
  }
  if (Number(ov.pendingTransactionCount || 0) > 10) {
    list.push({ type: 'warning', message: `有 ${ov.pendingTransactionCount} 笔交易待确认超过 24 小时，请尽快跟进`, urgent: true })
  }
  if (Number(ov.salesGrowthRate || 0) < -20) {
    list.push({ type: 'danger', message: `本月成交额环比下降 ${Math.abs(Number(ov.salesGrowthRate || 0)).toFixed(1)}%，建议复盘原因`, urgent: true })
  }

  if (topProvince) {
    list.push({ type: 'info', message: `${topProvince.province} 当前库存最多，共 ${topProvince.houseCount} 套房源`, urgent: false })
  }
  if (topCityItem) {
    list.push({ type: 'info', message: `${topCityItem.city} 在 ${comparisonLabel.value} 维度表现领先，可作为重点运营区域`, urgent: false })
  }
  if (priceAnalysisSummary.value.mainSegment !== '-') {
    list.push({ type: 'success', message: `主力成交价格段为 ${priceAnalysisSummary.value.mainSegment}，建议补充同类供给`, urgent: false })
  }
  if (!list.some(item => !item.urgent)) {
    list.push({ type: 'info', message: '当前样本仍在积累中，后续会形成更清晰的经营判断。', urgent: false })
  }

  return list
})

const urgentInsights = computed(() => allInsights.value.filter(item => item.urgent))
const normalInsights = computed(() => allInsights.value.filter(item => !item.urgent))

const loadPendingHouseReviews = async () => {
  const res = await request.get('/house/pending/audit')

  return asList(res.data?.list)
    .slice(0, 5)
    .map(item => ({
      ...item,
      houseTitle: item.houseTitle || item.title,
      priority: Number(item.auditStatus) === 1 ? 'urgent' : 'normal'
    }))
}

const loadPendingAgentReviews = async () => {
  const res = await request.get('/user/agent/list', {
    params: {
      status: 2,
      pageNum: 1,
      pageSize: 5
    }
  })

  return asList(res.data?.list)
}

const loadRecentDeals = async () => {
  const res = await request.get('/transaction/list', {
    params: {
      status: 3,
      pageNum: 1,
      pageSize: 5
    }
  })

  return asList(res.data?.list)
    .sort((a, b) => {
      const aTime = new Date(a.dealDate || a.updateTime || 0).getTime()
      const bTime = new Date(b.dealDate || b.updateTime || 0).getTime()
      return bTime - aTime
    })
    .slice(0, 5)
    .map(item => ({
      ...item,
      dealTime: item.dealDate || item.updateTime
    }))
}

const loadDashboard = async () => {
  loading.value = true
  error.value = ''

  try {
    const res = await request.get('/admin/dashboard', { params: { months: salesWindow.value } })
    dashboard.value = {
      ...dashboard.value,
      ...(res.data || {}),
      pendingHouseReviews: asList(res.data?.pendingHouseReviews).length ? asList(res.data?.pendingHouseReviews) : await loadPendingHouseReviews(),
      pendingAgentReviews: asList(res.data?.pendingAgentReviews).length ? asList(res.data?.pendingAgentReviews) : await loadPendingAgentReviews(),
      recentDeals: asList(res.data?.recentDeals).length ? asList(res.data?.recentDeals) : await loadRecentDeals()
    }

    const provinces = provinceSummary.value.map(item => item.province)
    if (!provinces.includes(selectedProvince.value)) {
      selectedProvince.value = provinces[0] || ''
    }
    selectedCity.value = ''
  } catch (err) {
    console.error('加载管理员看板失败:', err)
    error.value = err?.message || '加载管理员看板失败'
  } finally {
    loading.value = false
    await nextTick()
    renderAllCharts()
  }
}

const go = path => router.push(path)
const openHouseReview = item => router.push({
  path: '/admin-layout/house/audit',
  query: {
    source: 'dashboard',
    itemId: String(item?.id || '')
  }
})
const openStaffReview = () => router.push({
  path: '/admin-layout/user/staff',
  query: {
    tab: 'agent',
    status: '2',
    source: 'dashboard'
  }
})
const openTransactionDetail = id => router.push({
  path: '/admin-layout/transaction/manage',
  query: {
    status: '3',
    source: 'dashboard',
    transactionId: String(id || '')
  }
})

const setTrendWindow = async value => {
  trendWindow.value = value
  await nextTick()
  renderTrend()
}

const setTrendMetric = async value => {
  trendMetric.value = value
  await nextTick()
  renderTrend()
}

const setChartMode = async value => {
  chartMode.value = value
  await nextTick()
  renderTrend()
}

const setDistributionMetric = async value => {
  distributionMetric.value = value
  await nextTick()
  renderDistribution()
}

const setComparisonMetric = async value => {
  comparisonMetric.value = value
  await nextTick()
  renderCityComparison()
}

const setPriceAnalysisMode = async value => {
  priceAnalysisMode.value = value
  await nextTick()
  renderPriceAnalysis()
}

const setActiveAgentMode = value => {
  activeAgentMode.value = value
}

const formatComparisonValue = value => {
  const config = COMPARISON_METRICS.find(item => item.value === comparisonMetric.value)
  if (config?.unit === 'money') return money(value)
  if (config?.unit === 'percent') return `${Number(value || 0).toFixed(1)}%`
  return formatNumber(value)
}

const statusTone = status => {
  if (Number(status) === 1) return 'pending'
  if (Number(status) === 2) return 'signed'
  if (Number(status) === 3) return 'completed'
  return 'cancelled'
}

const formatMetricValue = value => {
  if (distributionMetric.value === 'averagePrice') return money(value)
  if (distributionMetric.value === 'averageUnitPrice') return unitPrice(value)
  return `${formatNumber(value)} 套`
}

const formatAgentMetric = item => {
  if (activeAgentMode.value === 'transactionCount') {
    return `${formatNumber(item.transactionCount)} 单`
  }
  return dealAmount(item.salesAmount)
}

const formatDealAmount = value => dealAmount(value)

const renderAllCharts = () => {
  renderTrend()
  renderPriceAnalysis()
  renderCityComparison()
  renderDistribution()
}

const renderTrend = () => {
  if (!trendChartRef.value) return
  if (!visibleMonthlySales.value.length) {
    trendChart?.clear()
    return
  }

  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const isBar = chartMode.value === 'bar'
  const isCountMode = trendMetric.value === 'count'
  const months = visibleMonthlySales.value.map(item => monthLabel(item.month))
  const amounts = visibleMonthlySales.value.map(item => Number(item.salesAmount || 0))
  const counts = visibleMonthlySales.value.map(item => Number(item.transactionCount || 0))
  const values = isCountMode ? counts : amounts
  const accentTop = isCountMode ? '#2563eb' : '#064e3b'
  const accentBottom = isCountMode ? '#60a5fa' : '#10b981'
  const areaTop = isCountMode ? 'rgba(37, 99, 235, 0.24)' : 'rgba(5, 150, 105, 0.28)'
  const areaBottom = isCountMode ? 'rgba(96, 165, 250, 0.03)' : 'rgba(5, 150, 105, 0.02)'

  trendChart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        formatter: params => {
          const index = params[0]?.dataIndex ?? 0
          return [
            months[index],
            `成交额：${money(amounts[index])}`,
            `成交单量：${counts[index]} 单`
          ].join('<br/>')
        }
      },
      grid: { left: '3%', right: '4%', top: '12%', bottom: '4%', containLabel: true },
      xAxis: {
        type: 'category',
        data: months,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#dbe5ec' } },
        axisLabel: { color: '#64748b' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
        axisLabel: {
          color: '#64748b',
          formatter: value => (isCountMode ? `${Number(value || 0)}` : shortMoney(value))
        }
      },
      series: [
        {
          type: isBar ? 'bar' : 'line',
          smooth: !isBar,
          data: values,
          barWidth: isBar ? '40%' : undefined,
          symbolSize: isBar ? 0 : 8,
          lineStyle: { width: 3, color: accentBottom },
          areaStyle: isBar
            ? undefined
            : {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: areaTop },
                { offset: 1, color: areaBottom }
              ])
            },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: accentTop },
              { offset: 1, color: accentBottom }
            ]),
            borderRadius: isBar ? [8, 8, 0, 0] : 0
          },
          label: {
            show: true,
            position: 'top',
            formatter: params => (
              isCountMode
                ? `${counts[params.dataIndex]} 单`
                : shortMoney(amounts[params.dataIndex])
            ),
            color: accentBottom,
            fontSize: 11
          }
        }
      ]
    },
    true
  )
}

const renderPriceAnalysis = () => {
  if (!priceAnalysisChartRef.value) return
  if (!priceAnalysisData.value.length) {
    priceAnalysisChart?.clear()
    return
  }

  if (!priceAnalysisChart) priceAnalysisChart = echarts.init(priceAnalysisChartRef.value)

  const data = priceAnalysisData.value
  const inventoryData = data.map(item => item.inventory)
  const dealsData = data.map(item => item.deals)

  priceAnalysisChart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
        formatter: params => {
          const index = params[0]?.dataIndex ?? 0
          const item = data[index]
          return [
            `<strong>${item.range}</strong>`,
            `库存量：${item.inventory} 套`,
            `成交量：${item.deals} 套`,
            `转化率：${item.conversionRate}%`,
            `趋势：${Number(item.trend) >= 0 ? '+' : ''}${item.trend}%`
          ].join('<br/>')
        }
      },
      legend: { data: ['库存量', '成交量'], top: 0, right: 0, textStyle: { color: '#4b5563' } },
      grid: { left: '3%', right: '4%', top: '16%', bottom: '4%', containLabel: true },
      xAxis: {
        type: 'category',
        data: data.map(item => item.range),
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        axisLabel: { color: '#6b7280', rotate: 15, fontSize: 11 }
      },
      yAxis: [
        {
          type: 'value',
          name: '库存',
          splitLine: { lineStyle: { type: 'dashed', color: '#e5e7eb' } },
          axisLabel: { color: '#6b7280' }
        },
        {
          type: 'value',
          name: '成交',
          splitLine: { show: false },
          axisLabel: { color: '#6b7280' }
        }
      ],
      series: [
        {
          name: '库存量',
          type: 'bar',
          barWidth: '40%',
          data: inventoryData.map((value, index) => ({
            value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: data[index].color },
                { offset: 1, color: `${data[index].color}88` }
              ]),
              borderRadius: [8, 8, 0, 0]
            }
          }))
        },
        {
          name: '成交量',
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          data: dealsData,
          lineStyle: { width: 3, color: '#f59e0b' },
          itemStyle: { color: '#f59e0b', borderColor: '#fff', borderWidth: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
              { offset: 1, color: 'rgba(245, 158, 11, 0.02)' }
            ])
          }
        }
      ]
    },
    true
  )
}

const renderCityComparison = () => {
  if (!cityComparisonChartRef.value) return
  if (!cityComparisonData.value.length) {
    cityComparisonChart?.clear()
    return
  }

  if (!cityComparisonChart) cityComparisonChart = echarts.init(cityComparisonChartRef.value)

  const config = COMPARISON_METRICS.find(item => item.value === comparisonMetric.value)
  const values = cityComparisonData.value.map(item => Number(item[comparisonMetric.value] || 0))

  cityComparisonChart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: params => {
          const item = cityComparisonData.value[params[0]?.dataIndex ?? 0]
          return [
            item.city,
            `${comparisonLabel.value}：${formatComparisonValue(item[comparisonMetric.value])}`,
            `成交量：${item.transactionCount} 笔`,
            `流动性：${item.turnoverRate}%`
          ].join('<br/>')
        }
      },
      grid: { left: '3%', right: '4%', top: '8%', bottom: '4%', containLabel: true },
      xAxis: {
        type: 'category',
        data: cityComparisonData.value.map(item => item.city),
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        axisLabel: { color: '#6b7280', rotate: 24, fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#e5e7eb' } },
        axisLabel: {
          color: '#6b7280',
          formatter: value => (config?.unit === 'money' ? shortMoney(value) : value)
        }
      },
      series: [
        {
          type: 'bar',
          barWidth: '48%',
          data: values.map((value, index) => ({
            value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: index < 3 ? '#064e3b' : '#059669' },
                { offset: 1, color: index < 3 ? '#10b981' : '#6ee7b7' }
              ]),
              borderRadius: [8, 8, 0, 0]
            }
          }))
        }
      ]
    },
    true
  )
}

const renderDistribution = () => {
  if (!distributionChartRef.value) return
  if (!distributionRows.value.length) {
    distributionChart?.clear()
    return
  }

  if (!distributionChart) distributionChart = echarts.init(distributionChartRef.value)

  distributionChart.setOption(
    {
      tooltip: {
        trigger: 'item',
        formatter: params => `${params.name}<br/>${metricLabel.value}：${formatMetricValue(params.value)}<br/>占比：${params.percent}%`
      },
      legend: {
        orient: 'vertical',
        right: '2%',
        top: 'center',
        textStyle: { color: '#475569', fontSize: 12 }
      },
      series: [
        {
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['34%', '50%'],
          label: {
            formatter: params => `${params.name}\n${formatMetricValue(params.value)}`,
            color: '#334155',
            fontSize: 11,
            lineHeight: 18
          },
          data: distributionRows.value.map(item => ({
            name: distributionItemLabel(item),
            value: metricValue(item, distributionMetric.value)
          })),
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 2
          },
          color: ['#064e3b', '#059669', '#10b981', '#34d399', '#f59e0b', '#fb7185', '#0d9488']
        }
      ]
    },
    true
  )
}

const handleResize = debounce(() => {
  trendChart?.resize()
  priceAnalysisChart?.resize()
  cityComparisonChart?.resize()
  distributionChart?.resize()
}, 180)

watch(selectedProvince, async () => {
  selectedCity.value = ''
  await nextTick()
  renderDistribution()
  renderCityComparison()
})

watch(selectedCity, async () => {
  await nextTick()
  renderDistribution()
})

watch(selectedAnalysisCity, async () => {
  await nextTick()
  renderPriceAnalysis()
})

watch(salesWindow, loadDashboard)

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  priceAnalysisChart?.dispose()
  cityComparisonChart?.dispose()
  distributionChart?.dispose()
})
</script>

<style scoped>
.admin-page {
  min-height: calc(100vh - 60px);
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(6, 78, 59, 0.08), transparent 28%),
    linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 52%, #d1fae5 100%);
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  margin-bottom: 18px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 14px;
  color: #991b1b;
}

.banner-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #dc2626;
  color: #fff;
  font-weight: 700;
}

.error-banner button {
  margin-left: auto;
  border: 0;
  border-radius: 999px;
  padding: 8px 16px;
  background: #dc2626;
  color: #fff;
  cursor: pointer;
}

.hero {
  display: block;
  margin-bottom: 20px;
}

.hero-content,
.quick-actions,
.summary-card,
.panel,
.insights-bar {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
}

.hero-content {
  padding: 28px 30px;
  border-radius: 28px;
}

.eyebrow,
.panel-kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #059669;
}

.hero-content h1 {
  margin: 0;
  font-size: 38px;
  background: linear-gradient(135deg, #064e3b 0%, #059669 50%, #10b981 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-text {
  margin: 14px 0 0;
  color: #607086;
  font-size: 16px;
  line-height: 1.75;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  border-radius: 28px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  border: 0;
  border-radius: 22px;
  padding: 18px 16px;
  background: linear-gradient(135deg, #ffffff 0%, rgba(236, 253, 245, 0.72) 100%);
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(6, 78, 59, 0.12);
}

.quick-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
  font-size: 16px;
  font-weight: 700;
}

.quick-card strong {
  display: block;
  color: #132238;
  font-size: 15px;
}

.quick-card p {
  margin: 6px 0 0;
  color: #607086;
  font-size: 13px;
  line-height: 1.5;
}

.insights-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 20px;
  padding: 16px 18px;
  border-radius: 24px;
}

.insights-header {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 108px;
  color: #b45309;
}

.insights-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f59e0b;
  box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.14);
}

.insights-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.insight-item {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: #475569;
  background: #f8fafc;
}

.insight-item.warning {
  color: #92400e;
  background: #fef3c7;
}

.insight-item.danger {
  color: #991b1b;
  background: #fee2e2;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.execution-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.execution-panel {
  display: flex;
  flex-direction: column;
  height: 472px;
  min-height: 472px;
}

.execution-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 6px;
}

.execution-list::-webkit-scrollbar {
  width: 6px;
}

.execution-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.72);
}

.execution-list::-webkit-scrollbar-track {
  background: transparent;
}

.execution-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.execution-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.execution-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.execution-item strong {
  color: #132238;
  font-size: 14px;
  line-height: 1.5;
}

.execution-item p {
  margin: 0 0 6px;
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
}

.execution-item small {
  color: #8b99ab;
}

.mini-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.mini-tag.ok {
  color: #047857;
  background: rgba(5, 150, 105, 0.12);
}

.mini-tag.warn {
  color: #b45309;
  background: rgba(245, 158, 11, 0.16);
}

.mini-tag.muted {
  color: #475569;
  background: rgba(100, 116, 139, 0.12);
}
.hero {
  display: block;
  margin-bottom: 20px;
}

.agent-rank-section {
  margin-bottom: 20px;
}

.summary-card {
  padding: 22px;
  border-radius: 24px;
}

.summary-card.ok {
  border-top: 4px solid #059669;
}

.summary-card.warn {
  border-top: 4px solid #f59e0b;
}

.summary-card.muted {
  border-top: 4px solid #64748b;
}

.summary-top,
.summary-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.summary-label {
  color: #607086;
  font-size: 13px;
}

.summary-main {
  margin: 14px 0 10px;
}

.summary-main strong {
  font-size: 28px;
  color: #132238;
  line-height: 1.2;
}

.summary-card small {
  color: #8b99ab;
  line-height: 1.5;
}

.summary-badge,
.trend-chip,
.panel-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.summary-badge.ok,
.panel-badge,
.trend-chip.up {
  color: #047857;
  background: rgba(5, 150, 105, 0.12);
}

.summary-badge.warn {
  color: #b45309;
  background: rgba(245, 158, 11, 0.14);
}

.summary-badge.muted {
  color: #475569;
  background: rgba(100, 116, 139, 0.12);
}

.trend-chip.down {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.12);
}

.layout-grid {
  display: flex;
  flex-direction: row;
  gap: 24px;
  align-items: flex-start;
}

.main-column {
  flex: 2;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-column {
  flex: 1;
  min-width: 320px;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel {
  padding: 24px;
  border-radius: 24px;
}

.panel-accent.warm {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 251, 235, 0.98) 100%);
}


.panel-accent.cool {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(240, 249, 255, 0.98) 100%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.panel-header h2 {
  margin: 0;
  color: #132238;
  font-size: 22px;
}

.panel-subtitle,
.comparison-intro{
  color: #607086;
  font-size: 13px;
}

.panel-subtitle {
  margin: -6px 0 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toggle-group {
  display: flex;
  gap: 4px;
  padding: 4px;
  background: #f1f5f9;
  border-radius: 12px;
}

.toggle-group button {
  border: 0;
  border-radius: 10px;
  padding: 8px 14px;
  background: transparent;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-group button.active {
  background: #fff;
  color: #059669;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.toggle-group.compact button {
  padding: 7px 12px;
  font-size: 12px;
}

.selector-group {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #607086;
  font-size: 13px;
}

.selector-group select {
  border: 1px solid #d9e3ea;
  border-radius: 12px;
  padding: 8px 12px;
  background: #fff;
  color: #334155;
}

.chart {
  width: 100%;
}

.trend-chart,
.price-analysis-chart {
  height: 320px;
}

.comparison-chart,
.district-chart {
  height: 300px;
}

.analysis-summary-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, #064e3b 0%, #059669 100%);
}

.analysis-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.analysis-item span {
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.analysis-item strong {
  color: #fff;
  font-size: 17px;
}

.analysis-item .warning {
  color: #fde68a;
}

.analysis-item .highlight {
  color: #a7f3d0;
}

.legend-row {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin: 12px 0 18px;
  color: #607086;
  font-size: 13px;
}

.legend-row span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.legend-bar,
.legend-line {
  display: inline-block;
  border-radius: 999px;
}

.legend-bar {
  width: 18px;
  height: 10px;
  background: linear-gradient(135deg, #059669 0%, #6ee7b7 100%);
}

.legend-line {
  width: 20px;
  height: 3px;
  background: #f59e0b;
}

.segment-grid,
.comparison-grid{
  display: grid;
  gap: 14px;
}

.segment-grid,
.comparison-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.segment-card,
.comparison-card,
.district-card,
.rank-item,
.status-item,
.tip-item {
  border-radius: 18px;
  background: #f8fafc;
}

.segment-card,
.comparison-card,
.district-card {
  padding: 16px;
}

.segment-card.active {
  background: linear-gradient(135deg, rgba(6, 78, 59, 0.08) 0%, rgba(16, 185, 129, 0.06) 100%);
  border: 1px solid rgba(5, 150, 105, 0.22);
}

.segment-head,
.segment-foot,
.rank-main,
.status-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.segment-head span {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(5, 150, 105, 0.12);
  color: #047857;
  font-size: 11px;
  font-weight: 600;
}

.segment-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 14px 0;
}

.segment-metrics small,
.comparison-card span,
.district-card small,
.rank-info small {
  color: #607086;
}

.segment-metrics strong,
.comparison-card strong,
.district-card strong,
.rank-value {
  color: #132238;
}

.segment-foot em,
.comparison-card small.up {
  color: #047857;
}

.segment-foot em.down,
.comparison-card small.down {
  color: #b91c1c;
}

.comparison-intro {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(59, 130, 246, 0.06);
  margin-bottom: 16px;
}

.comparison-card {
  text-align: center;
}

.comparison-card strong {
  display: block;
  margin: 8px 0 6px;
  font-size: 20px;
}

.chip-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.chip-block span {
  color: #607086;
  font-size: 13px;
}

.chip-block.subtle {
  padding: 12px;
  border-radius: 16px;
  background: #f8fafc;
}

.auto-drill-note {
  gap: 8px;
}

.auto-drill-copy {
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.auto-drill-copy strong {
  color: #047857;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: 0;
  border-radius: 999px;
  padding: 8px 14px;
  background: #eef2f7;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.small {
  padding: 7px 12px;
  font-size: 12px;
}

.chip.active {
  background: rgba(5, 150, 105, 0.12);
  color: #047857;
}

.district-card {
  text-align: center;
}

.district-card span {
  display: block;
  margin: 8px 0 6px;
  color: #047857;
  font-weight: 600;
}

.rank-list,
.status-list,
.tip-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.active-rank-list .rank-item {
  background: linear-gradient(135deg, #f8fafc 0%, rgba(236, 253, 245, 0.82) 100%);
}

.rank-item,
.status-item,
.tip-item {
  padding: 14px 16px;
}

.rank-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #e2e8f0;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.rank-num.top {
  background: rgba(5, 150, 105, 0.14);
  color: #047857;
}

.progress-bar {
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: #e2e8f0;
}

.progress-bar.soft {
  height: 7px;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #064e3b 0%, #059669 50%, #10b981 100%);
}

.progress-fill.pending {
  background: linear-gradient(90deg, #d97706 0%, #f59e0b 100%);
}

.progress-fill.signed {
  background: linear-gradient(90deg, #2563eb 0%, #60a5fa 100%);
}

.progress-fill.completed {
  background: linear-gradient(90deg, #059669 0%, #34d399 100%);
}

.progress-fill.cancelled {
  background: linear-gradient(90deg, #64748b 0%, #94a3b8 100%);
}

.tip-item strong {
  color: #334155;
  font-size: 14px;
  line-height: 1.6;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 20px;
  color: #64748b;
}

.empty-state.compact {
  padding: 24px 16px;
}

.spinner {
  width: 38px;
  height: 38px;
  border: 3px solid #dbe5ec;
  border-top-color: #059669;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1440px) {
  .summary-grid,
  .analysis-summary-bar,
  .segment-grid,
  .comparison-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quick-actions,
  .execution-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1280px) {
  .layout-grid {
    flex-direction: column;
  }

  .main-column,
  .side-column {
    width: 100%;
    max-width: none;
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .admin-page {
    padding: 16px;
  }

  .hero-content h1 {
    font-size: 28px;
  }

  .summary-grid,
  .analysis-summary-bar,
  .segment-grid,
  .comparison-grid,
  .execution-grid,
  .quick-actions {
    grid-template-columns: 1fr;
  }

  .insights-bar,
  .panel-header,
  .header-actions{
    flex-direction: column;
    align-items: stretch;
  }

  .chart {
    height: 280px;
  }
}
</style>
