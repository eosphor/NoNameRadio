    signingConfigs {
        create("release") {
            // Временная подпись релизов для тестирования: используем стандартный debug.keystore
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }
    }
