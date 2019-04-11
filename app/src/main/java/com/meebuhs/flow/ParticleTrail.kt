package com.meebuhs.flow

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Log
import androidx.core.graphics.ColorUtils
import com.meebuhs.flow.VectorUtils.Companion.dist


/**
 * Class representing the trail behind the particles. The trail is stored as a queue of points which is altered as the
 * particle travels through the space. New points are appended and thus index 0 is the point furthest from the particle.
 */
class ParticleTrail(radius: Float, color: Int) {
    private val trail = mutableListOf<Vector>()
    private val trailPath = Path()
    private val trailPaint = Paint()
    private var size = 0
    private var sizeLimit = 50
    private var colour = color

    init {
        trailPaint.color = colour
        trailPaint.strokeWidth = radius * 2
        trailPaint.strokeJoin = Paint.Join.ROUND
        trailPaint.strokeCap = Paint.Cap.ROUND
        trailPaint.style = Paint.Style.STROKE
    }

    fun draw(canvas: Canvas) {
        // points of the path with the newest point (255 alpha) at index 0
        if (size > 0) {
            val points = trail.asReversed()
            var counter = 0

            while (counter < size - 1) {
                trailPath.reset()
                var trailLength = 0
                var isFirst = true
                var endCurrentPath = false
                var lastPoint = Vector(0f, 0f)
                while (!endCurrentPath) {
                    val point = points[counter]

                    if (isFirst) {
                        trailPath.moveTo(point.x, point.y)
                        isFirst = false
                        ++trailLength
                        ++counter
                    } else {
                        if (dist(
                                point,
                                lastPoint
                            ) >= Resources.getSystem().displayMetrics.widthPixels / 2 || counter >= size - 1
                        ) {
                            endCurrentPath = true
                        } else {
                            trailPath.lineTo(point.x, point.y)
                            ++trailLength
                            ++counter
                        }
                    }
                    lastPoint = point
                }

                val startColour =
                    ColorUtils.blendARGB(colour, Color.TRANSPARENT, ((counter - trailLength) / size).toFloat())
                trailPaint.shader = LinearGradient(
                    points[counter - trailLength].x,
                    points[counter - trailLength].y,
                    points[counter - 1].x,
                    points[counter - 1].y,
                    startColour,
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(trailPath, trailPaint)
            }
        }
    }

    fun getStartColour(counter: Int, length: Int): Int {
        val colourCopy = colour
        val increment = 255 / size
        Log.d("colour", "Start ${255 - (counter - length) * increment}")
        ColorUtils.setAlphaComponent(colourCopy, 255 - (counter - length) * increment)
        return colourCopy
    }

    fun getEndColour(counter: Int): Int {
        val colourCopy = colour
        val increment = 255 / size
        Log.d("colour", "End ${255 - counter * increment}")
        ColorUtils.setAlphaComponent(colourCopy, 255 - counter * increment)
        return colourCopy
    }

    fun add(x: Float, y: Float) {
        trail.add(Vector(x, y))
        ++size
        if (size == sizeLimit) {
            trail.removeAt(0)
            --size
        }
    }
}