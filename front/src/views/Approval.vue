<template>
  <div class="approval-page">
    <van-nav-bar title="审批管理" fixed placeholder />

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
        <van-empty v-if="list.length === 0 && finished" description="暂无审批记录" />
        <div v-else class="approval-list">
          <div
            v-for="item in list"
            :key="item.id"
            class="approval-card"
          >
            <div class="card-person">
              <div class="person-avatar" :style="{ background: avatarColors[(item.applicantName || '').charCodeAt(0) % avatarColors.length] }">
                {{ (item.applicantName || '?').charAt(0) }}
              </div>
              <div class="person-info">
                <div class="person-name">{{ item.applicantName || '未知' }}</div>
                <div class="person-meta">{{ item.applicantUsername || '' }}</div>
              </div>
              <div class="person-tags">
                <van-tag class="level-tag" :class="'level-' + (item.level || 0)">{{ getLevelText(item.level) }}</van-tag>
                <van-tag :type="getStatusType(item.status)" class="status-tag">{{ getStatusText(item.status) }}</van-tag>
              </div>
            </div>

            <div class="card-divider"></div>

            <div class="card-body">
              <div class="body-row">
                <span class="body-label">请假类型</span>
                <span class="body-value">{{ getLeaveTypeText(item.leaveType) }}</span>
              </div>
              <div class="body-row">
                <span class="body-label">请假时间</span>
                <span class="body-value">{{ formatDate(item.leaveStartTime) || '' }} 至 {{ formatDate(item.leaveEndTime) || '' }}</span>
              </div>
              <div class="body-row">
                <span class="body-label">请假天数</span>
                <span class="body-value">{{ item.leaveDays || 0 }}天</span>
              </div>
            </div>

            <div class="card-reason" v-if="item.leaveReason">
              <van-icon name="info-o" />
              {{ item.leaveReason }}
            </div>

            <div class="card-actions" v-if="activeTab === 'pending'">
              <button class="act-btn approve" @click="handleApproval(item, 'APPROVED')">
                <van-icon name="success" /> 通过
              </button>
              <button class="act-btn reject" @click="handleApproval(item, 'REJECTED')">
                <van-icon name="close" /> 驳回
              </button>
            </div>

            <div class="card-history" v-if="activeTab === 'history' && item.status !== 'PENDING'">
              <div class="history-row">
                <span class="h-label">审批结果</span>
                <van-tag :type="item.status === 'APPROVED' ? 'success' : 'danger'">
                  {{ item.status === 'APPROVED' ? '已通过' : '已驳回' }}
                </van-tag>
              </div>
              <div class="history-row"><span class="h-label">审批意见</span>{{ item.opinion || '无' }}</div>
              <div class="history-row"><span class="h-label">审批时间</span>{{ item.approveTime || '' }}</div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-dialog
      v-model:show="showOpinionDialog"
      title="审批意见"
      show-cancel-button
      @confirm="onConfirmApproval"
    >
      <div class="approval-dialog-content">
        <div class="section-title" v-if="currentResult === 'APPROVED' && currentApproval?.level === 1">
          选择下一级审批人
          <div class="section-hint">未选择则本级直接审批通过</div>
        </div>
        <van-field
          v-if="currentResult === 'APPROVED' && currentApproval?.level === 1"
          v-model="nextApproverName"
          is-link
          readonly
          label="审批人"
          placeholder="请选择下一级审批人"
          @click="showApproverPicker = true"
        />
        <div class="section-title">审核意见</div>
        <van-field
          v-model="opinion"
          rows="3"
          autosize
          type="textarea"
          placeholder="请输入审核意见（可选）"
        />
        <div class="section-title">快捷输入</div>
        <div class="quick-input-tags">
          <van-tag v-for="tag in quickTags" :key="tag" plain size="medium" @click="addQuickTag(tag)">
            {{ tag }}
          </van-tag>
        </div>
      </div>
    </van-dialog>

    <van-popup v-model:show="showApproverPicker" position="bottom">
      <van-picker
        :columns="approverColumns"
        @confirm="onApproverConfirm"
        @cancel="showApproverPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showDialog } from 'vant'
import { getPendingApprovals, getApprovalHistory, processApproval } from '@/api/approval'
import { getApprovers } from '@/api/user'

const activeTab = ref('pending')
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const tabList = [
  { label: '待审批', name: 'pending' },
  { label: '审批历史', name: 'history' },
]

const showOpinionDialog = ref(false)
const opinion = ref('')
const currentApproval = ref(null)
const currentResult = ref('')
const showApproverPicker = ref(false)
const nextApproverName = ref('')
const nextApproverId = ref(null)
const approverColumns = ref([])

const quickTags = ['同意审批', '情况属实', '注意安全', '加强规范']

const avatarColors = ['#1d4ed8', '#059669', '#d97706', '#7c3aed', '#db2777', '#0284c7', '#dc2626']

