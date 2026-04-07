/** 后端 LocalDateTime 可能为字符串或 Jackson 时间戳数组 */
export function formatDateTime(v) {
  if (v == null || v === '') return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  if (Array.isArray(v) && v.length >= 3) {
    const [y, m = 1, d = 1, h = 0, mi = 0, s = 0] = v
    const pad = (n) => String(n).padStart(2, '0')
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(mi)}:${pad(s)}`
  }
  return String(v)
}

/** 将 ys_order.status 数值字符串映射为前端订单页 tab */
export function orderStatusUi(code) {
  const c = String(code)
  if (c === '2') return { key: 'pending', text: '待付款' }
  if (c === '3') return { key: 'processing', text: '待发货' }
  if (c === '6') return { key: 'shipped', text: '待收货' }
  if (c === '7' || c === '10') return { key: 'completed', text: '已完成' }
  if (c === '4' || c === '5') return { key: 'cancelled', text: '已取消' }
  if (c === '8') return { key: 'cancelled', text: '已退款' }
  if (c === '9') return { key: 'processing', text: '退款处理中' }
  return { key: 'processing', text: '处理中' }
}

export function groupOrderRows(rows) {
  if (!Array.isArray(rows) || rows.length === 0) return []
  const map = new Map()
  for (const row of rows) {
    const order = row?.order || row
    const goods = row?.goods || row
    const oid = order?.id
    if (!oid) continue
    const lineTotal =
      order.totalAmount != null
        ? Number(order.totalAmount)
        : Number(order.unitPrice || 0) * (order.quantity || 1)
    const itemName = goods.name || row.goodsName || `商品 #${order.goodsId}`
    if (!map.has(oid)) {
      const ui = orderStatusUi(order.status)
      map.set(oid, {
        id: oid,
        orderNumber: String(oid),
        orderTime: formatDateTime(order.addTime || order.addtime),
        totalAmount: 0,
        status: ui.key,
        statusCode: String(order.status),
        items: []
      })
    }
    const g = map.get(oid)
    g.totalAmount += lineTotal
    g.items.push({
      name: itemName,
      price: order.unitPrice != null ? Number(order.unitPrice) : 0,
      quantity: order.quantity != null ? order.quantity : 1
    })
  }
  return Array.from(map.values()).sort((a, b) => String(b.orderNumber).localeCompare(String(a.orderNumber)))
}
