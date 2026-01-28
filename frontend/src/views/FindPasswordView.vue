<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_BASE_URL = 'http://localhost:8080'

const id = ref('')
const phone = ref('')
const verificationCode = ref('')
const isVerificationSent = ref(false)
const isVerified = ref(false)
const tempPassword = ref('')
const errorMsg = ref('')

const requestVerification = async () => {
    if (!phone.value || !id.value) {
        alert('아이디와 휴대폰 번호를 모두 입력해주세요.')
        return
    }

    try {
        const res = await axios.post(`${API_BASE_URL}/api/auth/check-user-phone`, {
            username: id.value,
            phone: phone.value
        })

        if (!res.data.exists) {
            alert('입력하신 정보와 일치하는 회원을 찾을 수 없습니다.')
            return
        }

        isVerificationSent.value = true
        alert(`인증번호가 발송되었습니다. (테스트용: 아무 번호나 입력하세요)`)
    } catch (e) {
         console.error(e)
         alert('서버 통신 중 오류가 발생했습니다.')
    }
}

const verifyCode = () => {
    if (!verificationCode.value) {
        alert('인증번호를 입력해주세요.')
        return
    }
    isVerified.value = true
    alert('인증되었습니다.')
}

const handleFindPassword = async () => {
    if (!isVerified.value) {
        alert('휴대폰 인증을 완료해주세요.')
        return
    }

    try {
        const res = await axios.post(`${API_BASE_URL}/api/auth/reset-password`, {
            username: id.value,
            phone: phone.value
        })
        tempPassword.value = res.data.tempPassword
        errorMsg.value = ''
    } catch (e: any) {
        console.error(e)
        if (e.response && e.response.data && e.response.data.message) {
            errorMsg.value = e.response.data.message
        } else {
            errorMsg.value = '비밀번호 찾기 중 오류가 발생했습니다.'
        }
        tempPassword.value = ''
    }
}
</script>

<template>
  <div class="min-h-screen flex items-start justify-center bg-gray-50 pt-32 relative">
    <div class="max-w-md w-full space-y-8 p-8 bg-white shadow rounded z-10">
      <div>
        <h2 class="text-center text-3xl font-extrabold text-gray-900">비밀번호 찾기</h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          아이디와 가입 시 등록한 휴대폰 번호로 인증하여<br>임시 비밀번호를 발급받을 수 있습니다.
        </p>
      </div>

      <div v-if="tempPassword" class="mt-8 bg-indigo-50 p-6 rounded-lg text-center">
          <p class="text-gray-600 mb-2">임시 비밀번호가 발급되었습니다.</p>
          <div class="bg-white p-3 border border-indigo-200 rounded mb-4">
              <p class="text-xl font-mono font-bold text-indigo-700 tracking-wider select-all">{{ tempPassword }}</p>
          </div>
          <p class="text-xs text-red-500 mb-6">로그인 후 반드시 비밀번호를 변경해주세요.</p>
          
          <router-link to="/login" class="inline-flex justify-center w-full py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
              로그인하러 가기
          </router-link>
      </div>

      <form v-else class="mt-8 space-y-6" @submit.prevent="handleFindPassword">
        
        <div v-if="errorMsg" class="rounded-md bg-red-50 p-4">
           <div class="text-sm text-red-700">{{ errorMsg }}</div>
        </div>

        <div class="space-y-4">
            <!-- 아이디 -->
             <div>
              <label for="id" class="block text-sm font-medium text-gray-700">아이디</label>
              <input 
                id="id" 
                v-model="id" 
                type="text" 
                required 
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                placeholder="아이디 입력"
                :disabled="isVerified"
              >
            </div>

            <!-- 휴대폰 번호 -->
            <div>
              <label class="block text-sm font-medium text-gray-700">휴대폰 번호</label>
              <div class="flex mt-1 gap-2">
                <input v-model="phone" type="tel" :disabled="isVerified" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm disabled:bg-gray-100" placeholder="- 없이 숫자만 입력">
                <button type="button" @click="requestVerification" :disabled="isVerified" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none disabled:bg-gray-400 whitespace-nowrap flex-shrink-0">
                  인증요청
                </button>
              </div>
            </div>

            <!-- 인증 번호 확인 -->
            <div v-if="isVerificationSent && !isVerified">
              <div class="flex mt-1 gap-2">
                <input v-model="verificationCode" type="text" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="인증번호 입력">
                <button type="button" @click="verifyCode" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none whitespace-nowrap flex-shrink-0">
                  확인
                </button>
              </div>
            </div>
            <div v-if="isVerified" class="text-sm text-green-600 font-medium">
              ✓ 인증되었습니다.
            </div>
        </div>

        <div>
          <button 
            type="submit" 
            :disabled="!isVerified" 
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            임시 비밀번호 발급
          </button>
        </div>

        <div class="text-center mt-4">
             <router-link to="/login" class="text-sm text-gray-500 hover:text-gray-900">로그인으로 돌아가기</router-link>
             <span class="mx-2 text-gray-300">|</span>
             <router-link to="/find-id" class="text-sm text-gray-500 hover:text-gray-900">아이디 찾기</router-link>
        </div>
      </form>
    </div>
  </div>
</template>
