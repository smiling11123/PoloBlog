<script setup lang="ts">
import SearchListContainer from './components/search-list-container.vue'
import Article from './articles.vue'
defineOptions({
  name: 'SearchList',
})

const searchKeyword = ref('')

// 2. 接收 SearchListContainer 传来的值
const onGetSearchValue = (val: string) => {
  console.log('父组件：收到了搜索词', val)
  // 把值存起来，会自动传递给 Article
  searchKeyword.value = val
}


</script>

<template>
  <div class="container">
  <SearchListContainer @search="onGetSearchValue"/>
   
  <Article :keyword="searchKeyword"/>
  </div>
</template>

<style scoped lang="less">

</style>
