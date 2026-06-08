<template>
  <div class="leave-detail-page">
    <van-nav-bar
      title="请假详情"
      left-arrow
      @click-left="onClickLeft"
      fixed
      placeholder
    />

    <div class="detail-body" v-if="detail">
      <div class="status-banner" :class="getStatusType(detail.status)">
        <div class="banner-icon">
          <van-icon :name="getStatusIcon(detail.status)" />
        </div>
        <div class="banner-text">
          <div class="banner-title">{{ getStatusText(detail.status) }}</div>
          <div class="banner-sub">{{ getLeaveTypeText(detail.type) }} · {{ detail.days }}天</div>
        </div>
      </div>

      <div class="detail-card">
        <div class="dc-title">
          <van-icon name="contact" />
          <span>基本信息</span>
        </div>
        <div class="dc-rows">
          <div class="dc-row"><span class="dc-label">申请人</span><span class="dc-value">{{ detail.userName }}</span></div>
          <div class="dc-row"><span class="dc-label">学号/工号</span><span class="dc-value">{{ detail.userUsername }}</span></div>
        </div>
      </div>

      <div class="detail-card">
        <div class="dc-title">
          <van-icon name="notes-o" />
          <span>请假信息</span>
        </div>
        <div class="dc-rows">
          <div class="dc-row"><span class="dc-label">开始时间</span><span class="dc-value">{{ detail.startTime }}</span></div>
          <div class="dc-row"><span class="dc-label">结束时间</span><span class="dc-value">{{ detail.endTime }}</span></div>
          <div class="dc-row"><span class="dc-label">请假天数</span><span class="dc-value">{{ detail.days }}天</span></div>
          <div class="dc-row"><span class="dc-label">目的地</span><span class="dc-value">{{ detail.destination }}</span></div>
          <div class="dc-row"><span class="dc-label">是否离校</span><span class="dc-value">{{ detail.leaveSchool ? '是' : '否' }}</span></div>
          <div class="dc-row reason"><span class="dc-label">请假事由</span><span class="dc-value">{{ detail.reason }}</span></div>
        </div>
      </div>

      <div class="detail-card" v-if="filteredApprovalList.length > 0">
        <div class="dc-title">
          <van-icon name="records" />
          <span>审批流程</span>
        </div>
        <van-steps direction="vertical" :active="activeStep">
          <van-step v-for="(item, index) in filteredApprovalList" :key="item.id || index">
            <template #inactive-icon>
              <van-icon name="clock-o" v-if="item.status === 'PENDING'" />
              <van-icon name="success" v-if="item.status === 'APPROVED'" color="#10b981" />
              <van-icon name="cross" v-if="item.status === 'REJECTED'" color="#ef4444" />
            </template>
            <div class="step-content">
              <div class="step-title">{{ getLevelText(item.level) }} - {{ item.approverName }}</div>
              <div class="step-status">状态：{{ getApprovalStatusText(item.status) }}</div>
              <div class="step-opinion" v-if="item.opinion">意见：{{ item.opinion.trim() }}</div>
              <div class="step-time" v-if="item.approveTime">时间：{{ item.approveTime }}</div>
            </div>
          </van-step>
        </van-steps>
      </div>

      <div v-if="reversedReturnApplications.length > 0" class="detail-card">
        <div class="dc-title">
          <van-icon name="records" />
          <span>销假审批流程{{ reversedReturnApplications.length > 1 ? ` (${reversedReturnApplications.length}次申请)` : '' }}</span>
        </div>
        <div v-for="(returnApp, appIndex) in reversedReturnApplications" :key="appIndex" class="return-app-block" :class="{ 'has-border': appIndex < reversedReturnApplications.length - 1 }">
          <div v-if="reversedReturnApplications.length > 1" class="return-index">第{{ appIndex + 1 }}次申请</div>
          <div v-if="returnApp.remark" class="return-remark">备注：{{ returnApp.remark }}</div>
          <div class="return-status-row">
            状态：<van-tag :type="getReturnApplicationStatusType(returnApp.status)">{{ getReturnApplicationStatusText(returnApp.status) }}</van-tag>
          </div>
          <div class="return-time-row">申请时间：{{ returnApp.applyTime }}</div>
          <van-steps direction="vertical" :active="getReturnActiveStep(returnApp)">
            <van-step v-for="(item, index) in getFilteredReturnList(returnApp)" :key="item.id || index">
              <template #inactive-icon>
                <van-icon name="clock-o" v-if="item.status === 'PENDING'" />
                <van-icon name="success" v-if="item.status === 'APPROVED'" color="#10b981" />
                <van-icon name="cross" v-if="item.status === 'REJECTED'" color="#ef4444" />
              </template>
              <div class="step-content">
                <div class="step-title">{{ getReturnLevelText(item.level) }} - {{ item.approverName }}</div>
                <div class="step-status">状态：{{ getApprovalStatusText(item.status) }}</div>
                <div class="step-opinion" v-if="item.opinion">意见：{{ item.opinion.trim() }}</div>
                <div class="step-time" v-if="item.approveTime">时间：{{ item.approveTime }}</div>
              </div>
            </van-step>
          </van-steps>
        </div>
      </div>

      <div class="detail-card" v-if="detail.leaveReturn">
        <div class="dc-title">
          <van-icon name="info-o" />
          <span>销假信息</span>
        </div>
        <div class="dc-rows">
          <div class="dc-row"><span class="dc-label">销假时间</span><span class="dc-value">{{ detail.leaveReturn.returnTime }}</span></div>
          <div class="dc-row">
            <span class="dc-label">是否逾期</span>
            <span class="dc-value">
              <van-tag :type="detail.leaveReturn.isOverdue === 1 ? 'danger' : 'success'">
                {{ detail.leaveReturn.isOverdue === 1 ? '逾期' : '正常' }}
              </van-tag>
            </span>
          </div>
          <div class="dc-row"><span class="dc-label">备注</span><span class="dc-value">{{ detail.leaveReturn.remark || '无' }}</span></div>
        </div>
      </div>

      <div class="detail-actions" v-if="detail.status === 'PENDING' && isOwner">
        <button class="action-button danger" @click="handleCancel">
          <van-icon name="close" /> 撤销申请
        </button>
      </div>
      <div class="detail-actions" v-if="detail.status === 'APPROVED' && !detail.leaveReturn && !userStore.isStudent">
        <button class="action-button primary" @click="handleReturn">
          <van-icon name="success" /> 销假
        </button>
      </div>
      <div class="detail-actions" v-if="detail.status === 'APPROVED' && !detail.leaveReturn && userStore.isStudent">
        <button class="action-button warning" @click="handleApplyReturn">
          <van-icon name="clock-o" /> 提醒销假
        </button>
      </div>
    </div>

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
import { ref, onMounted, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getLeaveDetail, cancelLeave, returnLeave, applyReturn } from '@/api/leave'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const currentReturnLeaveId = ref(null)
const returnRemark = ref('')
const showReturnDialog = ref(false)
const loadingReturn = ref(false)
const userStore = useUserStore()

