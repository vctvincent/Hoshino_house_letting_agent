<template>
  <div class="agent-dashboard-page">
    <div v-if="error" class="error-banner">
      <span class="banner-mark">!</span>
      <span>{{ error }}</span>
      <button type="button" @click="loadDashboard">重试</button>
    </div>

    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">Business Dashboard</p>
        <h1>业务看板</h1>
        <p class="hero-text">
          聚合我的成交趋势、带看推进、交易流转和房源区域分布，
          用和系统看板一致的布局快速定位今天最值得处理的业务。
        </p>
      </div>
    </section>

    <section v-if="urgentInsights.length" class="insights-bar">
      <div class="insights-header">
        <span class="insights-dot"></span>
        <strong>业务提醒</strong>
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
          <span
            v-if="card.trend !== undefined"
            :class="['trend-chip', Number(card.trend) >= 0 ? 'up' : 'down']"
          >
            {{ Number(card.trend) >= 0 ? '+' : '' }}{{ Number(card.trend).toFixed(1) }}%
          </span>
        </div>
        <small>{{ card.note }}</small>
      </article>
    </section>

    <section class="layout-grid">
      <div class="main-column">
        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Monthly Trend</p>
              <h2>近 {{ trendWindow }} 个月成交趋势</h2>
            </div>
            <div class="header-actions">
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
          <div v-else-if="!visibleMonthlySales.length" class="empty-state compact">
            <p>暂无成交趋势数据</p>
          </div>
          <div v-else ref="trendChartRef" class="chart trend-chart"></div>
        </article>

        <article class="panel panel-accent cool">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Viewing Pipeline</p>
              <h2>我的带看推进</h2>
            </div>
            <span class="panel-badge">转化率 {{ viewingConversionRate }}%</span>
          </header>

          <div class="pipeline-grid">
            <article class="pipeline-card warn">
              <span>待确认</span>
              <strong>{{ overview.pendingViewingCount || 0 }}</strong>
              <small>待响应客户预约</small>
            </article>
            <article class="pipeline-card info">
              <span>已确认</span>
              <strong>{{ overview.confirmedViewingCount || 0 }}</strong>
              <small>已锁定带看时段</small>
            </article>
            <article class="pipeline-card ok">
              <span>已完成</span>
              <strong>{{ overview.completedViewingCount || 0 }}</strong>
              <small>已完成实地带看</small>
            </article>
            <article class="pipeline-card muted">
              <span>已取消</span>
              <strong>{{ overview.cancelledViewingCount || 0 }}</strong>
              <small>需判断是否重约</small>
            </article>
          </div>

          <div class="viewing-rate-card">
            <div class="rate-head">
              <span>带看完成率</span>
              <strong>{{ viewingConversionRate }}%</strong>
            </div>
            <div class="rate-bar">
              <div class="rate-fill" :style="{ width: `${viewingConversionRate}%` }"></div>
            </div>
            <p>每 {{ viewingStats.perDeal }} 次已完成带看，约带来 1 笔成交。</p>
          </div>
        </article>

        <article class="panel panel-accent warm">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Transaction Distribution</p>
              <h2>我的交易状态分布</h2>
            </div>
          </header>

          <div v-if="!hasTransactionData" class="empty-state compact">
            <p>暂无交易状态数据</p>
          </div>
          <template v-else>
            <div ref="transactionChartRef" class="chart transaction-chart"></div>
            <div class="transaction-summary">
              <div v-for="item in transactionSummaryItems" :key="item.label" class="transaction-summary-item">
                <span class="summary-dot" :class="item.tone"></span>
                <span>{{ item.label }} {{ item.value }}</span>
              </div>
            </div>
          </template>
        </article>

        <article class="panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">District Comparison</p>
              <h2>我的房源区域分布</h2>
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
            <div class="distribution-info">
              <strong>全量区级比较</strong>
              <span>指标 {{ metricLabel }}</span>
              <span>区级条目 {{ districtComparisonData.length }}</span>
            </div>

            <div v-if="!districtComparisonData.length" class="empty-state compact">
              <p>暂无区级对比数据</p>
            </div>
            <template v-else>
              <div ref="distributionChartRef" class="chart district-chart"></div>
              <div class="district-grid">
                <article
                  v-for="item in districtComparisonData.slice(0, 6)"
                  :key="item.key"
                  class="district-card"
                >
                  <strong>{{ item.displayName }}</strong>
                  <span>{{ formatMetric(item) }}</span>
                  <small>{{ item.locationLabel }}</small>
                </article>
              </div>
            </template>
          </template>
        </article>
      </div>

      <aside class="side-column">
        <article class="panel execution-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Pending Viewings</p>
              <h2>待处理带看</h2>
            </div>
            <div class="header-actions">
              <span class="panel-badge">{{ pendingViewings.length }} 条</span>
            </div>
          </header>

          <div class="execution-list execution-list--fixed">
            <template v-if="!pendingViewings.length">
              <div class="empty-state compact execution-empty-state">
                <p>暂无待处理带看</p>
              </div>
            </template>
            <template v-else>
              <article
                v-for="item in pendingViewings"
                :key="item.id"
                class="execution-item"
                @click="go('/layout/viewing-manage')"
              >
                <div class="execution-top">
                  <strong>{{ item.houseTitle || '未命名房源' }}</strong>
                  <span class="mini-tag warn">{{ item.statusLabel || '待推进' }}</span>
                </div>
                <p>{{ item.customerName || '未知客户' }}</p>
                <small>{{ formatDateTime(item.appointTime || item.updateTime) }}</small>
              </article>
            </template>
          </div>
        </article>

        <article class="panel execution-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Pending Transactions</p>
              <h2>待推进交易</h2>
            </div>
            <span class="panel-badge">{{ pendingTransactions.length }} 条</span>
          </header>

          <div class="execution-list execution-list--fixed">
            <template v-if="!pendingTransactions.length">
              <div class="empty-state compact execution-empty-state">
                <p>暂无待推进交易</p>
              </div>
            </template>
            <template v-else>
              <article
                v-for="item in pendingTransactions"
                :key="item.id"
                class="execution-item"
                @click="go('/layout/transaction/manage')"
              >
                <div class="execution-top">
                  <strong>{{ item.houseTitle || '未命名房源' }}</strong>
                  <span class="mini-tag muted">{{ item.statusLabel || '进行中' }}</span>
                </div>
                <p>{{ item.customerName || '未知客户' }}</p>
                <small>{{ formatDateTime(item.updateTime) }}</small>
              </article>
            </template>
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

          <div class="execution-list execution-list--fixed">
            <template v-if="!recentDeals.length">
              <div class="empty-state compact execution-empty-state">
                <p>暂无最近成交记录</p>
              </div>
            </template>
            <template v-else>
              <article
                v-for="item in recentDeals"
                :key="item.id"
                class="execution-item"
                @click="go('/layout/transaction/manage')"
              >
                <div class="execution-top">
                  <strong>{{ item.houseTitle || '未命名房源' }}</strong>
                  <span class="mini-tag ok">{{ formatDealAmount(item.finalPrice) }}</span>
                </div>
                <p>{{ item.customerName || '未知客户' }}</p>
                <small>{{ formatDateTime(item.dealTime) }}</small>
              </article>
            </template>
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

        <article v-if="normalInsights.length" class="panel tips-panel">
          <header class="panel-header">
            <div>
              <p class="panel-kicker">Highlights</p>
              <h2>今日提示</h2>
            </div>
          </header>

          <ul class="tips-list">
            <li v-for="(item, index) in normalInsights" :key="`${item.message}-${index}`">
              <span class="tip-mark">•</span>
              <span>{{ item.message }}</span>
            </li>
          </ul>
        </article>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const TREND_WINDOWS = [
  { label: '近3月', value: 3 },
  { label: '近6月', value: 6 }
]

