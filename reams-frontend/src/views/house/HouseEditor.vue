<template>
  <div class="agent-dashboard-page">
    <!-- 顶部 Hero 区域 -->
    <section class="manage-hero">
      <div class="manage-hero__content">
        <p class="eyebrow">Agent Listing Studio</p>
        <h1>{{ pageTitle }}</h1>
        <p class="manage-hero__text">{{ heroText }}</p>
      </div>
      
      <!-- 顶部操作按钮 -->
      <div class="manage-hero__actions" v-if="!isReadOnly">
        <el-button @click="handleCancel">{{ backButtonText }}</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ submitButtonText }}</el-button>
      </div>
    </section>

    <!-- 快捷操作卡片 - 仅新增模式显示 -->
    <section v-if="!isReadOnly && mode === 'create'" class="quick-action-section">
      <div class="quick-action-card" @click="router.push('/agent-layout/house/editor')">
        <div class="quick-action-icon">
          <el-icon><Plus /></el-icon>
        </div>
        <div class="quick-action-content">
          <h3>新增房源</h3>
          <p>发布新的房源信息</p>
        </div>
      </div>
    </section>

    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :disabled="isReadOnly"
          label-position="top"
          class="editor-form"
        >
          <div class="form-grid">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="section-head">
                  <div>
                    <h2>基础信息</h2>
                    <p>完善核心展示字段，让列表卡片和详情页都更完整。</p>
                  </div>
                </div>
              </template>

              <div class="field-grid two-col">
                <el-form-item label="房源标题" prop="title">
                  <el-input v-model="form.title" placeholder="例如：望京地铁口精装两居" />
                </el-form-item>

                <el-form-item label="房屋类型">
                  <el-select v-model="form.propertyType" placeholder="请选择">
                    <el-option
                      v-for="item in propertyTypeOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </div>

              <div class="field-grid three-col">
                <el-form-item label="省份">
                  <el-input v-model="form.province" placeholder="例如：广东省" />
                </el-form-item>
                <el-form-item label="城市">
                  <el-input v-model="form.city" placeholder="例如：深圳市" />
                </el-form-item>
                <el-form-item label="区县">
                  <el-input v-model="form.district" placeholder="例如：南山区" />
                </el-form-item>
              </div>

              <div class="field-grid two-col">
                <el-form-item label="详细地址" prop="address">
                  <el-input v-model="form.address" placeholder="请输入楼栋门牌等详细地址" />
                </el-form-item>
                <el-form-item label="小区名称">
                  <el-input v-model="form.community" placeholder="例如：万象天成" />
                </el-form-item>
              </div>

              <div class="field-grid three-col">
                <el-form-item label="总价（万元）" prop="price">
                  <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" />
                </el-form-item>
                <el-form-item label="面积（㎡）" prop="area">
                  <el-input-number v-model="form.area" :min="0" :precision="2" controls-position="right" />
                </el-form-item>
                <el-form-item label="单价（元/㎡）" prop="unitPrice">
                  <el-input-number
                    v-model="form.unitPrice"
                    :min="0"
                    :precision="0"
                    controls-position="right"
                    @change="isUnitPriceManuallyEdited = true"
                  />
                  <div class="input-hint">
                    <el-button
                      v-if="form.price && form.area"
                      type="primary"
                      link
                      size="small"
                      class="auto-calc-btn"
                      @click="autoCalculateUnitPrice"
                    >
                      重新计算
                    </el-button>
                  </div>
                </el-form-item>
              </div>

              <div class="field-grid two-col">
                <el-form-item label="户型" prop="houseType">
                  <el-input v-model="form.houseType" placeholder="例如：4室2厅" />
                </el-form-item>
                <el-form-item label="户型结构">
                  <el-input v-model="form.layout" placeholder="例如：平层 / 跃层" />
                </el-form-item>
              </div>

              <div class="image-section-inside">
                <h3 class="image-section-title">图片与展示素材</h3>
                <p class="image-section-desc">支持上传图片，也支持补充现有图片 URL。</p>

                <div v-if="!isReadOnly" class="image-toolbar">
                  <el-input
                    v-model="manualImageUrl"
                    placeholder="粘贴图片 URL 或 /uploads/... 本地路径"
                    clearable
                  >
                    <template #prepend>图片 URL</template>
                  </el-input>
                  <el-button type="primary" plain @click="addManualImage">加入图片列表</el-button>
                </div>

                <el-upload
                  v-model:file-list="fileList"
                  list-type="picture-card"
                  :disabled="isReadOnly"
                  :http-request="uploadImage"
                  :before-upload="beforeUpload"
                  :on-success="handleUploadSuccess"
                  :on-remove="handleRemove"
                  :limit="9"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>

                <div class="upload-hint">
                  <span>{{ imageHintText }}</span>
                  <span>当前已添加 {{ form.imageUrls.length }} 张图片。</span>
                </div>
              </div>
            </el-card>

            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="section-head">
                  <div>
                    <h2>居住属性</h2>
                    <p>这些信息会直接影响用户对性价比和居住体验的判断。</p>
                  </div>
                </div>
              </template>

              <div class="field-grid four-col">
                <el-form-item label="所在楼层">
                  <el-input v-model="form.floor" placeholder="例如：中楼层" />
                </el-form-item>
                <el-form-item label="总楼层">
                  <el-input-number v-model="form.totalFloor" :min="1" :max="200" controls-position="right" />
                </el-form-item>
                <el-form-item label="建筑年代">
                  <el-input-number v-model="form.buildingYear" :min="1950" :max="2100" controls-position="right" />
                </el-form-item>
                <el-form-item label="物业费（元/㎡）">
                  <el-input-number v-model="form.propertyFee" :min="0" :precision="2" controls-position="right" />
                </el-form-item>
              </div>

              <div class="field-grid four-col">
                <el-form-item label="朝向">
                  <el-select v-model="form.orientation" placeholder="请选择" clearable>
                    <el-option
                      v-for="item in orientationOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="装修情况">
                  <el-select v-model="form.decoration" placeholder="请选择" clearable>
                    <el-option
                      v-for="item in decorationOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="电梯">
                  <el-segmented v-model="form.elevator" :options="booleanOptions" block />
                </el-form-item>
                <el-form-item label="供暖">
                  <el-segmented v-model="form.heating" :options="booleanOptions" block />
                </el-form-item>
              </div>

              <div class="field-grid one-col">
                <el-form-item label="房源标签">
                  <el-input
                    v-model="form.tagsText"
                    placeholder="多个标签用逗号分隔，例如：学区房,地铁口,低总价"
                  />
                </el-form-item>
              </div>

              <div class="field-grid one-col">
                <el-form-item label="视频链接">
                  <el-input
                    v-model="form.videoUrl"
                    placeholder="可填写房源讲解视频或宣传视频 URL"
                  />
                </el-form-item>
              </div>

              <el-form-item label="配套设施">
                <el-input
                  v-model="form.facilitiesText"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入配套设施，用逗号分隔，例如：电梯,集中供暖,燃气,宽带,停车位"
                />
                <div class="input-hint single-line">直接输入配套设施名称，多个设施可用中英文逗号分隔。</div>
              </el-form-item>

              <el-form-item label="房源描述">
                <div class="description-wrapper">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="6"
                    placeholder="可补充周边配套、采光、户型优势、装修细节等，或点击右下角自动生成"
                    class="description-textarea"
                  />
                  <el-button
                    v-if="!isReadOnly"
                    type="primary"
                    plain
                    size="small"
                    :loading="generating"
                    class="generate-btn-inside"
                    @click="generateDescription"
                  >
                    自动生成
                  </el-button>
                </div>
              </el-form-item>
            </el-card>
          </div>
        </el-form>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api'
