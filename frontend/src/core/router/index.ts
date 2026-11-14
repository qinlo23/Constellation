import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isAuthenticated, ensureAuthorization, hasPermission, onAuthChange } from '@/core/permission'
import { ALL_ADMIN_MENU_ITEMS } from '@/modules/admin/constants/menu'
import adminRoutes from './modules/admin.routes.js'
import frontRoutes from './modules/front.routes.js'

// export const authState = globalAuthState

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [...frontRoutes, ...adminRoutes],
  // 滚动行为：保存位置、锚点或回到顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  },
})

// 全局前置守卫 - 路由权限控制
router.beforeEach(async (to) => {
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth)
  try {
    await ensureAuthorization()
  } catch {
    if (requiresAuth) return { name: 'front-home' }
  }
  if (requiresAuth && !isAuthenticated()) {
    ElMessage.warning('请先登录以访问此页面')
    return { name: 'front-home' }
  }
  if (to.meta.publicOnly && isAuthenticated()) return { name: 'front-profile' }
  const denied = to.matched.some(record =>
    typeof record.meta.permission === 'string' && !hasPermission(record.meta.permission))
  if (denied) {
    const first = hasPermission('admin:access')
      ? ALL_ADMIN_MENU_ITEMS.find(item => hasPermission(item.permission)) : undefined
    if (to.name === 'admin-home-dashboard' && first) return first.path
    ElMessage.warning('没有访问此页面的权限')
    return { name: 'front-profile' }
  }
  return true
})

onAuthChange(() => {
  if (!isAuthenticated() && router.currentRoute.value.path.startsWith('/admin')) {
    ElMessage.warning('登录已失效，请重新登录')
    void router.replace({ name: 'front-home' })
  }
})
export default router
