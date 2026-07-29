<template>
  <v-app class="ios-app">
    <!-- AppBar 頂部導覽列 (純白 30% 透明度無框 Header) -->
    <v-app-bar flat class="ios-bar px-2" density="comfortable">
      <v-app-bar-title class="font-weight-bold text-slate-900">
        <div class="d-flex align-center">
          <v-icon color="light-blue-darken-2" class="mr-2">mdi-chart-donut</v-icon>
          <span class="ios-title-text">即時人潮</span>
        </div>
      </v-app-bar-title>

      <!-- 區域篩選下拉選單 (iOS 26 30% 透明玻璃膠囊) -->
      <v-menu v-if="activeTab !== 'about'" location="bottom end" transition="scale-transition">
        <template v-slot:activator="{ props }">
          <button
            v-bind="props"
            class="ios-26-glass-btn ios-select-btn d-flex align-center justify-space-between px-3.5 py-1.5 mr-2"
          >
            <span class="text-caption font-weight-bold text-slate-900">{{ selectedArea }}</span>
            <v-icon size="16" color="slate-900" class="ml-1">mdi-chevron-down</v-icon>
          </button>
        </template>

        <v-list class="ios-glass-dropdown mt-2 pa-1.5" elevation="0">
          <v-list-item
            v-for="area in areas"
            :key="area"
            :value="area"
            :active="selectedArea === area"
            @click="selectedArea = area"
            class="ios-dropdown-item rounded-xl mb-1"
          >
            <v-list-item-title 
              class="text-caption font-weight-bold" 
              :class="selectedArea === area ? 'text-light-blue-darken-2' : 'text-slate-900'"
            >
              {{ area }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      
      <!-- 手動重整按鈕 (iOS 26 圓形玻璃按鈕) -->
      <button class="ios-26-glass-btn ios-icon-btn d-flex align-center justify-center mr-1" @click="fetchData" :disabled="loading">
        <v-icon color="light-blue-darken-2" size="20" :class="{ 'spin-animation': loading }">mdi-refresh</v-icon>
      </button>
    </v-app-bar>

    <!-- 主內容區 (預留底部空間避開懸浮 Dock) -->
    <v-main class="ios-main-content">
      <v-container class="pa-4 max-w-md">
        <!-- 頁面 3: 公告 & 關於 -->
        <NoticeAbout v-if="activeTab === 'about'" />

        <!-- 頁面 1 & 2: 列表與我的最愛 -->
        <template v-else>
          <!-- 更新時間提示區塊 -->
          <div v-if="lastUpdated" class="d-flex justify-end align-center mb-3 px-1">
            <div class="text-caption font-weight-bold text-slate-800 d-flex align-center">
              <v-icon size="14" color="slate-700" class="mr-1">mdi-clock-outline</v-icon>
              更新於：{{ lastUpdated }}
            </div>
          </div>

          <!-- 載入中動畫 -->
          <v-row v-if="loading && displayedCenters.length === 0" justify="center" class="my-12">
            <v-progress-circular indeterminate color="light-blue-darken-2" size="44" width="4"></v-progress-circular>
          </v-row>

          <!-- 空狀態提示 -->
          <div v-else-if="activeTab === 'favorite' && displayedCenters.length === 0" class="text-center py-12 ios-card pa-6 mt-4">
            <v-icon size="64" color="grey-lighten-1" class="mb-3">mdi-heart-outline</v-icon>
            <div class="text-h6 font-weight-bold text-slate-900">尚無收藏的運動中心</div>
            <div class="text-body-2 text-slate-700 mt-1">點擊卡片右上角愛心按鈕加入我的最愛</div>
          </div>

          <!-- 純白卡片列表 -->
          <v-card 
            v-else 
            v-for="center in displayedCenters" 
            :key="center.name" 
            class="ios-card mb-4 overflow-hidden" 
            elevation="0"
          >
            <v-card-item class="pb-3 pt-4">
              <v-card-title class="text-subtitle-1 font-weight-bold text-slate-900 d-flex justify-space-between align-center">
                <div class="d-flex align-center">
                  <span>{{ center.name }}運動中心</span>
                  <v-chip size="x-small" class="ml-2 ios-chip" variant="flat">
                    {{ center.area }}
                  </v-chip>
                </div>

                <!-- iOS 26 玻璃愛心按鈕 -->
                <button 
                  class="ios-26-card-glass-btn ios-heart-btn d-flex align-center justify-center"
                  @click.stop="toggleFavorite(center.name)"
                >
                  <v-icon size="18" :color="isFavorite(center.name) ? 'red-accent-3' : 'slate-700'">
                    {{ isFavorite(center.name) ? 'mdi-heart' : 'mdi-heart-outline' }}
                  </v-icon>
                </button>
              </v-card-title>
            </v-card-item>

            <!-- 明顯灰線區隔 -->
            <v-divider class="ios-card-divider"></v-divider>

            <v-card-text class="pt-4 pb-4" v-if="center.status === 'online'">
              <!-- 健身房 -->
              <div class="mb-4">
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColor(center.gym.current, center.gym.max)" class="mr-1.5">
                      mdi-dumbbell
                    </v-icon>
                    <span class="font-weight-bold text-slate-900">健身房</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.gym.current, center.gym.max)}`">
                      {{ center.gym.current }}
                    </span>
                    <span class="text-caption font-weight-semibold text-slate-700"> / {{ center.gym.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.gym.current / center.gym.max) * 100"
                  :color="getProgressColor(center.gym.current, center.gym.max)"
                  height="8"
                  rounded
                  class="ios-progress"
                ></v-progress-linear>
              </div>

              <!-- 游泳池 -->
              <div>
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColor(center.swim.current, center.swim.max)" class="mr-1.5">
                      mdi-pool
                    </v-icon>
                    <span class="font-weight-bold text-slate-900">游泳池</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.swim.current, center.swim.max)}`">
                      {{ center.swim.current }}
                    </span>
                    <span class="text-caption font-weight-semibold text-slate-700"> / {{ center.swim.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.swim.current / center.swim.max) * 100"
                  :color="getProgressColor(center.swim.current, center.swim.max)"
                  height="8"
                  rounded
                  class="ios-progress"
                ></v-progress-linear>
              </div>
            </v-card-text>

            <v-card-text v-else class="text-center text-slate-700 py-6">
              <v-icon color="error" class="mb-1">mdi-alert-circle-outline</v-icon>
              <div>資料載入失敗 {{ center.message ? `(${center.message})` : '' }}</div>
            </v-card-text>
          </v-card>
        </template>

      </v-container>
    </v-main>

    <!-- iOS 26 懸浮 Dock 導覽列 -->
    <div class="ios-26-dock-wrapper">
      <nav class="ios-26-dock">
        <button 
          class="ios-26-dock-item" 
          :class="{ 'is-active': activeTab === 'list' }"
          @click="activeTab = 'list'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20">mdi-format-list-bulleted</v-icon>
          </div>
          <span class="ios-dock-label">各區列表</span>
        </button>

        <button 
          class="ios-26-dock-item" 
          :class="{ 'is-active': activeTab === 'favorite' }"
          @click="activeTab = 'favorite'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20">
              {{ activeTab === 'favorite' ? 'mdi-heart' : 'mdi-heart-outline' }}
            </v-icon>
          </div>
          <span class="ios-dock-label">我的最愛</span>
        </button>

        <button 
          class="ios-26-dock-item" 
          :class="{ 'is-active': activeTab === 'about' }"
          @click="activeTab = 'about'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20">mdi-bullhorn-outline</v-icon>
          </div>
          <span class="ios-dock-label">公告&關於</span>
        </button>
      </nav>
    </div>
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
const areas = ['全部', '台北市', '新北市', '桃園市', '新竹市', '台中市', '彰化縣', '雲林縣', '嘉義市', '嘉義縣', '台南市', '高雄市']

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

  if (ratio < 50) return 'light-blue-darken-1'
  if (ratio <= 80) return 'amber-darken-2'
  return 'red-darken-1'
}

