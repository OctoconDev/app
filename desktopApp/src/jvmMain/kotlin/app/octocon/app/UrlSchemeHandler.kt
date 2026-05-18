package app.octocon.app

import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object UrlSchemeHandler {
    private const val URL_SCHEME = "octocon"

    /**
     * Registers the octocon:// URL scheme with the operating system.
     * This allows the browser to redirect OAuth callbacks to the desktop app.
     */
    fun registerUrlScheme() {
        val osName = System.getProperty("os.name").lowercase()

        when {
            osName.contains("win") -> registerWindowsUrlScheme()
            osName.contains("mac") -> registerMacUrlScheme()
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> registerLinuxUrlScheme()
        }
    }

    private fun registerWindowsUrlScheme() {
        try {
            val appPath = getApplicationPath()

            // Create registry entries for Windows
            val regCommands = listOf(
                """reg add "HKEY_CURRENT_USER\Software\Classes\$URL_SCHEME" /ve /d "URL:Octocon Protocol" /f""",
                """reg add "HKEY_CURRENT_USER\Software\Classes\$URL_SCHEME" /v "URL Protocol" /d "" /f""",
                """reg add "HKEY_CURRENT_USER\Software\Classes\$URL_SCHEME\shell\open\command" /ve /d "\"$appPath\" \"%1\"" /f"""
            )

            regCommands.forEach { command ->
                Runtime.getRuntime().exec(arrayOf("cmd", "/c", command)).waitFor()
            }

            println("Windows URL scheme registered successfully")
        } catch (e: Exception) {
            println("Failed to register Windows URL scheme: ${e.message}")
        }
    }

    private fun registerMacUrlScheme() {
        try {
            // On macOS, URL scheme registration is typically done through Info.plist
            // For runtime registration, we can use NSUserDefaults but this requires native code
            // As a workaround, we'll create a plist file if needed
            println("macOS URL scheme registration should be configured in the app bundle's Info.plist")
            println("Add CFBundleURLTypes configuration to your packaged app")
        } catch (e: Exception) {
            println("Failed to register macOS URL scheme: ${e.message}")
        }
    }

    private fun registerLinuxUrlScheme() {
        try {
            val appPath = getApplicationPath()
            val homeDir = System.getProperty("user.home")
            val desktopFile = File("$homeDir/.local/share/applications/octocon-url-handler.desktop")

            desktopFile.parentFile.mkdirs()

            val desktopEntry = """
                [Desktop Entry]
                Type=Application
                Name=Octocon URL Handler
                Exec=$appPath %u
                MimeType=x-scheme-handler/$URL_SCHEME
                NoDisplay=true
                Terminal=false
            """.trimIndent()

            desktopFile.writeText(desktopEntry)

            // Register the MIME type
            Runtime.getRuntime().exec(arrayOf(
                "xdg-mime", "default", "octocon-url-handler.desktop", "x-scheme-handler/$URL_SCHEME"
            )).waitFor()

            println("Linux URL scheme registered successfully")
        } catch (e: Exception) {
            println("Failed to register Linux URL scheme: ${e.message}")
        }
    }

    private fun getApplicationPath(): String {
        val jarPath = File(
            UrlSchemeHandler::class.java.protectionDomain.codeSource.location.toURI()
        ).absolutePath

        //TODO: Assume true for now
        // If running from JAR, return java command to launch it
        /*return if (jarPath.endsWith(".jar")) {
            val javaHome = System.getProperty("java.home")
            "\"$javaHome/bin/java\" -jar \"$jarPath\""
        } else {
            // Development mode - create a launcher script
            */return createDevelopmentLauncher()
        //}
    }

    private fun createDevelopmentLauncher(): String {
        val osName = System.getProperty("os.name").lowercase()
        val isWindows = osName.contains("win")

        // Find project root (assuming we're running from desktopApp module)
        val projectRoot = findProjectRoot() ?: run {
            println("Warning: Could not find project root, URL scheme may not work in development mode")
            return ""
        }

        val tmpDir = System.getProperty("java.io.tmpdir")
        val launcherFile = if (isWindows) {
            File(tmpDir, "octocon-dev-launcher.bat")
        } else {
            File(tmpDir, "octocon-dev-launcher.sh")
        }

        val launcherScript = if (isWindows) {
            """
                @echo off
                cd "$projectRoot"
                "$projectRoot\gradlew.bat" :desktopApp:run --args=%*
            """.trimIndent()
        } else {
            """
                #!/bin/bash
                cd "$projectRoot"
                "$projectRoot/gradlew" :desktopApp:run --args="${'$'}*"
            """.trimIndent()
        }

        launcherFile.writeText(launcherScript)
        if (!isWindows) {
            launcherFile.setExecutable(true)
        }

        return launcherFile.absolutePath
    }

    private fun findProjectRoot(): String? {
        var currentDir = File(
            UrlSchemeHandler::class.java.protectionDomain.codeSource.location.toURI()
        ).parentFile

        // Walk up the directory tree looking for gradlew or gradlew.bat
        // These files exist only at the project root
        while (currentDir != null) {
            val hasGradlewBat = File(currentDir, "gradlew.bat").exists()
            val hasGradlew = File(currentDir, "gradlew").exists()

            if (hasGradlewBat || hasGradlew) {
                println("Found project root at: ${currentDir.absolutePath}")
                return currentDir.absolutePath
            }
            currentDir = currentDir.parentFile
        }

        println("Could not find project root (no gradlew/gradlew.bat found)")
        return null
    }

    /**
     * Check if we're running in development mode
     */
    fun isDevMode(): Boolean {
        return app.octocon.app.utils.BuildConfig.isDebug()
    }

    /**
     * Parses a deep link URL and extracts relevant information
     * Format: octocon://deep/auth/token?token=xxx&id=yyy
     */
    fun parseDeepLink(url: String): DeepLinkData? {
        if (!url.startsWith("$URL_SCHEME://")) {
            return null
        }

        return try {
            val urlWithoutScheme = url.removePrefix("$URL_SCHEME://")
            val parts = urlWithoutScheme.split("?", limit = 2)
            val path = parts[0]
            val queryParams = if (parts.size > 1) parseQueryParams(parts[1]) else emptyMap()

            DeepLinkData(path = path, queryParams = queryParams)
        } catch (e: Exception) {
            println("Failed to parse deep link: ${e.message}")
            null
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        return query.split("&")
            .mapNotNull { param ->
                val keyValue = param.split("=", limit = 2)
                if (keyValue.size == 2) {
                    URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name()) to 
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name())
                } else {
                    null
                }
            }
            .toMap()
    }
}

data class DeepLinkData(
    val path: String,
    val queryParams: Map<String, String>
)
