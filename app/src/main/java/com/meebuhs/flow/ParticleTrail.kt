package com.meebuhs.flow

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader


/**
 * Class representing the trail behind the particles. The trail is stored as a queue of points which is altered as the
 * particle travels through the space. New points are appended and thus index 0 is the point furthest from the particle.
 */
class ParticleTrail(r: Float, colour: Int) {
    private val trail = mutableListOf<Pair<Float, Float>>()
    private val trailPath = Path()
    private val trailPaint = Paint()
    private var size = 0
    private var sizeLimit = 30

    init {
        trailPaint.color = colour
        trailPaint.strokeWidth = r * 2
        trailPaint.style = Paint.Style.STROKE
    }

    fun draw(canvas: Canvas) {
        if (size > 0) {
            trailPaint.shader = LinearGradient(
                trail[0].first,
                trail[0].second,
                trail[size - 1].first,
                trail[size - 1].second,
                Color.TRANSPARENT,
                trailPaint.color,
                Shader.TileMode.REPEAT
            )
            trailPath.reset()
            var isFirst = true
            for (point in trail) {
                if (isFirst) {
                    trailPath.moveTo(point.first, point.second)
                    isFirst = false
                } else {
                    trailPath.lineTo(point.first, point.second)
                }
            }
            canvas.drawPath(trailPath, trailPaint)
        }
    }

    fun add(x: Float, y: Float) {
        trail.add(Pair(x, y))
        ++size
        if (size == sizeLimit) {
            trail.removeAt(0)
            --size
        }
    }
}