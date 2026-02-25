plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.apollographql.apollo3")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // app/build.gradle.kts
    composeOptions {
        // ✅ 1.9.24 对应的正确版本
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Apollo 3.8.0
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.0")

    // ✅ Compose BOM（版本管理）
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // ✅ Compose 核心库
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ✅ Activity Compose 集成
    implementation("androidx.activity:activity-compose:1.8.2")

    // ✅ ViewModel Compose 集成
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
}

// ✅ Apollo 3.x 正确配置！
apollo {
    // 必须要有 service 块！
    service("service") {
        // 包名在这里设置
        packageName.set("com.example.myapplication")
        // 生成 Kotlin 代码
        generateKotlinModels.set(true)


        // ✅ 使用 introspection 方式，不从 URL 直接下载
        introspection {
            endpointUrl.set("https://beta.pokeapi.co/graphql/v1beta")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }

        // ✅ 指定 GraphQL 文件位置
        srcDir("src/main/graphql")
    }
}