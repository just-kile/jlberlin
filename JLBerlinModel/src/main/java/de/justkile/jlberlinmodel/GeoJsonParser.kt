package de.justkile.jlberlinmodel

import de.justkile.jlberlin.model.Coordinate
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.model.Districts
import java.io.FileInputStream
import java.io.InputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GeoJsonParser {

    fun parseGeoJson(input: InputStream): Districts {

        val json = input.bufferedReader().use { it.readText() }
        val result = Json.parseToJsonElement(json)
        val root = result.jsonObject
        val features = root.get("features")

        val model = features!!.jsonArray.map{ feature ->
            val properties = feature.jsonObject.get("properties")

            val name = properties!!.jsonObject.get("OTEIL")
            val district = properties.jsonObject.get("BEZIRK")

            val geometry = feature.jsonObject.get("geometry")
            val type = geometry!!.jsonObject.get("type")
            val typeValue = type!!.jsonPrimitive.content

            var coordinateModel : List<List<Coordinate>>
            if (typeValue == "Polygon") {
                val area = geometry!!.jsonObject.get("coordinates")!!.jsonArray[0].jsonArray
                val coordinates = parseCoordinatesFromArea(area)
                coordinateModel = listOf(coordinates)
            } else if (typeValue == "MultiPolygon") {
                val areas = geometry!!.jsonObject.get("coordinates")!!.jsonArray
                coordinateModel = areas.map { area ->
                    parseCoordinatesFromArea(area.jsonArray[0].jsonArray)
                }
            } else {
                throw Exception("Unknown type: $typeValue")
            }

            District(
                name = name!!.jsonPrimitive.content,
                parentName = district!!.jsonPrimitive.content,
                coordinates = coordinateModel
            )
        }

        return Districts(districts = model)
    }

    private fun parseCoordinatesFromArea(area: JsonArray): List<Coordinate> {
        val coordinates = area.jsonArray
        return coordinates.map { coord ->
            val longitude = coord.jsonArray[0].jsonPrimitive.double
            val latitude = coord.jsonArray[1].jsonPrimitive.double
            Coordinate(
                latitude = latitude,
                longitude = longitude
            )
        }
    }

}

fun main() {
    val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\app\\src\\main\\res\\raw\\lor_ortsteile.geojson")
    val model = GeoJsonParser().parseGeoJson(inputStream)
    //println(GeoJsonParser().parseGeoJson(inputStream))
}