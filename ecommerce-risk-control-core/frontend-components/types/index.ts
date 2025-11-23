// 风险事件类型定义
export interface RiskEvent {
  eventId: string
  orderId: string
  userId: string
  ruleId: string
  timestamp: number
  action: 'WARN' | 'VERIFY' | 'BLOCK'
  reason: string
  status?: 'PENDING' | 'PROCESSED' | 'IGNORED'
  amount?: number
  deviceId: string
  ip: string
}

// 风险规则类型定义
export interface RiskRule {
  ruleId: string
  ruleName: string
  ruleType: 'FREQUENCY' | 'AMOUNT' | 'DEVICE' | 'IP'
  threshold: number
  timeWindow?: number
  action: 'WARN' | 'VERIFY' | 'BLOCK'
}

// 统计数据接口
export interface Stats {
  total: number
  block: number
  verify: number
  warn: number
  totalTrend: number
  blockTrend: number
  verifyTrend: number
  warnTrend: number
}

// 趋势数据接口
export interface TrendData {
  dates: string[]
  totals: number[]
  blocks: number[]
  verifies: number[]
}

// 类型分布接口
export interface TypeDistribution {
  types: string[]
  counts: number[]
}