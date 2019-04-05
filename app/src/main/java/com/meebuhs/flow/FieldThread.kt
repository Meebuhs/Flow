package com.meebuhs.flow

import android.graphics.Canvas
import android.view.SurfaceHolder

class FieldThread(private val surfaceHolder: SurfaceHolder, private val fieldView: FieldView) : Thread() {
    private var running: Boolean = false

    private val targetFPS = 60

    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }

    override fun run() {
        var startTime: Long
        var elapsedTime: Long
        var waitTime: Long
        val targetTime = (1000 / targetFPS).toLong()

        while (running) {
            startTime = System.nanoTime()
            canvas = null

            try {
                canvas = this.surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    this.fieldView.update()
                    this.fieldView.draw(canvas!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            elapsedTime = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - elapsedTime

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private var canvas: Canvas? = null
    }
}