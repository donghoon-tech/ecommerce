<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

interface Product {
  id: string
  slug: string
  title: string
  price: number
  category: string
  thumbnailUrl: string
}

const route = useRoute()
const product = ref<Product | null>(null)

onMounted(async () => {
    const slug = route.params.slug
    try {
        const res = await fetch(`/api/products/${slug}`)
        if (res.ok) {
            product.value = await res.json()
        }
    } catch (e) {
        console.error(e)
    }
})
</script>

<template>
  <div class="container mx-auto p-4" v-if="product">
     <div class="flex flex-col md:flex-row gap-8">
        <div class="w-full md:w-1/2">
             <img :src="product.thumbnailUrl" class="w-full rounded shadow-lg" alt="Product">
        </div>
        <div class="w-full md:w-1/2">
             <h1 class="text-3xl font-bold mb-4">{{ product.title }}</h1>
             <p class="text-2xl text-gray-700 font-semibold mb-4">{{ product.price }} won</p>
             <p class="text-gray-600 mb-6">{{ product.category }}</p>
             <button class="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition">Add to Cart</button>
        </div>
     </div>
  </div>
  <div v-else class="text-center p-8">Loading...</div>
</template>
