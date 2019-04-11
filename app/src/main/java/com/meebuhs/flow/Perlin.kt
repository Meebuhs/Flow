package com.meebuhs.flow

import kotlin.math.floor

/**
 * Kotlin translation of Adrian Biagioli's C# implementation OctavePerlin:
 * http://flafla2.github.io/2014/08/09/perlinnoise.html
 *
 * All comments are his with some typos corrected.
 */
class Perlin {
    // Hash lookup table as defined by Ken Perlin. This is a randomly arranged array of all numbers from 0-255 inclusive
    // @formatter:off
    private val permutation = listOf(
        151,160,137, 91, 90, 15,131, 13,201, 95, 96, 53,194,233,  7,225,140, 36,103, 30, 69,142,  8, 99, 37,240, 21, 10,
        23,190,  6,148,247,120,234, 75,  0, 26,197, 62, 94,252,219,203,117, 35, 11, 32, 57,177, 33, 88,237,149, 56, 87,
        174, 20,125,136,171,168, 68,175, 74,165, 71,134,139, 48, 27,166, 77,146,158,231, 83,111,229,122, 60,211,133,230,
        220,105, 92, 41, 55, 46,245, 40,244,102,143, 54, 65, 25, 63,161,  1,216, 80, 73,209, 76,132,187,208, 89, 18,169,
        200,196,135,130,116,188,159, 86,164,100,109,198,173,186,  3, 64, 52,217,226,250,124,123,  5,202, 38,147,118,126,
        255, 82, 85,212,207,206, 59,227, 47, 16, 58, 17,182,189, 28, 42,223,183,170,213,119,248,152,  2, 44,154,163, 70,
        221,153,101,155,167, 43,172,  9,129, 22, 39,253, 19, 98,108,110, 79,113,224,232,178,185,112,104,218,246, 97,228,
        251, 34,242,193,238,210,144, 12,191,179,162,241, 81, 51,145,235,249, 14,239,107, 49,192,214, 31,181,199,106,157,
        184, 84,204,176,115,121, 50, 45,127,  4,150,254,138,236,205, 93,222,114, 67, 29, 24, 72,243,141,128,195, 78, 66,
        215,61,156,180
    )
    // @formatter:on


    private val p: ArrayList<Int> = arrayListOf() // Doubled permutation to avoid overflow

    init {
        for (i in 0 until 512) {
            p.add(permutation[i % 256])
        }
    }

    fun getOctavePerlin(x: Double, y: Double, z: Double, octaves: Int, persistence: Double): Double {
        var total = 0.0
        var frequency = 1.0
        var amplitude = 1.0
        for (i in 0 until octaves) {
            total += getPerlin(x * frequency, y * frequency, z * frequency) * amplitude

            amplitude *= persistence
            frequency *= 2
        }

        return total
    }

    fun getPerlin(x: Double, y: Double, z: Double): Double {
        // TODO: Utilise this tiling to fix issue where particles get stuck on the edge interface
//        if(repeat > 0) {  // If we have any repeat on, change the coordinates to their "local" repetitions
//            x = x%repeat
//            y = y%repeat
//            z = z%repeat
//        }

        val xi = floor(x).toInt() and 255  // Calculate the "unit cube" that the point asked will be located in
        val yi = floor(y).toInt() and 255  // The left bound is ( |_x_|,|_y_|,|_z_| ) and the right bound is that
        val zi = floor(z).toInt() and 255  // plus 1.  Next we calculate the location (from 0.0 to 1.0) in that cube.
        val xf = x - floor(x)                   // We also fade the location to smooth the result.
        val yf = y - floor(y)
        val zf = z - floor(z)
        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        /*
         * This here is Perlin's hash function. We take our x value (remember, between 0 and 255) and get a random
         * value (from our p[] array above) between 0 and 255.  We then add y to it and plug that into p[], and add z to
         * that. Then, we get another random value by adding 1 to that and putting it into p[] and add z to it.
         * We do the whole thing over again starting with x+1. Later we plug aa, ab, ba, and bb back into p[] along with
         * their +1's to get another set. In the end we have 8 values between 0 and 255 - one for each vertex on the
         * unit cube. These are all interpolated together using u, v, and w below.
         */
        val a = p[xi] + yi
        val aa = p[a] + zi
        val ab = p[a + 1] + zi
        val b = p[xi + 1] + yi
        val ba = p[b] + zi
        val bb = p[b + 1] + zi

        /*
         * This is where the "magic" happens.  We calculate a new set of p[] values and use that to get our final
         * gradient values. Then, we interpolate between those gradients with the u value to get4 x-values. Next, we
         * interpolate between the 4 x-values with v to get 2 y-values. Finally,we interpolate between the y-values to
         * get a z-value. When calculating the p[] values, remember that above, p[a+1] expands to p[xi]+yi+1 -- so you
         * are essentially adding 1 to yi.  Likewise, p[ab+1] expands to p[p[xi]+yi+1]+zi+1] -- so you are adding to zi.
         * The other 3 parameters are your possible return values (see grad()), which are actually the vectors from the
         * edges of the unit cube to the point in the unit cube itself.
         */

        var x1 = lerp(grad(p[aa], xf, yf, zf), grad(p[ba], xf - 1, yf, zf), u)
        var x2 = lerp(grad(p[ab], xf, yf - 1, zf), grad(p[bb], xf - 1, yf - 1, zf), u)
        val y1 = lerp(x1, x2, v)

        x1 = lerp(grad(p[aa + 1], xf, yf, zf - 1), grad(p[ba + 1], xf - 1, yf, zf - 1), u)
        x2 = lerp(grad(p[ab + 1], xf, yf - 1, zf - 1), grad(p[bb + 1], xf - 1, yf - 1, zf - 1), u)
        val y2 = lerp(x1, x2, v)

        return (lerp(y1, y2, w) + 1) / 2  // For convenience we bound it to 0 - 1 (theoretical min/max before is -1 - 1)
    }

    fun grad(hash: Int, x: Double, y: Double, z: Double): Double {
        val h = hash and 15     // Take the hashed value and take the first 4 bits of it (15 == 0b1111)
        // If the most significant bit (MSB) of the hash is 0 then set u = x.  Otherwise y.
        var u = if (h < 8) /* 0b1000 */ x else y
        var v: Double // In Ken Perlin's original implementation this was another conditional operator (?:).
        // I expanded it for readability.

        if (h < 4 /* 0b0100 */) {
            v = y  // If the first and second significant bits are 0 set v = y
        } else if (h == 12 /* 0b1100 */ || h == 14 /* 0b1110*/) {
            v = x  // If the first and second significant bits are 1 set v = x
        } else {
            v = z  // If the first and second significant bits are not equal (0/1, 1/0) set v = z
        }

        u = if ((h and 1) == 0) u else -u
        v = if ((h and 2) == 0) v else -v
        return u + v// Use the last 2 bits to decide if u and v are positive or negative.  Then return their addition.
    }

    fun fade(t: Double): Double {
        // Fade function as defined by Ken Perlin.  This eases coordinate values so that they will "ease" towards
        // integral values. This ends up smoothing the final output.
        return t * t * t * (t * (t * 6 - 15) + 10) // 6t^5 - 15t^4 + 10t^3
    }

    fun lerp(a: Double, b: Double, x: Double): Double {
        return a + x * (b - a)
    }
}