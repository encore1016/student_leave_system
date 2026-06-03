<template>
  <div class="home-page">
    <div class="wave-header">
      <div class="header-bg">
        <div class="header-content">
          <div class="user-info">
            <div class="avatar-letter" :style="{ background: avatarColors[(userInfo.name || '').charCodeAt(0) % avatarColors.length] }">
              {{ (userInfo.name || '?').charAt(0) }}
            </div>
            <div class="user-text">
              <div class="greeting">欢迎回来</div>
              <div class="user-name">{{ userInfo.name }}</div>
              <div class="user-role">{{ roleText }}</div>
            </div>
          </div>
        </div>
      </div>
      <svg class="wave-svg" viewBox="0 0 1440 120" preserveAspectRatio="none">
        <path d="M0,60 C360,120 720,0 1440,60 L1440,120 L0,120 Z" fill="var(--bg)"/>
      </svg>
    </div>

    <div class="body-content">
      <div class="stats-row">
        <div class="stat-card" @click="goToLeave('PENDING')">
          <div class="stat-icon pending">
            <van-icon name="clock-o" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </div>
        <div class="stat-card" @click="goToLeave('APPROVED')">
          <div class="stat-icon approved">
            <van-icon name="success" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.approved }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
        <div class="stat-card" @click="goToLeave('REJECTED')">
          <div class="stat-icon rejected">
            <van-icon name="fail" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.rejected }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </div>
      </div>

      <van-notice-bar
        v-if="returnReminder.length > 0"
        left-icon="volume-o"
        :text="userStore.isStudent ? `您有${returnReminder.length}条请假即将到期，请及时销假` : `您有${returnReminder.length}条销假申请待审批`"
        @click="showReminderList = true"
        class="notice-bar-custom"
      />

      <div class="section-card">
        <div class="section-header">
          <div class="section-title">
            <van-icon name="apps-o" />
            <span>快捷操作</span>
          </div>
        </div>
        <div class="quick-actions">
          <div v-if="userStore.isStudent" class="action-item" @click="goToApply">
            <div class="action-icon leave">
              <van-icon name="edit" />
            </div>
            <span>申请请假</span>
          </div>
          <div class="action-item" @click="goToLeave()">
            <div class="action-icon history">
              <van-icon name="orders-o" />
            </div>
            <span>{{ leaveButtonText }}</span>
          </div>
          <div class="action-item" @click="handleReminderClick">
            <div class="action-icon return">
              <van-icon name="clock-o" />
            </div>
            <span>{{ userStore.isStudent ? '销假记录' : '销假审批' }}</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <div class="section-header">
          <div class="section-title">
            <van-icon name="notes-o" />
            <span>最近申请</span>
          </div>
          <span class="section-more" @click="goToLeave()">查看全部</span>
        </div>
        <van-empty v-if="recentLeaves.length === 0" description="暂无请假记录" />
        <div v-else class="timeline-leaves">
          <div
            v-for="(item, index) in recentLeaves"
            :key="item.id"
            class="timeline-item"
            @click="goToDetail(item.id)"
          >
            <div class="timeline-dot" :class="getStatusType(item.status)"></div>
            <div class="timeline-line" v-if="index < recentLeaves.length - 1"></div>
            <div class="timeline-card">
              <div class="timeline-header">
                <span class="tl-type">{{ getLeaveTypeText(item.type) }}</span>
                <van-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</van-tag>
              </div>
              <div class="timeline-body">
                <div class="tl-date">
                  <van-icon name="clock-o" />
                  {{ formatDate(item.startTime) }} 至 {{ formatDate(item.endTime) }}
                </div>
                <div class="tl-reason">{{ item.reason }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <van-popup v-model:show="showReminderList" position="bottom" :style="{ height: '60%' }" round>
      <div class="reminder-popup">
        <div class="popup-header">
          <span>{{ userStore.isStudent ? '销假记录' : '待审批销假申请' }}</span>
          <van-icon name="cross" @click="showReminderList = false" />
        </div>
        <van-empty v-if="userStore.isStudent ? returnRecords.length === 0 : returnReminder.length === 0" :description="userStore.isStudent ? '暂无销假记录' : '暂无待审批申请'" />
        <div v-else-if="!userStore.isStudent" class="reminder-list">
          <div v-for="item in returnReminder" :key="item.id" class="reminder-item">
            <div class="reminder-info">
              <div class="reminder-type">{{ item.studentName }} - {{ getLeaveTypeText(item.leaveType) }}</div>
              <div class="reminder-remark" v-if="item.remark">备注：{{ item.remark }}</div>
              <div class="reminder-time">申请时间：{{ formatDate(item.applyTime) }}</div>
            </div>
            <div class="approval-buttons">
              <van-button type="success" size="small" @click="handleApproveReturn(item.id, true)">通过</van-button>
              <van-button type="danger" size="small" plain @click="handleApproveReturn(item.id, false)">驳回</van-button>
            </div>
          </div>
        </div>
        <div v-else class="return-records-list">
          <div v-for="item in returnRecords" :key="item.leaveId" class="return-record-item" @click="goToDetail(item.leaveId)">
            <div class="record-header">
              <div class="record-type">{{ getLeaveTypeText(item.leaveType) }}</div>
              <van-tag :type="getReturnStatusType(item.returnStatus)">{{ getReturnStatusText(item.returnStatus) }}</van-tag>
            </div>
            <div class="record-info-row">
              <van-icon name="clock-o" />
              <span>{{ formatDate(item.startTime) }} 至 {{ formatDate(item.endTime) }}</span>
            </div>
            <div class="record-info-row">
              <van-icon name="calendar-o" />
              <span>{{ item.days }}天 · {{ item.destination }}</span>
            </div>
            <div class="record-reason">{{ item.reason }}</div>
            <div class="record-detail">
              <div class="record-time">销假申请时间：{{ formatDate(item.applyTime) }}</div>
              <div class="record-remark" v-if="item.remark">备注：{{ item.remark }}</div>
              <div class="record-opinion" v-if="item.approvalOpinion">审批意见：{{ item.approvalOpinion }}</div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showReturnDialog" position="bottom" :style="{ height: '50%' }" round>
      <div class="return-dialog-content">
        <div class="popup-header">
          <span class="popup-title">申请销假</span>
          <van-icon name="cross" @click="showReturnDialog = false" class="close-icon" />
        </div>
        <div class="dialog-body">
          <van-field
            v-model="returnRemark"
            type="textarea"
            rows="5"
            placeholder="请输入销假备注（可选）"
            autosize
            label="备注说明"
            label-align="top"
          />
        </div>
        <div class="dialog-footer">
          <van-button type="primary" block round :loading="loadingReturn" @click="handleSubmitReturn">提交申请</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import { getLeaveList, getReturnReminder, returnLeave, getPendingReturnApplicationList, applyReturn, approveReturnApplication, getMyReturnRecords } from '@/api/leave'

const router = useRouter()
const userStore = useUserStore()

const avatarColors = ['#1d4ed8', '#2563eb', '#0284c7', '#059669', '#7c3aed', '#db2777', '#d97706']

const userInfo = computed(() => userStore.userInfo)
const roleText = computed(() => {
  const roleMap = { STUDENT: '学生', COUNSELOR: '辅导员', SECRETARY: '副书记', ADMIN: '管理员' }
  return roleMap[userInfo.value.role] || ''
})

const leaveButtonText = computed(() => {
  return userInfo.value.role === 'STUDENT' ? '我的请假' : '学生请假'
})

const stats = ref({ pending: 0, approved: 0, rejected: 0 })
const recentLeaves = ref([])
const returnReminder = ref([])
const showReminderList = ref(false)
const returnRecords = ref([])

const loadData = async () => {
  try {
    const leaves = await getLeaveList()
    stats.value.pending = leaves.filter(item => item.status === 'PENDING').length
    stats.value.approved = leaves.filter(item => item.status === 'APPROVED').length
    stats.value.rejected = leaves.filter(item => item.status === 'REJECTED').length
    recentLeaves.value = leaves.slice(0, 5)
    if (userStore.isStudent) {
      const reminders = await getReturnReminder()
      returnReminder.value = reminders
      const records = await getMyReturnRecords()
      returnRecords.value = records
    } else {
      const pendingApplications = await getPendingReturnApplicationList()
      returnReminder.value = pendingApplications
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const goToApply = () => router.push('/leave/apply')
const goToLeave = (status) => router.push({ path: '/leave', query: status ? { status } : {} })
const goToDetail = (id) => router.push(`/leave/detail/${id}`)
const handleReminderClick = () => { showReminderList.value = true }

const handleApproveReturn = (applicationId, approved) => {
  const title = approved ? '确认通过' : '确认驳回'
  const message = approved ? '确认要通过该销假申请吗？' : '确认要驳回该销假申请吗？'
  showDialog({ title, message, showCancelButton: true }).then(async () => {
    try {
      await approveReturnApplication(applicationId, { approved, opinion: approved ? '同意销假' : '驳回申请' })
      showDialog({ title: '提示', message: approved ? '已通过' : '已驳回', confirmButtonText: '知道了' }).then(() => loadData())
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '审批失败，请稍后重试'
      showDialog({ title: '审批失败', message: errorMsg, confirmButtonText: '知道了' })
    }
  }).catch(() => {})
}

const showReturnDialog = ref(false)
const returnRemark = ref('')
const currentReturnLeaveId = ref(null)
const loadingReturn = ref(false)

const handleReturn = async (leaveId) => {
  if (userStore.isStudent) {
    currentReturnLeaveId.value = leaveId
    returnRemark.value = ''
    showReturnDialog.value = true
  } else {
    showDialog({ title: '确认销假', message: '确认要进行销假操作吗？' }).then(async () => {
      try {
        await returnLeave(leaveId)
        showDialog({ title: '提示', message: '销假成功', confirmButtonText: '知道了' }).then(() => { showReminderList.value = false; loadData() })
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.message || '销假失败，请稍后重试'
        showDialog({ title: '销假失败', message: errorMsg, confirmButtonText: '知道了' })
      }
    }).catch(() => {})
  }
}

const handleSubmitReturn = async () => {
  loadingReturn.value = true
  try {
    await applyReturn(currentReturnLeaveId.value, { remark: returnRemark.value || '申请销假' })
    showReturnDialog.value = false
    showDialog({ title: '提示', message: '销假申请已提交', confirmButtonText: '知道了' }).then(() => { showReminderList.value = false; loadData() })
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '销假申请失败，请稍后重试'
    showDialog({ title: '申请失败', message: errorMsg, confirmButtonText: '知道了' })
  } finally { loadingReturn.value = false }
}

const getLeaveTypeText = (type) => {
  const typeMap = { SICK: '病假', PERSONAL: '事假', OTHER: '其他' }
  return typeMap[type] || type
}

const getStatusText = (status) => {
  const statusMap = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已撤销' }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'default' }
  return typeMap[status] || 'default'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 16).replace(' ', ' ')
}

const getReturnStatusText = (status) => {
  const statusMap = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return statusMap[status] || status
}

const getReturnStatusType = (status) => {
  const typeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return typeMap[status] || 'default'
}

onMounted(() => { loadData() })
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg);
}

.wave-header {
  position: relative;
}

.header-bg {
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 55%, #06b6d4 100%);
  padding: 48px 20px 0;
  position: relative;
  overflow: hidden;
}

.header-content {
  position: relative;
  z-index: 2;
  padding-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-letter {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-text {
  flex: 1;
}

.greeting {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  margin-bottom: 2px;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  color: white;
  margin-bottom: 2px;
  letter-spacing: 0.5px;
}

.user-role {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  display: inline-block;
  background: rgba(255,255,255,0.12);
  padding: 2px 12px;
  border-radius: var(--radius-full);
}

.wave-svg {
  display: block;
  width: 100%;
  height: 40px;
  margin-top: -1px;
}

.body-content {
  padding: 0 16px 80px;
  margin-top: -12px;
  position: relative;
  z-index: 2;
}

.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background: var(--card-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-md);
  padding: 16px 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:active {
  transform: scale(0.96);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}

.stat-icon.approved {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669;
}

.stat-icon.rejected {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: #dc2626;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card:nth-child(1) .stat-value { color: #d97706; }
.stat-card:nth-child(2) .stat-value { color: #059669; }
.stat-card:nth-child(3) .stat-value { color: #dc2626; }

.stat-label {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.notice-bar-custom {
  margin-bottom: 16px;
  border-radius: var(--radius-md);
  overflow: hidden;
  --van-notice-bar-background: linear-gradient(135deg, #fef3c7, #fde68a);
  --van-notice-bar-text-color: #92400e;
  --van-notice-bar-left-icon-color: #d97706;
}

.section-card {
  background: var(--card-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-title .van-icon {
  color: var(--primary);
  font-size: 18px;
}

.section-more {
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  font-weight: 500;
}

.section-more:active {
  opacity: 0.7;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 0;
}

.action-item:active {
  transform: scale(0.94);
}

.action-item span {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: var(--shadow-sm);
}

.action-icon.leave {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: var(--primary);
}

.action-icon.history {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669;
}

.action-icon.return {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}

.timeline-leaves {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 12px;
  position: relative;
  cursor: pointer;
  padding-left: 4px;
}

.timeline-item + .timeline-item {
  margin-top: 16px;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
  position: relative;
  z-index: 2;
  box-shadow: 0 0 0 3px rgba(255,255,255,0.8);
}

.timeline-dot.warning { background: linear-gradient(135deg, #f59e0b, #d97706); }
.timeline-dot.success { background: linear-gradient(135deg, #10b981, #059669); }
.timeline-dot.danger { background: linear-gradient(135deg, #ef4444, #dc2626); }
.timeline-dot.default { background: linear-gradient(135deg, #94a3b8, #64748b); }

.timeline-line {
  position: absolute;
  left: 7px;
  top: 20px;
  bottom: -16px;
  width: 2px;
  background: linear-gradient(to bottom, var(--primary-lighter), transparent);
  z-index: 1;
}

.timeline-card {
  flex: 1;
  background: rgba(249, 250, 251, 0.8);
  border-radius: var(--radius-sm);
  padding: 12px 14px;
  min-height: 0;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tl-type {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.tl-date {
  font-size: 12px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.tl-date .van-icon {
  font-size: 13px;
}

.tl-reason {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reminder-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
  font-size: 16px;
  font-weight: 600;
}

.popup-header .van-icon {
  font-size: 20px;
  color: var(--text-tertiary);
  cursor: pointer;
}

.reminder-list, .return-records-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(249, 250, 251, 0.8);
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
}

.reminder-info { flex: 1; }

.reminder-type {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.reminder-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.reminder-remark {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 6px 8px;
  background: rgba(59, 130, 246, 0.06);
  border-radius: 4px;
  border-left: 3px solid var(--primary);
  margin: 4px 0;
}

.approval-buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 12px;
}

.return-record-item {
  background: var(--card-bg);
  border-radius: var(--radius-sm);
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.return-record-item:active {
  background: rgba(249, 250, 251, 0.5);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.record-type {
  font-size: 15px;
  font-weight: 600;
}

.record-info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.record-reason {
  font-size: 14px;
  color: var(--text-primary);
  margin: 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.record-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(59, 130, 246, 0.06);
}

.record-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.record-remark, .record-opinion {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 6px 8px;
  background: rgba(59, 130, 246, 0.06);
  border-radius: 4px;
  border-left: 3px solid var(--primary);
}

.record-opinion {
  border-left-color: #10b981;
}

.popup-title {
  font-size: 18px;
  font-weight: 600;
}

.close-icon {
  font-size: 20px;
  color: var(--text-tertiary);
  cursor: pointer;
}

.return-dialog-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dialog-body {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}

.dialog-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(59, 130, 246, 0.06);
}
</style>
