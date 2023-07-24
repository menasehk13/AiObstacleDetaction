package com.menasehk.videostreaming

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlowLiteObstacleDetector(private val modelInputStream: InputStream) {

    private val tflite: Interpreter
    private val inputSize = 224
    private val modelInputShape: IntArray
    private val modelOutputShape: IntArray

    init {
        val tfliteModel = loadModelFile()
        val options = Interpreter.Options()
        tflite = Interpreter(tfliteModel, options)

        modelInputShape = tflite.getInputTensor(0).shape()
        modelOutputShape = tflite.getOutputTensor(0).shape()
    }


    private fun loadModelFile(): MappedByteBuffer {
        val fileSize = modelInputStream.available()
        val buffer = ByteBuffer.allocateDirect(fileSize)
        buffer.order(ByteOrder.nativeOrder())

        val byteArray = ByteArray(fileSize)
        modelInputStream.read(byteArray)

        buffer.put(byteArray)
        return buffer as MappedByteBuffer
    }

    fun close() {
        tflite.close()
    }

    private fun preprocess(inputBitmap: Bitmap): ByteBuffer {
        val preprocessedBitmap = Bitmap.createScaledBitmap(inputBitmap, inputSize, inputSize, false)
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixelValue = preprocessedBitmap.getPixel(x, y)

                val r = (pixelValue shr 16 and 0xFF) / 255.0f
                val g = (pixelValue shr 8 and 0xFF) / 255.0f
                val b = (pixelValue and 0xFF) / 255.0f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }
        return byteBuffer
    }

    fun detectObstacle(inputBitmap: Bitmap): Boolean {
        val inputBuffer = preprocess(inputBitmap)
        val outputBuffer = ByteBuffer.allocateDirect(modelOutputShape[0] * 4)
        outputBuffer.order(ByteOrder.nativeOrder())

        tflite.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val obstacleConfidence = outputBuffer.float

        return obstacleConfidence > 0.5
    }
}

