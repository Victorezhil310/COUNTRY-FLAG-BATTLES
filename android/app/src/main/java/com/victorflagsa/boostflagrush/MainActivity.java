package com.victorflagsa.boostflagrush;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.android.gms.games.PlayGamesSdk;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String ASSET_URL = "file:///android_asset/index.html";

    private WebView webView;
    private TextToSpeech tts;
    private volatile boolean ttsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Google Play Games Services SDK v2
        PlayGamesSdk.initialize(this);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith(ASSET_URL);
            }
        });
        webView.addJavascriptInterface(new TtsBridge(), "AndroidTts");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 15+ is always edge-to-edge, so inset the WebView itself rather than padding it:
            // a real size change fires the page's resize handler and the canvas re-fits inside the safe area.
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) webView.getLayoutParams();
                lp.setMargins(bars.left, bars.top, bars.right, bars.bottom);
                webView.setLayoutParams(lp);
                return insets;
            });
            root.requestApplyInsets();

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.hide(WindowInsets.Type.systemBars());
            }
        } else {
            // Android 7-10: these inset and controller APIs do not exist, and the window is not
            // edge-to-edge, so the plain immersive flags are enough.
            applyLegacyImmersive(root);
        }

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) return;
            int result = tts.setLanguage(Locale.US);
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady) {
                tts.setPitch(0.85f);
                tts.setSpeechRate(1.0f);
            }
        });

        // Without focus the +FLAG country search cannot raise the keyboard.
        webView.requestFocus();
        webView.loadUrl(ASSET_URL);
    }

    @SuppressWarnings("deprecation")
    private void setLegacyImmersiveFlags(View root) {
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @SuppressWarnings("deprecation")
    private void applyLegacyImmersive(View root) {
        root.setOnSystemUiVisibilityChangeListener(flags -> {
            if ((flags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) setLegacyImmersiveFlags(root);
        });
        setLegacyImmersiveFlags(root);
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        webView.destroy();
        super.onDestroy();
    }

    private class TtsBridge {

        @JavascriptInterface
        public boolean isReady() {
            return ttsReady;
        }

        @JavascriptInterface
        public boolean speak(String text) {
            if (!ttsReady || text == null || text.isEmpty()) return false;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boostflagrush");
            return true;
        }

        @JavascriptInterface
        public void stop() {
            if (ttsReady) tts.stop();
        }
    }
}
