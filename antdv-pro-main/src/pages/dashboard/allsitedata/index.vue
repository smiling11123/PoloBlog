<script setup lang="ts">
import type { Bar } from '@antv/g2plot'
import { TinyColor } from '@ctrl/tinycolor'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getAllSiteData } from '~/api/allSiteData'
import {
  CommentOutlined,
  CrownOutlined,
  EyeOutlined,
  FileTextOutlined,
  GlobalOutlined,
  MessageOutlined,
  RadarChartOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'

interface SiteData {
  totalUserCount: number
  totalArticleCount: number
  totalCommentCount: number
  totalViewCount: number
  totalVisitCount: number
  totalVisitorCount: number
  totalMessageSlipCount: number
}

const defaultSiteData: SiteData = {
  totalUserCount: 0,
  totalArticleCount: 0,
  totalCommentCount: 0,
  totalViewCount: 0,
  totalVisitCount: 0,
  totalVisitorCount: 0,
  totalMessageSlipCount: 0,
}

const loading = ref(false)
const siteData = ref<SiteData>({ ...defaultSiteData })
const distributionChartRef = ref<HTMLElement | null>(null)

let chartLibraryLoader: Promise<typeof import('@antv/g2plot')> | null = null
let distributionChart: Bar | null = null

const appStore = useAppStore()
const { layoutSetting } = storeToRefs(appStore)
const primaryColor = computed(() => layoutSetting.value.colorPrimary || '#1677FF')

function loadChartLibrary() {
  if (!chartLibraryLoader)
    chartLibraryLoader = import('@antv/g2plot')

  return chartLibraryLoader
}

function destroyChart() {
  distributionChart?.destroy()
  distributionChart = null
}

function safeDivide(dividend: number, divisor: number, digits = 2) {
  if (!divisor)
    return 0

  return Number((dividend / divisor).toFixed(digits))
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

const isDark = computed(() => layoutSetting.value.theme === 'dark')
const accentPalette = computed(() => ({
  base: primaryColor.value,
  deep: new TinyColor(primaryColor.value).darken(isDark.value ? 2 : 10).toHexString(),
  soft: new TinyColor(primaryColor.value).lighten(isDark.value ? 10 : 18).toHexString(),
  softer: new TinyColor(primaryColor.value).lighten(isDark.value ? 18 : 26).toHexString(),
  surface: new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.18 : 0.1).toRgbString(),
  surfaceStrong: new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.24 : 0.14).toRgbString(),
  glowStrong: `linear-gradient(135deg, ${new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.24 : 0.16).toRgbString()}, transparent)`,
  glowSoft: `linear-gradient(135deg, ${new TinyColor(primaryColor.value).setAlpha(isDark.value ? 0.16 : 0.1).toRgbString()}, transparent)`,
}))

const chartTokens = computed(() => ({
  textColor: isDark.value ? '#E2E8F0' : '#0F172A',
  subtleTextColor: isDark.value ? 'rgba(148, 163, 184, 0.92)' : '#64748B',
  gridColor: isDark.value ? 'rgba(148, 163, 184, 0.16)' : 'rgba(148, 163, 184, 0.22)',
  tooltipBg: isDark.value ? 'rgba(15, 23, 42, 0.96)' : 'rgba(255, 255, 255, 0.98)',
  tooltipBorder: isDark.value ? 'rgba(148, 163, 184, 0.24)' : 'rgba(148, 163, 184, 0.2)',
}))

const totalContentAsset = computed(() =>
  siteData.value.totalArticleCount + siteData.value.totalCommentCount + siteData.value.totalMessageSlipCount,
)

const heroMetrics = computed(() => [
  {
    label: '累计访问',
    value: siteData.value.totalVisitCount,
    helper: '包含重复访问次数',
    icon: GlobalOutlined,
    accent: accentPalette.value.base,
    glow: accentPalette.value.glowStrong,
  },
  {
    label: '文章浏览',
    value: siteData.value.totalViewCount,
    helper: '内容被打开的总次数',
    icon: EyeOutlined,
    accent: accentPalette.value.soft,
    glow: accentPalette.value.glowSoft,
  },
  {
    label: '内容资产',
    value: totalContentAsset.value,
    helper: '文章、评论和留言沉淀',
    icon: RadarChartOutlined,
    accent: accentPalette.value.softer,
    glow: accentPalette.value.glowSoft,
  },
])

