package com.example.utils

import android.util.Log
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ApiGateway {
    private var server: NettyApplicationEngine? = null

    fun startServer(port: Int = 8080, onMessageReceived: (String) -> Unit) {
        if (server != null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = embeddedServer(Netty, host = "127.0.0.1", port = port) {
                    install(CORS) {
                        allowHost("localhost")
                        allowHost("127.0.0.1")
                        allowHeader(io.ktor.http.HttpHeaders.ContentType)
                    }
                    routing {
                        get("/") {
                            call.respondText("Colony API Gateway Running")
                        }
                        post("/webhook") {
                            // Simple text receiver for now
                            val content = "Webhook received" // call.receiveText() requires ContentNegotiation, simplify for now
                            onMessageReceived(content)
                            call.respondText("Acknowledged")
                        }
                    }
                }.start(wait = false)
                Log.d("ApiGateway", "Server started on port $port")
            } catch (e: Exception) {
                Log.e("ApiGateway", "Failed to start server", e)
            }
        }
    }

    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
        Log.d("ApiGateway", "Server stopped")
    }
}
