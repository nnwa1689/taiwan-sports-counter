package tw.teddream.taiwansportscounter.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import tw.teddream.taiwansportscounter.MainActivity
import tw.teddream.taiwansportscounter.R
import tw.teddream.taiwansportscounter.data.AppConfig
import tw.teddream.taiwansportscounter.data.Facility
import tw.teddream.taiwansportscounter.data.SportsCenter
import tw.teddream.taiwansportscounter.data.SportsRepository
import tw.teddream.taiwansportscounter.ui.theme.Brand

/** 「我的最愛」小工具：顯示收藏運動中心的健身房／游泳池即時人數 */
class FavoritesWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 只讀共用快取；連網由 WidgetRefreshWorker 負責
        val repository = SportsRepository(context)
        val favorites = repository.favorites
        val cache = repository.loadCenters()
        val byName = cache?.centers.orEmpty().associateBy { it.name }
        val entry = WidgetEntry(
            centers = favorites.mapNotNull { byName[it] },
            hasFavorites = favorites.isNotEmpty(),
            updatedAt = cache?.updatedAt,
        )
        val openFavorites = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(AppConfig.FAVORITES_DEEP_LINK),
            context,
            MainActivity::class.java,
        )
        provideContent { WidgetContent(entry, actionStartActivity(openFavorites)) }
    }

    companion object {
        val SMALL = DpSize(110.dp, 110.dp)
        val MEDIUM = DpSize(250.dp, 110.dp)
        val LARGE = DpSize(250.dp, 250.dp)
    }
}

private data class WidgetEntry(
    /** 依加入最愛的順序排列 */
    val centers: List<SportsCenter>,
    val hasFavorites: Boolean,
    val updatedAt: Long?,
)

// MARK: - 顏色（小工具不能讀 Compose 主題，需各自指定日／夜色）

private object WidgetColors {
    val card = ColorProvider(day = Color.White, night = Color(0xFF1C1C1E))
    val chip = ColorProvider(day = Color(0xFFE5E5EA), night = Color(0xFF2C2C2E))
    val textPrimary = ColorProvider(day = Color(0xFF1C1C1E), night = Color.White)
    val textSecondary = ColorProvider(day = Color(0xFF6C6C70), night = Color(0xFFAEAEB2))
    val blue = ColorProvider(Brand.Blue)
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.TAIWAN)

// MARK: - Views

@Composable
private fun WidgetContent(entry: WidgetEntry, onClick: Action) {
    val size = LocalSize.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .background(WidgetColors.card)
            .padding(14.dp)
            .clickable(onClick),
    ) {
        when {
            !entry.hasFavorites -> MessageView("尚無我的最愛", "在 App 中點擊愛心加入", showHeart = true)
            entry.centers.isEmpty() -> MessageView("尚無資料", "開啟 App 更新人數", showHeart = false)
            size.width < FavoritesWidget.MEDIUM.width -> SmallView(entry.centers.first(), entry.updatedAt)
            size.height < FavoritesWidget.LARGE.height -> MediumView(entry.centers.take(2), entry.updatedAt)
            else -> LargeView(entry.centers, entry.updatedAt, size.height.value)
        }
    }
}

/** 單一設施：圖示、人數與進度條 */
@Composable
private fun FacilityView(iconRes: Int, facility: Facility?, modifier: GlanceModifier = GlanceModifier) {
    val current = facility?.currentCount ?: 0
    val max = facility?.maxCount ?: 0
    val color = ColorProvider(Brand.progressColor(current, max))
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
                modifier = GlanceModifier.size(14.dp),
            )
            Spacer(GlanceModifier.width(4.dp))
            Text("$current", style = TextStyle(color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold))
            Text(" / $max", style = TextStyle(color = WidgetColors.textSecondary, fontSize = 11.sp))
        }
        Spacer(GlanceModifier.height(4.dp))
        LinearProgressIndicator(
            progress = facility?.ratio ?: 0f,
            color = color,
            backgroundColor = WidgetColors.chip,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        )
    }
}