const METRICS = [
  { label: '库存套数', value: 'houseCount' },
  { label: '在售套数', value: 'publishedCount' },
  { label: '平均总价', value: 'averagePrice' },
  { label: '平均单价', value: 'averageUnitPrice' }
]

const ACTIVE_AGENT_MODES = [
  { label: '成交单量', value: 'transactionCount' },
  { label: '成交额', value: 'salesAmount' }
]

const VIEWING_STATUS_MAP = {
  0: '待确认',
  1: '已确认',
  2: '已完成',
  3: '已取消'
}

const TRANSACTION_STATUS_MAP = {
  0: '待确认',
  1: '谈判中',
  2: '已签约',
  3: '已完成',
  4: '已取消'
}

const router = useRouter()

const loading = ref(false)
const error = ref('')
const chartMode = ref('line')
const trendWindow = ref(6)
const distributionMetric = ref('houseCount')
const activeAgentMode = ref('transactionCount')

const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const transactionChartRef = ref(null)

const dashboard = shallowRef({
  overview: {},
  monthlySales: [],
  districtSales: [],
  inventoryDistribution: [],
  viewingStatusDistribution: [],
  pendingViewings: [],
  pendingTransactions: [],
  recentDeals: [],
  activeAgents: []
})

// 发起带看对话框相关状态
const bookViewingVisible = ref(false)
const bookLoading = ref(false)
const customerList = ref([])
const myHouseList = ref([])
const bookForm = ref({
  customerId: null,
  houseId: null,
  appointTime: '',
  customerPhone: '',
  remark: ''
})

