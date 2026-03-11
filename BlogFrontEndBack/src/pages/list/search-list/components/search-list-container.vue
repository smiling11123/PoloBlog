<script lang="ts" setup>
const emit = defineEmits(['search'])

const searchText = ref('')

const handleSearch = () => {
  console.log('子组件1：准备把值抛给父组件', searchText.value)
  // 2. 触发事件：把输入框的值发出去
  emit('search', searchText.value)
}
</script>

<template>
  <page-container class="search-list-container">
    <template #content>
      <div class="flex items-center justify-center">
        <a-input-search
          v-model:value="searchText"
          placeholder="请输入"
          enter-button="搜索"
          @search="handleSearch"
          style="width: 300px"
        />
      </div>
    </template>
  </page-container>
</template>

<style lang="less">
.search-list-container {
  .ant-tabs-nav {
    margin-bottom: 0;
  }
  .ant-tabs-nav::before {
    border-bottom: none;
  }
}
</style>
