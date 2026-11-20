# OpenFlow

A small, ad-free, open-source video player for Android. Paste a direct video
link, share a link from Telegram or WhatsApp, or open a file from your phone.
No ads, no tracking, no analytics. Only permission: INTERNET.

## Build
1. Install Android Studio and open this folder.
2. Let Gradle sync (it downloads Gradle 8.9 on first run).
3. Press Run on a device or emulator.

If Android Studio suggests newer plugin or library versions, accepting them is fine.

## What works now
- Paste a direct link (.mp4, .mkv, .m3u8 HLS, DASH) and play it
- Share a link from another app into OpenFlow
- Open local video files
- Built-in player controls: seek, speed (settings icon), fullscreen

## Roadmap
1. Telegram: add TDLib (official library) so users can log in and play t.me post links
2. Subtitles, gestures for volume and brightness, picture-in-picture
3. Background audio with a Media3 MediaSessionService
4. App icon and your own package name (change com.example.openflow)

## License
Add a LICENSE file before publishing (Apache-2.0 or GPL-3.0 are common choices).
Don't reuse names, icons, or assets from other apps.

## Get an APK without Android Studio
1. Create a free GitHub account and a new repository.
2. Upload everything in this folder, including the hidden `.github` folder.
3. Open the Actions tab, pick "Build APK", and press Run workflow.
4. When it finishes, download `OpenFlow-debug-apk` from the run page, unzip it, and install `app-debug.apk` on your phone.
