<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

interface Permission {
  id: string
  name: string
  description: string
}

interface Role {
  id: string
  name: string
  description: string
  permissions: string[]
}

const roles = ref<Role[]>([])
const allPermissions = ref<Permission[]>([])
const selectedRole = ref<Role | null>(null)
const loading = ref(false)
const saving = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 새 Role 생성 폼
const showCreateForm = ref(false)
const newRole = ref({ name: '', description: '', permissions: [] as string[] })

// 수정 중인 Role의 Permission 선택 상태
const editingPermissions = ref<string[]>([])

const authHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const PROTECTED_ROLES = ['UNVERIFIED', 'USER', 'ADMIN']

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchPermissions()])
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE_URL}/api/admin/roles`, { headers: authHeaders.value })
    roles.value = res.data
  } catch {
    errorMsg.value = 'Role 목록을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/admin/permissions`, { headers: authHeaders.value })
    allPermissions.value = res.data
  } catch {
    errorMsg.value = 'Permission 목록을 불러오는데 실패했습니다.'
  }
}

const selectRole = (role: Role) => {
  selectedRole.value = role
  editingPermissions.value = [...role.permissions]
  showCreateForm.value = false
  errorMsg.value = ''
  successMsg.value = ''
}

const togglePermission = (permName: string) => {
  const idx = editingPermissions.value.indexOf(permName)
  if (idx === -1) {
    editingPermissions.value.push(permName)
  } else {
    editingPermissions.value.splice(idx, 1)
  }
}

