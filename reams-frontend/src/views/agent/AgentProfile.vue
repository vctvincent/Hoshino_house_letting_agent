<template>
  <div class="agent-profile-page">
    <el-skeleton v-if="loading" animated :rows="10" class="profile-skeleton" />

    <template v-else-if="agent">
      <section class="profile-hero">
        <div class="profile-head">
          <el-button @click="router.back()">返回</el-button>
        </div>

        <div class="profile-grid">
          <div class="agent-main-card">
            <div class="avatar-ring">
              <div class="agent-avatar">{{ (agent.name || '中')[0] }}</div>
            </div>

            <div class="agent-main-copy">
              <p class="eyebrow">AGENT PROFILE</p>

              <div class="title-row">
                <div class="title-copy">
                  <h1>{{ agent.name || '未命名中介' }}</h1>
                  <p class="agent-company">{{ agent.company || 'REAMS 房产顾问' }}</p>
                </div>

                <div class="inline-rating">
                  <div class="rating-chip">
                    <span>服务评分</span>
                    <strong>{{ displayRating }}</strong>
                  </div>
                  <el-rate :model-value="agentRating" disabled allow-half />
                </div>
              </div>

              <p class="agent-intro">{{ agent.introduction || '这位顾问暂未填写个人介绍。' }}</p>

              <div class="agent-contact-grid">
                <div class="contact-chip">
                  <span>手机号</span>
                  <strong>{{ agent.phone || '暂无' }}</strong>
                </div>
                <div class="contact-chip">
                  <span>邮箱</span>
                  <strong>{{ agent.email || '暂无' }}</strong>
                </div>
                <div class="contact-chip">
                  <span>从业年限</span>
                  <strong>{{ agent.workYear || 0 }} 年</strong>
                </div>
                <div class="contact-chip">
                  <span>擅长区域</span>
                  <strong>{{ agent.specialty || '暂未填写' }}</strong>
                </div>
              </div>
            </div>
          </div>

          <div class="stats-panel">
            <div class="stat-card stat-viewing">
              <p>带看数量</p>
              <strong>{{ viewingCount }}</strong>
              <span>累计已记录带看</span>
            </div>

            <div class="stat-card stat-review">
              <p>服务评价</p>
              <strong>{{ reviewCount }}</strong>
              <span>完成带看后的用户反馈</span>
            </div>

            <div class="stat-card stat-deal">
              <p>成交数量</p>
              <strong>{{ agent.dealCount || 0 }}</strong>
              <span>当前展示的历史成交</span>
            </div>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <article class="panel-card reviews-card">
          <div class="section-head reviews-head">
            <div>
              <p class="section-kicker">Service Reviews</p>
              <h2>带看服务评价</h2>
            </div>
            <div class="review-badge">{{ reviewCount }} 条</div>
          </div>

          <div v-if="reviews.length" class="review-list">
            <article v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-item-head">
                <div class="review-user">
                  <strong>{{ review.customerName || '匿名用户' }}</strong>
                  <p>{{ formatTime(review.createTime) }}</p>
                </div>
                <div class="review-score">
                  <el-rate :model-value="Number(review.rating || 0)" disabled allow-half />
                  <span>{{ ratingText(review.rating) }}</span>
                </div>
              </div>

              <p class="review-content">{{ review.content || '该用户没有留下更多文字。' }}</p>

              <div v-if="review.imageList.length" class="review-images">
                <el-image
                  v-for="image in review.imageList"
                  :key="image"
                  class="review-image"
                  :src="image"
                  :preview-src-list="review.imageList"
                  fit="cover"
                  preview-teleported
                />
              </div>
            </article>
          </div>
          <el-empty v-else description="暂时还没有带看服务评价" :image-size="92" />
        </article>
      </section>
    </template>

    <el-empty v-else description="未找到该中介信息" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api'
import { formatImageUrl } from '@/utils/imageUtils'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const agent = ref(null)
const reviews = ref([])
const viewingCount = ref(0)
const reviewCount = ref(0)
const agentRating = ref(0)

const displayRating = computed(() => Number(agentRating.value || 0).toFixed(1))

function parseReviewImages(images) {
  if (!images) return []
  try {
    const parsed = typeof images === 'string' ? JSON.parse(images) : images
    return Array.isArray(parsed) ? parsed.map(item => formatImageUrl(item)).filter(Boolean) : []
  } catch {
    return []
  }
}

