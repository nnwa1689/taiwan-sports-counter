<img src="webapp/taiwan-sports-counter/public/icon-192.png" alt="動潮 ICON" width="96" />

# 動潮｜台灣運動中心即時人潮

查詢各縣市運動中心健身房與游泳池的即時人數，提供網頁版（WebAPP）、iOS App 與 Android App。

```
各運動中心官網 ──► API（抓取＋快取）──► WebAPP / iOS App / Android App（皆含小工具）
                  └─ 部分來源經 GCP Proxy（台灣 IP）
```

## API
`node server.js`

API 負責呼叫並解析各運動中心的資料，統一格式後提供給前端。

| 端點 | 說明 |
|---|---|
| `GET /sports-centers` | 所有運動中心的即時人數（陣列），header `X-Data-Updated-At` 為資料時間 |
| `GET /areas` | 縣市清單，由 `SPORTS_CENTERS` 依定義順序產生，新增館別時自動帶出新縣市 |

- 伺服器快取：資料 60 秒內直接回傳；過期時先回舊資料並於背景更新；同時間的請求共用同一次抓取。外部請求量固定，不隨使用者人數增加。
- 環境變數：`PORT`（預設 3000）、`CACHE_TTL_MS`（快取時間，預設 60000）

## PROXY
部署到 Google Cloud Run，使用台灣 IP 發出請求，取得資料

## WebAPP
`npm run dev`

VueJS 的 APP，已設定對應設定檔 VITE_API_URL。縣市清單由同一層路徑的 `/areas` 讀取。

## iOSAPP
SwiftUI 原生 App，功能與 WebAPP 相同，並提供主畫面／鎖定畫面小工具。

**需求**：Xcode 27 以上、iOS 26 以上（iPhone、iPad）

**執行**：用 Xcode 開啟 `iOSAPP/TaiwanSportsCounter.xcodeproj`，在 Signing & Capabilities 選擇自己的 Team 後執行。

**功能**
- 各區列表、我的最愛、公告&關於（與 WebAPP 相同）
- 縣市篩選（由 API `/areas` 讀取）、依距離排序、每 60 秒自動更新、下拉重新整理
- 「我的最愛」小工具：主畫面小／中／大、鎖定畫面矩形／單行，點擊開啟「我的最愛」
- 冷啟動先顯示上次快取的資料，不必等待 API

**專案結構**
```
iOSAPP/
├── TaiwanSportsCounter/          # App：畫面、定位、資料狀態
├── TaiwanSportsCounterWidget/    # 小工具（WidgetKit）
├── Shared/                       # App 與小工具共用：資料模型、設定、配色、App Group 儲存
├── Info.plist / WidgetInfo.plist # API_URL、URL scheme（dongchao://）等
├── *.entitlements                # App Group：group.tw.teddream.TaiwanSportsCounter
└── AppStore上架資料.md            # App Store 描述、關鍵字、審核備註草稿
```

**設定**
- API 網址：Build Settings 的 `API_URL`（預設 `https://teddream.tw/api/sports-centers`），`/areas` 會由同一層路徑推算。
- App 與小工具透過 App Group 共用「我的最愛」與資料快取；小工具在快取 5 分鐘內不重新呼叫 API，更新頻率由 iOS 決定（約 15–60 分鐘）。
- 隱私清單：`TaiwanSportsCounter/PrivacyInfo.xcprivacy`（不追蹤、不蒐集資料）。

## AndroidAPP
Kotlin + Jetpack Compose 原生 App，功能與 iOS App 相同，並提供主畫面小工具。

**需求**：Android Studio（JDK 17 以上）、Android SDK 36；支援 Android 8.0（API 26）以上

**執行**：用 Android Studio 開啟 `AndroidAPP/` 資料夾後執行；或用指令建置：

```bash
cd AndroidAPP && ./gradlew :app:assembleDebug
```

**功能**
- 各區列表、我的最愛、公告&關於（與 iOS 相同）
- 縣市篩選（由 API `/areas` 讀取）、依距離排序、在前景時每 60 秒自動更新、下拉重新整理
- 「我的最愛」小工具（Glance）：依大小自動切換小／中／大版面，點擊開啟「我的最愛」（`dongchao://favorite`）
- 冷啟動先顯示上次快取的資料，支援深色模式

**專案結構**
```
AndroidAPP/app/src/main/java/tw/teddream/taiwansportscounter/
├── data/     # 資料模型、設定、API 與快取（SportsRepository）、定位
├── ui/       # Compose 畫面、ViewModel、主題配色
└── widget/   # 小工具（Glance）與 WorkManager 更新排程
```

**設定**
- API 網址：`app/build.gradle.kts` 的 `API_URL`（預設 `https://teddream.tw/api/sports-centers`），`/areas` 會由同一層路徑推算。
- 我的最愛與資料快取存在 SharedPreferences，key 與 WebAPP／iOS 相同（`sports_center_favorites`）。
- 小工具由 WorkManager 每 15 分鐘排程更新（實際時間由系統決定），快取 5 分鐘內不重新呼叫 API。
- 定位權限只用來依距離排序，不會上傳。
