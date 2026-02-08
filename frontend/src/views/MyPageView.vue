<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const API_BASE_URL = 'http://localhost:8080' // Dev environment
const router = useRouter()

// 상태 관리
const form = ref({
  id: '', // username (read-only)
  password: '', // new password
  passwordConfirm: '',
  email: '',
  businessNumber: '',
  phoneNumber: '',
  verificationCode: '',
  businessAddress: '',
  yardAddress: '',
  companyName: ''
})

const isPhoneVerified = ref(true) // 기존 회원은 이미 인증되었다고 가정 (번호 변경 시 false로 전환 로직 필요)
const isSameAddress = ref(false)
const isChangingPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 초기 데이터 로딩 (가정)
// 초기 데이터 로딩
onMounted(async () => {
    try {
        const token = localStorage.getItem('token')
        if (!token) {
            alert('로그인이 필요합니다.')
            router.push('/login')
            return
        }
        
        const res = await axios.get(`${API_BASE_URL}/api/users/me`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        const data = res.data
        
        form.value.id = data.username
        form.value.email = data.email || ''
        form.value.phoneNumber = data.phone || ''
        form.value.companyName = data.companyName || ''
        form.value.businessNumber = data.businessNumber || ''
        form.value.businessAddress = data.businessAddress || ''
        form.value.yardAddress = data.yardAddress || ''
        
        if (data.businessAddress && data.businessAddress === data.yardAddress) {
            isSameAddress.value = true
        }

    } catch (e) {
        console.error(e)
        // 토큰이 만료되었거나 오류 발생 시
        if (axios.isAxiosError(e) && e.response?.status === 403) {
             alert('로그인 세션이 만료되었습니다.')
             localStorage.removeItem('token')
             router.push('/login')
        }
    }
})

// 유효성 검사 규칙
const validation = ref({
  password: {
    valid: true,
    msg: ''
  }
})

// 비밀번호 유효성 검사 (입력시에만 검사)
const validatePassword = () => {
  if (!form.value.password) {
      // 비밀번호 변경을 원하지 않는 경우
      validation.value.password = { valid: true, msg: '' }
      return
  }

  const pwdRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,20}$/
  if (!pwdRegex.test(form.value.password)) {
    validation.value.password = { valid: false, msg: '영문과 숫자를 포함한 8~20자를 입력해주세요.' }
  } else {
    validation.value.password = { valid: true, msg: '' }
  }
}

// 비밀번호 일치 확인
const isPasswordMismatch = ref(false)
watch(() => [form.value.password, form.value.passwordConfirm], ([pwd, confirm]) => {
    if (!pwd || !confirm) {
        isPasswordMismatch.value = false
        return
    }
    isPasswordMismatch.value = pwd !== confirm
})

watch(() => form.value.password, validatePassword)

// 주소 동기화
watch(isSameAddress, (newVal) => {
  if (newVal) {
    form.value.yardAddress = form.value.businessAddress
  }
})

watch(() => form.value.businessAddress, (newVal) => {
  if (isSameAddress.value) {
    form.value.yardAddress = newVal
  }
})

// 전화번호 변경 감지
watch(() => form.value.phoneNumber, () => {
    // 초기 로딩 시점에는 무시하거나, 실제 변경 시 인증 상태 초기화 로직
    // 여기서는 간단히 처리
})

const requestVerification = () => {
  if (!form.value.phoneNumber) {
    alert('휴대폰 번호를 입력해주세요.')
    return
  }
  isPhoneVerified.value = false
  alert(`인증번호가 발송되었습니다. (테스트용: 아무 번호나 입력)`)
}

const verifyCode = () => {
  if (!form.value.verificationCode) {
    alert('인증번호를 입력해주세요.')
    return
  }
  isPhoneVerified.value = true
  alert('인증되었습니다.')
}

const togglePasswordChange = (show: boolean) => {
  isChangingPassword.value = show
  if (!show) {
    // 취소 시 초기화
    form.value.password = ''
    form.value.passwordConfirm = ''
    validation.value.password = { valid: true, msg: '' }
    isPasswordMismatch.value = false
  }
}

