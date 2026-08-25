<template>
  <v-app class="ios-app">
    <!-- ==================== 1. 桌面端左側邊欄 (Desktop Sidebar) ==================== -->
    <v-navigation-drawer
      v-if="!$vuetify.display.smAndDown"
      permanent
      width="280"
      elevation="0"
      class="desktop-sidebar pa-4"
      :style="{ background: 'transparent !important' }"
    >
      <div class="d-flex flex-column h-100">
        <!-- 品牌標題區 -->
        <div class="d-flex align-center mb-6 px-2 pt-2">
          <img src="/logo.svg" alt="動潮 Logo" width="32" height="32" class="mr-3" />
          <div class="d-flex flex-column justify-center">
            <span class="ios-title-text text-h6 lh-1 mb-1 font-weight-bold text-slate-900">動潮</span>
            <span class="ios-subtitle-text text-caption text-grey-darken-1 font-weight-medium lh-1">台灣運動中心人潮</span>
          </div>
        </div>

        <!-- 縱向導覽功能選單 -->
        <div class="d-flex flex-column gap-2">
          <button 
            class="sidebar-nav-item d-flex align-center px-4 py-3" 
            :class="{ 'is-active': activeTab === 'list' }"
            @click="activeTab = 'list'"
          >
            <v-icon size="20" class="mr-3 nav-icon">mdi-format-list-bulleted</v-icon>
            <span class="font-weight-bold text-body-2 nav-label">各區列表</span>
          </button>

          <button 
            class="sidebar-nav-item d-flex align-center px-4 py-3" 
            :class="{ 'is-active': activeTab === 'favorite' }"
            @click="activeTab = 'favorite'"
          >
            <v-icon size="20" class="mr-3 nav-icon">
              {{ activeTab === 'favorite' ? 'mdi-heart' : 'mdi-heart-outline' }}
            </v-icon>
            <span class="font-weight-bold text-body-2 nav-label">我的最愛</span>
          </button>

          <button 
            class="sidebar-nav-item d-flex align-center px-4 py-3" 
            :class="{ 'is-active': activeTab === 'about' }"
            @click="activeTab = 'about'"
          >
            <v-icon size="20" class="mr-3 nav-icon">mdi-bullhorn-outline</v-icon>
            <span class="font-weight-bold text-body-2 nav-label">公告&關於</span>
          </button>
        </div>
      </div>
    </v-navigation-drawer>

    <!-- ==================== 2. 手機端頂部導覽列 (Mobile Top Bar) ==================== -->
    <v-app-bar 
      v-if="$vuetify.display.smAndDown" 
      flat 
      elevation="0"
      class="ios-bar px-2" 
      density="comfortable"
      :style="{ background: 'transparent !important' }"
    >
      <v-app-bar-title class="font-weight-bold text-slate-900">
        <div class="d-flex align-center">
          <img src="/logo.svg" alt="動潮 Logo" width="32" height="32" class="mr-3" />
          <div class="d-flex flex-column justify-center">
            <span class="ios-title-text text-subtitle-1 font-weight-bold lh-1 mb-0.5">動潮</span>
            <span class="ios-subtitle-text text-caption text-grey-darken-1 font-weight-medium lh-1">台灣運動中心人潮</span>
          </div>
        </div>
      </v-app-bar-title>

      <!-- 區域篩選下拉選單 (手機端) -->
      <v-menu v-if="activeTab !== 'about'" location="bottom end" transition="scale-transition">
        <template v-slot:activator="{ props }">
          <button
            v-bind="props"
            class="ios-glass-btn ios-select-btn d-flex align-center justify-space-between px-3.5 py-1.5 mr-2"
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
              :class="selectedArea === area ? 'ios-original-blue-text' : 'text-slate-900'"
            >
              {{ area }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      
      <!-- 手動重整按鈕 (手機端) -->
      <button class="ios-glass-btn ios-icon-btn d-flex align-center justify-center mr-1" @click="fetchData" :disabled="loading">
        <v-icon color="#007BBF" size="20" :class="{ 'spin-animation': loading }">mdi-refresh</v-icon>
      </button>
    </v-app-bar>

    <!-- ==================== 3. 主內容區 ==================== -->
    <v-main class="ios-main-content">
      <!-- 桌面端專用滿寬常駐控制列 -->
      <div 
        v-if="activeTab !== 'about'"
        class="sticky-top-header d-none d-md-flex align-center justify-space-between py-3 px-8 mb-4"
      >
        <!-- 桌面端控制按鈕 -->
        <div class="d-flex align-center gap-2">
          <v-menu location="bottom start" transition="scale-transition">
            <template v-slot:activator="{ props }">
              <button
                v-bind="props"
                class="ios-glass-btn ios-select-btn d-flex align-center justify-space-between px-3.5 py-1.5"
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
                  :class="selectedArea === area ? 'ios-original-blue-text' : 'text-slate-900'"
                >
                  {{ area }}
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>

          <button 
            class="ios-glass-btn ios-icon-btn d-flex align-center justify-center" 
            @click="fetchData" 
            :disabled="loading"
          >
            <v-icon color="#007BBF" size="20" :class="{ 'spin-animation': loading }">mdi-refresh</v-icon>
          </button>
        </div>

        <!-- 桌面端更新時間 -->
        <div v-if="lastUpdated" class="text-caption font-weight-bold text-slate-800 d-flex align-center">
          <v-icon size="14" color="slate-700" class="mr-1">mdi-clock-outline</v-icon>
          更新：{{ lastUpdated }}
        </div>
      </div>

      <v-container class="pa-4 max-w-md pt-0">
        <!-- 手機端最後更新時間提示 -->
        <div 
          v-if="lastUpdated && activeTab !== 'about'" 
          class="d-flex d-md-none align-center justify-end pt-2 pb-3 px-1 text-caption font-weight-bold text-slate-800"
        >
          <v-icon size="14" color="slate-700" class="mr-1">mdi-clock-outline</v-icon>
          <span>更新：{{ lastUpdated }}</span>
        </div>

        <!-- 頁面 3: 公告 & 關於 -->
        <NoticeAbout v-if="activeTab === 'about'" class="mt-4" />

        <!-- 頁面 1 & 2: 列表與我的最愛 -->
        <template v-else>
          <!-- 載入中動畫 -->
          <v-row v-if="loading && displayedCenters.length === 0" justify="center" class="my-12">
            <v-progress-circular indeterminate color="#007BBF" size="44" width="4"></v-progress-circular>
          </v-row>

          <!-- 空狀態提示 -->
          <div v-else-if="activeTab === 'favorite' && displayedCenters.length === 0" class="text-center py-12 ios-card elevation-0 pa-6 mt-4">
            <v-icon size="64" color="grey-lighten-1" class="mb-3">mdi-heart-outline</v-icon>
            <div class="text-h6 font-weight-bold text-slate-900">尚無收藏的運動中心</div>
            <div class="text-body-2 text-slate-700 mt-1">點擊卡片右上角愛心按鈕加入我的最愛</div>
          </div>

          <!-- 卡片列表（無陰影） -->
          <v-card 
            v-else 
            v-for="center in displayedCenters" 
            :key="center.name" 
            class="ios-card elevation-0 mb-4 overflow-hidden" 
            elevation="0"
          >
            <v-card-item class="pb-3 pt-4">
              <v-card-title class="text-subtitle-1 font-weight-bold text-slate-900 d-flex justify-space-between align-center">
                <div class="d-flex align-center">
                  <span>{{ center.name }}運動中心</span>
                  <v-chip size="x-small" class="ml-2 ios-chip" variant="flat">
                    {{ center.area }}
                    <template v-if="center.distance !== undefined && center.distance !== null">
                      • {{ center.distance.toFixed(1) }} km
                    </template>
                  </v-chip>
                </div>

                <button 
                  class="ios-card-btn ios-heart-btn d-flex align-center justify-center"
                  @click.stop="toggleFavorite(center.name)"
                >
                  <v-icon size="18" :color="isFavorite(center.name) ? 'red-accent-3' : 'slate-700'">
                    {{ isFavorite(center.name) ? 'mdi-heart' : 'mdi-heart-outline' }}
                  </v-icon>
                </button>
              </v-card-title>
            </v-card-item>

            <v-divider class="ios-card-divider"></v-divider>

            <v-card-text class="pt-4 pb-4" v-if="center.status === 'online'">
              <!-- 健身房 -->
              <div class="mb-4">
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColorHex(center.gym.current, center.gym.max)" class="mr-1.5">
                      mdi-dumbbell
                    </v-icon>
                    <span class="font-weight-bold text-slate-900">健身房</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :style="{ color: getProgressColorHex(center.gym.current, center.gym.max) }">
                      {{ center.gym.current }}
                    </span>
                    <span class="text-caption font-weight-semibold text-slate-700"> / {{ center.gym.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.gym.current / center.gym.max) * 100"
                  :color="getProgressColorHex(center.gym.current, center.gym.max)"
                  height="8"
                  rounded
                  class="ios-progress"
                ></v-progress-linear>
              </div>

              <!-- 游泳池 -->
              <div>
                <div class="d-flex justify-space-between align-center mb-1">
                  <div class="d-flex align-center">
                    <v-icon size="20" :color="getProgressColorHex(center.swim.current, center.swim.max)" class="mr-1.5">
                      mdi-pool
                    </v-icon>
                    <span class="font-weight-bold text-slate-900">游泳池</span>
                  </div>
                  <div>
                    <span class="text-h6 font-weight-black" :style="{ color: getProgressColorHex(center.swim.current, center.swim.max) }">
                      {{ center.swim.current }}
                    </span>
                    <span class="text-caption font-weight-semibold text-slate-700"> / {{ center.swim.max }} 人</span>
                  </div>
                </div>
                <v-progress-linear
                  :model-value="(center.swim.current / center.swim.max) * 100"
                  :color="getProgressColorHex(center.swim.current, center.swim.max)"
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

    <!-- ==================== 4. 手機端懸浮 Dock 導覽列 (Mobile Dock) ==================== -->
    <div v-if="$vuetify.display.smAndDown" class="ios-dock-wrapper">
      <nav class="ios-dock">
        <!-- 淺灰色選取項膠囊滑塊 -->
        <div class="ios-dock-pill" :style="pillStyle"></div>

        <button 
          ref="tabListRef"
          class="ios-dock-item" 
          :class="{ 'is-active': activeTab === 'list' }"
          @click="activeTab = 'list'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20" class="dock-icon">mdi-format-list-bulleted</v-icon>
          </div>
          <span class="ios-dock-label">各區列表</span>
        </button>

        <button 
          ref="tabFavoriteRef"
          class="ios-dock-item" 
          :class="{ 'is-active': activeTab === 'favorite' }"
          @click="activeTab = 'favorite'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20" class="dock-icon">
              {{ activeTab === 'favorite' ? 'mdi-heart' : 'mdi-heart-outline' }}
            </v-icon>
          </div>
          <span class="ios-dock-label">我的最愛</span>
        </button>

        <button 
          ref="tabAboutRef"
          class="ios-dock-item" 
          :class="{ 'is-active': activeTab === 'about' }"
          @click="activeTab = 'about'"
        >
          <div class="ios-dock-icon-box">
            <v-icon size="20" class="dock-icon">mdi-bullhorn-outline</v-icon>
          </div>
          <span class="ios-dock-label">公告&關於</span>
        </button>
      </nav>
    </div>
  </v-app>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import axios from 'axios'
import NoticeAbout from './components/NoticeAbout.vue'

const activeTab = ref('list')
const loading = ref(false)
const centers = ref([])
const lastUpdated = ref('')
const favorites = ref([])
const userLocation = ref(null)

const API_URL = import.meta.env.VITE_API_URL
let timer = null

const selectedArea = ref('全部')
const areas = ['全部', '台北市', '新北市', '桃園市', '新竹市', '台中市', '彰化縣', '雲林縣', '嘉義市', '嘉義縣', '台南市', '高雄市']
const FAVORITES_KEY = 'sports_center_favorites'

const tabListRef = ref(null)
const tabFavoriteRef = ref(null)
const tabAboutRef = ref(null)

const pillStyle = ref({
  width: '0px',
  transform: 'translateX(0px)',
  opacity: 0
})

const updatePill = () => {
  nextTick(() => {
    let targetEl = null
    if (activeTab.value === 'list') targetEl = tabListRef.value
    else if (activeTab.value === 'favorite') targetEl = tabFavoriteRef.value
    else if (activeTab.value === 'about') targetEl = tabAboutRef.value

    if (targetEl) {
      pillStyle.value = {
        width: `${targetEl.offsetWidth}px`,
        transform: `translateX(${targetEl.offsetLeft}px)`,
        opacity: 1
      }
    }
  })
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  document.documentElement.scrollTop = 0
  document.body.scrollTop = 0
}

watch(activeTab, () => {
  updatePill()
  nextTick(() => {
    scrollToTop()
  })
})

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

const getUserLocation = () => {
  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        userLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        }
      },
      (error) => {
        userLocation.value = null
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    )
  }
}

