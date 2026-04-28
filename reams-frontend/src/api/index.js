import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    // 静态资源不携带 token
    const isStaticResource = typeof config.url === 'string' && config.url.includes('/uploads/')
    
    if (token && !isStaticResource) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
   const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      if (res.code === 401) {
        sessionStorage.clear()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
