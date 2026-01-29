import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView
        },
        {
            path: '/find-id',
            name: 'find-id',
            component: () => import('../views/FindIdView.vue')
        },
        {
            path: '/find-password',
            name: 'find-password',
            component: () => import('../views/FindPasswordView.vue')
        },
        {
            path: '/register',
            name: 'register',
            component: () => import('../views/RegisterView.vue')
        },
        {
            path: '/mypage',
            name: 'mypage',
            component: () => import('../views/MyPageView.vue')
        },
        {
            path: '/products/:slug',
            name: 'product-detail',
            component: () => import('../views/ProductDetailView.vue')
        },
        {
            path: '/cart',
            name: 'cart',
            component: () => import('../views/CartView.vue')
        },
        {
            path: '/orders',
            name: 'orders',
            component: () => import('../views/OrderListView.vue')
        },
        {
            path: '/product/register',
            name: 'product-register',
            component: () => import('../views/ProductRegisterView.vue')
        }
    ]
})

export default router
