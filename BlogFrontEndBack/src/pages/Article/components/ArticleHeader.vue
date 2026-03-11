<template>
    <div class="headerContainer">

        <div class="left">
            <div class="title">
                <h1>{{ title }}</h1>
            </div>
            <span>浏览量：{{ viewCount }}</span>
        </div>
        <div class="right">
            <div class="info">
                <span>作者：{{ author }}</span>
                <span>更新于：{{ formatTimer(updateTime) }}</span>
            </div>

        </div>
    </div>


</template>


<script setup lang="ts">
import { formatTimer } from '~@/utils/tools';


defineProps({
    title: String,
    author: String,
    updateTime: String,
    viewCount: Number,
})


</script>

<style scoped lang="scss">
.headerContainer {
    max-width: 920px;
    margin: 0 auto;
    padding: 40px 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.right {
    .info {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }
}
</style>