<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCartSeller, getCartItemCount, addToCart as addToCartUtil, clearCart, type CartItem } from '../utils/cart'

interface Product {
  id: string
  slug: string
  title: string
  price: number
  category: string
  thumbnailUrl: string
  description?: string
  seller?: string
}

const route = useRoute()
const router = useRouter()
const product = ref<Product | null>(null)
const selectedImageIndex = ref(0)
const quantity = ref(1)

// Cart conflict detection
const currentCartSeller = ref<string | null>(null)
const currentCartItemCount = ref(0)
const hasCartConflict = computed(() => {
    if (!product.value || !currentCartSeller.value) return false
    return currentCartSeller.value !== product.value.seller
})

// Mock data helpers
const mockImages = computed(() => {
    if (!product.value) return []
    // Generate 4 stable random images based on product ID logic
    const baseId = product.value.id.charCodeAt(product.value.id.length - 1)
    return [
        product.value.thumbnailUrl || `https://picsum.photos/600/600?random=${baseId}`,
        `https://picsum.photos/600/600?random=${baseId + 1}`,
        `https://picsum.photos/600/600?random=${baseId + 2}`,
        `https://picsum.photos/600/600?random=${baseId + 3}`,
    ]
})

const selectedImage = computed(() => {
    if (mockImages.value.length === 0) return ''
    return mockImages.value[selectedImageIndex.value]
})

const nextImage = () => {
    if (mockImages.value.length === 0) return
    selectedImageIndex.value = (selectedImageIndex.value + 1) % mockImages.value.length
}

const prevImage = () => {
    if (mockImages.value.length === 0) return
    selectedImageIndex.value = (selectedImageIndex.value - 1 + mockImages.value.length) % mockImages.value.length
}

const sellerProducts = ref([
    { id: '101', name: '고강도 시스템 강관 A급', price: 12000, img: 'https://picsum.photos/300/300?random=101' },
    { id: '102', name: '안전 발판 400*1829', price: 8500, img: 'https://picsum.photos/300/300?random=102' },
    { id: '103', name: '연결핀 (50개입)', price: 45000, img: 'https://picsum.photos/300/300?random=103' },
    { id: '104', name: '클램프 자동/구리스', price: 3200, img: 'https://picsum.photos/300/300?random=104' },
    { id: '105', name: '파이프 행거 (대)', price: 7000, img: 'https://picsum.photos/300/300?random=105' },
])

const mockSellers = ['건설자재총판', '대한철강', '안전제일자재', '현대건설자재', 'K-스틸']

// Helper to get seller based on product ID
const getSellerForProduct = (productId: string) => {
    const numericId = parseInt(productId.replace(/\D/g, '')) || 0
    return mockSellers[numericId % mockSellers.length]
}

// Check cart status
const checkCartStatus = () => {
    currentCartSeller.value = getCartSeller()
    currentCartItemCount.value = getCartItemCount()
}

onMounted(async () => {
    // Scroll to top when entering product detail page
    window.scrollTo(0, 0)
    
    const slug = route.params.slug
    try {
        const res = await fetch(`/api/products/${slug}`)
        if (res.ok) {
            const data = await res.json()
            product.value = {
                ...data,
                // Ensure fields exist if backend is partial
                title: data.name || data.title, 
                description: data.description || "본 상품은 건설 현장에서 검증된 최고급 자재입니다. 내구성이 뛰어나며 안전 인증을 통과하였습니다. 대량 구매 시 추가 할인이 가능하오니 판매자에게 문의 바랍니다. \n\n[상품 상세 특징]\n- KC 인증 완료\n- 고강도 강철 사용\n- 부식 방지 코팅 처리",
                seller: data.seller || getSellerForProduct(data.id),
            }
        } else {
            throw new Error('Product not found')
        }
    } catch (e) {
        console.warn('Backend fetch failed, using mock data for layout demo', e)
        // Fallback for layout demonstration
        const mockId = 'mock-1'
        product.value = {
            id: mockId,
            slug: slug as string,
            title: '프리미엄 시스템 비계 (예시 상품)',
            price: 15000,
            category: '시스템비계 · 동바리',
            thumbnailUrl: `https://picsum.photos/600/600?random=1`,
            description: "이 화면은 백엔드 데이터 연동 실패 시 보여지는 예시 데이터입니다.\n\n강력한 내구성과 안전성을 자랑하는 프리미엄 시스템 비계입니다. 현장에서 가장 많이 사용하는 규격으로, 호환성이 뛰어납니다.",
            seller: getSellerForProduct(mockId),
        }
    }
    // ensure index is valid
    selectedImageIndex.value = 0
    
    // Check cart status
    checkCartStatus()
})

const decreaseQty = () => { if (quantity.value > 1) quantity.value-- }
const increaseQty = () => { quantity.value++ }

const totalPrice = computed(() => {
    return (product.value?.price || 0) * quantity.value
})

