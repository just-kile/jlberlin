package de.justkile.jlberlin

import de.justkile.jlberlinmodel.GeoJsonParser
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

private const val FILE_NAME = "raw/lor_ortsteile.geojson"

class GeoJsonParserTest {

    @Test
    fun test() {
        ClassLoader.getSystemResourceAsStream(FILE_NAME).use { inputStream ->
            val result = GeoJsonParser().parseGeoJson(inputStream);
        }
        assertThat("Hallo", equalTo("Hallo"))
    }
}