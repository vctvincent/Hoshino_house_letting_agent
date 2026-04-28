<template>
  <el-container class="admin-layout">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <el-dropdown @command="handleCommand" placement="bottom-start">
          <span class="logo-avatar-trigger">
            <el-avatar :size="46" :src="userAvatar || undefined" class="user-avatar logo-avatar">
              {{ userName?.charAt(0) || '管' }}
            </el-avatar>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <div class="logo-copy">
          <h2>REAMS</h2>
          <p>管理员后台</p>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="var(--sidebar-body-start)"
        text-color="var(--sidebar-menu-text)"
        active-text-color="var(--sidebar-menu-active-text)"
      >
        <el-menu-item index="/admin-layout/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>经营看板</span>
        </el-menu-item>

        <el-menu-item index="/admin-layout/house/list">
          <el-icon><House /></el-icon>
          <span>房源列表</span>
        </el-menu-item>

        <el-menu-item index="/admin-layout/house/audit">
          <el-icon><DocumentChecked /></el-icon>
          <span>房源审核</span>
        </el-menu-item>

        <el-menu-item index="/admin-layout/user/staff">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>

        <el-menu-item index="/admin-layout/transaction/manage">
          <el-icon><Tickets /></el-icon>
          <span>交易管理</span>
        </el-menu-item>

        <el-menu-item index="/admin-layout/message">
          <el-icon><Bell /></el-icon>
          <div class="menu-item-row">
            <span class="menu-item-text">消息中心</span>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="menu-badge">
              <span class="menu-badge-anchor" />
            </el-badge>
          </div>
        </el-menu-item>

        <el-menu-item index="/admin-layout/profile">
          <el-icon><Setting /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import request from '@/api'
import { useStore } from 'vuex'
import { formatImageUrl } from '@/utils/imageUtils'
import {
  DataAnalysis,
  House,
  User,
  Tickets,
  Bell,
  Setting,
  SwitchButton,
  DocumentChecked
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const store = useStore()

const userInfo = computed(() => store.getters.userInfo || {})
const userName = computed(() => userInfo.value.name || sessionStorage.getItem('username') || '管理员')
const userAvatar = computed(() => formatImageUrl(userInfo.value.avatar) || '')
const activeMenu = computed(() => route.path)
const unreadCount = ref(0)
const currentUserId = computed(() => userInfo.value?.userId || userInfo.value?.id)

const loadUnreadCount = async () => {
  try {
    const userId = currentUserId.value
    if (!userId) {
      unreadCount.value = 0
      return
    }
    const res = await request.get('/message/unread/count', {
      params: { userId, userType: 1 }
    })
    unreadCount.value = res?.data?.count || 0
  } catch {
    unreadCount.value = 0
  }
}

const handleCommand = command => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      store.dispatch('clearAuth')
      sessionStorage.removeItem('username')
      router.push('/login')
    }).catch(() => {})
  }
}

watch(() => route.path, () => {
  loadUnreadCount()
})

onMounted(() => {
  loadUnreadCount()
})
</script>

<style scoped>
@import '../../styles/sidebar-theme.css';

.admin-layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  background: linear-gradient(180deg, var(--sidebar-body-start) 0%, var(--sidebar-body-end) 100%);
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 16px;
  color: #ffffff;
  border-bottom: 1px solid var(--sidebar-divider);
  background: linear-gradient(180deg, var(--sidebar-top-start) 0%, var(--sidebar-top-end) 100%);
}

.logo-copy {
  min-width: 0;
}

.logo h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo p {
  margin: 5px 0 0;
  font-size: 12px;
  color: #ffffff;
  font-weight: 500;
}

.logo-avatar-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

.menu-item-row {
  width: 100%;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.menu-item-text {
  min-width: 0;
}

.menu-badge {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
}

.menu-badge-anchor {
  display: inline-block;
  width: 14px;
  height: 14px;
}

:deep(.menu-badge .el-badge__content) {
  transform: translate(50%, -50%);
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: var(--sidebar-menu-hover-bg) !important;
  color: var(--sidebar-menu-hover-text) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(
    90deg,
    var(--sidebar-menu-active-bg-start),
    var(--sidebar-menu-active-bg-end)
  ) !important;
  color: var(--sidebar-menu-active-text) !important;
  font-weight: 600;
  box-shadow: inset 3px 0 0 var(--sidebar-menu-active-border);
}

:deep(.el-menu-item.is-active::after) {
  display: none !important;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #fff !important;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-avatar {
  flex-shrink: 0;
  background: var(--sidebar-avatar-bg);
  color: #ffffff;
  font-weight: 600;
}

.logo-avatar {
  box-shadow: inset 0 0 0 1px var(--sidebar-avatar-ring);
}

.main-content {
  background-color: var(--content-bg);
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.sidebar::-webkit-scrollbar,
.main-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.sidebar::-webkit-scrollbar-thumb {
  background-color: var(--sidebar-scrollbar-thumb);
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background-color: var(--sidebar-scrollbar-thumb-hover);
}

.main-content::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}
</style>
