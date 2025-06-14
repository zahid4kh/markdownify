import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.UUID
import org.jetbrains.compose.reload.ComposeHotRun 
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import java.util.Scanner

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.hotReload)
}

group = "zahid4kh.markdownrenderer"
version = "1.0.0"

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)

    // Koin for dependency injection
    implementation(libs.koin.core)

    implementation(libs.deskit)

    implementation(libs.coil)
    implementation(libs.coil.svg)
    implementation(libs.coil.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.slf4j:slf4j-simple:2.0.12")

    implementation("com.vdurmont:semver4j:3.1.0")
}


compose.desktop {
    application {
        /*
        must match the annotation in Main.kt
        @file:JvmName("markdownrenderer").
        This also sets the app's dock name on Linux.
         */
        mainClass = "markdownrenderer"

        nativeDistributions {
            jvmArgs += listOf(
                "-Dfile.encoding=UTF-8"
            )

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "markdownify"
            packageVersion = "1.0.0"

            buildTypes.release.proguard {
                configurationFiles.from("proguard-rules.pro")
                isEnabled = true
                obfuscate = false
                optimize = false
            }

            linux{
                shortcut = true
                debMaintainer = "Zahid Khalilov <halilzahid@gmail.com>"
                iconFile.set(project.file("icons/linuxos.png"))
            }

            windows{
                shortcut = true
                dirChooser = true
                menu = true
                upgradeUuid = "020432f7-f1a8-4c61-b5a8-bdc2e6a5d155"
                iconFile.set(project.file("icons/windows.ico"))
            }

            macOS{
                dockName = "Markdownify"
                iconFile.set(project.file("icons/macos.icns"))
            }
        }
    }
}

//https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

tasks.register("generateUpgradeUuid") {
    group = "help"
    description = "Generates a unique UUID to be used for the Windows MSI upgradeUuid."
    doLast {
        println("--------------------------------------------------")
        println("Generated Upgrade UUID (must be pasted in the upgradeUuid for windows block only once so the MSI installer recognizes the update and does the uninstall/install):")
        println(UUID.randomUUID().toString())
        println("--------------------------------------------------")
    }
}

compose.resources{
    publicResClass = false
    packageOfResClass = "markdownify.resources"
    generateResClass = auto
}

// only for LinuxOS
val workDir = file("deb-temp")
val packageName = "${compose.desktop.application.nativeDistributions.packageName}"
val desktopRelativePath = "opt/$packageName/lib/$packageName-$packageName.desktop"
val appDisplayName = "Markdownify"
val maintainer = "Zahid Khalilov <halilzahid@gmail.com>"
val controlDescription = "A powerful desktop markdown editor with live preview"
val mainClass = "${compose.desktop.application.mainClass}"

fun promptUserChoice(): String {
    println(
        """
        🧩 Which packaging task do you want to run?
        1 = packageDeb (default)
        2 = packageReleaseDeb
        """.trimIndent()
    )
    print("👉 Enter your choice [1/2]: ")

    return Scanner(System.`in`).nextLine().trim().ifEmpty { "1" }
}

