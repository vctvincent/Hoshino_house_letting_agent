const HOUSE_LABEL = { 0: '未发布', 1: '已发布', 2: '已成交', 3: '已下架' }
const HOUSE_TYPE = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
const AUDIT_LABEL = { 0: '待审核', 1: '审核中', 2: '已通过', 3: '已驳回' }
const AUDIT_TYPE = { 0: 'warning', 1: '', 2: 'success', 3: 'danger' }
const VIEWING_LABEL = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消', 4: '已过期' }
const VIEWING_TYPE = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger', 4: 'info' }
const TRANSACTION_LABEL = { 0: '待确认', 1: '谈判中', 2: '已签约', 3: '已完成', 4: '已取消' }
const TRANSACTION_TYPE = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }

export const HOUSE_STATUS = Object.freeze({ UNPUBLISHED: 0, PUBLISHED: 1, SOLD: 2, DELISTED: 3 })
export const AUDIT_STATUS = Object.freeze({ PENDING: 0, REVIEWING: 1, APPROVED: 2, REJECTED: 3 })
export const VIEWING_STATUS = Object.freeze({ PENDING: 0, CONFIRMED: 1, COMPLETED: 2, CANCELLED: 3, EXPIRED: 4 })
export const TRANSACTION_STATUS = Object.freeze({ PENDING: 0, NEGOTIATING: 1, SIGNED: 2, COMPLETED: 3, CANCELLED: 4 })

export function getHouseStatusLabel(s) { return HOUSE_LABEL[Number(s)] || '状态未知' }
export function getHouseStatusType(s) { return HOUSE_TYPE[Number(s)] || 'info' }
export function getAuditStatusLabel(s) { return AUDIT_LABEL[Number(s)] || '审核未知' }
export function getAuditStatusType(s) { return AUDIT_TYPE[Number(s)] || 'info' }
export function getViewingStatusLabel(s) { return VIEWING_LABEL[Number(s)] || '未知状态' }
export function getViewingStatusType(s) { return VIEWING_TYPE[Number(s)] || 'info' }
export function getTransactionStatusLabel(s) { return TRANSACTION_LABEL[Number(s)] || '未知状态' }
export function getTransactionStatusType(s) { return TRANSACTION_TYPE[Number(s)] || 'info' }
