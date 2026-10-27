# 動潮 App Store 上架資料（草稿）

> 貼到 App Store Connect 前請再確認一次。括號內為字數上限。

## 基本資訊

| 欄位 | 內容 |
|---|---|
| App 名稱（30） | 動潮－運動中心即時人潮 |
| 副標題（30） | 健身房、游泳池人數一眼看 |
| 主要類別 | 健康與健身 |
| 次要類別 | 工具程式 |
| 支援網址 | https://teddream.tw |
| 隱私權政策網址 | （待建立，例如 https://teddream.tw/tsc/privacy） |
| 版權 | © 2026 TEDDREAM |
| 年齡分級 | 4+（問卷全部選「無」） |

> 名稱與副標題刻意避免「官方」及各營運單位名稱，以免被誤認為官方 App。

## 宣傳文字（170）

出門運動前先看人潮！即時查詢各縣市運動中心健身房與游泳池人數，加入我的最愛，還能用主畫面小工具隨時掌握。

## 描述（4000）

動潮幫你在出門前掌握運動中心的即時人潮，避開擁擠時段，享受更好的運動體驗。

【主要功能】
• 即時人數：查詢各運動中心健身房與游泳池目前人數與容留上限，約每分鐘更新
• 擁擠程度一目了然：以藍、橘、紅三色標示寬鬆、適中、擁擠
• 距離排序：同意定位後，自動由近到遠排列運動中心
• 縣市篩選：台北、新北、桃園、新竹、台中、彰化、雲林、嘉義、台南、高雄
• 我的最愛：收藏常去的運動中心，快速查看
• 主畫面與鎖定畫面小工具：不用開 App 就能看到收藏館的人數
• 支援深色模式與 iPad

【隱私】
• 不需註冊或登入
• 位置資訊只在你的裝置上用來計算距離，不會上傳
• 不追蹤、不蒐集任何個人資料

【聲明】
本 App 為個人開發的非官方服務，與各運動中心及其營運單位無隸屬或合作關係。人數資料取自各運動中心官方網站公開之即時資訊，可能有延遲或誤差，實際狀況請以現場公告為準。

## 關鍵字（100，以逗號分隔、不加空格）

運動中心,國民運動中心,健身房,游泳池,泳池,人潮,人數,即時,擁擠,重訓,台北,新北,桃園,台中,台南,高雄

## App 隱私問卷

- 是否蒐集資料：**否**（與 PrivacyInfo.xcprivacy 一致）
- 說明：位置只在裝置上計算距離、不傳送；我的最愛只存在裝置上。

## 審核備註（App Review Information → Notes）

### English（審核人員使用）

```
Dongchao shows real-time occupancy of gyms and swimming pools at public sports centers in Taiwan.

- No account or login is required. All features are available immediately.
- Location permission is optional and is only used on-device to sort sports centers by distance. Location is never sent to any server.
- Occupancy data comes from information that each sports center publicly displays on its own official website for citizens to avoid crowds. Our server reads each public page at most once per minute (server-side cache), so it adds no meaningful load to the sources. The app does not bypass any login, paywall, or authentication.
- This is an independent, non-official app. It is not affiliated with any sports center or operator, and this is stated in the app (About tab → "資料來源與聲明") and in the description.
- The first launch may take a few seconds to load data.
- Widget: long-press the Home Screen → Edit → Add Widget → 動潮. Add a favorite (heart button) in the app first.
```

### 中文（自行留存）

- 不需登入，所有功能可直接使用。
- 定位為選擇性授權，只在裝置上用來依距離排序，不會傳送到伺服器。
- 人數資料來自各運動中心於官方網站公開給民眾的即時資訊；伺服器有快取，每個來源每分鐘最多讀取一次，不繞過任何登入或驗證。
- 本 App 為非官方獨立服務，App 內「公告&關於 → 資料來源與聲明」及描述中皆有標示。

## 送審前檢查

- [ ] 後端已部署伺服器快取與 `/areas` 端點
- [ ] 隱私權政策網頁已上線
- [ ] 截圖：iPhone 6.9 吋、iPad 13 吋（避免出現任何營運單位 logo）
- [ ] Xcode Team 已換成付費開發者帳號
- [ ] 主要資料來源是否已取得同意（建議優先：救國團、sporetrofit、teamxports）
