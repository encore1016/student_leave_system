<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb o1"></div>
      <div class="bg-orb o2"></div>
      <div class="bg-orb o3"></div>
    </div>

    <div class="login-header">
      <div class="logo-box">
        <span>LS</span>
      </div>
      <h1>大学生请销假管理系统</h1>
      <p>Student Leave Management System</p>
    </div>

    <div class="login-card">
      <div class="card-title">账号登录</div>

      <van-form @submit="onSubmit">
        <van-field
          v-model="formData.username"
          name="username"
          label="学号/工号"
          placeholder="请输入学号或工号"
          :rules="[{ required: true, message: '请输入学号或工号' }]"
        />
        <van-field
          v-model="formData.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />

        <div class="form-actions">
          <button class="submit-btn" type="submit" :disabled="loading">
            <span v-if="loading" class="btn-loading"></span>
            <span v-else>登 录</span>
          </button>
        </div>
      </van-form>

      <div class="form-footer">
        <span>还没有账号？</span>
        <span class="register-link" @click="goRegister">立即注册</span>
      </div>
    </div>

    <div class="login-tips">
      <p>测试账号：学生 2024000101 / 123456</p>
      <p>辅导员：2024000002 / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formData = ref({ username: '', password: '' })
const loading = ref(false)

const onSubmit = async () => {
  loading.value = true
  try {
    await userStore.login(formData.value)
    showDialog({ title: '提示', message: '登录成功', confirmButtonText: '知道了' }).then(() => router.replace('/home'))
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '登录失败，请稍后重试'
    showDialog({ title: '登录失败', message: errorMsg, confirmButtonText: '知道了' })
  } finally { loading.value = false }
}

const goRegister = () => router.push('/register')
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #1d4ed8 60%, #06b6d4 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
}

.bg-orb.o1 {
  width: 300px;
  height: 300px;
  background: #3b82f6;
  top: -80px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.bg-orb.o2 {
  width: 200px;
  height: 200px;
  background: #06b6d4;
  bottom: 100px;
  left: -60px;
  animation: float 6s ease-in-out infinite reverse;
}

.bg-orb.o3 {
  width: 150px;
  height: 150px;
  background: #8b5cf6;
  top: 50%;
  right: -40px;
  animation: float 10s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.1); }
}

.login-header {
  text-align: center;
  color: white;
  padding-top: 60px;
  margin-bottom: 32px;
  position: relative;
  z-index: 2;
}

.logo-box {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: white;
  margin: 0 auto 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-header h1 {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.login-header p {
  font-size: 13px;
  opacity: 0.6;
  letter-spacing: 2px;
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  padding: 28px 24px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  text-align: center;
}

.form-actions {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(29, 78, 216, 0.3);
  letter-spacing: 4px;
}

.submit-btn:active {
  transform: scale(0.97);
  box-shadow: 0 4px 16px rgba(29, 78, 216, 0.2);
}

.submit-btn:disabled {
  opacity: 0.7;
  transform: none;
}

.btn-loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.register-link {
  color: var(--primary);
  font-weight: 600;
  cursor: pointer;
}

.register-link:active {
  opacity: 0.7;
}

.login-tips {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  line-height: 1.8;
  margin-top: 20px;
  position: relative;
  z-index: 2;
}
</style>
