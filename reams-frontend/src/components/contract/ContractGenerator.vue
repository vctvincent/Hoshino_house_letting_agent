<template>
  <div class="contract-generator">
    <!-- 步骤指示器 -->
    <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px;">
      <el-step title="补充信息" />
      <el-step title="合同预览" />
      <el-step title="电子签名" />
      <el-step title="生成合同" />
    </el-steps>

    <!-- 步骤 1: 补充缺失信息 -->
    <div v-show="currentStep === 0" class="step-content">
      <el-alert
        title="💡 请补充以下信息以生成合同"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>系统已自动填充房源、交易、客户和中介信息。</p>
        <p><strong>必填项：</strong>客户身份证号、签订日期</p>
        <p><strong>可选项：</strong>产权证号、资格证号（如无则留空，合同中显示"待补充"）</p>
      </el-alert>

      <el-form :model="contractInfo" label-width="120px" :rules="rules" ref="formRef">
        <el-divider content-position="left">👤 客户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户姓名">
              <el-input v-model="contractInfo.customerName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户电话">
              <el-input v-model="contractInfo.customerPhone" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="客户身份证号" prop="customerIdCard">
          <el-input 
            v-model="contractInfo.customerIdCard" 
            placeholder="请输入客户身份证号码"
            maxlength="18"
          />
        </el-form-item>

        <el-divider content-position="left">🏠 房源信息</el-divider>
        <el-form-item label="房屋地址">
          <el-input v-model="contractInfo.houseAddress" disabled />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产权证号">
              <el-input v-model="contractInfo.propertyCertificateNo" placeholder="如有请填写，否则留空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建筑面积">
              <el-input v-model="contractInfo.houseArea" disabled>
                <template #append>平方米</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户型">
              <el-input v-model="contractInfo.houseLayout" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼层">
              <el-input v-model="contractInfo.houseFloor" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">💰 交易信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成交价格">
              <el-input v-model="contractInfo.finalPrice" disabled>
                <template #append>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定金">
              <el-input v-model="contractInfo.deposit" disabled>
                <template #append>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="首付款">
              <el-input v-model="contractInfo.downPayment" disabled>
                <template #append>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="贷款金额">
              <el-input v-model="contractInfo.loanAmount" disabled>
                <template #append>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">🏢 中介信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="中介姓名">
              <el-input v-model="contractInfo.agentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中介电话">
              <el-input v-model="contractInfo.agentPhone" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资格证号" prop="agentLicenseNo">
              <el-input v-model="contractInfo.agentLicenseNo" placeholder="如有请填写，否则留空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中介公司">
              <el-input v-model="contractInfo.agentCompany" placeholder="请输入中介公司名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">📅 其他信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="签订日期" prop="signDate">
              <el-date-picker
                v-model="contractInfo.signDate"
                type="date"
                placeholder="选择签订日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="居间费率">
              <el-input v-model="contractInfo.serviceFeeRate" placeholder="如：1" />
              <template #append>%</template>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="step-actions">
        <el-button @click="$emit('close')">取消</el-button>
        <el-button type="primary" @click="nextStep">下一步：预览合同</el-button>
      </div>
    </div>

    <!-- 步骤 2: 合同预览 -->
    <div v-show="currentStep === 1" class="step-content">
      <el-alert
        title="📄 合同预览"
        type="success"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>请仔细查看生成的合同内容，确认无误后进入签名环节。</p>
      </el-alert>

      <div class="contract-preview" ref="contractPreviewRef">
        <!-- 这里将渲染完整的合同HTML -->
        <div v-html="contractHtml"></div>
      </div>

      <div class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="nextStep">下一步：电子签名</el-button>
      </div>
    </div>

    <!-- 步骤 3: 电子签名 -->
    <div v-show="currentStep === 2" class="step-content">
      <el-alert
        title="✍️ 电子签名"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>请双方在下方的签名板上手写签名。签名将嵌入到合同中。</p>
      </el-alert>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="signature-box">
            <h4>👤 客户签名（乙方）</h4>
            <canvas ref="customerSignatureCanvas" class="signature-canvas"></canvas>
            <div class="signature-actions">
              <el-button size="small" @click="clearCustomerSignature">清除</el-button>
              <el-button size="small" type="primary" @click="saveCustomerSignature" :disabled="!customerSigned">
                确认签名
              </el-button>
            </div>
            <p v-if="customerSigned" class="signed-hint">✅ 已签名</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="signature-box">
            <h4>🏢 中介签名（丙方）</h4>
            <canvas ref="agentSignatureCanvas" class="signature-canvas"></canvas>
            <div class="signature-actions">
              <el-button size="small" @click="clearAgentSignature">清除</el-button>
              <el-button size="small" type="primary" @click="saveAgentSignature" :disabled="!agentSigned">
                确认签名
              </el-button>
            </div>
            <p v-if="agentSigned" class="signed-hint">✅ 已签名</p>
          </div>
        </el-col>
      </el-row>

      <div class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="nextStep" :disabled="!customerSigned || !agentSigned">
          下一步：生成PDF
        </el-button>
      </div>
    </div>

    <!-- 步骤 4: 生成 PDF -->
    <div v-show="currentStep === 3" class="step-content">
      <el-alert
        title="📥 生成合同"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>正在生成 PDF 合同文件，请稍候...</p>
      </el-alert>

      <div v-loading="generating" class="generating-status">
        <el-progress 
          v-if="progress > 0"
          :percentage="progress" 
          :status="progress === 100 ? 'success' : ''"
        />
        <p style="margin-top: 15px; text-align: center; color: #606266;">
          {{ generatingText }}
        </p>
      </div>

      <div class="step-actions">
        <el-button @click="$emit('close')">关闭</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import html2pdf from 'html2pdf.js'
