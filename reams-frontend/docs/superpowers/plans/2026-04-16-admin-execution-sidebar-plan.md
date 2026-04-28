# Admin Execution Sidebar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the admin dashboard right sidebar with execution-focused admin modules for house review, agent review, recent deals, and quick shortcuts while keeping the existing analytics panels intact.

**Architecture:** Keep all changes inside `src/views/admin/Admin.vue` so the admin dashboard remains a single self-contained Vue SFC. Reuse the existing `/admin/dashboard` response as the primary data source, add computed compatibility layers for optional list data, and replace only the right-column content plus matching helper logic and styles.

**Tech Stack:** Vue 3 `script setup`, Vue Router, Axios request wrapper, ECharts, Vite

---

### Task 1: Replace right-column structure in the admin template

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] **Step 1: Replace the four existing right-column display modules in the template**

Replace the current right sidebar sections for city leaderboard, top agents, transaction status, and highlights with four new sections:
- `房源审核待办`
- `中介注册审核`
- `最近成交动态`
- `快捷处理`

Use this template shape inside the existing `<aside class="side-column">` block:

```vue
<aside class="side-column">
  <article class="panel priority-panel">
    <header class="panel-header">
      <div>
        <p class="panel-kicker">House Review Queue</p>
        <h2>房源审核待办</h2>
      </div>
      <span class="panel-badge">{{ formatNumber(houseReviewSummary.count) }} 套</span>
    </header>

    <p class="panel-subtitle">优先处理最近提交的待审核房源</p>

    <div v-if="houseReviewItems.length" class="todo-list">
      <article v-for="item in houseReviewItems" :key="item.id || item.title" class="todo-item">
        <strong>{{ item.title || '待审核房源' }}</strong>
        <small>{{ item.region || '区域待补充' }} · {{ item.timeText }}</small>
        <span>{{ item.owner || '提交人待确认' }}</span>
      </article>
    </div>
    <div v-else class="empty-state compact">
      <p>当前没有待审核房源</p>
    </div>

    <button type="button" class="panel-link" @click="go('/layout/audit/house')">去审核房源</button>
  </article>

  <article class="panel">
    <header class="panel-header">
      <div>
        <p class="panel-kicker">Agent Review Queue</p>
        <h2>中介注册审核</h2>
      </div>
      <span class="panel-badge">{{ formatNumber(agentReviewSummary.count) }} 人</span>
    </header>

    <div v-if="agentReviewItems.length" class="todo-list">
      <article v-for="item in agentReviewItems" :key="item.id || item.name" class="todo-item">
        <strong>{{ item.name || '待审核中介' }}</strong>
        <small>{{ item.account || item.phone || '账号待补充' }}</small>
        <span>{{ item.timeText }}</span>
      </article>
    </div>
    <div v-else class="empty-state compact">
      <p>当前没有待审核中介申请</p>
    </div>

    <button type="button" class="panel-link" @click="go('/layout/user/staff')">去审核中介</button>
  </article>

  <article class="panel">
    <header class="panel-header">
      <div>
        <p class="panel-kicker">Recent Deals</p>
        <h2>最近成交动态</h2>
      </div>
      <span class="panel-badge">近7天 {{ formatNumber(recentDealSummary.count) }} 单</span>
    </header>

    <div v-if="recentDealItems.length" class="deal-list">
      <article v-for="item in recentDealItems" :key="item.id || item.transactionNo" class="deal-item">
        <strong>{{ item.houseTitle || item.transactionNo || '最近成交' }}</strong>
        <small>{{ item.amountText }} · {{ item.timeText }}</small>
        <span>{{ item.agentName || item.customerName || '交易参与方待补充' }}</span>
      </article>
    </div>
    <div v-else class="empty-state compact">
      <p>近期暂无成交记录</p>
    </div>
  </article>

  <article class="panel highlight-panel">
    <header class="panel-header">
      <div>
        <p class="panel-kicker">Quick Actions</p>
        <h2>快捷处理</h2>
      </div>
    </header>

    <div class="quick-shortcut-list">
      <button v-for="item in adminShortcuts" :key="item.title" type="button" class="mini-shortcut" @click="go(item.path)">
        <span class="mini-shortcut-mark">{{ item.mark }}</span>
        <div>
          <strong>{{ item.title }}</strong>
          <small>{{ item.description }}</small>
        </div>
      </button>
    </div>
  </article>
</aside>
```

