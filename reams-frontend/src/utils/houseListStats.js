function toNumber(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num : 0
}

export function isPublishedHouse(house) {
  return Number(house?.houseStatus) === 1 && Number(house?.auditStatus) === 2
}

export function summarizeHouseListStats(houses = [], { totalCount = houses.length, now = new Date() } = {}) {
  const list = Array.isArray(houses) ? houses : []
  const today = new Date(now).toISOString().split('T')[0]

  const publishedCount = list.filter(isPublishedHouse).length
  const prices = list
    .map(house => toNumber(house?.price))
    .filter(price => price > 0)

  const averagePrice = prices.length > 0
    ? (prices.reduce((sum, price) => sum + price, 0) / prices.length).toFixed(1)
    : '0.0'

  const todayAdded = list.filter(house => {
    if (!house?.createTime) return false
    return new Date(house.createTime).toISOString().split('T')[0] === today
  }).length

  return {
    totalCount: toNumber(totalCount),
    publishedCount,
    averagePrice,
    todayAdded
  }
}
