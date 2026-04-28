/**
 * 图片 URL 处理工具函数
 * 用于统一处理后端返回的图片路径
 */

/**
 * 将后端返回的图片 URL 转换为前端可访问的静态资源 URL
 * @param {string} url - 后端返回的图片 URL（可能是 /uploads/xxx 或 /api/file/uploads/xxx）
 * @returns {string} - 转换后的相对路径 URL
 */
export const formatImageUrl = (url) => {
  if (!url) return '';
  
  // 如果已经是完整的 http/https URL（外部链接），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 如果包含 /api/ 前缀，移除它（保留后续路径）
  if (url.includes('/api/')) {
    url = url.replace('/api', '');
  }
  
  // 如果以 /uploads/ 开头，保持不变（相对路径）
  if (url.startsWith('/uploads/')) {
    return url;
  }
  
  // 如果是相对路径，添加 /uploads/ 前缀
  if (url.startsWith('uploads/')) {
    return '/' + url;
  }
  
  // 其他情况，当作文件名处理，添加 /uploads/ 前缀
  return '/uploads/' + url;
};

/**
 * 批量处理图片 URL 数组
 * @param {string|string[]} urls - 图片 URL 字符串或数组（JSON 字符串或数组）
 * @returns {string[]} - 转换后的 URL 数组
 */
export const formatImageUrls = (urls) => {
  if (!urls) return [];
  
  try {
    // 如果是 JSON 字符串，先解析
    const urlArray = typeof urls === 'string' ? JSON.parse(urls) : urls;
    
    if (!Array.isArray(urlArray)) {
      return [formatImageUrl(urlArray)];
    }
    
    return urlArray.map(url => formatImageUrl(url));
  } catch (error) {
    console.error('解析图片 URLs 失败:', error);
    return [];
  }
};

export default {
  formatImageUrl,
  formatImageUrls
};