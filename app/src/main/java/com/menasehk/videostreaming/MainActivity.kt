package com.menasehk.videostreaming

import android.R.attr
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.WebSocket
import kotlin.math.log


class MainActivity : AppCompatActivity(),CameraWebSocketClient.CameraImageListener {
    private var cameraWebSocketClient: CameraWebSocketClient? = null
    private var imageView: ImageView? = null
    private lateinit var webSocket: WebSocket
    lateinit var forwardBtn:Button
    lateinit var rightBtn:Button
    lateinit var leftBtn:Button
    lateinit var stopBtn:Button
    private lateinit var dataWebSocketClient: DataWebSocketClient
    private val obstacleDetector: TensorFlowLiteObstacleDetector by lazy {
        val modelInputStream = resources.openRawResource(R.raw.model)
        TensorFlowLiteObstacleDetector(modelInputStream)
    }


@SuppressLint("ClickableViewAccessibility")
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        forwardBtn = findViewById(R.id.btnUp)
        stopBtn = findViewById(R.id.btnDown)
        leftBtn = findViewById(R.id.btnLeft)
        rightBtn = findViewById(R.id.btnRight)
        imageView = findViewById(R.id.mjpegView)


    val serverCameraUri = "ws://192.168.1.103:80/Camera"
    cameraWebSocketClient = CameraWebSocketClient(serverCameraUri, this)
    val serverDataUri = "ws://192.168.1.103:80/CarInput"
    dataWebSocketClient = DataWebSocketClient(serverDataUri)

    forwardBtn.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
               dataWebSocketClient.sendData("f")
                Log.d("TAG", "onCreate: pressed")
            }
            MotionEvent.ACTION_UP -> {
                Log.d("TAG", "onCreate: released")

                dataWebSocketClient.sendData("s")
            }
        }
        false
    }

    stopBtn.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dataWebSocketClient.sendData("b")
                Log.d("TAG", "onCreate: pressed")
            }
            MotionEvent.ACTION_UP -> {
                Log.d("TAG", "onCreate: released")

                dataWebSocketClient.sendData("s")
            }
        }
        false
    }

    rightBtn.setOnClickListener {
        dataWebSocketClient.sendData("r")
    }

    leftBtn.setOnClickListener {
        dataWebSocketClient.sendData("l")
    }


    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }


    override fun onDestroy() {
        super.onDestroy()
        cameraWebSocketClient!!.closeWebSocket();

    }


    override fun onCameraImageReceived(bitmap: Bitmap?) {
        runOnUiThread { imageView!!.setImageBitmap(bitmap)
            val isObstacleDetected = obstacleDetector.detectObstacle(bitmap!!)
         handleObstacleDetectionResult(isObstacleDetected)
        }

    }

    private fun handleObstacleDetectionResult(obstacleDetected: Boolean) {
        if (obstacleDetected) {
            //Toast.makeText(this,"obstacle detacted",Toast.LENGTH_SHORT).show()
        }
    }

}