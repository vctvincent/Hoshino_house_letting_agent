# Admin 与 AgentWorkspace 看板修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 `Admin.vue` 的乱码与趋势图空白问题，并重构 `AgentWorkspace.vue` 为稳定可用的中介业务工作台。

**Architecture:** `Admin.vue` 采取保守修复，保持现有版式但拆开排行数据与趋势图数据请求，并统一清理文案与图表初始化逻辑。`AgentWorkspace.vue` 因源码损坏严重，优先重建稳定模板与脚本结构，再按“摘要、图表、推进、入口”四层重排页面内容，同时复用与 `Admin` 一致的图表渲染策略。

**Tech Stack:** Vue 3 `script setup`、Vite、ECharts、Element Plus、Axios

---

### Task 1: 修复 Admin 趋势图联动与渲染稳定性

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`
- Test: `E:\Graduation_thesis\workspace\reams-frontend\package.json`

- [ ] **Step 1: 写一个失败前提检查，确认当前趋势图逻辑耦合点**

在 `Admin.vue` 里先定位以下 4 个点并确认它们同时存在：

```js
watch(salesWindow, async () => {
  await loadDashboard()
})

watch(trendWindow, loadTrendData)

const loadDashboard = async () => {
  const res = await request.get('/admin/dashboard', { params: { months: salesWindow.value } })
}

const loadTrendData = async () => {
  const requiredMonths = Math.max(trendWindow.value, salesWindow.value)
  const res = await request.get('/admin/dashboard', { params: { months: requiredMonths } })
}
```

Run: `Select-String -Path 'E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue' -Pattern 'watch\\(salesWindow|watch\\(trendWindow|loadDashboard|loadTrendData'`

Expected: 能看到以上逻辑都在同一文件中存在，说明修复入口定位正确。

- [ ] **Step 2: 调整趋势图容器判定，避免 Top5 请求导致旧图表容器被销毁**

把模板中的趋势图 loading 判定保持为“只有没有旧趋势数据时才显示 loading”：

```vue
<div v-if="loading && !visibleMonthlySales.length" class="empty-state">
  <div class="spinner"></div>
  <p>数据加载中...</p>
</div>
```

不要改回下面这种会销毁已渲染图表的写法：

```vue
<div v-if="loading" class="empty-state">
```

- [ ] **Step 3: 在 renderTrend 中加入 DOM 失效重绑逻辑**

将 `renderTrend` 改成如下结构：

```js
const renderTrend = () => {
  if (!trendChartRef.value || !visibleMonthlySales.value.length) return

  if (trendChart && trendChart.getDom() !== trendChartRef.value) {
    trendChart.dispose()
    trendChart = null
  }

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value, undefined, { renderer: 'canvas' })
  }

  const data = visibleMonthlySales.value
  const months = data.map(item => month(item.month))
  const amounts = data.map(item => Number(item.salesAmount || 0))
  const counts = data.map(item => Number(item.transactionCount || 0))

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const idx = params[0].dataIndex
        return months[idx] + '<br/>成交额：' + money(amounts[idx]) + '<br/>成交笔数：' + counts[idx] + ' 笔'
      }
    },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [{
      type: chartMode.value === 'bar' ? 'bar' : 'line',
      data: amounts
    }]
  }, true)
}
```

- [ ] **Step 4: 保持排行与趋势图请求分离**

确认并保留下面的职责边界：

```js
const loadDashboard = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/dashboard', { params: { months: salesWindow.value } })
    const currentMonthlySales = dashboard.value.monthlySales
    dashboard.value = {
      ...dashboard.value,
      ...(res.data || {}),
      monthlySales: currentMonthlySales
    }
  } finally {
    loading.value = false
    await nextTick()
    renderPriceAnalysis()
    renderCityComparison()
    renderDistribution()
  }
}

