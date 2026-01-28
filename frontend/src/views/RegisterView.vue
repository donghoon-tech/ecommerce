<script setup lang="ts">
import { ref, watch } from 'vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080' // Dev environment

// 상태 관리
const form = ref({
  id: '', // username
  password: '',
  passwordConfirm: '',
  email: '',
  businessNumber: '',
  phoneNumber: '',
  verificationCode: '',
  businessAddress: '',
  yardAddress: '',
  companyName: '' // 회사명 추가
})

const fileInput = ref<HTMLInputElement | null>(null)
const isPhoneVerified = ref(false)
const isVerificationSent = ref(false)
const isSameAddress = ref(false)
const loading = ref(false)
const errorMsg = ref('')

// 화면 표시용 상태 메시지
const phoneMsg = ref({ type: '', text: '' }) // type: 'error' | 'success' | 'info'
const verificationMsg = ref({ type: '', text: '' })

// 유효성 검사 규칙
const validation = ref({
  id: {
    valid: true,
    msg: ''
  },
  password: {
    valid: true,
    msg: ''
  }
})

// 아이디 유효성 검사 (4~20자, 영문 소문자, 숫자, _, -)
const validateId = () => {
  const idRegex = /^[a-z0-9_-]{4,20}$/
  if (!form.value.id) {
    validation.value.id = { valid: false, msg: '아이디를 입력해주세요.' }
  } else if (!idRegex.test(form.value.id)) {
    validation.value.id = { valid: false, msg: '4~20자의 영문 소문자, 숫자, 특수문자(_, -)만 사용 가능합니다.' }
  } else {
    validation.value.id = { valid: true, msg: '' }
  }
}

// 비밀번호 유효성 검사 (8~20자, 영문+숫자 조합)
const validatePassword = () => {
  const pwdRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/
  if (!form.value.password) {
    validation.value.password = { valid: false, msg: '비밀번호를 입력해주세요.' }
  } else if (!pwdRegex.test(form.value.password)) {
    validation.value.password = { valid: false, msg: '8~20자의 영문과 숫자를 조합하여 입력해주세요.' }
  } else {
    validation.value.password = { valid: true, msg: '' }
  }
}

// 비밀번호 일치 확인
const isPasswordMismatch = ref(false)
watch(() => [form.value.password, form.value.passwordConfirm], ([pwd, confirm]) => {
  isPasswordMismatch.value = pwd !== confirm && confirm !== ''
})

// 실시간 유효성 검사 트리거
watch(() => form.value.id, validateId)
watch(() => form.value.password, validatePassword)

// 주소 동기화 로직
watch(isSameAddress, (newVal) => {
  if (newVal) {
    form.value.yardAddress = form.value.businessAddress
  } else {
    form.value.yardAddress = ''
  }
})

// 사업장 주소가 바뀌는데 체크되어 있다면 야적장 주소도 같이 업데이트
watch(() => form.value.businessAddress, (newVal) => {
  if (isSameAddress.value) {
    form.value.yardAddress = newVal
  }
})

// 더미 휴대폰 인증 로직
const requestVerification = () => {
  if (!form.value.phoneNumber) {
    phoneMsg.value = { type: 'error', text: '휴대폰 번호를 입력해주세요.' }
    return
  }
  // 성공 시 메시지
  isVerificationSent.value = true
  phoneMsg.value = { type: 'info', text: '인증번호가 발송되었습니다. (테스트용: 아무 번호나 입력하세요)' }
  verificationMsg.value = { type: '', text: '' } // 인증번호 입력창 메시지 초기화
}

const verifyCode = () => {
  if (!form.value.verificationCode) {
    verificationMsg.value = { type: 'error', text: '인증번호를 입력해주세요.' }
    return
  }
  isPhoneVerified.value = true
  phoneMsg.value = { type: 'success', text: '인증이 완료되었습니다.' }
  verificationMsg.value = { type: '', text: '' }
}

