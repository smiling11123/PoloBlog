<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getHotCategoryList } from '~@/api/Category'

// 1. 定义向父组件发送的事件
const emit = defineEmits(['change'])

// 定义后端数据类型接口
interface CategoryItem {
  id: number
  name: string
  [key: string]: any
}

// 存储后端原始数据
const category = ref<CategoryItem[]>([])

// 加载数据
onMounted(async () => {
  const res = await getHotCategoryList()
  if (res.data) {
    category.value = res.data
  }
})

// 列表项类型
interface ListType {
  name: string
  key: string
}

// 2. 动态生成列表
const list = computed<ListType[]>(() => {
  return [
    { name: '全部', key: 'all' },
    ...category.value.map((v) => ({
      name: v.name,
      key: String(v.id),
    })),
  ]
})

// 当前选中的 key 数组
const activeList = ref<string[]>([])

// 3. 点击处理逻辑
function handleClick(item: ListType) {
  const key = item.key
  
  if (key === 'all') {
    if (activeList.value.includes('all')) {
      activeList.value = [] 
    } else {
      activeList.value = list.value.map(v => v.key) 
    }
    return
  }

  if (activeList.value.includes(key)) {
    activeList.value = activeList.value.filter(v => v !== key)
    if (activeList.value.includes('all')) {
      activeList.value = activeList.value.filter(v => v !== 'all')
    }
  } else {
    const newList = [...activeList.value, key]
    if (newList.length === list.value.length - 1) {
      newList.push('all')
    }
    activeList.value = newList
  }
}

// 4. 计算选中的真实 ID 数组
const selectedIds = computed(() => {
  return activeList.value.filter(key => key !== 'all')
})

// 5. 监听变化并通知父组件
watch(selectedIds, (newVal) => {
  emit('change', newVal)
})
</script>

<template>
  <a-card :bordered="false">
    <a-form>
      <a-form-item label="分类">
        <div class="flex flex-wrap gap-2">
          <a-tag
            v-for="item in list"
            :key="item.key"
            class="cursor-pointer" 
            :color="activeList.includes(item.key) ? '#108ee9' : ''"
            @click="handleClick(item)"
          >
            {{ item.name }}
          </a-tag>
        </div>
      </a-form-item>
      <a-divider dashed />
    </a-form>
  </a-card>
</template>

<style lang="less">
.category-other-item {
  .ant-form-item {
    margin-bottom: 0;
  }
}
.cursor-pointer {
  cursor: pointer;
}
</style>
