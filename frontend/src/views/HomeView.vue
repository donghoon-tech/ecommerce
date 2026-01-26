<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface Product {
  id: string
  slug: string
  name: string
  price: number
  thumbnailUrl: string
  seller?: string
} 

const route = useRoute()
const router = useRouter()
const products = ref<Product[]>([])
const loading = ref(true)
const error = ref('')

// Search & Filter
const searchQuery = ref('')
const sortOrder = ref('latest')
const selectedSellers = ref<string[]>([])

const mockSellers = ['건설자재총판', '대한철강', '안전제일자재', '현대건설자재', 'K-스틸']

// Helper to get seller based on product ID (same as ProductDetailView)
const getSellerForProduct = (productId: string) => {
    const numericId = parseInt(productId.replace(/\D/g, '')) || 0
    return mockSellers[numericId % mockSellers.length]
}

// Pagination
const currentPage = ref(1)
const itemsPerPage = 10

onMounted(async () => {
    parseSellerQuery()
    
  try {
    const res = await fetch('/api/products')
    if (res.ok) {
        const data = await res.json()
        // Add mock seller to data for filtering demo using same logic as detail view
        products.value = data.map((p: any) => ({
            ...p,
            slug: p.slug || p.id, // Fallback to id if no slug
            seller: p.seller || getSellerForProduct(p.id)
        }))
    } else {
        error.value = `상품을 불러오는데 실패했습니다: ${res.statusText}`
    }
  } catch (e: any) {
    error.value = e.message || '알 수 없는 오류가 발생했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
})

// Parse Query Helper
const parseSellerQuery = () => {
    const query = route.query.seller
    if (Array.isArray(query)) {
        selectedSellers.value = query as string[]
    } else if (query) {
        selectedSellers.value = [query as string]
    } else {
        selectedSellers.value = []
    }
}

// Watch query change
watch(() => route.query.seller, () => {
    parseSellerQuery()
    currentPage.value = 1 // Reset page on filter change
})

// Toggle Seller Filter
const toggleSeller = (sellerName: string) => {
    const current = new Set(selectedSellers.value)
    if (current.has(sellerName)) {
        current.delete(sellerName)
    } else {
        current.add(sellerName)
    }
    
    const nextSellers = Array.from(current)
    router.push({
        query: {
            ...route.query,
            seller: nextSellers.length > 0 ? nextSellers : undefined
        }
    })
}

// Clear All Filters
const clearAllFilters = () => {
    searchQuery.value = ''
    router.push({ query: { ...route.query, seller: undefined } })
}

// Remove Single Seller Filter
const removeSellerFilter = (sellerName: string) => {
    toggleSeller(sellerName)
}

const getMockAddress = (id: string) => {
    const addresses = [
        '서울 강남구', '경기 김포시', '충북 청주시', '경남 김해시', '부산 강서구',
        '인천 서구', '경기 화성시', '충남 천안시', '전남 광양시', '경북 구미시'
    ]
    const index = id.charCodeAt(id.length - 1) % addresses.length
    return addresses[index]
}

const getMockImage = (index: number) => {
    return `https://picsum.photos/400/400?random=${index + 1}`
}

// Filtered & Sorted Products
const filteredProducts = computed(() => {
    let result = [...products.value]

    // 0. Seller Filter (Multiple)
    if (selectedSellers.value.length > 0) {
        // Mock logic: exact match
        result = result.filter(p => p.seller && selectedSellers.value.includes(p.seller))
    }

    // 1. Search
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(p => p.name.toLowerCase().includes(query))
    }

    // 2. Sort
    if (sortOrder.value === 'price-asc') {
        result.sort((a, b) => a.price - b.price)
    } else if (sortOrder.value === 'price-desc') {
        result.sort((a, b) => b.price - a.price)
    } 

    return result
})

// Pagination Logic
const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage))
const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    const end = start + itemsPerPage
    return filteredProducts.value.slice(start, end)
})

const setPage = (page: number) => {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }
}

// Category Logic
const expandedCategory = ref<string | null>(null)

