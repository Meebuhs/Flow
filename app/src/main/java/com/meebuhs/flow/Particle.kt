package com.meebuhs.flow

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Particle(x: Float = -1f, y: Float = -1f) {
    private var r: Float = 5f
    private var baseVelocity: Float = 20f

    private var xPosition = x
    private var yPosition = y
    private var xVelocity: Float
    private var yVelocity: Float

    private var inOrbit: Boolean = false
    private var xOrbit: Float = 0f
    private var yOrbit: Float = 0f
    private var rOrbit: Float = 0f

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels

    private val particlePaint = Paint()
    private val trail: ParticleTrail

    init {
        initialisePosition()

        // Start moving in a random direction
        val theta = ThreadLocalRandom.current().nextFloat() * 2 * PI.toFloat()
        xVelocity = baseVelocity * cos(theta)
        yVelocity = baseVelocity * sin(theta)

        val colour = getRandomColour("300")
        particlePaint.color = colour
        trail = ParticleTrail(r, colour)
    }

    private fun initialisePosition() {
        if (xPosition == -1f) {
            xPosition = ThreadLocalRandom.current().nextFloat() * (screenWidth - r)
        }
        if (yPosition == -1f) {
            yPosition = ThreadLocalRandom.current().nextFloat() * (screenHeight - r)
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(xPosition, yPosition, r, particlePaint)
        trail.draw(canvas)
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
        trail.add(xPosition, yPosition)
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
            rOrbit = ThreadLocalRandom.current().nextFloat() * (screenWidth / 2 - 100) + 100
        }
        xOrbit = x
        yOrbit = y
    }

    fun stopOrbit() {
        inOrbit = false
    }
}