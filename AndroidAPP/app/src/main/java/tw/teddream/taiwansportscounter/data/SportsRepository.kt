package tw.teddream.taiwansportscounter.data

import android.content.Context
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** App 與小工具共用的資料存取：API、最近一次資料快取、我的最愛 */
class SportsRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class CachedCenters(val centers: List<SportsCenter>, val updatedAt: Long)

    // MARK: - 網路

    suspend fun fetchCenters(timeoutMs: Int = 30_000): List<SportsCenter> =
        json.decodeFromString(getText(AppConfig.apiUrl, timeoutMs))

    suspend fun fetchAreas(timeoutMs: Int = 15_000): List<String> =
        json.decodeFromString(getText(AppConfig.areasUrl, timeoutMs))

    private suspend fun getText(url: String, timeoutMs: Int): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.useCaches = false
            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    // MARK: - 我的最愛（JSON 字串，格式與 webapp 的 localStorage 相同）

    var favorites: List<String>
        get() = prefs.getString(AppConfig.FAVORITES_KEY, null)
            ?.let { runCatching { json.decodeFromString<List<String>>(it) }.getOrNull() }
            ?: emptyList()
        set(value) {
            prefs.edit().putString(AppConfig.FAVORITES_KEY, json.encodeToString(value)).apply()
        }

    // MARK: - 最近一次成功取得的資料（冷啟動與小工具連線失敗時使用）

    fun saveCenters(centers: List<SportsCenter>, updatedAt: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putString(KEY_CENTERS, json.encodeToString(centers))
            .putLong(KEY_CACHED_AT, updatedAt)
            .apply()
    }

    fun loadCenters(): CachedCenters? {
        val text = prefs.getString(KEY_CENTERS, null) ?: return null
        val centers = runCatching { json.decodeFromString<List<SportsCenter>>(text) }.getOrNull() ?: return null
        return CachedCenters(centers, prefs.getLong(KEY_CACHED_AT, 0L))
    }

    fun isCacheFresh(maxAgeMs: Long): Boolean {
        val cache = loadCenters() ?: return false
        return System.currentTimeMillis() - cache.updatedAt < maxAgeMs
    }

    companion object {
        private const val PREFS_NAME = "dongchao"
        private const val KEY_CENTERS = "cached_centers"
        private const val KEY_CACHED_AT = "cached_centers_at"

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
