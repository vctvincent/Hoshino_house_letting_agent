<template>
  <div class="auth-page">
    <section class="auth-shell">
      <aside class="auth-visual">
        <div class="visual-overlay"></div>
        <div class="visual-content">
          <p class="visual-kicker">REAMS</p>
          <h1>星野</h1>
          <p class="visual-title">让每一次居住选择都值得信赖</p>
        </div>
      </aside>

      <section class="auth-card">
        <div class="card-head">
          <p class="card-kicker">Account Access</p>
          <h2>欢迎回来</h2>
          <p>请选择身份后登录系统。</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="客户登录" name="customer">
            <el-form ref="customerFormRef" :model="customerForm" :rules="customerRules" label-width="0" status-icon>
              <el-form-item prop="phone">
                <el-input v-model.trim="customerForm.phone" placeholder="请输入手机号" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="customerForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item prop="captchaCode">
                <div class="captcha-row">
                  <el-input
                    v-model.trim="customerForm.captchaCode"
                    placeholder="请输入图片验证码"
                    size="large"
                    @keyup.enter="handleLogin"
                  />
                  <button class="captcha-image" type="button" @click="refreshCaptcha">
                    <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                    <span v-else>加载中...</span>
                  </button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
              </el-form-item>
              <div class="form-foot">
                <el-link type="primary" @click="$router.push('/register')">还没有账号？立即注册</el-link>
              </div>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="中介登录" name="agent">
            <el-form ref="agentFormRef" :model="agentForm" :rules="agentRules" label-width="0" status-icon>
              <el-form-item prop="phone">
                <el-input v-model.trim="agentForm.phone" placeholder="请输入手机号" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="agentForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item prop="captchaCode">
                <div class="captcha-row">
                  <el-input
                    v-model.trim="agentForm.captchaCode"
                    placeholder="请输入图片验证码"
                    size="large"
                    @keyup.enter="handleLogin"
                  />
                  <button class="captcha-image" type="button" @click="refreshCaptcha">
                    <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                    <span v-else>加载中...</span>
                  </button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
              </el-form-item>
              <div class="form-foot">
                <el-link type="primary" @click="$router.push('/register/agent')">还没有账号？立即注册</el-link>
              </div>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="管理员登录" name="admin">
            <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules" label-width="0" status-icon>
              <el-form-item prop="name">
                <el-input v-model.trim="adminForm.name" placeholder="请输入管理员名称" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="adminForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item prop="captchaCode">
                <div class="captcha-row">
                  <el-input
                    v-model.trim="adminForm.captchaCode"
                    placeholder="请输入图片验证码"
                    size="large"
                    @keyup.enter="handleLogin"
                  />
                  <button class="captcha-image" type="button" @click="refreshCaptcha">
                    <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                    <span v-else>加载中...</span>
                  </button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </section>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import request from '@/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const activeTab = ref('customer')
    const captchaImage = ref('')
    const captchaKey = ref('')

    const customerFormRef = ref()
    const agentFormRef = ref()
    const adminFormRef = ref()

    const customerForm = reactive({ phone: '', password: '', captchaCode: '' })
    const agentForm = reactive({ phone: '', password: '', captchaCode: '' })
    const adminForm = reactive({ name: '', password: '', captchaCode: '' })

    const captchaRule = [{ required: true, message: '请输入图片验证码', trigger: 'blur' }]

    const customerRules = {
      phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      captchaCode: captchaRule
    }

    const agentRules = {
      phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      captchaCode: captchaRule
    }

    const adminRules = {
      name: [{ required: true, message: '请输入管理员名称', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    adminRules.captchaCode = captchaRule

    const refreshCaptcha = async () => {
      try {
        const res = await request.get('/auth/captcha')
        captchaImage.value = res.data?.captchaImage || ''
        captchaKey.value = res.data?.captchaKey || ''
      } catch (error) {
        captchaImage.value = ''
        captchaKey.value = ''
        console.error('加载验证码失败:', error)
      }
    }

    const handleTabChange = async (tab) => {
      activeTab.value = tab
      customerForm.captchaCode = ''
      agentForm.captchaCode = ''
      adminForm.captchaCode = ''
      await refreshCaptcha()
    }

    const handleLogin = async () => {
      let formRef
      let formData
      let url

      if (activeTab.value === 'customer') {
        formRef = customerFormRef.value
        formData = {
          phone: customerForm.phone,
          password: customerForm.password,
          captchaCode: customerForm.captchaCode,
          captchaKey: captchaKey.value
        }
        url = '/auth/login/customer'
      } else if (activeTab.value === 'agent') {
        formRef = agentFormRef.value
        formData = {
          phone: agentForm.phone,
          password: agentForm.password,
          captchaCode: agentForm.captchaCode,
          captchaKey: captchaKey.value
        }
        url = '/auth/login/agent'
      } else {
        formRef = adminFormRef.value
        formData = {
          name: adminForm.name.trim(),
          password: adminForm.password,
          captchaCode: adminForm.captchaCode,
          captchaKey: captchaKey.value
        }
        url = '/auth/login/admin'
      }

      await formRef.validate()

      loading.value = true
      try {
        const res = await request.post(url, formData)
        ElMessage.success('登录成功')
        store.dispatch('setAuth', {
          token: res.data.token,
          role: res.data.role,
          userInfo: res.data
        })
        
        // 根据角色跳转到不同的首页
        const role = res.data.role
        if (role === 'ROLE_ADMIN') {
          router.push('/admin-layout/admin')
        } else if (role === 'ROLE_AGENT') {
          router.push('/agent-layout/agent/workspace')
        } else {
          router.push('/layout/dashboard')
        }
      } catch (error) {
        console.error(error)
        if (activeTab.value === 'customer') customerForm.captchaCode = ''
        if (activeTab.value === 'agent') agentForm.captchaCode = ''
        if (activeTab.value === 'admin') adminForm.captchaCode = ''
        await refreshCaptcha()
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      refreshCaptcha()
    })

    return {
      loading,
      activeTab,
      captchaImage,
      customerFormRef,
      agentFormRef,
      adminFormRef,
      customerForm,
      agentForm,
      adminForm,
      customerRules,
      agentRules,
      adminRules,
      handleTabChange,
      handleLogin,
      refreshCaptcha
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(15, 118, 110, 0.18), transparent 30%),
    linear-gradient(135deg, #f4f7f7 0%, #e7f0ee 100%);
}

.auth-shell {
  position: relative;
  min-height: calc(100vh - 56px);
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(420px, 520px);
  border-radius: 32px;
  overflow: hidden;
  background: url('/uploads/auth-login-background.png') center/cover no-repeat;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.18);
}

.auth-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(4, 18, 24, 0.22) 0%, rgba(4, 18, 24, 0.16) 42%, rgba(4, 18, 24, 0.36) 72%, rgba(4, 18, 24, 0.58) 100%),
    linear-gradient(180deg, rgba(4, 18, 24, 0.06), rgba(4, 18, 24, 0.38));
}

