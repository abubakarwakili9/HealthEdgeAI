package com.example.healthedgeai.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.healthedgeai.util.BluetoothConnectionManager

class ConnectionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    var connectionState = BluetoothConnectionManager.ConnectionState.DISCONNECTED
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 3f

        // Set color based on connection state
        when (connectionState) {
            BluetoothConnectionManager.ConnectionState.CONNECTED -> {
                paint.color = Color.GREEN
            }
            BluetoothConnectionManager.ConnectionState.CONNECTING -> {
                paint.color = Color.YELLOW
            }
            BluetoothConnectionManager.ConnectionState.DISCONNECTING -> {
                paint.color = Color.YELLOW
            }
            BluetoothConnectionManager.ConnectionState.DISCONNECTED -> {
                paint.color = Color.RED
            }
        }

        // Draw circle
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 40
        val desiredHeight = 40

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}