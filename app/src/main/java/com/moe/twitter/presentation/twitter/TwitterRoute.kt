package com.moe.twitter.presentation.twitter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moe.twitter.presentation.twitter.components.GhostEffectCoordinator
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TwitterRoute() {
    val viewModel: TwitterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()
    val ghostCoordinator = remember { GhostEffectCoordinator(scope) }

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
        ghostCoordinator = ghostCoordinator,
        onAction = viewModel::onAction
    )

    // Refresh auth state whenever we return to foreground (e.g., after OAuth browser)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAuthState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

// TwitterRoute is not previewable as it requires:
// - Koin ViewModel injection
// - Real ClipboardManager from Android context
// - Real Toast functionality
//
// To preview this screen, use TwitterScreen directly with mock state (see TwitterScreen.kt previews)


