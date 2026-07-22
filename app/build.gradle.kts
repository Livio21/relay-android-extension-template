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
        versionCode = 3
        versionName = "1.0.2-test"
        manifestPlaceholders["relayHostCertificateSha256"] = providers.gradleProperty("relayHostCertificateSha256")
            .orElse("REPLACE_WITH_LOWERCASE_SHA256")
            .get()
    }
}

kotlin {
    jvmToolchain(17)
}
