package com.meebuhs.flow

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class FieldView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    private val thread: FieldThread
    private val particles: ArrayList<Particle>

    init {
        holder.addCallback(this)
        thread = FieldThread(holder, this)
        particles = ArrayList()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        for (i in 0..200) {
            particles.add(Particle())
        }

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

    /**
     * Update the field state.
     */
    fun update() {
        for (particle in particles) particle.update()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (particle in particles) particle.draw(canvas)
    }
}