const detail = ref(null)
const approvalList = ref([])
const returnApplications = ref([])

const isOwner = computed(() => detail.value && detail.value.userId === userStore.userInfo.userId)

const activeStep = computed(() => {
  return filteredApprovalList.value.filter(item => item.status === 'APPROVED').length
})

const reversedReturnApplications = computed(() => [...returnApplications.value].reverse())

const filteredApprovalList = computed(() => {
  const map = new Map()
  for (let i = approvalList.value.length - 1; i >= 0; i--) {
    const item = approvalList.value[i]
    if (!map.has(item.level)) map.set(item.level, item)
  }
  return Array.from(map.values()).sort((a, b) => a.level - b.level)
})

const getFilteredReturnList = (returnApp) => {
  const processes = returnApp.approvalProcesses || []
  const map = new Map()
  for (let i = processes.length - 1; i >= 0; i--) {
    const item = processes[i]
    if (!map.has(item.level)) map.set(item.level, item)
  }
  return Array.from(map.values()).sort((a, b) => a.level - b.level)
}

const getReturnActiveStep = (returnApp) => {
  const filtered = getFilteredReturnList(returnApp)
  return filtered.filter(item => item.status === 'APPROVED').length
}

const getReturnApplicationStatusText = (status) => {
  const statusMap = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
  return statusMap[status] || status
}

const getReturnApplicationStatusType = (status) => {
  const typeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'default' }
  return typeMap[status] || 'default'
}

const loadDetail = async () => {
  try {
    const id = route.params.id
    const data = await getLeaveDetail(id)
    detail.value = data
    approvalList.value = data.approvalProcesses || []
    returnApplications.value = data.returnApplications || []
  } catch (error) {
    console.error('加载详情失败:', error)
  }
}

