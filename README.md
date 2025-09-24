# NoNameRadio 📻

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://developer.android.com)

A modern Android radio streaming application based on the [Radio Browser](http://www.radio-browser.info) API. This is a fork of RadioDroid with enhanced features and modernized architecture.

> **Keywords**: Android radio app, online radio streaming, Media3 ExoPlayer, radio stations, internet radio, audio streaming, HLS support, Material Design

## Features

- **Extensive Station Directory**: Access thousands of online radio stations via the Radio Browser API
- **Modern Media Playback**: Utilizes Media3 (ExoPlayer) for robust and efficient audio streaming, including HLS support
- **MediaSession Integration**: Seamless integration with Android system media controls, notifications, lock screen, and automotive interfaces
- **Network Resilience**: Advanced network change detection and retry logic for uninterrupted playback
- **Customizable Playback**: Features like sleep timer, auto-play on startup, and external player options
- **Favorites & History**: Easily manage your preferred stations and track your listening history
- **Recording Functionality**: Record your favorite broadcasts (requires storage permissions)
- **User Interface**: Clean and intuitive design with light and dark themes
- **Search & Filtering**: Efficiently find stations by name, tags, country, or language
- **MPD Support**: Integration with Music Player Daemon

## Technical Improvements

This fork focuses on modernizing the codebase and improving the user experience:

- **Media3 Migration**: Full transition to `androidx.media3` for all media playback, replacing older `MediaPlayer` fallbacks
- **Simplified Player Architecture**: Removal of `MediaPlayerWrapper` and related legacy components, streamlining the player logic
- **Unified MediaSource Handling**: Implementation of `DefaultMediaSourceFactory` with `media3-datasource-okhttp` for consistent and efficient media source management
- **Modern Network Management**: Replaced deprecated `ConnectivityManager.CONNECTIVITY_ACTION` with `ConnectivityManager.NetworkCallback` for reliable network state monitoring
- **Reduced `@UnstableApi` Usage**: Minimized reliance on unstable APIs, enhancing code stability and future compatibility
- **Build System Optimization**: Cleaned `build.gradle` dependencies, updated library versions, and applied Gradle build optimizations (e.g., R8, caching)
- **Analytics Integration**: AppMetrica integration for usage analytics and crash reporting

## 🚀 Quick Start

### Download & Install
1. **Download APK**: Get the latest release from [GitHub Releases](https://github.com/eosphor/NoNameRadio/releases)
2. **Enable Unknown Sources**: Allow installation from unknown sources in Android settings
3. **Install**: Tap the downloaded APK file to install
4. **Launch**: Open NoNameRadio and start listening to radio stations!

### Build from Source
```bash
git clone https://github.com/eosphor/NoNameRadio.git
cd NoNameRadio/RadioDroid
./gradlew assembleFreeDebug
```

## Usage

- **Browse Stations**: Navigate through various categories or use the search function to find stations
- **Play/Pause**: Tap a station to start playback. Use the player controls in the notification or app
- **Favorites**: Star stations to add them to your favorites list
- **Alarms**: Set alarms to wake up to your favorite radio station
- **Recordings**: Record live streams (ensure storage permissions are granted)

## ❓ FAQ

**Q: What makes NoNameRadio different from other radio apps?**
A: NoNameRadio uses modern Media3 (ExoPlayer) technology, provides seamless integration with Android system controls, and offers thousands of radio stations via the Radio Browser API.

**Q: Does it work offline?**
A: NoNameRadio requires an internet connection to stream radio stations, but you can record streams for offline listening.

**Q: Is it free and open source?**
A: Yes! NoNameRadio is completely free and open source under GPLv3 license.

**Q: Which Android versions are supported?**
A: Android 5.0 (API level 21) and higher.

**Q: Can I contribute to the project?**
A: Absolutely! See our [Contributing Guide](.github/CONTRIBUTING.md) for details.

## 📱 Screenshots

*Screenshots coming soon - help us by contributing some!*

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](.github/CONTRIBUTING.md) for details on how to get started.

## 🐛 Bug Reports & Feature Requests

- **Bug Reports**: Use our [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.md)
- **Feature Requests**: Use our [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.md)
- **Security Issues**: See our [Security Policy](.github/SECURITY.md)

## 📄 License

This project is free software licensed under the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

## 🙏 Acknowledgements

- **Radio Browser**: For providing the extensive and open radio station directory
- **Original RadioDroid Developers**: Special thanks to the original contributors of RadioDroid for their foundational work

## 📍 Project Information

- **Project Home**: [https://github.com/eosphor/NoNameRadio](https://github.com/eosphor/NoNameRadio)
- **Original Project**: [https://github.com/morckx/RadioDroid](https://github.com/morckx/RadioDroid) (initially: [https://github.com/segler-alex/RadioDroid](https://github.com/segler-alex/RadioDroid))

## 👨‍💻 Author

[@monviso](https://www.linkedin.com/in/monviso/)

---

**⭐ If you find this project useful, please give it a star!**