const extractCenterCoords = (center) => {
  if (center.lat !== undefined && center.lng !== undefined) {
    return { lat: Number(center.lat), lng: Number(center.lng) }
  }
  if (center.latitude !== undefined && center.longitude !== undefined) {
    return { lat: Number(center.latitude), lng: Number(center.longitude) }
  }
  return null
}

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  if (lat1 === null || lon1 === null || lat2 === null || lon2 === null || isNaN(lat1) || isNaN(lat2)) {
    return null
  }
  const R = 6371
  const dLat = (lat2 - lat1) * (Math.PI / 180)
  const dLon = (lon2 - lon1) * (Math.PI / 180)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

const displayedCenters = computed(() => {
  let list = [...centers.value]

  if (userLocation.value) {
    list = list.map(center => {
      const coords = extractCenterCoords(center)
      let distance = null

      if (coords) {
        distance = calculateDistance(
          userLocation.value.lat,
          userLocation.value.lng,
          coords.lat,
          coords.lng
        )
      }

      return { ...center, distance }
    })

    list.sort((a, b) => {
      if (a.distance === null || a.distance === undefined) return 1
      if (b.distance === null || b.distance === undefined) return -1
      return a.distance - b.distance
    })
  } else {
    list = list.map(center => ({ ...center, distance: undefined }))
  }

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

const getProgressColorHex = (current, max) => {
  if (!max || max === 0) return '#8E8E93'
  const ratio = (current / max) * 100

  if (ratio < 50) return '#007BBF'
  if (ratio <= 80) return '#FF9500'
  return '#FF3B30'
}

onMounted(() => {
  loadFavorites()
  getUserLocation()
  fetchData()
  updatePill()
  window.addEventListener('resize', updatePill)
  timer = setInterval(() => {
    fetchData()
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', updatePill)
})
</script>

<style scoped>
:root {
  --ios-blue: #007BBF;
}

.lh-1 {
  line-height: 1.1 !important;
}

.gap-2 {
  gap: 8px;
}

.ios-original-blue-text {
  color: #007BBF !important;
}

/* APP 底色 */
.ios-app {
  background-color: #f2f2f7 !important;
  background-image: none !important;
  min-height: 100vh;
  font-family: var(--app-font-family) !important;
  padding-top: env(safe-area-inset-top);
}

.ios-main-content {
  padding-bottom: 140px !important;
}

@media (min-width: 960px) {
  .ios-main-content {
    padding-bottom: 40px !important;
  }
}

/* 電腦版 Header */
.sticky-top-header {
  position: sticky;
  top: 0;
  z-index: 99;
  width: 100%;
  background: rgba(242, 242, 247, 0.05) !important;
  backdrop-filter: blur(4px) !important;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.05) !important;
}

/* 桌面端邊欄 */
:deep(.v-navigation-drawer),
.desktop-sidebar {
  background: rgba(242, 242, 247, 0.05) !important;
  backdrop-filter: blur(4px) !important;
  border-right: 0.5px solid rgba(0, 0, 0, 0.05) !important;
}

.sidebar-nav-item {
  width: 100%;
  border-radius: 12px;
  border: none;
  background: transparent;
  transition: all 0.2s ease;
  cursor: pointer;
}

.sidebar-nav-item .nav-icon {
  color: #8E8E93 !important;
}

.sidebar-nav-item .nav-label {
  color: #1C1C1E !important;
}

.sidebar-nav-item:hover {
  background: rgba(0, 0, 0, 0.04);
}

.sidebar-nav-item.is-active {
  background: rgba(0, 0, 0, 0.08) !important;
}

.sidebar-nav-item.is-active .nav-icon,
.sidebar-nav-item.is-active .nav-label {
  color: #007BBF !important;
}

/* Header (Mobile Top Bar) */
:deep(.v-app-bar),
.ios-bar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(242, 242, 247, 0.05) !important;
  backdrop-filter: blur(4px) !important;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.05) !important;
  box-shadow: none !important;
}

.ios-title-text {
  letter-spacing: -0.4px;
}

.ios-subtitle-text {
  font-size: 10px !important;
  letter-spacing: -0.1px;
}

/* 玻璃按鈕 */
.ios-glass-btn {
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  transition: all 0.15s ease;
  cursor: pointer;
  outline: none;
}

.ios-glass-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.25);
}