const handleCancel = () => {
  showDialog({ title: '确认撤销', message: '确认要撤销该请假申请吗？' }).then(async () => {
    try {
      await cancelLeave(detail.value.id)
      showDialog({ title: '提示', message: '撤销成功', confirmButtonText: '知道了' }).then(() => router.back())
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '撤销失败，请稍后重试'
      showDialog({ title: '撤销失败', message: errorMsg, confirmButtonText: '知道了' })
    }
  }).catch(() => {})
}

const handleReturn = () => {
  showDialog({ title: '确认销假', message: '确认要进行销假操作吗？' }).then(async () => {
    try {
      await returnLeave(detail.value.id)
      showDialog({ title: '提示', message: '销假成功', confirmButtonText: '知道了' }).then(() => loadDetail())
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '销假失败，请稍后重试'
      showDialog({ title: '销假失败', message: errorMsg, confirmButtonText: '知道了' })
    }
  }).catch(() => {})
}

const handleApplyReturn = () => {
  currentReturnLeaveId.value = detail.value.id
  returnRemark.value = ''
  showReturnDialog.value = true
}

const handleSubmitReturn = async () => {
  loadingReturn.value = true
  try {
    await applyReturn(currentReturnLeaveId.value, { remark: returnRemark.value || '申请销假' })
    showReturnDialog.value = false
    showDialog({ title: '提示', message: '销假申请已提交', confirmButtonText: '知道了' }).then(() => loadDetail())
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '发送失败，请稍后重试'
    showDialog({ title: '发送失败', message: errorMsg, confirmButtonText: '知道了' })
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

const getStatusIcon = (status) => {
  const iconMap = { PENDING: 'clock-o', APPROVED: 'checked', REJECTED: 'fail', CANCELLED: 'minus' }
  return iconMap[status] || 'info-o'
}

const getLevelText = (level) => {
  const levelMap = { 1: '辅导员审批', 2: '副书记审批' }
  return levelMap[level] || `第${level}级审批`
}

const getReturnLevelText = (level) => {
  const levelMap = { 1: '辅导员审批', 2: '副书记审批', 3: '管理员审批' }
  return levelMap[level] || `第${level}级审批`
}

const getApprovalStatusText = (status) => {
  const statusMap = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
  return statusMap[status] || status
}

const onClickLeft = () => router.back()

onMounted(() => { loadDetail() })
</script>

<style scoped>
.leave-detail-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 80px;
}

.detail-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--radius-md);
  color: white;
}

.status-banner.warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.status-banner.success {
  background: linear-gradient(135deg, #10b981, #059669);
}

.status-banner.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.status-banner.default {
  background: linear-gradient(135deg, #94a3b8, #64748b);
}

.banner-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 2px;
}

.banner-sub {
  font-size: 13px;
  opacity: 0.85;
}

.detail-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-sm);
}

.dc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
}

.dc-title .van-icon {
  color: var(--primary);
  font-size: 17px;
}

.dc-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dc-row {
  display: flex;
  align-items: flex-start;
}

.dc-label {
  min-width: 72px;
  font-size: 13px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.dc-value {
  font-size: 14px;
  color: var(--text-primary);
}

.dc-row.reason .dc-value {
  line-height: 1.5;
  word-break: break-all;
}

.step-content {
  padding: 8px 0;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.step-status {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.step-opinion {
  font-size: 12px;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.step-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.return-app-block {
  padding: 12px 0;
}

.return-app-block.has-border {
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
  margin-bottom: 12px;
}

.return-index {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed rgba(59, 130, 246, 0.1);
}

.return-remark {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.return-status-row {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.return-time-row {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 12px;
}

.detail-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(59, 130, 246, 0.06);
}

.action-button {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-full);
  border: none;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.action-button:active { transform: scale(0.98); }

.action-button.primary {
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  color: white;
  box-shadow: var(--shadow-glow);
}

.action-button.danger {
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: white;
  box-shadow: 0 4px 16px rgba(220, 38, 38, 0.3);
}

.action-button.warning {
  background: linear-gradient(135deg, #d97706, #f59e0b);
  color: white;
  box-shadow: 0 4px 16px rgba(217, 119, 6, 0.3);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
}

.popup-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
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