onMounted(() => {
  loadFavorites()
  fetchData()
  timer = setInterval(() => {
    fetchData()
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.ios-app {
  background-color: #f2f2f7 !important;
  min-height: 100vh;
  font-family: var(--app-font-family) !important;
}

.ios-main-content {
  padding-bottom: 140px !important;
}

/* 1. Header: 純白 30% 透明 */
.ios-bar {
  background: rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(25px) saturate(190%);
  /*-webkit-backdrop-filter: blur(25px) saturate(190%);*/
  border: none !important;
  box-shadow: none !important;
}

.ios-title-text {
  letter-spacing: -0.3px;
}

/* 2. 按鈕 (iOS 26 高光玻璃) */
.ios-26-glass-btn {
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(16px) saturate(180%);
  /*-webkit-backdrop-filter: blur(16px) saturate(180%);*/
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05), inset 0 1px 1px rgba(255, 255, 255, 0.9);
  transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
  cursor: pointer;
  outline: none;
}

.ios-26-glass-btn:active {
  transform: scale(0.92);
  background: rgba(255, 255, 255, 0.65);
}

.ios-select-btn {
  border-radius: 9999px;
  min-width: 92px;
}

.ios-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.ios-26-card-glass-btn {
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(242, 242, 247, 0.7);
  backdrop-filter: blur(12px) saturate(180%);
  /*-webkit-backdrop-filter: blur(12px) saturate(180%);*/
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.8), 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
  cursor: pointer;
  outline: none;
}

.ios-26-card-glass-btn:active {
  transform: scale(0.92);
  background: rgba(225, 225, 230, 0.85);
}

.ios-heart-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

/* 3. 下拉選單: 30% 透明玻璃 */
.ios-glass-dropdown {
  background: rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(25px) saturate(200%) !important;
  /*-webkit-backdrop-filter: blur(25px) saturate(200%) !important;*/
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  border-radius: 20px !important;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.1) !important;
  min-width: 130px;
}

.ios-dropdown-item {
  transition: all 0.15s ease !important;
  min-height: 38px !important;
}

.ios-dropdown-item:hover {
  background: rgba(255, 255, 255, 0.4) !important;
}

/* 卡片與內容 */
.ios-card {
  background: #ffffff !important;
  border-radius: 20px !important;
  border: none !important;
  box-shadow: none !important;
}

.ios-card-divider {
  border-color: #e5e5ea !important;
  opacity: 1 !important;
}

.ios-chip {
  background-color: #f2f2f7 !important;
  color: #0284c7 !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
  border: none !important;
}

.ios-progress {
  background-color: #e5e5ea !important;
}

/* 4. 懸浮 Dock 導覽列 */
.ios-26-dock-wrapper {
  position: fixed;
  bottom: 24px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  z-index: 1000;
  pointer-events: none;
}

.ios-26-dock {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px;
  width: calc(100% - 36px);
  max-width: 380px;
  height: 66px;
  border-radius: 36px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(35px) saturate(220%);
  /*-webkit-backdrop-filter: blur(35px) saturate(220%);*/
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.ios-26-dock-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  border-radius: 28px;
  border: none;
  background: transparent;
  color: #1c1c1e;
  transition: all 0.25s cubic-bezier(0.2, 0.8, 0.2, 1);
  cursor: pointer;
}

.ios-dock-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ios-dock-label {
  font-size: 11.5px;
  font-weight: 700;
  margin-top: 2px;
  letter-spacing: -0.2px;
}

.ios-26-dock-item:active {
  transform: scale(0.92);
}

.ios-26-dock-item.is-active {
  background: rgba(2, 132, 199, 0.15);
  color: #0284c7;
  border: 1px solid rgba(2, 132, 199, 0.25);
}

.ios-26-dock-item.is-active .ios-dock-icon-box {
  transform: translateY(-1px) scale(1.1);
}

.spin-animation {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 深色模式適配 */
@media (prefers-color-scheme: dark) {
  .ios-app {
    background-color: #000000 !important;
  }

  .ios-bar {
    background: rgba(28, 28, 30, 0.3) !important;
  }

  .ios-26-glass-btn {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.3);
  }

  .ios-26-glass-btn:active {
    background: rgba(255, 255, 255, 0.22);
  }

  .ios-26-card-glass-btn {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }

  .ios-glass-dropdown {
    background: rgba(30, 30, 32, 0.3) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
  }

  .ios-card {
    background: #1c1c1e !important;
  }

  .ios-card-divider {
    border-color: #38383a !important;
  }

  .ios-26-dock {
    background: rgba(30, 30, 32, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.15);
  }

  .ios-26-dock-item {
    color: #f2f2f7;
  }

  .ios-26-dock-item.is-active {
    background: rgba(56, 189, 248, 0.2);
    color: #38bdf8;
    border: 1px solid rgba(56, 189, 248, 0.35);
  }

  .text-slate-900,
  .text-slate-800 {
    color: #ffffff !important;
  }

  .text-slate-700 {
    color: #aeaeb2 !important; /* 已修正 Hex 碼長度 */
  }

  .ios-chip {
    background-color: #2c2c2e !important;
    color: #38bdf8 !important;
  }
}
</style>