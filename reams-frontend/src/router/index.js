import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'CustomerRegister',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/register/agent',
    name: 'AgentRegister',
    component: () => import('../views/auth/AgentRegister.vue')
  },
  {
    path: '/',
    name: 'Home',
    redirect: to => {
      const token = sessionStorage.getItem('token')
      const role = sessionStorage.getItem('role')
      if (token) {
        // 根据角色跳转到不同的首页
        if (role === 'ROLE_ADMIN') {
          return '/admin-layout/admin'
        } else if (role === 'ROLE_AGENT') {
          return '/agent-layout/agent/workspace'
        } else {
          return '/layout/dashboard'
        }
      } else {
        return '/login'
      }
    }
  },
  
  // ==================== 管理员专用布局（侧边栏） ====================
  {
    path: '/admin-layout',
    name: 'AdminLayout',
    component: () => import('../views/layout/AdminLayout.vue'),
    meta: { roles: ['ROLE_ADMIN'] },
    children: [
      {
        path: 'admin',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Admin.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'house/list',
        name: 'AdminHouseList',
        component: () => import('../views/house/HouseList.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'house/detail/:id',
        name: 'AdminHouseDetail',
        component: () => import('../views/house/HouseDetail.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'house/audit',
        name: 'HouseAudit',
        component: () => import('../views/house/HouseAudit.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'customer-manage',
        name: 'CustomerManage',
        component: () => import('../views/system/CustomerManage.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'user/staff',
        name: 'StaffManage',
        component: () => import('../views/user/StaffManage.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'viewing-manage',
        name: 'AdminViewingManage',
        component: () => import('../views/viewing/ViewingManage.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'transaction/manage',
        name: 'AdminTransactionManage',
        component: () => import('../views/transaction/TransactionManage.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'transaction/detail/:id',
        name: 'AdminTransactionDetail',
        component: () => import('../views/transaction/TransactionDetail.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'message',
        name: 'AdminMessage',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: { roles: ['ROLE_ADMIN'] }
      }
    ]
  },
  
  // ==================== 中介专用布局（侧边栏） ====================
  {
    path: '/agent-layout',
    name: 'AgentLayout',
    component: () => import('../views/layout/AgentLayout.vue'),
    meta: { roles: ['ROLE_AGENT'] },
    children: [
      {
        path: 'agent/workspace',
        name: 'AgentWorkspace',
        component: () => import('../views/agent/AgentWorkspace.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'house/list',
        name: 'AgentHouseList',
        component: () => import('../views/house/HouseList.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'house/detail/:id',
        name: 'AgentHouseDetail',
        component: () => import('../views/house/HouseDetail.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'house/manage',
        name: 'AgentHouseManage',
        component: () => import('../views/house/HouseManage.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'house/editor/:id?',
        name: 'AgentHouseEditor',
        component: () => import('../views/house/HouseEditor.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'viewing-manage',
        name: 'AgentViewingManage',
        component: () => import('../views/viewing/ViewingManage.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'transaction/manage',
        name: 'AgentTransactionManage',
        component: () => import('../views/transaction/TransactionManage.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'transaction/detail/:id',
        name: 'AgentTransactionDetail',
        component: () => import('../views/transaction/TransactionDetail.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'message',
        name: 'AgentMessage',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      },
      {
        path: 'profile',
        name: 'AgentProfilePage',
        component: () => import('../views/user/Profile.vue'),
        meta: { roles: ['ROLE_AGENT'] }
      }
    ]
  },
  
  // ==================== 客户布局（保持不变） ====================
  {
    path: '/layout',
    name: 'Layout',
    component: () => import('../views/layout/Layout.vue'),
    children: [
      // 公共页面 - 所有角色都可以访问
      {
        path: 'house/list',
        name: 'HouseList',
        component: () => import('../views/house/HouseList.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      },
      {
        path: 'house/detail/:id',
        name: 'HouseDetail',
        component: () => import('../views/house/HouseDetail.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      },
      {
        path: 'transaction/detail/:id',
        name: 'TransactionDetail',
        component: () => import('../views/transaction/TransactionDetail.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      },
      {
        path: 'transaction/customer',
        name: 'CustomerTransaction',
        component: () => import('../views/transaction/CustomerTransaction.vue'),
        meta: { roles: ['ROLE_CUSTOMER'] }
      },
      {
        path: 'viewing/my',
        name: 'MyViewings',
        component: () => import('../views/viewing/MyViewings.vue'),
        meta: { roles: ['ROLE_CUSTOMER'] }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      },
      {
        path: 'agent/profile/:id',
        name: 'AgentProfile',
        component: () => import('../views/agent/AgentProfile.vue'),
        meta: { roles: ['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  // 公开页面白名单（包括客户注册和中介注册）
  const publicPages = ['/login', '/register', '/register/agent']
  
  if (publicPages.includes(to.path)) {
    next()
  } else {
    if (!token) {
      next('/login')
    } else {
      // 检查权限
      if (to.meta.roles && !to.meta.roles.includes(role)) {
        if (typeof ElementPlus !== 'undefined' && ElementPlus.ElMessage) {
           ElementPlus.ElMessage.error('没有权限访问该页面')
        }
        // 根据角色跳转到对应的首页
        if (role === 'ROLE_ADMIN') {
          next('/admin-layout/admin')
        } else if (role === 'ROLE_AGENT') {
          next('/agent-layout/agent/workspace')
        } else {
          next('/layout/dashboard')
        }
      } else {
        next()
      }
    }
  }
})

export default router
