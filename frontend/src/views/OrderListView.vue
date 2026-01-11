<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createClient } from '@supabase/supabase-js'

interface Order {
  id: string
  orderNumber: string
  status: string
  totalPrice: number
  createdAt: string
  product: {
    title: string
  }
}

const orders = ref<Order[]>([])
const loading = ref(true)

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY
const supabase = createClient(supabaseUrl, supabaseKey)

onMounted(async () => {
    // Get JWT
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) return

    try {
        const res = await fetch('/api/orders', {
            headers: {
                'Authorization': `Bearer ${session.access_token}`
            }
        })
        if (res.ok) {
            orders.value = await res.json()
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
})
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">My Orders</h1>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else class="space-y-4">
        <div v-for="order in orders" :key="order.id" class="border p-4 rounded bg-white shadow flex justify-between">
            <div>
                <p class="font-bold">{{ order.product.title }}</p>
                <p>Order #: {{ order.orderNumber }}</p>
                <p>Status: {{ order.status }}</p>
            </div>
            <div class="text-right">
                <p class="font-semibold">{{ order.totalPrice }} won</p>
                <p class="text-sm text-gray-500">{{ new Date(order.createdAt).toLocaleDateString() }}</p>
            </div>
        </div>
    </div>
  </div>
</template>