.auth-shell::after {
  content: '';
  position: absolute;
  inset: -6%;
  background: url('/uploads/auth-login-background.png') center/cover no-repeat;
  filter: blur(24px) saturate(0.92);
  transform: scale(1.08);
  opacity: 0.96;
  pointer-events: none;
  -webkit-mask-image: linear-gradient(90deg, transparent 0%, transparent 46%, rgba(0, 0, 0, 0.32) 60%, #000 100%);
  mask-image: linear-gradient(90deg, transparent 0%, transparent 46%, rgba(0, 0, 0, 0.32) 60%, #000 100%);
}

.auth-visual,
.auth-card {
  position: absolute;
  z-index: 1;
}

.auth-visual {
  inset: 0 auto 0 0;
  width: min(52%, 760px);
  padding: 56px;
  color: #ffffff;
}

.visual-content {
  position: relative;
  max-width: 460px;
}

.visual-kicker,
.card-kicker {
  margin: 0 0 20px;
  font-size: 18px;
  letter-spacing: 0.25em;
  text-transform: uppercase;
  font-weight: 700;
}

.visual-content h1 {
  margin: 0 0 24px;
  font-size: 68px;
  line-height: 1.05;
  font-weight: 900;
  letter-spacing: 0.04em;
}

.visual-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.5;
  font-weight: 500;
  opacity: 0.95;
  letter-spacing: 0.05em;
}

.auth-card {
  top: 50%;
  right: 120px;
  width: min(100%, 470px);
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 34px 30px;
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.08));
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(10px);
}

.card-head h2 {
  margin: 0;
  font-size: 34px;
  color: #f8fafc;
}

.card-head p:last-child {
  margin: 12px 0 0;
  color: rgba(241, 245, 249, 0.82);
  font-size: 16px;
}

.login-tabs {
  margin-top: 28px;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(255, 255, 255, 0.12);
}

:deep(.el-tabs__item) {
  height: 42px;
  font-size: 15px;
  font-weight: 700;
  color: rgba(241, 245, 249, 0.72);
}

:deep(.el-tabs__item.is-active) {
  color: #ffffff;
}

:deep(.el-tabs__active-bar) {
  background-color: #a7f3d0;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.18) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(167, 243, 208, 0.9) inset;
}

:deep(.el-input__inner) {
  color: #f8fafc;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(226, 232, 240, 0.68);
}

:deep(.el-link) {
  color: #dcfce7;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px;
  gap: 12px;
  width: 100%;
}

.captcha-image {
  height: 50px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.16);
  overflow: hidden;
  cursor: pointer;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-image span {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(226, 232, 240, 0.82);
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.88), rgba(20, 184, 166, 0.82));
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 14px 28px rgba(16, 185, 129, 0.24);
}

.form-foot {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1080px) {
  .auth-shell {
    min-height: auto;
    display: block;
  }

  .auth-visual {
    position: relative;
    inset: auto;
    width: auto;
    min-height: 280px;
  }

  .auth-card {
    position: relative;
    top: auto;
    right: auto;
    width: auto;
    margin: 0 24px 24px;
    transform: none;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: 16px;
  }

  .auth-card,
  .auth-visual {
    padding: 28px 22px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }
}
</style>
