plugins {
    alias(libs.plugins.kombucha.android.library)
    alias(libs.plugins.kombucha.jetpackCompose.library)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.github.ikarenkov.sample.game"
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.debug.logcat)

    implementation(libs.modo)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.constraintLayout)

    implementation(projects.kombucha.core)

    implementation(projects.sample.core.feature)

}

//impl(
//    packageName = "ru.ikarenkov.teamaker.game",
//    compose = true,
//    dependencies = deps(
//        androidx.compose.base,
//        androidx.compose.constraintLayout,
//        androidx.compose.accompanist.insets,
//        androidx.compose.accompanist.systemUiController,
//        di.koinAndroid,
//        log.logcat,
//    ) + deps(
//        tea.core,
//        tea.compose,
//        navigation.modoCompose,
//        core.feature
//    )
//).withPlugin(Plugins.parcelize)