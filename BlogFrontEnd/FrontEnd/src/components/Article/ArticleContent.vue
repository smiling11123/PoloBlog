<template>
  <article class="article-content">
    <div class="markdown-body" v-html="htmlContent"></div>
  </article>
  
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css/github-markdown.css'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
// Import your theme store
import { useThemStore } from '@/stores/them'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

// Access the store
const themStore = useThemStore()

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang): any {
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
/* 1. Container Layout */
.article-content {
  max-width: 920px;
  margin: 0 auto;
  padding: 40px 20px;
  border-radius: 16px;
  
  /* Adapted: Use cardBg for container background */
  background-color: v-bind('themStore.them.cardBg');
  
  min-height: 50vh;
  border-radius: 4px;
  transition: background-color 0.3s;
  
  /* Add border for better visibility in light mode */
  border: 1px solid v-bind('themStore.them.cardBorder');
  box-shadow: v-bind('themStore.them.cardShadow');
}

/* 2. Global Style Corrections */
:deep(.markdown-body) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  border-radius: 16px;
  /* Force transparent background, text color from store */
  background-color: transparent !important; 
  color: v-bind('themStore.them.textColor') !important; /* Main body text */
}

/* 3. Heading Optimization */
:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3) {
  border-bottom: none;
  margin-top: 2rem;
  margin-bottom: 1rem;
  font-weight: 700;
  /* Use primary text color for headings */
  color: v-bind('themStore.them.textColor') !important;
}

/* 4. Image Optimization */
:deep(.markdown-body img) {
  max-width: 100%;
  border-radius: 16px;
  display: block;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid v-bind('themStore.them.cardBorder');
}

/* 5. Link Optimization */
:deep(.markdown-body a) {
  /* Use accent color for links */
  color: v-bind('themStore.them.textColor');
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s;
}

.plain-links :deep(.markdown-body a) {
  color: inherit !important;
  text-decoration: none !important;
  border-bottom: none !important;
}

:deep(.markdown-body a:hover) {
  border-bottom-color: v-bind('themStore.them.textColor');
}

/* 6. Blockquote Optimization */
:deep(.markdown-body blockquote) {
  color: v-bind('themStore.them.textTertiary');
  padding: 10px 20px;
  /* Use hover bg as a slightly different background for quotes */
  background-color: v-bind('themStore.them.cardBgHover');
  border-left: 4px solid v-bind('themStore.them.accentColor');
  border-radius: 4px;
  margin: 20px 0;
}

/* 7. Code Blocks (Keep Dark Theme) */
:deep(.markdown-body pre) {
  position: relative;
  background-color: #282c34; 
  border-radius: 16px;
  margin: 20px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden !important; 
  padding-top: 40px !important; 
}

/* Mac-style window buttons for code blocks */
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

/* 8. Table Optimization */
:deep(.markdown-body table) {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  margin-bottom: 20px;
  background-color: transparent !important;
}

:deep(.markdown-body table tr) {
  background-color: transparent !important;
  border-top: 1px solid v-bind('themStore.them.cardBorder') !important;
}

/* Zebra striping using hover bg */
:deep(.markdown-body table tr:nth-child(2n)) {
  background-color: v-bind('themStore.them.cardBgHover') !important;
}

:deep(.markdown-body table th) {
  font-weight: 600;
  /* Use widgetBg for table header background */
  background-color: v-bind('themStore.them.widgetBg') !important; 
  color: v-bind('themStore.them.textColor') !important; /* Header text */
  border: 1px solid v-bind('themStore.them.cardBorder') !important;
  padding: 12px 16px;
}

:deep(.markdown-body table td) {
  padding: 12px 16px;
  border: 1px solid v-bind('themStore.them.cardBorder') !important;
  background-color: transparent !important;
  color: v-bind('themStore.them.textSecondary') !important; /* Cell text */
}

:deep(.markdown-body table tr:hover) {
  /* Use headerInfoBg or just a slightly more opaque version of hover */
  background-color: v-bind('themStore.them.headerInfoBg') !important;
}
</style>