let trendChart = null
let distributionChart = null
let transactionChart = null

const shortcuts = [
  {
    title: '房源管理',
    description: '管理在售、编辑和新增房源',
    path: '/layout/house/manage',
    mark: '房'
  },
  {
    title: '带看管理',
    description: '推进预约、完成记录和评价邀请',
    path: '/layout/viewing-manage',
    mark: '看'
  },
  {
    title: '交易管理',
    description: '跟进谈判、签约和成交流程',
    path: '/layout/transaction/manage',
    mark: '交'
  }
]

const asList = value => (Array.isArray(value) ? value : [])

const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => fn(...args), delay)
  }
}

const overview = computed(() => dashboard.value.overview || {})
const inventoryRows = computed(() => asList(dashboard.value.inventoryDistribution))
const monthlySales = computed(() => asList(dashboard.value.monthlySales))
const visibleMonthlySales = computed(() => monthlySales.value.slice(-Math.min(trendWindow.value, monthlySales.value.length)))

const formatNumber = value => Number(value || 0).toLocaleString('zh-CN')

const money = value => {
  const num = Number(value || 0)
  if (num >= 10000) {
    return `${(num / 10000).toFixed(2)} 亿元`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(2)} 千万`
  }
  return `${num.toFixed(2)} 万元`
}

const shortMoney = value => {
  const num = Number(value || 0)
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}亿`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}千万`
  }
  return `${num.toFixed(0)}万`
}

const unitPrice = value => `${Number(value || 0).toFixed(0)} 元/㎡`

const formatMetricValue = value => {
  if (distributionMetric.value === 'averagePrice') {
    return money(value)
  }
  if (distributionMetric.value === 'averageUnitPrice') {
    return unitPrice(value)
  }
  return `${formatNumber(value)} 套`
}

const formatMetric = item => formatMetricValue(metricValue(item, distributionMetric.value))

const formatDateTime = value => {
  if (!value) {
    return '时间待定'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '时间待定'
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatMonthLabel = month => {
  const text = String(month || '')
  if (text.includes('-')) {
    return `${text.split('-')[1]}月`
  }
  return text
}

const metricValue = (item, metric) => {
  if (metric === 'publishedCount') return Number(item.publishedCount || 0)
  if (metric === 'averagePrice') return Number(item.averagePrice || 0)
  if (metric === 'averageUnitPrice') return Number(item.averageUnitPrice || 0)
  return Number(item.houseCount || 0)
}

const buildProvinceSummary = rows => {
  const map = new Map()
  rows.forEach(row => {
    const province = row.province || '未标注'
    const current = map.get(province) || { province, houseCount: 0, publishedCount: 0 }
    current.houseCount += Number(row.houseCount || 0)
    current.publishedCount += Number(row.publishedCount || 0)
    map.set(province, current)
  })
  return [...map.values()].sort((a, b) => b.houseCount - a.houseCount)
}

const buildCitySummary = (rows, province) => {
  const map = new Map()
  rows
    .filter(row => (row.province || '未标注') === province)
    .forEach(row => {
      const city = row.city || '未标注'
      const current = map.get(city) || {
        city,
        houseCount: 0,
        publishedCount: 0,
        averagePriceSum: 0,
        averagePriceWeight: 0,
        averageUnitPriceSum: 0,
        averageUnitPriceWeight: 0
      }

      const houseCount = Number(row.houseCount || 0)
      current.houseCount += houseCount
      current.publishedCount += Number(row.publishedCount || 0)
      current.averagePriceSum += Number(row.averagePrice || 0) * houseCount
      current.averagePriceWeight += houseCount
      current.averageUnitPriceSum += Number(row.averageUnitPrice || 0) * houseCount
      current.averageUnitPriceWeight += houseCount
      map.set(city, current)
    })

  return [...map.values()]
    .map(item => ({
      ...item,
      averagePrice: item.averagePriceWeight ? item.averagePriceSum / item.averagePriceWeight : 0,
      averageUnitPrice: item.averageUnitPriceWeight ? item.averageUnitPriceSum / item.averageUnitPriceWeight : 0
    }))
    .sort((a, b) => b.houseCount - a.houseCount)
}

const provinceSummary = computed(() => buildProvinceSummary(inventoryRows.value))

const districtComparisonData = computed(() => {
  const map = new Map()

  inventoryRows.value.forEach(row => {
    const province = row.province || '未标注'
    const city = row.city || '未标注'
    const district = row.district || '未标注'
    const key = `${province}-${city}-${district}`

    const current = map.get(key) || {
      ...row,
      key,
      province,
      city,
      district,
      houseCount: 0,
      publishedCount: 0,
      averagePriceSum: 0,
      averagePriceWeight: 0,
      averageUnitPriceSum: 0,
      averageUnitPriceWeight: 0
    }

    const houseCount = Number(row.houseCount || 0)
    current.houseCount += houseCount
    current.publishedCount += Number(row.publishedCount || 0)
    current.averagePriceSum += Number(row.averagePrice || 0) * houseCount
    current.averagePriceWeight += houseCount
    current.averageUnitPriceSum += Number(row.averageUnitPrice || 0) * houseCount
    current.averageUnitPriceWeight += houseCount
    map.set(key, current)
  })

  return [...map.values()]
    .map(item => ({
      ...item,
      averagePrice: item.averagePriceWeight ? item.averagePriceSum / item.averagePriceWeight : 0,
      averageUnitPrice: item.averageUnitPriceWeight ? item.averageUnitPriceSum / item.averageUnitPriceWeight : 0,
      displayName: item.district,
      locationLabel: `${item.province} · ${item.city}`
    }))
    .sort((a, b) => metricValue(b, distributionMetric.value) - metricValue(a, distributionMetric.value))
})

const hasTransactionData = computed(() => {
  return (
    Number(overview.value.pendingTransactionCount || 0) > 0 ||
    Number(overview.value.negotiatingTransactionCount || 0) > 0 ||
    Number(overview.value.signedTransactionCount || 0) > 0 ||
    Number(overview.value.completedTransactionCount || 0) > 0 ||
    Number(overview.value.cancelledTransactionCount || 0) > 0
  )
})

const transactionSummaryItems = computed(() => [
  { label: '待确认', value: overview.value.pendingTransactionCount || 0, tone: 'warn' },
  { label: '谈判中', value: overview.value.negotiatingTransactionCount || 0, tone: 'info' },
  { label: '已签约', value: overview.value.signedTransactionCount || 0, tone: 'signed' },
  { label: '已完成', value: overview.value.completedTransactionCount || 0, tone: 'ok' },
  { label: '已取消', value: overview.value.cancelledTransactionCount || 0, tone: 'muted' }
])

const metricLabel = computed(() => METRICS.find(item => item.value === distributionMetric.value)?.label || '库存套数')

const viewingConversionRate = computed(() => {
  const completed = Number(overview.value.completedViewingCount || 0)
  const total = completed + Number(overview.value.pendingViewingCount || 0) + Number(overview.value.confirmedViewingCount || 0)
  return total ? Number(((completed / total) * 100).toFixed(1)) : 0
})

const viewingStats = computed(() => {
  const completed = Number(overview.value.completedViewingCount || 0)
  const deals = Number(overview.value.completedTransactionCount || 0)
  return {
    perDeal: completed && deals ? Number((completed / deals).toFixed(1)) : 0
  }
})

const summaryCards = computed(() => [
  {
    key: 'published-house',
    label: '我的在售房源',
    value: `${formatNumber(overview.value.publishedHouseCount || 0)} 套`,
    badge: `待审 ${formatNumber(overview.value.pendingAuditCount || 0)}`,
    tone: 'ok',
    note: `我的房源总数 ${formatNumber(overview.value.myHouseCount || 0)} 套`
  },
  {
    key: 'pending-viewing',
    label: '待处理带看',
    value: `${formatNumber(Number(overview.value.pendingViewingCount || 0) + Number(overview.value.confirmedViewingCount || 0))} 次`,
    badge: `待确认 ${formatNumber(overview.value.pendingViewingCount || 0)}`,
    tone: 'warn',
    note: `已确认 ${formatNumber(overview.value.confirmedViewingCount || 0)} 次`
  },
  {
    key: 'active-transaction',
    label: '进行中交易',
    value: `${formatNumber(
      Number(overview.value.pendingTransactionCount || 0) +
      Number(overview.value.negotiatingTransactionCount || 0) +
      Number(overview.value.signedTransactionCount || 0)
    )} 笔`,
    badge: `已完成 ${formatNumber(overview.value.completedTransactionCount || 0)}`,
    tone: 'muted',
    note: `谈判中 ${formatNumber(overview.value.negotiatingTransactionCount || 0)} 笔`
  },
  {
    key: 'monthly-sales',
    label: '本月成交额',
    value: money(overview.value.currentMonthSalesAmount || 0),
    badge: '月度',
    tone: Number(overview.value.salesGrowthRate || 0) >= 0 ? 'ok' : 'warn',
    note: `累计成交额 ${money(overview.value.totalSalesAmount || 0)}`,
    trend: Number(overview.value.salesGrowthRate || 0)
  }
])

const allInsights = computed(() => {
  const list = []

  if (Number(overview.value.pendingViewingCount || 0) > 0) {
    list.push({
      type: 'warning',
      message: `当前还有 ${overview.value.pendingViewingCount} 个带看申请待确认，建议优先处理。`,
      urgent: true
    })
  }

  if (Number(overview.value.pendingTransactionCount || 0) > 0) {
    list.push({
      type: 'danger',
      message: `有 ${overview.value.pendingTransactionCount} 笔交易处于待确认状态，建议尽快推进。`,
      urgent: true
    })
  }

  if (Number(overview.value.pendingAuditCount || 0) > 0) {
    list.push({
      type: 'warning',
      message: `当前有 ${overview.value.pendingAuditCount} 套房源仍在审核中，影响上架节奏。`,
      urgent: true
    })
  }

  if (provinceSummary.value[0]) {
    list.push({
      type: 'info',
      message: `${provinceSummary.value[0].province} 是你当前库存最集中的省份，共 ${provinceSummary.value[0].houseCount} 套房源。`,
      urgent: false
    })
  }

  if (Number(overview.value.avgRating || 0) >= 4.8) {
    list.push({
      type: 'success',
      message: `你的服务评分达到 ${Number(overview.value.avgRating || 0).toFixed(2)}，口碑表现稳定。`,
      urgent: false
    })
  }

  if (Number(overview.value.completedTransactionCount || 0) > 0) {
    list.push({
      type: 'info',
      message: `累计已完成 ${overview.value.completedTransactionCount} 笔交易，可重点跟进高意向带看转化。`,
      urgent: false
    })
  }

  if (!list.some(item => !item.urgent)) {
    list.push({
      type: 'info',
      message: '当前业务数据还在积累中，后续会形成更清晰的经营趋势。',
      urgent: false
    })
  }

  return list
})

const urgentInsights = computed(() => allInsights.value.filter(item => item.urgent))
const normalInsights = computed(() => allInsights.value.filter(item => !item.urgent))

const pendingViewings = computed(() => {
  return asList(dashboard.value.pendingViewings).map(item => ({
    ...item,
    statusLabel: VIEWING_STATUS_MAP[item.status] || '待推进'
  }))
})

const pendingTransactions = computed(() => {
  return asList(dashboard.value.pendingTransactions).map(item => ({
    ...item,
    statusLabel: TRANSACTION_STATUS_MAP[item.status] || '进行中'
  }))
})

const recentDeals = computed(() => asList(dashboard.value.recentDeals))

const activeAgents = computed(() => {
  const metric = activeAgentMode.value
  const sorted = [...asList(dashboard.value.activeAgents)].sort(
    (a, b) => Number(b[metric] || 0) - Number(a[metric] || 0)
  )
  const maxValue = Number(sorted[0]?.[metric] || 0)
  return sorted.map(item => ({
    ...item,
    shareRate: maxValue > 0 ? Number(((Number(item[metric] || 0) / maxValue) * 100).toFixed(2)) : 0
  }))
})

const activeAgentTitle = computed(() =>
  activeAgentMode.value === 'salesAmount' ? '本月成交额 Top 5' : '本月成交单量 Top 5'
)

const formatAgentMetric = item => {
  if (activeAgentMode.value === 'salesAmount') {
    return money(item.salesAmount || 0)
  }
  return `${formatNumber(item.transactionCount || 0)} 单`
}

const formatDealAmount = value => money(value || 0)

const renderTrend = () => {
  if (!trendChartRef.value) {
    return
  }

  if (!visibleMonthlySales.value.length) {
    trendChart?.clear()
    return
  }

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const rows = visibleMonthlySales.value
  const months = rows.map(item => formatMonthLabel(item.month))
  const amounts = rows.map(item => Number(item.salesAmount || 0))
  const counts = rows.map(item => Number(item.transactionCount || 0))
  const useBar = chartMode.value === 'bar'

  trendChart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        formatter: params => {
          const row = rows[params[0]?.dataIndex ?? 0] || {}
          return [
            formatMonthLabel(row.month),
            `成交额：${money(row.salesAmount || 0)}`,
            `成交笔数：${formatNumber(row.transactionCount || 0)} 笔`
          ].join('<br/>')
        }
      },
      grid: { left: '4%', right: '4%', top: '10%', bottom: '8%', containLabel: true },
      xAxis: {
        type: 'category',
        data: months,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#e5e7eb' } },
        axisLabel: { color: '#6b7280' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#e5e7eb' } },
        axisLabel: {
          color: '#6b7280',
          formatter: value => shortMoney(value)
        }
      },
      series: [
        {
          type: useBar ? 'bar' : 'line',
          smooth: !useBar,
          barWidth: useBar ? '42%' : undefined,
          symbolSize: useBar ? 0 : 8,
          data: amounts,
          lineStyle: { width: 3, color: '#059669' },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#064e3b' },
              { offset: 1, color: '#10b981' }
            ]),
            borderRadius: useBar ? [8, 8, 0, 0] : 0
          },
          areaStyle: useBar
            ? undefined
            : {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(5, 150, 105, 0.26)' },
                  { offset: 1, color: 'rgba(5, 150, 105, 0.03)' }
                ])
              },
          label: {
            show: true,
            position: 'top',
            formatter: ({ dataIndex }) => `${counts[dataIndex]}笔`,
            color: '#059669',
            fontSize: 11
          }
        }
      ]
    },
    true
  )
}

const renderTransactionChart = () => {
  if (!transactionChartRef.value) {
    return
  }

  const data = [
    { name: '待确认', value: Number(overview.value.pendingTransactionCount || 0), color: '#f59e0b' },
    { name: '谈判中', value: Number(overview.value.negotiatingTransactionCount || 0), color: '#3b82f6' },
    { name: '已签约', value: Number(overview.value.signedTransactionCount || 0), color: '#0d9488' },
    { name: '已完成', value: Number(overview.value.completedTransactionCount || 0), color: '#059669' },
    { name: '已取消', value: Number(overview.value.cancelledTransactionCount || 0), color: '#94a3b8' }
  ].filter(item => item.value > 0)

  if (!data.length) {
    transactionChart?.clear()
    return
  }

  if (!transactionChart) {
    transactionChart = echarts.init(transactionChartRef.value)
  }

  transactionChart.setOption(
    {
      tooltip: {
        trigger: 'item',
        formatter: '{b}：{c} 笔 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '4%',
        top: 'center',
        textStyle: { color: '#475569', fontSize: 12 }
      },
      series: [
        {
          type: 'pie',
          radius: ['44%', '74%'],
          center: ['34%', '50%'],
          label: { show: false },
          data: data.map(item => ({
            name: item.name,
            value: item.value,
            itemStyle: { color: item.color, borderColor: '#fff', borderWidth: 2 }
          }))
        }
      ]
    },
    true
  )
}

const renderDistribution = () => {
  if (!distributionChartRef.value) {
    return
  }

  if (!districtComparisonData.value.length) {
    distributionChart?.clear()
    return
  }

  if (!distributionChart) {
    distributionChart = echarts.init(distributionChartRef.value)
  }

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
          radius: ['42%', '70%'],
          center: ['34%', '50%'],
          data: districtComparisonData.value.map(item => ({
            name: item.displayName,
            value: metricValue(item, distributionMetric.value)
          })),
          label: {
            formatter: params => `${params.name}\n${formatMetricValue(params.value)}`,
            color: '#334155',
            fontSize: 11,
            lineHeight: 18
          },
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 2
          },
          color: ['#064e3b', '#059669', '#10b981', '#34d399', '#f59e0b', '#a78bfa', '#f472b6']
        }
      ]
    },
    true
  )
}

const loadDashboard = async () => {
  loading.value = true
  error.value = ''

  try {
    const res = await request.get('/user/agent/dashboard', {
      params: { months: 6 }
    })

    dashboard.value = {
      ...dashboard.value,
      ...(res.data || {})
    }
  } catch (err) {
    error.value = err.message || '加载失败'
    console.error('加载中介业务看板失败:', err)
  } finally {
    loading.value = false
    await nextTick()
    renderTrend()
    renderTransactionChart()
    renderDistribution()
  }
}

const go = path => router.push(path)

// 显示发起带看对话框
const showBookViewingDialog = async () => {
  bookViewingVisible.value = true
  // 加载客户列表和房源列表
  await Promise.all([loadCustomerList(), loadMyHouseList()])
}

// 加载客户列表
const loadCustomerList = async () => {
  try {
    const res = await request.get('/user/customer/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    customerList.value = res.data?.list || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
    ElMessage.error('加载客户列表失败')
  }
}

// 加载我的房源列表
const loadMyHouseList = async () => {
  try {
    const res = await request.get('/house/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
    myHouseList.value = res.data?.list || []
  } catch (error) {
    console.error('加载房源列表失败:', error)
    ElMessage.error('加载房源列表失败')
  }
}

// 提交带看申请
const handleBookViewing = async () => {
  // 表单验证
  if (!bookForm.value.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!bookForm.value.houseId) {
    ElMessage.warning('请选择房源')
    return
  }
  if (!bookForm.value.appointTime) {
    ElMessage.warning('请选择预约时间')
    return
  }

  try {
    bookLoading.value = true
    const res = await request.post('/viewing/agent/book', bookForm.value)
    if (res.code === 200) {
      ElMessage.success('带看申请已发送，等待客户确认')
      bookViewingVisible.value = false
      // 重置表单
      bookForm.value = {
        customerId: null,
        houseId: null,
        appointTime: '',
        customerPhone: '',
        remark: ''
      }
      // 刷新数据
      await loadDashboard()
    }
  } catch (error) {
    console.error('发起带看失败:', error)
    ElMessage.error(error.response?.data?.message || '发起带看失败')
  } finally {
    bookLoading.value = false
  }
}

const setChartMode = async value => {
  chartMode.value = value
  await nextTick()
  renderTrend()
}

const setTrendWindow = async value => {
  trendWindow.value = value
  await nextTick()
  renderTrend()
}

const setDistributionMetric = async value => {
  distributionMetric.value = value
  await nextTick()
  renderDistribution()
}

const setActiveAgentMode = value => {
  activeAgentMode.value = value
}

const handleResize = debounce(() => {
  trendChart?.resize()
  transactionChart?.resize()
  distributionChart?.resize()
}, 180)

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  transactionChart?.dispose()
  distributionChart?.dispose()
})
</script>

<style scoped>
.agent-workspace {
  --el-color-primary: #059669;
  --el-color-primary-dark-2: #047857;
  --content-bg: #f0f2f5;
}

/* 消除 page-shell 带来的双层视觉效果 */
.agent-workspace :deep(.page-shell),
.agent-workspace :deep(.page-shell-hero),
.agent-workspace :deep(.page-shell-hero--split) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
  border: none !important;
}

/* 确保整个页面背景统一 */
.agent-workspace {
  background: #f0f2f5;
}

.agent-dashboard-page {
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

.insight-item.success {
  color: #047857;
  background: rgba(5, 150, 105, 0.12);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
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

.chart {
  width: 100%;
}

.trend-chart {
  height: 320px;
}

.transaction-chart,
.district-chart {
  height: 300px;
}

.pipeline-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.pipeline-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
}

.pipeline-card span {
  color: #607086;
  font-size: 13px;
}

.pipeline-card strong {
  font-size: 30px;
  color: #132238;
}

.pipeline-card small {
  color: #8b99ab;
}

.pipeline-card.warn {
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 100%);
}

.pipeline-card.info {
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
}

.pipeline-card.ok {
  background: linear-gradient(135deg, #ecfdf5 0%, #ffffff 100%);
}

.pipeline-card.muted {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.viewing-rate-card {
  margin-top: 18px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
}

.rate-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rate-head span {
  color: #607086;
  font-size: 13px;
}

.rate-head strong {
  color: #047857;
  font-size: 18px;
}

.rate-bar,
.progress-bar {
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: #e2e8f0;
}

.rate-fill,
.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #064e3b 0%, #059669 50%, #10b981 100%);
}

.viewing-rate-card p {
  margin: 10px 0 0;
  color: #607086;
  font-size: 13px;
}

.transaction-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.transaction-summary-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #475569;
  font-size: 13px;
}

.summary-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.summary-dot.warn {
  background: #f59e0b;
}

.summary-dot.info {
  background: #3b82f6;
}

.summary-dot.signed {
  background: #0d9488;
}

.summary-dot.ok {
  background: #059669;
}

.summary-dot.muted {
  background: #94a3b8;
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

.distribution-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  margin-bottom: 14px;
  color: #607086;
  font-size: 13px;
}

.distribution-info strong {
  color: #132238;
}

.district-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.district-card {
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
  text-align: center;
}

.district-card strong {
  color: #132238;
}

.district-card span {
  display: block;
  margin: 8px 0 6px;
  color: #047857;
  font-weight: 600;
}

.district-card small {
  color: #607086;
}

.execution-panel {
  display: flex;
  flex-direction: column;
}

.execution-list,
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
  padding-right: 4px;
}

.execution-list--fixed {
  --execution-window-height: 252px;
  flex: none;
  min-height: var(--execution-window-height);
  max-height: var(--execution-window-height);
  overflow-y: auto;
}

.execution-list::-webkit-scrollbar {
  width: 6px;
}

.execution-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.execution-list::-webkit-scrollbar-track {
  background: transparent;
}

.execution-empty-state {
  min-height: 100%;
  margin: 0;
  border-radius: 18px;
  border: 1px dashed rgba(148, 163, 184, 0.28);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96) 0%, rgba(241, 245, 249, 0.92) 100%);
}

.execution-item,
.rank-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: #f8fafc;
  transition: all 0.2s ease;
  cursor: pointer;
  flex-shrink: 0;
}

.execution-item:hover,
.rank-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
}

.execution-top,
.rank-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.execution-item strong,
.rank-info strong {
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

.execution-item small,
.rank-info small {
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

.active-rank-list .rank-item {
  background: linear-gradient(135deg, #f8fafc 0%, rgba(236, 253, 245, 0.82) 100%);
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

.rank-value {
  color: #132238;
}

.tips-panel {
  background: linear-gradient(135deg, rgba(6, 78, 59, 0.05) 0%, rgba(5, 150, 105, 0.05) 50%, rgba(16, 185, 129, 0.05) 100%);
  border: 1px solid rgba(5, 150, 105, 0.15);
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tips-list li {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.76);
  border-radius: 12px;
  color: #4b5563;
  line-height: 1.6;
}

.tip-mark {
  color: #059669;
  font-weight: 700;
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
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  .pipeline-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero,
  .district-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
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
  .agent-dashboard-page {
    padding: 16px;
  }

  .hero-content h1 {
    font-size: 28px;
  }

  .summary-grid,
  .quick-actions,
  .pipeline-grid,
  .district-grid {
    grid-template-columns: 1fr;
  }

  .insights-bar,
  .panel-header,
  .header-actions,
  .distribution-info {
    flex-direction: column;
    align-items: stretch;
  }

  .chart {
    height: 280px;
  }
}
</style>