- [ ] **Step 2: Remove old template references that will no longer exist**

Delete template usage of:

```vue
citySalesLeaderboard
topAgentsCompact
statusDistribution
normalInsights
```

Expected result: the template no longer references removed right-column ranking modules.

- [ ] **Step 3: Run a text check to confirm removed bindings are gone from the template section**

Run:

```powershell
Select-String -Path 'E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue' -Pattern 'citySalesLeaderboard|topAgentsCompact|statusDistribution|normalInsights'
```

Expected: matches may remain in script temporarily, but no remaining usage inside the `<aside class="side-column">` template block.

- [ ] **Step 4: Commit the template replacement**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "feat: replace admin sidebar rankings with execution panels"
```

### Task 2: Add computed admin execution data with compatibility fallbacks

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] **Step 1: Extend the dashboard shape with optional admin execution arrays**

Update the initial `dashboard` state object to include optional list fields:

```js
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
```

- [ ] **Step 2: Add reusable time formatting helpers for sidebar list rows**

Add these helpers near the existing formatting functions:

```js
const formatDate = value => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = value => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return `${date.toLocaleDateString('zh-CN')} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
}
```

- [ ] **Step 3: Add computed summaries and item lists for the three new execution panels**

Create these computed properties, adapting field names defensively:

```js
const houseReviewItems = computed(() => asList(dashboard.value.pendingHouseReviews).slice(0, 5).map(item => ({
  id: item.id || item.houseId || item.auditId,
  title: item.title || item.houseTitle || '待审核房源',
  region: [item.province, item.city, item.district].filter(Boolean).join(' '),
  owner: item.agentName || item.publisherName || item.ownerName || '提交人待确认',
  timeText: formatDateTime(item.createTime || item.submitTime || item.updateTime)
})))

const houseReviewSummary = computed(() => ({
  count: Number(dashboard.value.overview?.pendingAuditCount || houseReviewItems.value.length || 0)
}))

const agentReviewItems = computed(() => asList(dashboard.value.pendingAgentReviews).slice(0, 5).map(item => ({
  id: item.id || item.userId || item.agentId,
  name: item.name || item.realName || item.agentName || '待审核中介',
  account: item.account || item.username || item.phone || '',
  phone: item.phone || '',
  timeText: formatDateTime(item.createTime || item.applyTime || item.submitTime)
})))

const agentReviewSummary = computed(() => ({
  count: Number(dashboard.value.overview?.pendingAgentReviewCount || agentReviewItems.value.length || 0)
}))

const recentDealItems = computed(() => asList(dashboard.value.recentDeals).slice(0, 5).map(item => ({
  id: item.id || item.transactionId,
  transactionNo: item.transactionNo || item.orderNo || '',
  houseTitle: item.houseTitle || item.title || '最近成交',
  agentName: item.agentName || '',
  customerName: item.customerName || '',
  amountText: money(item.finalPrice || item.salesAmount || item.amount || 0),
  timeText: formatDateTime(item.dealDate || item.createTime || item.updateTime)
})))

const recentDealSummary = computed(() => ({
  count: Number(dashboard.value.overview?.recentDealCount || recentDealItems.value.length || 0)
}))
```

- [ ] **Step 4: Replace the old shortcut data source with admin-specific quick shortcuts**

Update the shortcut definition to this exact shape:

```js
const adminShortcuts = [
  { title: '房源审核', description: '处理待审核房源', path: '/layout/audit/house', mark: '审' },
  { title: '员工管理', description: '查看员工与中介信息', path: '/layout/user/staff', mark: '员' },
  { title: '交易记录', description: '进入交易管理列表', path: '/layout/transaction/manage', mark: '交' }
]
```

