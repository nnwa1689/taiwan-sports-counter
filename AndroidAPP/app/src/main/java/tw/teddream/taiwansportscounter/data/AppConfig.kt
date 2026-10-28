package tw.teddream.taiwansportscounter.data

import java.net.URI
import tw.teddream.taiwansportscounter.BuildConfig

object AppConfig {
    /** 與 webapp 的 VITE_API_URL 相同，可在 app/build.gradle.kts 的 API_URL 修改 */
    val apiUrl: String = BuildConfig.API_URL

    /** 縣市清單端點，與 sports-centers 同一層路徑（例如 /api/areas） */
    val areasUrl: String = URI(apiUrl).resolve("areas").toString()

    /** 縣市篩選的「不篩選」選項（用戶端自己的概念，不由 API 提供） */
    const val ALL_AREAS = "全部"

    /** App 在前景時的自動更新間隔 */
    const val REFRESH_INTERVAL_MS = 60_000L

    /** 共用快取在這個時間內視為新鮮，小工具不重新呼叫 API */
    const val WIDGET_CACHE_MAX_AGE_MS = 5 * 60_000L

    /** 與 webapp localStorage、iOS 相同的 key */
    const val FAVORITES_KEY = "sports_center_favorites"

    /** 小工具點擊後開啟「我的最愛」 */
    const val FAVORITES_DEEP_LINK = "dongchao://favorite"
}