/** 運動中心區塊：名稱 + 健身房 + 游泳池（小型、中型共用） */
@Composable
private fun CenterBlock(center: SportsCenter, modifier: GlanceModifier = GlanceModifier) {
    Column(modifier = modifier) {
        Text(
            "${center.name}運動中心",
            maxLines = 1,
            style = TextStyle(color = WidgetColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(GlanceModifier.height(8.dp))
        if (center.isOnline) {
            FacilityView(R.drawable.ic_widget_gym, center.gym, GlanceModifier.fillMaxWidth())
            Spacer(GlanceModifier.height(8.dp))
            FacilityView(R.drawable.ic_widget_pool, center.swim, GlanceModifier.fillMaxWidth())
        } else {
            Text("資料暫無法取得", style = TextStyle(color = WidgetColors.textSecondary, fontSize = 11.sp))
        }
    }
}

@Composable
private fun UpdatedAt(updatedAt: Long?) {
    if (updatedAt == null) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_clock),
            contentDescription = null,
            colorFilter = ColorFilter.tint(WidgetColors.textSecondary),
            modifier = GlanceModifier.size(10.dp),
        )
        Spacer(GlanceModifier.width(2.dp))
        // 小工具空間有限，固定用 24 小時制「HH:mm」
        Text(
            timeFormat.format(Date(updatedAt)),
            style = TextStyle(color = WidgetColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun Header(updatedAt: Long?) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.logo),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            "我的最愛",
            style = TextStyle(color = WidgetColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            modifier = GlanceModifier.defaultWeight(),
        )
        UpdatedAt(updatedAt)
    }
}

@Composable
private fun SmallView(center: SportsCenter, updatedAt: Long?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        CenterBlock(center, GlanceModifier.fillMaxWidth().defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                center.area,
                style = TextStyle(color = WidgetColors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight(),
            )
            UpdatedAt(updatedAt)
        }
    }
}

@Composable
private fun MediumView(centers: List<SportsCenter>, updatedAt: Long?) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(updatedAt)
        Spacer(GlanceModifier.height(10.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            CenterBlock(centers[0], GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(16.dp))
            if (centers.size > 1) {
                CenterBlock(centers[1], GlanceModifier.defaultWeight())
            } else {
                Spacer(GlanceModifier.defaultWeight())
            }
        }
    }
}

@Composable
private fun LargeView(centers: List<SportsCenter>, updatedAt: Long?, heightDp: Float) {
    // 依小工具高度決定顯示幾間（每列約 46dp），最多 6 間
    val rows = ((heightDp - 60f) / 46f).toInt().coerceIn(1, 6)
    val shown = centers.take(rows)
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Header(updatedAt)
        Spacer(GlanceModifier.height(8.dp))
        shown.forEachIndexed { index, center ->
            if (index > 0) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(WidgetColors.chip)) {}
                Spacer(GlanceModifier.height(6.dp))
            }
            Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.width(92.dp)) {
                    Text(
                        "${center.name}運動中心",
                        maxLines = 1,
                        style = TextStyle(color = WidgetColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    )
                    Text(
                        center.area,
                        style = TextStyle(color = WidgetColors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    )
                }
                if (center.isOnline) {
                    FacilityView(R.drawable.ic_widget_gym, center.gym, GlanceModifier.defaultWeight())
                    Spacer(GlanceModifier.width(10.dp))
                    FacilityView(R.drawable.ic_widget_pool, center.swim, GlanceModifier.defaultWeight())
                } else {
                    Text(
                        "資料暫無法取得",
                        style = TextStyle(color = WidgetColors.textSecondary, fontSize = 12.sp),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        if (centers.size < 6) {
            Text(
                "在 App 中點擊愛心加入更多運動中心",
                style = TextStyle(color = WidgetColors.textSecondary, fontSize = 11.sp),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MessageView(title: String, message: String, showHeart: Boolean) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showHeart) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_heart),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WidgetColors.textSecondary),
                modifier = GlanceModifier.size(28.dp),
            )
            Spacer(GlanceModifier.height(6.dp))
        }
        Text(title, style = TextStyle(color = WidgetColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        Text(message, style = TextStyle(color = WidgetColors.textSecondary, fontSize = 11.sp))
    }
}