Then update the hero quick action template loop to use `adminShortcuts`.

- [ ] **Step 5: Remove obsolete computed values that only supported the old sidebar**

Delete these computed blocks if they are no longer used anywhere:

```js
topAgentsCompact
statusDistribution
citySalesLeaderboard
allInsights
normalInsights
```

Keep `urgentInsights` only if the top warning strip still uses it. If `urgentInsights` depends on `allInsights`, replace it with a direct computed source such as `adminAlerts`.

- [ ] **Step 6: Run a text check to confirm the new sidebar data layer exists**

Run:

```powershell
Select-String -Path 'E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue' -Pattern 'houseReviewItems|agentReviewItems|recentDealItems|adminShortcuts|recentDealSummary'
```

Expected: all five identifiers are present.

- [ ] **Step 7: Commit the data-layer refactor**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "feat: add admin execution sidebar data models"
```

### Task 3: Restyle the new admin execution panels

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] **Step 1: Add styles for the new list-based execution cards**

Append or replace scoped CSS with these blocks:

```css
.priority-panel {
  border: 1px solid rgba(245, 158, 11, 0.18);
  box-shadow: 0 18px 38px rgba(245, 158, 11, 0.08);
}

.todo-list,
.deal-list,
.quick-shortcut-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item,
.deal-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #f8fafc;
}

.todo-item strong,
.deal-item strong {
  color: #132238;
  font-size: 14px;
}

.todo-item small,
.deal-item small,
.todo-item span,
.deal-item span {
  color: #607086;
  font-size: 12px;
  line-height: 1.5;
}

.panel-link {
  width: 100%;
  margin-top: 14px;
  border: 0;
  border-radius: 14px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #064e3b 0%, #059669 100%);
  color: #fff;
  cursor: pointer;
}

.mini-shortcut {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 0;
  border-radius: 18px;
  padding: 14px 16px;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
}

.mini-shortcut-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
  font-weight: 700;
}

.mini-shortcut small {
  display: block;
  margin-top: 4px;
  color: #607086;
}
```

- [ ] **Step 2: Remove styles that only existed for ranking/status modules if no longer used**

Delete obsolete CSS blocks for:

```css
.rank-list
.rank-item
.rank-info
.rank-num
.rank-value
.status-list
.status-item
.status-top
```

Only remove them if no remaining template elements rely on them.

- [ ] **Step 3: Update responsive behavior for the new execution cards**

Ensure the mobile media query still works with the new right-column components by keeping:

```css
@media (max-width: 768px) {
  .quick-shortcut-list,
  .todo-list,
  .deal-list {
    gap: 10px;
  }
}
```

- [ ] **Step 4: Commit the style changes**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "style: align admin execution sidebar with workspace design"
```

### Task 4: Verify navigation and build output

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] **Step 1: Check that the three quick-entry routes are hard-coded correctly**

Run:

```powershell
Select-String -Path 'E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue' -Pattern '/layout/audit/house|/layout/user/staff|/layout/transaction/manage'
```

Expected: all three route strings appear in the shortcut definitions or buttons.

- [ ] **Step 2: Build the frontend to verify the page compiles**

Run:

```powershell
npm run build
```

Workdir:

```text
E:\Graduation_thesis\workspace\reams-frontend
```

Expected: Vite build completes successfully and emits an `assets/Admin.*` bundle.

- [ ] **Step 3: Confirm the new panel identifiers exist in the built source file**

Run:

```powershell
Select-String -Path 'E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue' -Pattern '房源审核待办|中介注册审核|最近成交动态|快捷处理'
```

Expected: all four headings are present.

- [ ] **Step 4: Commit the verified implementation**

```bash
git -C E:\Graduation_thesis\workspace\reams-frontend add src/views/admin/Admin.vue
git -C E:\Graduation_thesis\workspace\reams-frontend commit -m "feat: convert admin sidebar into execution workspace"
```
