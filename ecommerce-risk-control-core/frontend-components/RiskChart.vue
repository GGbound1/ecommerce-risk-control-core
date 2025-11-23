<template>
  <div ref="chartEl" class="chart-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'

// 图表数据类型定义
interface LineChartData {
  xAxis: string[]
  series: { name: string; data: number[]; color?: string }[]
}

interface PieChartData {
  legend: string[]
  series: number[]
  colors?: string[]
}

type ChartData = LineChartData | PieChartData

// 组件属性
const props = defineProps<{
  type: 'line' | 'pie'
  data: ChartData
  animation?: boolean
  title?: string
}>()

// 图表实例
const chartEl = ref<HTMLDivElement | null>(null)
let chart: ECharts | null = null

// 初始化图表
const initChart = () => {
  if (chartEl.value) {
    if (chart) chart.dispose()
    chart = echarts.init(chartEl.value)
    updateChartOptions()
  }
}

// 更新图表配置
const updateChartOptions = () => {
  if (!chart) return

  const baseOption: EChartsOption = {
    backgroundColor: 'transparent',
    animation: props.animation !== false,
    animationDuration: 1000,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(96, 165, 250, 0.5)',
      textStyle: { color: '#f8fafc' },
    },
    title: props.title ? {
      text: props.title,
      left: 'center',
      textStyle: { color: '#1e293b', fontSize: 16 }
    } : undefined,
  }

  let option: EChartsOption = { ...baseOption }

  // 折线图配置
  if (props.type === 'line' && isLineChartData(props.data)) {
    const lineData = props.data as LineChartData
    option = {
      ...baseOption,
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: lineData.xAxis,
        axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.3)' } },
        axisLabel: { color: '#94a3b8' },
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)' } },
      },
      series: lineData.series.map((item, index) => ({
        name: item.name,
        type: 'line',
        data: item.data,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: item.color || getThemeColors()[index % getThemeColors().length],
        },
        itemStyle: {
          color: item.color || getThemeColors()[index % getThemeColors().length],
        }
      })),
    }
  }

  // 饼图配置
  if (props.type === 'pie' && isPieChartData(props.data)) {
    const pieData = props.data as PieChartData
    option = {
      ...baseOption,
      tooltip: {
        ...baseOption.tooltip,
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { color: '#94a3b8' },
        data: pieData.legend,
      },
      series: [
        {
          name: '风险分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: { show: false },
          emphasis: {
            label: { 
              show: true, 
              fontSize: 16,
              fontWeight: 'bold'
            },
          },
          data: pieData.legend.map((name, index) => ({
            name,
            value: pieData.series[index],
            itemStyle: {
              color: pieData.colors?.[index] || getThemeColors()[index % getThemeColors().length],
            },
          })),
        },
      ],
    }
  }

  chart.setOption(option)
}

// 类型守卫
function isLineChartData(data: ChartData): data is LineChartData {
  return 'xAxis' in data && Array.isArray(data.xAxis)
}

function isPieChartData(data: ChartData): data is PieChartData {
  return 'legend' in data && Array.isArray(data.legend)
}

// 莫兰迪色系
function getThemeColors(): string[] {
  return ['#A5B4CB', '#B7B7A4', '#D8AFA8', '#9D8189', '#CB997E', '#E8C3B9']
}

// 响应式处理
const handleResize = () => chart?.resize()

// 生命周期
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

watch(() => props.data, updateChartOptions, { deep: true })
watch(() => props.type, updateChartOptions)
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
  min-height: 300px;
}
</style>