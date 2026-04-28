import { createStore } from 'vuex'

export default createStore({
  state: {
    token: sessionStorage.getItem('token') || '',
    role: sessionStorage.getItem('role') || '',
    userInfo: JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      sessionStorage.setItem('token', token)
    },
    SET_ROLE(state, role) {
      state.role = role
      sessionStorage.setItem('role', role)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    CLEAR_AUTH(state) {
      state.token = ''
      state.role = ''
      state.userInfo = {}
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('role')
      sessionStorage.removeItem('userInfo')
    }
  },
  actions: {
    setAuth({ commit }, { token, role, userInfo }) {
      commit('SET_TOKEN', token)
      commit('SET_ROLE', role)
      commit('SET_USER_INFO', userInfo)
    },
    clearAuth({ commit }) {
      commit('CLEAR_AUTH')
    }
  },
  getters: {
    token: state => state.token,
    role: state => state.role,
    userInfo: state => state.userInfo,
    isLoggedIn: state => !!state.token
  }
})