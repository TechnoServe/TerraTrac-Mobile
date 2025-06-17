package org.technoserve.farmcollector.ui.screens.privacy

import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun PrivacyPolicyWebView(url: String, onScrollAtBottom: () -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Add a scroll listener to detect when the user has reached the bottom
                        view?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                            if (!view.canScrollVertically(1)) {
                                // User has scrolled to the bottom of the WebView
                                onScrollAtBottom()
                            }
                        }
                    }
                }
                loadUrl(url)
            }
        }
    )
}
