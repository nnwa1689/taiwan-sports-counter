package tw.teddream.taiwansportscounter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import tw.teddream.taiwansportscounter.ui.AppTab
import tw.teddream.taiwansportscounter.ui.DongchaoApp
import tw.teddream.taiwansportscounter.ui.MainViewModel
import tw.teddream.taiwansportscounter.ui.theme.DongchaoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
        setContent {
            DongchaoTheme {
                DongchaoApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /** 從小工具點進來（dongchao://favorite）時切到「我的最愛」 */
    private fun handleDeepLink(intent: Intent?) {
        if (intent?.data?.scheme == "dongchao" && intent.data?.host == "favorite") {
            viewModel.selectTab(AppTab.FAVORITE)
        }
    }
}
