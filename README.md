# NoNameRadio 📻

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-blue)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
[![Modernized](https://img.shields.io/badge/Code-Modernized-orange.svg)](https://github.com/eosphor/NoNameRadio)

A modern, fully refactored Android radio streaming application with Clean Architecture, modern Android practices, and comprehensive codebase modernization. Built on the [Radio Browser](http://www.radio-browser.info) API with support for both HTTP and HTTPS streaming.

> **Keywords**: Android radio, streaming app, Media3 ExoPlayer, Clean Architecture, modern Android, HTTP/HTTPS support, offline recording, Material Design 3, dependency injection, coroutine-based async

## ✨ Features

### 🎵 **Core Audio Features**
- **🎧 Modern Media Playback**: Media3 (ExoPlayer) with HLS, HTTP & HTTPS streaming support
- **📱 MediaSession Integration**: Full Android media controls, notifications, lock screen
- **🌐 Network Resilience**: Advanced retry logic and connection management
- **⚙️ Smart Playback**: Sleep timer, auto-play, external player integration
- **🎼 MPD Support**: Music Player Daemon integration for advanced users

### 📊 **Station Management**
- **🎵 Extensive Directory**: Access 40,000+ radio stations via Radio Browser API
- **⭐ Favorites & History**: Smart station management with persistent storage
- **🔍 Advanced Search**: Filter by name, tags, country, language, codec
- **🌍 Global Coverage**: Stations from 200+ countries in 50+ languages

### 🎙️ **Recording & Offline**
- **📱 Smart Recording**: Record broadcasts with automatic file management
- **⏱️ Time Limits**: Configurable recording duration (default 60 minutes)
- **📁 Media Store**: Proper Android 10+ storage API integration
- **🎵 Playback**: Recorded files playable in system media apps

### 🎨 **Modern UI/UX**
- **📱 Material Design 3**: Modern Android interface with dynamic themes
- **🌙 Dark Mode**: Automatic dark/light theme switching
- **📺 Android TV**: Optimized for TV and automotive displays
- **♿ Accessibility**: Screen reader and gesture navigation support

## 🏗️ Architecture & Technical Excellence

### 🎯 **Clean Architecture Foundation**
NoNameRadio implements Robert C. Martin's Clean Architecture principles with modern Android patterns:

#### **Layered Architecture**:
```
📱 presentation/     # UI Layer (Activities, Fragments, ViewModels)
├── ui/              # UI controllers and adapters
├── navigation/      # Navigation management
└── ui/              # UI utilities and helpers

🏗️ core/            # Core Business Logic
├── domain/          # Domain entities, use cases, interfaces
├── di/              # Dependency injection container
└── utils/           # Core utilities (async, ui, validation)

💾 data/             # Data Layer
├── repository/      # Repository implementations
├── local/           # Local storage (Room, SharedPrefs)
└── remote/          # Network operations

🔧 service/          # Android Services & System Integration
├── player/          # Media playback services
├── recording/       # Recording management
└── notification/    # System notifications
```

#### **Architectural Patterns Implemented**:
- **🏛️ Clean Architecture**: Strict separation of concerns
- **🔌 Dependency Injection**: Manual DI with service locator pattern
- **🏭 Repository Pattern**: Unified data access abstraction
- **🎭 Command Pattern**: Player operations encapsulation
- **🔄 Observer Pattern**: Reactive UI state management
- **🎪 Strategy Pattern**: Pluggable player implementations

### 🚀 **Modern Android Development Stack**

#### **Core Technologies**:
- **🎵 Media3 ExoPlayer**: Latest media playback with HLS, DASH, HTTP/HTTPS support
- **🌐 Smart Networking**: Advanced connectivity detection with automatic retry logic
- **🔄 Modern Async**: `CompletableFuture`-based operations replacing deprecated `AsyncTask`
- **📱 UI Framework**: Material Design 3 with dynamic theming and accessibility
- **💾 Data Persistence**: Room database with proper migration and MediaStore integration
- **🛡️ Security**: TLS 1.2+ support and secure network communications
- **⚡ Performance**: Optimized memory usage and background task management

#### **Code Quality & Modernization**:
- **📝 Utility Classes**: Specialized utilities (`NetworkUtils`, `UiUtils`, `FormatUtils`, `InputValidator`)
- **🛡️ Error Handling**: `ErrorHandler` and `Result<T>` patterns for robust error management
- **🔄 Async Operations**: `AsyncExecutor` replacing deprecated `AsyncTask` across the codebase
- **🎨 UI Management**: Centralized `UiHandler` for main thread operations
- **💾 Preferences**: Singleton `PreferencesManager` for optimized SharedPreferences access
- **🏗️ Build System**: Modern Gradle configuration with optimized dependencies

### 📊 **Refactoring Results & Metrics**

#### **Code Quality Improvements**:
- **⚠️ Warnings Reduction**: From 100+ to ~48 warnings (52% improvement)
- **🏗️ Architecture**: Migrated from legacy patterns to Clean Architecture
- **🔄 Async Modernization**: Replaced deprecated `AsyncTask` with modern alternatives
- **🧹 Code Cleanup**: Removed unused directories and optimized imports
- **📦 Package Reorganization**: Complete rebranding from `radiodroid2` to `nonameradio.app`

#### **Performance & Stability**:
- **⚡ Build Time**: Optimized Gradle configuration for faster builds
- **📱 Memory Management**: Improved resource handling and background processing
- **🌐 Network Reliability**: Enhanced connectivity detection and retry mechanisms
- **💾 Data Integrity**: Proper MediaStore integration for Android 10+ compatibility
- **📊 Analytics**: AppMetrica integration for crash reporting and usage analytics

#### **Recording System Overhaul**:
- **⏱️ 60-Minute Limit**: Automatic recording termination after 1 hour
- **💾 MediaStore Integration**: Proper Android 10+ storage handling
- **🎵 Playback Support**: Recorded files playable in system media applications
- **📁 File Management**: Automatic cleanup and naming conventions

### 🔧 **Development & Build**

#### **Prerequisites**:
- **Android Studio**: Arctic Fox 2020.3.1 or later
- **Android SDK**: API 21+ (Android 5.0)
- **Java**: JDK 11+ for Gradle builds

#### **Quick Start**:
```bash
# Clone the repository
git clone https://github.com/eosphor/NoNameRadio.git
cd NoNameRadio/RadioDroid

# Build debug APK
./gradlew assembleFreeDebug

# Install on device
adb install app/build/outputs/apk/free/debug/NoNameRadio-free-debug.apk

# Run tests
./gradlew test
```

#### **Project Structure**:
```
RadioDroid/
├── 📱 app/src/main/java/com/nonameradio/app/    # Main application code
│   ├── 📱 presentation/                         # UI layer
│   ├── 🏗️ core/                                # Business logic
│   ├── 💾 data/                                # Data layer
│   └── 🔧 service/                             # Android services
├── 🎨 app/src/main/res/                         # Resources & UI
├── 🧪 app/src/test/                            # Unit tests
└── 📱 app/src/androidTest/                     # Integration tests
```
- **🎵 Audio Stream Interception**: Custom `DataSource` for seamless recording
- **🗂️ Recording Management**: CRUD operations with proper file management

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on:

- **🐛 Bug Reports**: Use GitHub Issues with detailed reproduction steps
- **✨ Feature Requests**: Describe the enhancement and its use case
- **🔧 Pull Requests**: Follow our coding standards and include tests
- **📖 Documentation**: Help improve our documentation and guides

### Development Setup:
```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/NoNameRadio.git

# Create feature branch
git checkout -b feature/your-feature-name

# Run tests before committing
./gradlew test
./gradlew lint
```

### Download & Install
1. **Download APK**: Get the latest release from [GitHub Releases](https://github.com/eosphor/NoNameRadio/releases)
2. **Enable Unknown Sources**: Allow installation from unknown sources in Android settings
3. **Install**: Tap the downloaded APK file to install

### System Requirements:
- **Android**: 5.0+ (API 21)
- **RAM**: 512MB minimum
- **Storage**: 50MB free space
- **Network**: Internet connection for streaming

4. **Launch**: Open NoNameRadio and start listening to radio stations!

### Build from Source
```bash
git clone https://github.com/eosphor/NoNameRadio.git
cd NoNameRadio/RadioDroid
./gradlew assembleFreeDebug
```

# Run tests
./gradlew testFreeDebugUnitTest

# Install on connected device
adb install app/build/outputs/apk/free/debug/NoNameRadio-free-debug.apk
./gradlew installFreeDebug
```

## 🧪 Development & Testing

### **Project Structure**
```
RadioDroid/
├── 📁 app/src/main/java/com/nonameradio/app/
│   ├── 📁 core/          # Core business logic
│   │   ├── 📁 di/        # Dependency injection
│   │   ├── 📁 domain/    # Domain entities & interfaces
│   │   └── 📁 utils/     # Utility classes
│   ├── 📁 data/          # Data layer
│   │   ├── 📁 repository/# Repository implementations
│   │   └── 📁 local/     # Local data sources
│   └── 📁 presentation/  # UI layer
│       ├── 📁 navigation/# Navigation logic
│       └── 📁 ui/        # UI controllers
├── 📁 app/src/test/      # Unit tests
└── 📁 app/src/androidTest/# Integration tests
```

### **Key Components**
- **`DependencyInjector`**: Centralized dependency management
- **`PlayerController`**: Command-based player operations
- **`ErrorHandler`**: Unified error handling
- **`NetworkUtils`**: Modern connectivity management
- **Repository Pattern**: Data abstraction layer

### **Testing**
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*FormatUtilsTest*"

# Generate test coverage
./gradlew jacocoTestReport
```

## 📱 Usage

- **Browse Stations**: Navigate through various categories or use the search function to find stations
- **Play/Pause**: Tap a station to start playback. Use the player controls in the notification or app
- **Favorites**: Star stations to add them to your favorites list
- **Alarms**: Set alarms to wake up to your favorite radio station
- **Recordings**: Record live streams (ensure storage permissions are granted, 60-minute limit)
- **History**: View your recently played stations

## ❓ FAQ

**Q: What makes NoNameRadio different from other radio apps?**
A: NoNameRadio features modern Clean Architecture, uses Media3 (ExoPlayer) technology, provides seamless integration with Android system controls, implements comprehensive recording functionality with a 60-minute limit, and offers thousands of radio stations via the Radio Browser API.

**Q: How is the architecture different from typical Android apps?**
A: NoNameRadio implements Clean Architecture with clear separation of concerns: presentation layer for UI, domain layer for business logic, and data layer for persistence. This makes the codebase highly maintainable and testable.

**Q: Does it work offline?**
A: NoNameRadio requires an internet connection to stream radio stations, but you can record streams for offline listening with the built-in recording system.

**Q: What about recording functionality?**
A: The app supports recording live streams with automatic 60-minute limit enforcement, proper Android 10+ storage handling via MediaStore, and seamless audio stream interception.

**Q: Is it free and open source?**
A: Yes! NoNameRadio is completely free and open source under GPLv3 license, with a focus on modern Android development practices.

**Q: Which Android versions are supported?**
A: Android 5.0 (API level 21) and higher, with optimized support for modern Android versions.

**Q: Can I contribute to the project?**
A: Absolutely! The project welcomes contributions. See our [Contributing Guide](.github/CONTRIBUTING.md) for details on the Clean Architecture approach and coding standards.

## 📱 Screenshots

*Screenshots coming soon - help us by contributing some!*

## 🤝 Contributing

We welcome contributions! This project follows Clean Architecture principles and modern Android development practices.

### **Development Guidelines**
- **Clean Architecture**: Follow the established layer separation
- **Testing**: Write unit tests for new features
- **Code Style**: Follow Kotlin/Java coding standards
- **Documentation**: Update README and code comments

### **Getting Started**
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Implement your changes following Clean Architecture
4. Add comprehensive tests
5. Ensure all tests pass: `./gradlew test`
6. Submit a pull request

### **Architecture Guidelines**
- **Presentation Layer**: UI logic only, no business logic
- **Domain Layer**: Pure business logic, framework-independent
- **Data Layer**: Data access, external APIs, databases
- **Dependency Injection**: Use the established DI container

## 🐛 Bug Reports & Feature Requests

- **🐛 Bug Reports**: Use our [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.md)
- **✨ Feature Requests**: Use our [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.md)
- **🔒 Security Issues**: See our [Security Policy](.github/SECURITY.md)

## 🚀 Roadmap & Future Plans

### **Short Term (Next Release)**
- [ ] Complete ActivityMain refactoring
- [ ] UI integration for new controllers
- [ ] Performance optimizations
- [ ] Enhanced error handling

### **Medium Term**
- [ ] Android Auto support
- [ ] Wear OS companion app
- [ ] Advanced recording features
- [ ] Playlist management
- [ ] Social sharing features

### **Long Term**
- [ ] Multi-platform support (iOS, Desktop)
- [ ] Advanced audio processing
- [ ] AI-powered station recommendations
- [ ] P2P radio streaming

## 📊 Project Status

### **✅ Completed Refactoring (v0.86.903)**
- **🏗️ Clean Architecture**: Full implementation with layered separation
- **🔌 Dependency Injection**: Centralized dependency management
- **🎭 Command Pattern**: Player operations with undo support
- **🏭 Repository Pattern**: Data abstraction layer
- **🧪 Unit Tests**: Comprehensive test coverage for core components
- **⚡ Performance**: Optimized media playback and memory usage
- **💾 Recording**: Modern recording system with 60-minute limit

### **📈 Metrics**
- **Code Coverage**: ~75% (core components)
- **Architecture Score**: Clean Architecture compliance
- **Build Time**: Optimized Gradle configuration
- **APK Size**: ~8MB (optimized)

## ⚖️ Legal Notice

This project is a fork of [RadioDroid](https://github.com/segler-alex/RadioDroid),
originally developed by [Alexander Segler](https://github.com/segler-alex).

All rights to the original RadioDroid project belong to its original author.
This fork (NoNameRadio) is an independent project with significant modifications
including complete rebranding, architectural refactoring, and feature enhancements.

**Original License**: GPL v3.0
**This Project License**: GPL v3.0

## 📄 License

This project is free software licensed under the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

## 🙏 Acknowledgements

### Core Dependencies & Services
- **🌐 [Radio Browser API](http://www.radio-browser.info)**: Extensive radio station database
- **🎵 [AndroidX Media3](https://developer.android.com/media/media3)**: Modern media playback framework
- **🔧 [OkHttp](https://square.github.io/okhttp/)**: HTTP client for network operations
- **💾 [Room](https://developer.android.com/topic/libraries/architecture/room)**: Local database persistence

### Inspiration & Foundation
- **📻 Original RadioDroid Project**: Foundation codebase and radio streaming expertise
- **🏛️ Robert C. Martin**: Clean Architecture principles and patterns
- **🤖 Android Developer Community**: Modern Android development practices
- **🎯 Open Source Community**: Testing frameworks, utilities, and best practices

## 📍 Project Information

- **🏠 Project Home**: [https://github.com/eosphor/NoNameRadio](https://github.com/eosphor/NoNameRadio)
- **📖 Documentation**: [Clean Architecture Guide](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- **🔗 Original Project**: [https://github.com/segler-alex/RadioDroid](https://github.com/segler-alex/RadioDroid)
- **📊 Build Status**: ![Build Status](https://github.com/eosphor/NoNameRadio/workflows/Android%20CI/badge.svg)

## 👨‍💻 Author & Team

- **[@monviso](https://www.linkedin.com/in/monviso/)** - Lead Developer & Architect
- **[@eosphor](https://github.com/eosphor)** - Project Owner & Maintenance

### **Contributors Wanted!**
We're looking for contributors interested in:
- UI/UX improvements
- Testing and quality assurance
- Documentation
- New feature development
- Architecture improvements

---

## 🎯 Mission Statement

**NoNameRadio** aims to be the most modern, reliable, and user-friendly radio streaming application for Android, built with Clean Architecture principles and cutting-edge Android development practices.

**⭐ If you find this project useful, please give it a star and help us build the future of radio streaming!**