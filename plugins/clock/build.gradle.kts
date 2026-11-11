plugins { id("com.android.application") }

android {
    namespace = "app.nookscreen.plugin.clock"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.nookscreen.plugin.clock"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies { implementation(project(":plugin-api")) }
