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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String ASSET_URL = "file:///android_asset/index.html";

    private WebView webView;
    private TextToSpeech tts;
    private volatile boolean ttsReady;

    private AdView adView;
    private AppOpenAd appOpenAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Google Play Games Services SDK v2
        PlayGamesSdk.initialize(this);

        // Initialize Google Mobile Ads SDK asynchronously
        MobileAds.initialize(this, initializationStatus -> {
            loadAppOpenAd();
        });

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
        webView.addJavascriptInterface(new PlayGamesBridge(), "PlayGamesBridge");
        webView.addJavascriptInterface(new AdMobBridge(), "AdMobBridge");
        webView.addJavascriptInterface(new IntegrityBridge(), "IntegrityBridge");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Create AdMob Banner View at bottom
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_banner_id));
        adView.setAdSize(AdSize.BANNER);
        FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        adParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        root.addView(adView, adParams);

        // Load Banner Ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        setContentView(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

        webView.requestFocus();
        webView.loadUrl(ASSET_URL);
    }

    private void loadAppOpenAd() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                this,
                getString(R.string.admob_open_id),
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError loadAdError) {
                        appOpenAd = null;
                    }
                });
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
        if (adView != null) adView.pause();
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
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

    private class PlayGamesBridge {

        @JavascriptInterface
        public void checkSignIn() {
            try {
                GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(MainActivity.this);
                gamesSignInClient.isAuthenticated().addOnCompleteListener(task -> {
                    boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
                    if (!isAuthenticated) {
                        gamesSignInClient.signIn();
                    }
                });
            } catch (Exception ignored) {}
        }

        @JavascriptInterface
        public void submitScore(long score) {
            try {
                LeaderboardsClient leaderboardsClient = PlayGames.getLeaderboardsClient(MainActivity.this);
                leaderboardsClient.submitScore("CgkI_v_V6_sCEAIQAQ", score);
            } catch (Exception ignored) {}
        }

        @JavascriptInterface
        public void unlockAchievement(String achievementId) {
            try {
                AchievementsClient achievementsClient = PlayGames.getAchievementsClient(MainActivity.this);
                achievementsClient.unlock(achievementId);
            } catch (Exception ignored) {}
        }
    }

    private class AdMobBridge {

        @JavascriptInterface
        public void showAppOpenAd() {
            runOnUiThread(() -> {
                if (appOpenAd != null) {
                    appOpenAd.show(MainActivity.this);
                }
            });
        }

        @JavascriptInterface
        public void reloadBanner() {
            runOnUiThread(() -> {
                if (adView != null) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                }
            });
        }
    }

    private class IntegrityBridge {

        @JavascriptInterface
        public void requestIntegrityToken(String nonce) {
            try {
                IntegrityManager integrityManager = IntegrityManagerFactory.create(MainActivity.this);
                integrityManager.requestIntegrityToken(
                        IntegrityTokenRequest.builder()
                                .setCloudProjectNumber(1030417220838L)
                                .setNonce(nonce != null && !nonce.isEmpty() ? nonce : "boostflagrush")
                                .build()
                );
            } catch (Exception ignored) {}
        }
    }
}