tasks.register("addStartupWMClassToDebDynamic") {
    group = "release"
    description = "Finds .deb file, modifies .desktop and control files, and rebuilds it"

    doLast {
        val debRoot = file("build/compose/binaries")
        if (!debRoot.exists()) throw GradleException("❌ Folder not found: ${debRoot}")

        val allDebs = debRoot.walkTopDown().filter { it.isFile && it.extension == "deb" }.toList()
        if (allDebs.isEmpty()) throw GradleException("❌ No .deb files found under ${debRoot}")

        // picking the latest .deb file
        val originalDeb = allDebs.maxByOrNull { it.lastModified() }!!
        println("📦 Found deb package: ${originalDeb.relativeTo(rootDir)}")

        val modifiedDeb = File(originalDeb.parentFile, originalDeb.nameWithoutExtension + "-wm.deb")

        // cleaning up "deb-temp" folder, if exists
        if (workDir.exists()) workDir.deleteRecursively()
        workDir.mkdirs()

        // Step 1: Extracting generated debian package
        exec {
            commandLine("dpkg-deb", "-R", originalDeb.absolutePath, workDir.absolutePath)
        }

        // Step 2: Modifying the desktop entry file
        val desktopFile = File(workDir, desktopRelativePath)
        if (!desktopFile.exists()) throw GradleException("❌ .desktop file not found: ${desktopRelativePath}")

        val lines = desktopFile.readLines().toMutableList()

        // Modifying the Name field (app's display name on dock)
        var nameModified = false
        for (i in lines.indices) {
            if (lines[i].trim().startsWith("Name=")) {
                lines[i] = "Name=$appDisplayName"
                nameModified = true
                println("✅ Modified Name entry to: $appDisplayName")
                break
            }
        }

        // adding Name field if it doesn't exist
        if (!nameModified) {
            lines.add("Name=$appDisplayName")
            println("✅ Added Name entry: $appDisplayName")
        }

        for (i in lines.indices) {
            if (lines[i].trim().startsWith("StartupWMClass=")) {
                if (lines[i] != "StartupWMClass=$mainClass") {
                    lines[i] = "StartupWMClass=$mainClass"
                    println("✅ Updated StartupWMClass entry to: $mainClass")
                } else {
                    println("ℹ️ StartupWMClass already correctly set to: $mainClass")
                }
                break
            }
        }

        // Adding StartupWMClass if it doesn't exist
        if (!lines.any { it.trim().startsWith("StartupWMClass=") }) {
            lines.add("StartupWMClass=$mainClass")
            println("✅ Added StartupWMClass entry: $mainClass")
        }

        // Writing changes back to file
        desktopFile.writeText(lines.joinToString("\n"))

        println("\n📄 Final .desktop file content:")
        println("--------------------------------")
        desktopFile.readLines().forEach { println(it) }
        println("--------------------------------\n")

        // Step 3: Modifying the DEBIAN/control file
        val controlFile = File(workDir, "DEBIAN/control")
        if (!controlFile.exists()) throw GradleException("❌ control file not found: DEBIAN/control")

        val controlLines = controlFile.readLines().toMutableList()

        // Update maintainer field
        var maintainerModified = false
        for (i in controlLines.indices) {
            if (controlLines[i].trim().startsWith("Maintainer:")) {
                controlLines[i] = "Maintainer: $maintainer"
                maintainerModified = true
                println("✅ Modified Maintainer entry")
                break
            }
        }

        // Add maintainer field if it doesn't exist
        if (!maintainerModified) {
            controlLines.add("Maintainer: $maintainer")
            println("✅ Added Maintainer entry")
        }

        // Update description field for better info
        for (i in controlLines.indices) {
            if (controlLines[i].trim().startsWith("Description:")) {
                controlLines[i] = "Description: $controlDescription"
                println("✅ Modified Description entry")
                break
            }
        }

        // Write changes back to control file
        controlFile.writeText(controlLines.joinToString("\n"))

        println("\n📄 Final control file content:")
        println("--------------------------------")
        controlFile.readLines().forEach { println(it) }
        println("--------------------------------\n")

        // Step 4: Repackaging the debian package back
        exec {
            commandLine("dpkg-deb", "-b", workDir.absolutePath, modifiedDeb.absolutePath)
        }

        println("✅ Done: Rebuilt with Name=$appDisplayName, StartupWMClass=$mainClass, and updated control file -> ${modifiedDeb.name}")
    }
}


tasks.register("packageDebWithWMClass") {
    group = "release"
    description = "Runs packaging task (packageDeb or packageReleaseDeb), then adds StartupWMClass"

    doLast {
        val choice = promptUserChoice()

        val packagingTask = when (choice) {
            "2" -> "packageReleaseDeb"
            else -> "packageDeb"
        }

        println("▶️ Running: ${packagingTask}")
        gradle.includedBuilds.forEach { it.task(":${packagingTask}") } // just in case of composite builds

        exec {
            commandLine("./gradlew clean")
            commandLine("./gradlew", packagingTask)
        }

        tasks.named("addStartupWMClassToDebDynamic").get().actions.forEach { it.execute(this) }
    }
}