function ratingText(score) {
  if (score >= 5) return '非常满意'
  if (score >= 4) return '满意'
  if (score >= 3) return '一般'
  if (score >= 2) return '不太满意'
  return '待改进'
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function calculateAverage(list) {
  if (!list.length) return 0
  const total = list.reduce((sum, item) => sum + Number(item.rating || 0), 0)
  return Math.round((total / list.length) * 10) / 10
}

async function loadReviews() {
  if (!agent.value?.id) {
    reviews.value = []
    reviewCount.value = 0
    agentRating.value = 0
    return
  }

  try {
    const [listRes, ratingRes] = await Promise.all([
      request.get('/review/list', {
        params: {
          agentId: agent.value.id,
          targetType: 2,
          pageNum: 1,
          pageSize: 10
        }
      }),
      request.get(`/review/agent/${agent.value.id}/rating`)
    ])

    reviews.value = (listRes.data?.list || []).map(item => ({
      ...item,
      imageList: parseReviewImages(item.images)
    }))
    reviewCount.value = listRes.data?.total || reviews.value.length

    const ratingValue = Number(ratingRes.data)
    agentRating.value = Number.isFinite(ratingValue) ? ratingValue : calculateAverage(reviews.value)
  } catch (error) {
    console.error('加载服务评价失败:', error)
    reviews.value = []
    reviewCount.value = 0
    agentRating.value = 0
  }
}

async function loadAgentInfo() {
  loading.value = true
  try {
    const agentId = route.params.id
    const res = await request.get(`/user/agent/${agentId}`)
    if (res.code === 200 && res.data) {
      agent.value = res.data

      const viewingRes = await request.get('/viewing/list', {
        params: {
          agentId,
          pageNum: 1,
          pageSize: 1
        }
      })
      viewingCount.value = viewingRes.data?.total || 0

      await loadReviews()
    }
  } catch (error) {
    console.error('加载中介信息失败:', error)
    ElMessage.error('加载中介信息失败')
    agent.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAgentInfo()
})
</script>

<style scoped>
.agent-profile-page {
  max-width: 1260px;
  margin: 0 auto;
  padding: 28px 24px 40px;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.12), transparent 24%),
    linear-gradient(180deg, #f5fbf7 0%, #eef7f1 100%);
}

.profile-skeleton,
.profile-hero,
.panel-card {
  padding: 24px;
  border-radius: 28px;
  border: 1px solid rgba(5, 150, 105, 0.12);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 40px rgba(6, 78, 59, 0.08);
}

.profile-head,
.section-head,
.review-item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.profile-head {
  margin-bottom: 20px;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}

.agent-main-card {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 20px;
  padding: 24px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.92), rgba(255, 255, 255, 0.98));
}

.avatar-ring {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 140px;
  height: 140px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(5, 150, 105, 0.24));
}

.agent-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 104px;
  height: 104px;
  border-radius: 999px;
  background: linear-gradient(135deg, #10b981, #0f766e);
  color: #fff;
  font-size: 42px;
  font-weight: 800;
}

.eyebrow,
.section-kicker {
  margin: 0 0 10px;
  color: #059669;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.title-copy {
  min-width: 0;
}

.agent-main-copy h1,
.section-head h2 {
  margin: 0;
  color: #0f172a;
}

.agent-company {
  margin: 10px 0 0;
  color: #0f766e;
  font-weight: 700;
}

.inline-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
  align-self: center;
}

.rating-chip {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  color: #065f46;
}

.rating-chip span {
  font-size: 12px;
  font-weight: 700;
}

.rating-chip strong {
  font-size: 24px;
  line-height: 1;
}

.agent-intro,
.review-user p,
.review-content {
  color: #64748b;
  line-height: 1.8;
}

.agent-intro {
  margin: 14px 0 0;
}

.agent-contact-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.main-card-actions {
  margin-top: 18px;
}

.main-card-actions .el-button {
  min-height: 48px;
  border-radius: 16px;
  font-weight: 700;
  padding: 0 24px;
}

.contact-chip,
.stat-card {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(5, 150, 105, 0.1);
}

.contact-chip span,
.stat-card p {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.contact-chip strong,
.stat-card strong {
  color: #0f172a;
}

.stats-panel {
  display: grid;
  gap: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
}

.stat-viewing {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.stat-review {
  background: linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%);
}

.stat-deal {
  background: linear-gradient(135deg, #fffaf0 0%, #fef3c7 100%);
}

.stat-card strong {
  font-size: 28px;
}

.stat-card span {
  color: #64748b;
  line-height: 1.6;
}

.content-grid {
  display: grid;
  gap: 20px;
  margin-top: 20px;
}

.review-badge {
  flex-shrink: 0;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(236, 253, 245, 0.9);
  color: #047857;
  font-size: 12px;
  font-weight: 700;
}

.review-list {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.review-item {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.94));
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.review-user strong {
  color: #0f172a;
}

.review-user p {
  margin: 8px 0 0;
  font-size: 12px;
}

.review-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-score span {
  color: #64748b;
  font-size: 12px;
}

.review-content {
  margin: 14px 0 0;
  white-space: pre-wrap;
}

.review-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(110px, 140px));
  gap: 10px;
  margin-top: 14px;
}

.review-image {
  width: 100%;
  height: 110px;
  border-radius: 16px;
  overflow: hidden;
}

@media (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .agent-profile-page {
    padding: 16px 12px 28px;
  }

  .section-head,
  .review-item-head,
  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .inline-rating {
    align-items: flex-start;
  }

  .agent-main-card {
    grid-template-columns: 1fr;
  }

  .avatar-ring {
    width: 120px;
    height: 120px;
  }

  .agent-contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