import SignaturePad from 'signature_pad'
import request from '@/api'
import store from '@/store'

// Props
const props = defineProps({
  transaction: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close', 'success'])

// 步骤控制
const currentStep = ref(0)
const formRef = ref(null)
const contractPreviewRef = ref(null)

// 合同信息
const contractInfo = ref({
  customerIdCard: '',
  customerName: props.transaction.customerName || '',
  customerPhone: props.transaction.customerPhone || '',
  houseAddress: props.transaction.houseAddress || '',
  propertyCertificateNo: '',
  houseArea: props.transaction.houseArea || '',
  houseLayout: props.transaction.houseLayout || '',
  houseFloor: props.transaction.houseFloor || '',
  finalPrice: props.transaction.finalPrice || 0,
  deposit: props.transaction.deposit || 0,
  downPayment: props.transaction.downPayment || 0,
  loanAmount: props.transaction.loanAmount || 0,
  agentName: props.transaction.agentName || '',
  agentPhone: props.transaction.agentPhone || '',
  agentLicenseNo: '',
  agentCompany: '',
  signDate: new Date().toISOString().split('T')[0],
  serviceFeeRate: 1
})

// 表单验证规则
const rules = {
  customerIdCard: [
    { required: true, message: '请输入客户身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  signDate: [
    { required: true, message: '请选择签订日期', trigger: 'change' }
  ]
}

// 签名相关
const customerSignatureCanvas = ref(null)
const agentSignatureCanvas = ref(null)
let customerPad = null
let agentPad = null
const customerSigned = ref(false)
const agentSigned = ref(false)
const customerSignatureData = ref('')
const agentSignatureData = ref('')

// 生成进度
const generating = ref(false)
const progress = ref(0)
const generatingText = ref('')

// 合同 HTML
const contractHtml = computed(() => {
  const info = contractInfo.value
  const finalPriceYuan = info.finalPrice * 10000
  const finalPriceText = numberToChinese(finalPriceYuan)
  const contractNo = `REAMS-HT-${new Date().getFullYear()}-${String(Date.now()).slice(-5)}`
  
  // 居间服务费计算
  const serviceFee = (info.finalPrice * info.serviceFeeRate / 100).toFixed(2)

  return `
    <div class="contract-page">
      <div class="watermark">REAMS 合同专用</div>
      
      <!-- 合同头部 -->
      <div class="contract-header">
        <div class="company-name">REAMS 房屋中介管理平台</div>
        <div class="contract-title">房屋买卖居间合同</div>
        <div class="contract-no">合同编号：<span>${contractNo}</span></div>
      </div>

      <!-- 三方基本信息 -->
      <table class="info-table">
        <caption>第一条 合同当事人</caption>
        <tr>
          <td class="label">甲方（卖方）</td>
          <td class="value" colspan="3">待补充（系统暂不支持卖方信息自动填充）</td>
        </tr>
        <tr>
          <td class="label">乙方（买方）</td>
          <td class="value">姓名：<span class="fill">${info.customerName}</span></td>
          <td class="value">身份证号：<span class="fill">${info.customerIdCard}</span></td>
        </tr>
        <tr>
          <td class="label">联系电话</td>
          <td class="value" colspan="2"><span class="fill">${info.customerPhone}</span></td>
        </tr>
        <tr>
          <td class="label">丙方（居间方）</td>
          <td class="value">公司：<span class="fill">${info.agentCompany || '待补充'}</span></td>
          <td class="value">经纪人：<span class="fill">${info.agentName || '待补充'}</span></td>
        </tr>
        <tr>
          <td class="label">营业执照号</td>
          <td class="value"><span class="fill">待补充</span></td>
          <td class="value">联系电话：<span class="fill">${info.agentPhone || '待补充'}</span></td>
        </tr>
      </table>

      <!-- 房屋基本信息 -->
      <table class="info-table">
        <caption>第二条 房屋基本情况</caption>
        <tr>
          <td class="label">房屋坐落</td>
          <td class="value" colspan="3">${info.houseAddress}</td>
        </tr>
        <tr>
          <td class="label">产权证号</td>
          <td class="value" colspan="3">${info.propertyCertificateNo || '待补充'}</td>
        </tr>
        <tr>
          <td class="label">建筑面积</td>
          <td class="value">${info.houseArea} 平方米</td>
          <td class="label">户型</td>
          <td class="value">${info.houseLayout}</td>
        </tr>
        <tr>
          <td class="label">楼层</td>
          <td class="value">${info.houseFloor}</td>
          <td class="label">成交总价</td>
          <td class="value" style="color: #c00; font-weight: bold;">¥${finalPriceYuan.toFixed(2)}（${finalPriceText}）</td>
        </tr>
      </table>

      <!-- 成交价格及付款方式 -->
      <div class="clause-section">
        <div class="clause-title">第三条 成交价格及付款方式</div>
        <div class="clause">
          <p><span class="clause-num">3.1</span> 经甲乙双方协商一致，该房屋成交总价为人民币<span class="highlight">${finalPriceText}（¥${finalPriceYuan.toFixed(2)}）</span>。</p>
          <p><span class="clause-num">3.2</span> 付款方式如下：</p>
        </div>
      </div>

      <table class="amount-table">
        <thead>
          <tr>
            <th>款项名称</th>
            <th>金额（万元）</th>
            <th>支付时间</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>定金</td>
            <td class="money">${info.deposit}</td>
            <td>签订本合同当日</td>
            <td>由丙方代收代管</td>
          </tr>
          <tr>
            <td>首付款</td>
            <td class="money">${info.downPayment || '-'}</td>
            <td>网签后5个工作日内</td>
            <td>-</td>
          </tr>
          <tr>
            <td>贷款金额</td>
            <td class="money">${info.loanAmount || '-'}</td>
            <td>银行放款后3日内</td>
            <td>-</td>
          </tr>
          <tr>
            <th colspan="1">合计</th>
            <td class="money" colspan="3">¥${finalPriceYuan.toFixed(2)}</td>
          </tr>
        </tbody>
      </table>

      <!-- 居间服务费 -->
      <div class="clause-section">
        <div class="clause-title">第四条 居间服务费</div>
        <div class="clause">
          <p><span class="clause-num">4.1</span> 乙方向丙方支付居间服务费：成交价的 <span class="highlight">${info.serviceFeeRate}%</span>，即人民币 <span class="highlight">${serviceFee}</span> 万元，于签订本合同当日支付。</p>
          <p><span class="clause-num">4.2</span> 居间服务费一经支付，非因丙方原因导致交易未完成的，居间服务费不予退还。</p>
        </div>
      </div>

      <!-- 房屋交付、过户、违约责任等条款 -->
      <div class="clause-section">
        <div class="clause-title">第五条 房屋交付</div>
        <div class="clause">
          <p><span class="clause-num">5.1</span> 甲方应在收到全部房款后 <span class="highlight">7</span> 个工作日内将房屋交付给乙方，并配合办理水、电、燃气、物业管理等过户手续。</p>
          <p><span class="clause-num">5.2</span> 交付时甲方应保证房屋及附属设施完好。</p>
        </div>
      </div>

      <div class="clause-section">
        <div class="clause-title">第六条 产权过户</div>
        <div class="clause">
          <p><span class="clause-num">6.1</span> 甲乙双方应在首付款支付后 <span class="highlight">10</span> 个工作日内共同前往不动产登记中心办理产权过户手续。</p>
          <p><span class="clause-num">6.2</span> 过户所需税费由甲乙双方按国家及地方规定各自承担。</p>
        </div>
      </div>

      <div class="clause-section">
        <div class="clause-title">第七条 违约责任</div>
        <div class="clause">
          <p><span class="clause-num">7.1</span> 甲方违约：拒绝出售房屋或擅自提高售价的，应向乙方双倍返还定金。</p>
          <p><span class="clause-num">7.2</span> 乙方违约：拒绝购买房屋或无正当理由拖延付款超过 <span class="highlight">30</span> 天的，甲方有权解除合同，定金不予退还。</p>
          <p><span class="clause-num">7.3</span> 丙方责任：因丙方故意隐瞒重要事实或提供虚假信息造成损失的，丙方应退还居间服务费并承担相应赔偿责任。</p>
        </div>
      </div>

      <div class="clause-section">
        <div class="clause-title">第八条 争议解决</div>
        <div class="clause">
          <p><span class="clause-num">8.1</span> 本合同在履行过程中发生的争议，由各方协商解决。</p>
          <p><span class="clause-num">8.2</span> 协商不成的，任何一方可向房屋所在地人民法院提起诉讼。</p>
        </div>
      </div>

      <!-- 签字区 -->
      <div class="sign-section">
        <div class="sign-row">
          <div class="sign-block">
            <div class="sign-title">甲方（卖方）签章</div>
            <div class="sign-line">签名/盖章：<span class="blank"></span></div>
            <div class="sign-line">身份证号：<span class="blank"></span></div>
            <div class="sign-line">签订日期：<span class="blank">${info.signDate} 年  月  日</span></div>
          </div>
          <div class="sign-block">
            <div class="sign-title">乙方（买方）签章</div>
            <div class="sign-line">签名/盖章：<span class="blank">${info.customerName}</span></div>
            <div class="sign-line">身份证号：<span class="blank">${info.customerIdCard}</span></div>
            <div class="sign-line">签订日期：<span class="blank">${info.signDate}</span></div>
            ${customerSignatureData.value ? `<div class="signature-image"><img src="${customerSignatureData.value}" alt="客户签名" /></div>` : ''}
          </div>
        </div>

        <div class="sign-row" style="justify-content: center;">
          <div class="sign-block" style="width: 60%;">
            <div class="sign-title">丙方（居间方）签章</div>
            <div class="sign-line">公司盖章：<span class="blank">${info.agentCompany || 'REAMS平台'}</span></div>
            <div class="sign-line">经纪人签名：<span class="blank">${info.agentName}</span></div>
            <div class="sign-line">资格证号：<span class="blank">${info.agentLicenseNo || '待补充'}</span></div>
            <div class="sign-line">签订日期：<span class="blank">${info.signDate}</span></div>
            ${agentSignatureData.value ? `<div class="signature-image"><img src="${agentSignatureData.value}" alt="中介签名" /></div>` : ''}
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <div style="text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; font-size: 15px; color: #999;">
        REAMS 房屋中介管理平台 · 合同编号 ${contractNo}
      </div>
    </div>
  `
})

// 数字转中文大写
function numberToChinese(num) {
  const fraction = ['角', '分']
  const digit = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const unit = [['元', '万', '亿'], ['', '拾', '佰', '仟']]
  const head = num < 0 ? '负' : ''
  num = Math.abs(num)
  let s = ''
  for (let i = 0; i < fraction.length; i++) {
    s += (digit[Math.floor(num * 10 * Math.pow(10, i)) % 10] + fraction[i]).replace(/零./, '')
  }
  s = s || '整'
  num = Math.floor(num)
  for (let i = 0; i < unit[0].length && num > 0; i++) {
    let p = ''
    for (let j = 0; j < unit[1].length && num > 0; j++) {
      p = digit[num % 10] + unit[1][j] + p
      num = Math.floor(num / 10)
    }
    s = p.replace(/(零.)*零$/, '').replace(/^$/, '零') + unit[0][i] + s
  }
  return head + s.replace(/(零.)*零元/, '元').replace(/(零.)+/g, '零').replace(/^整$/, '零元整')
}

// 步骤导航
function nextStep() {
  if (currentStep.value === 0) {
    // 验证表单
    formRef.value.validate((valid) => {
      if (valid) {
        currentStep.value++
      } else {
        ElMessage.warning('请填写所有必填项')
      }
    })
  } else if (currentStep.value === 2) {
    // 生成 PDF
    generatePDF()
    currentStep.value++
  } else {
    currentStep.value++
  }
}

function prevStep() {
  currentStep.value--
}

// 初始化签名板
onMounted(async () => {
  await nextTick()
  initSignaturePads()
})

function initSignaturePads() {
  if (customerSignatureCanvas.value) {
    customerPad = new SignaturePad(customerSignatureCanvas.value, {
      backgroundColor: 'rgba(255, 255, 255, 0)',
      penColor: 'rgb(0, 0, 0)'
    })
    
    customerPad.addEventListener('endStroke', () => {
      customerSigned.value = !customerPad.isEmpty()
    })
  }

  if (agentSignatureCanvas.value) {
    agentPad = new SignaturePad(agentSignatureCanvas.value, {
      backgroundColor: 'rgba(255, 255, 255, 0)',
      penColor: 'rgb(0, 0, 0)'
    })
    
    agentPad.addEventListener('endStroke', () => {
      agentSigned.value = !agentPad.isEmpty()
    })
  }
}

function clearCustomerSignature() {
  if (customerPad) {
    customerPad.clear()
    customerSigned.value = false
    customerSignatureData.value = ''
  }
}

function saveCustomerSignature() {
  if (customerPad && !customerPad.isEmpty()) {
    customerSignatureData.value = customerPad.toDataURL('image/png')
    ElMessage.success('客户签名已保存')
  }
}

function clearAgentSignature() {
  if (agentPad) {
    agentPad.clear()
    agentSigned.value = false
    agentSignatureData.value = ''
  }
}

function saveAgentSignature() {
  if (agentPad && !agentPad.isEmpty()) {
    agentSignatureData.value = agentPad.toDataURL('image/png')
    ElMessage.success('中介签名已保存')
  }
}

// 生成 PDF
async function generatePDF() {
  generating.value = true
  progress.value = 0
  generatingText.value = '正在准备合同内容...'

  try {
    await nextTick()
    
    progress.value = 20
    generatingText.value = '正在生成 PDF...'

    const element = contractPreviewRef.value
    const opt = {
      margin: 10,
      filename: `合同_${props.transaction.transactionNo}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    }

    const pdfBlob = await html2pdf().set(opt).from(element).outputPdf('blob')
    
    progress.value = 60
    generatingText.value = '正在上传合同文件...'

    // 上传 PDF
    const formData = new FormData()
    formData.append('file', pdfBlob, `合同_${props.transaction.transactionNo}.pdf`)
    formData.append('type', 'contract')

    const uploadRes = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (uploadRes.code === 200) {
      progress.value = 80
      generatingText.value = '正在保存合同信息...'

      // 更新交易记录
      const updateRes = await request.put('/transaction/update', {
        id: props.transaction.id,
        contractUrl: uploadRes.data.url,
        status: 2
      })

      if (updateRes.code === 200) {
        progress.value = 100
        generatingText.value = '合同生成成功！'
        ElMessage.success('合同已生成并保存')
        emit('success', uploadRes.data.url)
      } else {
        ElMessage.error('保存合同信息失败')
      }
    } else {
      ElMessage.error('上传合同文件失败')
    }
  } catch (error) {
    console.error('生成合同失败:', error)
    ElMessage.error('生成合同失败：' + error.message)
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.contract-generator {
  padding: 20px;
}

.step-content {
  max-height: 70vh;
  overflow-y: auto;
}

.contract-preview {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  background: #fff;
  max-height: 60vh;
  overflow-y: auto;
}

/* 合同样式 */
.contract-page {
  font-family: "SimSun", "Songti SC", serif;
  color: #1a1a1a;
  line-height: 1.8;
  position: relative;
}

.watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-30deg);
  font-size: 80px;
  color: rgba(0,0,0,0.03);
  font-weight: bold;
  letter-spacing: 20px;
  pointer-events: none;
  white-space: nowrap;
  z-index: 0;
}

.contract-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 3px double #c00;
}

.company-name {
  font-size: 16px;
  color: #666;
  letter-spacing: 4px;
  margin-bottom: 8px;
}

.contract-title {
  font-size: 26px;
  font-weight: bold;
  color: #c00;
  letter-spacing: 6px;
  margin: 10px 0;
}

.contract-no {
  font-size: 15px;
  color: #999;
  margin-top: 8px;
}

.contract-no span {
  color: #333;
  font-weight: bold;
}

.info-table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  font-size: 16px;
}

.info-table caption {
  text-align: left;
  font-size: 16px;
  font-weight: bold;
  padding: 10px 0;
  color: #c00;
  border-bottom: 1px solid #ddd;
  margin-bottom: -1px;
}

.info-table td {
  padding: 8px 12px;
  border: 1px solid #ddd;
  vertical-align: middle;
}

.info-table .label {
  width: 120px;
  background: #fafafa;
  font-weight: bold;
  color: #555;
  white-space: nowrap;
}

.info-table .fill {
  color: #c00;
  font-weight: bold;
}

.clause-section {
  margin: 25px 0;
}

.clause-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 4px solid #c00;
}

.clause {
  margin: 12px 0;
  padding-left: 20px;
}

.clause-num {
  font-weight: bold;
  color: #c00;
}

.clause p {
  text-indent: 2em;
  margin: 6px 0;
  font-size: 16px;
}

.highlight {
  color: #c00;
  font-weight: bold;
}

.amount-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
  font-size: 16px;
}

.amount-table th {
  background: #f8f8f8;
  padding: 10px;
  border: 1px solid #ddd;
  font-weight: bold;
  color: #555;
}

.amount-table td {
  padding: 10px;
  border: 1px solid #ddd;
  text-align: center;
}

.amount-table .money {
  color: #c00;
  font-weight: bold;
  font-size: 16px;
}

.sign-section {
  margin-top: 50px;
}

.sign-row {
  display: flex;
  justify-content: space-between;
  margin: 30px 0;
}

.sign-block {
  width: 45%;
}

.sign-title {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 20px;
  color: #333;
}

.sign-line {
  margin: 12px 0;
  font-size: 16px;
  color: #666;
}

.sign-line .blank {
  display: inline-block;
  width: 150px;
  border-bottom: 1px solid #999;
  margin-left: 8px;
}

.signature-image {
  margin-top: 10px;
  text-align: center;
}

.signature-image img {
  max-width: 200px;
  max-height: 80px;
  border: 1px solid #ddd;
  padding: 5px;
}

/* 签名板样式 */
.signature-box {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.signature-box h4 {
  margin: 0 0 15px 0;
  color: #303133;
  text-align: center;
}

.signature-canvas {
  width: 100%;
  height: 200px;
  border: 2px solid #409eff;
  border-radius: 4px;
  background: #fff;
  cursor: crosshair;
}

.signature-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.signed-hint {
  text-align: center;
  color: #67c23a;
  font-weight: bold;
  margin-top: 10px;
}

/* 步骤操作按钮 */
.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.generating-status {
  padding: 40px;
  text-align: center;
}
</style>