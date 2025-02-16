package de.justkile.jlberlin

import de.justkile.jlberlinmodel.Coordinate
import de.justkile.jlberlinmodel.District
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GeomitryModelTest {

    @Test
    fun testPointInsidePolygon() {
        val polygon = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val point = Coordinate(5.0, 5.0)
        assertTrue(point.isInPolygon(polygon))
    }

    @Test
    fun testPointOutsidePolygon() {
        val polygon = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val point = Coordinate(15.0, 5.0)
        assertFalse(point.isInPolygon(polygon))
    }

    @Test
    fun testPointOnEdgeOfPolygon() {
        val polygon = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val point = Coordinate(0.0, 5.0)
        assertTrue(point.isInPolygon(polygon))
    }

    @Test
    fun testPointOnVertexOfPolygon() {
        val polygon = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val point = Coordinate(0.0, 0.0)
        assertTrue(point.isInPolygon(polygon))
    }

    @Test
    fun testComplexPolygon() {
        val polygon = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(5.0, 5.0),
            Coordinate(5.0, 0.0),
            Coordinate(10.0, 10.0),
            Coordinate(0.0, 10.0)
        )
        val pointInside = Coordinate(2.0, 3.0)
        val pointOutside = Coordinate(3.0, 2.0)
        assertTrue(pointInside.isInPolygon(polygon))
        assertFalse(pointOutside.isInPolygon(polygon))
    }

    @Test
    fun testCoordinateInsideDistrict() {
        val polygon1 = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val polygon2 = listOf(
            Coordinate(20.0, 20.0),
            Coordinate(20.0, 30.0),
            Coordinate(30.0, 30.0),
            Coordinate(30.0, 20.0)
        )
        val district = District("TestDistrict", "ParentDistrict", listOf(polygon1, polygon2))
        val coordinate = Coordinate(5.0, 5.0)
        assertTrue(district.isCoordinateInDistrict(coordinate))
    }

    @Test
    fun testCoordinateOutsideDistrict() {
        val polygon1 = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 10.0),
            Coordinate(10.0, 10.0),
            Coordinate(10.0, 0.0)
        )
        val polygon2 = listOf(
            Coordinate(20.0, 20.0),
            Coordinate(20.0, 30.0),
            Coordinate(30.0, 30.0),
            Coordinate(30.0, 20.0)
        )
        val district = District("TestDistrict", "ParentDistrict", listOf(polygon1, polygon2))
        val coordinate = Coordinate(15.0, 15.0)
        assertFalse(district.isCoordinateInDistrict(coordinate))
    }

}