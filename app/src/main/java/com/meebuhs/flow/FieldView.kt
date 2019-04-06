package com.meebuhs.flow

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class FieldView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    private var lastTouchDown: Long
    private var orbitStarted = false

    private val thread: FieldThread
    private val particles: ArrayList<Particle> = arrayListOf()
    private var orbits: MutableMap<Int, Pair<Float, Float>> = mutableMapOf()
    private val orbitIds: ArrayList<Int> = arrayListOf()
    private val clickActionTimeThreshold = 100

    init {
        holder.addCallback(this)
        thread = FieldThread(holder, this)
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
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        for (i in 0 until event.pointerCount) {
            /*
             *  Current user interaction involves tapping the screen to add particles and creating an orbit by holding
             *  down a finger on the screen, multiple held presses will create multiple orbits.
             */
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchDown = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (System.currentTimeMillis() - lastTouchDown > clickActionTimeThreshold) {
                        if (!orbitStarted) {
                            setFirstOrbit(event.getX(i), event.getY(i), event.getPointerId(i))
                        } else {
                            moveOrbit(event.getX(i), event.getY(i), event.getPointerId(i))
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - lastTouchDown < clickActionTimeThreshold) {
                        if (!performClick()) {
                            particles.add(Particle(event.getX(i), event.getY(i)))
                        }
                    }
                    clearOrbits()
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // Remove the orbit with the id of the removed pointer
                    if (event.actionIndex == i) {
                        removeOrbit(event.getPointerId(i))
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Add a new orbit
                    if (event.actionIndex == i) {
                        addOrbit(event.getX(i), event.getY(i), event.getPointerId(i))
                    }
                }
            }
        }
        return true
    }

    private fun setFirstOrbit(x: Float, y: Float, id: Int) {
        for (particle in particles) {
            particle.setOrbit(x, y, id)
        }
        orbitStarted = true
        orbits[id] = Pair(x, y)
        orbitIds.add(id)
    }

    private fun moveOrbit(x: Float, y: Float, id: Int) {
        for (particle in particles) {
            if (particle.orbitId == id) {
                particle.moveOrbit(x, y)
            }
        }
        orbits[id] = Pair(x, y)
    }

    private fun clearOrbits() {
        for (particle in particles) {
            particle.stopOrbit()
        }
        orbitStarted = false
        orbits.clear()
        orbitIds.clear()
    }

    /**
     * Remove the orbit with the given ID and redistribute its particles amongst the other existing orbits
     */
    private fun removeOrbit(id: Int) {
        try {
            var counter = 0
            orbits.remove(id)
            orbitIds.remove(id)
            for (particle in particles) {
                if (particle.orbitId == id) {
                    val orbitId = orbitIds[counter]
                    particle.setOrbit(orbits[orbitId]!!.first, orbits[orbitId]!!.second, orbitId)
                    counter = (++counter) % orbitIds.size
                }
            }
        } catch (e: NullPointerException) {
            // Due to concurrency of touch events, if all fingers are removed in a sufficiently short period of time,
            // orbits are cleared before this method tries to clear a pointer orbit. In these cases we just call
            // clearOrbits to ensure clean up has occurred
            e.printStackTrace()
            clearOrbits()
        }
    }

    /**
     * Add a new orbit and reallocate some existing particles to the new orbit
     */
    private fun addOrbit(x: Float, y: Float, id: Int) {
        var counter = 0
        orbits[id] = Pair(x, y)
        orbitIds.add(id)
        for (particle in particles) {
            if (counter == orbits.size) {
                particle.setOrbit(x, y, id)
                counter = 0
            }
            ++counter
        }
    }
}