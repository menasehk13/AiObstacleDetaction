package com.menasehk.videostreaming

import okhttp3.*
import okio.ByteString

class DataWebSocketClient(private val serverUri: String) {

    private var webSocket: WebSocket? = null

    init {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url(serverUri).build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@DataWebSocketClient.webSocket = webSocket
                // WebSocket connection is established
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Handle incoming messages as needed
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Handle incoming binary messages as needed
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // WebSocket connection is closed
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Handle WebSocket errors
            }
        })
    }

    fun sendData(data: String) {
        webSocket?.send(data)
    }

    fun closeWebSocket() {
        webSocket?.close(1000, null)
    }


}
