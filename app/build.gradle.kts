plugins {
    id("com.android.application")
}

android {
    namespace = "example.relay.source"
    compileSdk = 36

    defaultConfig {
        applicationId = "example.relay.source"
        minSdk = 23
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.0-test"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Relay loads this API from its own process. The extension must not package a duplicate copy.
    compileOnly(files(providers.gradleProperty("relaySourceApiJar")
        .orElse("../../relay-source-api/build/libs/relay-source-api.jar")
        .get()))
}
