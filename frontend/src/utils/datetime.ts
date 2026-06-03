type DateLike = Date | number | string | null | undefined

function toDate(value: DateLike) {
  if (value == null || value === '') return null
  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatDate(value: DateLike, fallback = '-') {
  if (typeof value === 'string') return value ? value.slice(0, 10) : fallback
  const date = toDate(value)
  return date ? date.toISOString().slice(0, 10) : fallback
}

export function formatDateTime(value: DateLike, fallback = '-') {
  const date = toDate(value)
  return date ? date.toLocaleString() : fallback
}

export function todayKey() {
  return formatDate(new Date(), '')
}
