<template>
  <div class="auth-page">
    <section class="auth-shell">
      <aside class="auth-visual">
        <div class="visual-overlay"></div>
        <div class="visual-content">
          <p class="visual-kicker">REAMS</p>
          <h1>星野</h1>
        </div>
      </aside>

      <section class="auth-card">
        <div class="card-head">
          <p class="card-kicker">Create Account</p>
          <h2>创建客户账号</h2>
          <p>请填写手机号和密码，注册后可直接登录系统。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" status-icon class="auth-form">
          <el-form-item prop="phone">
            <el-input v-model.trim="form.phone" size="large" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleRegister">立即注册</el-button>
          </el-form-item>
          <div class="form-foot">
            <el-link type="primary" @click="$router.push('/login')">已有账号？返回登录</el-link>
          </div>
        </el-form>
      </section>
    </section>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api'

export default {
  name: 'CustomerRegister',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const formRef = ref()

    const form = reactive({
      phone: '',
      password: '',
      confirmPassword: ''
    })

    const validatePhone = (rule, value, callback) => {
      if (!/^1[3-9]\d{9}$/.test(String(value || '').trim())) {
        callback(new Error('请输入正确的手机号'))
        return
      }
      callback()
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
        return
      }
      callback()
    }

    const rules = {
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { validator: validatePhone, trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需要在 6 到 20 位之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    const handleRegister = async () => {
      await formRef.value.validate()
      loading.value = true
      try {
        const payload = {
          password: form.password,
          nickname: `用户${form.phone.slice(-4)}`,
          phone: form.phone,
          gender: 0,
          status: 1
        }
        await request.post('/auth/register/customer', payload)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('客户注册失败:', error)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      rules,
      loading,
      formRef,
      handleRegister
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
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-weight: 700;
}

.visual-content h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.15;
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

.auth-form {
  margin-top: 28px;
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
}
</style>
