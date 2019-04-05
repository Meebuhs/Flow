package com.meebuhs.flow

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*

class Particle {
    private var r: Float = 5f
    private var baseVelocity: Float = 20f

    private var xPosition: Float
    private var yPosition: Float
    private var xVelocity: Float
    private var yVelocity: Float

    private var inOrbit: Boolean = false
    private var xOrbit: Float = 0f
    private var yOrbit: Float = 0f
    private var rOrbit: Float = 0f

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    private val paint: Paint

    init {
        xPosition = ThreadLocalRandom.current().nextFloat() * (screenWidth - r)
        yPosition = ThreadLocalRandom.current().nextFloat() * (screenHeight - r)
        val theta = ThreadLocalRandom.current().nextFloat() * 2 * PI.toFloat()
        xVelocity = baseVelocity * cos(theta)
        yVelocity = baseVelocity * sin(theta)
        paint = Paint()
        paint.color = Color.parseColor("#FFFFFF")
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(xPosition, yPosition, r, paint)
    }

    fun update() {
        if (inOrbit) {
            val xDelta = xOrbit - xPosition
            val yDelta = yOrbit - yPosition

            if (abs(sqrt(xDelta * xDelta + yDelta * yDelta) - rOrbit) <= baseVelocity / 2) {
                orbit(xDelta, yDelta)
            } else {
                travelToOrbit(xDelta, yDelta)
            }
        } else {
            continueStraight()
        }
    }

    /**
     * Travel along orbit path
     */
    private fun orbit(xDelta: Float, yDelta: Float) {
        val currentTheta = atan2(-yDelta, -xDelta)
        val theta = baseVelocity / rOrbit

        xPosition = xOrbit + rOrbit * cos(currentTheta + theta)
        yPosition = yOrbit + rOrbit * sin(currentTheta + theta)
    }

    /**
     * Travel along a straight line path to the orbit path
     */
    private fun travelToOrbit(xDelta: Float, yDelta: Float) {
        val theta = atan2(yDelta, xDelta)

        // If particle is within the orbit radius, travel outward
        if (abs(sqrt(xDelta * xDelta + yDelta * yDelta)) <= rOrbit) {
            xPosition -= baseVelocity * cos(theta)
            yPosition -= baseVelocity * sin(theta)
        } else {
            xPosition += baseVelocity * cos(theta)
            yPosition += baseVelocity * sin(theta)
        }
    }

    /**
     * Continue in a straight trajectory unless the edge of the screen is hit
     */
    private fun continueStraight() {
        if (xPosition > screenWidth - r || xPosition < r) {
            xVelocity *= -1
        }

        if (yPosition > screenHeight - r || yPosition < r) {
            yVelocity *= -1
        }

        xPosition += xVelocity
        yPosition += yVelocity
    }


    fun startOrbit(x: Float, y: Float) {
        if (!inOrbit) {
            inOrbit = true
            rOrbit = ThreadLocalRandom.current().nextFloat() * (screenWidth / 2)
        }
        xOrbit = x
        yOrbit = y
    }

    fun stopOrbit() {
        inOrbit = false
    }
}