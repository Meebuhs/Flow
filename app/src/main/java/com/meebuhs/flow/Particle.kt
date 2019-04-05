package com.meebuhs.flow

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

class Particle {
    private var r: Float = 5f
    private var xPosition: Float
    private var yPosition: Float
    private var xVelocity: Float
    private var yVelocity: Float

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    private val paint: Paint

    init {
        xPosition = ThreadLocalRandom.current().nextFloat() * (screenWidth - r)
        yPosition = ThreadLocalRandom.current().nextFloat() * (screenHeight - r)
        val heading = ThreadLocalRandom.current().nextFloat() * 2 * 3.14
        xVelocity = 20 * cos(heading).toFloat()
        yVelocity = 20 * sin(heading).toFloat()
        paint = Paint()
        paint.color = Color.parseColor("#FFFFFF")
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(xPosition, yPosition, r, paint)
    }

    fun update() {
        if (xPosition > screenWidth - r || xPosition < r) {
            xVelocity *= -1
        }

        if (yPosition > screenHeight - r || yPosition < r) {
            yVelocity *= -1
        }

        xPosition += xVelocity
        yPosition += yVelocity
    }
}