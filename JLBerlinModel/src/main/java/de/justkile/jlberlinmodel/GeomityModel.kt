package de.justkile.jlberlinmodel

data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    fun isInPolygon(polygon: List<Coordinate>): Boolean {
        var result = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            if (polygon[i].longitude == this.longitude && polygon[i].latitude == this.latitude) {
                return true // Point is on a vertex
            }
            if ((polygon[i].longitude < this.longitude && polygon[j].longitude >= this.longitude)
                || (polygon[j].longitude < this.longitude && polygon[i].longitude >= this.longitude)
            ) {
                val intersectLat =
                    polygon[i].latitude + (this.longitude - polygon[i].longitude) / (polygon[j].longitude - polygon[i].longitude) * (polygon[j].latitude - polygon[i].latitude)
                if (intersectLat == this.latitude) {
                    return true // Point is on an edge
                }
                if (intersectLat < this.latitude) {
                    result = !result
                }
            }
            j = i
        }
        return result
    }
}

data class Bounds(
    val northEast: Coordinate,
    val southWest: Coordinate
)

data class District(
    val name: String,
    val parentName: String,

    // List of polygons, represented by a list of coordinates
    val coordinates: List<List<Coordinate>>
) {

    fun isCoordinateInDistrict(coordinate: Coordinate): Boolean {
        return isCoordinateInBounds(coordinate) && coordinates.any { coordinate.isInPolygon(it) }
    }

    fun calcCoordinatesForLevelOfDetail(epsilon: Double): List<List<Coordinate>> {
        return coordinates.map { reducePoints(it, epsilon) }
    }

    val bounds by lazy {
        val northEast = Coordinate(
            coordinates.flatten().maxByOrNull { it.latitude }!!.latitude,
            coordinates.flatten().maxByOrNull { it.longitude }!!.longitude
        )
        val southWest = Coordinate(
            coordinates.flatten().minByOrNull { it.latitude }!!.latitude,
            coordinates.flatten().minByOrNull { it.longitude }!!.longitude
        )
        Bounds(northEast, southWest)
    }

    private fun isCoordinateInBounds(coordinate: Coordinate): Boolean {
        return coordinate.latitude <= bounds.northEast.latitude &&
                coordinate.latitude >= bounds.southWest.latitude &&
                coordinate.longitude <= bounds.northEast.longitude &&
                coordinate.longitude >= bounds.southWest.longitude
    }

    private fun reducePoints(coordinates: List<Coordinate>, epsilon: Double): List<Coordinate> {
        if (coordinates.size < 3) return coordinates

        val firstPoint = coordinates.first()
        val lastPoint = coordinates.last()

        var maxDistance = 0.0
        var index = 0

        for (i in 1 until coordinates.size - 1) {
            val distance = perpendicularDistance(coordinates[i], firstPoint, lastPoint)
            if (distance > maxDistance) {
                index = i
                maxDistance = distance
            }
        }

        return if (maxDistance > epsilon) {
            val left = reducePoints(coordinates.subList(0, index + 1), epsilon)
            val right = reducePoints(coordinates.subList(index, coordinates.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(firstPoint, lastPoint)
        }
    }

    private fun perpendicularDistance(point: Coordinate, lineStart: Coordinate, lineEnd: Coordinate): Double {
        val dx = lineEnd.latitude - lineStart.latitude
        val dy = lineEnd.longitude - lineStart.longitude

        val mag = Math.hypot(dx, dy)
        if (mag == 0.0) return Math.hypot(point.latitude - lineStart.latitude, point.longitude - lineStart.longitude)

        val u = ((point.latitude - lineStart.latitude) * dx + (point.longitude - lineStart.longitude) * dy) / (mag * mag)

        val closestPoint = Coordinate(
            lineStart.latitude + u * dx,
            lineStart.longitude + u * dy
        )

        return Math.hypot(point.latitude - closestPoint.latitude, point.longitude - closestPoint.longitude)
    }

}


data class Districts(
    val districts: List<District>

) {
    fun findDistrictByCoordinate(coordinate: Coordinate): District? {
        return districts.find { it.isCoordinateInDistrict(coordinate) }
    }

    val bounds by lazy {
        val northEast = Coordinate(
            districts.maxOfOrNull { it.bounds.northEast.latitude }!!,
            districts.maxOfOrNull { it.bounds.northEast.longitude }!!,
        )
        val southWest = Coordinate(
            districts.minOfOrNull { it.bounds.northEast.latitude }!!,
            districts.minOfOrNull { it.bounds.northEast.longitude }!!,
        )
        Bounds(northEast, southWest)
    }

    val center by lazy {
        Coordinate(
            (bounds.northEast.latitude + bounds.southWest.latitude) / 2,
            (bounds.northEast.longitude + bounds.southWest.longitude) / 2
        )
    }
}