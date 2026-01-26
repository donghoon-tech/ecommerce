<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, removeFromCart, clearCart, saveCart, type CartItem } from '../utils/cart'

const router = useRouter()
const cartItems = ref<CartItem[]>([])

const loadCart = () => {
    cartItems.value = getCart()
}

onMounted(() => {
    loadCart()
})

const updateQuantity = (productId: string, delta: number) => {
    const item = cartItems.value.find(i => i.productId === productId)
    if (item) {
        item.quantity = Math.max(1, item.quantity + delta)
        saveCart(cartItems.value)
        loadCart()
    }
}

const removeItem = (productId: string) => {
    if (confirm('이 상품을 장바구니에서 삭제하시겠습니까?')) {
        removeFromCart(productId)
        loadCart()
    }
}

const clearAllItems = () => {
    if (confirm('장바구니를 전체 비우시겠습니까?')) {
        clearCart()
        loadCart()
    }
}

const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

const totalItems = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const sellerName = computed(() => {
    return cartItems.value.length > 0 ? cartItems.value[0].seller : ''
})

const goToCheckout = () => {
    if (cartItems.value.length === 0) {
        alert('장바구니에 상품이 없습니다.')
        return
    }
    alert('주문서 작성 화면으로 이동합니다.')
}

const goToProduct = (slug: string) => {
    router.push(`/products/${slug}`)
}
</script>

<template>
    <div class="bg-gray-50 min-h-screen pb-20">
        <div class="container mx-auto px-4 py-8 max-w-5xl">
            <!-- Header -->
            <div class="mb-8">
                <h1 class="text-3xl font-bold text-gray-900 mb-2">장바구니</h1>
                <p class="text-gray-600">총 <span class="font-bold text-indigo-600">{{ totalItems }}</span>개 상품</p>
            </div>

            <!-- Empty State -->
            <div v-if="cartItems.length === 0" class="bg-white rounded-2xl shadow-sm border border-gray-100 p-20 text-center">
                <svg class="w-24 h-24 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
                <h2 class="text-xl font-bold text-gray-400 mb-2">장바구니가 비어있습니다</h2>
                <p class="text-gray-500 mb-6">상품을 담아보세요</p>
                <button @click="router.push('/')" class="bg-indigo-600 text-white px-6 py-3 rounded-lg font-bold hover:bg-indigo-700 transition">
                    상품 둘러보기
                </button>
            </div>

            <!-- Cart Content -->
            <div v-else class="flex flex-col lg:flex-row gap-6">
                <!-- Cart Items -->
                <div class="flex-1 space-y-4">
                    <!-- Seller Info -->
                    <div class="bg-indigo-50 border border-indigo-200 rounded-lg p-4 flex items-center gap-3">
                        <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                        </svg>
                        <div>
                            <p class="text-sm text-indigo-700 font-medium">판매자</p>
                            <p class="text-lg font-bold text-indigo-900">{{ sellerName }}</p>
                        </div>
                    </div>

                    <!-- Cart Items List -->
                    <div v-for="item in cartItems" :key="item.productId" class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition">
                        <div class="flex gap-4">
                            <!-- Thumbnail -->
                            <div 
                                @click="goToProduct(item.productSlug)"
                                class="w-24 h-24 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0 cursor-pointer hover:opacity-75 transition"
                            >
                                <img 
                                    v-if="item.thumbnailUrl" 
                                    :src="item.thumbnailUrl" 
                                    :alt="item.productName"
                                    class="w-full h-full object-cover"
                                >
                            </div>

                            <!-- Item Info -->
                            <div class="flex-1 min-w-0">
                                <h3 
                                    @click="goToProduct(item.productSlug)"
                                    class="text-lg font-bold text-gray-900 mb-1 line-clamp-2 cursor-pointer hover:text-indigo-600 transition"
                                >
                                    {{ item.productName }}
                                </h3>
                                <p class="text-sm text-gray-500 mb-3">{{ item.seller }}</p>
                                
                                <div class="flex items-center justify-between">
                                    <!-- Quantity Control -->
                                    <div class="flex items-center bg-gray-50 border border-gray-300 rounded">
                                        <button 
                                            @click="updateQuantity(item.productId, -1)"
                                            class="px-3 py-1.5 hover:bg-gray-100 text-gray-600 font-bold"
                                        >-</button>
                                        <span class="px-4 py-1.5 text-gray-900 font-bold min-w-[3rem] text-center">{{ item.quantity }}</span>
                                        <button 
                                            @click="updateQuantity(item.productId, 1)"
                                            class="px-3 py-1.5 hover:bg-gray-100 text-gray-600 font-bold"
                                        >+</button>
                                    </div>

                                    <!-- Price -->
                                    <div class="text-right">
                                        <p class="text-xl font-bold text-indigo-600">{{ (item.price * item.quantity).toLocaleString() }}원</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Remove Button -->
                            <button 
                                @click="removeItem(item.productId)"
                                class="text-gray-400 hover:text-red-500 transition p-2"
                                title="삭제"
                            >
                                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <!-- Clear All Button -->
                    <button 
                        @click="clearAllItems"
                        class="w-full bg-gray-100 text-gray-600 py-3 rounded-lg font-bold hover:bg-gray-200 transition"
                    >
                        장바구니 전체 비우기
                    </button>
                </div>

                <!-- Order Summary (Sticky) -->
                <div class="lg:w-96">
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 sticky top-24">
                        <h2 class="text-xl font-bold text-gray-900 mb-4 pb-4 border-b">주문 요약</h2>
                        
                        <div class="space-y-3 mb-4">
                            <div class="flex justify-between text-gray-600">
                                <span>상품 금액</span>
                                <span class="font-bold text-gray-900">{{ totalPrice.toLocaleString() }}원</span>
                            </div>
                            <div class="flex justify-between text-gray-600">
                                <span>배송비</span>
                                <span class="font-bold text-gray-900">3,000원</span>
                            </div>
                        </div>

                        <div class="border-t pt-4 mb-6">
                            <div class="flex justify-between items-end">
                                <span class="text-gray-600 font-medium">총 결제금액</span>
                                <div class="text-right">
                                    <span class="text-3xl font-black text-indigo-600">{{ (totalPrice + 3000).toLocaleString() }}</span>
                                    <span class="text-sm text-gray-500 ml-1">원</span>
                                </div>
                            </div>
                        </div>

                        <button 
                            @click="goToCheckout"
                            class="w-full bg-indigo-600 text-white py-4 rounded-xl font-bold hover:bg-indigo-700 shadow-md hover:shadow-lg transition transform active:scale-95 mb-3"
                        >
                            주문하기
                        </button>
                        
                        <button 
                            @click="router.push('/')"
                            class="w-full border border-gray-300 text-gray-700 py-3 rounded-xl font-bold hover:bg-gray-50 transition"
                        >
                            쇼핑 계속하기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
