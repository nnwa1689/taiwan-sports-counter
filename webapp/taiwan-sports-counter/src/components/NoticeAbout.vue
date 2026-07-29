<template>
  <div class="notice-about-container">
    
    <!-- 1. 最新公告區塊 (純白無外框卡片 + 明顯灰線) -->
    <v-card class="ios-card mb-4 pa-2" elevation="0">
      <v-card-title class="d-flex align-center font-weight-bold text-slate-900 pb-2">
        <v-icon color="light-blue-darken-2" class="mr-2">mdi-bullhorn-outline</v-icon>
        最新公告
      </v-card-title>
      
      <v-divider class="ios-card-divider"></v-divider>

      <v-card-text class="pt-4">
        <v-timeline density="compact" align="start" class="ios-timeline">
          <v-timeline-item
            v-for="(notice, index) in notices"
            :key="index"
            :dot-color="notice.typeColor"
            size="x-small"
          >
            <div class="d-flex justify-space-between align-center mb-1">
              <span class="text-subtitle-2 font-weight-bold text-slate-900">{{ notice.title }}</span>
              <span class="text-caption text-grey-darken-1 font-weight-medium">{{ notice.date }}</span>
            </div>
            <div class="text-body-2 text-slate-700 line-height-relaxed">
              {{ notice.content }}
            </div>
          </v-timeline-item>
        </v-timeline>
      </v-card-text>
    </v-card>

    <!-- 2. 關於本站區塊 (純白無外框卡片 + 明顯灰線) -->
    <v-card class="ios-card pa-2" elevation="0">
      <v-card-title class="d-flex align-center font-weight-bold text-slate-900 pb-2">
        <v-icon color="light-blue-darken-2" class="mr-2">mdi-information-outline</v-icon>
        關於本站
      </v-card-title>

      <v-divider class="ios-card-divider"></v-divider>

      <v-card-text class="pt-4">
        <!-- 應用程式簡介與版本 -->
        <div class="d-flex align-center mb-4 pa-3 ios-sub-card rounded-xl">
          <v-avatar color="light-blue-darken-2" size="48" class="mr-3">
            <v-icon size="28" color="white">mdi-chart-donut</v-icon>
          </v-avatar>
          <div>
            <div class="text-subtitle-1 font-weight-bold text-slate-900">台灣運動中心即時人潮</div>
            <div class="text-caption text-grey-darken-1 font-weight-medium">版本 v1.2.3</div>
          </div>
        </div>

        <p class="text-body-2 text-slate-700 mb-4 leading-relaxed">
          本平台提供各縣市運動中心健身房與游泳池之即時人潮數據查詢，協助您避開擁擠高峰期，享有更好的運動體驗。數據每分鐘自動更新。
        </p>

        <v-divider class="ios-card-divider mb-4"></v-divider>

        <!-- 技術簡介標籤 -->
        <div class="text-caption font-weight-bold text-grey-darken-1 mb-2">使用技術與架構</div>
        <div class="d-flex flex-wrap gap-1 mb-4">
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">Vue 3</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">Vuetify 3</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">Vite</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">Axios</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">REST API</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">GEMINI</v-chip>
          <v-chip size="small" class="ios-tech-chip mr-1 mb-1">EXPRESS</v-chip>
        </div>

        <!-- 開發者與版權資訊 -->
        <div class="text-caption text-center text-grey-darken-1 pt-2">
          Designed & Developed by <a href="https://teddream.tw" target="_blank" class="font-weight-bold text-light-blue-darken-2">TEDDREAM</a>
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
    date: '2026-07-28',
    content: '新增嘉義、雲林等區，高雄新增楠梓、鼓山。',
    typeColor: 'light-blue-darken-2'
  },
  {
    title: '介面全面升級 iOS 規範',
    date: '2026-07-27',
    content: '採用 iOS 標準原生語彙，純白卡片襯托灰底與高對比線條。',
    typeColor: 'light-blue-darken-2'
  },
  {
    title: '新增區域篩選與我的最愛',
    date: '2026-07-27',
    content: '現在您可以跨縣市篩選運動中心，並透過愛心按鈕將常用的館區加入「我的最愛」快顯清單。',
    typeColor: 'light-blue-darken-2'
  },
  {
    title: '部分區域尚無法取得資料',
    date: '2026-07-28',
    content: '因受到官方網站系統政策影響，部分縣市運動中心的即時人潮資料無法取得，造成不便敬請見諒。（舞動陽光體系）',
    typeColor: 'amber-darken-2'
  }
])
</script>

<style scoped>
.notice-about-container {
  font-family: var(--app-font-family) !important;
}

/* 純白卡片，無外框線 */
.ios-card {
  background: #ffffff !important;
  border-radius: 16px !important;
  border: none !important;
  box-shadow: none !important;
}

/* 顯眼的分隔線 */
.ios-card-divider {
  border-color: #c6c6c8 !important;
  opacity: 1 !important;
}

/* 內嵌區塊 */
.ios-sub-card {
  background: #f2f2f7 !important;
  border: none !important;
}

/* 技術標籤 */
.ios-tech-chip {
  background: #f2f2f7 !important;
  color: #0284c7 !important;
  font-weight: 600 !important;
  border: none !important;
}

.ios-timeline :deep(.v-timeline-item__body) {
  padding-start: 12px !important;
}

/* ==========================================================================
   深色模式
   ========================================================================== */
@media (prefers-color-scheme: dark) {
  .ios-card {
    background: #1c1c1e !important;
    border: none !important;
  }

  .ios-card-divider {
    border-color: #545458 !important;
    opacity: 1 !important;
  }

  .ios-sub-card {
    background: #2c2c2e !important;
    border: none !important;
  }

  .text-slate-900 {
    color: #ffffff !important;
  }

  .text-slate-700,
  .text-grey-darken-1 {
    color: #8e8e93 !important;
  }

  .ios-tech-chip {
    background: #2c2c2e !important;
    color: #38bdf8 !important;
  }
}
</style>