package com.menasehk.videostreaming

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.niqdev.mjpeg.Mjpeg
import com.github.niqdev.mjpeg.MjpegInputStream
import com.github.niqdev.mjpeg.MjpegSurfaceView
import com.longdo.mjpegviewer.MjpegView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var mjpegView:MjpegSurfaceView
    val streamUrl:String = "ws://192.168.1.102:81"
    val STREAM_VIDEO:String="http://192.168.1.102"
    private lateinit var webSocket: WebSocket
    lateinit var forwardBtn:Button
    lateinit var rightBtn:Button
    lateinit var leftBtn:Button
    lateinit var stopBtn:Button
    private val obstacleDetector: TensorFlowLiteObstacleDetector by lazy {
        val modelInputStream = resources.openRawResource(R.raw.model)
        TensorFlowLiteObstacleDetector(modelInputStream)
    }

override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        forwardBtn = findViewById(R.id.btnUp)
        stopBtn = findViewById(R.id.btnDown)
        leftBtn = findViewById(R.id.btnLeft)
        rightBtn = findViewById(R.id.btnRight)
        mjpegView = findViewById(R.id.mjpegView)
        forwardBtn.setOnClickListener {
            sendCommand("f")
        }

        leftBtn.setOnClickListener {
            sendCommand("l")

        }

        rightBtn.setOnClickListener {
            sendCommand("r")

        }

        stopBtn.setOnClickListener {
            sendCommand("d")
        }

    connectWebSocket()
    }

    override fun onResume() {
        super.onResume()

        startStreaming()

    }

    override fun onPause() {
        super.onPause()
        disconnectWebSocket()
        stopStreaming()
    }
    private fun startStreaming() {
        try {
            Mjpeg.newInstance()
                .open(STREAM_VIDEO)
                .subscribe(
                    { inputStream: MjpegInputStream ->
                        mjpegView.setSource(inputStream)
                        mjpegView.showFps(true)
//                        GlobalScope.launch(Dispatchers.IO) {
//                            while (true) {
//                                try {
//
//                                    val bitmap = BitmapFactory.decodeStream(inputStream)
//                                    if (bitmap!=null){
//                                        val isObstacleDetected = obstacleDetector.detectObstacle(bitmap)
//                                        handleObstacleDetectionResult(isObstacleDetected)
//                                        delay(100000)
//                                        Log.d("TAG", "startStreaming: ${bitmap}")
//                                    }
//
//                                } catch (e: Exception) {
//                                    Log.e("Streaming", "Error while processing frame", e)
//                                }
//                            }
//                        }
                    },
                    { error: Throwable ->
                        Log.e("Streaming", "Error while streaming", error)
                    }
                )
        } catch (e: IOException) {
            Log.e("Streaming", "Error while streaming", e)
        }
    }

    private fun handleObstacleDetectionResult(obstacleDetected: Boolean) {
        runOnUiThread {
            if (obstacleDetected) {
               Toast.makeText(this,"obstacle detacted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopStreaming() {
        mjpegView.stopPlayback()
    }
    private fun connectWebSocket() {
        val request = Request.Builder().url(streamUrl).build()
        val okHttpClient = OkHttpClient()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {

            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closing")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error while streaming", t)
            }
        })
    }

    private fun disconnectWebSocket() {
        webSocket.close(1000, "User disconnected")
    }

    private fun sendCommand(command: String) {
        if (webSocket.send(command)) {
            Log.d("WebSocket", "Sent command: $command")
        } else {
            Log.e("WebSocket", "Failed to send command: $command")
        }
    }

}