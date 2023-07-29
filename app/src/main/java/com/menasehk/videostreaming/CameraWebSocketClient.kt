package com.menasehk.videostreaming

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.ByteArrayInputStream
import java.io.InputStream


class CameraWebSocketClient(serverUri: String?, private val listener: CameraImageListener?) :
    WebSocketListener() {
    private var webSocket: WebSocket? = null

    init {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url(serverUri!!).build()
        client.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        // WebSocket connection is established, you can send data here if needed
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Handle incoming messages as needed
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        val imageBytes = bytes.toByteArray()
        val inputStream: InputStream = ByteArrayInputStream(imageBytes)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Notify the listener about the received image
        listener?.onCameraImageReceived(bitmap)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // WebSocket connection is closed
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Handle WebSocket errors
    }

    fun closeWebSocket() {
        if (webSocket != null) {
            webSocket!!.close(1000, null)
        }
    }

    interface CameraImageListener {
        fun onCameraImageReceived(bitmap: Bitmap?)
    }
}
