<template>
  <v-app class="glass-app">
    <!-- AppBar 頂部導覽列（Glass 毛玻璃效果） -->
    <v-app-bar flat class="glass-bar px-2" density="comfortable">
      <v-app-bar-title class="font-weight-bold text-slate-800">
        <div class="d-flex align-center">
          <v-icon color="light-blue-darken-1" class="mr-2 app-icon">mdi-chart-donut</v-icon>
          <span>即時人潮</span>
        </div>
      </v-app-bar-title>

      <!-- 區域篩選下拉選單 (僅在非「公告&關於」頁面顯示) -->
      <v-menu v-if="activeTab !== 'about'" location="bottom end" transition="slide-y-transition">
        <template v-slot:activator="{ props }">
          <button
            v-bind="props"
            class="glass-select-trigger d-flex align-center justify-space-between px-3 py-1.5 mr-2"
          >
            <span class="text-caption font-weight-bold text-light-blue-darken-3 dropdown-text">{{ selectedArea }}</span>
            <v-icon size="16" color="light-blue-darken-2" class="ml-1 dropdown-icon">mdi-chevron-down</v-icon>
          </button>
        </template>

        <v-list class="glass-dropdown-menu mt-2 pa-1.5" elevation="0">
          <v-list-item
            v-for="area in areas"
            :key="area"
            :value="area"
            :active="selectedArea === area"
            @click="selectedArea = area"
            class="glass-dropdown-item rounded-lg mb-1"
          >
            <v-list-item-title 
              class="text-caption font-weight-bold" 
              :class="selectedArea === area ? 'text-light-blue-darken-3' : 'text-slate-700'"
            >
              {{ area }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      
      <!-- 手動重整按鈕 -->
      <v-btn icon variant="text" class="glass-btn" @click="fetchData" :loading="loading">
        <v-icon color="light-blue-darken-2" class="refresh-icon">mdi-refresh</v-icon>
      </v-btn>
    </v-app-bar>

    <!-- 主內容區 -->
    <v-main class="pb-24 mb-10">
      <v-container class="pa-4 max-w-md">
        <!-- 頁面 3: 公告 & 關於 (獨立元件) -->
        <NoticeAbout v-if="activeTab === 'about'" />

        <!-- 頁面 1 & 2: 列表與我的最愛 -->
        <template v-else>
          <!-- 頁面內更新時間提示區塊 -->
          <div v-if="lastUpdated" class="d-flex justify-end align-center mb-3 px-1">
            <div class="text-caption font-weight-medium text-slate-600 d-flex align-center">
              <v-icon size="14" color="light-blue-darken-1" class="mr-1 clock-icon">mdi-clock-outline</v-icon>
              更新於：{{ lastUpdated }}
            </div>
          </div>

          <!-- 首次載入中動畫 -->
          <v-row v-if="loading && displayedCenters.length === 0" justify="center" class="my-12">
            <v-progress-circular indeterminate color="light-blue-darken-1" size="48" width="4"></v-progress-circular>
          </v-row>

          <!-- 我的最愛分頁空狀態提示 -->
          <div v-else-if="activeTab === 'favorite' && displayedCenters.length === 0" class="text-center py-12 glass-card pa-6 mt-4">
            <v-icon size="64" color="light-blue-lighten-2" class="mb-3">mdi-heart-outline</v-icon>
            <div class="text-h6 font-weight-bold text-slate-700">尚無收藏的運動中心</div>
            <div class="text-body-2 text-grey-darken-1 mt-1">點擊卡片右上角的愛心圖示，將常用的館區加入我的最愛</div>
          </div>

          <!-- 運動中心卡片列表 -->
          <v-card 
            v-else 
            v-for="center in displayedCenters" 
            :key="center.name" 
            class="glass-card mb-4 overflow-hidden" 
            elevation="0"
          >
            <v-card-item class="pb-2 pt-4">
              <v-card-title class="text-subtitle-1 font-weight-bold text-slate-800 d-flex justify-space-between align-center">
                <div class="d-flex align-center">
                  <span>{{ center.name }}運動中心</span>
                  <!-- 地區標籤 -->
                  <v-chip size="x-small" class="ml-2 glass-chip" variant="flat">
                    {{ center.area }}
                  </v-chip>
                </div>

                <!-- 我的最愛按鈕 (愛心) -->
                <v-btn 
                  icon 
                  variant="text" 
                  size="small" 
                  class="glass-btn-icon" 
                  @click.stop="toggleFavorite(center.name)"
                >
                  <v-icon :color="isFavorite(center.name) ? 'red-accent-3' : 'grey-lighten-1'">
                    {{ isFavorite(center.name) ? 'mdi-heart' : 'mdi-heart-outline' }}
                  </v-icon>
                </v-btn>
              </v-card-title>
            </v-card-item>

            <v-card-text class="pt-2 pb-4" v-if="center.status === 'online'">
              <!-- 項目 1: 健身房 -->
              <div class="mb-4">
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColor(center.gym.current, center.gym.max)" class="mr-1.5">
                      mdi-dumbbell
                    </v-icon>
                    <span class="font-weight-bold text-slate-700">健身房</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.gym.current, center.gym.max)}`">
                      {{ center.gym.current }}
                    </span>
                    <span class="text-caption font-weight-medium text-grey-darken-1"> / {{ center.gym.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.gym.current / center.gym.max) * 100"
                  :color="getProgressColor(center.gym.current, center.gym.max)"
                  height="8"
                  rounded
                  class="glass-progress"
                ></v-progress-linear>
              </div>

              <!-- 項目 2: 游泳池 -->
              <div>
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColor(center.swim.current, center.swim.max)" class="mr-1.5">
                      mdi-pool
                    </v-icon>
                    <span class="font-weight-bold text-slate-700">游泳池</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.swim.current, center.swim.max)}`">
                      {{ center.swim.current }}
                    </span>
                    <span class="text-caption font-weight-medium text-grey-darken-1"> / {{ center.swim.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.swim.current / center.swim.max) * 100"
                  :color="getProgressColor(center.swim.current, center.swim.max)"
                  height="8"
                  rounded
                  class="glass-progress"
                ></v-progress-linear>
              </div>
            </v-card-text>

            <!-- 斷線或錯誤處理 -->
            <v-card-text v-else class="text-center text-grey-darken-1 py-6">
              <v-icon color="error" class="mb-1">mdi-alert-circle-outline</v-icon>
              <div>資料載入失敗 {{ center.message ? `(${center.message})` : '' }}</div>
            </v-card-text>
          </v-card>
        </template>

      </v-container>
    </v-main>

    <!-- Bottom Navigation 懸浮 Dock 導覽列 -->
    <v-bottom-navigation v-model="activeTab" class="glass-nav" active color="light-blue-darken-2" grow>
      <v-btn value="list" class="nav-btn">
        <v-icon size="20">mdi-format-list-bulleted</v-icon>
        <span class="text-caption font-weight-bold mt-1">各區列表</span>
      </v-btn>
      <v-btn value="favorite" class="nav-btn">
        <v-icon size="20" :color="activeTab === 'favorite' ? 'red-accent-3' : ''">
          {{ activeTab === 'favorite' ? 'mdi-heart' : 'mdi-heart-outline' }}
        </v-icon>
        <span class="text-caption font-weight-bold mt-1">我的最愛</span>
      </v-btn>
      <v-btn value="about" class="nav-btn">
        <v-icon size="20">mdi-bullhorn-outline</v-icon>
        <span class="text-caption font-weight-bold mt-1">公告&關於</span>
      </v-btn>
    </v-bottom-navigation>
  </v-app>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'
import NoticeAbout from './components/NoticeAbout.vue'

const activeTab = ref('list')
const loading = ref(false)
const centers = ref([])
const lastUpdated = ref('')
const favorites = ref([])

const API_URL = import.meta.env.VITE_API_URL
let timer = null

const selectedArea = ref('全部')
const areas = ['全部', '台北市', '新北市', '桃園市', '新竹市', '台中市', '彰化縣', '高雄市']

const FAVORITES_KEY = 'sports_center_favorites'

const loadFavorites = () => {
  try {
    const saved = localStorage.getItem(FAVORITES_KEY)
    favorites.value = saved ? JSON.parse(saved) : []
  } catch (e) {
    console.error('無法讀取我的最愛設定:', e)
    favorites.value = []
  }
}

const isFavorite = (name) => {
  return favorites.value.includes(name)
}

const toggleFavorite = (name) => {
  if (isFavorite(name)) {
    favorites.value = favorites.value.filter(item => item !== name)
  } else {
    favorites.value.push(name)
  }
  localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites.value))
}