const primaryMetricSeed = computed(() => [
  {
    key: 'users',
    label: '总用户数',
    value: siteData.value.totalUserCount,
    helper: '账号规模',
    accent: accentPalette.value.base,
    surface: `linear-gradient(135deg, ${accentPalette.value.surfaceStrong}, transparent)`,
    icon: UserOutlined,
  },
  {
    key: 'articles',
    label: '总文章数',
    value: siteData.value.totalArticleCount,
    helper: '内容库存',
    accent: accentPalette.value.deep,
    surface: `linear-gradient(135deg, ${accentPalette.value.surface}, transparent)`,
    icon: FileTextOutlined,
  },
  {
    key: 'comments',
    label: '总评论数',
    value: siteData.value.totalCommentCount,
    helper: '互动沉淀',
    accent: accentPalette.value.soft,
    surface: `linear-gradient(135deg, ${accentPalette.value.surface}, transparent)`,
    icon: CommentOutlined,
  },
  {
    key: 'visitors',
    label: '独立访客',
    value: siteData.value.totalVisitorCount,
    helper: '累计触达',
    accent: accentPalette.value.softer,
    surface: `linear-gradient(135deg, ${accentPalette.value.surface}, transparent)`,
    icon: CrownOutlined,
  },
  {
    key: 'messages',
    label: '留言数量',
    value: siteData.value.totalMessageSlipCount,
    helper: '社区反馈',
    accent: accentPalette.value.deep,
    surface: `linear-gradient(135deg, ${accentPalette.value.surface}, transparent)`,
    icon: MessageOutlined,
  },
])

const primaryScaleBase = computed(() => Math.max(...primaryMetricSeed.value.map(item => item.value), 1))

const primaryMetrics = computed(() =>
  primaryMetricSeed.value.map(item => ({
    ...item,
    progress: item.value ? Math.max(14, Math.round((item.value / primaryScaleBase.value) * 100)) : 0,
  })),
)

const derivedMetrics = computed(() => [
  {
    label: '篇均浏览',
    value: `${formatNumber(safeDivide(siteData.value.totalViewCount, siteData.value.totalArticleCount, 1), 1)} 次`,
    helper: '每篇文章平均带来的浏览次数',
    accent: accentPalette.value.base,
  },
  {
    label: '篇均评论',
    value: `${formatNumber(safeDivide(siteData.value.totalCommentCount, siteData.value.totalArticleCount, 1), 1)} 条`,
    helper: '每篇文章平均沉淀的评论数量',
    accent: accentPalette.value.soft,
  },
  {
    label: '人均访问',
    value: `${formatNumber(safeDivide(siteData.value.totalVisitCount, siteData.value.totalVisitorCount, 2), 2)} 次`,
    helper: '每位独立访客平均访问站点次数',
    accent: accentPalette.value.softer,
  },
  {
    label: '评论转化',
    value: `${formatNumber(safeDivide(siteData.value.totalCommentCount, siteData.value.totalViewCount, 4) * 100, 2)}%`,
    helper: '浏览到评论的转化能力',
    accent: accentPalette.value.deep,
  },
])

const operationInsights = computed(() => [
  {
    label: '内容沉淀',
    value: formatNumber(totalContentAsset.value),
    helper: '文章、评论和留言的总内容资产',
    icon: RadarChartOutlined,
    accent: accentPalette.value.base,
  },
  {
    label: '访问深度',
    value: `${formatNumber(safeDivide(siteData.value.totalViewCount, siteData.value.totalVisitCount, 2), 2)} 页/访`,
    helper: '访问后继续浏览内容的深度',
    icon: ThunderboltOutlined,
    accent: accentPalette.value.soft,
  },
  {
    label: '社区参与',
    value: `${formatNumber(safeDivide(siteData.value.totalMessageSlipCount, siteData.value.totalVisitorCount, 4) * 100, 2)}%`,
    helper: '留言在访客中的渗透情况',
    icon: TeamOutlined,
    accent: accentPalette.value.deep,
  },
])

const signalItems = computed(() => {
  const visitDepth = safeDivide(siteData.value.totalViewCount, siteData.value.totalVisitCount, 2)
  const interactionDensity = safeDivide(siteData.value.totalCommentCount + siteData.value.totalMessageSlipCount, siteData.value.totalViewCount, 4) * 100
  const visitorReturn = safeDivide(siteData.value.totalVisitCount, siteData.value.totalVisitorCount, 2)

  return [
    {
      label: '访问深度',
      value: `${formatNumber(visitDepth, 2)} 页/访`,
      helper: '浏览量 / 访问量',
      score: Math.min(100, visitDepth * 18),
      accent: `linear-gradient(90deg, ${accentPalette.value.soft}, ${accentPalette.value.base})`,
    },
    {
      label: '互动密度',
      value: `${formatNumber(interactionDensity, 2)}%`,
      helper: '评论与留言 / 浏览量',
      score: Math.min(100, interactionDensity * 4),
      accent: `linear-gradient(90deg, ${accentPalette.value.softer}, ${accentPalette.value.base})`,
    },
    {
      label: '访客回流',
      value: `${formatNumber(visitorReturn, 2)} 次`,
      helper: '访问量 / 独立访客',
      score: Math.min(100, visitorReturn * 24),
      accent: `linear-gradient(90deg, ${accentPalette.value.base}, ${accentPalette.value.deep})`,
    },
  ]
})

