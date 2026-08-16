package com.example.utils

import android.util.Log
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import java.io.OutputStream
import java.net.InetSocketAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ApiGateway {
    private var server: HttpServer? = null

    fun startServer(port: Int = 8080, onMessageReceived: (String) -> Unit) {
        if (server != null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0).apply {
                    createContext("/") { exchange ->
                        val response = "Colony API Gateway Running"
                        exchange.sendResponseHeaders(200, response.length.toLong())
                        exchange.responseBody.use { os ->
                            os.write(response.toByteArray())
                        }
                    }
                    createContext("/webhook") { exchange ->
                        if ("POST" == exchange.requestMethod) {
                            val content = "Webhook received"
                            onMessageReceived(content)
                            val response = "Acknowledged"
                            exchange.sendResponseHeaders(200, response.length.toLong())
                            exchange.responseBody.use { os ->
                                os.write(response.toByteArray())
                            }
                        } else {
                            val response = "Method Not Allowed"
                            exchange.sendResponseHeaders(405, response.length.toLong())
                            exchange.responseBody.use { os ->
                                os.write(response.toByteArray())
                            }
                        }
                    }
                    executor = null
                    start()
                }
                Log.d("ApiGateway", "Lightweight native HttpServer started on port $port")
            } catch (e: Exception) {
                Log.e("ApiGateway", "Failed to start native HttpServer", e)
            }
        }
    }

    fun stopServer() {
        try {
            server?.stop(0)
            server = null
            Log.d("ApiGateway", "Lightweight native HttpServer stopped")
        } catch (e: Exception) {
            Log.e("ApiGateway", "Failed to stop native HttpServer", e)
        }
    }
}