// Cart Actions
const addToCart = () => {
    if (!product.value) return
    
    const cartItem: CartItem = {
        productId: product.value.id,
        productName: product.value.title,
        productSlug: product.value.slug,
        seller: product.value.seller || '',
        price: product.value.price,
        quantity: quantity.value,
        thumbnailUrl: product.value.thumbnailUrl
    }
    
    addToCartUtil(cartItem)
    checkCartStatus()
    alert(`${product.value.title} ${quantity.value}개를 장바구니에 담았습니다.`)
}

const clearAndAddToCart = () => {
    if (!product.value) return
    
    clearCart()
    addToCart()
}

const goToCart = () => {
    router.push('/cart')
}

const buyNow = () => {
    alert(`주문 작성을 시작합니다.`)
}
</script>

<template>
  <div class="bg-gray-50 min-h-screen pb-20">
      <!-- Loading State -->
      <div v-if="!product" class="flex justify-center items-center h-screen">
          <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-indigo-600"></div>
      </div>

      <div v-else class="container mx-auto px-4 py-8 max-w-6xl">
        <!-- Breadcrumb -->
        <nav class="text-sm text-gray-500 mb-6 flex items-center gap-2">
            <router-link to="/" class="hover:text-indigo-600">홈</router-link>
            <span>&gt;</span>
            <span class="hover:text-indigo-600 cursor-pointer">{{ product.category }}</span>
            <span>&gt;</span>
            <span class="text-gray-900 font-medium">{{ product.title }}</span>
        </nav>

        <!-- Top Section: Image & Info -->
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-8 mb-16 items-start">
            
            <!-- Left: Images (Span 3) -->
            <div class="lg:col-span-3 flex flex-col h-full">
                <!-- Main Image -->
                <div class="flex-1 bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 relative group select-none min-h-[400px]">
                    <img :key="selectedImageIndex" :src="selectedImage" class="absolute inset-0 w-full h-full object-contain object-center transition-opacity duration-300" alt="Product Detail">
                    <div class="absolute top-4 left-4 bg-indigo-600 text-white text-xs px-2 py-1 rounded font-bold z-10">BEST</div>
                    
                    <!-- Slider Arrows -->
                    <button 
                        @click.stop="prevImage" 
                        class="absolute left-4 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 p-3 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all duration-300 hover:scale-110 focus:outline-none z-10"
                        title="이전 이미지"
                    >
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
                    </button>
                    <button 
                        @click.stop="nextImage" 
                        class="absolute right-4 top-1/2 -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 p-3 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all duration-300 hover:scale-110 focus:outline-none z-10"
                        title="다음 이미지"
                    >
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
                    </button>

                    <!-- Page Indicator -->
                    <div class="absolute bottom-4 right-4 bg-black/60 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full z-10">
                        {{ selectedImageIndex + 1 }} / {{ mockImages.length }}
                    </div>
                </div>

                <!-- Thumbnails -->
                <div class="flex gap-4 overflow-x-auto pb-2 scrollbar-hide mt-4 h-20 flex-shrink-0">
                    <button 
                        v-for="(img, idx) in mockImages" 
                        :key="idx" 
                        @click="selectedImageIndex = idx"
                        :class="['flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden border-2 transition-all relative', selectedImageIndex === idx ? 'border-indigo-600 ring-2 ring-indigo-200' : 'border-transparent hover:border-gray-300 opacity-70 hover:opacity-100']"
                    >
                         <img :src="img" class="w-full h-full object-cover">
                         <div v-if="selectedImageIndex === idx" class="absolute inset-0 bg-indigo-600/10"></div>
                    </button>
                </div>
            </div>

            <!-- Right: Product Info & Actions (Span 2) -->
            <div class="lg:col-span-2 w-full flex flex-col h-full">
                <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 h-full flex flex-col">
                    <div class="flex justify-between items-start mb-2">
                        <span class="text-indigo-600 font-bold text-sm tracking-wide">{{ product.seller }}</span>
                    </div>
                    
                    <h1 class="text-2xl font-bold text-gray-900 mb-4 leading-tight">{{ product.title }}</h1>
                    
                    <div class="flex items-end gap-2 mb-6 border-b border-gray-100 pb-6">
                        <span class="text-3xl font-bold text-gray-900">{{ product.price.toLocaleString() }}</span>
                        <span class="text-lg text-gray-400 mb-1">원</span>
                    </div>

                    <div class="space-y-4 mb-8">
                        <div class="flex justify-between items-center text-sm text-gray-600">
                            <span>배송비</span>
                            <span class="font-medium text-gray-900">3,000원 (50,000원 이상 무료)</span>
                        </div>
                        <div class="flex justify-between items-center text-sm text-gray-600">
                            <span>배송예정</span>
                            <span class="font-medium text-gray-900">모레 도착 예정</span>
                        </div>
                    </div>

                    <!-- Quantity -->
                    <div class="bg-gray-50 p-4 rounded-lg mb-6 flex justify-between items-center">
                        <span class="font-medium text-gray-700">수량</span>
                        <div class="flex items-center bg-white border border-gray-300 rounded">
                            <button @click="decreaseQty" class="px-3 py-1 hover:bg-gray-100 text-gray-600">-</button>
                            <input type="text" :value="quantity" readonly class="w-12 text-center text-gray-900 font-bold focus:outline-none">
                            <button @click="increaseQty" class="px-3 py-1 hover:bg-gray-100 text-gray-600">+</button>
                        </div>
                    </div>

                    <!-- Total -->
                    <div class="flex justify-between items-end mb-6">
                        <span class="text-gray-600 font-medium">총 상품 금액</span>
                        <div class="text-right">
                             <span class="text-2xl font-black text-indigo-600">{{ totalPrice.toLocaleString() }}</span>
                             <span class="text-sm text-gray-500">원</span>
                        </div>
                    </div>

                    <!-- Spacer to push buttons to bottom -->
                    <div class="mt-auto"></div>

                    <!-- Cart Conflict Warning -->
                    <div v-if="hasCartConflict" class="bg-yellow-50 border-2 border-yellow-300 rounded-xl p-5">
                        <div class="flex items-center gap-3 mb-4">
                            <svg class="w-7 h-7 text-yellow-600 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                            <div class="flex-1 min-w-0">
                                <h3 class="text-base font-bold text-yellow-900 mb-1">다른 판매자 상품이 장바구니에 있습니다</h3>
                                <p class="text-sm text-yellow-800">
                                    현재 <strong>'{{ currentCartSeller }}'</strong> 상품 {{ currentCartItemCount }}개가 담겨있습니다. 한 주문에는 같은 판매자 상품만 담을 수 있습니다.
                                </p>
                            </div>
                        </div>
                        
                        <div class="flex flex-col gap-2">
                            <button 
                                @click="goToCart"
                                class="w-full bg-white border-2 border-yellow-600 text-yellow-900 py-3 px-4 rounded-lg font-bold hover:bg-yellow-50 transition flex items-center justify-center gap-2"
                            >
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                                장바구니 확인하기
                            </button>
                            <button 
                                @click="clearAndAddToCart"
                                class="w-full bg-red-500 text-white py-3 px-4 rounded-lg font-bold hover:bg-red-600 transition flex items-center justify-center gap-2"
                            >
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                                기존 상품 삭제하고 담기
                            </button>
                        </div>
                    </div>

                    <!-- Normal Buttons -->
                    <div v-else class="flex gap-3">
                        <button @click="addToCart" class="flex-1 border border-indigo-600 text-indigo-600 py-3.5 rounded-xl font-bold hover:bg-indigo-50 transition transform active:scale-95">
                            장바구니
                        </button>
                        <button @click="buyNow" class="flex-1 bg-indigo-600 text-white py-3.5 rounded-xl font-bold hover:bg-indigo-700 shadow-md hover:shadow-lg transition transform active:scale-95">
                            구매하기
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- NEW MIDDLE SECTION: Seller's Other Products -->
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 mb-16">
            <div class="flex justify-between items-center mb-6 border-b pb-4">
                <h3 class="text-xl font-bold text-gray-900">이 판매자의 다른 상품</h3>
                 <button @click="$router.push({ path: '/', query: { seller: product?.seller } })" class="text-sm text-indigo-600 font-bold hover:bg-indigo-50 px-3 py-1.5 rounded transition">
                     이 판매자의 상품 모아보기 &rarr;
                 </button>
            </div>
            
            <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
                <div v-for="item in sellerProducts" :key="item.id" class="group cursor-pointer">
                    <div class="overflow-hidden rounded-lg bg-gray-100 mb-2 border border-gray-100">
                        <img :src="item.img" class="w-full aspect-square object-cover transition-transform duration-300 group-hover:scale-105">
                    </div>
                    <h4 class="text-sm text-gray-800 line-clamp-2 group-hover:text-indigo-600 transition h-10">{{ item.name }}</h4>
                    <p class="text-sm font-bold text-gray-900 mt-1">{{ item.price.toLocaleString() }}원</p>
                </div>
            </div>
        </div>

        <!-- Middle Section: Content -->
        <div class="w-full">
            <div class="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 min-h-[500px]">
                <h3 class="text-xl font-bold text-gray-900 mb-6 pb-4 border-b">상품 상세 정보</h3>
                <div class="prose max-w-none text-gray-800 leading-relaxed whitespace-pre-line">
                    {{ product.description }}
                </div>
                <div class="mt-8 space-y-4">
                    <img :src="`https://picsum.photos/800/600?random=${product.id}99`" class="w-full rounded-lg shadow-sm">
                    <p class="text-center text-gray-500 py-4">
                        상세한 제품 스펙과 시공 방법은 위 이미지를 참고해주세요.
                    </p>
                </div>
            </div>
        </div>
      </div>
  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
