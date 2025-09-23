# Changelog

All notable changes to NoNameRadio will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Media3 Session integration with MediaSessionService
- Modern NetworkCallback for connectivity monitoring
- Enhanced ExoPlayer retry logic with exponential backoff
- BroadcastReceiver for real-time UI state synchronization

### Changed
- Updated Media3 to version 1.8.0
- Replaced custom DataSource with DefaultMediaSourceFactory
- Modernized connectivity handling (removed CONNECTIVITY_ACTION)

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