const distributionChartData = computed(() => {
  const items = [
    { label: '访问总量', value: siteData.value.totalVisitCount, accent: accentPalette.value.base },
    { label: '浏览总量', value: siteData.value.totalViewCount, accent: accentPalette.value.base },
    { label: '互动总量', value: siteData.value.totalCommentCount + siteData.value.totalMessageSlipCount, accent: accentPalette.value.base },
    { label: '用户总量', value: siteData.value.totalUserCount, accent: accentPalette.value.base },
    { label: '访客总量', value: siteData.value.totalVisitorCount, accent: accentPalette.value.base },
  ]
  const base = Math.max(...items.map(item => item.value), 1)

  return items.map(item => ({
    ...item,
    relative: Number(((item.value / base) * 100).toFixed(1)),
    displayValue: formatNumber(item.value),
  }))
})

async function initDistributionChart() {
  if (!distributionChartRef.value || !distributionChartData.value.some(item => item.value > 0)) {
    destroyChart()
    return
  }

  const { Bar } = await loadChartLibrary()
  destroyChart()

  distributionChart = new Bar(distributionChartRef.value, {
    data: distributionChartData.value,
    xField: 'relative',
    yField: 'label',
    seriesField: 'label',
    legend: false,
    padding: [8, 24, 20, 16],
    color: distributionChartData.value.map(item => item.accent),
    maxBarWidth: 18,
    barWidthRatio: 0.72,
    barStyle: {
      radius: [999, 999, 999, 999],
      fillOpacity: 0.92,
    },
    xAxis: {
      min: 0,
      max: 100,
      label: {
        formatter: value => `${value}%`,
        style: {
          fill: chartTokens.value.subtleTextColor,
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
    yAxis: {
      label: {
        style: {
          fill: chartTokens.value.textColor,
          fontWeight: 600,
        },
      },
    },
    label: {
      position: 'right',
      offset: 10,
      style: {
        fill: chartTokens.value.textColor,
        fontWeight: 700,
      },
      formatter: datum => datum.displayValue,
    },
    tooltip: {
      showMarkers: false,
      formatter: datum => ({
        name: datum.label,
        value: `${datum.displayValue} (${datum.relative}%)`,
      }),
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
    interactions: [{ type: 'active-region' }],
  })

  distributionChart.render()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getAllSiteData()
    if (res?.data)
      siteData.value = res.data

    await nextTick()
    await initDistributionChart()
  } catch (error) {
    message.error('Failed to fetch site data')
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
    await initDistributionChart()
  },
)

onUnmounted(() => {
  destroyChart()
})
</script>

<template>
  <page-container>
    <div :class="['all-site-data-container', { 'is-dark': isDark }]">
      <a-spin :spinning="loading">
        <section class="overview-hero">
          <div class="overview-copy">
            <p class="hero-eyebrow">SITE INTELLIGENCE</p>
            <h2>全站数据中枢</h2> 
          </div>
          <div class="hero-metrics">
            <article v-for="item in heroMetrics" :key="item.label" class="hero-metric"
              :style="{ '--metric-glow': item.glow }">
              <span class="hero-metric__glow"></span>
              <span class="hero-metric__icon" :style="{ color: item.accent }">
                <component :is="item.icon" />
              </span>
              <div class="hero-metric__body">
                <span class="hero-metric__label">{{ item.label }}</span>
                <strong>{{ formatNumber(item.value) }}</strong>
                <small>{{ item.helper }}</small>
              </div>
            </article>
          </div>
        </section>

        <div class="metric-grid">
          <article v-for="item in primaryMetrics" :key="item.key" class="metric-card"
            :style="{ '--accent': item.accent, '--accent-surface': item.surface }">
            <div class="metric-card__top">
              <span class="metric-card__icon">
                <component :is="item.icon" />
              </span>
              <span class="metric-card__scale">规模 {{ item.progress }}%</span>
            </div>
            <div class="metric-card__body">
              <span class="metric-card__label">{{ item.label }}</span>
              <strong>{{ formatNumber(item.value) }}</strong>
              <span class="metric-card__helper">{{ item.helper }}</span>
            </div>
            <div class="metric-card__progress">
              <span :style="{ width: `${item.progress}%` }"></span>
            </div>
          </article>
        </div>

      </a-spin>
    </div>
  </page-container>
</template>

<style scoped lang="less">
.all-site-data-container {
  --panel-bg: rgba(255, 255, 255, 0.98);
  --panel-border: rgba(148, 163, 184, 0.2);
  --panel-shadow: 0 22px 44px rgba(15, 23, 42, 0.08);
  --text-strong: #0f172a;
  --text-muted: #475569;
  --text-subtle: #64748b;
  --track-bg: rgba(148, 163, 184, 0.2);
  --panel-soft: rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: var(--text-strong);
}

.all-site-data-container.is-dark {
  --panel-bg: rgba(15, 23, 42, 0.98);
  --panel-border: rgba(148, 163, 184, 0.2);
  --panel-shadow: 0 28px 56px rgba(2, 6, 23, 0.42);
  --text-strong: #f8fafc;
  --text-muted: rgba(226, 232, 240, 0.84);
  --text-subtle: rgba(148, 163, 184, 0.94);
  --track-bg: rgba(148, 163, 184, 0.18);
  --panel-soft: rgba(255, 255, 255, 0.06);
}

.overview-hero,
.metric-card,
.detail-panel {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--panel-border);
  //box-shadow: var(--panel-shadow);
}

.overview-hero::before,
.metric-card::before,
.detail-panel::before {
  display: none;
}

.overview-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.9fr);
  gap: 16px;
  padding: 30px;
  border-radius: 28px;
  //background: var(--panel-bg);
  color: var(--text-strong);
}