.ios-select-btn {
  border-radius: 9999px;
  min-width: 84px;
}

.ios-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.ios-card-btn {
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(0, 0, 0, 0.03);
  transition: all 0.15s ease;
  cursor: pointer;
  outline: none;
}

.ios-card-btn:active {
  transform: scale(0.92);
}

.ios-heart-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

/* 地區下拉選單 */
.ios-glass-dropdown {
  background: rgba(255, 255, 255, 0.08) !important;
  backdrop-filter: blur(4px) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06) !important;
  min-width: 130px;
}

.ios-dropdown-item {
  transition: all 0.15s ease !important;
  min-height: 38px !important;
}

.ios-dropdown-item:hover {
  background: rgba(0, 0, 0, 0.04) !important;
}

/* 卡片與內容（無陰影） */
.ios-card {
  background: #ffffff !important;
  border-radius: 20px !important;
  border: none !important;
  box-shadow: none !important;
  filter: none !important;
}

.ios-card-divider {
  border-color: #e5e5ea !important;
  opacity: 1 !important;
}

.ios-chip {
  background-color: #e5e5ea !important;
  color: #007BBF !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
  border: none !important;
}

.ios-progress {
  background-color: #e5e5ea !important;
}

/* ==================== 懸浮 Dock 導覽列 (Mobile Dock) ==================== */
.ios-dock-wrapper {
  position: fixed;
  bottom: 24px; /* 原為 24px，已稍微調高 */
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  z-index: 1000;
  pointer-events: none;
}

