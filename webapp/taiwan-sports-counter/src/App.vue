<template>
  <v-app>
    <!-- AppBar 頂部導覽列 -->
    <v-app-bar color="teal-darken-1" elevation="2" density="compact">
      <v-app-bar-title class="font-weight-bold">
        台北運動中心即時人潮
        <!-- 頂部顯示最新更新時間 -->
        <div class="text-caption font-weight-regular text-teal-lighten-4 mt-n1" v-if="lastUpdated">
          更新於：{{ lastUpdated }}
        </div>
      </v-app-bar-title>
      
      <!-- 手動重整按鈕 -->
      <v-btn icon @click="fetchData" :loading="loading">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
    </v-app-bar>

    <!-- 主內容區 -->
    <v-main class="bg-grey-lighten-4">
      <v-container class="pa-4">
        
        <!-- 首次載入中動畫 -->
        <v-row v-if="loading && centers.length === 0" justify="center" class="my-8">
          <v-progress-circular indeterminate color="teal"></v-progress-circular>
        </v-row>

        <!-- 運動中心卡片列表 -->
        <v-card v-else v-for="center in centers" :key="center.name" class="mb-4" rounded="lg" elevation="2">
          <v-card-item class="bg-teal-lighten-5">
            <v-card-title class="text-subtitle-1 font-weight-bold text-teal-darken-4">
              {{ center.name }}運動中心
            </v-card-title>
          </v-card-item>

          <v-card-text class="pt-4" v-if="center.status === 'online'">
            <!-- 項目 1: 健身房 -->
            <div class="mb-4">
              <div class="d-flex justify-space-between align-center mb-1">
                <div>
                  <!-- 讓 Icon 的顏色也跟著變，視覺整體感更強 -->
                  <v-icon :color="getProgressColor(center.gym.current, center.gym.max)" class="mr-1">
                    mdi-weight-lifter
                  </v-icon>
                  <span class="font-weight-medium">健身房</span>
                </div>
                <div>
                  <!-- 數字也同步變色 -->
                  <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.gym.current, center.gym.max)}`">
                    {{ center.gym.current }}
                  </span>
                  <span class="text-caption text-grey"> / {{ center.gym.max }} 人</span>
                </div>
              </div>
              <!-- 動態綁定進度條顏色 -->
              <v-progress-linear
                :model-value="(center.gym.current / center.gym.max) * 100"
                :color="getProgressColor(center.gym.current, center.gym.max)"
                height="6"
                rounded
              ></v-progress-linear>
            </div>

            <!-- 項目 2: 游泳池 -->
            <div>
              <div class="d-flex justify-space-between align-center mb-1">
                <div>
                  <v-icon :color="getProgressColor(center.swim.current, center.swim.max)" class="mr-1">
                    mdi-pool
                  </v-icon>
                  <span class="font-weight-medium">游泳池</span>
                </div>
                <div>
                  <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.swim.current, center.swim.max)}`">
                    {{ center.swim.current }}
                  </span>
                  <span class="text-caption text-grey"> / {{ center.swim.max }} 人</span>
                </div>
              </div>
              <!-- 動態綁定進度條顏色 -->
              <v-progress-linear
                :model-value="(center.swim.current / center.swim.max) * 100"
                :color="getProgressColor(center.swim.current, center.swim.max)"
                height="6"
                rounded
              ></v-progress-linear>
            </div>
          </v-card-text>

          <!-- 斷線或錯誤處理 -->
          <v-card-text v-else class="text-center text-grey-darken-1 py-6">
            <v-icon color="error" class="mb-1">mdi-alert-circle-outline</v-icon>
            <div>資料載入失敗 ({{ center.message }})</div>
          </v-card-text>
        </v-card>

      </v-container>
    </v-main>

    <!-- Bottom Navigation 底部導覽 -->
    <v-bottom-navigation v-model="activeTab" color="teal-darken-1" grow>
      <v-btn value="list">
        <v-icon>mdi-format-list-bulleted</v-icon>
        各區列表
      </v-btn>
      <v-btn value="favorite">
        <v-icon>mdi-heart</v-icon>
        我的最愛
      </v-btn>
    </v-bottom-navigation>
  </v-app>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const activeTab = ref('list')
const loading = ref(false)
const centers = ref([])
const lastUpdated = ref('')
let timer = null

// 取得資料的函式
const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get('https://teddream.tw/api/sports-centers')
    centers.value = response.data
    
    // 成功抓取後，更新最新日期與時間紀錄（格式如：下午 3:45:21）
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

// 根據目前人數與最大容量，計算擁擠度並回傳 Vuetify 顏色代碼
const getProgressColor = (current, max) => {
  if (!max || max === 0) return 'grey'; // 防呆：避免分母為 0 導致錯誤
  
  const ratio = (current / max) * 100

  if (ratio < 50) {
    return 'green-darken-1'  // 小於 50%：綠色（舒適）
  } else if (ratio <= 80) {
    return 'amber-darken-1'  // 50% ~ 80%：黃橘色（微擁擠）
  } else {
    return 'red-darken-1'    // 大於 80%：紅色（爆滿）
  }
}

onMounted(() => {
  // 1. 畫面掛載後先抓第一次
  fetchData()
  
  // 2. 設定每 1 分鐘（60000 毫秒）自動執行一次
  timer = setInterval(() => {
    fetchData()
  }, 60000)
})

onUnmounted(() => {
  // 3. 離開頁面時記得清除定時器，避免記憶體洩漏
  if (timer) {
    clearInterval(timer)
  }
})
</script>