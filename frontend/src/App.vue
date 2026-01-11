<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="bg-gray-100 min-h-screen text-gray-800">
     <nav class="bg-white shadow p-4 flex justify-between items-center">
       <router-link to="/" class="text-xl font-bold text-indigo-600">Ecommerce</router-link>
       <div>
         <router-link to="/login" class="mr-4 hover:text-indigo-500">Login</router-link>
       </div>
     </nav>
     <main>
       <RouterView />
     </main>
  </div>
</template>

<style>
/* Global styles if needed */
</style>