const categories = [
  {
    name: '시스템비계 · 동바리',
    items: ['시스템 비계', '시스템 동바리', '동바리 K-서포트', '워킹타워']
  },
  {
    name: '유로폼 · 헌치',
    items: ['유로폼 / 부속', '인코너 / 앵글', '헌치', '쇼트폼 제작']
  },
  {
    name: '파이프 · 크램프',
    items: ['강관파이프', '크램프 / 강관핀', '각파이프']
  },
  {
    name: '써포트 · 웨일러',
    items: ['써포트', '잭써포트', '스크류잭 / 앵글잭', '합벽지지대']
  }
]

const toggleCategory = (categoryName: string) => {
  if (expandedCategory.value === categoryName) {
      expandedCategory.value = null
  } else {
      expandedCategory.value = categoryName
  }
}
</script>

<template>
<div class="container mx-auto px-4 py-8 flex items-start gap-8">
    <!-- 사이드바: 카테고리 -->
    <aside class="w-1/4 hidden md:block sticky top-24">
      <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
        <!-- Categories -->
        <h2 class="text-xl font-bold text-gray-900 mb-4 px-2 border-b border-gray-100 pb-2">카테고리</h2>
        <ul class="space-y-4 mb-8">
          <li>
            <a href="#" class="block px-3 py-2 text-indigo-600 font-bold bg-indigo-50 rounded-md whitespace-nowrap">전체보기</a>
          </li>
          
          <li v-for="category in categories" :key="category.name">
            <button 
              @click="toggleCategory(category.name)"
              class="w-full text-left flex justify-between items-center block px-3 py-1 text-sm font-bold text-gray-800 border-b border-gray-100 mb-1 whitespace-nowrap hover:text-indigo-600 focus:outline-none"
            >
              {{ category.name }}
              <svg 
                class="w-4 h-4 transform transition-transform duration-200"
                :class="expandedCategory === category.name ? 'rotate-180' : ''"
                fill="none" stroke="currentColor" viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </button>
            <transition
              enter-active-class="transition-all duration-300 ease-out"
              leave-active-class="transition-all duration-200 ease-in"
              enter-from-class="opacity-0 max-h-0"
              enter-to-class="opacity-100 max-h-40"
              leave-from-class="opacity-100 max-h-40"
              leave-to-class="opacity-0 max-h-0"
            >
              <ul v-show="expandedCategory === category.name" class="ml-2 space-y-1 overflow-hidden">
                <li v-for="item in category.items" :key="item">
                  <a href="#" class="block px-3 py-1.5 text-sm text-gray-600 hover:text-indigo-600 hover:bg-gray-50 rounded-md whitespace-nowrap">
                    {{ item }}
                  </a>
                </li>
              </ul>
            </transition>
          </li>
        </ul>
      </div>
    </aside>

    <!-- 메인 컨텐츠 -->
    <main class="w-full md:w-3/4">
      <!-- 검색 및 필터 바 -->
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100 mb-6 flex flex-col sm:flex-row justify-between items-center gap-4">
          <div class="relative w-full sm:w-96">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="상품명 검색..." 
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              >
              <div class="absolute left-3 top-2.5 text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
              </div>
          </div>
          <div class="flex items-center gap-2">
              <select v-model="sortOrder" class="border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500">
                  <option value="latest">최신순</option>
                  <option value="price-asc">낮은 가격순</option>
                  <option value="price-desc">높은 가격순</option>
              </select>
          </div>
      </div>

      <!-- Active Filters (Chips) -->
      <div v-if="selectedSellers.length > 0" class="flex items-start mb-6 -mt-2 animate-fade-in-down">
          <span class="text-sm font-bold text-gray-500 mr-3 mt-1.5 self-center">적용된 필터:</span>
          <div class="flex flex-wrap gap-2">
              <div v-for="seller in selectedSellers" :key="seller" class="flex items-center bg-indigo-50 border border-indigo-200 text-indigo-700 px-3 py-1.5 rounded-full shadow-sm">
                  <span class="text-xs font-bold uppercase mr-1 text-indigo-400">판매자</span>
                  <span class="font-bold text-sm mr-2">{{ seller }}</span>
                  <button @click="removeSellerFilter(seller)" class="text-indigo-400 hover:text-indigo-700 focus:outline-none rounded-full hover:bg-indigo-100 p-0.5 transition-colors">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                  </button>
              </div>
              <button @click="clearAllFilters" class="text-xs text-gray-500 hover:text-red-500 underline ml-2 self-center">
                  전체 해제
              </button>
          </div>
      </div>

      <!-- 상품 목록 -->
      <div v-if="loading" class="text-center py-20">
          <p class="text-gray-500 text-lg">상품 목록을 불러오는 중입니다...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-600 px-6 py-4 rounded-lg mb-8" role="alert">
          <p class="font-bold">오류 발생</p>
          <p>{{ error }}</p>
          <p class="text-sm mt-2">백엔드 서버가 실행 중인지 확인해주세요.</p>
      </div>

      <div v-else-if="products.length === 0" class="text-center py-20 bg-white rounded-lg shadow-sm w-full">
          <p class="text-gray-500 text-lg">등록된 상품이 없습니다.</p>
      </div>

      <div v-else>
          <div class="grid grid-cols-2 gap-6 min-h-[600px] content-start">
            <div v-for="(product, index) in paginatedProducts" :key="product.id" class="group bg-white border border-gray-100 rounded-lg overflow-hidden hover:shadow-xl transition-shadow duration-300 cursor-pointer h-full flex flex-col">
              <router-link :to="`/products/${product.slug}`" class="block flex-1">
                <!-- 썸네일 영역 -->
                <div class="aspect-w-1 aspect-h-1 w-full bg-gray-200 overflow-hidden relative" style="padding-top: 100%;">
                  <img 
                    :src="getMockImage(index)" 
                    alt="상품 이미지" 
                    class="absolute top-0 left-0 w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-300"
                  >
                </div>
                
                <!-- 상품 정보 영역 -->
                <div class="p-4 flex flex-col flex-1">
                  <!-- 1. 상품명 (제일 위) -->
                  <h3 class="text-lg font-bold text-gray-900 mb-1 line-clamp-2">{{ product.name }}</h3>

                  <!-- 2. 가격 (중간) -->
                  <div class="mb-3">
                      <p class="text-xl font-bold text-indigo-600">
                        {{ product.price.toLocaleString() }}원
                      </p>
                  </div>
                  
                  <!-- 3. 주소 -->
                  <div class="flex items-center text-sm text-gray-500 mb-2">
                      <svg class="w-4 h-4 mr-1 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                      {{ getMockAddress(product.id) }}
                  </div>

                  <!-- 4. 판매자 정보 (하단 고정) -->
                  <div class="mt-auto pt-3 border-t border-gray-100">
                      <div class="flex items-center text-sm text-gray-600 mb-2">
                          <svg class="w-4 h-4 mr-1 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
                          <span class="font-medium">{{ product.seller }}</span>
                      </div>
                      <button 
                          @click.prevent="$router.push({ path: '/', query: { seller: product.seller } })"
                          class="w-full text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 py-2 px-3 rounded-md transition-colors flex items-center justify-center gap-1"
                      >
                          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
                          이 판매자 상품 모아보기
                      </button>
                  </div>
                </div>
              </router-link>
            </div>
          </div>

          <!-- 페이지네이션 -->
          <div class="mt-8 flex justify-center space-x-2" v-if="totalPages > 1">
              <button 
                  @click="setPage(currentPage - 1)" 
                  :disabled="currentPage === 1"
                  class="px-3 py-1 border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                  &lt;
              </button>
              
              <button 
                  v-for="page in totalPages" 
                  :key="page"
                  @click="setPage(page)"
                  :class="['px-3 py-1 border rounded', currentPage === page ? 'bg-indigo-600 text-white border-indigo-600' : 'hover:bg-gray-50']"
              >
                  {{ page }}
              </button>

              <button 
                  @click="setPage(currentPage + 1)" 
                  :disabled="currentPage === totalPages"
                  class="px-3 py-1 border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                  &gt;
              </button>
          </div>
      </div>
    </main>
</div>
</template>
