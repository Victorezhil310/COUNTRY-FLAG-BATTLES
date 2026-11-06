# Boost Flag Rush — Country Flag Arena

196 country flags bounce inside a neon ring with one moving gap. Every flag that escapes through
the gap is eliminated with an emoji reaction and a spoken line; the last flag standing wins the
round. Six difficulty levels, turbo boost, infinite mode, and a flag-size slider.

- `index.html` — the game, single file, no build step (loads flags from flagcdn.com)
- `docs/` — the same game with every flag and font bundled, for static hosting
- `android/` — the Android wrapper that turns the game into a Play Store `.aab`
- `play/play_store_icon_512.png` — 512×512 icon for the Play Console store listing

## Android

The app is a `WebView` around `android/app/src/main/assets/index.html`. All 196 flags and the
Montserrat variable font ship inside the bundle, so the manifest declares **no permissions at all**
and the game is fully playable offline.

| | |
|---|---|
| applicationId | `com.victorflagsa.boostflagrush` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Orientation | portrait, immersive fullscreen |
| Screens | small → xlarge, `anyDensity`, resizable (split-screen and foldables) |
| Input | touch, mouse and stylus (pointer events), plus the soft keyboard for flag search |
| Speech | Android `TextToSpeech` through the `AndroidTts` JS bridge, with the Web Speech API as fallback |

The canvas is a fixed 9:16 stage that scales to fit, so tall phones letterbox vertically instead of
cropping. Android 11+ gets edge-to-edge insets handled by resizing the WebView; Android 7-10 uses the
legacy immersive flags, because the API 30 inset classes do not exist there.

Verified on an Android 16/17 x86_64 emulator: 196/196 flags loaded offline, round start, eliminations
and scoring, emoji reactions, `TextToSpeech` speaking through the bridge, and the +FLAG search box
raising the keyboard.

### Build the .aab

```bash
cd android
JAVA_HOME=/path/to/jdk17+ gradle bundleRelease
# output: app/build/outputs/bundle/release/app-release.aab
```

`tools/port_to_android.js` regenerates `android/app/src/main/assets/index.html` from the root
`index.html`. Edit the root file, then re-run it — never hand-edit the asset copy.

### Signing

`android/keystore/upload.jks` + `android/keystore.properties` are git-ignored on purpose. This is
the **upload key**: Play App Signing holds the real signing key, so if you lose the upload key you
can reset it in the Console, but if you lose the *account* key before enabling Play App Signing you
cannot recover the app. Back both up outside this machine.

## Size — nowhere near any Play limit

| Artifact | Size | Use |
|---|---|---|
| `release/BoostFlagRush-v1.aab` | 616 KB | upload this to Play Console |
| `release/BoostFlagRush-v1.apk` | 603 KB | sideload for testing only; universal, signed with the same upload key |

The 100 MB figure is Play's **legacy single-APK cap**, and it does not bind an app published as a bundle. Bundles
get a far larger allowance (roughly 200 MB compressed for the base module, more via install-time feature
modules). At 0.6 MB this app is about 300× under even the APK cap, which is a download-conversion advantage —
there is no reason to add bulk to reach any number.

Rebuild the APK with:

```bash
java -jar bundletool.jar build-apks --bundle=release/BoostFlagRush-v1.aab \
  --output=app.apks --mode=universal \
  --ks android/keystore/upload.jks --ks-pass pass:<password> \
  --ks-key-alias boostflagrush-upload --key-pass pass:<password>
```

## Search Console

`docs/` carries both verification methods: the `google8886a45a486e8c72.html` file and a
`google-site-verification` meta tag on every web page. The tag is deliberately stripped from the Android asset
copy by `tools/port_to_android.js` — Google crawls the site, not the app binary.

## Google Play checklist

1. Upload `app-release.aab` to a new release in Play Console (Android 16 / API 36 is the current
   minimum target for new apps).
2. Data safety: declare **no data collected** — the app has no network permission and no analytics.
3. Store listing: use `play/play_store_icon_512.png` as the app icon plus 2+ screenshots.
4. Content rating questionnaire, then a testing track before production.

## Web deploy

Live at https://victorezhil.github.io/COUNTRY-FLAG-BATTLES-/ — GitHub Pages serving the `docs/`
folder from `main`. `docs/` is a self-contained static build (game + 196 flags + font), so it works
with no CDN and no network beyond the initial page load. Double-tap the canvas for fullscreen.
