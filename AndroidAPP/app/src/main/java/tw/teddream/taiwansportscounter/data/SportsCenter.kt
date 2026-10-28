package tw.teddream.taiwansportscounter.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** 單一設施（健身房／游泳池）的人數；後端 parseInt 失敗時會回傳 null，一律視為 0 */
@Serializable
data class Facility(
    private val current: Int? = null,
    private val max: Int? = null,
) {
    val currentCount: Int get() = current ?: 0
    val maxCount: Int get() = max ?: 0

    /** 0..1 的使用比例 */
    val ratio: Float
        get() = if (maxCount > 0) (currentCount.toFloat() / maxCount).coerceIn(0f, 1f) else 0f
}

/** 對應後端 `/sports-centers` 回傳的單筆運動中心資料 */
@Serializable
data class SportsCenter(
    val name: String,
    val area: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val status: String = "offline",
    val message: String? = null,
    val gym: Facility? = null,
    val swim: Facility? = null,
    /** 由使用者位置計算出的距離（公里），不由 API 提供 */
    @Transient val distance: Double? = null,
) {
    val isOnline: Boolean get() = status == "online"
}
