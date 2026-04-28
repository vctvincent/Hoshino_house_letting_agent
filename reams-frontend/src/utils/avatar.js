const DEFAULT_AVATAR =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

export function getDefaultAvatar() {
  return DEFAULT_AVATAR
}

export function resolveAvatarUrl(url) {
  if (!url) return DEFAULT_AVATAR
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/api/uploads/')) return url.replace('/api', '')
  if (url.startsWith('/uploads/')) return url
  if (url.startsWith('uploads/')) return '/' + url
  if (url.startsWith('/')) return url
  return '/uploads/' + url
}

export function getAvatarText(name) {
  const value = (name || '').trim()
  return value ? value.charAt(0) : 'U'
}