import { formatImageUrl } from '@/utils/imageUtils'
import {
  booleanOptions,
  createDefaultHouseForm,
  createUploadFileList,
  decorationOptions,
  hydrateHouseForm,
  normalizeHousePayload,
  orientationOptions,
  propertyTypeOptions
} from '../../utils/houseFormShared'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const formRef = ref()
const manualImageUrl = ref('')
const generating = ref(false)
const fileList = ref([])
const isUnitPriceManuallyEdited = ref(false)
const form = reactive({
  id: null,
  ...createDefaultHouseForm()
})

const editorId = computed(() => route.params.id || route.query.id || '')
const isEditMode = computed(() => Boolean(editorId.value))
const isReadOnly = computed(() => route.query.readonly === '1')
const pageTitle = computed(() => (isEditMode.value ? '编辑房源' : '新增房源'))
const modeLabel = computed(() => {
  if (isReadOnly.value) {
    return '审核预览'
  }
  return isEditMode.value ? '编辑模式' : '新增模式'
})
const submitButtonText = computed(() => (isEditMode.value ? '保存修改' : '提交房源'))
const backButtonText = computed(() => (isReadOnly.value ? '返回审核' : '取消'))
const heroText = computed(() =>
  isReadOnly.value
    ? '管理员可在这里查看房源完整资料与展示素材，确认信息无误后再返回审核列表处理。'
    : isEditMode.value
    ? '调整房源资料、图片和展示文案后，可直接保存修改并重新同步展示。'
    : '填写完整的房源资料、展示图片和亮点描述，有助于更快通过审核并提升曝光。'
)
const imageHintText = computed(() =>
  isReadOnly.value
    ? '审核预览模式下仅查看已提交的图片资料，不允许修改。'
    : isEditMode.value
    ? '修改后会重新走审核流，建议首图继续保持最有代表性的房源照片。'
    : '建议首图使用最具代表性的横向大图，列表卡片会优先展示第一张。'
)