const handleUpdate = async () => {
  validatePassword()

  if (!validation.value.password.valid) {
    errorMsg.value = '비밀번호 형식을 확인해주세요.'
    return
  }
  if (isPasswordMismatch.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  
  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
      const token = localStorage.getItem('token')
      // API 호출 (Spring Boot) - PUT
      await axios.put(`${API_BASE_URL}/api/users/me`, {
        password: form.value.password || undefined, // 변경 시에만 전송
        phone: form.value.phoneNumber,
        companyName: form.value.companyName,
        email: form.value.email,
        businessNumber: form.value.businessNumber,
        businessAddress: form.value.businessAddress,
        yardAddress: form.value.yardAddress
      }, {
        headers: { Authorization: `Bearer ${token}` }
      })

      successMsg.value = '정보가 성공적으로 수정되었습니다.'
      
      // 스크롤 최상단으로 이동
      window.scrollTo({ top: 0, behavior: 'smooth' })
      
      // 비밀번호가 변경되었어도 로그아웃 하지 않음
      if (isChangingPassword.value && form.value.password) {
         // alert('비밀번호가 변경되었습니다.') // 선택적: 명시적 알림이 필요하면 추가
      }
      
      togglePasswordChange(false)
      
      // 에러 메시지 초기화
      errorMsg.value = ''

  } catch (e: any) {
    console.error(e)
    if (e.response && e.response.data && e.response.data.message) {
        errorMsg.value = e.response.data.message
    } else {
        errorMsg.value = '정보 수정 중 오류가 발생했습니다.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-xl w-full space-y-8 bg-white p-8 shadow rounded-lg">
      <div>
        <h2 class="text-center text-3xl font-extrabold text-gray-900">내 정보</h2>
      </div>

      <form class="mt-8 space-y-6" @submit.prevent="handleUpdate">
        
        <!-- 메시지 표시 -->
        <div v-if="errorMsg" class="rounded-md bg-red-50 p-4">
          <div class="text-sm text-red-700">{{ errorMsg }}</div>
        </div>
        <div v-if="successMsg" class="rounded-md bg-green-50 p-4">
          <div class="text-sm text-green-700">{{ successMsg }}</div>
        </div>

        <div class="space-y-4">
          
          <!-- 기본 정보 섹션 -->
          <div class="bg-gray-50 p-4 rounded-md space-y-3">
            <h3 class="text-lg font-medium text-gray-900">계정 정보</h3>
            
            <!-- 아이디 (수정 불가) -->
            <div>
              <label for="id" class="block text-sm font-medium text-gray-700">아이디</label>
              <input 
                id="id" 
                v-model="form.id" 
                type="text" 
                readonly 
                class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 bg-gray-100 text-gray-500 sm:text-sm cursor-not-allowed" 
              >
            </div>

            <!-- 회사명 -->
            <div>
              <label for="companyName" class="block text-sm font-medium text-gray-700">회사명</label>
              <input id="companyName" v-model="form.companyName" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="회사명 입력">
            </div>
          </div>
        
          <div class="bg-gray-50 p-4 rounded-md space-y-3">
             <h3 class="text-lg font-medium text-gray-900">비밀번호 변경</h3>
             
             <div v-if="!isChangingPassword">
               <button type="button" @click="togglePasswordChange(true)" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none whitespace-nowrap">
                 변경
               </button>
             </div>

             <div v-else class="space-y-3">
              <!-- 비밀번호 -->
              <div>
                <label for="password" class="block text-sm font-medium text-gray-700">새 비밀번호</label>
                <input 
                  id="password" 
                  v-model="form.password" 
                  @blur="validatePassword"
                  type="password" 
                  class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  :class="{'border-red-500': !validation.password.valid}"
                  placeholder="영문+숫자 조합 8~20자"
                >
                <p v-if="!validation.password.valid" class="mt-1 text-sm text-red-600">{{ validation.password.msg }}</p>
              </div>

              <!-- 비밀번호 확인 -->
              <div>
                <label for="passwordConfirm" class="block text-sm font-medium text-gray-700">새 비밀번호 확인</label>
                <input 
                  id="passwordConfirm" 
                  v-model="form.passwordConfirm" 
                  type="password" 
                  class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  placeholder="비밀번호 재확인"
                >
                <p v-if="isPasswordMismatch" class="mt-1 text-sm text-red-600">비밀번호가 일치하지 않습니다.</p>
              </div>

              <div class="flex justify-end">
                <button type="button" @click="togglePasswordChange(false)" class="text-xs text-gray-500 hover:text-gray-700 underline">
                  비밀번호 변경 취소
                </button>
              </div>
             </div>
          </div>

          <!-- 연락처 정보 섹션 -->
          <div class="bg-gray-50 p-4 rounded-md space-y-3">
            <h3 class="text-lg font-medium text-gray-900">연락처 정보</h3>
            
            <!-- 대표 번호 -->
            <div>
              <label class="block text-sm font-medium text-gray-700">대표 번호</label>
              <div class="flex mt-1 gap-2">
                <input v-model="form.phoneNumber" type="tel" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="- 없이 숫자만 입력">
                <button type="button" @click="requestVerification" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none whitespace-nowrap flex-shrink-0">
                  재인증
                </button>
              </div>
            </div>

            <!-- 인증 번호 확인 -->
             <div v-if="!isPhoneVerified">
              <div class="flex mt-1 gap-2">
                <input v-model="form.verificationCode" type="text" class="block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="인증번호 입력">
                <button type="button" @click="verifyCode" class="inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none whitespace-nowrap flex-shrink-0">
                  확인
                </button>
              </div>
            </div>

            <!-- 이메일 (선택) -->
            <div>
              <label for="email" class="block text-sm font-medium text-gray-700">이메일</label>
              <input id="email" v-model="form.email" type="email" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="example@domain.com">
            </div>
          </div>

          <!-- 사업자 정보 섹션 -->
          <div class="bg-gray-50 p-4 rounded-md space-y-3">
            <h3 class="text-lg font-medium text-gray-900">사업자 정보</h3>
            <!-- 사업자 번호 -->
            <div>
              <label for="businessNumber" class="block text-sm font-medium text-gray-700">사업자 등록 번호</label>
              <input id="businessNumber" v-model="form.businessNumber" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="000-00-00000">
            </div>
          </div>

          <!-- 주소 정보 섹션 -->
          <div class="bg-gray-50 p-4 rounded-md space-y-3">
            <h3 class="text-lg font-medium text-gray-900">주소지 정보</h3>

            <!-- 사업장 주소 -->
            <div>
              <label for="businessAddress" class="block text-sm font-medium text-gray-700">사업장 주소</label>
              <input id="businessAddress" v-model="form.businessAddress" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="서울시 강남구...">
            </div>

            <!-- 야적장 주소 -->
            <div>
              <div class="flex items-center justify-between">
                <label for="yardAddress" class="block text-sm font-medium text-gray-700">야적장 주소</label>
                <div class="flex items-center">
                  <input id="sameAddress" type="checkbox" v-model="isSameAddress" class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded">
                  <label for="sameAddress" class="ml-2 block text-sm text-gray-900">사업장 주소와 동일</label>
                </div>
              </div>
              <input id="yardAddress" v-model="form.yardAddress" :disabled="isSameAddress" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm disabled:bg-gray-200" placeholder="물건을 상/하차할 주소">
            </div>
          </div>

        </div>

        <div class="flex gap-4">
          <button 
            type="button" 
            @click="$router.push('/')"
            class="w-full flex justify-center py-2 px-4 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none"
          >
            취소
          </button>
          <button 
            type="submit" 
            :disabled="loading || (!!form.password && (!validation.password.valid || form.password !== form.passwordConfirm))"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading">저장 중...</span>
            <span v-else>변경사항 저장</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
