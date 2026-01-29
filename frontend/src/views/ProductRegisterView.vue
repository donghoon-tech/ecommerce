<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  name: '',
  price: 0,
  stock: 0,
  description: '',
  category: '',
  image: null as File | null
})

const categories = [
  { value: 'electronics', label: '전자제품' },
  { value: 'clothing', label: '의류' },
  { value: 'books', label: '도서' },
  { value: 'furniture', label: '가구' },
  { value: 'others', label: '기타' }
]

const loading = ref(false)
const previewImage = ref<string | null>(null)

const handleImageChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    form.value.image = file
    
    // Preview
    const reader = new FileReader()
    reader.onload = (e) => {
      previewImage.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

const handleSubmit = async () => {
  if (!form.value.name || form.value.price <= 0 || form.value.stock < 0) {
    alert('필수 정보를 올바르게 입력해주세요.')
    return
  }
  
  loading.value = true
  
  // TODO: Actual API call
  console.log('Product Registering...', form.value)
  
  setTimeout(() => {
    loading.value = false
    alert('상품이 등록되었습니다.')
    router.push('/')
  }, 1000)
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-3xl mx-auto bg-white p-8 shadow rounded-lg">
      <div class="mb-8 border-b pb-4">
        <h2 class="text-2xl font-bold text-gray-900">상품 등록</h2>
        <p class="mt-1 text-sm text-gray-500">새로운 상품을 등록하여 판매를 시작해보세요.</p>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        
        <!-- 상품 기본 정보 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900">기본 정보</h3>
          
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div>
              <label for="name" class="block text-sm font-medium text-gray-700">상품명 <span class="text-red-500">*</span></label>
              <input 
                id="name" 
                v-model="form.name" 
                type="text" 
                required 
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="예: 아이폰 15 프로"
              >
            </div>

            <div>
              <label for="category" class="block text-sm font-medium text-gray-700">카테고리 <span class="text-red-500">*</span></label>
              <select 
                id="category" 
                v-model="form.category" 
                required
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              >
                <option value="" disabled>카테고리 선택</option>
                <option v-for="cat in categories" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div>
              <label for="price" class="block text-sm font-medium text-gray-700">판매 가격 (원) <span class="text-red-500">*</span></label>
              <input 
                id="price" 
                v-model.number="form.price" 
                type="number" 
                min="0" 
                required 
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="0"
              >
            </div>

            <div>
              <label for="stock" class="block text-sm font-medium text-gray-700">재고 수량 <span class="text-red-500">*</span></label>
              <input 
                id="stock" 
                v-model.number="form.stock" 
                type="number" 
                min="0" 
                required 
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="0"
              >
            </div>
          </div>
        </div>

        <!-- 상품 상세 설명 -->
        <div class="pt-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">상세 설명</h3>
          <div>
            <label for="description" class="block text-sm font-medium text-gray-700 sr-only">상세 설명</label>
            <textarea 
              id="description" 
              v-model="form.description" 
              rows="5" 
              class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              placeholder="상품에 대한 상세한 설명을 입력해주세요."
            ></textarea>
          </div>
        </div>

        <!-- 이미지 업로드 -->
        <div class="pt-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">상품 이미지</h3>
          <div class="flex items-center space-x-6">
            <div class="shrink-0">
              <div v-if="previewImage" class="h-32 w-32 object-cover rounded-lg border border-gray-200 overflow-hidden">
                <img :src="previewImage" alt="Preview" class="h-full w-full object-cover" />
              </div>
              <div v-else class="h-32 w-32 rounded-lg border-2 border-dashed border-gray-300 flex items-center justify-center bg-gray-50 text-gray-400">
                <span class="text-sm">이미지 없음</span>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">대표 이미지 (선택)</label>
              <input 
                type="file" 
                @change="handleImageChange"
                accept="image/*"
                class="mt-1 block w-full text-sm text-slate-500
                  file:mr-4 file:py-2 file:px-4
                  file:rounded-full file:border-0
                  file:text-sm file:font-semibold
                  file:bg-indigo-50 file:text-indigo-700
                  hover:file:bg-indigo-100
                "
              />
              <p class="mt-1 text-xs text-gray-500">JPG, PNG, GIF files up to 10MB</p>
            </div>
          </div>
        </div>

        <!-- 버튼 영역 -->
        <div class="pt-6 border-t flex justify-end space-x-3">
          <button 
            type="button" 
            @click="$router.back()"
            class="py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none"
          >
            취소
          </button>
          <button 
            type="submit" 
            :disabled="loading" 
            class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            {{ loading ? '등록 중...' : '상품 등록' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