const displayedCenters = computed(() => {
  let list = centers.value

  if (activeTab.value === 'favorite') {
    list = list.filter(center => isFavorite(center.name))
  }

  if (selectedArea.value !== '全部') {
    list = list.filter(center => center.area === selectedArea.value)
  }

  return list
})

const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(API_URL)
    centers.value = response.data
    
    const now = new Date()
    lastUpdated.value = now.toLocaleTimeString('zh-TW', { 
      hour: '2-digit', 
      minute: '2-digit', 
      second: '2-digit' 
    })
  } catch (error) {
    console.error('無法取得運動中心資料:', error)
  } finally {
    loading.value = false
  }
}

const getProgressColor = (current, max) => {
  if (!max || max === 0) return 'grey'
  
  const ratio = (current / max) * 100

  if (ratio < 50) {
    return 'light-blue-darken-1'  // 改為天藍色 (正常)
  } else if (ratio <= 80) {
    return 'amber-darken-2'       // 橘黃色 (稍多)
  } else {
    return 'red-darken-1'         // 紅色 (擁擠)
  }
}

onMounted(() => {
  loadFavorites()
  fetchData()
  timer = setInterval(() => {
    fetchData()
  }, 60000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
/* App 動態柔和漸層背景 (天藍背景) */
.glass-app {
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 40%, #e0e7ff 100%) !important;
  min-height: 100vh;
  font-family: var(--app-font-family) !important;
}

/* 頂部 Header Glass 效果 */
.glass-bar {
  background: rgba(255, 255, 255, 0.65) !important;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5) !important;
}

/* iOS Liquid Glass 下拉選單觸發按鈕 */
.glass-select-trigger {
  background: rgba(255, 255, 255, 0.65) !important;
  backdrop-filter: blur(20px) saturate(180%) !important;
  -webkit-backdrop-filter: blur(20px) saturate(180%) !important;
  border-radius: 20px !important;
  border: 1px solid rgba(255, 255, 255, 0.85) !important;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03),
              0 1px 0 rgba(255, 255, 255, 0.9) inset !important;
  min-width: 95px;
  cursor: pointer;
  outline: none;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.glass-select-trigger:hover {
  background: rgba(255, 255, 255, 0.85) !important;
  transform: translateY(-1px);
}

.glass-select-trigger:active {
  transform: scale(0.96);
}

/* 下拉彈出選單毛玻璃 */
.glass-dropdown-menu {
  background: rgba(255, 255, 255, 0.72) !important;
  backdrop-filter: blur(30px) saturate(220%) !important;
  -webkit-backdrop-filter: blur(30px) saturate(220%) !important;
  border-radius: 20px !important;
  border: 1px solid rgba(255, 255, 255, 0.85) !important;
  box-shadow: 0 15px 35px -5px rgba(15, 23, 42, 0.15),
              0 0 10px rgba(255, 255, 255, 0.5) inset !important;
  min-width: 125px;
}

.glass-dropdown-item {
  transition: all 0.2s ease !important;
  min-height: 36px !important;
}

.glass-dropdown-item:hover {
  background: rgba(2, 132, 199, 0.12) !important;
}

.glass-dropdown-item.v-list-item--active {
  background: rgba(2, 132, 199, 0.2) !important;
}

/* 卡片 Glass 效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(25px) saturate(190%);
  -webkit-backdrop-filter: blur(25px) saturate(190%);
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05),
              0 0 10px 0 rgba(255, 255, 255, 0.5) inset !important;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.08) !important;
}

/* 膠囊標籤 (天藍色質感) */
.glass-chip {
  background: rgba(2, 132, 199, 0.15) !important;
  color: #0369a1 !important;
  font-weight: 600 !important;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(2, 132, 199, 0.3) !important;
}

/* 進度條底色潤飾 */
.glass-progress {
  background-color: rgba(0, 0, 0, 0.05) !important;
}

/* 愛心與重整按鈕 */
.glass-btn-icon {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.glass-btn-icon:active {
  transform: scale(0.85);
}

/* iOS Floating Liquid Glass Dock 導覽列 */
.glass-nav {
  position: fixed !important;
  bottom: 20px !important;
  left: 50% !important;
  transform: translateX(-50%) !important;
  width: calc(100% - 32px) !important;
  max-width: 420px !important;
  height: 66px !important;
  border-radius: 36px !important;
  padding: 0 6px !important;
  
  background: rgba(255, 255, 255, 0.68) !important;
  backdrop-filter: blur(30px) saturate(220%) !important;
  -webkit-backdrop-filter: blur(30px) saturate(220%) !important;
  
  border: 1px solid rgba(255, 255, 255, 0.85) !important;
  box-shadow: 
    0 20px 40px -10px rgba(15, 23, 42, 0.15),
    0 6px 16px rgba(0, 0, 0, 0.03),
    0 1px 0 rgba(255, 255, 255, 0.9) inset !important;
  
  z-index: 1000;
  display: flex !important;
  align-items: center !important;
}

.glass-nav :deep(.v-btn) {
  border-radius: 28px !important;
  margin: 0 2px !important;
  height: 50px !important;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
  background: transparent !important;
}

.glass-nav :deep(.v-btn:active) {
  transform: scale(0.92) !important;
}

.glass-nav :deep(.v-btn--active) {
  background: rgba(2, 132, 199, 0.12) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(2, 132, 199, 0.2) !important;
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.1) !important;
}

/* ==========================================================================
   系統深色模式 (Dark Mode) 自動切換
   ========================================================================== */
@media (prefers-color-scheme: dark) {
  .glass-app {
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%) !important;
  }

  .glass-bar {
    background: rgba(15, 23, 42, 0.75) !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  .glass-select-trigger,
  .glass-card,
  .glass-dropdown-menu,
  .glass-nav {
    background: rgba(15, 23, 42, 0.65) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.3),
                0 0 10px 0 rgba(255, 255, 255, 0.05) inset !important;
  }

  .text-slate-800,
  .text-slate-700 {
    color: #f1f5f9 !important;
  }

  .text-slate-600,
  .text-grey-darken-1 {
    color: #94a3b8 !important;
  }

  .text-light-blue-darken-3,
  .dropdown-text,
  .app-icon,
  .dropdown-icon,
  .refresh-icon,
  .clock-icon {
    color: #38bdf8 !important;
  }

  .glass-chip {
    background: rgba(56, 189, 248, 0.18) !important;
    color: #38bdf8 !important;
    border: 1px solid rgba(56, 189, 248, 0.35) !important;
  }

  .glass-dropdown-item:hover {
    background: rgba(56, 189, 248, 0.15) !important;
  }

  .glass-dropdown-item.v-list-item--active {
    background: rgba(56, 189, 248, 0.25) !important;
  }

  .glass-nav :deep(.v-btn--active) {
    background: rgba(56, 189, 248, 0.18) !important;
    border: 1px solid rgba(56, 189, 248, 0.3) !important;
    color: #38bdf8 !important;
  }

  .glass-btn-icon {
    background: rgba(255, 255, 255, 0.1) !important;
  }
}
</style>