export function getActiveFlowIndex(rawStatus) {
  const status = Number(rawStatus)

  if (status === 0) return 0
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 3
  if (status === 4) return 1
  return 0
}

export function getFlowNodeStateByStatus(rawStatus, index) {
  const status = Number(rawStatus)

  if (status === 4) {
    return index <= 1 ? 'done' : 'disabled'
  }

  const activeIndex = getActiveFlowIndex(status)

  if (index < activeIndex) return 'done'
  if (index === activeIndex) return 'active'
  return 'pending'
}

export function getConnectorStateByStatus(rawStatus, index) {
  const nodeState = getFlowNodeStateByStatus(rawStatus, index)
  return nodeState === 'done' || nodeState === 'active' ? 'active' : 'inactive'
}
