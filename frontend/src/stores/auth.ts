import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    const isLoggedIn = ref(false)
    const username = ref('')
    const role = ref('')
    const permissions = ref<string[]>([])

    const setAuth = (user: string, userRole: string, perms: string[]) => {
        username.value = user
        role.value = userRole
        permissions.value = perms
        isLoggedIn.value = true
        
        localStorage.setItem('username', user)
        localStorage.setItem('role', userRole)
        localStorage.setItem('permissions', JSON.stringify(perms))
        // Removed token storage from localStorage (HttpOnly Cookie handling)
        localStorage.removeItem('token')
    }

    const clearAuth = () => {
        isLoggedIn.value = false
        username.value = ''
        role.value = ''
        permissions.value = []
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        localStorage.removeItem('permissions')
        localStorage.removeItem('token')
    }

    const initAuth = () => {
        const storedUser = localStorage.getItem('username')
        const storedRole = localStorage.getItem('role')
        const storedPerms = localStorage.getItem('permissions')
        
        if (storedUser && storedRole) {
            username.value = storedUser
            role.value = storedRole
            permissions.value = storedPerms ? JSON.parse(storedPerms) : []
            isLoggedIn.value = true
        }
    }

    return { isLoggedIn, username, role, permissions, setAuth, clearAuth, initAuth }
})
