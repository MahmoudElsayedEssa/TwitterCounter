package com.moe.twitter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moe.twitter.ui.theme.TwitterCounterTheme
import com.moe.twitter.presentation.twitter.TwitterRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwitterCounterTheme {
                TwitterRoute()
            }
        }
    }
}
