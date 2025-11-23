/**
 * 格式化时间戳为可读时间字符串
 * @param timestamp 时间戳(毫秒)
 * @param format 格式化字符串
 * @returns 格式化后的时间字符串
 */
export function formatTime(timestamp: number, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!timestamp || isNaN(timestamp)) return ''

  const date = new Date(timestamp)

  const padZero = (num: number): string => {
    return num < 10 ? `0${num}` : num.toString()
  }

  const year = date.getFullYear()
  const month = padZero(date.getMonth() + 1)
  const day = padZero(date.getDate())
  const hour = padZero(date.getHours())
  const minute = padZero(date.getMinutes())
  const second = padZero(date.getSeconds())

  return format
    .replace('YYYY', year.toString())
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化数字为带千分位的字符串
 * @param num 要格式化的数字
 * @returns 带千分位的数字字符串
 */
export function formatNumber(num: number | string): string {
  if (num === undefined || num === null) return '0'

  const number = typeof num === 'string' ? parseFloat(num) : num
  if (isNaN(number)) return '0'

  return new Intl.NumberFormat('zh-CN').format(number)
}

/**
 * 格式化百分比
 * @param ratio 比例值(如0.123表示12.3%)
 * @param decimalPlaces 保留小数位数
 * @returns 格式化后的百分比字符串
 */
export function formatPercentage(ratio: number, decimalPlaces: number = 2): string {
  if (isNaN(ratio)) return '0%'
  const percentage = ratio * 100
  return `${percentage.toFixed(decimalPlaces)}%`
}