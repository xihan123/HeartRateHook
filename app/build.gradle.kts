import com.android.tools.build.apkzlib.sign.SigningExtension
import com.android.tools.build.apkzlib.sign.SigningOptions
import com.android.tools.build.apkzlib.zfile.ZFiles
import com.android.tools.build.apkzlib.zip.AlignmentRules
import com.android.tools.build.apkzlib.zip.CompressionMethod
import com.android.tools.build.apkzlib.zip.ZFile
import com.android.tools.build.apkzlib.zip.ZFileOptions
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.lsplugin.jgit)
    alias(libs.plugins.lsplugin.resopt)
    alias(libs.plugins.lsplugin.lsparanoid)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val repo = jgit.repo()
val commitCount = (repo?.commitCount("refs/remotes/origin/master") ?: 25)
println("commitCount: $commitCount")
val latestTag = repo?.latestTag?.removePrefix("v") ?: "1.x.x-SNAPSHOT"

val verCode by extra(commitCount  + 25)
val verName by extra(latestTag)
println("verCode: $verCode, verName: $verName")
val androidTargetSdkVersion by extra(35)
val androidMinSdkVersion by extra(26)

lsparanoid {
    seed = null
    classFilter = { true }
    includeDependencies = false
    variantFilter = { true }
}

android {
    namespace = "website.xihan.pbra"
    compileSdk = androidTargetSdkVersion

    signingConfigs {
        create("xihantest") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = verCode
        versionName = verName

        signingConfig = signingConfigs.getByName("xihantest")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
//            "-language-version=2.0",
        )
    }

    packagingOptions.apply {
        resources.excludes += mutableSetOf(
            "META-INF/**",
            "**/*.properties",
            "okhttp3/**",
            "schema/**",
            "**.bin",
            "kotlin-tooling-metadata.json"
        )
        dex.useLegacyPackaging = true
    }

    lint.checkReleaseBuilds = false

    dependenciesInfo.includeInApk = false
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
//    implementation(libs.dexkit)
    implementation(libs.fast.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.serialize)
    compileOnly(libs.xposed.api)
}

tasks.register("stopMiLife") {
    exec {
        commandLine("adb", "shell", "am", "force-stop", "com.mi.health")
    }
}

val restart = task("restart").apply {
    doLast {
        exec {
            commandLine("adb", "shell", "am", "force-stop", "com.mi.health")
        }
        exec {
            commandLine(
                "adb",
                "shell",
                "am",
                "start",
                "$(pm resolve-activity --components com.mi.health)"
            )
        }
    }
}

afterEvaluate {
    tasks.getByPath("installDebug").finalizedBy(restart)
}

val synthesizeDistReleaseApksCI by tasks.registering {
    group = "build"
    dependsOn(":app:packageRelease")
    inputs.files(tasks.named("packageRelease").get().outputs.files)
    val srcApkDir =
        File(project.buildDir, "outputs" + File.separator + "apk" + File.separator + "release")
    if (srcApkDir !in tasks.named("packageRelease").get().outputs.files) {
        val msg = "srcApkDir should be in packageRelease outputs, srcApkDir: $srcApkDir, " +
                "packageRelease outputs: ${tasks.named("packageRelease").get().outputs.files.files}"
        logger.error(msg)
    }
    val outputAbiVariants = mapOf(
        "arm32" to arrayOf("armeabi-v7a"),
        "arm64" to arrayOf("arm64-v8a"),
        "armAll" to arrayOf("armeabi-v7a", "arm64-v8a"),
        "universal" to arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    )
    val versionName = android.defaultConfig.versionName
    val versionCode = android.defaultConfig.versionCode
    val outputDir = File(project.buildDir, "outputs" + File.separator + "ci")
    outputAbiVariants.forEach { (variant, _) ->
        val outputName = "HeartRateHook-v${versionName}-${versionCode}-${variant}.apk"
        outputs.file(File(outputDir, outputName))
    }
    val signConfig = android.signingConfigs.findByName("xihantest")
    val minSdk = android.defaultConfig.minSdk!!
    doLast {
        if (signConfig == null) {
            logger.error("Task :app:synthesizeDistReleaseApksCI: No release signing config found, skip signing")
        }
        val requiredAbiList = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        outputDir.mkdir()
        val options = ZFileOptions().apply {
            alignmentRule = AlignmentRules.constantForSuffix(".so", 4096)
            noTimestamps = true
            autoSortFiles = true
        }
        require(srcApkDir.exists()) { "srcApkDir not found: $srcApkDir" }
        // srcApkDir should have one apk file
        val srcApkFiles =
            srcApkDir.listFiles()?.filter { it.isFile && it.name.endsWith(".apk") } ?: emptyList()
        require(srcApkFiles.size == 1) { "input apk should have one apk file, but found ${srcApkFiles.size}" }
        val inputApk = srcApkFiles.single()
        val startTime = System.currentTimeMillis()
        ZFile.openReadOnly(inputApk).use { srcApk ->
            // check whether all required abis are in the apk
            requiredAbiList.forEach { abi ->
                val path = "lib/$abi/libmmkv.so"
                require(srcApk.get(path) != null) { "input apk should contain $path, but not found" }
            }
            outputAbiVariants.forEach { (variant, abis) ->
                val outputApk =
                    File(outputDir, "HeartRateHook-v${versionName}-${versionCode}-${variant}.apk")
                if (outputApk.exists()) {
                    outputApk.delete()
                }
                ZFiles.apk(outputApk, options).use { dstApk ->
                    if (signConfig != null) {
                        val keyStore =
                            KeyStore.getInstance(signConfig.storeType ?: KeyStore.getDefaultType())
                        FileInputStream(signConfig.storeFile!!).use {
                            keyStore.load(it, signConfig.storePassword!!.toCharArray())
                        }
                        val protParam =
                            KeyStore.PasswordProtection(signConfig.keyPassword!!.toCharArray())
                        val keyEntry = keyStore.getEntry(signConfig.keyAlias!!, protParam)
                        val privateKey = keyEntry as KeyStore.PrivateKeyEntry
                        val signingOptions = SigningOptions.builder()
                            .setMinSdkVersion(minSdk)
                            .setV1SigningEnabled(minSdk < 24)
                            .setV2SigningEnabled(true)
                            .setKey(privateKey.privateKey)
                            .setCertificates(privateKey.certificate as X509Certificate)
                            .setValidation(SigningOptions.Validation.ASSUME_INVALID)
                            .build()
                        SigningExtension(signingOptions).register(dstApk)
                    }
                    // add input apk to the output apk
                    srcApk.entries().forEach { entry ->
                        val cdh = entry.centralDirectoryHeader
                        val name = cdh.name
                        val isCompressed =
                            cdh.compressionInfoWithWait.method != CompressionMethod.STORE
                        if (name.startsWith("lib/")) {
                            val abi = name.substring(4).split('/').first()
                            if (abis.contains(abi)) {
                                dstApk.add(name, entry.open(), isCompressed)
                            }
                        } else if (name.startsWith("META-INF/com/android/")) {
                            // drop gradle version
                        } else {
                            // add all other entries to the output apk
                            dstApk.add(name, entry.open(), isCompressed)
                        }
                    }
                    dstApk.update()
                }
            }
        }
        val endTime = System.currentTimeMillis()
        logger.info("Task :app:synthesizeDistReleaseApksCI: completed in ${endTime - startTime}ms")
    }
}