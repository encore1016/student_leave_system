<template>
  <div class="leave-apply-page">
    <van-nav-bar
      title="申请请假"
      left-arrow
      @click-left="onClickLeft"
      fixed
      placeholder
    />

    <div class="apply-body">
      <div class="apply-card">
        <div class="card-step">
          <div class="step-dot active"></div>
          <div class="step-line"></div>
          <div class="step-dot"></div>
        </div>

        <van-form @submit="onSubmit" class="apply-form">
          <van-field
            v-model="formData.type"
            is-link
            readonly
            name="type"
            label="请假类型"
            placeholder="请选择请假类型"
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择请假类型' }]"
          />
          <van-field
            v-model="formData.startTime"
            is-link
            readonly
            name="startTime"
            label="开始时间"
            placeholder="请选择开始时间"
            @click="showStartTimePicker = true"
            :rules="[{ required: true, message: '请选择开始时间' }]"
          />
          <van-field
            v-model="formData.endTime"
            is-link
            readonly
            name="endTime"
            label="结束时间"
            placeholder="请选择结束时间"
            @click="showEndTimePicker = true"
            :rules="[{ required: true, message: '请选择结束时间' }]"
          />
          <van-field
            v-model="formData.days"
            type="number"
            name="days"
            label="请假天数"
            placeholder="请输入请假天数"
            :rules="[
              { required: true, message: '请输入请假天数' },
              { pattern: /^[1-9]\d*$/, message: '请输入正整数' }
            ]"
          />
          <van-field
            v-model="formData.destination"
            name="destination"
            label="目的地"
            placeholder="请输入目的地"
            :rules="[{ required: true, message: '请输入目的地' }]"
          />
          <van-field name="leaveSchool" label="是否离校" readonly>
            <template #input>
              <van-switch v-model="formData.leaveSchool" size="24" />
            </template>
          </van-field>
          <van-field
            v-model="formData.reason"
            rows="4"
            autosize
            type="textarea"
            name="reason"
            label="请假事由"
            placeholder="请输入请假事由"
            :rules="[{ required: true, message: '请输入请假事由' }]"
          />

          <div class="form-actions">
            <button class="submit-btn" type="submit" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              <span v-else><van-icon name="success" /> 提交申请</span>
            </button>
          </div>
        </van-form>
      </div>
    </div>

    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showStartTimePicker" position="bottom">
      <van-picker
        :columns="startDateColumns"
        v-model="selectedStartDate"
        @confirm="onStartDateConfirm"
        @cancel="showStartTimePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showStartTimeOnlyPicker" position="bottom">
      <van-picker
        :columns="startTimeColumns"
        v-model="selectedStartTime"
        @confirm="onStartTimeConfirm"
        @cancel="showStartTimeOnlyPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showEndTimePicker" position="bottom">
      <van-picker
        :columns="endDateColumns"
        v-model="selectedEndDate"
        @confirm="onEndDateConfirm"
        @cancel="showEndTimePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showEndTimeOnlyPicker" position="bottom">
      <van-picker
        :columns="endTimeColumns"
        v-model="selectedEndTime"
        @confirm="onEndTimeConfirm"
        @cancel="showEndTimeOnlyPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast, showDialog } from 'vant'
import { createLeave } from '@/api/leave'

const router = useRouter()

const minDate = new Date()
const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = now.getMonth() + 1
const currentDay = now.getDate()
const currentHour = now.getHours()
const currentMinute = now.getMinutes()

const formData = ref({
  type: '', startTime: '', endTime: '', days: '', destination: '', leaveSchool: false, reason: ''
})

const loading = ref(false)
const showTypePicker = ref(false)
const showStartTimePicker = ref(false)
const showEndTimePicker = ref(false)
const showStartTimeOnlyPicker = ref(false)
const showEndTimeOnlyPicker = ref(false)
const isStartTime = ref(true)

const generateDateColumns = () => {
  const years = []; const months = []; const days = []
  for (let i = 0; i < 5; i++) years.push({ text: `${currentYear + i}年`, value: currentYear + i })
  for (let i = 1; i <= 12; i++) months.push({ text: `${i}月`, value: i })
  for (let i = 1; i <= 31; i++) days.push({ text: `${i}日`, value: i })
  return [years, months, days]
}

const generateTimeColumns = () => {
  const hours = []; const minutes = []; const seconds = []
  for (let i = 0; i < 24; i++) hours.push({ text: `${String(i).padStart(2, '0')}时`, value: i })
  for (let i = 0; i < 60; i++) { minutes.push({ text: `${String(i).padStart(2, '0')}分`, value: i }); seconds.push({ text: `${String(i).padStart(2, '0')}秒`, value: i }) }
  return [hours, minutes, seconds]
}

