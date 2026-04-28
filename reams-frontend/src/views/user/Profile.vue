<template>
  <div :class="['profile-page', { 'profile-page--dashboard': isBackofficeProfile }]">
    <!-- ============ Dashboard Style Hero Section ============ -->
    <section :class="['hero-section', { 'hero-section--dashboard': isBackofficeProfile }]">
      <div :class="['hero-content', { 'hero-content--dashboard': isBackofficeProfile }]">
        <div class="hero-text-group">
          <p class="eyebrow">User Profile Center</p>
          <h1 class="hero-title">个人资料</h1>
          <p class="hero-desc">管理您的个人信息、收藏、交易和带看记录。</p>
        </div>

        <div :class="['hero-user-card', { 'hero-user-card--dashboard': isBackofficeProfile }]">
          <div class="avatar-container" @click="triggerAvatarUpload">
            <img :src="avatarUrl" alt="用户头像" class="avatar-img" />
            <div class="avatar-overlay">
              <span>更换</span>
            </div>
          </div>
          <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarUpload"
          />
          <div class="user-meta">
            <h2 class="user-name">{{ displayName }}</h2>
            <p class="user-contact">{{ profileForm.phone || userInfo?.phone || '未绑定手机号' }}</p>
            <p v-if="userInfo?.email" class="user-email">{{ userInfo.email }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ Main Content Area ============ -->
    <section :class="['profile-main', { 'profile-main--dashboard': isBackofficeProfile }]">

      <!-- ===== 瀹㈡埛锛氬乏渚у鑸?+ 鍙充晶鍐呭 ===== -->
      <el-row v-if="role === 'ROLE_CUSTOMER'" :gutter="20">
        <el-col :span="6">
          <el-card class="nav-card" shadow="hover">
            <el-menu
                :default-active="activeTab"
                class="profile-nav"
                @select="handleMenuSelect"
            >
              <el-menu-item index="profile">
                <span class="nav-icon">👤</span>
                <span>个人资料</span>
              </el-menu-item>
              <el-menu-item index="favorites">
                <span class="nav-icon">⭐</span>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="transactions">
                <span class="nav-icon">💵</span>
                <span>我的交易</span>
              </el-menu-item>
              <el-menu-item index="viewings">
                <span class="nav-icon">🏠</span>
                <span>我的带看</span>
              </el-menu-item>
            </el-menu>

            <el-divider />

            <div class="stat-grid">
              <div class="stat-box">
                <div class="stat-value">{{ favorites.length }}</div>
                <div class="stat-label">收藏</div>
              </div>
              <div class="stat-box">
                <div class="stat-value">{{ transactions.length }}</div>
                <div class="stat-label">交易</div>
              </div>
              <div class="stat-box">
                <div class="stat-value">{{ viewings.length }}</div>
                <div class="stat-label">带看</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="18">
          <!-- 涓汉璧勬枡 -->
          <el-card v-show="activeTab === 'profile'" class="form-card" shadow="hover">
            <template #header>
              <div class="card-header-bar">
                <span class="card-header-icon">👤</span>
                <span class="card-header-text">个人资料</span>
              </div>
            </template>
            <el-form
                :model="profileForm"
                :rules="profileRules"
                ref="profileFormRef"
                label-width="90px"
                class="profile-form"
            >
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saveLoading" @click="handleSave" class="submit-btn">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <div class="card-header-bar" style="margin-bottom: 24px">
              <span class="card-header-icon">🔒</span>
              <span class="card-header-text">修改密码</span>
            </div>
            <el-form
                :model="passwordForm"
                :rules="passwordRules"
                ref="passwordFormRef"
                label-width="90px"
                class="profile-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" :loading="passwordLoading" @click="handleChangePassword" class="submit-btn">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 鎴戠殑鏀惰棌 -->
          <el-card v-show="activeTab === 'favorites'" class="form-card" shadow="hover">
            <template #header>
              <div class="card-header-bar">
                <span class="card-header-icon">⭐</span>
                <span class="card-header-text">我的收藏</span>
              </div>
            </template>
            <div class="favorites-grid" v-loading="favoritesLoading">
              <div v-for="item in favorites" :key="item.id" class="fav-card" @click="viewHouseDetail(item.houseId)">
                <div class="fav-img">
                  <img :src="buildImageUrl(item.houseImage)" alt="房源图片" />
                </div>
                <div class="fav-info">
                  <h4 class="fav-title">{{ item.houseTitle }}</h4>
                  <div class="fav-price">¥{{ item.housePrice }}万元</div>
                  <div class="fav-detail">{{ item.houseType || '未知户型' }} · {{ item.houseArea }}㎡ · {{ item.houseAddress }}</div>
                </div>
                <div class="fav-actions">
                  <el-button type="primary" size="small" round @click.stop="viewHouseDetail(item.houseId)">查看</el-button>
                  <el-button type="danger" size="small" round plain @click.stop="removeFavorite(item.id)">取消</el-button>
                </div>
              </div>
            </div>
            <el-empty v-if="!favoritesLoading && favorites.length === 0" description="暂无收藏房源" />
          </el-card>

          <!-- 鎴戠殑浜ゆ槗 -->
          <el-card v-show="activeTab === 'transactions'" class="form-card" shadow="hover">
            <template #header>
              <div class="card-header-bar">
                <span class="card-header-icon">馃挵</span>
                <span class="card-header-text">我的交易</span>
              </div>
            </template>
            <el-table
                :data="transactions"
                border
                style="width: 100%"
                v-loading="transactionsLoading"
                :header-cell-style="{ background: '#ecfdf5', color: '#1f2937', fontWeight: 'bold' }"
                stripe
            >
              <el-table-column prop="transactionNo" label="交易单号" width="180" show-overflow-tooltip />
              <el-table-column prop="houseTitle" label="房源" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                  <span class="table-link" @click="viewHouseDetail(row.houseId)">{{ row.houseTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="agentName" label="中介" width="100" />
              <el-table-column prop="finalPrice" label="成交价(万元)" width="110">
                <template #default="{ row }">
                  <span style="color: #059669; font-weight: 700">{{ row.finalPrice || '面议' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="dealDate" label="成交日期" width="120">
                <template #default="{ row }">{{ row.dealDate || '-' }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0">待确认</el-tag>
                  <el-tag v-else-if="row.status === 1" type="warning">谈判中</el-tag>
                  <el-tag v-else-if="row.status === 2" type="primary">已签约</el-tag>
                  <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
                  <el-tag v-else-if="row.status === 4" type="info">已取消</el-tag>
                  <el-tag v-else type="info">未知</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="鎿嶄綔" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="viewTransactionDetail(row)">详情</el-button>
                  <el-button
                      v-if="row.status === 0"
                      size="small"
                      type="success"
                      link
                      @click="confirmTransaction(row)"
                  >确认交易</el-button>
                  <el-button
                      v-if="row.status === 2"
                      size="small"
                      type="warning"
                      link
                      @click="openUploadDialog(row)"
                  >上传合同</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!transactionsLoading && transactions.length === 0" description="暂无交易记录" />
          </el-card>

          <!-- 鎴戠殑甯︾湅 -->
          <el-card v-show="activeTab === 'viewings'" class="form-card" shadow="hover">
            <template #header>
              <div class="card-header-bar">
                <span class="card-header-icon">馃搵</span>
                <span class="card-header-text">我的带看记录</span>
              </div>
            </template>
            <el-table
                :data="viewings"
                border
                style="width: 100%"
                v-loading="viewingsLoading"
                :header-cell-style="{ background: '#ecfdf5', color: '#1f2937', fontWeight: 'bold' }"
                stripe
            >
              <el-table-column prop="houseTitle" label="房源" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                  <span class="table-link" @click="viewHouseDetail(row.houseId)">{{ row.houseTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="houseAddress" label="地址" min-width="180" show-overflow-tooltip />
              <el-table-column prop="appointTime" label="预约时间" width="160">
                <template #default="{ row }">{{ formatTime(row.appointTime) }}</template>
              </el-table-column>
              <el-table-column prop="agentName" label="中介" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="viewingStatusConfig[row.status]?.type || 'info'" effect="plain" size="small">
                    {{ viewingStatusConfig[row.status]?.text || '未知' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="鎿嶄綔" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="handleViewDetail(row)">详情</el-button>
                  <el-button
                      v-if="row.status === VIEWING_STATUS.PENDING || row.status === VIEWING_STATUS.CONFIRMED"
                      size="small"
                      type="danger"
                      link
                      @click="handleCancelViewing(row)"
                  >取消</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!viewingsLoading && viewings.length === 0" description="暂无带看记录" />
          </el-card>
        </el-col>
      </el-row>

      <!-- ===== 绠＄悊鍛?/ 涓粙锛氭innerText?===== -->
      <el-row v-else :gutter="0" class="profile-shell-row profile-shell-row--dashboard">
        <el-col :span="24">
          <div class="dashboard-profile-stack">
            <el-card :class="['form-card', 'form-card--dashboard', 'dashboard-panel-card']" shadow="hover">
              <template #header>
                <div class="card-header-bar">
                  <span class="card-header-icon">👤</span>
                  <span class="card-header-text">个人资料</span>
                </div>
              </template>
              <div class="dashboard-card-body">
                <div class="dashboard-form-shell">
                  <el-form
                      :model="profileForm"
                      :rules="profileRules"
                      ref="profileFormRef"
                      label-width="90px"
                      class="profile-form profile-form--dashboard-shell"
                  >
                    <el-form-item label="姓名">
                      <el-input :model-value="profileForm.realName || profileForm.name" disabled class="readonly-field" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                    </el-form-item>
                    <el-form-item v-if="role === 'ROLE_AGENT'" label="个人简介" prop="introduction">
                      <el-input
                          v-model="profileForm.introduction"
                          type="textarea"
                          :rows="5"
                          placeholder="请输入个人简介，介绍您的从业经验和擅长领域..."
                          maxlength="500"
                          show-word-limit
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" :loading="saveLoading" @click="handleSave" class="submit-btn">
                        保存修改
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-card>

            <el-card :class="['form-card', 'form-card--dashboard', 'dashboard-panel-card', 'dashboard-panel-card--secondary']" shadow="hover">
              <template #header>
                <div class="card-header-bar">
                  <span class="card-header-icon">🔒</span>
                  <span class="card-header-text">修改密码</span>
                </div>
              </template>
              <div class="dashboard-card-body dashboard-card-body--compact">
                <div class="dashboard-form-shell dashboard-form-shell--narrow">
                  <el-form
                      :model="passwordForm"
                      :rules="passwordRules"
                      ref="passwordFormRef"
                      label-width="90px"
                      class="profile-form profile-form--dashboard-shell"
                  >
                    <el-form-item label="原密码" prop="oldPassword">
                      <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword" class="submit-btn">
                        修改密码
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>

      <el-row v-if="false" :gutter="20" class="profile-shell-row profile-shell-row--dashboard">
        <el-col :span="24">
          <el-card :class="['form-card', 'form-card--dashboard']" shadow="hover">
            <template #header>
              <div class="card-header-bar">
                <span class="card-header-icon">👤</span>
                <span class="card-header-text">个人资料</span>
              </div>
            </template>
            <el-form
                :model="profileForm"
                :rules="profileRules"
                ref="profileFormRef"
                label-width="90px"
                class="profile-form"
            >
              <el-form-item label="姓名">
                <el-input :model-value="profileForm.realName || profileForm.name" disabled class="readonly-field" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item v-if="role === 'ROLE_AGENT'" label="个人简介" prop="introduction">
                <el-input
                    v-model="profileForm.introduction"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人简介，介绍您的从业经验和擅长领域..."
                    maxlength="500"
                    show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saveLoading" @click="handleSave" class="submit-btn">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <div class="card-header-bar" style="margin-bottom: 24px">
              <span class="card-header-icon">🔒</span>
              <span class="card-header-text">修改密码</span>
            </div>
            <el-form
                :model="passwordForm"
                :rules="passwordRules"
                ref="passwordFormRef"
                label-width="90px"
                class="profile-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" :loading="passwordLoading" @click="handleChangePassword" class="submit-btn">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 涓婁紶鍚堝悓瀵硅瘽妗?-->
    <el-dialog v-model="showUploadDialog" title="上传合同" width="500px" :close-on-click-modal="false">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="合同 URL" required>
          <el-input v-model="uploadForm.contractUrl" placeholder="/uploads/contracts/xxx.pdf" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadContract">纭涓婁紶</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Star, Document, Lock } from '@element-plus/icons-vue'
import request from '@/api'

const VIEWING_STATUS = Object.freeze({
  PENDING: 0,
  CONFIRMED: 1,
  COMPLETED: 2,
  REJECTED: 3,
  EXPIRED: 4
})

const viewingStatusConfig = Object.freeze({
  [VIEWING_STATUS.PENDING]: { type: 'warning', text: '待确认' },
  [VIEWING_STATUS.CONFIRMED]: { type: 'success', text: '已确认' },
  [VIEWING_STATUS.COMPLETED]: { type: 'info', text: '已完成' },
  [VIEWING_STATUS.REJECTED]: { type: 'danger', text: '已取消' },
  [VIEWING_STATUS.EXPIRED]: { type: 'info', text: '已过期' }
})

const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

const store = useStore()
const router = useRouter()

const userInfo = computed(() => store.getters.userInfo)
const role = computed(() => store.getters.role)
const userId = computed(() => userInfo.value?.userId || userInfo.value?.id)
const isBackofficeProfile = computed(() => role.value === 'ROLE_AGENT' || role.value === 'ROLE_ADMIN')

const activeTab = ref('profile')
const viewingsLoading = ref(false)
const favoritesLoading = ref(false)
const transactionsLoading = ref(false)
const saveLoading = ref(false)
const passwordLoading = ref(false)
const viewings = ref([])
const favorites = ref([])
const transactions = ref([])
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarInputRef = ref(null)
const showUploadDialog = ref(false)
const currentTransaction = ref(null)
const uploadForm = reactive({ contractUrl: '' })

const profileForm = reactive({
  realName: '',
  name: '',
  nickname: '',
  phone: '',
  email: '',
  introduction: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ pattern: EMAIL_REGEX, message: '请输入有效的邮箱地址', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

const avatarUrl = computed(() => userInfo.value?.avatar || DEFAULT_AVATAR)

const displayName = computed(() => {
  const info = userInfo.value
  if (!info) return ''
  return info.nickname || info.name || info.realName || info.phone || '未知用户'
})

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
}

function buildImageUrl(path) {
  if (!path) return '/placeholder.jpg'
  if (path.startsWith('http')) return path
  return 'http://localhost:8080/api' + path
}

async function loadUserInfo() {
  try {
    const res = await request.get('/auth/info')
    if (res.code === 200) {
      const data = res.data
      profileForm.email = data.email || ''
      profileForm.phone = data.phone || ''
      if (role.value === 'ROLE_ADMIN') {
        profileForm.realName = data.realName || data.name || ''
      } else if (role.value === 'ROLE_AGENT') {
        profileForm.name = data.name || data.phone || ''
        profileForm.introduction = data.introduction || ''
      } else {
        profileForm.nickname = data.nickname || data.phone || ''
      }
      store.commit('SET_USER_INFO', { ...userInfo.value, ...data })
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { ElMessage.warning('请选择图片文件'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片大小不能超过 5MB'); return }
  try {
    const formData = new FormData()
    formData.append('file', file)
    // 修复：使用专门的头像上传接口，后端会根据用户角色自动存储到对应目录
    const res = await request.post('/file/upload/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 200) {
      store.commit('SET_USER_INFO', { ...userInfo.value, avatar: res.data.url })
      ElMessage.success('头像更新成功')
    } else { ElMessage.error(res.message || '上传失败') }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
  event.target.value = ''
}

async function loadMyViewings() {
  viewingsLoading.value = true
  try {
    const res = await request.get('/viewing/my/customer')
    if (res.code === 200) viewings.value = res.data || []
  } catch (error) { console.error('加载带看列表失败:', error) }
  finally { viewingsLoading.value = false }
}

async function loadFavorites() {
  favoritesLoading.value = true
  try {
    const res = await request.get('/favorite/list', { params: { customerId: userId.value } })
    if (res.code === 200) favorites.value = res.data || []
  } catch (error) { console.error('鍔犺浇鏀惰棌鍒楄〃澶辫触:', error) }
  finally { favoritesLoading.value = false }
}

async function loadTransactions() {
  transactionsLoading.value = true
  try {
    const res = await request.get('/transaction/my/customer')
    if (res.code === 200) transactions.value = res.data || []
  } catch (error) { console.error('鍔犺浇浜ゆ槗鍒楄〃澶辫触:', error) }
  finally { transactionsLoading.value = false }
}

function handleMenuSelect(index) {
  activeTab.value = index
}

async function handleSave() {
  if (!profileFormRef.value) return
  try { await profileFormRef.value.validate() } catch { return }
  saveLoading.value = true
  try {
    let res
    if (role.value === 'ROLE_ADMIN') {
      res = await request.put('/auth/admin/profile', { realName: profileForm.realName, email: profileForm.email })
    } else if (role.value === 'ROLE_AGENT') {
      res = await request.put('/user/agent/profile', { name: profileForm.name, email: profileForm.email, introduction: profileForm.introduction })
    } else {
      res = await request.put('/user/customer/profile', { nickname: profileForm.nickname, email: profileForm.email })
    }
    if (res.code === 200) {
      ElMessage.success('保存成功')
      const updatedInfo = { ...userInfo.value }
      if (role.value === 'ROLE_ADMIN') updatedInfo.realName = profileForm.realName
      else if (role.value === 'ROLE_AGENT') { updatedInfo.name = profileForm.name; updatedInfo.introduction = profileForm.introduction }
      else updatedInfo.nickname = profileForm.nickname
      updatedInfo.email = profileForm.email
      store.commit('SET_USER_INFO', updatedInfo)
    } else { ElMessage.error(res.message || '保存失败') }
  } catch (error) { console.error('保存失败:', error); ElMessage.error('保存失败') }
  finally { saveLoading.value = false }
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  try { await passwordFormRef.value.validate() } catch { return }
  passwordLoading.value = true
  try {
    const res = await request.post('/auth/password', { oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      setTimeout(() => {
        sessionStorage.removeItem('token'); sessionStorage.removeItem('role'); sessionStorage.removeItem('userInfo')
        store.commit('CLEAR_AUTH'); router.push('/login')
      }, 1500)
    } else { ElMessage.error(res.message || '密码修改失败') }
  } catch (error) { console.error('密码修改失败:', error); ElMessage.error('密码修改失败，请检查原密码是否正确') }
  finally { passwordLoading.value = false }
}

async function removeFavorite(id) {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const res = await request.delete('/favorite/' + id)
    if (res.code === 200) { ElMessage.success('已取消收藏'); favorites.value = favorites.value.filter(f => f.id !== id) }
  } catch (error) { if (error !== 'cancel') console.error('取消收藏失败:', error) }
}

function viewHouseDetail(houseId) {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    router.push(`/agent-layout/house/detail/${houseId}`)
  } else if (currentPath.startsWith('/admin-layout/')) {
    router.push(`/admin-layout/house/detail/${houseId}`)
  } else {
    router.push(`/layout/house/detail/${houseId}`)
  }
}

function requestViewing(item) {
  const currentPath = route.path || ''
  let messagePath = '/layout/message'
  if (currentPath.startsWith('/agent-layout/')) {
    messagePath = '/agent-layout/message'
  } else if (currentPath.startsWith('/admin-layout/')) {
    messagePath = '/admin-layout/message'
  }
  router.push({ path: messagePath, query: { targetId: item.agentId, targetType: 'agent', currentHouseId: item.houseId, currentHouseTitle: item.houseTitle } })
}

function handleViewDetail(row) {
  const statusText = viewingStatusConfig[row.status]?.text || '未知'
  ElMessageBox({
    title: '带看详情',
    message: '<div style="line-height: 2"><p><strong>房源：</strong>' + (row.houseTitle || '-') + '</p><p><strong>地址：</strong>' + (row.houseAddress || '-') + '</p><p><strong>中介：</strong>' + (row.agentName || '-') + ' (' + (row.agentPhone || '-') + ')</p><p><strong>预约时间：</strong>' + formatTime(row.appointTime) + '</p><p><strong>状态：</strong>' + statusText + '</p></div>',
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

async function handleCancelViewing(row) {
  try {
    await ElMessageBox.confirm('确定取消这个带看预约吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const res = await request.put('/viewing/' + row.id + '/cancel')
    if (res.code === 200) { ElMessage.success('已取消带看'); row.status = VIEWING_STATUS.REJECTED }
    else ElMessage.error(res.message || '取消失败')
  } catch (error) { if (error !== 'cancel') { console.error('取消带看失败:', error); ElMessage.error('取消失败') } }
}

function viewTransactionDetail(row) {
  router.push('/layout/transaction/detail/' + row.id)
}

async function confirmTransaction(row) {
  try {
    await ElMessageBox.confirm('确定要确认这笔交易吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    const res = await request.post('/transaction/status/' + row.id, null, { params: { status: 1 } })
    if (res.code === 200) { ElMessage.success('交易已确认'); loadTransactions() }
    else ElMessage.error(res.message || '操作失败')
  } catch (error) {
    if (error !== 'cancel') { console.error('确认交易失败:', error); ElMessage.error('确认交易失败') }
  }
}

function openUploadDialog(row) {
  currentTransaction.value = row
  uploadForm.contractUrl = ''
  showUploadDialog.value = true
}

async function handleUploadContract() {
  if (!uploadForm.contractUrl) { ElMessage.warning('请填写合同 URL'); return }
  try {
    const res = await request.put('/transaction/update', {
      id: currentTransaction.value.id, contractUrl: uploadForm.contractUrl, status: 2
    })
    if (res.code === 200) { ElMessage.success('合同上传成功'); showUploadDialog.value = false; loadTransactions() }
    else ElMessage.error(res.message || '鎿嶄綔澶辫触')
  } catch (error) { console.error('上传合同失败:', error); ElMessage.error('上传合同失败') }
}

onMounted(() => {
  loadUserInfo()
  if (role.value === 'ROLE_CUSTOMER') {
    loadFavorites()
    loadTransactions()
    loadMyViewings()
  }
})
</script>

<style scoped>
/* ==================== Variables ==================== */
.profile-page {
  --primary-start: #064e3b;
  --primary-end: #059669;
  --primary-gradient: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  --primary-dark: #064e3b;
  --primary-light: #a7f3d0;
  --primary-bg: #ecfdf5;
  --text-primary: #1f2937;
  --text-secondary: #374151;
  --text-muted: #6b7280;
  --bg-page: #f8fafc;
  --shadow-card: 0 4px 20px rgba(0, 0, 0, 0.05);
  --shadow-hover: 0 12px 32px rgba(6, 78, 59, 0.15);
  --radius-lg: 16px;
  --radius-md: 12px;
  --transition-base: 0.3s ease;
  --dashboard-shell-width: 1280px;
  min-height: calc(100vh - 84px);
  background-color: var(--bg-page);
}

.profile-page--dashboard {
  background:
    radial-gradient(circle at top left, rgba(6, 78, 59, 0.08), transparent 28%),
    linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 52%, #d1fae5 100%);
}

/* ==================== Hero Section (Dashboard Style) ==================== */
.hero-section {
  background: var(--primary-gradient);
  padding: 40px 24px 60px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
  opacity: 0.1;
}

.hero-content {
  max-width: var(--dashboard-shell-width);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
  flex-wrap: wrap;
  gap: 30px;
}

.profile-page:not(.profile-page--dashboard) .hero-section {
  padding: 24px 24px 32px;
}

.profile-page:not(.profile-page--dashboard) .hero-content {
  width: 100%;
  box-sizing: border-box;
  padding: 22px 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
  position: relative;
  overflow: hidden;
  align-items: center;
  gap: 20px;
}

.profile-page:not(.profile-page--dashboard) .hero-content::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #064e3b 0%, #059669 52%, #10b981 100%);
}

.hero-section--dashboard {
  background: transparent;
  padding: 18px 24px 0;
  color: var(--text-primary);
}

.hero-section--dashboard::before {
  display: none;
}

.hero-content--dashboard {
  width: 100%;
  box-sizing: border-box;
  padding: 22px 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
  position: relative;
  overflow: hidden;
  align-items: center;
  gap: 20px;
}

.hero-content--dashboard::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #064e3b 0%, #059669 52%, #10b981 100%);
}

.hero-text-group {
  flex: 1;
  min-width: 300px;
}

.profile-page--dashboard .hero-text-group {
  max-width: 760px;
}

.eyebrow {
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 2px;
  opacity: 0.8;
  margin-bottom: 8px;
  font-weight: 600;
}

.profile-page:not(.profile-page--dashboard) .eyebrow {
  color: #22a67a;
  opacity: 1;
}

.profile-page--dashboard .eyebrow {
  color: #059669;
  opacity: 1;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 12px;
  line-height: 1.2;
}

.profile-page:not(.profile-page--dashboard) .hero-title {
  background: linear-gradient(135deg, #064e3b 0%, #059669 50%, #10b981 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.profile-page--dashboard .hero-title {
  font-size: 38px;
}

.profile-page--dashboard .hero-title {
  background: linear-gradient(135deg, #064e3b 0%, #059669 50%, #10b981 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
  max-width: 500px;
}

.profile-page:not(.profile-page--dashboard) .hero-desc {
  color: #607086;
  opacity: 1;
  max-width: 720px;
  font-size: 16px;
}

.profile-page--dashboard .hero-desc {
  color: #607086;
  opacity: 1;
  max-width: 720px;
  font-size: 16px;
}

/* User Card in Hero */
.hero-user-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 16px 24px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.profile-page:not(.profile-page--dashboard) .hero-user-card {
  background: linear-gradient(135deg, #ffffff 0%, rgba(236, 253, 245, 0.88) 100%);
  border: 1px solid rgba(5, 150, 105, 0.14);
  box-shadow: none;
  padding: 14px 18px;
  border-radius: 22px;
  min-width: 290px;
  justify-content: flex-start;
}

.hero-user-card--dashboard {
  background: linear-gradient(135deg, #ffffff 0%, rgba(236, 253, 245, 0.88) 100%);
  border: 1px solid rgba(5, 150, 105, 0.14);
  box-shadow: none;
  padding: 14px 18px;
  border-radius: 22px;
  min-width: 290px;
  justify-content: flex-start;
}

.avatar-container {
  width: 80px; height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.8);
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}

.profile-page--dashboard .avatar-container {
  border-color: rgba(5, 150, 105, 0.24);
  width: 72px;
  height: 72px;
}

.avatar-img {
  width: 100%; height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.3s;
  font-size: 12px; font-weight: 600;
}

.avatar-container:hover .avatar-overlay { opacity: 1; }

.user-meta {
  color: #fff;
}

.profile-page:not(.profile-page--dashboard) .user-meta {
  color: #132238;
}

.profile-page--dashboard .user-meta {
  color: #132238;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
}

.profile-page--dashboard .user-name {
  font-size: 20px;
}

.user-contact, .user-email {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.profile-page:not(.profile-page--dashboard) .user-contact,
.profile-page:not(.profile-page--dashboard) .user-email {
  color: #607086;
  opacity: 1;
}

.profile-page--dashboard .user-contact,
.profile-page--dashboard .user-email {
  color: #607086;
  opacity: 1;
}

/* Stats Bar */
.hero-stats {
  max-width: 1120px;
  margin: 20px auto 0;
  padding: 0 24px;
  position: relative;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.profile-page--dashboard .hero-stats {
  margin: 16px auto 0;
}

.stat-item {
  background: #fff;
  padding: 20px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  width: 48px; height: 48px;
  background: var(--primary-bg);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary-dark);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

/* ==================== Main Content ==================== */
.profile-main {
  max-width: var(--dashboard-shell-width);
  margin: 40px auto 0;
  padding: 0 0 60px;  /* 移除左右padding,与Hero区域保持一致 */
}

.profile-main--dashboard {
  margin-top: 16px;
  max-width: var(--dashboard-shell-width);
  box-sizing: border-box;
  padding: 0 0 40px;  /* Dashboard样式也统一 */
}

/* ==================== 宸︿晶瀵艰埅鍗＄墖 ==================== */
.nav-card {
  border-radius: var(--radius-lg); border: none;
  box-shadow: var(--shadow-card); position: sticky; top: 90px;
}

.nav-card :deep(.el-card__body) { padding: 20px; }

.profile-nav { border-right: none; background: transparent; }

.profile-nav .el-menu-item {
  height: 52px; line-height: 52px; border-radius: 8px;
  margin-bottom: 4px; transition: all var(--transition-base); font-size: 16px;
}

.profile-nav .el-menu-item:hover {
  background: var(--primary-bg); color: var(--primary-dark);
}

.profile-nav .el-menu-item.is-active {
  background: var(--primary-gradient); color: #fff; font-weight: 600;
}

.nav-icon { margin-right: 8px; }

.stat-grid {
  display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px;
}

.stat-box {
  text-align: center; padding: 14px 8px;
  border-radius: 10px; background: var(--primary-bg);
}

.stat-value {
  font-size: 28px; font-weight: 800; color: var(--primary-dark);
}

.stat-label {
  font-size: 14px; color: var(--text-muted); margin-top: 2px;
}

/* ==================== 鍙充晶琛ㄥ崟鍗＄墖 ==================== */
.form-card {
  border-radius: var(--radius-lg); border: none;
  box-shadow: var(--shadow-card); transition: box-shadow var(--transition-base);
}

.form-card:hover { box-shadow: var(--shadow-hover); }

.form-card :deep(.el-card__header) {
  padding: 20px 24px; border-bottom: 1px solid #e5e7eb;
}

.form-card--dashboard {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
  position: relative;
  overflow: hidden;
}

.form-card--dashboard::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #064e3b 0%, #059669 52%, #10b981 100%);
}

.form-card--dashboard :deep(.el-card__header) {
  padding: 24px 28px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.profile-page--dashboard .form-card--dashboard :deep(.el-card__body) {
  padding: 28px;
}

.profile-shell-row--dashboard {
  max-width: 100%;
  margin: 0 auto;
}

.dashboard-profile-stack {
  display: grid;
  gap: 18px;
}

.dashboard-panel-card {
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
}

.dashboard-card-body {
  display: flex;
  justify-content: flex-start;
}

.dashboard-card-body--compact {
  align-items: flex-start;
}

.dashboard-form-shell {
  width: min(100%, 760px);
}

.dashboard-form-shell--narrow {
  width: min(100%, 760px);
}

.profile-form--dashboard-shell {
  max-width: none;
}

.card-header-bar {
  display: flex; align-items: center; gap: 10px;
}

.card-header-icon { font-size: 24px; }

.card-header-text {
  font-size: 22px; font-weight: 700; color: var(--text-primary);
}

.profile-page--dashboard .card-header-icon {
  color: #059669;
}

.profile-page--dashboard .card-header-text {
  color: #132238;
}

.profile-form { max-width: 600px; }

.profile-form :deep(.el-form-item__label) {
  font-size: 16px; font-weight: 500; color: var(--text-primary);
}

.profile-form :deep(.el-input__wrapper),
.profile-form :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.profile-page--dashboard .profile-form :deep(.el-input__wrapper),
.profile-page--dashboard .profile-form :deep(.el-textarea__inner) {
  background: #f8fafc;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.22) inset;
}

.profile-page--dashboard .profile-form :deep(.el-input__wrapper.is-focus),
.profile-page--dashboard .profile-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px rgba(5, 150, 105, 0.4) inset;
}

.profile-form :deep(.el-input__inner),
.profile-form :deep(.el-textarea__inner) {
  font-size: 16px;
}

.submit-btn {
  border-radius: 8px; padding: 10px 36px;
  font-size: 16px; font-weight: 600;
}

.profile-page--dashboard .submit-btn {
  border-radius: 999px;
  padding: 10px 28px;
  min-width: 144px;
}

/* ==================== 鍙瀛楁 ==================== */
.readonly-field :deep(.el-input__wrapper) {
  background-color: var(--primary-bg); border-radius: 8px;
}

.profile-page--dashboard .readonly-field :deep(.el-input__wrapper) {
  background: rgba(236, 253, 245, 0.9);
  box-shadow: 0 0 0 1px rgba(5, 150, 105, 0.08) inset;
}

.readonly-field :deep(.el-input__inner) {
  color: #606266; cursor: not-allowed; font-size: 16px;
}

/* ==================== 鏀惰棌鍗＄墖 ==================== */
.favorites-grid {
  display: flex; flex-direction: column; gap: 16px;
}

.fav-card {
  display: flex; gap: 16px; padding: 16px;
  border-radius: 10px; border: 1px solid #e5e7eb;
  transition: all var(--transition-base); cursor: pointer; align-items: center;
}

.fav-card:hover {
  border-color: var(--primary-start);
  box-shadow: 0 4px 12px rgba(6, 78, 59, 0.12);
  transform: translateX(4px);
}

.fav-img {
  width: 120px; height: 90px; min-width: 120px;
  border-radius: 8px; overflow: hidden;
}

.fav-img img { width: 100%; height: 100%; object-fit: cover; }

.fav-info { flex: 1; min-width: 0; }

.fav-title {
  font-size: 17px; font-weight: 600; color: var(--text-primary);
  margin: 0 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.fav-price {
  font-size: 22px; font-weight: 800; color: #f56c6c; margin-bottom: 4px;
}

.fav-detail {
  font-size: 15px; color: var(--text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.fav-actions {
  display: flex; flex-direction: column; gap: 8px; flex-shrink: 0;
}

/* ==================== 琛ㄦ牸 ==================== */
:deep(.el-table) {
  border-radius: 8px; overflow: hidden; font-size: 15px;
}

:deep(.el-table th.el-table__cell) {
  background: var(--primary-bg) !important;
  color: var(--text-primary); font-weight: bold; font-size: 15px;
}

:deep(.el-table td.el-table__cell) { font-size: 15px; }

/* ==================== 琛ㄦ牸鍐呴摼鎺?==================== */
.table-link {
  color: #059669;
  cursor: pointer;
  font-weight: 500;
  transition: color var(--transition-base);
}

.table-link:hover {
  color: #064e3b;
  text-decoration: underline;
}

:deep(.el-divider) { border-color: #e5e7eb; }

/* ==================== 鍝嶅簲寮?==================== */
@media (max-width: 768px) {
  .banner-bg { height: 140px; }
  .profile-main { padding: 0 12px 30px; margin-top: -40px; }
  .user-header { flex-direction: column; text-align: center; padding: 20px; }
  .user-header__row { justify-content: center; }
  .fav-card { flex-direction: column; }
  .fav-img { width: 100%; height: 160px; }
  .fav-actions { flex-direction: row; justify-content: flex-end; }
  .profile-page--dashboard .hero-section {
    padding: 16px 16px 0;
  }

  .profile-page--dashboard .hero-content--dashboard {
    padding: 20px 18px;
    gap: 14px;
  }

  .profile-page--dashboard .hero-content--dashboard,
  .profile-page--dashboard .form-card--dashboard :deep(.el-card__body),
  .profile-page--dashboard .form-card--dashboard :deep(.el-card__header) {
    padding-left: 18px;
    padding-right: 18px;
  }
  .profile-page--dashboard .profile-main {
    margin-top: 16px;
    padding: 0 16px 32px;
  }
  .profile-page--dashboard .hero-user-card--dashboard {
    width: 100%;
    min-width: 0;
  }
}
</style>
