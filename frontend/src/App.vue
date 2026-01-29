<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ref, onMounted } from 'vue'

const isLoggedIn = ref(false)
const username = ref('')

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const storedUsername = localStorage.getItem('username')
  if (token) {
    isLoggedIn.value = true
    username.value = storedUsername || '사용자'
  } else {
    isLoggedIn.value = false
    username.value = ''
  }
}

const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    isLoggedIn.value = false
    window.location.href = '/'
  }
}

onMounted(() => {
  checkLoginStatus()
})
</script>

<template>
  <div class="bg-gray-100 min-h-screen text-gray-800">
     <nav class="bg-white shadow">
       <div class="container mx-auto px-4 py-4 flex justify-between items-center">
         <div class="flex items-center space-x-8">
           <router-link to="/" class="text-2xl font-bold text-indigo-600">가설라인</router-link>
           <router-link to="/" class="text-gray-600 hover:text-indigo-600 font-semibold transition">상품목록</router-link>
           <router-link to="/product/register" class="text-gray-600 hover:text-indigo-600 font-semibold transition">상품등록</router-link>
         </div>
         <div class="flex items-center space-x-6">
           <template v-if="!isLoggedIn">
             <router-link to="/login" class="text-gray-600 hover:text-indigo-600 font-medium transition">로그인</router-link>
           </template>
           <template v-else>
             <span class="text-sm text-gray-500 hidden sm:inline-block">반갑습니다, <strong>{{ username }}</strong>님</span>
             <router-link to="/mypage" class="text-gray-600 hover:text-indigo-600 font-medium transition">마이페이지</router-link>
             <button @click="handleLogout" class="text-gray-600 hover:text-red-600 font-medium transition">로그아웃</button>
           </template>
         </div>
       </div>
     </nav>
     <main>
       <RouterView />
     </main>

     <footer class="bg-white border-t mt-12">
       <div class="container mx-auto px-4 py-8">
         <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
           <div>
             <h3 class="text-lg font-bold text-gray-900 mb-4">(주)가설라인</h3>
             <div class="text-sm text-gray-600 space-y-1">
               <p>대표자: 김대표 | 사업자등록번호: 123-45-67890</p>
               <p>주소: 서울특별시 강남구 테헤란로 123, 가설라인 타워 10층</p>
               <p>통신판매업신고: 제 2026-서울강남-01234호</p>
               <p>개인정보보호책임자: 이보안 (privacy@example.com)</p>
             </div>
           </div>
           <div>
             <h3 class="text-lg font-bold text-gray-900 mb-4">고객센터</h3>
             <div class="text-sm text-gray-600 space-y-1">
               <p class="text-xl font-bold text-indigo-600 mb-2">02-1234-5678</p>
               <p>평일 09:00 - 18:00 (점심시간 12:00 - 13:00)</p>
               <p>토/일/공휴일 휴무</p>
               <p>이메일: support@example.com</p>
             </div>
           </div>
         </div>
         <div class="border-t mt-8 pt-8 text-center text-xs text-gray-500">
           <p>© 2026 (주)가설라인. All rights reserved.</p>
         </div>
       </div>
     </footer>
  </div>
</template>

<style>
/* Global styles if needed */
</style>
