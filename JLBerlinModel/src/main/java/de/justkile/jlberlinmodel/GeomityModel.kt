package de.justkile.jlberlin.model

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

    // List of polygones, represented by a list of coordinates
    val coordinates: List<List<Coordinate>>
    ) {

    fun isCoordinateInDistrict(coordinate: Coordinate): Boolean {
        return isCoordinateInBounds(coordinate) && coordinates.any { coordinate.isInPolygon(it) }
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

}


data class Districts(
    val districts: List<District>
) {
    fun findDistrictByCoordinate(coordinate: Coordinate): District? {
        return districts.find { it.isCoordinateInDistrict(coordinate) }
    }
}