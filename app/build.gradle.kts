plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
    id("kotlin-parcelize")

    //kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"

}

android {
    namespace = "com.morales.bnatest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.morales.bnatest"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64", "x86", "armeabi-v7a")
        }

        viewBinding {
            enable = true
        }

        aaptOptions {
            // 设置不压缩的文件扩展名，这里将 tflite 文件添加到不压缩列表中
            noCompress.add("tflite")
        }

    }

    chaquopy {
        defaultConfig {

            buildPython("C:/Users/34791/anaconda3/envs/my_env/python.exe")
            pip {
                // A requirement specifier, with or without a version number:
                install("numpy")
                install("pandas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        // 开启数据绑定
        dataBinding {
            isEnabled = true
        }
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
}

dependencies {
    // AndroidX Core KTX
    implementation("androidx.core:core-ktx:1.13.0")
    // AndroidX AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Material Design 组件
    implementation("com.google.android.material:material:1.9.0")
    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Lifecycle LiveData KTX
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // Lifecycle ViewModel KTX
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // Navigation Fragment KTX
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    // Navigation UI KTX
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta02")
    // Fragment KTX
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    // Chaquopy Android
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")


    // 添加 TensorFlow Lite 核心库依赖，使用最新稳定版本
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    // 添加 TensorFlow Lite GPU 加速库依赖，使用最新稳定版本
4    // 添加 TensorFlow Lite 支持库依赖，使用最新稳定版本
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation ("com.google.code.gson:gson:2.8.8")

    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")


    //implementation ("com.github.Othershe:CalendarView:1.2.1")

    //implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")







    // 测试依赖
    testImplementation("junit:junit:4.13.2")
}