package tw.teddream.taiwansportscounter.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

/** 取得使用者目前位置，用於依距離排序（未授權時不做任何事） */
object LocationHelper {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    fun hasPermission(context: Context): Boolean = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun requestLocation(context: Context, onResult: (Location) -> Unit) {
        if (!hasPermission(context)) return
        val manager = context.getSystemService(LocationManager::class.java) ?: return
        val providers = enabledProviders(manager)
        if (providers.isEmpty()) return

        // 先用各來源中最新的已知位置立即排序
        providers.mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
            ?.let(onResult)

        // 再向每個來源要一次目前位置，哪個先回來就先用（有的裝置 fused 拿不到，但 GPS／網路拿得到）
        providers.forEach { provider ->
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                null as android.os.CancellationSignal?,
                ContextCompat.getMainExecutor(context),
            ) { location -> location?.let(onResult) }
        }
    }

    private fun enabledProviders(manager: LocationManager): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            manager.hasProvider(LocationManager.FUSED_PROVIDER) &&
            manager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
        ) {
            add(LocationManager.FUSED_PROVIDER)
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
    }
}