.ios-dock {
  pointer-events: auto;
  position: relative;
  display: flex;
  align-items: center;
  padding: 4px;
  width: fit-content;
  max-width: 290px;
  height: 60px;
  border-radius: 28px;
  background: rgba(242, 242, 247, 0.08);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 淺灰色膠囊滑塊 */
.ios-dock-pill {
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 0;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 22px;
  transition: transform 0.3s cubic-bezier(0.2, 1, 0.2, 1), 
              width 0.3s cubic-bezier(0.2, 1, 0.2, 1),
              opacity 0.2s ease;
  z-index: 1;
}

.ios-dock-item {
  position: relative;
  z-index: 2;
  flex: 1;
  min-width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  border-radius: 22px;
  border: none;
  background: transparent;
  transition: color 0.2s ease, transform 0.15s ease;
  cursor: pointer;
  user-select: none;
}

.ios-dock-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  transition: transform 0.2s ease;
}

.ios-dock-item .dock-icon {
  color: #8E8E93 !important;
}

.ios-dock-label {
  font-size: 11px;
  font-weight: 700;
  margin-top: 2px;
  letter-spacing: -0.2px;
  color: #1C1C1E !important;
}

.ios-dock-item:active {
  transform: scale(0.92);
}

.ios-dock-item.is-active .dock-icon,
.ios-dock-item.is-active .ios-dock-label {
  color: #007BBF !important;
}

