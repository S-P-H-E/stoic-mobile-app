package com.stoic.app

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_UPLOAD_REQUEST_CODE = 1

    private lateinit var myWebView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.webview)
        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        val appName = "STOIC"
        val appVersion = "1.0"
        val osName = "Android"
        val osVersion = android.os.Build.VERSION.RELEASE

        val customUserAgent = "$appName/$appVersion ($osName $osVersion)"
        webSettings.userAgentString = customUserAgent

        myWebView.setBackgroundColor(Color.parseColor("#09090b"))

        myWebView.webChromeClient = object : WebChromeClient() {
            private var isFullscreen = false
            private var savedState: Bundle? = null

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (isFullscreen) {
                    return
                }

                isFullscreen = true
                savedState = Bundle()
                myWebView.saveState(savedState!!)

                customView = view
                customViewCallback = callback

                // Hide system UI elements for fullscreen
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )

                myWebView.visibility = View.GONE
                val decorView = window.decorView as ViewGroup
                decorView.addView(view)

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                if (!isFullscreen) {
                    return
                }

                isFullscreen = false

                val decorView = window.decorView as ViewGroup
                decorView.removeView(customView)
                customView = null
                customViewCallback?.onCustomViewHidden()

                myWebView.visibility = View.VISIBLE

                // Clear system UI visibility flags
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                savedState?.let { myWebView.restoreState(it) }
                savedState = null

                super.onHideCustomView()
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"

                val chooserIntent = Intent.createChooser(intent, "Select Image")
                startActivityForResult(chooserIntent, FILE_UPLOAD_REQUEST_CODE)

                return true
            }
        }

        myWebView.webViewClient = WebViewClient()

        myWebView.loadUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_UPLOAD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.data?.let {
                    fileChooserCallback?.onReceiveValue(arrayOf(it))
                }
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }
            fileChooserCallback = null
        }
    }
}


