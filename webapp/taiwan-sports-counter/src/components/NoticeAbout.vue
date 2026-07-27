<template>
  <div class="notice-about-container">
    
    <!-- 1. 最新公告區塊 -->
    <v-card class="glass-card mb-5 pa-2" elevation="0">
      <v-card-title class="d-flex align-center font-weight-bold text-slate-800 pb-1">
        <v-icon color="light-blue-darken-1" class="mr-2 notice-title-icon">mdi-bullhorn-outline</v-icon>
        最新公告
      </v-card-title>
      
      <v-card-text class="pt-2">
        <v-timeline density="compact" align="start" class="glass-timeline">
          <v-timeline-item
            v-for="(notice, index) in notices"
            :key="index"
            :dot-color="notice.typeColor"
            size="x-small"
          >
            <div class="d-flex justify-space-between align-center mb-1">
              <span class="text-subtitle-2 font-weight-bold text-slate-800">{{ notice.title }}</span>
              <span class="text-caption text-grey-darken-1 font-weight-medium">{{ notice.date }}</span>
            </div>
            <div class="text-body-2 text-slate-600 line-height-relaxed">
              {{ notice.content }}
            </div>
          </v-timeline-item>
        </v-timeline>
      </v-card-text>
    </v-card>

    <!-- 2. 關於作者與本站區塊 -->
    <v-card class="glass-card pa-2" elevation="0">
      <v-card-title class="d-flex align-center font-weight-bold text-slate-800 pb-1">
        <v-icon color="light-blue-darken-1" class="mr-2 about-title-icon">mdi-information-outline</v-icon>
        關於本站
      </v-card-title>

      <v-card-text class="pt-2">
        <!-- 應用程式簡介與版本 -->
        <div class="d-flex align-center mb-4 pa-3 glass-sub-card rounded-xl">
          <v-avatar color="light-blue-darken-1" size="48" class="mr-3 glass-avatar">
            <v-icon size="28" color="white">mdi-chart-donut</v-icon>
          </v-avatar>
          <div>
            <div class="text-subtitle-1 font-weight-bold text-slate-800">台灣運動中心即時人潮</div>
            <div class="text-caption text-grey-darken-1 font-weight-medium">版本 v1.2.1</div>
          </div>
        </div>

        <p class="text-body-2 text-slate-700 mb-4 leading-relaxed">
          本平台提供各縣市運動中心健身房與游泳池之即時人潮數據查詢，協助您避開擁擠高峰期，享有更好的運動體驗。數據每分鐘自動更新。
        </p>

        <v-divider class="my-4 border-dashed"></v-divider>

        <!-- 技術簡介標籤 -->
        <div class="text-caption font-weight-bold text-slate-500 mb-2">使用技術與架構</div>
        <div class="d-flex flex-wrap gap-1 mb-4">
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">Vue 3</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">Vuetify 3</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">Vite</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">Axios</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">REST API</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">GEMINI</v-chip>
          <v-chip size="small" class="glass-tech-chip mr-1 mb-1">EXPRESS</v-chip>
        </div>

        <!-- 開發者與版權資訊 -->
        <div class="text-caption text-center text-grey-darken-1 pt-2">
          Designed & Developed by <a href="https://teddream.tw" target="_blank" class="font-weight-bold text-light-blue-darken-2 author-text">TEDDREAM</a>
          <br>
          © 2026 All Rights Reserved.
        </div>
      </v-card-text>
    </v-card>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const notices = ref([
  {
    title: '視覺風格更新',
    date: '2026-07-27',
    content: '全新上線 Liquid Glass 毛玻璃介面，優化下拉選單、卡片與懸浮導覽列之視覺透光體驗。',
    typeColor: 'light-blue-darken-1'
  },
  {
    title: '新增區域篩選與我的最愛',
    date: '2026-07-27',
    content: '現在您可以跨縣市篩選運動中心，並透過愛心按鈕將常用的館區加入「我的最愛」快顯清單。',
    typeColor: 'light-blue-darken-1'
  },
  {
    title: '部分區域尚無法取得資料',
    date: '2026-07-27',
    content: '因受到官方網站系統政策影響，部分縣市運動中心的即時人潮資料無法取得，造成不便敬請見諒。（高雄全部，台北中正，新北泰山、淡水、新莊、蘆洲）',
    typeColor: 'amber-darken-2'
  }
])
</script>

<style scoped>
/* Glass 卡片通用樣式 (淺色) */
.glass-card {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(25px) saturate(190%);
  -webkit-backdrop-filter: blur(25px) saturate(190%);
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05),
              0 0 10px 0 rgba(255, 255, 255, 0.5) inset !important;
}

/* 內嵌小卡片 */
.glass-sub-card {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(10px);
}

.glass-avatar {
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.25);
}

/* 技術標籤 (天藍色) */
.glass-tech-chip {
  background: rgba(2, 132, 199, 0.12) !important;
  color: #0369a1 !important;
  font-weight: 600 !important;
  border: 1px solid rgba(2, 132, 199, 0.2) !important;
}

/* 時間軸線條微調 */
.glass-timeline :deep(.v-timeline-item__body) {
  padding-start: 12px !important;
}

/* ==========================================================================
   深色模式適應
   ========================================================================== */
@media (prefers-color-scheme: dark) {
  .glass-card {
    background: rgba(15, 23, 42, 0.65) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.3) !important;
  }

  .glass-sub-card {
    background: rgba(30, 41, 59, 0.6) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  .text-slate-800,
  .text-slate-700 {
    color: #f1f5f9 !important;
  }

  .text-slate-600,
  .text-slate-500,
  .text-grey-darken-1 {
    color: #94a3b8 !important;
  }

  .notice-title-icon,
  .about-title-icon,
  .author-text {
    color: #38bdf8 !important;
  }

  .glass-tech-chip {
    background: rgba(56, 189, 248, 0.15) !important;
    color: #38bdf8 !important;
    border: 1px solid rgba(56, 189, 248, 0.3) !important;
  }
}
</style>