const rules = {
  title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  price: [{ required: true, message: '请输入总价', trigger: 'change' }],
  area: [{ required: true, message: '请输入面积', trigger: 'change' }],
  houseType: [{ required: true, message: '请输入户型', trigger: 'blur' }]
}

const unitPricePreview = computed(() => {
  if (!form.price || !form.area) {
    return '待自动计算'
  }

  const area = Number(form.area)
  const price = Number(form.price)
  if (!area) {
    return '待自动计算'
  }

  return `${Math.round((price * 10000) / area).toLocaleString()} 元/㎡`
})

watch([() => form.price, () => form.area], ([newPrice, newArea]) => {
  if (!isUnitPriceManuallyEdited.value && newPrice && newArea) {
    const area = Number(newArea)
    const price = Number(newPrice)
    if (area > 0 && price > 0) {
      form.unitPrice = Math.round((price * 10000) / area)
    }
  }
})

const resetFormState = () => {
  Object.assign(form, { id: null, ...createDefaultHouseForm() })
  fileList.value = []
  manualImageUrl.value = ''
  generating.value = false
  isUnitPriceManuallyEdited.value = false
  formRef.value?.clearValidate?.()
}

const syncImageUrls = currentFiles => {
  form.imageUrls = currentFiles
    .map(item => item.response?.data?.url || item.url)
    .filter(Boolean)
    .map(url => formatImageUrl(url))
}

const addManualImage = () => {
  const url = manualImageUrl.value.trim()
  if (!url) {
    return
  }

  const normalizedUrl = formatImageUrl(url)
  if (form.imageUrls.includes(normalizedUrl)) {
    ElMessage.warning('这张图片已经添加过了')
    return
  }

  form.imageUrls.push(normalizedUrl)
  fileList.value = createUploadFileList(form.imageUrls)
  manualImageUrl.value = ''
}

const beforeUpload = file => {
  const isImage = file.type?.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
  }

  return isImage && isLt10M
}

const uploadImage = async ({ file, onSuccess, onError }) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/file/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    onSuccess(res)
  } catch (error) {
    onError(error)
  }
}

const handleUploadSuccess = (response, uploadFile, currentFiles) => {
  const imageUrl = response?.data?.url
  if (!imageUrl) {
    ElMessage.error('上传成功，但没有拿到图片地址')
    return
  }

  uploadFile.url = formatImageUrl(imageUrl)
  uploadFile.response = response
  syncImageUrls(currentFiles)
  ElMessage.success(`${uploadFile.name} 上传成功`)
}

const handleRemove = (_file, currentFiles) => {
  syncImageUrls(currentFiles)
}

const autoCalculateUnitPrice = () => {
  if (!form.price || !form.area) {
    ElMessage.warning('请先填写总价和面积')
    return
  }

  const area = Number(form.area)
  const price = Number(form.price)
  if (area > 0 && price > 0) {
    form.unitPrice = Math.round((price * 10000) / area)
    isUnitPriceManuallyEdited.value = false
    ElMessage.success('已根据总价和面积重新计算单价')
  }
}

