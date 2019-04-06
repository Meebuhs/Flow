package com.meebuhs.flow

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class FieldView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    private var lastTouchDown: Long

    private val thread: FieldThread
    private val particles: ArrayList<Particle>
    private val clickActionTimeThreshold = 100

    init {
        holder.addCallback(this)
        thread = FieldThread(holder, this)
        particles = ArrayList()
        lastTouchDown = System.currentTimeMillis()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        var retry = true
        while (retry) {
            try {
                thread.setRunning(false)
                thread.join()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            retry = false
        }
    }

    fun update() {
        for (particle in particles) particle.update()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (particle in particles) particle.draw(canvas)
    }

    override fun performClick(): Boolean {
        particles.add(Particle())

        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO: Add multi-touch support where particles are split between multiple orbits
        val x = event.x
        val y = event.y

        /*
         *  Current user interaction involves tapping the screen to add particles and creating an orbit by holding down
         *  a finger on the screen.
         */
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchDown = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                if (System.currentTimeMillis() - lastTouchDown > clickActionTimeThreshold) {
                    setOrbits(x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - lastTouchDown < clickActionTimeThreshold) {
                    performClick()
                }
                removeOrbits()
            }
        }
        return true
    }

    private fun setOrbits(x: Float, y: Float) {
        for (particle in particles) {
            particle.startOrbit(x, y)
        }
    }

    private fun removeOrbits() {
        for (particle in particles) {
            particle.stopOrbit()
        }
    }
}