.overview-copy,
.hero-metrics,
.metric-card__body,
.panel-head,
.insight-list,
.signal-list {
  position: relative;
  z-index: 1;
}

.overview-copy {
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

.overview-copy h2 {
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

.hero-metrics {
  display: grid;
  gap: 14px;
}

.hero-metric {
  position: relative;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid var(--panel-border);
  background: transparent;
}

.hero-metric__glow {
  display: none;
}

.hero-metric__icon,
.hero-metric__body {
  position: relative;
  z-index: 1;
}

.hero-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  //background: var(--panel-soft);
  font-size: 18px;
}

.hero-metric__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-metric__label {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
}

.hero-metric strong {
  color: var(--text-strong);
  font-size: 28px;
  line-height: 1.1;
}

.hero-metric small {
  color: var(--text-subtle);
  font-size: 12px;
}

.metric-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
  border-radius: 24px;
  //background: var(--panel-bg);
}

.metric-card::after {
  display: none;
}

.metric-card__top,
.metric-card__body,
.metric-card__progress {
  position: relative;
  z-index: 1;
}

.metric-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--accent);
  font-size: 20px;
}

.all-site-data-container.is-dark .metric-card__icon {
  background: rgba(255, 255, 255, 0.06);
}

.metric-card__scale {
  color: var(--text-subtle);
  font-size: 12px;
  font-weight: 600;
}

.metric-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-card__label,
.derived-item__label,
.insight-item__label,
.signal-item__label {
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
}

.metric-card__body strong {
  font-size: 30px;
  line-height: 1.1;
}

.metric-card__helper,
.derived-item__helper,
.insight-item__helper,
.signal-item__helper {
  color: var(--text-subtle);
  font-size: 12px;
  line-height: 1.7;
}

.metric-card__progress {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  //background: var(--track-bg);
}

.metric-card__progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  //background: linear-gradient(90deg, var(--accent), color-mix(in srgb, var(--accent) 65%, white));
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.8fr);
  gap: 16px;
}

.detail-grid--bottom {
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
}

.detail-panel {
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

.derived-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.derived-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 20px;
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.all-site-data-container.is-dark .derived-item {
  background: transparent;
}

.derived-item strong {
  position: relative;
  width: fit-content;
  font-size: 28px;
  line-height: 1.1;
}

.derived-item strong::after {
  position: absolute;
  right: -10px;
  bottom: 5px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  //background: var(--derived-accent);
  box-shadow: 0 0 0 8px color-mix(in srgb, var(--derived-accent) 18%, transparent);
  content: '';
}

.insight-list,
.signal-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.insight-item,
.signal-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--panel-border);
}

.signal-item {
  flex-direction: column;
  gap: 10px;
}

.insight-item:last-child,
.signal-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.insight-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(148, 163, 184, 0.12);
  font-size: 18px;
}

.insight-item__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.insight-item__body strong,
.signal-item__top strong {
  color: var(--text-strong);
  font-size: 22px;
  line-height: 1.15;
}

.chart-surface {
  position: relative;
  z-index: 1;
  height: 320px;
}

.signal-item__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.signal-item__track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  //background: var(--track-bg);
}

.signal-item__track span {
  display: block;
  height: 100%;
  border-radius: inherit;
}

@media (max-width: 1400px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .detail-grid,
  .detail-grid--bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .overview-hero {
    grid-template-columns: 1fr;
  }

  .derived-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {

  .overview-hero,
  .detail-panel {
    padding: 22px 18px;
  }

  .overview-copy h2 {
    font-size: 28px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .signal-item__top {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
