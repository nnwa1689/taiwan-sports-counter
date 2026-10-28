package tw.teddream.taiwansportscounter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import tw.teddream.taiwansportscounter.R
import tw.teddream.taiwansportscounter.data.AppConfig
import tw.teddream.taiwansportscounter.data.LocationHelper
import tw.teddream.taiwansportscounter.ui.theme.Brand
import tw.teddream.taiwansportscounter.ui.theme.DongchaoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DongchaoApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { LocationHelper.requestLocation(context, viewModel::updateLocation) }

    // 縣市清單只會隨後端部署改變，啟動時讀一次即可
    LaunchedEffect(Unit) {
        viewModel.fetchAreas()
        if (!LocationHelper.hasPermission(context)) {
            permissionLauncher.launch(LocationHelper.permissions)
        }
    }

    // 在前景時：更新位置並每 60 秒自動更新一次（與 webapp 相同），進入背景即停止
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            LocationHelper.requestLocation(context, viewModel::updateLocation)
            while (true) {
                viewModel.fetchData()
                delay(AppConfig.REFRESH_INTERVAL_MS)
            }
        }
    }

    Scaffold(
        containerColor = DongchaoTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { BrandTitle() },
                actions = {
                    if (tab != AppTab.ABOUT) {
                        AreaMenu(
                            areas = state.areas,
                            selected = state.selectedArea,
                            onSelect = viewModel::selectArea,
                        )
                        RefreshButton(loading = state.loading, onClick = viewModel::fetchData)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DongchaoTheme.colors.background,
                    scrolledContainerColor = DongchaoTheme.colors.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DongchaoTheme.colors.card) {
                TabItem(tab, AppTab.LIST, "各區列表", Icons.AutoMirrored.Filled.FormatListBulleted, viewModel::selectTab)
                TabItem(
                    tab, AppTab.FAVORITE, "我的最愛",
                    if (tab == AppTab.FAVORITE) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    viewModel::selectTab,
                )
                TabItem(tab, AppTab.ABOUT, "公告&關於", Icons.Outlined.Campaign, viewModel::selectTab)
            }
        },
    ) { padding ->
        when (tab) {
            AppTab.ABOUT -> NoticeAboutScreen(contentPadding = padding)
            else -> CenterListScreen(
                state = state,
                tab = tab,
                contentPadding = padding,
                onRefresh = viewModel::fetchData,
                onToggleFavorite = viewModel::toggleFavorite,
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabItem(
    current: AppTab,
    tab: AppTab,
    label: String,
    icon: ImageVector,
    onSelect: (AppTab) -> Unit,
) {
    NavigationBarItem(
        selected = current == tab,
        onClick = { onSelect(tab) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Brand.Blue,
            selectedTextColor = Brand.Blue,
            indicatorColor = DongchaoTheme.colors.chip,
            unselectedIconColor = Brand.Gray,
            unselectedTextColor = DongchaoTheme.colors.textPrimary,
        ),
    )
}

/** 導覽列左側的品牌標題（Logo + 動潮 + 副標） */
@Composable
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "動潮 Logo",
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text("動潮", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textPrimary)
            Text(
                "台灣運動中心人潮",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = DongchaoTheme.colors.textSecondary,
            )
        }
    }
}

/** 區域篩選下拉選單 */
@Composable
private fun AreaMenu(areas: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    TextButton(onClick = { expanded = true }) {
        Text(selected, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DongchaoTheme.colors.textPrimary)
        Icon(
            Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = DongchaoTheme.colors.textPrimary,
            modifier = Modifier.size(18.dp),
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = DongchaoTheme.colors.card,
    ) {
        areas.forEach { area ->
            val isSelected = area == selected
            DropdownMenuItem(
                text = {
                    Text(
                        area,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Brand.Blue else DongchaoTheme.colors.textPrimary,
                    )
                },
                trailingIcon = {
                    if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Brand.Blue)
                },
                onClick = {
                    onSelect(area)
                    expanded = false
                },
            )
        }
    }
}

/** 手動重整按鈕（載入中改顯示與下拉重整相同的系統 loading 圖示） */
@Composable
private fun RefreshButton(loading: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = !loading, modifier = Modifier.padding(end = 4.dp)) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Brand.Blue)
        } else {
            Icon(Icons.Filled.Refresh, contentDescription = "重新整理", tint = Brand.Blue)
        }
    }
}
