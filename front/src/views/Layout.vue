<template>
  <div class="layout">
    <router-view />
    <div class="custom-tabbar">
      <div
        v-for="tab in tabs"
        :key="tab.to"
        class="tab-item"
        :class="{ active: isActive(tab.to) }"
        @click="goTo(tab.to)"
      >
        <div class="tab-icon-wrap" :class="{ 'center-btn': tab.center }">
          <div class="tab-icon">
            <van-icon :name="tab.icon" />
          </div>
        </div>
        <div class="tab-label">{{ tab.label }}</div>
      </div>
      <div class="tabbar-indicator" :style="indicatorStyle"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tabs = computed(() => {
  const items = [
    { to: '/home', icon: 'home-o', label: '首页', center: false },
    { to: '/leave', icon: 'orders-o', label: '请假', center: false },
    { to: '/approval', icon: 'checked', label: '审批', center: false, show: !userStore.isStudent },
    { to: '/profile', icon: 'user-o', label: '我的', center: false },
  ]
  return items.filter(t => t.show !== false)
})

const isActive = (path) => {
  return route.path.startsWith(path)
}

const indicatorStyle = computed(() => {
  const idx = tabs.value.findIndex(t => isActive(t.to))
  if (idx === -1) return { opacity: 0 }
  const width = 100 / tabs.value.length
  return {
    transform: `translateX(${idx * 100}%)`,
    width: `${width}%`
  }
})

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  padding-bottom: 80px;
}

.custom-tabbar {
  position: fixed;
  bottom: 16px;
  left: 16px;
  right: 16px;
  height: 64px;
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 8px;
  box-shadow: 0 8px 40px rgba(15, 23, 42, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  z-index: 1000;
  border: 1px solid rgba(59, 130, 246, 0.15);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  position: relative;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item .tab-icon-wrap {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item .tab-icon {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.45);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item .tab-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  letter-spacing: 0.3px;
}

.tab-item.active .tab-icon {
  color: #fff;
  filter: drop-shadow(0 0 8px rgba(59, 130, 246, 0.6));
}

.tab-item.active .tab-label {
  color: #fff;
  font-weight: 600;
}

.tab-item:active {
  transform: scale(0.92);
}

.tabbar-indicator {
  position: absolute;
  bottom: 10px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-light), var(--accent));
  border-radius: 2px;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  opacity: 0;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.4);
}
</style>