const generateDescription = () => {
  if (!form.houseType && !form.area && !form.floor) {
    ElMessage.warning('请先填写户型、面积或楼层等基本信息')
    return
  }

  generating.value = true

  try {
    const lines = []

    const basicInfo = []
    if (form.houseType) basicInfo.push(form.houseType)
    if (form.area) basicInfo.push(`建筑面积${form.area}㎡`)
    if (form.floor && form.totalFloor) {
      basicInfo.push(`位于${form.floor}/${form.totalFloor}层`)
    } else if (form.floor) {
      basicInfo.push(form.floor)
    }
    if (basicInfo.length > 0) {
      lines.push(`【房源概况】${basicInfo.join('，')}`)
    }

    const locationInfo = []
    if (form.community) locationInfo.push(form.community)
    if (form.address) locationInfo.push(form.address)
    if (form.orientation) locationInfo.push(form.orientation)
    if (locationInfo.length > 0) {
      lines.push(`【位置朝向】${locationInfo.join('，')}`)
    }

    const facilityInfo = []
    if (form.decoration) facilityInfo.push(form.decoration)
    if (form.elevator === 1) facilityInfo.push('有电梯')
    if (form.heating === 1) facilityInfo.push('集中供暖')
    if (form.propertyFee) facilityInfo.push(`物业费${form.propertyFee}元/㎡/月`)
    if (facilityInfo.length > 0) {
      lines.push(`【装修配套】${facilityInfo.join('，')}`)
    }

    const propertyInfo = []
    if (form.propertyType) propertyInfo.push(form.propertyType)
    if (form.buildingYear) propertyInfo.push(`建于${form.buildingYear}年`)
    if (form.layout) propertyInfo.push(form.layout)
    if (propertyInfo.length > 0) {
      lines.push(`【房屋属性】${propertyInfo.join('，')}`)
    }

    const highlights = []
    if (form.price) {
      highlights.push(`售价${form.price}万`)
      if (form.price && form.area) {
        const unitPrice = Math.round((Number(form.price) * 10000) / Number(form.area))
        highlights.push(`单价${unitPrice.toLocaleString()}元/㎡`)
      }
    }
    if (form.tagsText) {
      highlights.push(`标签：${form.tagsText}`)
    }
    if (highlights.length > 0) {
      lines.push(`【房源亮点】${highlights.join('，')}`)
    }

    const description = lines.join('\n')
    if (!description) {
      ElMessage.warning('没有足够的信息生成描述，请填写更多房源信息')
      return
    }

    form.description = description
    ElMessage.success('描述已生成，您可以继续修改')
  } catch (error) {
    console.error('生成描述失败:', error)
    ElMessage.error('生成描述失败，请重试')
  } finally {
    generating.value = false
  }
}

const loadHouseDetail = async houseId => {
  loading.value = true
  try {
    const res = await request.get(`/house/detail/${houseId}`)
    hydrateHouseForm(form, res.data)
    fileList.value = createUploadFileList(form.imageUrls)
  } catch (error) {
    console.error('加载房源详情失败:', error)
    ElMessage.error('加载房源信息失败')
    router.push('/layout/house/manage')
  } finally {
    loading.value = false
  }
}

const initializePage = async () => {
  resetFormState()

  if (!isEditMode.value) {
    return
  }

  await loadHouseDetail(editorId.value)
}

const handleCancel = () => {
  if (isReadOnly.value) {
    // 只读模式返回审核页面
    router.back()
  } else {
    // 编辑/新增模式返回房源管理页面
    router.push('/agent-layout/house/manage')
  }
}

const handleSubmit = async () => {
  if (isReadOnly.value) {
    return
  }

  const valid = await formRef.value.validate().then(() => true).catch(() => false)
  if (!valid) {
    return
  }

  if (!form.unitPrice && form.price && form.area) {
    form.unitPrice = Math.round((Number(form.price) * 10000) / Number(form.area))
  }

  try {
    const payload = normalizeHousePayload(form)

    if (isEditMode.value) {
      await request.put('/house/update', payload)
      ElMessage.success('房源已更新')
    } else {
      await request.post('/house/add', payload)
      ElMessage.success('房源已提交，等待审核')
    }

    router.push('/agent-layout/house/manage')
  } catch (error) {
    console.error(isEditMode.value ? '更新房源失败:' : '提交房源失败:', error)
  }
}

watch(editorId, async (newId, oldId) => {
  if (newId !== oldId) {
    await initializePage()
  }
})

onMounted(async () => {
  await initializePage()
})
</script>

