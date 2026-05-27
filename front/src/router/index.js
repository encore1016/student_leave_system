import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    redirect: '/home',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', requireAuth: true }
      },
      {
        path: '/leave',
        name: 'Leave',
        component: () => import('@/views/Leave.vue'),
        meta: { title: '请假管理', requireAuth: true }
      },
      {
        path: '/approval',
        name: 'Approval',
        component: () => import('@/views/Approval.vue'),
        meta: { title: '审批管理', requireAuth: true }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requireAuth: true }
      }
    ]
  },
  {
    path: '/leave/apply',
    name: 'LeaveApply',
    component: () => import('@/views/LeaveApply.vue'),
    meta: { title: '申请请假', requireAuth: true }
  },
  {
    path: '/leave/detail/:id',
    name: 'LeaveDetail',
    component: () => import('@/views/LeaveDetail.vue'),
    meta: { title: '请假详情', requireAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.title) {
    document.title = to.meta.title + ' - 大学生请销假管理系统'
  }

  if (to.meta.requireAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
