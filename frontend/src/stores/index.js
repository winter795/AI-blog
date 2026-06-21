import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  function setToken(t) { token.value = t; localStorage.setItem('token', t) }
  function setUserInfo(info) { userInfo.value = info; localStorage.setItem('userInfo', JSON.stringify(info)) }
  function logout() { token.value = ''; userInfo.value = null; localStorage.removeItem('token'); localStorage.removeItem('userInfo') }
  return { token, userInfo, setToken, setUserInfo, logout }
})

export const useTheme = () => {
  const dark = ref(localStorage.getItem('dark') === '1')
  watch(dark, v => {
    localStorage.setItem('dark', v ? '1' : '0')
    document.documentElement.classList.toggle('dark', v)
  }, { immediate: true })
  return { dark }
}
