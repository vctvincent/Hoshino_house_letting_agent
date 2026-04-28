<template>
  <div class="message-page">
    <!-- 顶部 Hero 区域 -->
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow">REAMS Message Center</p>
        <h1>消息中心</h1>
        <p class="hero-text">在这里继续聊天、处理带看和查看业务消息。</p>
      </div>
      <div class="quick-actions" v-if="unreadTotal">
        <button type="button" class="quick-card" @click="markAllRead">
          <span class="quick-mark">✓</span>
          <div>
            <strong>全部已读</strong>
            <p>标记所有消息为已读</p>
          </div>
        </button>
      </div>
    </section>

    <aside class="sidebar">
      <div class="panel-head">
        <div>
          <p class="eyebrow">REAMS MESSAGE</p>
          <h2>消息中心</h2>
        </div>
        <el-button
          v-if="unreadTotal"
          type="primary"
          plain
          size="small"
          class="ghost-button"
          @click="markAllRead"
        >
          全部已读
        </el-button>
      </div>

      <div class="sidebar-search">
        <el-input v-model="keyword" placeholder="搜索会话、消息或联系人" clearable />
      </div>

      <div class="conversation-list" v-loading="loadingConversations">
        <button
          v-for="item in filteredConversations"
          :key="`${item.type}-${item.id}`"
          type="button"
          class="conversation-item"
          :class="{ active: isCurrentConversation(item) }"
          @click="openConversation(item)"
        >
          <div class="avatar-box">
            <el-avatar :size="48" :src="item.avatarUrl || undefined">{{ item.avatarText }}</el-avatar>
            <span v-if="item.unreadCount" class="unread-badge">{{ item.unreadCount }}</span>
          </div>

          <div class="conversation-main">
            <div class="line line-top">
              <strong class="conversation-name">{{ item.name }}</strong>
              <span class="conversation-time">{{ formatSidebarTime(item.lastMessage?.createTime) }}</span>
            </div>
            <div class="line line-bottom">
              <span class="conversation-role">{{ partnerTypeText(item) }}</span>
              <span class="ellipsis">{{ previewText(item.lastMessage) }}</span>
            </div>
          </div>
        </button>

        <el-empty
          v-if="!loadingConversations && !filteredConversations.length"
          description="暂无会话"
          :image-size="72"
        />
      </div>
    </aside>

    <section class="thread-panel">
      <template v-if="currentConversation">
        <header class="panel-head thread-header">
          <div class="thread-user">
            <el-avatar :size="44" :src="currentConversation.avatarUrl || undefined">{{ currentConversation.avatarText }}</el-avatar>
            <div>
              <h3>{{ currentConversation.name }}</h3>
              <p>{{ partnerTypeText(currentConversation) }}</p>
            </div>
          </div>

          <div class="thread-actions">
            <span class="thread-tip">在这里继续聊天、处理带看和查看业务消息。</span>
            <el-button
              v-if="canRequestViewing"
              type="primary"
              plain
              class="ghost-button"
              @click="openViewingDialog"
            >
              发起带看申请
            </el-button>
          </div>
        </header>

        <main ref="chatRef" class="message-list" v-loading="loadingMessages">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-row"
            :class="{ self: isSelf(msg) }"
          >
            <el-avatar v-if="!isSelf(msg)" :size="36" :src="currentConversation.avatarUrl || undefined" class="message-avatar">
              {{ currentConversation.avatarText }}
            </el-avatar>

            <div class="message-body">
              <div v-if="msg.messageScene !== SCENE.CHAT" class="system-card">
                <div class="card-top">
                  <div>
                    <span class="scene-tag">{{ sceneText(msg.messageScene) }}</span>
                    <h4>{{ msg.title || '系统消息' }}</h4>
                  </div>
                  <small>{{ formatFullTime(msg.createTime) }}</small>
                </div>

                <p class="card-content">{{ filterContent(msg.content) }}</p>

                <div
                  v-if="msg.houseInfo"
                  class="house-showcase-card"
                  :class="{ clickable: !!msg.houseId }"
                  @click="msg.houseId && goToHouseDetail(msg.houseId)"
                >
                  <div class="house-showcase-media">
                    <img
                      :src="msg.houseInfo.imageUrl"
                      :alt="msg.houseInfo.title"
                      @load="handleMessageMediaLoad"
                      @error="useDefaultImage"
                    />
                    <span class="house-scene-badge">{{ houseSceneBadge(msg) }}</span>
                  </div>
                  <div class="house-showcase-content">
                    <div class="house-showcase-head">
                      <strong>{{ msg.houseInfo.title }}</strong>
                      <span v-if="msg.houseInfo.priceText" class="house-price-pill">{{ msg.houseInfo.priceText }}</span>
                    </div>
                    <p class="house-showcase-location">{{ msg.houseInfo.locationText || msg.houseInfo.address || '暂无地址信息' }}</p>
                    <div class="house-showcase-tags">
                      <span v-if="msg.houseInfo.houseType">{{ msg.houseInfo.houseType }}</span>
                      <span v-if="msg.houseInfo.areaText">{{ msg.houseInfo.areaText }}</span>
                      <span v-if="msg.houseInfo.community">{{ msg.houseInfo.community }}</span>
                    </div>
                    <div class="house-showcase-price">
                      <div class="house-showcase-total">
                        <span class="price-symbol">￥</span>
                        <span class="price-number">{{ msg.houseInfo.priceValue || '面议' }}</span>
                        <span class="price-unit">万元</span>
                      </div>
                      <span v-if="msg.houseInfo.unitPriceText" class="house-showcase-unit">{{ msg.houseInfo.unitPriceText }}</span>
                    </div>
                  </div>
                </div>

                <div v-if="msg.viewingInfo" class="status-box">
                  <el-tag size="small" effect="light" :type="viewingStatusType(msg.viewingInfo.status)">
                    {{ viewingStatusText(msg.viewingInfo.status) }}
                  </el-tag>
                  <span>{{ formatFullTime(msg.viewingInfo.actualTime || msg.viewingInfo.appointTime) }}</span>
                </div>

                <div class="card-actions">
                  <el-button v-if="msg.viewingId" size="small" plain @click="showViewingDetail(msg)">
                    带看详情
                  </el-button>
                  <el-button v-if="canConfirm(msg)" size="small" type="success" plain @click="confirmViewing(msg)">
                    {{ myType === TYPE.AGENT ? '确认带看' : '接受带看' }}
                  </el-button>
                  <el-button v-if="canReject(msg)" size="small" type="danger" plain @click="rejectViewing(msg)">
                    {{ myType === TYPE.AGENT ? '拒绝' : '拒绝' }}
                  </el-button>
                  <el-button
                    v-if="canReview(msg)"
                    size="small"
                    type="primary"
                    plain
                    @click="goToProfile()"
                  >
                    去评价
                  </el-button>
                  <el-button v-if="msg.houseId" size="small" plain @click="goToHouseDetail(msg.houseId)">
                    查看房源
                  </el-button>
                </div>
              </div>

              <div v-else class="chat-bubble" :class="{ 'image-bubble': isImageMessage(msg) }">
                <template v-if="isImageMessage(msg)">
                  <el-image
                    class="message-image"
                    :src="msg.content"
                    :preview-src-list="[msg.content]"
                    fit="cover"
                    preview-teleported
                    @load="handleMessageMediaLoad"
                  />
                </template>
                <div v-else class="bubble-text">{{ msg.content }}</div>
                <small>{{ formatFullTime(msg.createTime) }}</small>
              </div>
            </div>

            <el-avatar v-if="isSelf(msg)" :size="36" :src="myAvatarUrl || undefined" class="message-avatar self-avatar">
              {{ myAvatarText }}
            </el-avatar>
          </div>

          <el-empty
            v-if="!loadingMessages && !messages.length"
            description="暂无消息"
            :image-size="82"
          />
        </main>

        <footer class="composer">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="hidden-file-input"
            @change="handleFileChange"
          />
          <div v-if="pendingImage.previewUrl" class="pending-image-card">
            <img :src="pendingImage.previewUrl" alt="待发送图片" class="pending-image-preview" />
            <div class="pending-image-meta">
              <strong>待发送图片</strong>
              <span>{{ pendingImage.name || '剪贴板图片' }}</span>
            </div>
            <el-button text class="pending-image-remove" @click="clearPendingImage">移除</el-button>
          </div>
          <el-input
            v-model="inputText"
            ref="composerInputRef"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入消息，按 Enter 发送"
            @keydown.enter.exact.prevent="sendComposer"
            @paste="handleComposerPaste"
            :disabled="isSystemConversation"
          />
          <div v-if="emojiPickerVisible" class="emoji-picker">
            <button
              v-for="emoji in emojiList"
              :key="emoji"
              type="button"
              class="emoji-item"
              @click="appendEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
          <div class="composer-bar">
            <span v-if="isSystemConversation" class="hint">系统通知会话不支持主动发送消息。</span>
            <span v-else class="hint">
              按 Enter 发送文本，图片会先在这里预览，确认后再一起发送。
            </span>
            <div class="composer-actions">
              <el-button
                class="icon-action"
                plain
                :disabled="isSystemConversation"
                @click="toggleEmojiPicker"
              >
                😊
              </el-button>
              <el-button
                class="icon-action"
                plain
                :disabled="isSystemConversation || imageUploading"
                @click="openFilePicker"
              >
                🖼
              </el-button>
              <el-button type="primary" :disabled="!canSubmitComposer" @click="sendComposer">
                发送
              </el-button>
            </div>
          </div>
        </footer>
      </template>

      <div v-else class="thread-empty">
        <el-empty description="选择一个会话开始查看消息" :image-size="96" />
      </div>
    </section>

    <el-dialog v-model="viewingDialogVisible" title="发起带看申请" width="640px" class="viewing-dialog">
      <el-form label-position="top" class="viewing-form">
        <el-form-item label="选择房源">
          <!-- 如果还没有选择房源，显示选择按钮 -->
          <div v-if="!selectedHouseForViewing" class="house-select-placeholder">
            <el-button
              type="primary"
              size="large"
              @click="goToSelectHouse"
              style="width: 100%"
            >
              <el-icon><Search /></el-icon>
              点击选择房源
            </el-button>
            <p class="placeholder-hint">将跳转到房源列表页面选择要带看的房源</p>
          </div>

          <!-- 如果已选择房源，显示房源信息卡片 -->
          <el-card v-else class="selected-house-card" shadow="hover">
            <div class="selected-house-header">
              <div class="selected-house-info">
                <h4>{{ selectedHouseForViewing.title }}</h4>
                <p class="house-location">{{ selectedHouseForViewing.location }}</p>
                <div class="house-tags">
                  <el-tag size="small">{{ selectedHouseForViewing.houseType }}</el-tag>
                  <el-tag size="small">{{ selectedHouseForViewing.area }}㎡</el-tag>
                  <el-tag v-if="selectedHouseForViewing.floor" size="small">{{ selectedHouseForViewing.floor }}层</el-tag>
                </div>
              </div>
              <div class="selected-house-price">
                <span class="price-amount">¥{{ selectedHouseForViewing.price }}万元</span>
                <span v-if="selectedHouseForViewing.unitPrice" class="unit-price">{{ selectedHouseForViewing.unitPrice }}元/㎡</span>
              </div>
            </div>
            <div class="selected-house-image">
              <img :src="selectedHouseForViewing.imageUrl" :alt="selectedHouseForViewing.title" @error="handleImageError" />
            </div>
            <div class="selected-house-actions">
              <el-button type="primary" plain size="small" @click="goToSelectHouse">
                更换房源
              </el-button>
              <el-button type="danger" plain size="small" @click="clearSelectedHouse">
                清除选择
              </el-button>
            </div>
          </el-card>
        </el-form-item>

        <el-form-item label="预约时间">
          <el-date-picker
            v-model="viewingForm.appointTime"
            type="datetime"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disablePastDate"
          />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input
            v-model="viewingForm.phone"
            maxlength="11"
            placeholder="请输入联系电话"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="viewingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitViewingRequest" :disabled="!viewingForm.houseId">发送申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="带看详情" width="720px">
      <el-descriptions v-if="detailRecord" :column="2" border>
        <el-descriptions-item label="房源标题" :span="2">
          {{ detailRecord.houseTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="客户">
          {{ detailRecord.customerName || detailRecord.customerPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ viewingStatusText(detailRecord.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatFullTime(detailRecord.appointTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="完成时间">
          {{ detailRecord.actualTime ? formatFullTime(detailRecord.actualTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="房源地址" :span="2">
          {{ detailRecord.houseAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailRecord.cancelReason" label="取消原因" :span="2">
          {{ detailRecord.cancelReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailRecord.remark" label="备注" :span="2">
          {{ detailRecord.remark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/api'
import { formatImageUrl, formatImageUrls } from '@/utils/imageUtils'

const TYPE = { ADMIN: 1, AGENT: 2, CUSTOMER: 3 }
const SCENE = {
  CHAT: 'CHAT',
  VIEWING_REQUEST: 'VIEWING_REQUEST',
  VIEWING_REVIEW_INVITE: 'VIEWING_REVIEW_INVITE',
  VIEWING_CONFIRMED: 'VIEWING_CONFIRMED',
  VIEWING_REJECTED: 'VIEWING_REJECTED',
  HOUSE_AUDIT_APPROVED: 'HOUSE_AUDIT_APPROVED',
  HOUSE_AUDIT_REJECTED: 'HOUSE_AUDIT_REJECTED',
  TRANSACTION_CREATED: 'TRANSACTION_CREATED',
  TRANSACTION_STATUS_UPDATED: 'TRANSACTION_STATUS_UPDATED',
  TRANSACTION_NEGOTIATION: 'TRANSACTION_NEGOTIATION'
}

const PHONE_RE = /^1\d{10}$/
const typeMap = { admin: TYPE.ADMIN, agent: TYPE.AGENT, customer: TYPE.CUSTOMER, ADMIN: TYPE.ADMIN, AGENT: TYPE.AGENT, CUSTOMER: TYPE.CUSTOMER }

const route = useRoute()
const router = useRouter()
const store = useStore()

const goToHouseDetail = houseId => {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    router.push(`/agent-layout/house/detail/${houseId}`)
    return
  }
  if (currentPath.startsWith('/admin-layout/')) {
    router.push(`/admin-layout/house/detail/${houseId}`)
    return
  }
  router.push(`/layout/house/detail/${houseId}`)
}

const goToProfile = () => {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    router.push('/agent-layout/profile')
    return
  }
  if (currentPath.startsWith('/admin-layout/')) {
    router.push('/admin-layout/profile')
    return
  }
  router.push('/layout/profile')
}

const getMessageRoutePath = () => {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    return '/agent-layout/message'
  }
  if (currentPath.startsWith('/admin-layout/')) {
    return '/admin-layout/message'
  }
  return '/layout/message'
}

const getHouseListRoutePath = () => {
  const currentPath = route.path || ''
  if (currentPath.startsWith('/agent-layout/')) {
    return '/agent-layout/house/list'
  }
  if (currentPath.startsWith('/admin-layout/')) {
    return '/admin-layout/house/list'
  }
  return '/layout/house/list'
}

const chatRef = ref()
const composerInputRef = ref()
const fileInputRef = ref()
const keyword = ref('')
const inputText = ref('')
const emojiPickerVisible = ref(false)
const imageUploading = ref(false)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const viewingDialogVisible = ref(false)
const detailVisible = ref(false)
const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const houseOptions = ref([])
const detailRecord = ref(null)
const houseMap = ref({})
const viewingMap = ref({})
const pendingImage = reactive({
  file: null,
  previewUrl: '',
  name: ''
})
const emojiList = ['😀', '😁', '😂', '🤣', '😊', '😍', '🥳', '😎', '👍', '👏', '🎉', '🔥', '❤️', '🏠', '💼', '📷']

const viewingForm = reactive({
  houseId: '',
  appointTime: '',
  phone: ''
})

// 选中的房源信息(用于带看申请)
const selectedHouseForViewing = ref(null)

const formatPriceValue = price => {
  if (price === null || price === undefined || price === '') return ''
  const num = Number(price)
  return Number.isFinite(num) && num > 0 ? num.toFixed(2) : ''
}

const formatUnitPriceValue = unitPrice => {
  const num = Number(unitPrice)
  return Number.isFinite(num) && num > 0 ? `${Math.round(num)}元/㎡` : ''
}

const formatAreaValue = area => {
  const num = Number(area)
  return Number.isFinite(num) && num > 0 ? `${num}㎡` : ''
}

const buildHouseLocation = house => {
  const pieces = [house?.district, house?.community, house?.address].filter(Boolean)
  return pieces.join(' · ') || house?.address || ''
}

const mapHouseCard = house => ({
  id: Number(house?.id),
  title: house?.title || '房源信息待补充',
  address: house?.address || '',
  locationText: buildHouseLocation(house),
  priceText: formatPriceValue(house?.price) ? `${formatPriceValue(house.price)} 万元` : '',
  priceValue: formatPriceValue(house?.price),
  unitPriceText: formatUnitPriceValue(house?.unitPrice),
  imageUrl: formatImageUrls(house?.images || [])[0] || '/default-house.jpg',
  houseType: house?.houseType || '',
  areaText: formatAreaValue(house?.area),
  community: house?.community || '',
  floorText:
    house?.floor && house?.totalFloor
      ? `${house.floor}/${house.totalFloor}层`
      : house?.floor
        ? `${house.floor}`
        : '',
  tagText: [house?.district, house?.propertyType].filter(Boolean)[0] || ''
})

const userInfo = computed(() => store.getters.userInfo || {})
const role = computed(() => store.getters.role || '')
const myId = computed(() => userInfo.value.userId || userInfo.value.id)
const myType = computed(() => {
  if (role.value === 'ROLE_ADMIN') return TYPE.ADMIN
  if (role.value === 'ROLE_AGENT') return TYPE.AGENT
  return TYPE.CUSTOMER
})

const myAvatarUrl = computed(() => formatImageUrl(userInfo.value.avatar))

const myAvatarText = computed(() => {
  const text = userInfo.value.nickname || userInfo.value.name || userInfo.value.phone || '我'
  return String(text).slice(0, 1)
})

const unreadTotal = computed(() =>
  conversations.value.reduce((sum, item) => sum + Number(item.unreadCount || 0), 0)
)

const filteredConversations = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  const baseList = conversations.value.filter(item => {
    const isSelfConversation =
      Number(item.id) === Number(myId.value) &&
      Number(item.type) === Number(myType.value)
    return !isSelfConversation
  })

  if (!q) return baseList

  return baseList.filter(item =>
    [item.name, item.lastMessage?.title, item.lastMessage?.content]
      .filter(Boolean)
      .some(text => String(text).toLowerCase().includes(q))
  )
})

const isSystemConversation = computed(
  () => currentConversation.value?.type === TYPE.ADMIN && currentConversation.value?.id === 0
)

const canSendText = computed(() => !!inputText.value.trim())
const canSubmitComposer = computed(
  () =>
    !!currentConversation.value &&
    !isSystemConversation.value &&
    !imageUploading.value &&
    (canSendText.value || !!pendingImage.file)
)

const canRequestViewing = computed(
  () => !isSystemConversation.value && (
    // 客户可以和中介聊天时发起带看
    (myType.value === TYPE.CUSTOMER && currentConversation.value?.type === TYPE.AGENT) ||
    // 中介可以与客户聊天时发起带看
    (myType.value === TYPE.AGENT && currentConversation.value?.type === TYPE.CUSTOMER)
  )
)

const formatSidebarTime = time => {
  if (!time) return ''
  const date = new Date(time)
  return date.toDateString() === new Date().toDateString()
    ? date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    : date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const formatFullTime = time => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const partnerTypeText = item => {
  if (!item) return ''
  if (item.type === TYPE.ADMIN && item.id === 0) return '系统通知'
  if (item.type === TYPE.ADMIN) return '管理员'
  if (item.type === TYPE.AGENT) return '中介'
  return '客户'
}

const sceneText = scene =>
  ({
    [SCENE.CHAT]: '聊天消息',
    [SCENE.VIEWING_REQUEST]: '带看申请',
    [SCENE.VIEWING_REVIEW_INVITE]: '评价邀请',
    [SCENE.VIEWING_CONFIRMED]: '带看确认',
    [SCENE.VIEWING_REJECTED]: '带看取消',
    [SCENE.HOUSE_AUDIT_APPROVED]: '房源审核',
    [SCENE.HOUSE_AUDIT_REJECTED]: '房源审核',
    [SCENE.TRANSACTION_CREATED]: '交易创建',
    [SCENE.TRANSACTION_STATUS_UPDATED]: '交易状态',
    [SCENE.TRANSACTION_NEGOTIATION]: '价格协商'
  }[scene] || '系统消息')

const previewText = msg => {
  if (!msg) return '暂无消息'
  if ((msg.contentType || 'TEXT') === 'IMAGE') return '[图片]'
  if (msg.messageScene && msg.messageScene !== SCENE.CHAT) {
    return `${sceneText(msg.messageScene)} · ${msg.title || msg.content || '系统消息'}`
  }
  return msg.content || msg.title || '暂无消息'
}

const houseSceneBadge = msg =>
  ({
    [SCENE.VIEWING_REQUEST]: '带看房源',
    [SCENE.VIEWING_CONFIRMED]: '确认带看',
    [SCENE.VIEWING_REJECTED]: '带看变更',
    [SCENE.VIEWING_REVIEW_INVITE]: '评价邀请',
    [SCENE.TRANSACTION_CREATED]: '交易房源',
    [SCENE.TRANSACTION_STATUS_UPDATED]: '交易进度',
    [SCENE.TRANSACTION_NEGOTIATION]: '协商房源',
    [SCENE.HOUSE_AUDIT_APPROVED]: '审核通过',
    [SCENE.HOUSE_AUDIT_REJECTED]: '审核退回'
  }[msg.messageScene] || '关联房源')

/**
 * 过滤消息内容，移除冗余的房源图片行
 */
const filterContent = content => {
  if (!content) return ''
  // 移除"🖼️ 房源图片："开头的整行
  return content
    .split('\n')
    .filter(line => !line.includes('🖼️ 房源图片：'))
    .join('\n')
}

const viewingStatusText = status =>
  ({
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消',
    4: '已过期'
  }[status] || '未知状态')

const viewingStatusType = status =>
  ({
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'info'
  }[status] || 'info')

const isSelf = msg =>
  Number(msg.senderId) === Number(myId.value) &&
  Number(msg.senderType) === Number(myType.value)

const isImageMessage = msg => (msg.contentType || 'TEXT') === 'IMAGE'

const isCurrentConversation = item =>
  currentConversation.value &&
  Number(currentConversation.value.id) === Number(item.id) &&
  Number(currentConversation.value.type) === Number(item.type)

const disablePastDate = date => date.getTime() < Date.now() - 60 * 1000

const useDefaultImage = event => {
  event.target.src = '/default-house.jpg'
}

const scrollBottom = async () => {
  await nextTick()

  await new Promise(resolve => {
    requestAnimationFrame(() => {
      requestAnimationFrame(resolve)
    })
  })

  if (chatRef.value) {
    chatRef.value.scrollTop = chatRef.value.scrollHeight
  }
}

const handleMessageMediaLoad = () => {
  scrollBottom()
}

const fetchPartnerMeta = async (id, type) => {
  try {
    if (type === TYPE.AGENT) {
      const res = await request.get(`/user/agent/${id}`)
      const text = res.data?.name || res.data?.phone || `中介 ${id}`
      return { name: text, avatarText: String(text).slice(0, 1), avatarUrl: formatImageUrl(res.data?.avatar) }
    }

    if (type === TYPE.CUSTOMER) {
      const res = await request.get(`/user/customer/${id}`)
      const text = res.data?.nickname || res.data?.phone || `客户 ${id}`
      return { name: text, avatarText: String(text).slice(0, 1), avatarUrl: formatImageUrl(res.data?.avatar) }
    }

    return {
      name: id === 0 ? '星野' : '管理员',
      avatarText: id === 0 ? '星' : '管'
    }
  } catch {
    return {
      name: type === TYPE.AGENT ? `中介 ${id}` : type === TYPE.CUSTOMER ? `客户 ${id}` : '星野',
      avatarText: type === TYPE.AGENT ? '中' : type === TYPE.CUSTOMER ? '客' : '星'
    }
  }
}

const ensureHouseInfo = async ids => {
  const missing = ids.filter(id => id && !houseMap.value[id])
  if (!missing.length) return

  const list = await Promise.all(
    missing.map(async id => {
      try {
        const res = await request.get(`/house/detail/${id}`)
        return [id, mapHouseCard(res.data)]
      } catch {
        return [id, null]
      }
    })
  )

  houseMap.value = {
    ...houseMap.value,
    ...Object.fromEntries(list.filter(([, value]) => value))
  }
}

const findViewingFallback = async msg => {
  if (!msg.houseId || !currentConversation.value || currentConversation.value.type === TYPE.ADMIN) return null

  const customerId =
    myType.value === TYPE.CUSTOMER
      ? myId.value
      : currentConversation.value.type === TYPE.CUSTOMER
        ? currentConversation.value.id
        : null

  const agentId =
    myType.value === TYPE.AGENT
      ? myId.value
      : currentConversation.value.type === TYPE.AGENT
        ? currentConversation.value.id
        : null

  if (!customerId || !agentId) return null

  try {
    const res = await request.get('/viewing/list', {
      params: {
        customerId,
        agentId,
        houseId: msg.houseId,
        pageNum: 1,
        pageSize: 20
      }
    })
    return res.data?.list?.[0] || null
  } catch {
    return null
  }
}

const ensureViewingInfo = async list => {
  const ids = [...new Set(list.map(item => item.viewingId).filter(Boolean))]

  if (ids.length) {
    const details = await Promise.all(
      ids.map(async id => {
        try {
          const res = await request.get(`/viewing/detail/${id}`)
          return [id, res.data]
        } catch {
          return [id, null]
        }
      })
    )

    viewingMap.value = {
      ...viewingMap.value,
      ...Object.fromEntries(details.filter(([, value]) => value))
    }
  }

  for (const msg of list) {
    if (!msg.viewingId && msg.messageScene === SCENE.VIEWING_REQUEST) {
      const fallback = await findViewingFallback(msg)
      if (fallback) {
        msg.viewingId = fallback.id
        viewingMap.value = {
          ...viewingMap.value,
          [fallback.id]: fallback
        }
      }
    }
  }
}

const normalizeMessage = msg => ({
  ...msg,
  contentType: msg.contentType || msg.lastContentType || 'TEXT',
  messageScene: msg.messageScene || (msg.messageType === 2 ? SCENE.VIEWING_REQUEST : SCENE.CHAT),
  houseInfo: msg.houseId ? houseMap.value[msg.houseId] : null,
  viewingInfo: msg.viewingId ? viewingMap.value[msg.viewingId] : null
})

const loadConversations = async () => {
  loadingConversations.value = true
  try {
    const res = await request.get('/message/conversations', {
      params: { userId: myId.value, userType: myType.value }
    })

    conversations.value = await Promise.all(
      (res.data || []).map(async item => {
        const meta = await fetchPartnerMeta(Number(item.id), Number(item.type))
        return {
          id: Number(item.id),
          type: Number(item.type),
          unreadCount: Number(item.unread_count || 0),
          name: meta.name,
          avatarText: meta.avatarText,
          avatarUrl: meta.avatarUrl,
          lastMessage: {
            id: item.last_msg_id,
            senderId: item.last_sender_id,
            senderType: item.last_sender_type,
            receiverId: item.last_receiver_id,
            receiverType: item.last_receiver_type,
            messageType: item.last_message_type,
            contentType: item.last_content_type || 'TEXT',
            title: item.last_title,
            content: item.last_content,
            createTime: item.last_time
          }
        }
      })
    )

  } catch (error) {
    console.error('加载会话失败:', error)
    ElMessage.error('加载会话失败')
  } finally {
    loadingConversations.value = false
  }
}

const appendAvailableContacts = async () => {
  try {
    const existingKeys = new Set(conversations.value.map(item => `${item.type}-${item.id}`))
    const extras = []

    if (myType.value === TYPE.CUSTOMER) {
      const res = await request.get('/user/agent/all')
      for (const agent of res.data || []) {
        const key = `${TYPE.AGENT}-${agent.id}`
        if (existingKeys.has(key) || Number(agent.id) === Number(myId.value)) continue
        extras.push({
          id: Number(agent.id),
          type: TYPE.AGENT,
          unreadCount: 0,
          name: agent.name || agent.phone || `中介 ${agent.id}`,
          avatarText: String(agent.name || agent.phone || '中').slice(0, 1),
          avatarUrl: formatImageUrl(agent.avatar),
          lastMessage: null
        })
      }
    } else if (myType.value === TYPE.AGENT) {
      const res = await request.get('/user/customer/all')
      for (const customer of res.data || []) {
        const key = `${TYPE.CUSTOMER}-${customer.id}`
        if (existingKeys.has(key) || Number(customer.id) === Number(myId.value)) continue
        extras.push({
          id: Number(customer.id),
          type: TYPE.CUSTOMER,
          unreadCount: 0,
          name: customer.nickname || customer.phone || `客户 ${customer.id}`,
          avatarText: String(customer.nickname || customer.phone || '客').slice(0, 1),
          avatarUrl: formatImageUrl(customer.avatar),
          lastMessage: null
        })
      }
    } else if (myType.value === TYPE.ADMIN) {
      const [agentRes, customerRes] = await Promise.all([
        request.get('/user/agent/all'),
        request.get('/user/customer/all')
      ])

      for (const agent of agentRes.data || []) {
        const key = `${TYPE.AGENT}-${agent.id}`
        if (!existingKeys.has(key)) {
          extras.push({
            id: Number(agent.id),
            type: TYPE.AGENT,
            unreadCount: 0,
            name: agent.name || agent.phone || `中介 ${agent.id}`,
            avatarText: String(agent.name || agent.phone || '中').slice(0, 1),
            avatarUrl: formatImageUrl(agent.avatar),
            lastMessage: null
          })
        }
      }

      for (const customer of customerRes.data || []) {
        const key = `${TYPE.CUSTOMER}-${customer.id}`
        if (!existingKeys.has(key)) {
          extras.push({
            id: Number(customer.id),
            type: TYPE.CUSTOMER,
            unreadCount: 0,
            name: customer.nickname || customer.phone || `客户 ${customer.id}`,
            avatarText: String(customer.nickname || customer.phone || '客').slice(0, 1),
            avatarUrl: formatImageUrl(customer.avatar),
            lastMessage: null
          })
        }
      }
    }

    if (extras.length) {
      conversations.value = [...conversations.value, ...extras]
    }
  } catch (error) {
    console.error('补充联系人失败:', error)
  }
}

const markIncomingRead = async raw => {
  const unread = raw.filter(
    msg =>
      Number(msg.receiverId) === Number(myId.value) &&
      Number(msg.receiverType) === Number(myType.value) &&
      Number(msg.isRead) === 0
  )

  if (!unread.length) return

  await Promise.all(unread.map(msg => request.put(`/message/read/${msg.id}`)))

  conversations.value = conversations.value.map(item =>
    currentConversation.value && isCurrentConversation(item)
      ? { ...item, unreadCount: 0 }
      : item
  )
}

const loadMessages = async conversation => {
  loadingMessages.value = true
  let loaded = false
  try {
    const res = await request.get('/message/chat', {
      params: {
        userId: myId.value,
        userType: myType.value,
        targetId: conversation.id,
        targetType: conversation.type,
        pageNum: 1,
        pageSize: 100
      }
    })

    const raw = res.data?.list || []
    await ensureHouseInfo(raw.map(item => item.houseId).filter(Boolean))
    await ensureViewingInfo(raw)
    messages.value = raw.map(normalizeMessage)
    await markIncomingRead(raw)
    loaded = true
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
  }

  if (loaded) {
    await scrollBottom()
  }
}

const openConversation = async item => {
  currentConversation.value = item
  inputText.value = ''
  emojiPickerVisible.value = false
  await loadMessages(item)
}

const sendTextMessage = async () => {
  try {
    await request.post('/message/send', {
      senderId: myId.value,
      senderType: myType.value,
      receiverId: currentConversation.value.id,
      receiverType: currentConversation.value.type,
      messageType: 2,
      contentType: 'TEXT',
      messageScene: SCENE.CHAT,
      title: '聊天消息',
      content: inputText.value.trim()
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    throw error
  }
}

const sendComposer = async () => {
  if (!canSubmitComposer.value) return

  try {
    if (canSendText.value) {
      await sendTextMessage()
    }

    if (pendingImage.file) {
      await uploadImageAndSend(pendingImage.file, false)
    }

    inputText.value = ''
    emojiPickerVisible.value = false
    clearPendingImage()
    await Promise.all([loadMessages(currentConversation.value), loadConversations()])
  } catch {
    // individual branches already show user-facing errors
  }
}

const appendEmoji = emoji => {
  if (isSystemConversation.value) return
  inputText.value += emoji
  nextTick(() => {
    const textarea = composerInputRef.value?.textarea
    textarea?.focus()
  })
}

const toggleEmojiPicker = () => {
  if (isSystemConversation.value) return
  emojiPickerVisible.value = !emojiPickerVisible.value
}

const openFilePicker = () => {
  if (isSystemConversation.value || imageUploading.value) return
  fileInputRef.value?.click()
}

const clearPendingImage = () => {
  if (pendingImage.previewUrl) {
    URL.revokeObjectURL(pendingImage.previewUrl)
  }
  pendingImage.file = null
  pendingImage.previewUrl = ''
  pendingImage.name = ''
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const setPendingImage = file => {
  if (!file) return
  clearPendingImage()
  pendingImage.file = file
  pendingImage.previewUrl = URL.createObjectURL(file)
  pendingImage.name = file.name || ''
}

const sendImageMessage = async imageUrl => {
  await request.post('/message/send', {
    senderId: myId.value,
    senderType: myType.value,
    receiverId: currentConversation.value.id,
    receiverType: currentConversation.value.type,
    messageType: 2,
    contentType: 'IMAGE',
    messageScene: SCENE.CHAT,
    title: '图片消息',
    content: imageUrl
  })
}

const uploadImageAndSend = async (file, refreshAfterSend = true) => {
  if (!file || imageUploading.value || isSystemConversation.value) return

  const formData = new FormData()
  formData.append('file', file)
  imageUploading.value = true

  try {
    const res = await request.post('/file/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await sendImageMessage(res.data?.url || res.url)
    if (refreshAfterSend) {
      await Promise.all([loadMessages(currentConversation.value), loadConversations()])
      clearPendingImage()
      ElMessage.success('图片已发送')
    }
  } catch (error) {
    console.error('发送图片失败:', error)
    ElMessage.error(error.message || '发送图片失败')
    throw error
  } finally {
    imageUploading.value = false
  }
}

const handleFileChange = async event => {
  const file = event.target.files?.[0]
  setPendingImage(file)
}

const handleComposerPaste = async event => {
  const items = Array.from(event.clipboardData?.items || [])
  const imageItem = items.find(item => item.type?.startsWith('image/'))
  if (!imageItem) return

  event.preventDefault()
  const file = imageItem.getAsFile()
  setPendingImage(file)
}

const loadAgentHouses = async agentId => {
  const res = await request.get('/house/list', {
    params: {
      agentId,
      houseStatus: 1,
      auditStatus: 2,
      pageNum: 1,
      pageSize: 100
    }
  })

  houseOptions.value = (res.data?.list || []).map(item => ({
    id: item.id,
    label: `${item.title} · ${item.price} 万元 · ${item.houseType || '待补充户型'}`
  }))
}

const loadAgentHouseCards = async agentId => {
  const res = await request.get('/house/list', {
    params: {
      agentId,
      houseStatus: 1,
      auditStatus: 2,
      pageNum: 1,
      pageSize: 100
    }
  })

  houseOptions.value = (res.data?.list || []).map(item => mapHouseCard(item))
}

const openViewingDialog = async () => {
  viewingForm.houseId = route.query.currentHouseId ? Number(route.query.currentHouseId) : ''
  viewingForm.appointTime = ''
  viewingForm.phone = userInfo.value?.phone || ''

  // 如果有路由传入的选中房源,先加载房源信息
  if (route.query.selectedHouseId) {
    await loadSelectedHouseInfo(route.query.selectedHouseId)
  }

  viewingDialogVisible.value = true
}

// 跳转到房源列表选择房源
const goToSelectHouse = () => {
  if (!currentConversation.value?.id) {
    ElMessage.warning('请先选择中介')
    return
  }

  // 关闭对话框
  viewingDialogVisible.value = false

  // 跳转到房源列表,传递中介ID和选择模式标记
  router.push({
    path: getHouseListRoutePath(),
    query: {
      selectForViewing: '1',
      listingAgentId: myType.value === TYPE.AGENT ? myId.value : currentConversation.value.id,
      targetId: currentConversation.value.id,
      targetType: currentConversation.value.type,
      returnTo: getMessageRoutePath()
    }
  })
}

// 加载选中的房源信息
const loadSelectedHouseInfo = async (houseId) => {
  try {
    const res = await request.get(`/house/detail/${houseId}`)
    const house = res.data

    selectedHouseForViewing.value = {
      id: house.id,
      title: house.title || '房源信息待补充',
      location: buildHouseLocation(house),
      price: formatPriceValue(house.price),
      unitPrice: formatUnitPriceValue(house.unitPrice),
      imageUrl: formatImageUrls(house.images || [])[0] || '/default-house.jpg',
      houseType: house.houseType || '',
      area: formatAreaValue(house.area),
      floor: house.floor ? `${house.floor}/${house.totalFloor}层` : ''
    }

    viewingForm.houseId = house.id
  } catch (error) {
    console.error('加载房源信息失败:', error)
    ElMessage.error('加载房源信息失败')
  }
}

// 清除选中的房源
const clearSelectedHouse = () => {
  selectedHouseForViewing.value = null
  viewingForm.houseId = ''
}

const submitViewingRequest = async () => {
  if (!viewingForm.houseId) return ElMessage.warning('请选择房源')
  if (!viewingForm.appointTime) return ElMessage.warning('请选择预约时间')
  if (!PHONE_RE.test(viewingForm.phone)) return ElMessage.warning('请输入有效手机号')

  try {
    // 根据角色调用不同的API
    if (myType.value === TYPE.AGENT) {
      // 中介发起带看申请
      await request.post('/viewing/agent/book', {
        customerId: currentConversation.value.id,
        houseId: viewingForm.houseId,
        appointTime: new Date(viewingForm.appointTime).toISOString(),
        customerPhone: viewingForm.phone
      })
    } else {
      // 客户发起带看申请
      await request.post('/viewing/book', {
        agentId: currentConversation.value.id,
        houseId: viewingForm.houseId,
        appointTime: new Date(viewingForm.appointTime).toISOString(),
        customerPhone: viewingForm.phone
      })
    }

    viewingDialogVisible.value = false
    ElMessage.success('带看申请已发送')
    await Promise.all([loadMessages(currentConversation.value), loadConversations()])
  } catch (error) {
    console.error('发送带看申请失败:', error)
    ElMessage.error(error.message || '发送带看申请失败')
  }
}

const canConfirm = msg => {
  // 中介确认客户发起的带看 (状态0)
  if (myType.value === TYPE.AGENT &&
      msg.messageScene === SCENE.VIEWING_REQUEST &&
      !isSelf(msg) &&
      (msg.viewingInfo?.status === 0 || msg.viewingInfo?.status === undefined)) {
    return true
  }
  
  // 客户确认中介发起的带看 (状态4)
  if (myType.value === TYPE.CUSTOMER &&
      msg.messageScene === SCENE.VIEWING_REQUEST &&
      !isSelf(msg) &&
      msg.viewingInfo?.status === 4) {
    return true
  }
  
  return false
}

const canReject = canConfirm

const canReview = msg =>
  myType.value === TYPE.CUSTOMER &&
  msg.messageScene === SCENE.VIEWING_REVIEW_INVITE &&
  msg.viewingInfo?.status === 2

const showViewingDetail = async msg => {
  if (!msg.viewingId) return ElMessage.warning('未找到关联带看记录')

  try {
    const res = await request.get(`/viewing/detail/${msg.viewingId}`)
    detailRecord.value = res.data
    detailVisible.value = true
  } catch {
    ElMessage.error('加载带看详情失败')
  }
}

const confirmViewing = async msg => {
  try {
    await ElMessageBox.confirm('确认接受这次带看申请吗？', '确认带看', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    // 根据角色和状态调用不同的API
    if (myType.value === TYPE.CUSTOMER && msg.viewingInfo?.status === 4) {
      // 客户确认中介发起的带看
      await request.post(`/viewing/customer/confirm/${msg.viewingId}`)
    } else {
      // 中介确认客户发起的带看
      await request.post(`/viewing/confirm/${msg.viewingId}`)
    }
    
    await request.post('/message/send', {
      senderId: myId.value,
      senderType: myType.value,
      receiverId: msg.senderId,
      receiverType: msg.senderType,
      messageType: 1,
      messageScene: SCENE.VIEWING_CONFIRMED,
      title: '带看已确认',
      content: myType.value === TYPE.CUSTOMER 
        ? '客户已经确认本次带看申请，请按约定时间准时带看。'
        : '中介已经确认本次带看申请，请按约定时间准时到场。',
      houseId: msg.houseId,
      viewingId: msg.viewingId
    })

    ElMessage.success('已确认带看')
    await Promise.all([loadMessages(currentConversation.value), loadConversations()])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '确认带看失败')
    }
  }
}

const rejectViewing = async msg => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入取消原因，客户会在消息里看到这条说明。',
      '取消带看',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputValidator: val => (val && val.trim() ? true : '请填写取消原因')
      }
    )

    await request.post(`/viewing/cancel/${msg.viewingId}`, null, {
      params: { reason: value }
    })

    await request.post('/message/send', {
      senderId: myId.value,
      senderType: myType.value,
      receiverId: msg.senderId,
      receiverType: msg.senderType,
      messageType: 1,
      messageScene: SCENE.VIEWING_REJECTED,
      title: '带看已取消',
      content: `本次带看已取消，原因：${value}`,
      houseId: msg.houseId,
      viewingId: msg.viewingId
    })

    ElMessage.success('已取消带看')
    await Promise.all([loadMessages(currentConversation.value), loadConversations()])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消带看失败')
    }
  }
}

const markAllRead = async () => {
  try {
    await request.post('/message/read/all', null, {
      params: { userId: myId.value, userType: myType.value }
    })
    await loadConversations()
    if (currentConversation.value) {
      await loadMessages(currentConversation.value)
    }
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('全部已读失败')
  }
}

const initFromRoute = async () => {
  const { targetId, targetType } = route.query

  if (!targetId || !targetType) {
    if (!currentConversation.value && conversations.value.length) {
      await openConversation(conversations.value[0])
    }
    return
  }

  const mappedType = typeMap[targetType] || Number(targetType)
  
  // 防御性检查:确保mappedType是有效数字
  if (!mappedType || isNaN(mappedType)) {
    console.warn('无效的targetType:', targetType)
    ElMessage.warning('会话类型错误')
    return
  }
  
  const existing = conversations.value.find(
    item => Number(item.id) === Number(targetId) && Number(item.type) === Number(mappedType)
  )

  if (existing) {
    await openConversation(existing)
    return
  }

  const meta = await fetchPartnerMeta(Number(targetId), Number(mappedType))
  const tempConversation = {
    id: Number(targetId),
    type: Number(mappedType),
    unreadCount: 0,
    name: meta.name,
    avatarText: meta.avatarText,
    avatarUrl: meta.avatarUrl,
    lastMessage: null
  }

  conversations.value = [tempConversation, ...conversations.value]
  await openConversation(tempConversation)
}

onMounted(async () => {
  await loadConversations()
  await initFromRoute()
})

watch(
  () => [route.query.targetId, route.query.targetType, route.query.targetName, route.query.currentHouseId, route.query.selectedHouseId],
  async ([newTargetId, newTargetType, , , newSelectedHouseId], [oldTargetId, oldTargetType, , , oldSelectedHouseId]) => {
    if (newTargetId === oldTargetId && newTargetType === oldTargetType && newSelectedHouseId === oldSelectedHouseId) return
    await loadConversations()
    await initFromRoute()

    // 如果有选中的房源ID,加载房源信息并自动打开带看申请对话框
    if (newSelectedHouseId) {
      await loadSelectedHouseInfo(newSelectedHouseId)
      // 清除路由参数
      router.replace({ query: { ...route.query, selectedHouseId: undefined } })
      
      // 自动打开带看申请对话框
      nextTick(() => {
        viewingDialogVisible.value = true
      })
    }
  }
)
</script>

<style>
.message-page {
  --primary-start: #064e3b;
  --primary-end: #059669;
  --primary-soft: #d1fae5;
  --primary-bg: #ecfdf5;
  --text-primary: #153125;
  --text-secondary: #335243;
  --text-muted: #6b7f76;
  --border-soft: rgba(16, 85, 62, 0.08);
  --shadow-card: 0 18px 42px rgba(6, 78, 59, 0.12);
  --shadow-hover: 0 18px 36px rgba(6, 78, 59, 0.16);

  position: relative;
  display: grid;
  grid-template-columns: minmax(320px, 360px) minmax(0, 1fr);
  gap: 10px;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  max-height: 100vh;
  max-width: none;
  margin: 0;
  padding: 0;
  overflow: hidden;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top left, rgba(167, 243, 208, 0.6), transparent 26%),
    radial-gradient(circle at right 12% top 18%, rgba(16, 185, 129, 0.18), transparent 22%),
    linear-gradient(180deg, #f5fffb 0%, #edf8f2 44%, #f7fbf8 100%);
}

.message-page > .hero {
  display: none;
}

.page-glow {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(18px);
  opacity: 0.72;
}

.glow-left {
  top: 24px;
  left: -40px;
  width: 180px;
  height: 180px;
  background: rgba(110, 231, 183, 0.18);
}

.glow-right {
  right: -20px;
  bottom: 40px;
  width: 220px;
  height: 220px;
  background: rgba(52, 211, 153, 0.14);
}

.sidebar,
.thread-panel {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 30px;
  overflow: hidden;
  backdrop-filter: blur(14px);
  box-shadow: var(--shadow-card);
}

.sidebar {
  border-radius: 0 30px 30px 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(244, 252, 248, 0.92));
}

.thread-panel {
  border-radius: 30px 0 0 30px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(246, 252, 248, 0.94));
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  padding: 24px 24px 18px;
  border-bottom: 1px solid var(--border-soft);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.12));
}

.eyebrow {
  margin: 0 0 8px;
  color: #4f7c6a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.panel-head h2,
.panel-head h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 30px;
  line-height: 1.2;
}

.panel-head h3 {
  font-size: 24px;
}

.panel-head p,
.head-subtitle {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.65;
}

.ghost-button {
  border-color: rgba(5, 150, 105, 0.2);
  color: #0f766e;
  background: rgba(236, 253, 245, 0.8);
}

.sidebar-search {
  padding: 18px 18px 0;
}

.sidebar-search :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 16px;
  background: rgba(248, 255, 251, 0.9);
  box-shadow: inset 0 0 0 1px rgba(5, 150, 105, 0.12);
}

.conversation-list,
.message-list {
  flex: 1;
  overflow: auto;
}

.conversation-list {
  padding: 12px;
}

.conversation-item {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  gap: 14px;
  width: 100%;
  margin-bottom: 10px;
  padding: 14px;
  border: 0;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.42);
  text-align: left;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}

.conversation-item:hover {
  transform: translateY(-2px);
  background: rgba(240, 253, 244, 0.92);
  box-shadow: 0 14px 24px rgba(6, 78, 59, 0.08);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(209, 250, 229, 0.96), rgba(236, 253, 245, 0.98));
  box-shadow: var(--shadow-hover);
}

.avatar-box {
  position: relative;
}

.avatar-box :deep(.el-avatar) {
  background: linear-gradient(135deg, #0f766e, #34d399);
  color: #ffffff;
  font-weight: 700;
}

.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(239, 68, 68, 0.18);
}

.conversation-main {
  min-width: 0;
}

.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.line-top {
  margin-bottom: 6px;
}

.conversation-name {
  color: var(--text-primary);
  font-size: 16px;
}

.conversation-time {
  color: var(--text-muted);
  font-size: 12px;
  white-space: nowrap;
}

.line-bottom {
  color: var(--text-muted);
  font-size: 13px;
}

.conversation-role {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(5, 150, 105, 0.08);
  color: #0f766e;
}

.ellipsis {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.thread-header {
  align-items: center;
}

.thread-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thread-user :deep(.el-avatar) {
  background: linear-gradient(135deg, #0f766e, #34d399);
  color: #fff;
  font-weight: 700;
}

.thread-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.thread-tip {
  color: var(--text-muted);
  font-size: 13px;
}

.message-list {
  padding: 28px 24px;
  background:
    radial-gradient(circle at top right, rgba(16, 185, 129, 0.08), transparent 18%),
    linear-gradient(180deg, rgba(248, 255, 252, 0.9), rgba(241, 249, 244, 0.95));
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.message-row.self {
  justify-content: flex-end;
}

.message-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #6ee7b7, #10b981);
  color: #ffffff;
  font-weight: 700;
}

.self-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #0f766e, #10b981);
}

.message-body {
  max-width: min(92%, 1320px);
}

.chat-bubble .bubble-text {
  padding: 14px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
  box-shadow: 0 14px 28px rgba(20, 52, 39, 0.08);
}

.image-bubble {
  max-width: 320px;
}

.message-image {
  width: 100%;
  max-width: 320px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 14px 28px rgba(20, 52, 39, 0.12);
}

.message-page .conversation-item,
.message-page .chat-bubble .bubble-text,
.message-page .system-card,
.message-page .house-box,
.message-page .composer,
.message-page .panel-head,
.message-page .sidebar,
.message-page .thread-panel {
  box-sizing: border-box;
}

.message-row.self .chat-bubble .bubble-text {
  background: linear-gradient(135deg, #0f766e, #10b981);
  color: #ffffff;
}

.chat-bubble small {
  display: block;
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.message-row.self .chat-bubble small {
  text-align: right;
}

.system-card {
  padding: 18px;
  border: 1px solid rgba(16, 185, 129, 0.14);
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(248, 255, 252, 0.98), rgba(236, 253, 245, 0.96));
  box-shadow: 0 18px 30px rgba(6, 78, 59, 0.08);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.scene-tag {
  display: inline-block;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(5, 150, 105, 0.1);
  color: #047857;
  font-size: 12px;
  font-weight: 700;
}

.system-card h4 {
  margin: 10px 0 0;
  color: var(--text-primary);
  font-size: 20px;
}

.card-top small {
  color: var(--text-muted);
  white-space: nowrap;
}

.card-content {
  margin: 14px 0 0;
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

.house-showcase-card {
  display: grid;
  grid-template-columns: 168px minmax(0, 1fr);
  gap: 16px;
  margin-top: 16px;
  padding: 16px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 30px rgba(6, 78, 59, 0.08);
  border: 1px solid rgba(16, 185, 129, 0.08);
}

.house-showcase-card.clickable {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.house-showcase-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 34px rgba(6, 78, 59, 0.12);
}

.house-showcase-media {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
}

.house-showcase-media img {
  width: 100%;
  height: 132px;
  object-fit: cover;
  display: block;
}

.house-scene-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(6, 78, 59, 0.78);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.house-showcase-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.house-showcase-head,
.house-showcase-price,
.viewing-house-price {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.house-showcase-head strong,
.viewing-house-content strong {
  color: var(--text-primary);
  font-size: 18px;
  line-height: 1.4;
}

.house-price-pill {
  flex-shrink: 0;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 1);
  color: #059669;
  font-size: 12px;
  font-weight: 700;
}

.house-showcase-location,
.viewing-house-content p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.house-showcase-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.emoji-picker {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

/* 选中房源卡片样式 */
.house-select-placeholder {
  text-align: center;
  padding: 24px;
  background: var(--primary-bg);
  border: 2px dashed var(--primary-soft);
  border-radius: 12px;
}

.placeholder-hint {
  margin-top: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.selected-house-card {
  border: 1px solid var(--border-soft);
  border-radius: 12px;
  overflow: hidden;
}

.selected-house-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  background: var(--primary-bg);
  border-bottom: 1px solid var(--border-soft);
}

.selected-house-info h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--text-primary);
}

.house-location {
  margin: 0 0 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

.house-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.selected-house-price {
  text-align: right;
  flex-shrink: 0;
}

.price-amount {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.unit-price {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.selected-house-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.selected-house-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-house-actions {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid var(--border-soft);
  justify-content: flex-end;
}

.viewing-house-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.house-showcase-tags span,
.viewing-house-tags span {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 0.92);
  color: #0f766e;
  font-size: 12px;
  font-weight: 600;
}

.house-showcase-total,
.viewing-house-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  color: #f56c6c;
  font-size: 14px;
  font-weight: 700;
}

.price-number {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.price-unit,
.house-showcase-unit,
.viewing-house-unit {
  color: var(--text-muted);
  font-size: 13px;
}

.viewing-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}

.viewing-house-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 320px));
  gap: 18px;
  justify-content: flex-start;
}

.viewing-house-card {
  width: 100%;
  border: 1px solid rgba(16, 185, 129, 0.1);
  border-radius: 18px;
  padding: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  cursor: pointer;
  text-align: left;
  appearance: none;
  -webkit-appearance: none;
  box-shadow: 0 2px 12px rgba(6, 78, 59, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.viewing-house-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(6, 78, 59, 0.18);
}

.viewing-house-card.selected {
  border-color: rgba(5, 150, 105, 0.42);
  box-shadow: 0 12px 30px rgba(5, 150, 105, 0.24);
}

.viewing-house-image-wrapper {
  position: relative;
  overflow: hidden;
  background: #f3f6f5;
}

.viewing-house-image-wrapper img {
  width: 100%;
  height: 196px;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.viewing-house-card:hover .viewing-house-image-wrapper img {
  transform: scale(1.08);
}

.viewing-house-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 14px;
  background: linear-gradient(180deg, transparent 35%, rgba(15, 23, 42, 0.28));
  opacity: 0;
  transition: opacity 0.25s ease;
}

.viewing-house-card:hover .viewing-house-overlay,
.viewing-house-card.selected .viewing-house-overlay {
  opacity: 1;
}

.viewing-house-overlay-text {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #064e3b;
  font-size: 12px;
  font-weight: 700;
}

.viewing-house-badge {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(6, 78, 59, 0.78);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.viewing-house-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 16px 18px;
}

.viewing-house-title {
  color: var(--text-primary);
  font-size: 18px;
  line-height: 1.45;
}

.viewing-house-address {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.7;
}

.viewing-house-content p {
  margin: 0;
}

.status-box,
.card-actions,
.composer-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-box,
.card-actions {
  margin-top: 14px;
}

.status-box {
  color: var(--text-muted);
  flex-wrap: wrap;
}

.card-actions {
  flex-wrap: wrap;
}

.composer {
  padding: 18px 22px 20px;
  border-top: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(10px);
  position: relative;
}

.composer :deep(.el-textarea__inner) {
  min-height: 104px !important;
  border-radius: 18px;
  background: rgba(249, 255, 251, 0.96);
  box-shadow: inset 0 0 0 1px rgba(5, 150, 105, 0.12);
}

.hidden-file-input {
  display: none;
}

.pending-image-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid rgba(16, 185, 129, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 10px 22px rgba(6, 78, 59, 0.06);
}

.pending-image-preview {
  width: 78px;
  height: 78px;
  border-radius: 14px;
  object-fit: cover;
  flex-shrink: 0;
}

.pending-image-meta {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.pending-image-meta strong {
  color: var(--text-primary);
  font-size: 14px;
}

.pending-image-meta span {
  color: var(--text-muted);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-image-remove {
  color: #ef4444;
}

.emoji-picker {
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  border: 1px solid rgba(16, 185, 129, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 24px rgba(6, 78, 59, 0.08);
}

.emoji-item {
  border: 0;
  border-radius: 12px;
  background: rgba(236, 253, 245, 0.9);
  padding: 8px 0;
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.emoji-item:hover {
  transform: translateY(-2px);
  background: rgba(209, 250, 229, 1);
}

.composer-bar {
  justify-content: space-between;
  margin-top: 12px;
}

.composer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-action {
  min-width: 44px;
  padding: 0 12px;
}

.hint {
  color: var(--text-muted);
  font-size: 13px;
}

.thread-empty {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
}

@media (max-width: 1180px) {
  .message-page {
    grid-template-columns: 1fr;
    height: auto;
    padding: 20px;
    max-height: none;
  }

  .sidebar {
    border-radius: 30px;
    min-height: 320px;
  }

  .thread-panel {
    border-radius: 30px;
  }

  .message-body {
    max-width: 100%;
  }

  .viewing-house-grid {
    grid-template-columns: 1fr;
    justify-content: stretch;
  }
}

@media (max-width: 768px) {
  .message-page {
    gap: 16px;
    padding: 16px;
  }

  .panel-head,
  .thread-header,
  .composer-bar,
  .card-top,
  .thread-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-head h2 {
    font-size: 24px;
  }

  .conversation-item {
    grid-template-columns: 52px minmax(0, 1fr);
  }

  .house-showcase-card {
    grid-template-columns: 1fr;
  }

  .house-showcase-media img,
  .viewing-house-image img {
    width: 100%;
    height: 170px;
  }

  .pending-image-card {
    align-items: flex-start;
  }

  .emoji-picker {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}
</style>
