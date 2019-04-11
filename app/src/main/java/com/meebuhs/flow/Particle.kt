package com.meebuhs.flow

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import com.meebuhs.flow.VectorUtils.Companion.add
import com.meebuhs.flow.VectorUtils.Companion.dist
import com.meebuhs.flow.VectorUtils.Companion.fromAngle
import com.meebuhs.flow.VectorUtils.Companion.mag
import com.meebuhs.flow.VectorUtils.Companion.mult
import com.meebuhs.flow.VectorUtils.Companion.sub
import com.meebuhs.flow.VectorUtils.Companion.unitComponentsFromAngle
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor


class Particle(x: Float = -1f, y: Float = -1f) {
    private var radius: Float = 5f
    private var baseVelocity: Float = 20f
    private var maxVelocity: Float = 20f

    private var position = Vector(x, y)
    private var velocity = Vector(0f, 0f)
    private var acceleration = Vector(0f, 0f)

    var orbitId: Int = 0
    private var inOrbit: Boolean = false
    private var orbitPosition = Vector(0f, 0f)
    private var rOrbit: Float = 0f

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels

    private val particlePaint = Paint()
    private val trail: ParticleTrail
    // TODO: move perlin, timestep and increment out of particle
    private val perlin = Perlin()
    private var timeStep = 0.01
    private val increment = 0.03

    init {
        initialisePosition()

        // Start moving in a random direction
        // TODO: This only happens in movement.STANDARD
//        val theta = ThreadLocalRandom.current().nextFloat() * 2 * PI.toFloat()
//        velocity.x = baseVelocity * cos(theta)
//        velocity.y = baseVelocity * sin(theta)

        val colour = getRandomColour("200")
        particlePaint.color = colour
        trail = ParticleTrail(radius, colour)
    }

    private fun initialisePosition() {
        if (position.x == -1f) {
            position.x = ThreadLocalRandom.current().nextFloat() * (screenWidth - radius)
        }
        if (position.y == -1f) {
            position.y = ThreadLocalRandom.current().nextFloat() * (screenHeight - radius)
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, particlePaint)
        trail.draw(canvas)
    }

    fun update() {
        // TODO: Hardcoded movement
        when (Movement.PERLIN) {
            Movement.STANDARD -> {
                if (inOrbit) {
                    val delta = sub(orbitPosition, position)

                    if (abs(dist(orbitPosition, position) - rOrbit) <= baseVelocity / 2) {
                        orbit(delta)
                    } else {
                        travelToOrbit(delta)
                    }
                } else {
                    continueStraight()
                }
                trail.add(position.x, position.y)
            }
            Movement.PERLIN -> {
                // follow, update, edges, show
                // TODO: hardcoded field scale
                val gridPosition = Vector(floor(position.x / 20), floor(position.y / 20))

                val angle = perlin.getOctavePerlin(
                    gridPosition.x.toDouble() * increment,
                    gridPosition.y.toDouble() * increment,
                    timeStep,
                    6,
                    0.2
                ) * PI * 8
                val force = mult(fromAngle(angle.toFloat()), 5f)
                acceleration.add(force)

                position.add(velocity)
                velocity.limit(maxVelocity)
                velocity.add(acceleration)
                acceleration.mult(0f)

                wrapAroundEdges()

                trail.add(position.x, position.y)
                timeStep += 0.005
            }
        }
    }

    /**
     * Travel along orbit path
     */
    private fun orbit(delta: Vector) {
        val currentTheta = atan2(-delta.y, -delta.x)
        val theta = baseVelocity / rOrbit

        position = add(orbitPosition, mult(unitComponentsFromAngle(currentTheta + theta), rOrbit))
    }

    /**
     * Travel along a straight line path to the orbit path
     */
    private fun travelToOrbit(delta: Vector) {
        val theta = atan2(delta.y, delta.x)

        // If particle is within the orbit radius, travel outward
        if (abs(mag(delta)) <= rOrbit) {
            position = sub(position, mult(unitComponentsFromAngle(theta), baseVelocity))
        } else {
            position = add(position, mult(unitComponentsFromAngle(theta), baseVelocity))

        }
    }

    /**
     * Continue in a straight trajectory unless the edge of the screen is hit
     */
    private fun continueStraight() {
        if (position.x > screenWidth - radius || position.x < radius) {
            velocity.x *= -1
        }

        if (position.y > screenHeight - radius || position.y < radius) {
            velocity.y *= -1
        }

        position.x += velocity.x
        position.y += velocity.y
    }


    fun setOrbit(vector: Vector, id: Int) {
        inOrbit = true
        orbitId = id
        rOrbit = ThreadLocalRandom.current().nextFloat() * (screenWidth / 2 - 100) + 100
        position = vector
    }

    fun moveOrbit(vector: Vector) {
        orbitPosition = vector
    }

    fun stopOrbit() {
        inOrbit = false
    }

    private fun wrapAroundEdges() {
        if (position.x > screenWidth) {
            position.x = 0f
        }
        if (position.x < 0f) {
            position.x = screenWidth.toFloat()
        }
        if (position.y > screenHeight) {
            position.y = 0f
        }
        if (position.y < 0f) {
            position.y = screenHeight.toFloat()
        }
    }
}