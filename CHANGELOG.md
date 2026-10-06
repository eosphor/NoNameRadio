# Changelog

All notable changes to NoNameRadio will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.87.2] - 2026-10-06

### Security
- Removed the "trust all certificates / accept any hostname" OkHttp configuration that was applied
  to every HTTP client on Android 8.0 (API 26, the current minSdk); TLS is now validated normally
- Alarm receiver is no longer exported; test-only alarm components (`TestAlarmActivity`,
  `AlarmSetupReceiver`) are declared only in debug builds
- `BootReceiver` ignores intents other than `BOOT_COMPLETED`
- Dropped unused `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` permissions
- Last.fm metadata requests use HTTPS with properly encoded query parameters

### Fixed
- Radio-browser API requests again use the original implementation (response cache, JSON POST
  parameters, server failover); the simplified `NetworkUtils` path dropped the cache and sent
  unencoded query parameters, breaking searches containing characters like `&` or spaces
- `getStationById` built a request from a relative URL
- Background downloads no longer use `getActivity()` from worker threads (NPE when the fragment is detached)
- Analytics playback event JSON is now properly escaped
- `UtilsTest` used JUnit 5 annotations with the JUnit 4 runner and Kotlin `assert`, so it never ran

### Changed
- LeakCanary: replaced `leakcanary-object-watcher-android:3.0-alpha-8` (watcher only, no analysis)
  with the stable `leakcanary-android:2.14`, declared via the version catalog
- AppMetrica SDK logs are enabled only in debug builds
- Removed unused/dead code: `NetworkUtils`, `Tls12SocketFactory`, pre-Lollipop TLS workarounds,
  unused JUnit 5 dependencies
- CI: Gradle caching via `gradle/actions/setup-gradle`, unit tests job, `action-gh-release@v2`
- Repository cleanup: removed committed `.git` backup directory, `.DS_Store`, build log;
  `.gitignore` no longer ignores the `gradle/` directory (version catalog and wrapper)

## [Unreleased]

### Added
- Media3 Session integration with MediaSessionService
- Modern NetworkCallback for connectivity monitoring
- Enhanced ExoPlayer retry logic with exponential backoff
- BroadcastReceiver for real-time UI state synchronization
- Gradle version catalogs with libs.versions.toml configuration
- Centralized dependency management through version catalogs
- Common lint configurations for consistent code quality
- Kotlin DSL migration for all build scripts
- Plugin management through version catalogs

### Changed
- Updated Media3 to version 1.8.0
- Replaced custom DataSource with DefaultMediaSourceFactory
- Modernized connectivity handling (removed CONNECTIVITY_ACTION)
- Updated AndroidX dependencies to latest compatible minor versions
- Updated Material Design components to latest stable releases
- Updated Glide image loading library to latest minor version
- Updated OkHttp networking library to latest compatible version
- Updated Room database library to latest minor release
- Migrated dependency versions to centralized libs.versions.toml
- Migrated all build scripts from Groovy to Kotlin DSL
- Centralized plugin management through version catalogs

### Removed
- MediaPlayer fallback (obsolete with minSdk 21)
- Custom RadioDataSourceFactory and IcyDataSource
- Debug logging for production readiness

### Fixed
- UI state synchronization for play/pause button
- ICY metadata display in mini-player
- RecyclerView performance with large station lists
- Null pointer exceptions in player fragments

## [0.86.903] - 2024-09-23

### Added
- Complete rebranding from RadioDroid to NoNameRadio
- AppMetrica analytics integration
- GitHub Actions CI/CD workflows
- Comprehensive documentation and README

### Changed
- Application ID to com.nonameradio.app
- All string resources and UI text
- Package structure and naming

### Security
- Updated all dependencies to latest stable versions
- Removed deprecated API usage
