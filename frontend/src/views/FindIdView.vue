<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_BASE_URL = 'http://localhost:8080'

const phone = ref('')
const verificationCode = ref('')
const isVerificationSent = ref(false)
const isVerified = ref(false)
const foundId = ref('')
const errorMsg = ref('')

// 상태 메시지 추가
const phoneMsg = ref({ type: '', text: '' })
const verificationMsg = ref({ type: '', text: '' })

const requestVerification = async () => {
    if (!phone.value) {
        phoneMsg.value = { type: 'error', text: '휴대폰 번호를 입력해주세요.' }
        return
    }

    try {
        const res = await axios.post(`${API_BASE_URL}/api/auth/check-phone`, {
            phone: phone.value
        })

        if (!res.data.exists) {
            phoneMsg.value = { type: 'error', text: '해당 번호로 가입된 아이디가 없습니다.' }
            return
        }

        isVerificationSent.value = true
        phoneMsg.value = { type: 'info', text: '인증번호가 발송되었습니다. (테스트용: 아무 번호나 입력하세요)' }
        verificationMsg.value = { type: '', text: '' } // 초기화
    } catch (e) {
        console.error(e)
        phoneMsg.value = { type: 'error', text: '서버 통신 중 오류가 발생했습니다.' }
    }
}

const verifyCode = () => {
    if (!verificationCode.value) {
        verificationMsg.value = { type: 'error', text: '인증번호를 입력해주세요.' }
        return
    }
    isVerified.value = true
    phoneMsg.value = { type: 'success', text: '인증이 완료되었습니다.' }
    verificationMsg.value = { type: '', text: '' }
}

const handleFindId = async () => {
    if (!isVerified.value) {
        errorMsg.value = '휴대폰 인증을 완료해주세요.'
        return
    }

    try {
        const res = await axios.post(`${API_BASE_URL}/api/auth/find-id`, {
            phone: phone.value
        })
        foundId.value = res.data.username
        errorMsg.value = ''
    } catch (e: any) {
        console.error(e)
        if (e.response && e.response.data && e.response.data.message) {
            errorMsg.value = e.response.data.message
        } else {
            errorMsg.value = '아이디 찾기 중 오류가 발생했습니다.'
        }
        foundId.value = ''
    }
}
</script>

<template>
  <div class="min-h-screen flex items-start justify-center bg-gray-50 pt-32 relative">
    <div class="max-w-md w-full space-y-8 p-8 bg-white shadow rounded z-10">
      <div>
        <h2 class="text-center text-3xl font-extrabold text-gray-900">아이디 찾기</h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          가입 시 등록한 휴대폰 번호로 인증하여<br>아이디를 찾을 수 있습니다.
        </p>
      </div>

      <div v-if="foundId" class="mt-8 bg-indigo-50 p-6 rounded-lg text-center">
          <p class="text-gray-600 mb-2">회원님의 아이디는 다음과 같습니다.</p>
          <p class="text-2xl font-bold text-indigo-700 mb-6">{{ foundId }}</p>
          <router-link to="/login" class="inline-flex justify-center w-full py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
              로그인하러 가기
          </router-link>
      </div>

      <form v-else class="mt-8 space-y-6" @submit.prevent="handleFindId">
        
        <div v-if="errorMsg" class="rounded-md bg-red-50 p-4">
           <div class="text-sm text-red-700">{{ errorMsg }}</div>
        </div>

        <div class="space-y-4">
            <!-- 휴대폰 번호 -->
            <div>
              <label class="block text-sm font-medium text-gray-700">휴대폰 번호</label>
              <div class="flex mt-1 gap-2">
                <input v-model="phone" type="tel" :disabled="isVerified" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm disabled:bg-gray-100" placeholder="- 없이 숫자만 입력">
                <button type="button" @click="requestVerification" :disabled="isVerified" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none disabled:bg-gray-400 whitespace-nowrap flex-shrink-0">
                  {{ isVerificationSent ? '재전송' : '인증요청' }}
                </button>
              </div>
              <!-- 메시지 영역 -->
              <p v-if="phoneMsg.text && !isVerified" class="mt-1 text-xs" :class="phoneMsg.type === 'error' ? 'text-red-600' : 'text-blue-600'">
                  {{ phoneMsg.text }}
              </p>
              <p v-if="isVerified" class="mt-1 text-xs text-green-600">✓ 인증이 완료되었습니다.</p>
            </div>

            <!-- 인증 번호 확인 -->
            <div v-if="isVerificationSent && !isVerified && phoneMsg.type !== 'error'">
              <div class="flex mt-1 gap-2">
                <input v-model="verificationCode" type="text" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="인증번호 입력">
                <button type="button" @click="verifyCode" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none whitespace-nowrap flex-shrink-0">
                  확인
                </button>
              </div>
              <p v-if="verificationMsg.text" class="mt-1 text-xs text-red-600">{{ verificationMsg.text }}</p>
            </div>
        </div>

        <div>
          <button 
            type="submit" 
            :disabled="!isVerified" 
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            아이디 찾기
          </button>
        </div>

        <div class="text-center mt-4">
             <router-link to="/login" class="text-sm text-gray-500 hover:text-gray-900">로그인으로 돌아가기</router-link>
             <span class="mx-2 text-gray-300">|</span>
             <router-link to="/find-password" class="text-sm text-gray-500 hover:text-gray-900">비밀번호 찾기</router-link>
        </div>
      </form>
    </div>
  </div>
</template>
