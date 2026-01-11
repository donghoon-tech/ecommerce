<script setup lang="ts">
import { onMounted, ref } from 'vue'

interface Product {
  id: string
  slug: string
  title: string
  price: number
  thumbnailUrl: string
}

const products = ref<Product[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await fetch('/api/products')
    if (res.ok) {
        products.value = await res.json()
    } else {
        error.value = `Failed to load products: ${res.statusText}`
    }
  } catch (e: any) {
    error.value = e.message || 'Unknown error'
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">Products</h1>
    
    <div v-if="loading" class="text-center py-8">
        <p class="text-gray-500">Loading products...</p>
    </div>

    <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4" role="alert">
        <p class="font-bold">Error</p>
        <p>{{ error }}</p>
        <p class="text-sm mt-2">Make sure the backend is running.</p>
    </div>

    <div v-else-if="products.length === 0" class="text-center py-8">
        <p class="text-gray-500">No products found.</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div v-for="product in products" :key="product.id" class="border p-4 rounded shadow hover:shadow-lg transition bg-white">
        <img v-if="product.thumbnailUrl" :src="product.thumbnailUrl" alt="Product Image" class="w-full h-48 object-cover mb-2 rounded">
        <h2 class="text-xl font-semibold">{{ product.title }}</h2>
        <p class="text-gray-600">{{ product.price }} won</p>
        <router-link :to="`/products/${product.slug}`" class="text-blue-500 mt-2 inline-block">View Details</router-link>
      </div>
    </div>
  </div>
</template>