const loadTrendData = async () => {
  const requiredMonths = Math.max(trendWindow.value, salesWindow.value)
  const res = await request.get('/admin/dashboard', { params: { months: requiredMonths } })
  if (res.data && res.data.monthlySales) {
    dashboard.value.monthlySales = res.data.monthlySales
    await nextTick()
    renderTrend()
  }
}
```

- [ ] **Step 5: 固定 watch 行为，禁止 Top5 时间切换刷新趋势图**

最终 watch 必须是：

```js
watch(salesWindow, async () => {
  await loadDashboard()
})

watch(trendWindow, loadTrendData)
```

不要保留这种错误联动：

```js
watch(salesWindow, async () => {
  await loadDashboard()
  await loadTrendData()
})
```

- [ ] **Step 6: 运行构建验证趋势图修复没有引入编译错误**

Run: `npm run build`

Workdir: `E:\Graduation_thesis\workspace\reams-frontend`

Expected: `vite build` 成功，没有 `Admin.vue` 模板或脚本错误。

- [ ] **Step 7: Commit**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "fix: stabilize admin trend chart rendering"
```

---

### Task 2: 清理 Admin 可见乱码与图表展示文案

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`
- Test: `E:\Graduation_thesis\workspace\reams-frontend\package.json`

- [ ] **Step 1: 清理模板首屏和主区中的乱码文案**

把模板中的可见中文恢复为正常中文，至少覆盖以下片段：

```vue
<h1>系统经营看板</h1>
<p class="desc">支持按省份下钻到城市与区县分布，并提供指标切换与时间筛选，让重点经营信息一眼看清。</p>
<span>经营预警</span>
<h2>近 {{ trendWindow }} 个月成交趋势</h2>
<h2>城市价格段成交分析</h2>
<h2>城市成交多维对比</h2>
<h2>房源分布（区县）</h2>
```

- [ ] **Step 2: 清理右侧排行和空态文案**

模板里的右侧模块需要至少包含以下正常文案：

```vue
<h2>{{ selectedCity || selectedProvince || '全区域' }} 成交 Top 5</h2>
<p class="panel-subtitle">成交排行按 {{ salesWindowLabel }} 统计</p>
<p>暂无区县排行数据</p>
<h2>头部中介 Top 5</h2>
<span class="panel-badge">按 {{ salesWindowLabel }} 成交额</span>
<p>暂无中介排行数据</p>
<h2>交易流程分布</h2>
<p>暂无交易流程数据</p>
<h2>经营提示</h2>
```

- [ ] **Step 3: 清理脚本中的中文常量与提示文案**

脚本中至少要修正以下常量与文案：

```js
const shortcuts = [
  { title: '房源审核', description: '快速处理待审核房源', path: '/layout/audit/house', icon: 'icon-house' },
  { title: '员工管理', description: '管理系统员工账号', path: '/layout/user/staff', icon: 'icon-staff' }
]

const STATUS_MAP = {
  0: '待确认',
  1: '谈判中',
  2: '已签约',
  3: '已完成',
  4: '已取消'
}

const metricLabel = computed(() => METRICS.find(item => item.value === distributionMetric.value)?.label || '库存套数')
```

- [ ] **Step 4: 清理图表 tooltip、坐标轴和 legend 文案**

至少把以下内容改成正常中文：

```js
legend: { data: ['库存量', '成交量'] }

formatter: (params) => {
  const item = data[params[0].dataIndex]
  return item.city + '<br/>' + comparisonLabel.value + '：' + formatValue(item[metric]) +
    '<br/>成交量：' + item.transactionCount + ' 笔<br/>流动性：' + item.turnoverRate + '%'
}

name: '库存量（套）'
name: '成交量（套）'
```

- [ ] **Step 5: 运行构建并确认 Admin 不再有模板乱码造成的语法问题**

Run: `npm run build`

Workdir: `E:\Graduation_thesis\workspace\reams-frontend`

Expected: 构建成功，并且 `Admin.*.js`/`Admin.*.css` 正常产出。

- [ ] **Step 6: Commit**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "fix: clean admin dashboard copy and labels"
```

---

