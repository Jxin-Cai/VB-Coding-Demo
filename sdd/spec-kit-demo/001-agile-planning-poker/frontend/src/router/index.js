import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/pools'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/pools',
    name: 'PoolList',
    component: () => import('../views/PoolList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/backlog',
    name: 'Backlog',
    component: () => import('../views/Backlog.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/voting/:storyCardId',
    name: 'Voting',
    component: () => import('../views/Voting.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/pools')
  } else {
    next()
  }
})

export default router
