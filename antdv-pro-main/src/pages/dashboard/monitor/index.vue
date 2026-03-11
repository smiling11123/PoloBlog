<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { getRecentArticleDaily } from '@/api/ArticleDaily'
import { Line } from '@antv/g2plot'
import { message } from 'ant-design-vue'

interface ArticleDailyStat {
  date: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
}

const articleId = ref('1')
const chartRef = ref<HTMLElement | null>(null)
let chart: Line | null = null
const loading = ref(false)
const hasChartData = ref(false)

// 专业配色方案：低饱和度，商务风格
const colorPalette = ['#5B8FF9', '#5AD8A6', '#F6BD16']

const renderChart = (rawData: ArticleDailyStat[]) => {
  if (chart) {
    chart.destroy()
    chart = null
  }

  hasChartData.value = rawData.length > 0

  if (!chartRef.value || rawData.length === 0)
    return

  // Sort by date ascending
  const chartData = [...rawData].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())

  const transformedData: Array<{ date: string, type: string, value: number }> = []
  chartData.forEach((item) => {
    transformedData.push({ date: item.date, type: '浏览量', value: item.viewCount || 0 })
    transformedData.push({ date: item.date, type: '点赞数', value: item.likeCount || 0 })
    transformedData.push({ date: item.date, type: '评论数', value: item.commentCount || 0 })
  })

  chart = new Line(chartRef.value, {
    data: transformedData,
    xField: 'date',
    yField: 'value',
    seriesField: 'type',
    smooth: true,
    color: colorPalette,
    lineStyle: {
      lineWidth: 2.5,
    },
    point: {
      size: 4,
      shape: 'circle',
      style: {
        lineWidth: 2,
        stroke: '#fff',
      },
    },
    tooltip: {
      showMarkers: true,
      domStyles: {
        'g2-tooltip': {
          background: 'rgba(255, 255, 255, 0.96)',
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
          borderRadius: '8px',
          border: '1px solid #f0f0f0',
          padding: '12px 16px',
        },
        'g2-tooltip-title': {
          color: '#262626',
          fontWeight: 600,
          marginBottom: '8px',
          fontSize: '13px',
        },
        'g2-tooltip-list-item': {
          color: '#595959',
          fontSize: '13px',
          marginBottom: '4px',
        },
      },
    },
    legend: {
      position: 'top-right',
      itemSpacing: 24,
      itemName: {
        style: {
          fill: '#595959',
          fontSize: 13,
          fontWeight: 500,
        },
      },
      marker: {
        symbol: 'circle',
        style: {
          r: 4,
        },
      },
    },
    xAxis: {
      grid: {
        line: {
          style: {
            stroke: '#f0f0f0',
            lineWidth: 1,
          },
        },
      },
      line: {
        style: {
          stroke: '#d9d9d9',
        },
      },
      tickLine: {
        style: {
          stroke: '#d9d9d9',
        },
      },
      label: {
        style: {
          fill: '#8c8c8c',
          fontSize: 12,
        },
      },
    },
    yAxis: {
      grid: {
        line: {
          style: {
            stroke: '#f0f0f0',
            lineWidth: 1,
          },
        },
      },
      line: null,
      tickLine: null,
      label: {
        style: {
          fill: '#8c8c8c',
          fontSize: 12,
        },
      },
    },
    animation: {
      appear: {
        animation: 'path-in',
        duration: 800,
        easing: 'ease-out',
      },
    },
  })
  
  chart.render()
}

const handleSearch = async () => {
  if (!articleId.value) {
    message.warning('请输入文章ID')
    return
  }
  loading.value = true
  try {
    const res: any = await getRecentArticleDaily(articleId.value)
    const rawData: ArticleDailyStat[] = res?.data || []

    await nextTick()
    renderChart(rawData)
  } catch (e) {
    console.error('获取文章数据失败', e)
    message.error('获取文章数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  handleSearch()
})

onUnmounted(() => {
  chart?.destroy()
})
</script>

<template>
  <div class="monitor-container">
    <a-card 
      title="文章数据监控" 
      :bordered="false" 
      class="chart-card"
      :body-style="{ padding: '24px' }"
    >
      <template #extra>
        <a-input-search
          v-model:value="articleId"
          placeholder="输入文章ID"
          enter-button="分析"
          class="search-input"
          @search="handleSearch"
        >
          <template #enterButton>
            <a-button type="primary" class="search-btn">
              分析
            </a-button>
          </template>
        </a-input-search>
      </template>

      <a-spin :spinning="loading" tip="加载中...">
        <div class="chart-wrapper">
          <div ref="chartRef" class="chart-container"></div>
          <a-empty 
            v-if="!loading && !hasChartData" 
            description="暂无数据"
            class="empty-state"
          />
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<style scoped lang="less">
.monitor-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03), 
              0 1px 6px -1px rgba(0, 0, 0, 0.02), 
              0 2px 4px 0 rgba(0, 0, 0, 0.02);
  background: #ffffff;
  
  :deep(.ant-card-head) {
    border-bottom: 1px solid #f0f0f0;
    padding: 0 24px;
    min-height: 56px;
    
    .ant-card-head-title {
      font-size: 16px;
      font-weight: 600;
      color: #262626;
      letter-spacing: 0.5px;
    }
  }
  
  :deep(.ant-card-extra) {
    padding: 12px 0;
  }
}

.search-input {
  width: 280px;
  
  :deep(.ant-input) {
    border-radius: 6px 0 0 6px;
    border-color: #d9d9d9;
    
    &:hover, &:focus {
      border-color: #5B8FF9;
    }
  }
  
  :deep(.ant-input-group-addon) {
    background: transparent;
    border: none;
    padding: 0;
  }
}

.search-btn {
  border-radius: 0 6px 6px 0;
  height: 32px;
  padding: 0 20px;
  font-weight: 500;
  background: #5B8FF9;
  border-color: #5B8FF9;
  box-shadow: 0 2px 0 rgba(91, 143, 249, 0.1);
  
  &:hover {
    background: #4a7de4;
    border-color: #4a7de4;
  }
}

.chart-wrapper {
  position: relative;
  margin-top: 8px;
}

.chart-container {
  height: 420px;
  width: 100%;
}

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #bfbfbf;
}

// 响应式适配
@media (max-width: 768px) {
  .monitor-container {
    padding: 16px;
  }
  
  .search-input {
    width: 200px;
  }
  
  .chart-container {
    height: 320px;
  }
}
</style>
