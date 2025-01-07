package de.justkile.jlberlin.datasource

import android.content.Context
import de.justkile.jlberlin.R
import de.justkile.jlberlinmodel.GeoJsonParser

class DistrictLocalDataSource(context: Context) {

    val districts = GeoJsonParser().parseGeoJson(context.resources.openRawResource(R.raw.lor_ortsteile))

}