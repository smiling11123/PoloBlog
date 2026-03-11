<template>
  <article class="article-content">
    <div class="markdown-body" v-html="htmlContent"></div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css/github-markdown-light.css'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import { useAppStore } from "@/stores/app"
import { theme } from 'ant-design-vue'

const { useToken } = theme
const { token } = useToken()
const themStore = useAppStore()
const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

const htmlContent = computed(() => {
  return md.render(props.content || '')
})
</script>
<style scoped lang="scss">
/* 1. 容器布局 */
.article-content {
  max-width: 920px;
  margin: 0 auto;
  padding: 40px 20px;
  /* 使用 token.xxx */
  background-color: v-bind('token.colorBgContainer');
  min-height: 50vh;
  border-radius: 4px;
  transition: background-color 0.3s;
}

/* 2. 全局样式修正 */
:deep(.markdown-body) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  
  /* 强制背景透明，文字颜色取自 Token */
  background-color: transparent !important; 
  color: v-bind('token.colorText') !important;
}

/* 3. 标题优化 */
:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3) {
  border-bottom: none;
  margin-top: 2rem;
  margin-bottom: 1rem;
  font-weight: 700;
  color: v-bind('token.colorTextHeading') !important;
}

/* 4. 图片优化 */
:deep(.markdown-body img) {
  max-width: 100%;
  border-radius: 8px;
  display: block;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid v-bind('token.colorBorder');
}

/* 5. 链接优化 */
:deep(.markdown-body a) {
  color: v-bind('token.colorPrimary');
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s;
}
:deep(.markdown-body a:hover) {
  border-bottom-color: v-bind('token.colorPrimary');
}

/* 6. 引用块优化 */
:deep(.markdown-body blockquote) {
  color: v-bind('token.colorTextSecondary');
  padding: 10px 20px;
  background-color: v-bind('token.colorFillQuaternary');
  border-left: 4px solid v-bind('token.colorPrimary');
  border-radius: 4px;
  margin: 20px 0;
}

/* 7. 代码块 (保持深色) */
:deep(.markdown-body pre) {
  position: relative;
  background-color: #282c34; 
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden !important; 
  padding-top: 40px !important; 
}
:deep(.markdown-body pre)::before {
  content: " ";
  position: absolute;
  top: 15px; left: 15px;
  width: 12px; height: 12px;
  border-radius: 50%;
  background: #fc625d; 
  box-shadow: 20px 0 0 #fdbc40, 40px 0 0 #35cd4b; 
  z-index: 10;
}
:deep(.markdown-body pre code) {
  display: block;
  overflow-x: auto;
  padding: 0 15px 15px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 14px;
  color: #abb2bf;
  width: 100%;
}
:deep(.markdown-body pre code::-webkit-scrollbar) { height: 8px; }
:deep(.markdown-body pre code::-webkit-scrollbar-thumb) {
  background-color: #4b5263;
  border-radius: 4px;
}
:deep(.markdown-body pre code::-webkit-scrollbar-track) { background-color: transparent; }
:deep(.markdown-body table) {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  margin-bottom: 20px;
  background-color: transparent !important;
}


:deep(.markdown-body table tr) {
  background-color: transparent !important;
  border-top: 1px solid v-bind('token.colorBorderSecondary') !important;
}


:deep(.markdown-body table tr:nth-child(2n)) {
  background-color: v-bind('token.colorFillQuaternary') !important;
}

:deep(.markdown-body table th) {
  font-weight: 600;

  background-color: v-bind('token.colorFillAlter') !important; 
  color: v-bind('token.colorTextHeading') !important; /* 表头字更亮 */
  border: 1px solid v-bind('token.colorBorderSecondary') !important;
  padding: 12px 16px;
}

:deep(.markdown-body table td) {
  padding: 12px 16px;
  border: 1px solid v-bind('token.colorBorderSecondary') !important;
  background-color: transparent !important; /* 确保奇数行透明 */
  color: v-bind('token.colorText') !important; /* 普通字颜色 */
}


:deep(.markdown-body table tr:hover) {

  background-color: v-bind('token.colorFillTertiary') !important;
}

</style>
