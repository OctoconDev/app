package app.octocon.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * Manages single instance behavior for the desktop application.
 * When a new instance is launched (e.g., from a URL click), it sends
 * the data to the running instance and exits.
 */
class SingleInstanceManager(
    private val port: Int = 57832, // Random port for IPC
    private val onDeepLinkReceived: (String) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    /**
     * Attempts to acquire single instance lock.
     * Returns true if this is the first instance, false if another instance is running.
     */
    fun tryAcquireLock(deepLinkUrl: String? = null): Boolean {
        return try {
            // Try to connect to existing instance
            if (tryConnectToExistingInstance(deepLinkUrl)) {
                // Successfully sent to existing instance, this instance should exit
                false
            } else {
                // No existing instance, start server
                startServer()
                true
            }
        } catch (e: Exception) {
            println("Error in single instance manager: ${e.message}")
            // On error, allow this instance to run
            true
        }
    }

    private fun tryConnectToExistingInstance(deepLinkUrl: String?): Boolean {
        return try {
            Socket(InetAddress.getLoopbackAddress(), port).use { socket ->
                val writer = PrintWriter(socket.getOutputStream(), true)
                val message = deepLinkUrl ?: "PING"
                writer.println(message)
                println("Sent deep link to existing instance: $message")
                true
            }
        } catch (e: Exception) {
            // No existing instance found
            false
        }
    }

    private fun startServer() {
        try {
            serverSocket = ServerSocket(port, 50, InetAddress.getLoopbackAddress())
            isRunning = true

            println("Single instance server started on port $port")

            // Start listening thread
            thread(isDaemon = true, name = "SingleInstanceServer") {
                listenForConnections()
            }
        } catch (e: Exception) {
            println("Failed to start single instance server: ${e.message}")
        }
    }

    private fun listenForConnections() {
        while (isRunning) {
            try {
                serverSocket?.accept()?.use { clientSocket ->
                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    val message = reader.readLine()

                    if (message != null && message != "PING") {
                        println("Received deep link from new instance: $message")
                        // Handle deep link on main thread
                        CoroutineScope(Dispatchers.Main).launch {
                            onDeepLinkReceived(message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    println("Error accepting connection: ${e.message}")
                }
            }
        }
    }

    fun release() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            println("Error closing server socket: ${e.message}")
        }
    }
}
