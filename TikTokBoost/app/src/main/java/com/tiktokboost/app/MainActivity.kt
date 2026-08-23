package com.tiktokboost.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.TikTokBoostApp
import com.tiktokboost.app.ui.theme.TikTokBoostTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Session.init(applicationContext)
        AppState.refresh()
        setContent {
            TikTokBoostTheme {
                TikTokBoostApp()
            }
        }
    }
}
