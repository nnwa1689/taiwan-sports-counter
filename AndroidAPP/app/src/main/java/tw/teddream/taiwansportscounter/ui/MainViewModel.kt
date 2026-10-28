package tw.teddream.taiwansportscounter.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.teddream.taiwansportscounter.data.AppConfig
import tw.teddream.taiwansportscounter.data.SportsCenter
import tw.teddream.taiwansportscounter.data.SportsRepository
import tw.teddream.taiwansportscounter.widget.WidgetUpdater

enum class AppTab { LIST, FAVORITE, ABOUT }

data class UiState(
    val centers: List<SportsCenter> = emptyList(),
    val loading: Boolean = false,
    val lastUpdated: Long? = null,
    val errorMessage: String? = null,
    val favorites: List<String> = emptyList(),
    /** 由 API /areas 取得的縣市清單；尚未取得時為 null */
    val apiAreas: List<String>? = null,
    val selectedArea: String = AppConfig.ALL_AREAS,
    val userLocation: Location? = null,
) {
    /** 篩選選單的選項：「全部」+ API 定義的縣市 */
    val areas: List<String>
        // /areas 尚未載入或失敗時，改由運動中心資料的 area 依出現順序推導
        get() = listOf(AppConfig.ALL_AREAS) + (apiAreas ?: centers.map { it.area }.distinct())

    fun isFavorite(name: String) = name in favorites

    /** 顯示清單：距離排序 → 最愛篩選 → 地區篩選 */
    fun displayedCenters(tab: AppTab): List<SportsCenter> {
        var list = centers
        userLocation?.let { location ->
            list = list.map { center ->
                if (center.lat != null && center.lng != null) {
                    val result = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, center.lat, center.lng, result)
                    center.copy(distance = result[0] / 1000.0)
                } else {
                    center
                }
            }.sortedWith(compareBy(nullsLast()) { it.distance })
        }
        if (tab == AppTab.FAVORITE) list = list.filter { isFavorite(it.name) }
        if (selectedArea != AppConfig.ALL_AREAS) list = list.filter { it.area == selectedArea }
        return list
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SportsRepository(application)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _tab = MutableStateFlow(AppTab.LIST)
    val tab: StateFlow<AppTab> = _tab.asStateFlow()

    init {
        // 先顯示上次的快取，避免冷啟動時等待 API
        val cache = repository.loadCenters()
        _state.value = UiState(
            centers = cache?.centers.orEmpty(),
            lastUpdated = cache?.updatedAt,
            favorites = repository.favorites,
        )
    }

    fun selectTab(tab: AppTab) {
        _tab.value = tab
    }

    fun selectArea(area: String) {
        _state.update { it.copy(selectedArea = area) }
    }

    fun updateLocation(location: Location) {
        _state.update { it.copy(userLocation = location) }
    }

    fun fetchData() {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            runCatching { repository.fetchCenters() }
                .onSuccess { centers ->
                    val now = System.currentTimeMillis()
                    _state.update { it.copy(centers = centers, lastUpdated = now, errorMessage = null, loading = false) }
                    // 寫入共用快取並通知小工具更新
                    repository.saveCenters(centers, now)
                    WidgetUpdater.updateWidgets(getApplication())
                }
                .onFailure {
                    _state.update { it.copy(errorMessage = "無法取得運動中心資料", loading = false) }
                }
        }
    }

    fun fetchAreas() {
        viewModelScope.launch {
            val areas = runCatching { repository.fetchAreas() }.getOrNull()
            _state.update { state ->
                val next = state.copy(apiAreas = areas ?: state.apiAreas)
                // 目前選取的縣市不在新清單中時，回到「全部」
                if (next.selectedArea in next.areas) next else next.copy(selectedArea = AppConfig.ALL_AREAS)
            }
        }
    }

    fun toggleFavorite(name: String) {
        val favorites = _state.value.favorites.let { if (name in it) it - name else it + name }
        _state.update { it.copy(favorites = favorites) }
        repository.favorites = favorites
        viewModelScope.launch { WidgetUpdater.updateWidgets(getApplication()) }
    }
}