const saveRole = async () => {
  if (!selectedRole.value) return
  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const res = await axios.put(
      `${API_BASE_URL}/api/admin/roles/${selectedRole.value.id}`,
      {
        name: selectedRole.value.name,
        description: selectedRole.value.description,
        permissions: editingPermissions.value
      },
      { headers: authHeaders.value }
    )
    // 목록 내 해당 role 업데이트
    const idx = roles.value.findIndex(r => r.id === res.data.id)
    if (idx !== -1) roles.value[idx] = res.data
    selectedRole.value = res.data
    editingPermissions.value = [...res.data.permissions]
    successMsg.value = '저장되었습니다.'
  } catch (e: any) {
    errorMsg.value = e.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

const deleteRole = async (role: Role) => {
  if (PROTECTED_ROLES.includes(role.name)) {
    errorMsg.value = `기본 Role(${role.name})은 삭제할 수 없습니다.`
    return
  }
  if (!confirm(`"${role.name}" Role을 삭제하시겠습니까?`)) return
  try {
    await axios.delete(`${API_BASE_URL}/api/admin/roles/${role.id}`, { headers: authHeaders.value })
    roles.value = roles.value.filter(r => r.id !== role.id)
    if (selectedRole.value?.id === role.id) selectedRole.value = null
    successMsg.value = '삭제되었습니다.'
  } catch (e: any) {
    errorMsg.value = e.response?.data?.message || '삭제에 실패했습니다.'
  }
}

const createRole = async () => {
  if (!newRole.value.name.trim()) {
    errorMsg.value = 'Role 이름을 입력해주세요.'
    return
  }
  saving.value = true
  errorMsg.value = ''
  try {
    const res = await axios.post(
      `${API_BASE_URL}/api/admin/roles`,
      newRole.value,
      { headers: authHeaders.value }
    )
    roles.value.push(res.data)
    showCreateForm.value = false
    newRole.value = { name: '', description: '', permissions: [] }
    successMsg.value = '새 Role이 생성되었습니다.'
  } catch (e: any) {
    errorMsg.value = e.response?.data?.message || '생성에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

const toggleNewRolePermission = (permName: string) => {
  const idx = newRole.value.permissions.indexOf(permName)
  if (idx === -1) {
    newRole.value.permissions.push(permName)
  } else {
    newRole.value.permissions.splice(idx, 1)
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto py-8 px-4">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">권한 관리</h1>
        </div>
        <button
          @click="showCreateForm = true; selectedRole = null; errorMsg = ''; successMsg = ''"
          class="inline-flex items-center px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors"
        >
          + 새 역할 생성
        </button>
      </div>

      <!-- Alerts -->
      <div v-if="errorMsg" class="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
        {{ errorMsg }}
      </div>
      <div v-if="successMsg" class="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-md text-sm">
        {{ successMsg }}
      </div>

      <div class="grid grid-cols-3 gap-6">
        <!-- Role 목록 (좌측) -->
        <div class="col-span-1">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <div class="px-4 py-3 bg-gray-50 border-b border-gray-200">
              <h2 class="text-sm font-semibold text-gray-700">역할 목록</h2>
            </div>
            <div v-if="loading" class="p-6 text-center text-gray-400 text-sm">불러오는 중...</div>
            <ul v-else class="divide-y divide-gray-100">
              <li
                v-for="role in roles"
                :key="role.id"
                @click="selectRole(role)"
                class="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-indigo-50 transition-colors"
                :class="{ 'bg-indigo-50 border-l-2 border-indigo-500': selectedRole?.id === role.id }"
              >
                <div>
                  <div class="flex items-center gap-2">
                    <span class="text-sm font-medium text-gray-900">{{ role.name }}</span>
                    <span
                      v-if="PROTECTED_ROLES.includes(role.name)"
                      class="text-xs px-1.5 py-0.5 bg-gray-100 text-gray-500 rounded"
                    >기본</span>
                  </div>
                  <p class="text-xs text-gray-400 mt-0.5">{{ role.permissions.length }}개 권한</p>
                </div>
                <button
                  v-if="!PROTECTED_ROLES.includes(role.name)"
                  @click.stop="deleteRole(role)"
                  class="text-xs text-red-400 hover:text-red-600 transition-colors"
                >삭제</button>
              </li>
            </ul>
          </div>
        </div>

        <!-- 우측: Role 상세 or 생성 폼 -->
        <div class="col-span-2">
          <!-- Role 상세/수정 패널 -->
          <div v-if="selectedRole" class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <h2 class="text-lg font-semibold text-gray-900">{{ selectedRole.name }}</h2>
                <p class="text-sm text-gray-500 mt-0.5">{{ selectedRole.description || '설명 없음' }}</p>
              </div>
            </div>

            <div class="p-6">
              <!-- 이름/설명 수정 (기본 Role 아닌 경우에만) -->
              <div v-if="!PROTECTED_ROLES.includes(selectedRole.name)" class="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">역할 이름</label>
                  <input
                    v-model="selectedRole.name"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
                  />
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">역할 표시 이름 (설명)</label>
                  <input
                    v-model="selectedRole.description"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
                    placeholder="역할에 대한 설명 (화면에 노출됨)"
                  />
                </div>
              </div>

              <!-- Permission 바인딩 -->
              <div>
                <h3 class="text-sm font-semibold text-gray-700 mb-3">권한 매칭</h3>
                <div class="grid grid-cols-2 gap-2">
                  <label
                    v-for="perm in allPermissions"
                    :key="perm.id"
                    class="flex items-start gap-2 p-3 border rounded-md cursor-pointer hover:bg-gray-50 transition-colors"
                    :class="editingPermissions.includes(perm.name) ? 'border-indigo-400 bg-indigo-50' : 'border-gray-200'"
                  >
                    <input
                      type="checkbox"
                      :value="perm.name"
                      :checked="editingPermissions.includes(perm.name)"
                      @change="togglePermission(perm.name)"
                      class="mt-0.5 accent-indigo-600"
                    />
                    <div>
                      <div class="text-sm font-mono font-medium text-gray-800">{{ perm.name }}</div>
                      <div class="text-xs text-gray-400 mt-0.5">{{ perm.description || '-' }}</div>
                    </div>
                  </label>
                </div>
              </div>

              <!-- 저장 버튼 -->
              <div class="mt-6 flex justify-end">
                <button
                  @click="saveRole"
                  :disabled="saving"
                  class="px-5 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 disabled:opacity-50 transition-colors"
                >
                  {{ saving ? '저장 중...' : '저장' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 새 Role 생성 폼 -->
          <div v-else-if="showCreateForm" class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">새 역할 생성</h2>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">역할 이름 <span class="text-red-500">*</span></label>
                  <input
                    v-model="newRole.name"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
                    placeholder="예: MANAGER"
                  />
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 mb-1">역할 표시 이름 (설명)</label>
                  <input
                    v-model="newRole.description"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
                    placeholder="역할에 대한 설명 (화면에 노출됨)"
                  />
                </div>
              </div>
              <div>
                <h3 class="text-sm font-semibold text-gray-700 mb-3">Permission 선택</h3>
                <div class="grid grid-cols-2 gap-2">
                  <label
                    v-for="perm in allPermissions"
                    :key="perm.id"
                    class="flex items-start gap-2 p-3 border rounded-md cursor-pointer hover:bg-gray-50 transition-colors"
                    :class="newRole.permissions.includes(perm.name) ? 'border-indigo-400 bg-indigo-50' : 'border-gray-200'"
                  >
                    <input
                      type="checkbox"
                      :checked="newRole.permissions.includes(perm.name)"
                      @change="toggleNewRolePermission(perm.name)"
                      class="mt-0.5 accent-indigo-600"
                    />
                    <div>
                      <div class="text-sm font-mono font-medium text-gray-800">{{ perm.name }}</div>
                      <div class="text-xs text-gray-400 mt-0.5">{{ perm.description || '-' }}</div>
                    </div>
                  </label>
                </div>
              </div>
              <div class="mt-6 flex justify-end gap-2">
                <button
                  @click="showCreateForm = false"
                  class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                >취소</button>
                <button
                  @click="createRole"
                  :disabled="saving"
                  class="px-5 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {{ saving ? '생성 중...' : '생성' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 기본 안내 -->
          <div v-else class="bg-white rounded-lg shadow-sm border border-gray-200 flex items-center justify-center h-48">
            <p class="text-sm text-gray-400">좌측에서 역할을 선택하거나, 새 역할을 생성하세요</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
