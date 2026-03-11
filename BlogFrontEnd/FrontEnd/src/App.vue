<template>
  <div class="app-container">
    <RouterView/>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useAppStore } from './stores/app';
import Login from './views/Login.vue';
import Header from './components/Header.vue';
import { useUserStore } from './stores/user';
import { useThemStore } from './stores/them';
import Menu from './components/Menu.vue';
import Topbar from './components/Topbar.vue';
import Content from './views/HomeView.vue';
import Waves from './components/Waves.vue';

const appStore = useAppStore()
const useStore = useUserStore()
const themStore = useThemStore()


</script>

<style lang="scss">

</style>
