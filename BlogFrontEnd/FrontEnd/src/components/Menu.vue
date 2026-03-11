<template>
    <div class="menu-inner">
        <ul class="menu-list">
            <li class="menu-item">
                <router-link to="/" active-class="active-link">首页</router-link>
            </li>
            <li class="menu-item">
                <router-link to="/categories" active-class="active-link">分类</router-link>
            </li>
            <li class="menu-item">
                <router-link to="/archive" active-class="active-link">归档</router-link>
            </li>
            <li class="menu-item">
                <router-link to="/about" active-class="active-link">关于我</router-link>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { useThemStore } from '../stores/them';

const themStore = useThemStore();
</script>

<style scoped lang="scss">
.menu-inner {
    min-width: 150px;
    background-color: v-bind('themStore.them.wavesColor1');
    border: 1px solid v-bind('themStore.them.cardBorder');
    border-radius: 12px;
    padding: 8px;
    box-shadow: v-bind('themStore.them.cardShadow');
    color: v-bind('themStore.them.textColor');

    .menu-list {
        list-style: none;
        padding: 0;
        margin: 0;

        .menu-item {
            border-radius: 8px;
            transition: all 0.25s ease;

            a,
            .search-btn {
                display: block;
                padding: 12px 16px;
                text-decoration: none;
                color: inherit;
                background: transparent;
                border: none;
                width: 100%;
                text-align: left;
                cursor: pointer;
                font-size: inherit;
            }

            &:hover {
                background-color: v-bind('themStore.them.menuBgHover');
            }

            &.search-item {
                margin-top: 8px;
                padding-top: 8px;
                border-top: 1px solid v-bind('themStore.them.cardBorder');
            }
        }
    }
}
</style>