const startDateColumns = ref(generateDateColumns())
const startTimeColumns = ref(generateTimeColumns())
const endDateColumns = ref(generateDateColumns())
const endTimeColumns = ref(generateTimeColumns())
const selectedStartDate = ref([currentYear, currentMonth, currentDay])
const selectedStartTime = ref([currentHour, currentMinute, currentMinute])
const selectedEndDate = ref([currentYear, currentMonth, currentDay])
const selectedEndTime = ref([currentHour, currentMinute, currentMinute])
const tempStartDate = ref('')
const tempStartTime = ref('')
const tempEndDate = ref('')
const tempEndTime = ref('')

const typeColumns = [
  { text: '病假', value: 'SICK' },
  { text: '事假', value: 'PERSONAL' },
  { text: '其他', value: 'OTHER' }
]

const onTypeConfirm = ({ selectedOptions }) => {
  formData.value.type = selectedOptions[0].text
  showTypePicker.value = false
}

const onStartDateConfirm = ({ selectedValues }) => {
  const year = selectedValues[0]; const month = String(selectedValues[1]).padStart(2, '0'); const day = String(selectedValues[2]).padStart(2, '0')
  tempStartDate.value = `${year}-${month}-${day}`
  selectedStartDate.value = [year, selectedValues[1], selectedValues[2]]
  isStartTime.value = true
  showStartTimePicker.value = false
  if (!tempStartTime.value) tempStartTime.value = `${String(currentHour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`
  showStartTimeOnlyPicker.value = true
}

const onStartTimeConfirm = ({ selectedValues }) => {
  const hours = String(selectedValues[0]).padStart(2, '0'); const minutes = String(selectedValues[1]).padStart(2, '0'); const seconds = String(selectedValues[2]).padStart(2, '0')
  tempStartTime.value = `${hours}:${minutes}`
  formData.value.startTime = `${tempStartDate.value} ${hours}:${minutes}:${seconds}`
  selectedStartTime.value = [selectedValues[0], selectedValues[1], selectedValues[2]]
  showStartTimeOnlyPicker.value = false
}

const onEndDateConfirm = ({ selectedValues }) => {
  const year = selectedValues[0]; const month = String(selectedValues[1]).padStart(2, '0'); const day = String(selectedValues[2]).padStart(2, '0')
  tempEndDate.value = `${year}-${month}-${day}`
  selectedEndDate.value = [year, selectedValues[1], selectedValues[2]]
  isStartTime.value = false
  showEndTimePicker.value = false
  if (!tempEndTime.value) tempEndTime.value = `${String(currentHour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`
  showEndTimeOnlyPicker.value = true
}

const onEndTimeConfirm = ({ selectedValues }) => {
  const hours = String(selectedValues[0]).padStart(2, '0'); const minutes = String(selectedValues[1]).padStart(2, '0'); const seconds = String(selectedValues[2]).padStart(2, '0')
  tempEndTime.value = `${hours}:${minutes}`
  formData.value.endTime = `${tempEndDate.value} ${hours}:${minutes}:${seconds}`
  selectedEndTime.value = [selectedValues[0], selectedValues[1], selectedValues[2]]
  showEndTimeOnlyPicker.value = false
}

const getTypeValue = (text) => {
  const item = typeColumns.find(col => col.text === text)
  return item ? item.value : text
}

const onSubmit = async () => {
  loading.value = true
  try {
    if (formData.value.startTime && formData.value.endTime) {
      const startTime = new Date(formData.value.startTime)
      const endTime = new Date(formData.value.endTime)
      if (endTime <= startTime) {
        showDialog({ title: '时间错误', message: '结束时间不能早于或等于开始时间，请重新选择', confirmButtonText: '知道了' })
        loading.value = false
        return
      }
    }
    const submitData = {
      ...formData.value,
      type: getTypeValue(formData.value.type),
      leaveSchool: formData.value.leaveSchool ? 1 : 0,
      days: parseInt(formData.value.days),
      destination: formData.value.destination.trim()
    }
    await createLeave(submitData)
    showDialog({ title: '提示', message: '申请成功', confirmButtonText: '知道了' }).then(() => setTimeout(() => router.back(), 500))
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '申请失败，请稍后重试'
    showDialog({ title: '申请失败', message: errorMsg, confirmButtonText: '知道了' })
  } finally { loading.value = false }
}

const onClickLeft = () => router.back()
</script>

<style scoped>
.leave-apply-page {
  min-height: 100vh;
  background: var(--bg);
}

.apply-body {
  padding: 16px 16px 40px;
}

.apply-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  padding: 24px 16px;
  box-shadow: var(--shadow-sm);
}

.card-step {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 24px;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--text-tertiary);
  flex-shrink: 0;
}

.step-dot.active {
  background: var(--primary);
  box-shadow: 0 0 0 3px rgba(29, 78, 216, 0.15);
}

.step-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary), var(--text-tertiary));
}

.apply-form {
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
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