const loadApproverList = async () => {
  try {
    const approvers = await getApprovers()
    approverColumns.value = [
      { text: '直接结束，不再提交', value: null },
      ...approvers.map(item => ({
        text: `${item.name} - ${item.role === 'SECRETARY' ? '副书记' : '书记'}`,
        value: item.id
      }))
    ]
  } catch (error) {
    console.error('加载审批人列表失败:', error)
  }
}

const loadData = async () => {
  try {
    let data
    if (activeTab.value === 'pending') {
      data = await getPendingApprovals({ processType: 'LEAVE' })
    } else {
      data = await getApprovalHistory({ processType: 'LEAVE' })
    }
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

const handleApproval = (item, result) => {
  currentApproval.value = item
  currentResult.value = result
  opinion.value = ''
  nextApproverName.value = ''
  nextApproverId.value = null
  if (result === 'APPROVED' && item.level === 1) loadApproverList()
  showOpinionDialog.value = true
}

const onConfirmApproval = async () => {
  try {
    const params = {
      approvalId: currentApproval.value.id,
      result: currentResult.value,
      opinion: opinion.value.trim()
    }
    if (currentResult.value === 'APPROVED' && currentApproval.value.level === 1 && nextApproverId.value !== null) {
      params.nextApproverId = nextApproverId.value
    }
    await processApproval(params)
    opinion.value = ''
    nextApproverName.value = ''
    nextApproverId.value = null
    showOpinionDialog.value = false
    showDialog({ title: '提示', message: currentResult.value === 'APPROVED' ? '审批通过' : '已驳回', confirmButtonText: '知道了' }).then(() => onRefresh())
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '审批失败，请稍后重试'
    showDialog({ title: '审批失败', message: errorMsg, confirmButtonText: '知道了' })
  }
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

const getLevelText = (level) => {
  const levelMap = { 1: '辅导员审批', 2: '副书记审批' }
  return level ? (levelMap[level] || `第${level}级审批`) : ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 16).replace(' ', ' ')
}

const onApproverConfirm = ({ selectedOptions }) => {
  if (selectedOptions && selectedOptions[0]) {
    const selected = selectedOptions[0]
    if (selected.value === null) {
      nextApproverName.value = ''
      nextApproverId.value = null
    } else {
      nextApproverName.value = selected.text
      nextApproverId.value = selected.value
    }
  }
  showApproverPicker.value = false
}

const addQuickTag = (tag) => {
  opinion.value = opinion.value ? opinion.value + ' ' + tag : tag
}

onMounted(() => { loadData() })
</script>

<style scoped>
.approval-page {
  min-height: 100vh;
  background: var(--bg);
}

.pill-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px 4px;
  position: sticky;
  top: 46px;
  z-index: 10;
  background: var(--bg);
}

.pill-tab {
  flex-shrink: 0;
  padding: 8px 20px;
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

.pill-tab:active { transform: scale(0.94); }

.pill-tab.active {
  background: var(--primary);
  color: white;
  border-color: transparent;
}

.approval-list {
  padding: 8px 16px 100px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.approval-card {
  background: var(--card-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-md);
  padding: 18px;
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}


.card-person {
  display: flex;
  align-items: center;
  gap: 12px;
}

.person-avatar {
  width: 42px;
  height: 42px;
  border-radius: 21px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
}

.person-info {
  flex: 1;
  min-width: 0;
}

.person-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.person-meta {
  font-size: 12px;
  color: var(--text-tertiary);
}

.person-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.level-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  border: none;
}

.level-tag.level-1 {
  background: rgba(29, 78, 216, 0.08);
  color: var(--primary);
}

.level-tag.level-2 {
  background: rgba(124, 58, 237, 0.08);
  color: #7c3aed;
}

.level-tag:empty {
  display: none;
}

.status-tag {
  flex-shrink: 0;
}

.card-divider {
  height: 1px;
  background: rgba(59, 130, 246, 0.06);
  margin: 12px 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.body-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.body-label {
  font-size: 13px;
  color: var(--text-tertiary);
  flex-shrink: 0;
  min-width: 64px;
}

.body-value {
  font-size: 14px;
  color: var(--text-primary);
}

.card-reason {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: flex-start;
  gap: 5px;
  padding: 10px 12px;
  background: rgba(249, 250, 251, 0.6);
  border-radius: var(--radius-sm);
  line-height: 1.5;
}

.card-reason .van-icon {
  font-size: 14px;
  color: var(--primary);
  flex-shrink: 0;
  margin-top: 2px;
}

.card-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.act-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.act-btn:active { transform: scale(0.96); }

.act-btn.approve {
  background: #059669;
  color: white;
}

.act-btn.reject {
  background: rgba(220, 38, 38, 0.06);
  color: #dc2626;
  border: 1px solid rgba(220, 38, 38, 0.12);
}

.card-history {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(59, 130, 246, 0.06);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.history-row .h-label {
  color: var(--text-tertiary);
  min-width: 60px;
  font-size: 12px;
}

.approval-dialog-content {
  padding: 12px 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  margin-top: 16px;
}

.section-title:first-child { margin-top: 0; }

.section-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
  font-weight: normal;
}

.quick-input-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.quick-input-tags .van-tag { cursor: pointer; }
</style>
