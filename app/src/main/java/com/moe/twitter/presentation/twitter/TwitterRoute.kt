package com.moe.twitter.presentation.twitter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TwitterRoute() {
    val viewModel: TwitterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TwitterEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is TwitterEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("tweet", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    TwitterScreen(
        state = state,
        ghostEvents = viewModel.ghostEvents,
        onAction = viewModel::onAction
    )
}

// TwitterRoute is not previewable as it requires:
// - Koin ViewModel injection
// - Real ClipboardManager from Android context
// - Real Toast functionality
//
// To preview this screen, use TwitterScreen directly with mock state (see TwitterScreen.kt previews)


