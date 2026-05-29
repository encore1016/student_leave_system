<template>
  <div class="register-page">
    <van-nav-bar
      title="注册账号"
      left-arrow
      @click-left="onClickLeft"
      fixed
      placeholder
    />

    <div class="register-body">
      <div class="register-card">
        <div class="card-icon">
          <van-icon name="user-o" />
        </div>
        <div class="card-title">创建账号</div>
        <div class="card-subtitle">填写以下信息完成注册</div>

        <van-form @submit="onSubmit" class="register-form">
          <van-field
            v-model="formData.username"
            name="username"
            label="学号/工号"
            placeholder="请输入10位学号或工号"
            :rules="[
              { required: true, message: '请输入学号或工号' },
              { pattern: /^\d{10}$/, message: '请输入10位数字' }
            ]"
          />
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码（至少6位）"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6位' }
            ]"
          />
          <van-field
            v-model="formData.name"
            name="name"
            label="姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '请输入姓名' }]"
          />
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1\d{10}$/, message: '请输入正确的手机号' }
            ]"
          />
          <van-field
            v-model="formData.email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱（可选）"
          />
          <van-field
            v-model="formData.role"
            is-link
            readonly
            name="role"
            label="角色"
            placeholder="请选择角色"
            @click="showRolePicker = true"
            :rules="[{ required: true, message: '请选择角色' }]"
          />
          <van-field
            v-model="formData.classId"
            name="classId"
            label="班级ID"
            placeholder="请输入班级ID"
            :rules="[{ required: true, message: '请输入班级ID' }]"
          />
          <van-field
            v-model="formData.collegeId"
            name="collegeId"
            label="学院ID"
            placeholder="请输入学院ID"
            :rules="[{ required: true, message: '请输入学院ID' }]"
          />

          <div class="form-actions">
            <button class="submit-btn" type="submit" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              <span v-else>注 册</span>
            </button>
          </div>
        </van-form>
      </div>
    </div>

    <van-popup v-model:show="showRolePicker" position="bottom">
      <van-picker
        :columns="roleColumns"
        @confirm="onRoleConfirm"
        @cancel="showRolePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { register } from '@/api/user'

const router = useRouter()

const formData = ref({
  username: '', password: '', name: '', phone: '', email: '', role: '', classId: '', collegeId: ''
})

const loading = ref(false)
const showRolePicker = ref(false)

const roleColumns = [
  { text: '学生', value: 'STUDENT' },
  { text: '辅导员', value: 'COUNSELOR' },
  { text: '副书记', value: 'SECRETARY' }
]

const onRoleConfirm = ({ selectedOptions }) => {
  formData.value.role = selectedOptions[0].value
  showRolePicker.value = false
}

const onSubmit = async () => {
  loading.value = true
  try {
    await register(formData.value)
    showDialog({ title: '提示', message: '注册成功，请登录', confirmButtonText: '知道了' }).then(() => router.replace('/login'))
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '注册失败，请稍后重试'
    showDialog({ title: '注册失败', message: errorMsg, confirmButtonText: '知道了' })
  } finally { loading.value = false }
}

const onClickLeft = () => router.back()
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #1d4ed8 60%, #06b6d4 100%);
}

.register-body {
  padding: 20px 20px 40px;
}

.register-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  padding: 28px 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  margin: 0 auto 12px;
  box-shadow: var(--shadow-glow);
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 13px;
  text-align: center;
  color: var(--text-tertiary);
  margin-bottom: 24px;
}

.register-form {
  --van-cell-label-width: 80px;
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

.submit-btn:active { transform: scale(0.97); }
.submit-btn:disabled { opacity: 0.7; transform: none; }

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
</style>