<style scoped>
.agent-dashboard-page {
  min-height: 100%;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(5, 150, 105, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(234, 179, 8, 0.1), transparent 22%),
    linear-gradient(180deg, #f7fcf8 0%, #eef8f1 100%);
}

.agent-dashboard-page :deep(.el-form-item__label) {
  margin-bottom: 10px;
  color: #163323;
  font-size: 16px;
  font-weight: 700;
}

.agent-dashboard-page :deep(.el-input__wrapper),
.agent-dashboard-page :deep(.el-textarea__inner),
.agent-dashboard-page :deep(.el-select__wrapper),
.agent-dashboard-page :deep(.el-input-number),
.agent-dashboard-page :deep(.el-segmented) {
  border-radius: 14px;
}

.agent-dashboard-page :deep(.el-upload--picture-card),
.agent-dashboard-page :deep(.el-upload-list__item) {
  border-radius: 18px;
}

.agent-dashboard-page :deep(.el-input__inner),
.agent-dashboard-page :deep(.el-textarea__inner),
.agent-dashboard-page :deep(.el-select .el-input__inner),
.agent-dashboard-page :deep(.el-input-number .el-input__inner) {
  font-size: 16px;
}

/* Hero 区域 - 白色卡片样式 */
.manage-hero {
  margin-bottom: 24px;
  display: flex;
  gap: 20px;
  padding: 0;
  border-radius: 16px;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.manage-hero__content {
  flex: 1;
  padding: 38px 32px 32px;
  min-width: 0;
}

.manage-hero .eyebrow {
  margin: 0 0 10px;
  color: #059669;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.manage-hero h1 {
  margin: 0;
  font-size: 34px;
  color: #163323;
}

.manage-hero__text {
  max-width: 600px;
  margin-top: 8px;
  line-height: 1.6;
  color: #6a7e73;
  font-size: 15px;
}

.manage-hero__actions {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 108px 32px 24px 0;
  min-width: 180px;
  align-self: stretch;
  gap: 14px;
}

.manage-hero__actions .el-button {
  min-width: 100px;
  min-height: 48px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 12px;
}

.manage-hero__actions .el-button--primary {
  border: none;
  background: linear-gradient(135deg, #047857 0%, #059669 100%);
  box-shadow: 0 12px 26px rgba(5, 150, 105, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.manage-hero__actions .el-button--primary:hover,
.manage-hero__actions .el-button--primary:focus {
  background: linear-gradient(135deg, #065f46 0%, #047857 100%);
  box-shadow: 0 16px 30px rgba(5, 150, 105, 0.26);
  transform: translateY(-1px);
}

/* 快捷操作卡片区域 */
.quick-action-section {
  margin-bottom: 24px;
}

.quick-action-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.9) 0%, rgba(209, 250, 229, 0.7) 100%);
  border: 1px solid rgba(5, 150, 105, 0.15);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  max-width: 400px;
}

.quick-action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(6, 78, 59, 0.12);
  border-color: rgba(5, 150, 105, 0.3);
}

.quick-action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #064e3b 0%, #059669 100%);
  border-radius: 16px;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}

.quick-action-content h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: #064e3b;
}

.quick-action-content p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.editor-form {
  display: grid;
  gap: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1.18fr 1fr;
  gap: 20px;
}

.section-card {
  border: 0;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 34px rgba(6, 78, 59, 0.08);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #e7f1ea;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-head h2 {
  margin: 0;
  color: #163323;
  font-size: 20px;
}

.section-head p {
  margin: 6px 0 0;
  color: #6a7e73;
  font-size: 14px;
  line-height: 1.6;
}

.field-grid {
  display: grid;
  gap: 18px;
}

.one-col {
  grid-template-columns: 1fr;
}

.two-col {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.three-col {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.four-col {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.input-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
  color: #7b8f84;
  font-size: 13px;
  line-height: 1.6;
}

.input-hint.single-line {
  display: block;
}

.auto-calc-btn {
  flex-shrink: 0;
}

.image-section-inside {
  margin-top: 15px;
  padding-top: 26px;
  border-top: 1px solid #e7f1ea;
}

.image-section-title {
  margin: 0 0 8px;
  color: #163323;
  font-size: 18px;
  font-weight: 700;
}

.image-section-desc {
  margin: 0 0 18px;
  color: #6a7e73;
  font-size: 14px;
}

.image-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  margin-bottom: 18px;
}

.upload-hint {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  color: #6a7e73;
  font-size: 13px;
}

.description-wrapper {
  position: relative;
  width: 100%;
}

.description-textarea :deep(.el-textarea__inner) {
  padding: 16px 18px 54px;
  line-height: 1.9;
}

.generate-btn-inside {
  position: absolute;
  right: 12px;
  bottom: 12px;
  height: auto;
  padding: 9px 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-soft);
  
  .el-button {
    min-width: 120px;
    font-weight: 600;
  }
}

@media (max-width: 1200px) {
  .form-grid,
  .three-col,
  .four-col {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .agent-dashboard-page {
    padding: 16px;
  }

  .manage-hero,
  .manage-hero__actions,
  .image-toolbar,
  .form-grid,
  .two-col,
  .three-col,
  .four-col,
  .upload-hint {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .manage-hero {
    flex-direction: column;
    padding: 0;
  }

  .manage-hero__content {
    padding: 24px 22px;
  }

  .manage-hero h1 {
    font-size: 28px;
  }

  .manage-hero__actions {
    padding: 0 22px 24px;
    justify-content: flex-start;
  }
}
</style>
