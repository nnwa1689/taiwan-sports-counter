<template>
  <v-app>
    <!-- AppBar 頂部導覽列 -->
    <v-app-bar color="teal-darken-1" elevation="2" density="compact">
      <v-app-bar-title class="font-weight-bold">
        運動中心即時人潮
        <!-- 頂部顯示最新更新時間 -->
        <div class="text-caption font-weight-regular text-teal-lighten-4 mt-n1" v-if="lastUpdated">
          更新於：{{ lastUpdated }}
        </div>
      </v-app-bar-title>

      <!-- 區域篩選下拉選單[cite: 2] -->
      <v-select
        v-model="selectedArea"
        :items="areas"
        density="compact"
        hide-details
        variant="solo-filled"
        flat
        bg-color="teal-darken-2"
        class="mr-2"
        style="max-width: 120px;"
      ></v-select>
      
      <!-- 手動重整按鈕[cite: 2] -->
      <v-btn icon @click="fetchData" :loading="loading">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
    </v-app-bar>

    <!-- 主內容區 -->
    <v-main class="bg-grey-lighten-4">
      <v-container class="pa-4">
        
        <!-- 首次載入中動畫[cite: 2] -->
        <v-row v-if="loading && filteredCenters.length === 0" justify="center" class="my-8">
          <v-progress-circular indeterminate color="teal"></v-progress-circular>
        </v-row>

        <!-- 運動中心卡片列表（改為循環過濾後的 filteredCenters）[cite: 2] -->
        <v-card v-else v-for="center in filteredCenters" :key="center.name" class="mb-4" rounded="lg" elevation="2">
          <v-card-item class="bg-teal-lighten-5">
            <v-card-title class="text-subtitle-1 font-weight-bold text-teal-darken-4 d-flex justify-space-between align-center">
              <span>{{ center.name }}運動中心</span>
              <!-- 顯示縣市小標籤，強化視覺效果 -->
              <v-chip size="x-small" color="teal-darken-2" variant="tonal" class="ml-2">
                {{ center.area }}
              </v-chip>
            </v-card-title>
          </v-card-item>

          <v-card-text class="pt-4" v-if="center.status === 'online'">
            <!-- 項目 1: 健身房[cite: 2] -->
            <div class="mb-4">
              <div class="d-flex justify-space-between align-center mb-1">
                <div>
                  <v-icon :color="getProgressColor(center.gym.current, center.gym.max)" class="mr-1">
                    mdi-weight-lifter
                  </v-icon>
                  <span class="font-weight-medium">健身房</span>
                </div>
                <div>
                  <span class="text-h6 font-weight-black" :class="`text-${getProgressColor(center.gym.current, center.gym.max)}`">
                    {{ center.gym.current }}
                  </span>
                  <span class="text-caption text-grey"> / {{ center.gym.max }} 人</span>
                </div>
              </div>
              <v-progress-linear
                :model-value="(center.gym.current / center.gym.max) * 100"
                :color="getProgressColor(center.gym.current, center.gym.max)"
                height="6"
                rounded
              ></v-progress-linear>
            </div>

            <!-- 項目 2: 游泳池[cite: 2] -->
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
              <v-progress-linear
                :model-value="(center.swim.current / center.swim.max) * 100"
                :color="getProgressColor(center.swim.current, center.swim.max)"
                height="6"
                rounded
              ></v-progress-linear>
            </div>
          </v-card-text>

          <!-- 斷線或錯誤處理[cite: 2] -->
          <v-card-text v-else class="text-center text-grey-darken-1 py-6">
            <v-icon color="error" class="mb-1">mdi-alert-circle-outline</v-icon>
            <div>資料載入失敗 ({{ center.message }})</div>
          </v-card-text>
        </v-card>

      </v-container>
    </v-main>

    <!-- Bottom Navigation 底部導覽[cite: 2] -->
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
import { ref, onMounted, onUnmounted, computed } from 'vue' // 引入 computed
import axios from 'axios'

const activeTab = ref('list')
const loading = ref(false)
const centers = ref([])
const lastUpdated = ref('')
let timer = null

// 區域篩選狀態定義
const selectedArea = ref('全部')
const areas = ['全部', '台北市', '新北市','高雄市']

// 透過選取之地區過濾運動中心清單[cite: 2]
const filteredCenters = computed(() => {
  if (selectedArea.value === '全部') {
    return centers.value
  }
  return centers.value.filter(center => center.area === selectedArea.value)
})

// 取得資料的函式[cite: 2]
const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get('https://teddream.tw/api/sports-centers')
    centers.value = response.data
    
    // 成功抓取後，更新最新日期與時間紀錄
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

// 根據目前人數與最大容量，計算擁擠度並回傳 Vuetify 顏色代碼[cite: 2]
const getProgressColor = (current, max) => {
  if (!max || max === 0) return 'grey'; 
  
  const ratio = (current / max) * 100

  if (ratio < 50) {
    return 'green-darken-1'  
  } else if (ratio <= 80) {
    return 'amber-darken-1'  
  } else {
    return 'red-darken-1'    
  }
}

onMounted(() => {
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