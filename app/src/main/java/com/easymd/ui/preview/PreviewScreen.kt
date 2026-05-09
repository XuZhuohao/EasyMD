package com.easymd.ui.preview

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    file: File?,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()

    LaunchedEffect(file) {
        file?.let { viewModel.loadFile(it, darkTheme) }
    }

    LaunchedEffect(darkTheme) {
        viewModel.onThemeChanged(darkTheme)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "分享")
                    }
                }
            )
        }
    ) { padding ->
        if (file == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("请先在编辑器中打开文档", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = false
                            domStorageEnabled = false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = true
                        }
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(
                        null, state.html, "text/html", "utf-8", null
                    )
                },
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
    }
}
