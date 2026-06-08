<template>
  <div class="leave-page">
    <van-nav-bar title="请假管理" fixed placeholder />

    <div class="pill-tabs">
      <div
        v-for="tab in tabList"
        :key="tab.name"
        class="pill-tab"
        :class="{ active: activeTab === tab.name }"
        @click="onTabChange(tab.name)"
      >
        {{ tab.label }}
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty v-if="list.length === 0 && finished" description="暂无请假记录" />
        <div v-else class="leave-list">
          <div
            v-for="item in list"
            :key="item.id"
            class="leave-card"
            @click="goToDetail(item.id)"
          >
            <div class="card-shine"></div>
            <div class="card-top">
              <div class="card-badge" :class="getStatusType(item.status)">
                <span class="badge-dot"></span>
                {{ getStatusText(item.status) }}
              </div>
              <div class="card-type">{{ getLeaveTypeText(item.type) }}</div>
            </div>
            <div class="card-meta">
              <div class="meta-row">
                <van-icon name="clock-o" />
                <span>{{ formatDate(item.startTime) }} 至 {{ formatDate(item.endTime) }}</span>
              </div>
              <div class="meta-row">
                <van-icon name="calendar-o" />
                <span>{{ item.days }}天 · {{ item.destination }}</span>
              </div>
            </div>
            <div class="card-reason">{{ item.reason }}</div>
            <div class="card-actions" v-if="item.status === 'PENDING' || item.status === 'APPROVED'">
              <button
                v-if="(item.userUsername === userStore.userInfo.username || userStore.isAdmin) && item.status === 'PENDING'"
                class="action-btn danger"
                @click.stop="handleCancel(item.id)"
              >
                <van-icon name="close" /> 撤销
              </button>
              <button
                v-if="!userStore.isStudent && item.status === 'APPROVED' && !item.leaveReturn"
                class="action-btn primary"
                @click.stop="handleReturn(item.id)"
              >
                <van-icon name="success" /> 销假
              </button>
              <button
                v-if="userStore.isStudent && item.status === 'APPROVED' && !item.leaveReturn"
                class="action-btn warning"
                @click.stop="handleApplyReturn(item.id)"
              >
                <van-icon name="clock-o" /> 提醒销假
              </button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <div class="fab" v-if="userStore.isStudent" @click="goToApply">
      <van-icon name="plus" size="22" />
      <span>申请</span>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getLeaveList, cancelLeave, returnLeave, applyReturn } from '@/api/leave'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentReturnLeaveId = ref(null)
const returnRemark = ref('')
const showReturnDialog = ref(false)
const loadingReturn = ref(false)

const activeTab = ref('')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const tabList = [
  { label: '全部', name: '' },
  { label: '待审批', name: 'PENDING' },
  { label: '已通过', name: 'APPROVED' },
  { label: '已驳回', name: 'REJECTED' },
]

const loadData = async () => {
  try {
    const params = activeTab.value ? { status: activeTab.value } : {}
    const data = await getLeaveList(params)
    list.value = data
    finished.value = true
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const onLoad = async () => { await loadData(); loading.value = false }

const onRefresh = async () => { finished.value = false; await loadData(); refreshing.value = false }

const onTabChange = (name) => {
  activeTab.value = name
  list.value = []
  finished.value = false
  loadData()
}

const goToApply = () => router.push('/leave/apply')
const goToDetail = (id) => router.push(`/leave/detail/${id}`)

const handleCancel = (id) => {
  showDialog({ title: '确认撤销', message: '确认要撤销该请假申请吗？' }).then(async () => {
    try {
      await cancelLeave(id)
      showDialog({ title: '提示', message: '撤销成功', confirmButtonText: '知道了' })
      onRefresh()
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '撤销失败，请稍后重试'
      showDialog({ title: '撤销失败', message: errorMsg, confirmButtonText: '知道了' })
    }
  }).catch(() => {})
}

const handleReturn = (leaveId) => {
  showDialog({ title: '确认销假', message: '确认要进行销假操作吗？' }).then(async () => {
    try {
      await returnLeave(leaveId)
      showDialog({ title: '提示', message: '销假成功', confirmButtonText: '知道了' })
      onRefresh()
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '销假失败，请稍后重试'
      showDialog({ title: '销假失败', message: errorMsg, confirmButtonText: '知道了' })
    }
  }).catch(() => {})
}

const handleApplyReturn = (leaveId) => {
  currentReturnLeaveId.value = leaveId
  returnRemark.value = ''
  showReturnDialog.value = true
}

const handleSubmitReturn = async () => {
  loadingReturn.value = true
  try {
    await applyReturn(currentReturnLeaveId.value, { remark: returnRemark.value || '申请销假' })
    showReturnDialog.value = false
    showDialog({ title: '提示', message: '销假申请已提交', confirmButtonText: '知道了' }).then(() => onRefresh())
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

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 16).replace(' ', ' ')
}

onMounted(() => {
  if (route.query.status) activeTab.value = route.query.status
})
</script>

<style scoped>
.leave-page {
  min-height: 100vh;
  background: var(--bg);
}

.pill-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px 4px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  position: sticky;
  top: 46px;
  z-index: 10;
  background: var(--bg);
}

.pill-tab {
  flex-shrink: 0;
  padding: 8px 18px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(59, 130, 246, 0.08);
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.pill-tab:active {
  transform: scale(0.94);
}

.pill-tab.active {
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  color: white;
  border-color: transparent;
  box-shadow: var(--shadow-glow);
}

.leave-list {
  padding: 8px 16px 100px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.leave-card {
  background: var(--card-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-md);
  padding: 18px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.leave-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-md);
}

.card-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--accent), var(--primary));
  opacity: 0.5;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: var(--radius-full);
}

.card-badge .badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.card-badge.warning {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}
.card-badge.warning .badge-dot { background: #d97706; }

.card-badge.success {
  background: rgba(5, 150, 105, 0.1);
  color: #059669;
}
.card-badge.success .badge-dot { background: #059669; }

.card-badge.danger {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}
.card-badge.danger .badge-dot { background: #dc2626; }

.card-badge.default {
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}
.card-badge.default .badge-dot { background: #64748b; }

.card-type {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-row .van-icon {
  font-size: 14px;
  color: var(--text-tertiary);
}

.card-reason {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  padding-top: 10px;
  border-top: 1px solid rgba(59, 130, 246, 0.06);
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(59, 130, 246, 0.06);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:active {
  transform: scale(0.94);
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  color: white;
  box-shadow: 0 2px 8px rgba(29, 78, 216, 0.25);
}

.action-btn.danger {
  background: rgba(220, 38, 38, 0.08);
  color: #dc2626;
  border: 1px solid rgba(220, 38, 38, 0.15);
}

.action-btn.warning {
  background: rgba(245, 158, 11, 0.08);
  color: #d97706;
  border: 1px solid rgba(245, 158, 11, 0.15);
}

.fab {
  position: fixed;
  right: 20px;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 14px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--primary), var(--primary-mid));
  box-shadow: 0 8px 32px rgba(29, 78, 216, 0.35);
  color: white;
  cursor: pointer;
  z-index: 999;
  transition: all 0.3s ease;
  border: 1px solid rgba(255,255,255,0.1);
}

.fab:active {
  transform: scale(0.92);
  box-shadow: 0 4px 16px rgba(29, 78, 216, 0.25);
}

.fab span {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
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
