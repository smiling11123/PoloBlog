<template>
    <div class="articleContainet">


        <div class="header">
            <ArticleHeader :title="articledetail?.data.title" :author="articledetail?.data.auth || '匿名作者'"
                :updateTime="articledetail?.data.updateTime" :viewCount="articledetail?.data.viewCount" />

        </div>
        <div class="content">
            <ArticleContent :content="articledetail?.data.content" />

        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getArticleDetail } from '~@/api/Article/ArticleDetail';
import { useRoute } from 'vue-router';
import ArticleHeader from './components/ArticleHeader.vue';
import ArticleContent from './components/ArticleContent.vue';

const route = useRoute()
const articledetail = ref()
const articlId = computed(() => route.params.id)

onMounted(async () => {
    console.log(articlId.value)
    articledetail.value = await getArticleDetail(articlId.value)
    console.log(articledetail.value)
})


</script>
<style scoped lang="scss">
    .content {
        margin-top: 20px;
    }
</style>