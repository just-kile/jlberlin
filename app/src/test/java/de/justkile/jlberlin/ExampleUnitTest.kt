package de.justkile.jlberlin

import de.justkile.jlberlin.model.Coordinate
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val c = Coordinate(0.0, 0.0)
        assertEquals(0, c.latitude)
    }
}