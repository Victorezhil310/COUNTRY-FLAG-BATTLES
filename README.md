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
| Speech | Android `TextToSpeech` through the `AndroidTts` JS bridge, with the Web Speech API as fallback |

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

## Google Play checklist

1. Upload `app-release.aab` to a new release in Play Console (Android 16 / API 36 is the current
   minimum target for new apps).
2. Data safety: declare **no data collected** — the app has no network permission and no analytics.
3. Store listing: use `play/play_store_icon_512.png` as the app icon plus 2+ screenshots.
4. Content rating questionnaire, then a testing track before production.

## Web deploy

`docs/` is a self-contained static build, so any free static host works — GitHub Pages pointed at
the `docs/` folder on the `main` branch is enough. Double-tap the canvas for fullscreen.
