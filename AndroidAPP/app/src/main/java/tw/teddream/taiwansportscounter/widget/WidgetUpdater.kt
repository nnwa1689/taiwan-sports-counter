package tw.teddream.taiwansportscounter.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import tw.teddream.taiwansportscounter.data.AppConfig
import tw.teddream.taiwansportscounter.data.SportsRepository

/**
 * 小工具的更新排程。
 * WorkManager 週期最短 15 分鐘，實際執行時間由系統依省電狀態調整（與 iOS 相同，不保證準時）。
 */
object WidgetUpdater {
    private const val PERIODIC_WORK = "favorites_widget_refresh"
    private const val ONE_TIME_WORK = "favorites_widget_refresh_now"

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_WORK, ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
    }

    /** 只重新繪製（用現有快取），不連網 */
    suspend fun updateWidgets(context: Context) {
        FavoritesWidget().updateAll(context)
    }
}

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SportsRepository(applicationContext)
        // 快取夠新（例如 App 開著、每 60 秒剛寫入）就不連網，避免重複呼叫 API；
        // 過期才抓最新資料，失敗時沿用舊快取
        if (!repository.isCacheFresh(AppConfig.WIDGET_CACHE_MAX_AGE_MS)) {
            runCatching { repository.fetchCenters(timeoutMs = 20_000) }
                .onSuccess { repository.saveCenters(it) }
        }
        WidgetUpdater.updateWidgets(applicationContext)
        return Result.success()
    }
}
