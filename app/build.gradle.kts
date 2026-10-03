plugins { id("com.android.application") }
android {
    namespace = "com.hernandovela.reto6"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.hernandovela.reto6"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies { testImplementation("junit:junit:4.13.2") }

