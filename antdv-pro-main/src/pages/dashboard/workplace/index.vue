<script setup lang="ts">
import type { DualAxes, Line } from '@antv/g2plot'
import { TinyColor } from '@ctrl/tinycolor'
import { computed, ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { getDailyStats } from '@/api/DaliyStats'

interface DailyStatItem {
  date: string
  totalViewCount: number
  newUserCount: number
  loginUserCount: number
  newArticleCount: number
  newCommentCount: number
}

const viewChartRef = ref<HTMLElement | null>(null)
const userChartRef = ref<HTMLElement | null>(null)
const contentChartRef = ref<HTMLElement | null>(null)
let chartLibraryLoader: Promise<typeof import('@antv/g2plot')> | null = null

let viewChart: DualAxes | null = null
let userChart: Line | null = null
let contentChart: Line | null = null
const loading = ref(true)
const dailyStats = ref<DailyStatItem[]>([])
const appStore = useAppStore()
const { layoutSetting } = storeToRefs(appStore)
const primaryColor = computed(() => layoutSetting.value.colorPrimary || '#1677FF')

function loadChartLibrary() {
  if (!chartLibraryLoader)
    chartLibraryLoader = import('@antv/g2plot')

  return chartLibraryLoader
}

function destroyCharts() {
  viewChart?.destroy()
  userChart?.destroy()
  contentChart?.destroy()
  viewChart = null
  userChart = null
  contentChart = null
}

function formatNumber(value: number, digits = 0) {
  if (!Number.isFinite(value))
    return '0'

  if (digits === 0)
    return Math.round(value).toLocaleString()

  return value.toLocaleString(undefined, {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  })
}

function safeDivide(dividend: number, divisor: number, digits = 1) {
  if (!divisor)
    return 0

  return Number((dividend / divisor).toFixed(digits))
}

function formatShortDate(value: string) {
  return value?.slice(5) || value
}

const isDark = computed(() => layoutSetting.value.theme === 'dark')
const accentPalette = computed(() => ({
  base: primaryColor.value,
  deep: new TinyColor(primaryColor.value).darken(isDark.value ? 2 : 10).toHexString(),
  soft: new TinyColor(primaryColor.value).lighten(isDark.value ? 10 : 18).toHexString(),
  softer: new TinyColor(primaryColor.value).lighten(isDark.value ? 18 : 26).toHexString(),
  glowStrong: `linear-gradient(135deg, ${new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.22 : 0.14).toRgbString()}, transparent)`,
  glowSoft: `linear-gradient(135deg, ${new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.14 : 0.08).toRgbString()}, transparent)`,
}))
const chartTokens = computed(() => ({
  textColor: isDark.value ? '#E2E8F0' : '#0F172A',
  mutedTextColor: isDark.value ? 'rgba(226, 232, 240, 0.82)' : '#475569',
  subtleTextColor: isDark.value ? 'rgba(148, 163, 184, 0.92)' : '#64748B',
  gridColor: isDark.value ? 'rgba(148, 163, 184, 0.16)' : 'rgba(148, 163, 184, 0.22)',
  tooltipBg: isDark.value ? 'rgba(15, 23, 42, 0.96)' : 'rgba(255, 255, 255, 0.98)',
  tooltipBorder: isDark.value ? 'rgba(148, 163, 184, 0.22)' : 'rgba(148, 163, 184, 0.18)',
  surfaceStroke: isDark.value ? '#020617' : '#FFFFFF',
  palette: {
    views: accentPalette.value.base,
    activeUsers: accentPalette.value.soft,
    newUsers: accentPalette.value.deep,
    loginUsers: accentPalette.value.softer,
    articles: accentPalette.value.base,
    comments: accentPalette.value.soft,
  },
}))

const summaryCards = computed(() => {
  const items = dailyStats.value
  const totalViews = items.reduce((sum, item) => sum + (item.totalViewCount || 0), 0)
  const totalNewUsers = items.reduce((sum, item) => sum + (item.newUserCount || 0), 0)
  const totalLoginUsers = items.reduce((sum, item) => sum + (item.loginUserCount || 0), 0)
  const totalNewArticles = items.reduce((sum, item) => sum + (item.newArticleCount || 0), 0)
  const totalNewComments = items.reduce((sum, item) => sum + (item.newCommentCount || 0), 0)
  const peakDay = items.reduce<DailyStatItem | null>((best, item) => {
    if (!best || (item.totalViewCount || 0) > (best.totalViewCount || 0))
      return item
    return best
  }, null)
  const last7 = items.slice(-7)
  const prev7 = items.slice(-14, -7)
  const last7Views = last7.reduce((sum, item) => sum + (item.totalViewCount || 0), 0)
  const prev7Views = prev7.reduce((sum, item) => sum + (item.totalViewCount || 0), 0)
  const last7Delta = prev7Views
    ? `${last7Views >= prev7Views ? '+' : ''}${formatNumber(((last7Views - prev7Views) / prev7Views) * 100, 1)}%`
    : '暂无对比'

  return [
    {
      label: '最近 7 天浏览',
      value: formatNumber(last7Views),
      helper: prev7.length ? `较前 7 天 ${last7Delta}` : '最近一周浏览量',
      gradient: accentPalette.value.glowStrong,
    },
    {
      label: '30 天总浏览',
      value: formatNumber(totalViews),
      helper: `日均 ${formatNumber(safeDivide(totalViews, items.length || 1, 1), 1)}`,
      gradient: accentPalette.value.glowSoft,
    },
    {
      label: '用户活跃',
      value: formatNumber(totalLoginUsers),
      helper: `新增用户 ${formatNumber(totalNewUsers)}`,
      gradient: accentPalette.value.glowSoft,
    },
    {
      label: '内容增长',
      value: formatNumber(totalNewArticles + totalNewComments),
      helper: `文章 ${formatNumber(totalNewArticles)} / 评论 ${formatNumber(totalNewComments)}`,
      gradient: accentPalette.value.glowSoft,
    },
    {
      label: '流量峰值日',
      value: peakDay ? formatNumber(peakDay.totalViewCount || 0) : '0',
      helper: peakDay ? peakDay.date : '暂无数据',
      gradient: accentPalette.value.glowStrong,
    },
  ]
})

const recentStats = computed(() => dailyStats.value.slice(-7).reverse())

const initCharts = async (data: DailyStatItem[]) => {
  const chartData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
  const { DualAxes, Line } = await loadChartLibrary()
  destroyCharts()

  if (viewChartRef.value) {
    const activityData = chartData.map(item => ({
      date: item.date,
      activeUsers: (item.newUserCount || 0) + (item.loginUserCount || 0),
    }))

    viewChart = new DualAxes(viewChartRef.value, {
      data: [chartData, activityData],
      xField: 'date',
      yField: ['totalViewCount', 'activeUsers'],
      legend: {
        position: 'top',
        itemName: {
          style: {
            fill: chartTokens.value.textColor,
          },
        },
      },
      xAxis: {
        label: {
          autoRotate: false,
          autoHide: true,
          formatter: value => formatShortDate(value),
          style: {
            fill: chartTokens.value.mutedTextColor,
          },
        },
      },
      yAxis: {
        totalViewCount: {
          nice: true,
          label: {
            style: {
              fill: chartTokens.value.mutedTextColor,
            },
          },
          grid: {
            line: {
              style: {
                stroke: chartTokens.value.gridColor,
                lineDash: [4, 4],
              },
            },
          },
        },
        activeUsers: {
          nice: true,
          label: {
            style: {
              fill: chartTokens.value.mutedTextColor,
            },
          },
        },
      },
      tooltip: {
        showCrosshairs: true,
        domStyles: {
          'g2-tooltip': {
            background: chartTokens.value.tooltipBg,
            color: chartTokens.value.textColor,
            border: `1px solid ${chartTokens.value.tooltipBorder}`,
            boxShadow: 'none',
          },
          'g2-tooltip-title': {
            color: chartTokens.value.textColor,
          },
          'g2-tooltip-list-item': {
            color: chartTokens.value.textColor,
          },
        },
      },
      geometryOptions: [
        {
          geometry: 'line',
          smooth: true,
          color: chartTokens.value.palette.views,
          lineStyle: {
            lineWidth: 3,
          },
          point: {
            size: 3,
            shape: 'circle',
            style: {
              fill: chartTokens.value.palette.views,
              stroke: chartTokens.value.surfaceStroke,
              lineWidth: 2,
            },
          },
        },
        {
          geometry: 'column',
          color: chartTokens.value.palette.activeUsers,
          columnWidthRatio: 0.42,
          columnStyle: {
            radius: [10, 10, 0, 0],
          },
        },
      ],
    })
    viewChart.render()
  }

  const userData: Array<{ date: string, type: string, value: number }> = []
  chartData.forEach((item) => {
    userData.push({ date: item.date, type: '新增用户', value: item.newUserCount || 0 })
    userData.push({ date: item.date, type: '登录用户', value: item.loginUserCount || 0 })
  })

  if (userChartRef.value) {
    userChart = new Line(userChartRef.value, {
      data: userData,
      xField: 'date',
      yField: 'value',
      seriesField: 'type',
      smooth: true,
      color: [chartTokens.value.palette.newUsers, chartTokens.value.palette.loginUsers],
      legend: {
        position: 'top',
        itemName: {
          style: {
            fill: chartTokens.value.textColor,
          },
        },
      },
      xAxis: {
        label: {
          autoRotate: false,
          autoHide: true,
          formatter: value => formatShortDate(value),
          style: {
            fill: chartTokens.value.mutedTextColor,
          },
        },
      },
      yAxis: {
        nice: true,
        label: {
          style: {
            fill: chartTokens.value.mutedTextColor,
          },
        },
        grid: {
          line: {
            style: {
              stroke: chartTokens.value.gridColor,
              lineDash: [4, 4],
            },
          },
        },
      },
      point: {
        size: 3,
        shape: 'circle',
        style: {
          stroke: chartTokens.value.surfaceStroke,
          lineWidth: 2,
        },
      },
      tooltip: {
        showCrosshairs: true,
        domStyles: {
          'g2-tooltip': {
            background: chartTokens.value.tooltipBg,
            color: chartTokens.value.textColor,
            border: `1px solid ${chartTokens.value.tooltipBorder}`,
            boxShadow: 'none',
          },
          'g2-tooltip-title': {
            color: chartTokens.value.textColor,
          },
          'g2-tooltip-list-item': {
            color: chartTokens.value.textColor,
          },
        },
      },
    })
    userChart.render()
  }

  const contentData: Array<{ date: string, type: string, value: number }> = []
  chartData.forEach((item) => {
    contentData.push({ date: item.date, type: '新增文章', value: item.newArticleCount || 0 })
    contentData.push({ date: item.date, type: '新增评论', value: item.newCommentCount || 0 })
  })

  if (contentChartRef.value) {
    contentChart = new Line(contentChartRef.value, {
      data: contentData,
      xField: 'date',
      yField: 'value',
      seriesField: 'type',
      smooth: true,
      color: [chartTokens.value.palette.articles, chartTokens.value.palette.comments],
      legend: {
        position: 'top',
        itemName: {
          style: {
            fill: chartTokens.value.textColor,
          },
        },
      },
      xAxis: {
        label: {
          autoRotate: false,
          autoHide: true,
          formatter: value => formatShortDate(value),
          style: {
            fill: chartTokens.value.mutedTextColor,
          },
        },
      },
      yAxis: {
        nice: true,
        label: {
          style: {
            fill: chartTokens.value.mutedTextColor,
          },
        },
        grid: {
          line: {
            style: {
              stroke: chartTokens.value.gridColor,
              lineDash: [4, 4],
            },
          },
        },
      },
      point: {
        size: 3,
        shape: 'circle',
        style: {
          stroke: chartTokens.value.surfaceStroke,
          lineWidth: 2,
        },
      },
      tooltip: {
        showCrosshairs: true,
        domStyles: {
          'g2-tooltip': {
            background: chartTokens.value.tooltipBg,
            color: chartTokens.value.textColor,
            border: `1px solid ${chartTokens.value.tooltipBorder}`,
            boxShadow: 'none',
          },
          'g2-tooltip-title': {
            color: chartTokens.value.textColor,
          },
          'g2-tooltip-list-item': {
            color: chartTokens.value.textColor,
          },
        },
      },
    })
    contentChart.render()
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getDailyStats()
    const rawData: DailyStatItem[] = res?.data || []
    dailyStats.value = [...rawData].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())

    await nextTick()
    if (dailyStats.value.length > 0)
      await initCharts(dailyStats.value)
  } catch (e) {
    console.error('Failed to fetch daily stats', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

watch(
  () => layoutSetting.value.theme,
  async () => {
    await nextTick()
    if (dailyStats.value.length)
      await initCharts(dailyStats.value)
  },
)

onUnmounted(() => {
  destroyCharts()
})
</script>

<template>
  <page-container>
    <div :class="['workplace-container', { 'is-dark': isDark }]">
      <a-spin :spinning="loading">
        <section class="monitor-hero">
          <div class="hero-copy">
            <p class="hero-eyebrow">MONITOR CENTER</p>
            <h2>站点监控趋势</h2>
            <p class="hero-desc">把浏览量、活跃用户和内容产出拆成三条可读趋势线，不再只看一堆生硬柱状条。</p>
            <div class="hero-tags">
              <span>30 天趋势</span>
              <span>双轴主图</span>
              <span>重点波动追踪</span>
            </div>
          </div>
          <div class="hero-side">
            <article class="hero-pill">
              <span>样本天数</span>
              <strong>{{ dailyStats.length }}</strong>
            </article>
            <article class="hero-pill">
              <span>最新记录</span>
              <strong>{{ dailyStats[dailyStats.length - 1]?.date || '--' }}</strong>
            </article>
          </div>
        </section>

        <div class="summary-grid">
          <article v-for="item in summaryCards" :key="item.label" class="summary-card" :style="{ '--card-glow': item.gradient }">
            <span class="summary-card__glow"></span>
            <span class="summary-card__label">{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <span class="summary-card__helper">{{ item.helper }}</span>
          </article>
        </div>

      </a-spin>
    </div>
  </page-container>
</template>

<style scoped lang="less">
.workplace-container {
  --panel-bg: rgba(255, 255, 255, 0.98);
  --panel-border: rgba(148, 163, 184, 0.2);
  --panel-shadow: 0 22px 44px rgba(15, 23, 42, 0.08);
  --text-strong: #0f172a;
  --text-muted: #475569;
  --text-subtle: #64748b;
  --panel-soft: rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: var(--text-strong);
}

.workplace-container.is-dark {
  --panel-bg: rgba(15, 23, 42, 0.98);
  --panel-border: rgba(148, 163, 184, 0.2);
  --panel-shadow: 0 28px 56px rgba(2, 6, 23, 0.42);
  --text-strong: #f8fafc;
  --text-muted: rgba(226, 232, 240, 0.84);
  --text-subtle: rgba(148, 163, 184, 0.94);
  --panel-soft: rgba(255, 255, 255, 0.06);
}

.monitor-hero,
.summary-card,
.panel {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--panel-border);
  //box-shadow: var(--panel-shadow);
}

.monitor-hero::before,
.summary-card::before,
.panel::before {
  display: none;
}

.monitor-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.8fr);
  gap: 16px;
  padding: 30px;
  border-radius: 28px;
  //background: var(--panel-bg);
  color: var(--text-strong);
}

.hero-copy,
.hero-side,
.panel-head,
.recent-list,
.summary-card {
  position: relative;
  z-index: 1;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-eyebrow,
.panel-head p {
  margin: 0;
  color: v-bind(primaryColor);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-copy h2 {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.hero-desc {
  margin: 0;
  max-width: 560px;
  color: var(--text-muted);
  font-size: 15px;
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-tags span,
.panel-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid var(--panel-border);
  background: transparent;
  color: var(--text-subtle);
  font-size: 12px;
  font-weight: 600;
}

.hero-side {
  display: grid;
  gap: 14px;
}

.hero-pill {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid var(--panel-border);
  background: transparent;
}

.hero-pill span {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
}

.hero-pill strong {
  color: var(--text-strong);
  font-size: 24px;
  line-height: 1.15;
}

.summary-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  border-radius: 24px;
 // background: var(--panel-bg);
}

.summary-card__glow {
  display: none;
}

.summary-card__label,
.summary-card strong,
.summary-card__helper {
  position: relative;
  z-index: 1;
}

.summary-card strong {
  color: var(--text-strong);
  font-size: 30px;
  line-height: 1.1;
}

.summary-card__label {
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
}

.summary-card__helper {
  color: var(--text-subtle);
  font-size: 12px;
  line-height: 1.7;
}

.monitor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.monitor-grid--bottom {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  border-radius: 26px;
  //background: var(--panel-bg);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.panel-head h3 {
  margin: 6px 0 0;
  color: var(--text-strong);
  font-size: 24px;
  font-weight: 700;
}

.panel-chip--neutral {
  border-color: var(--panel-border);
  background: transparent;
  color: var(--text-subtle);
}

.chart-surface {
  position: relative;
  z-index: 1;
  height: 300px;
}

.chart-surface--lg {
  height: 360px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--panel-border);
}

.recent-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.recent-item__date {
  display: flex;
  min-width: 72px;
  flex-direction: column;
  gap: 4px;
}

.recent-item__date span {
  color: var(--text-strong);
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.recent-item__date small {
  color: var(--text-subtle);
  font-size: 12px;
}

.recent-item__meta {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.recent-item__meta strong {
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
}

.recent-item__meta span {
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 1400px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .monitor-grid,
  .monitor-grid--bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .monitor-hero {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .monitor-hero,
  .panel {
    padding: 22px 18px;
  }

  .monitor-hero h2 {
    font-size: 28px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .recent-item,
  .hero-side {
    flex-direction: column;
    gap: 8px;
  }

  .hero-side {
    display: flex;
  }
}
</style>