.spin-animation {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ==================== 深色模式適配 (Dark Mode) ==================== */
@media (prefers-color-scheme: dark) {
  .ios-app {
    background-color: #000000 !important;
    background-image: none !important;
  }

  .ios-original-blue-text {
    color: #007BBF !important;
  }

  .sticky-top-header {
    background: rgba(0, 0, 0, 0.08) !important;
    backdrop-filter: blur(4px) !important;
    border-bottom: 0.5px solid rgba(255, 255, 255, 0.08) !important;
  }

  :deep(.v-navigation-drawer),
  .desktop-sidebar {
    background: rgba(0, 0, 0, 0.08) !important;
    backdrop-filter: blur(4px) !important;
    border-right: 0.5px solid rgba(255, 255, 255, 0.08) !important;
  }

  .sidebar-nav-item .nav-label {
    color: #FFFFFF !important;
  }

  .sidebar-nav-item.is-active {
    background: rgba(255, 255, 255, 0.15) !important;
  }

  .sidebar-nav-item.is-active .nav-icon,
  .sidebar-nav-item.is-active .nav-label {
    color: #007BBF !important;
  }

  :deep(.v-app-bar),
  .ios-bar {
    background: rgba(0, 0, 0, 0.08) !important;
    border-bottom: 0.5px solid rgba(255, 255, 255, 0.08) !important;
  }

  .ios-glass-btn {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .ios-glass-dropdown {
    background: rgba(0, 0, 0, 0.15) !important;
    backdrop-filter: blur(4px) !important;
    border: 1px solid rgba(255, 255, 255, 0.15) !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 2px 8px rgba(0, 0, 0, 0.2) !important;
  }

  .ios-card {
    background: #1c1c1e !important;
    box-shadow: none !important;
    filter: none !important;
  }

  .ios-card-divider {
    border-color: #38383a !important;
  }

  .ios-dock {
    background: rgba(0, 0, 0, 0.15);
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255, 255, 255, 0.15);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  .ios-dock-pill {
    background: rgba(255, 255, 255, 0.15);
  }

  .ios-dock-label {
    color: #FFFFFF !important;
  }

  .ios-dock-item.is-active .dock-icon,
  .ios-dock-item.is-active .ios-dock-label {
    color: #007BBF !important;
  }

  .text-slate-900,
  .text-slate-800 {
    color: #ffffff !important;
  }

  .text-slate-700 {
    color: #aeaeb2 !important;
  }

  .ios-chip {
    background-color: #2c2c2e !important;
    color: #007BBF !important;
  }
}
</style>