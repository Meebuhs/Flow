package com.meebuhs.flow

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vector(var x: Float, var y: Float) {
    fun add(other: Vector) {
        x += other.x
        y += other.y
    }

    fun mult(scalar: Float) {
        x *= scalar
        y *= scalar
    }

    fun div(scalar: Float) {
        x /= scalar
        y /= scalar
    }


    fun limit(scalar: Float) {
        if (mag() * mag() > scalar * scalar) {
            normalise()
            mult(scalar)
        }

    }

    fun mag(): Float {
        return x * x + y * y
    }

    fun normalise() {
        val mag = mag()
        if (mag != 0f && mag != 1f) {
            div(mag)
        }
    }

}

class VectorUtils {
    companion object {
        fun add(first: Vector, second: Vector): Vector {
            return Vector(first.x + second.x, first.y + second.y)
        }

        fun sub(first: Vector, second: Vector): Vector {
            return Vector(first.x - second.x, first.y - second.y)
        }

        fun mult(vector: Vector, scalar: Float): Vector {
            return Vector(vector.x * scalar, vector.y * scalar)
        }

        fun mag(vector: Vector): Float {
            return sqrt(vector.x * vector.x + vector.y * vector.y)
        }

        fun dist(first: Vector, second: Vector): Float {
            val delta = sub(first, second)
            return mag(delta)
        }

        fun unitComponentsFromAngle(angle: Float): Vector {
            return Vector(cos(angle), sin(angle))
        }

        fun fromAngle(angle: Float): Vector {
            return Vector(cos(angle), sin(angle))
        }
    }
}