const PRICE_ANALYSIS_PALETTE = ['#059669', '#10b981', '#0f766e', '#f59e0b', '#f97316', '#0d9488']

export function normalizeAnalysisCityName(value) {
  const text = String(value || '').trim()
  if (!text) return ''
  if (text.endsWith('市') && text.length > 1) {
    return text.slice(0, -1)
  }
  return text
}

function toNumber(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num : 0
}

function buildSegments(mode) {
  if (mode === 'unitPrice') {
    return [
      { range: '1万以下', min: 0, max: 10000 },
      { range: '1-2万', min: 10000, max: 20000 },
      { range: '2-3万', min: 20000, max: 30000 },
      { range: '3-5万', min: 30000, max: 50000 },
      { range: '5-8万', min: 50000, max: 80000 },
      { range: '8万以上', min: 80000, max: Infinity }
    ]
  }

  if (mode === 'area') {
    return [
      { range: '50㎡以下', min: 0, max: 50 },
      { range: '50-70㎡', min: 50, max: 70 },
      { range: '70-90㎡', min: 70, max: 90 },
      { range: '90-120㎡', min: 90, max: 120 },
      { range: '120-150㎡', min: 120, max: 150 },
      { range: '150㎡以上', min: 150, max: Infinity }
    ]
  }

  return [
    { range: '100万以下', min: 0, max: 100 },
    { range: '100-200万', min: 100, max: 200 },
    { range: '200-300万', min: 200, max: 300 },
    { range: '300-500万', min: 300, max: 500 },
    { range: '500-800万', min: 500, max: 800 },
    { range: '800万以上', min: 800, max: Infinity }
  ]
}

function resolveSegmentBaseValue(row, mode) {
  const averagePrice = toNumber(row.averagePrice)
  const averageUnitPrice = toNumber(row.averageUnitPrice)
  const estimatedArea = averagePrice > 0 && averageUnitPrice > 0
    ? (averagePrice * 10000) / averageUnitPrice
    : 0

  if (mode === 'unitPrice') return averageUnitPrice
  if (mode === 'area') return estimatedArea
  return averagePrice
}

function isMatchingCity(row, selectedCity) {
  if (!selectedCity) return true
  return normalizeAnalysisCityName(row.city) === selectedCity
}

function sumSegmentMetric(rows, segment, mode, metric) {
  return rows.reduce((sum, row) => {
    const baseValue = resolveSegmentBaseValue(row, mode)
    if (baseValue >= segment.min && baseValue < segment.max) {
      return sum + toNumber(row[metric])
    }
    return sum
  }, 0)
}

export function buildPriceAnalysisData({
  houseDistributionRows = [],
  districtSalesRows = [],
  selectedCity = '',
  mode = 'totalPrice'
} = {}) {
  const normalizedCity = normalizeAnalysisCityName(selectedCity)
  const inventoryRows = houseDistributionRows.filter(row => isMatchingCity(row, normalizedCity))
  const dealRows = districtSalesRows.filter(row => isMatchingCity(row, normalizedCity))

  if (!inventoryRows.length && !dealRows.length) {
    return []
  }

  return buildSegments(mode).map((segment, index) => {
    const inventory = sumSegmentMetric(inventoryRows, segment, mode, 'houseCount')
    const deals = sumSegmentMetric(dealRows, segment, mode, 'transactionCount')

    return {
      ...segment,
      inventory,
      deals,
      conversionRate: inventory > 0 ? ((deals / inventory) * 100).toFixed(1) : '0.0',
      trend: (deals - inventory * 0.12).toFixed(1),
      color: PRICE_ANALYSIS_PALETTE[index % PRICE_ANALYSIS_PALETTE.length]
    }
  })
}

export function buildPriceAnalysisSummary(priceAnalysisData = [], options = {}) {
  const totalInventory = priceAnalysisData.reduce((sum, item) => sum + toNumber(item.inventory), 0)
  const totalDeals = priceAnalysisData.reduce((sum, item) => sum + toNumber(item.deals), 0)
  const summaryInventory = options.totalInventoryOverride == null
    ? totalInventory
    : toNumber(options.totalInventoryOverride)
  const summaryDeals = options.totalDealsOverride == null
    ? totalDeals
    : toNumber(options.totalDealsOverride)

  const mainSegment = priceAnalysisData.reduce((best, item) => {
    if (!best) return item

    if (toNumber(item.deals) > toNumber(best.deals)) return item
    if (toNumber(item.deals) === toNumber(best.deals) && toNumber(item.inventory) > toNumber(best.inventory)) return item

    return best
  }, null)

  return {
    totalInventory: summaryInventory,
    totalDeals: summaryDeals,
    turnoverPeriod: summaryDeals > 0 ? (summaryInventory / summaryDeals * 3).toFixed(1) : '0.0',
    mainSegment: mainSegment && (toNumber(mainSegment.deals) > 0 || toNumber(mainSegment.inventory) > 0)
      ? mainSegment.range
      : '-'
  }
}