### Task 3: 重建 AgentWorkspace 的稳定模板与脚本基础

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\agent\AgentWorkspace.vue`
- Test: `E:\Graduation_thesis\workspace\reams-frontend\package.json`

- [ ] **Step 1: 用完整稳定的模板骨架替换损坏片段**

把 `AgentWorkspace.vue` 的模板重新整理为以下顶层结构：

```vue
<template>
  <div class="page">
    <div v-if="error" class="error-banner">
      <i class="icon-error"></i>
      <span>{{ error }}</span>
      <button @click="loadDashboard">重试</button>
    </div>

    <section class="hero">
      <div class="hero-content">
        <p class="kicker">Business Dashboard</p>
        <h1>业务看板</h1>
        <p class="desc">把成交趋势、交易分布、带看推进和库存分布收敛到一个工作台里，方便中介快速判断下一步重点。</p>
      </div>

      <div class="actions">
        <button
          v-for="item in shortcuts"
          :key="item.title"
          class="action-card"
          @click="go(item.path)"
        >
          <i :class="item.icon"></i>
          <div class="action-info">
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </div>
          <i class="icon-arrow">→</i>
        </button>
      </div>
    </section>

    <section class="metrics">
      <article v-for="card in summaryCards" :key="card.key" :class="['metric-card', card.tone]">
        <div class="metric-header">
          <span class="metric-label">{{ card.label }}</span>
          <span :class="['metric-badge', card.tone]">{{ card.badge }}</span>
        </div>
        <div class="metric-body">
          <h3>{{ card.value }}</h3>
          <div v-if="card.trend !== undefined" class="metric-trend">
            <span :class="card.trend >= 0 ? 'up' : 'down'">{{ card.trend >= 0 ? '+' : '' }}{{ card.trend }}%</span>
          </div>
        </div>
        <p class="metric-note">{{ card.note }}</p>
      </article>
    </section>

    <section class="grid">
      <div class="main"></div>
      <aside class="side"></aside>
    </section>
  </div>
