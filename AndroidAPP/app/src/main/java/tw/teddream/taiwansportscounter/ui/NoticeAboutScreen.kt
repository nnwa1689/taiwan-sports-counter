package tw.teddream.taiwansportscounter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tw.teddream.taiwansportscounter.BuildConfig
import tw.teddream.taiwansportscounter.R
import tw.teddream.taiwansportscounter.ui.theme.Brand
import tw.teddream.taiwansportscounter.ui.theme.DongchaoTheme

private data class Notice(val title: String, val date: String, val content: String)

private val notices = listOf(
    Notice("推出 Android App", "2026-10-28", "動潮現在有 Android App 了，也可以在主畫面加入「我的最愛」小工具，不用開 App 就能看到收藏館的人數。"),
    Notice("新增距離排序功能", "2026-08-15", "現在中心清單將以距離排序（如果你同意提供位置）。"),
    Notice("新增桌面版UI", "2026-08-01", "調整了一下桌面版介面。"),
    Notice("已修復無法顯示的區域", "2026-07-28", "已修復全部的運動中心，並調整了一下介面。"),
    Notice("視覺風格更新", "2026-07-28", "新增嘉義、雲林等區，高雄新增楠梓、鼓山。"),
    Notice("新增區域篩選與我的最愛", "2026-07-27", "現在您可以跨縣市篩選運動中心，並透過愛心按鈕將常用的館區加入「我的最愛」快顯清單。"),
)

private val techs = listOf("Jetpack Compose", "Material 3", "Kotlin Coroutines", "Glance", "REST API")

private const val SITE_URL = "https://teddream.tw"

@Composable
fun NoticeAboutScreen(contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { NoticeCard() }
        item { AboutCard() }
        item { DataSourceCard() }
    }
}

/** 帶標題與分隔線的卡片 */
@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    AppCard {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Brand.Blue)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textPrimary)
            }
            HorizontalDivider(color = DongchaoTheme.colors.divider)
            Box(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

// MARK: - 最新公告

@Composable
private fun NoticeCard() {
    SectionCard("最新公告", Icons.Outlined.Campaign) {
        Column {
            notices.forEachIndexed { index, notice ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // 時間軸圓點與連接線
                    Column(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Brand.Blue),
                        )
                        if (index < notices.lastIndex) {
                            Box(
                                Modifier
                                    .width(1.5.dp)
                                    .weight(1f)
                                    .background(DongchaoTheme.colors.divider),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                notice.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DongchaoTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(notice.date, fontSize = 12.sp, color = DongchaoTheme.colors.textSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            notice.content,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = DongchaoTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

// MARK: - 關於本站

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutCard() {
    SectionCard("關於本站", Icons.Outlined.Info) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DongchaoTheme.colors.background)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "動潮｜台灣運動中心即時人潮",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DongchaoTheme.colors.textPrimary,
                    )
                    Text("版本 v${BuildConfig.VERSION_NAME}", fontSize = 12.sp, color = DongchaoTheme.colors.textSecondary)
                }
            }

            Text(
                "本平台提供各縣市運動中心健身房與游泳池之即時人潮數據查詢，協助您避開擁擠高峰期，享有更好的運動體驗。數據每分鐘自動更新。",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = DongchaoTheme.colors.textSecondary,
            )

            HorizontalDivider(color = DongchaoTheme.colors.divider)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("使用技術與架構", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DongchaoTheme.colors.textSecondary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    techs.forEach { tech ->
                        Text(
                            tech,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Brand.Blue,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DongchaoTheme.colors.background)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            Text(
                buildAnnotatedString {
                    append("Designed & Developed by ")
                    withLink(LinkAnnotation.Url(SITE_URL, TextLinkStyles(SpanStyle(fontWeight = FontWeight.Bold)))) {
                        append("TEDDREAM")
                    }
                    append("\n© 2026 All Rights Reserved.")
                },
                fontSize = 12.sp,
                color = DongchaoTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// MARK: - 資料來源與聲明

@Composable
private fun DataSourceCard() {
    SectionCard("資料來源與聲明", Icons.Outlined.Description) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row {
                Icon(
                    Icons.Outlined.GppMaybe,
                    contentDescription = null,
                    tint = Brand.Orange,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "本 App 為個人開發的非官方服務，與各運動中心及其營運單位無隸屬或合作關係。",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DongchaoTheme.colors.textPrimary,
                )
            }
            Text(
                "人數資料取自各運動中心官方網站公開之即時資訊，經整理後顯示，約每分鐘更新一次，可能有延遲或誤差，實際狀況請以現場公告為準。",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = DongchaoTheme.colors.textSecondary,
            )
            HorizontalDivider(color = DongchaoTheme.colors.divider)
        }
    }
}
