export function formatDateTime(value, { showTime = true } = {}) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const opts = showTime
    ? { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }
    : { year: 'numeric', month: '2-digit', day: '2-digit' }
  return date.toLocaleString('zh-CN', opts)
}

export function formatTime(value) { return formatDateTime(value, { showTime: true }) }
export function formatDate(value) { return formatDateTime(value, { showTime: false }) }

export function formatNumber(value, fallback = '--') {
  if (value === null || value === undefined || value === '') return fallback
  const num = Number(value)
  return Number.isNaN(num) ? fallback : num.toLocaleString()
}

export function formatPrice(value) { return formatNumber(value, '--') }
