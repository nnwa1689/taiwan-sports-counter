package tw.teddream.taiwansportscounter.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoritesWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdater.schedule(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // 新加入小工具或系統要求更新時：確保週期排程存在，並視快取新鮮度立即更新一次
        WidgetUpdater.schedule(context)
        WidgetUpdater.refreshNow(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdater.cancel(context)
    }
}
