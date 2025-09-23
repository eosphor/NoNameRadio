# NoNameRadio

A modern Android radio streaming application based on the [Radio Browser](http://www.radio-browser.info) API. This is a fork of RadioDroid with enhanced features and a modernized architecture.

Project home: `https://github.com/eosphor/NoNameRadio`

Original project: `https://github.com/morckx/RadioDroid` (initially: `https://github.com/segler-alex/RadioDroid`)

## Features

- **Stream thousands of radio stations** from around the world
- **Modern Media3 architecture** with ExoPlayer integration
- **Media Session support** for system integration (lock screen controls, Android Auto, etc.)
- **Favorites and History** management
- **Recording capabilities** for offline listening
- **Sleep timer** functionality
- **Alarm clock** with radio station wake-up
- **Material Design** interface
- **Dark/Light themes** support
- **Multiple languages** support

## Technical Improvements

This fork includes several modernizations over the original RadioDroid:

- **Media3 ExoPlayer** integration with HLS support
- **MediaSessionService** for proper media controls
- **OkHttp** data source for better network handling
- **Modern Android architecture** components
- **Updated dependencies** to latest stable versions
- **Removed deprecated APIs** and legacy code

## Installation

### From Source

1. Clone the repository:
   ```bash
   git clone https://github.com/eosphor/NoNameRadio.git
   cd NoNameRadio
   ```

2. Open in Android Studio and build the project

3. Install the APK on your device

### Build Requirements

- Android Studio Arctic Fox or later
- Android SDK API 21+ (Android 5.0+)
- Gradle 8.10+
- Kotlin 1.9.24+

## Usage

1. **Browse Stations**: Use the search functionality to find radio stations by name, country, or genre
2. **Play Music**: Tap on any station to start streaming
3. **Add to Favorites**: Star stations you like for quick access
4. **Record**: Use the record button to save streams for offline listening
5. **Set Alarms**: Configure radio alarms to wake up to your favorite station

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## License

This project is licensed under the GPLv3 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Original RadioDroid**: [morckx/RadioDroid](https://github.com/morckx/RadioDroid)
- **Radio Browser API**: [radio-browser.info](http://www.radio-browser.info)
- **Android Media3**: Google's modern media playback framework

## Author

Created by [@monviso](https://www.linkedin.com/in/monviso/)