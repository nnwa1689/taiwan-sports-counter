package tw.teddream.taiwansportscounter.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import tw.teddream.taiwansportscounter.data.Facility
import tw.teddream.taiwansportscounter.data.SportsCenter
import tw.teddream.taiwansportscounter.ui.theme.Brand
import tw.teddream.taiwansportscounter.ui.theme.DongchaoTheme

/** 「各區列表」與「我的最愛」共用的清單頁 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterListScreen(
    state: UiState,
    tab: AppTab,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val centers = state.displayedCenters(tab)

    // 只有使用者下拉時才顯示下拉的 loading，自動更新不顯示
    var pulling by remember { mutableStateOf(false) }
    LaunchedEffect(state.loading) { if (!state.loading) pulling = false }

    PullToRefreshBox(
        isRefreshing = pulling,
        onRefresh = {
            pulling = true
            onRefresh()
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.lastUpdated?.let { updatedAt ->
                item { UpdatedAtRow(updatedAt) }
            }
            when {
                state.loading && centers.isEmpty() -> item {
                    CircularProgressIndicator(color = Brand.Blue, modifier = Modifier.padding(vertical = 48.dp))
                }
                tab == AppTab.FAVORITE && centers.isEmpty() -> item { EmptyFavoritesCard() }
                centers.isEmpty() && state.errorMessage != null -> item { ErrorCard(state.errorMessage) }
                else -> items(centers, key = { it.name }) { center ->
                    CenterCard(
                        center = center,
                        isFavorite = state.isFavorite(center.name),
                        onToggleFavorite = { onToggleFavorite(center.name) },
                    )
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.TAIWAN)

@Composable
private fun UpdatedAtRow(updatedAt: Long) {
    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(top = 8.dp, end = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = DongchaoTheme.colors.textPrimary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "更新：${timeFormat.format(Date(updatedAt))}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DongchaoTheme.colors.textPrimary,
        )
    }
}

/** 圓角卡片（無陰影，與 webapp 的 ios-card 相同） */
@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DongchaoTheme.colors.card),
    ) { content() }
}

@Composable
fun CenterCard(center: SportsCenter, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    AppCard {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 名稱過長時先截斷名稱，保留縣市標籤與愛心按鈕
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${center.name}運動中心",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DongchaoTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        center.distance?.let { "${center.area} • ${"%.1f".format(it)} km" } ?: center.area,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand.Blue,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DongchaoTheme.colors.chip)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DongchaoTheme.colors.textPrimary.copy(alpha = 0.04f))
                        .border(1.dp, DongchaoTheme.colors.textPrimary.copy(alpha = 0.06f), CircleShape),
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "移除最愛" else "加入最愛",
                        tint = if (isFavorite) Brand.Red else DongchaoTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            HorizontalDivider(color = DongchaoTheme.colors.divider)

            if (center.isOnline) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FacilityRow("健身房", Icons.Filled.FitnessCenter, center.gym ?: Facility())
                    FacilityRow("游泳池", Icons.Filled.Pool, center.swim ?: Facility())
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Brand.Red)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "資料載入失敗" + (center.message?.let { " ($it)" } ?: ""),
                        fontSize = 14.sp,
                        color = DongchaoTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FacilityRow(title: String, icon: ImageVector, facility: Facility) {
    val color = Brand.progressColor(facility.currentCount, facility.maxCount)
    val ratio by animateFloatAsState(facility.ratio, label = "ratio")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textPrimary)
            Spacer(Modifier.weight(1f))
            Text("${facility.currentCount}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(
                " / ${facility.maxCount} 人",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DongchaoTheme.colors.textSecondary,
            )
        }
        ProgressBar(ratio, color)
    }
}

@Composable
private fun ProgressBar(ratio: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(DongchaoTheme.colors.chip),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio)
                .height(8.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun EmptyFavoritesCard() {
    AppCard(modifier = Modifier.padding(top = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = DongchaoTheme.colors.chip,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text("尚無收藏的運動中心", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "點擊卡片右上角愛心按鈕加入我的最愛",
                fontSize = 14.sp,
                color = DongchaoTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Column(
        modifier = Modifier.padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = DongchaoTheme.colors.textSecondary, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textPrimary)
        Text("請下拉或點擊右上角重新整理", fontSize = 14.sp, color = DongchaoTheme.colors.textSecondary)
    }
}