</template>
```

- [ ] **Step 2: 重建脚本常量和基础状态，去掉乱码与坏字符串**

脚本开头至少恢复成下面这类稳定结构：

```js
import { computed, nextTick, onMounted, onUnmounted, ref, watch, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/api'

const SALES_WINDOWS = [{ label: '本月', value: 1 }, { label: '近3月', value: 3 }, { label: '近6月', value: 6 }]
const TREND_WINDOWS = [{ label: '近3月', value: 3 }, { label: '近6月', value: 6 }]
const METRICS = [
  { label: '库存套数', value: 'houseCount' },
  { label: '在售套数', value: 'publishedCount' },
  { label: '平均总价', value: 'averagePrice' },
  { label: '平均单价', value: 'averageUnitPrice' }
]

const VIEWING_STATUS_MAP = {
  0: '待确认',
  1: '已确认',
  2: '已完成',
  3: '已取消'
}

const router = useRouter()
const loading = ref(false)
const error = ref(null)
const chartMode = ref('line')
const trendWindow = ref(6)
const salesWindow = ref(6)
const distributionMetric = ref('houseCount')
const selectedProvince = ref('')
```

- [ ] **Step 3: 先恢复工作台数据容器与快捷入口配置**

先把 `dashboard` 和 `shortcuts` 整成可用的正常结构：

```js
const dashboard = shallowRef({
  overview: {},
  monthlySales: [],
  districtSales: [],
  inventoryDistribution: [],
  viewingStatusDistribution: [],
  recentReviews: []
})

const shortcuts = [
  {
    title: '房源管理',
    description: '管理发布、编辑与审核状态',
    path: '/layout/house/manage',
    icon: 'icon-house'
  },
  {
    title: '带看管理',
    description: '推进预约、完成与评价邀请',
    path: '/layout/viewing-manage',
    icon: 'icon-viewing'
  },
  {
    title: '交易管理',
    description: '跟进谈判、签约和成交流程',
    path: '/layout/transaction/manage',
    icon: 'icon-transaction'
  }
]
```

- [ ] **Step 4: 运行构建，确认 AgentWorkspace 已恢复为可编译状态**

Run: `npm run build`

Workdir: `E:\Graduation_thesis\workspace\reams-frontend`

Expected: 不再出现 `AgentWorkspace.vue` 的模板解析错误、属性字符串错误或脚本语法错误。

- [ ] **Step 5: Commit**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/agent/AgentWorkspace.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "refactor: rebuild agent workspace foundation"
```

---

### Task 4: 按新结构重排 AgentWorkspace 业务模块

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\agent\AgentWorkspace.vue`
- Test: `E:\Graduation_thesis\workspace\reams-frontend\package.json`

- [ ] **Step 1: 实现顶部业务摘要卡片**

新增或修复 `summaryCards`：

```js
const summaryCards = computed(() => {
  const ov = dashboard.value.overview
  return [
    {
      key: 'sales',
      label: '本月成交额',
      value: money(ov.currentMonthSalesAmount),
      note: '本月完成 ' + (ov.currentMonthCompletedCount || 0) + ' 笔交易',
      badge: '累计 ' + money(ov.completedSalesAmount),
      tone: 'ok',
      trend: ov.salesGrowthRate || 0
    },
    {
      key: 'viewings',
      label: '已完成带看',
      value: String(ov.completedViewingCount || 0),
      note: '待确认 ' + (ov.pendingViewingCount || 0) + ' 次',
      badge: '已确认 ' + (ov.confirmedViewingCount || 0) + ' 次',
      tone: 'ok'
    },
    {
      key: 'transactions',
      label: '待跟进交易',
      value: String(ov.pendingTransactionCount || 0),
      note: '谈判中 ' + (ov.negotiatingTransactionCount || 0) + ' 笔',
      badge: '已签约 ' + (ov.signedTransactionCount || 0) + ' 笔',
      tone: Number(ov.pendingTransactionCount || 0) > 0 ? 'warn' : 'muted'
    },
    {
      key: 'rating',
      label: '服务评分',
      value: Number(ov.avgRating || 0).toFixed(1),
      note: '最近评价 ' + (dashboard.value.recentReviews || []).length + ' 条',
      badge: '带看转化 ' + viewingConversionRate.value + '%',
      tone: 'muted'
    }
  ]
})
```

- [ ] **Step 2: 实现核心图表区，左侧趋势图右侧交易分布**

模板主区写成：

```vue
<section class="grid">
  <div class="main">
    <article class="panel">
      <header class="panel-header">
        <div class="panel-title">
          <p class="kicker">My Sales Trend</p>
          <h2>近 {{ trendWindow }} 个月成交趋势</h2>
        </div>
        <div class="panel-controls">
          <div class="btn-group">
            <button
              v-for="item in TREND_WINDOWS"
              :key="item.value"
              :class="{ active: trendWindow === item.value }"
              @click="setTrendWindow(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>
      </header>
      <div v-if="loading && !visibleMonthlySales.length" class="empty-state">
        <div class="spinner"></div>
        <p>数据加载中...</p>
      </div>
      <div v-else-if="!visibleMonthlySales.length" class="empty-state">
        <i class="icon-empty"></i>
        <p>暂无成交趋势数据</p>
      </div>
      <div v-else ref="trendChartRef" class="chart trend-chart"></div>
    </article>

    <article class="panel">
      <header class="panel-header">
        <div class="panel-title">
          <p class="kicker">Transaction Status</p>
          <h2>交易状态分布</h2>
        </div>
      </header>
      <div ref="transactionChartRef" class="chart transaction-chart"></div>
    </article>
  </div>
  <aside class="side"></aside>
</section>
```

- [ ] **Step 3: 实现业务推进区和库存分布**

模板继续补齐：

```vue
<article class="panel">
  <header class="panel-header">
    <div class="panel-title">
      <p class="kicker">Viewing Pipeline</p>
      <h2>带看推进情况</h2>
    </div>
  </header>
  <div class="viewing-pipeline">
    <div class="pipeline-step">
      <div class="step-info">
        <strong>{{ dashboard.overview.pendingViewingCount || 0 }}</strong>
        <span>待确认</span>
      </div>
    </div>
    <div class="pipeline-step">
      <div class="step-info">
        <strong>{{ dashboard.overview.confirmedViewingCount || 0 }}</strong>
        <span>已确认</span>
      </div>
    </div>
    <div class="pipeline-step highlight">
      <div class="step-info">
        <strong>{{ dashboard.overview.completedViewingCount || 0 }}</strong>
        <span>已完成</span>
      </div>
    </div>
  </div>
</article>

<article class="panel">
  <header class="panel-header">
    <div class="panel-title">
      <p class="kicker">My Inventory</p>
      <h2>库存与区域分布</h2>
    </div>
  </header>
  <div v-if="!provinceSummary.length" class="empty-state">
    <i class="icon-empty"></i>
    <p>暂无区域库存数据</p>
  </div>
  <template v-else>
    <div class="province-chips">
      <button
        v-for="item in provinceSummary"
        :key="item.province"
        :class="['chip', { active: selectedProvince === item.province }]"
        @click="selectedProvince = item.province"
      >
        <span class="chip-name">{{ item.province }}</span>
        <span class="chip-count">{{ item.houseCount }}套</span>
      </button>
    </div>
    <div ref="distributionChartRef" class="chart donut-chart"></div>
  </template>
</article>
```

- [ ] **Step 4: 实现侧栏，仅保留三项快捷入口和提醒**

侧栏结构至少保留：

```vue
<aside class="side">
  <article class="panel">
    <header class="panel-header">
      <div class="panel-title">
        <p class="kicker">Quick Actions</p>
        <h2>快捷入口</h2>
      </div>
    </header>
    <div class="action-stack">
      <button
        v-for="item in shortcuts"
        :key="item.title"
        class="action-card vertical"
        @click="go(item.path)"
      >
        <strong>{{ item.title }}</strong>
        <span>{{ item.description }}</span>
      </button>
    </div>
  </article>

  <article class="panel tips-panel">
    <header class="panel-header">
      <div class="panel-title">
        <p class="kicker">Highlights</p>
        <h2>今日提醒</h2>
      </div>
    </header>
    <ul class="tips-list">
      <li v-for="(item, index) in normalInsights" :key="index">
        <i :class="item.icon || 'icon-tip'"></i>
        <span>{{ item.message }}</span>
      </li>
    </ul>
  </article>
</aside>
```

- [ ] **Step 5: 运行构建验证新的 AgentWorkspace 结构**

Run: `npm run build`

Workdir: `E:\Graduation_thesis\workspace\reams-frontend`

Expected: `AgentWorkspace.*.js` 与对应 CSS 正常产出，无模板结构错误。

- [ ] **Step 6: Commit**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/agent/AgentWorkspace.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "refactor: reorganize agent business workspace"
```

---

### Task 5: 统一两页样式细节并做最终验证

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\agent\AgentWorkspace.vue`
- Test: `E:\Graduation_thesis\workspace\reams-frontend\package.json`

- [ ] **Step 1: 统一卡片、标题、副标题和空态样式**

两页至少对齐以下样式语义：

```css
.panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.panel-title h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.empty-state {
  display: flex;
  min-height: 220px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #6b7280;
}
```

- [ ] **Step 2: 统一按钮和快捷入口样式，但保留 AgentWorkspace 的三入口结构**

两页的快捷入口都使用同一套基础风格：

```css
.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #ffffff;
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}
```

- [ ] **Step 3: 最终运行构建**

Run: `npm run build`

Workdir: `E:\Graduation_thesis\workspace\reams-frontend`

Expected: 整个前端构建成功。

- [ ] **Step 4: 记录人工验收点**

人工确认以下结果：

```text
1. Admin 页面不再出现“裸字乱码”。
2. Admin 中切换“本月 / 近3月 / 近6月”排行时，趋势图不消失。
3. AgentWorkspace 页面中文正常显示。
4. AgentWorkspace 首页结构变为摘要、图表、推进、入口四层。
5. AgentWorkspace 快捷入口只保留：房源管理、带看管理、交易管理。
```

- [ ] **Step 5: Commit**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue src/views/agent/AgentWorkspace.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "style: align admin and agent dashboard details"
```
