import { formatImageUrl, formatImageUrls } from '@/utils/imageUtils'

export const propertyTypeOptions = ['住宅', '公寓', '别墅', '商铺', '写字楼']
export const decorationOptions = ['毛坯', '简装', '精装', '豪华']
export const orientationOptions = ['东', '南', '西', '北', '南北', '东西', '东南', '西南', '东北', '西北']
export const booleanOptions = [
  { label: '有', value: 1 },
  { label: '无', value: 0 }
]
export const facilityOptions = [
  '近地铁',
  '学区房',
  '近商圈',
  '满五唯一',
  '南北通透',
  '带车位',
  '电梯房',
  '景观阳台',
  '中央空调',
  '小区绿化好'
]

export const createDefaultHouseForm = () => ({
  title: '',
  propertyType: '住宅',
  province: '',
  city: '',
  district: '',
  address: '',
  community: '',
  price: null,
  area: null,
  unitPrice: null, // 新增：单价字段
  houseType: '',
  layout: '',
  floor: '',
  totalFloor: null,
  buildingYear: null,
  orientation: '',
  decoration: '',
  propertyFee: null,
  elevator: null,
  heating: null,
  description: '',
  videoUrl: '',
  facilitiesList: [],
  facilitiesText: '', // 新增：配套设施文本输入
  tagsText: '',
  imageUrls: []
})

const parseJsonArray = value => {
  if (!value) {
    return []
  }

  if (Array.isArray(value)) {
    return value.filter(Boolean)
  }

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : []
    } catch {
      return value
        .split(/[，,]/)
        .map(item => item.trim())
        .filter(Boolean)
    }
  }

  return []
}

export const hydrateHouseForm = (form, house) => {
  const defaults = createDefaultHouseForm()
  const facilitiesArray = parseJsonArray(house.facilities)
  Object.assign(form, defaults, {
    id: house.id,
    title: house.title || '',
    propertyType: house.propertyType || '住宅',
    province: house.province || '',
    city: house.city || '',
    district: house.district || '',
    address: house.address || '',
    community: house.community || '',
    price: house.price ?? null,
    area: house.area ?? null,
    unitPrice: house.unitPrice ?? null, // 加载单价数据
    houseType: house.houseType || '',
    layout: house.layout || '',
    floor: house.floor || '',
    totalFloor: house.totalFloor ?? null,
    buildingYear: house.buildingYear ?? null,
    orientation: house.orientation || '',
    decoration: house.decoration || '',
    propertyFee: house.propertyFee ?? null,
    elevator: house.elevator ?? null,
    heating: house.heating ?? null,
    description: house.description || '',
    videoUrl: house.videoUrl || '',
    facilitiesList: facilitiesArray,
    facilitiesText: facilitiesArray.join(','),
    tagsText: house.tags || '',
    imageUrls: formatImageUrls(house.images)
  })
}

export const normalizeHousePayload = form => {
  const normalizedImageUrls = Array.from(
    new Set(
      (form.imageUrls || [])
        .map(url => String(url || '').trim())
        .filter(Boolean)
        .map(url => formatImageUrl(url))
    )
  )

  // 从facilitiesText文本解析配套设施数组
  const facilities = Array.from(
    new Set(
      String(form.facilitiesText || '')
        .split(/[，,]/)
        .map(item => item.trim())
        .filter(Boolean)
    )
  )

  const tags = Array.from(
    new Set(
      String(form.tagsText || '')
        .split(/[，,]/)
        .map(item => item.trim())
        .filter(Boolean)
    )
  ).join(',')

  return {
    id: form.id,
    title: form.title?.trim(),
    propertyType: form.propertyType || null,
    province: form.province?.trim(),
    city: form.city?.trim(),
    district: form.district?.trim(),
    address: form.address?.trim(),
    community: form.community?.trim(),
    price: form.price,
    area: form.area,
    unitPrice: form.unitPrice, // 添加单价字段
    houseType: form.houseType?.trim(),
    layout: form.layout?.trim(),
    floor: form.floor?.trim(),
    totalFloor: form.totalFloor,
    buildingYear: form.buildingYear,
    orientation: form.orientation || null,
    decoration: form.decoration || null,
    propertyFee: form.propertyFee,
    elevator: form.elevator,
    heating: form.heating,
    description: form.description?.trim(),
    videoUrl: form.videoUrl?.trim(),
    facilities: JSON.stringify(facilities),
    tags,
    images: JSON.stringify(normalizedImageUrls)
  }
}

export const createUploadFileList = imageUrls =>
  (imageUrls || []).map((url, index) => ({
    uid: `${Date.now()}-${index}-${url}`,
    name: `house-image-${index + 1}`,
    status: 'success',
    url
  }))