const handleRegister = async () => {
  // 실시간 검증 실행
  validateId()
  validatePassword()

  // 유효성 검사
  if (!validation.value.id.valid || !validation.value.password.valid) {
    errorMsg.value = '입력 정보를 확인해주세요.'
    return
  }
  if (isPasswordMismatch.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  if (!isPhoneVerified.value) {
    errorMsg.value = '휴대폰 인증을 완료해주세요.'
    return
  }
  
  loading.value = true
  errorMsg.value = ''

  try {
      // API 호출 (Spring Boot)
      await axios.post(`${API_BASE_URL}/api/auth/register`, {
        username: form.value.id,
        password: form.value.password,
        phone: form.value.phoneNumber,
        companyName: form.value.companyName,
        email: form.value.email || null,
        businessNumber: form.value.businessNumber || null,
        businessAddress: form.value.businessAddress || null,
        yardAddress: form.value.yardAddress || null
      })

      // 성공 화면 전환을 위해 errorMsg를 활용하거나 별도 상태값 사용
      // 여기서는 간단히 alert 대신 화면 내용을 교체하는 방식을 사용
      isRegisterSuccess.value = true

  } catch (e: any) {
    console.error(e)
    if (e.response && e.response.data && e.response.data.message) {
        errorMsg.value = e.response.data.message
    } else {
        errorMsg.value = '회원가입 처리 중 오류가 발생했습니다.'
    }
  } finally {
    loading.value = false
  }
}

const isRegisterSuccess = ref(false)
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-xl w-full space-y-8 bg-white p-8 shadow rounded-lg">
      
      <!-- 회원가입 성공 화면 -->
      <div v-if="isRegisterSuccess" class="text-center py-10">
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
          <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-900 mb-2">회원가입 완료!</h2>
        <p class="text-gray-600 mb-8">성공적으로 회원가입이 되었습니다.<br>로그인 후 서비스를 이용해주세요.</p>
        <router-link to="/login" class="inline-flex justify-center w-full py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          로그인하러 가기
        </router-link>
      </div>

      <!-- 회원가입 폼 -->
      <div v-else>
        <div>
          <h2 class="text-center text-3xl font-extrabold text-gray-900">회원가입</h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            이미 계정이 있으신가요?
            <router-link to="/login" class="font-medium text-indigo-600 hover:text-indigo-500">
              로그인하기
            </router-link>
          </p>
        </div>

        <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
          
          <!-- 에러 메시지 -->
          <div v-if="errorMsg" class="rounded-md bg-red-50 p-4">
            <div class="text-sm text-red-700">{{ errorMsg }}</div>
          </div>

          <div class="space-y-4">
            
            <!-- 기본 정보 섹션 -->
            <div class="bg-gray-50 p-4 rounded-md space-y-3">
              <h3 class="text-lg font-medium text-gray-900">기본 정보</h3>
              
              <!-- 아이디 -->
              <div>
                <label for="id" class="block text-sm font-medium text-gray-700">아이디 <span class="text-red-500">*</span></label>
                <input 
                  id="id" 
                  v-model="form.id" 
                  @blur="validateId"
                  type="text" 
                  required 
                  class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  :class="{'border-red-500': !validation.id.valid}"
                  placeholder="4~20자 영문 소문자, 숫자, _, -"
                >
                <p v-if="!validation.id.valid" class="mt-1 text-xs text-red-600">{{ validation.id.msg }}</p>
              </div>

              <!-- 비밀번호 -->
              <div>
                <label for="password" class="block text-sm font-medium text-gray-700">비밀번호 <span class="text-red-500">*</span></label>
                <input 
                  id="password" 
                  v-model="form.password" 
                  @blur="validatePassword"
                  type="password" 
                  required 
                  class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  :class="{'border-red-500': !validation.password.valid}"
                  placeholder="8~20자 영문+숫자 조합"
                >
                <p v-if="!validation.password.valid" class="mt-1 text-xs text-red-600">{{ validation.password.msg }}</p>
              </div>

              <!-- 비밀번호 확인 -->
              <div>
                <label for="passwordConfirm" class="block text-sm font-medium text-gray-700">비밀번호 재확인 <span class="text-red-500">*</span></label>
                <input id="passwordConfirm" v-model="form.passwordConfirm" type="password" required class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="비밀번호를 다시 입력하세요">
                <p v-if="isPasswordMismatch" class="mt-1 text-sm text-red-600">비밀번호가 일치하지 않습니다.</p>
              </div>

              <!-- 회사명 (추가) -->
              <div>
                <label for="companyName" class="block text-sm font-medium text-gray-700">회사명</label>
                <input id="companyName" v-model="form.companyName" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="회사명 입력">
              </div>
            </div>

            <!-- 연락처 정보 섹션 -->
            <div class="bg-gray-50 p-4 rounded-md space-y-3">
              <h3 class="text-lg font-medium text-gray-900">연락처 정보</h3>
              
              <!-- 대표 번호 -->
              <div>
                <label class="block text-sm font-medium text-gray-700">대표 번호 (본인 인증) <span class="text-red-500">*</span></label>
                <div class="flex mt-1 gap-2">
                  <input v-model="form.phoneNumber" type="tel" :disabled="isPhoneVerified" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm disabled:bg-gray-100" placeholder="- 없이 숫자만 입력">
                  <button type="button" @click="requestVerification" :disabled="isPhoneVerified" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none disabled:bg-gray-400 whitespace-nowrap flex-shrink-0">
                    {{ isVerificationSent ? '재전송' : '인증요청' }}
                  </button>
                </div>
                <!-- 인증 메시지 -->
                <p v-if="phoneMsg.text && !isPhoneVerified" class="mt-1 text-xs" :class="phoneMsg.type === 'error' ? 'text-red-600' : 'text-blue-600'">
                    {{ phoneMsg.text }}
                </p>
                 <p v-if="isPhoneVerified" class="mt-1 text-xs text-green-600">✓ 인증이 완료되었습니다.</p>
              </div>

              <!-- 인증 번호 확인 -->
              <div v-if="!isPhoneVerified && phoneMsg.type === 'info'">
                <div class="flex mt-1 gap-2">
                  <input v-model="form.verificationCode" type="text" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="인증번호 입력">
                  <button type="button" @click="verifyCode" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none whitespace-nowrap flex-shrink-0">
                    확인
                  </button>
                </div>
                <p v-if="verificationMsg.text" class="mt-1 text-xs text-red-600">{{ verificationMsg.text }}</p>
              </div>

              <!-- 이메일 (선택) -->
              <div>
                <label for="email" class="block text-sm font-medium text-gray-700">이메일 (선택)</label>
                <input id="email" v-model="form.email" type="email" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="example@domain.com">
              </div>
            </div>

            <!-- 사업자 정보 섹션 -->
            <div class="bg-gray-50 p-4 rounded-md space-y-3">
              <h3 class="text-lg font-medium text-gray-900">사업자 정보 (선택)</h3>

              <!-- 사업자 번호 -->
              <div>
                <label for="businessNumber" class="block text-sm font-medium text-gray-700">사업자 등록 번호</label>
                <input id="businessNumber" v-model="form.businessNumber" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="000-00-00000">
              </div>

              <!-- 사업자 등록증 파일 -->
              <div>
                <label class="block text-sm font-medium text-gray-700">사업자 등록증 사본</label>
                <p class="text-xs text-gray-500 mb-1">가입 후 마이페이지에서 등록 가능합니다.</p>
                <input ref="fileInput" type="file" disabled class="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-gray-100 file:text-gray-400 cursor-not-allowed">
              </div>
            </div>

            <!-- 주소 정보 섹션 -->
            <div class="bg-gray-50 p-4 rounded-md space-y-3">
              <h3 class="text-lg font-medium text-gray-900">주소지 정보</h3>

              <!-- 사업장 주소 -->
              <div>
                <label for="businessAddress" class="block text-sm font-medium text-gray-700">사업장 주소 (선택)</label>
                <input id="businessAddress" v-model="form.businessAddress" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="서울시 강남구...">
              </div>

              <!-- 야적장 주소 -->
              <div>
                <div class="flex items-center justify-between">
                  <label for="yardAddress" class="block text-sm font-medium text-gray-700">야적장 주소 (선택)</label>
                  <div class="flex items-center">
                    <input id="sameAddress" type="checkbox" v-model="isSameAddress" class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded">
                    <label for="sameAddress" class="ml-2 block text-sm text-gray-900">사업장 주소와 동일</label>
                  </div>
                </div>
                <input id="yardAddress" v-model="form.yardAddress" :disabled="isSameAddress" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm disabled:bg-gray-200" placeholder="물건을 상/하차할 주소">
              </div>
            </div>

          </div>

          <div>
            <button 
              type="submit" 
              :disabled="loading || !isPhoneVerified || !validation.id.valid || !validation.password.valid || isPasswordMismatch || !form.id || !form.password" 
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading">처리 중...</span>
              <span v-else>회원가입 완료</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
