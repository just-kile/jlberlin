package de.justkile.jlberlinsim

import de.justkile.jlberlinmodel.GeoJsonParser
import java.io.FileInputStream
import de.justkile.jlberlin.model.Coordinate
import de.justkile.jlberlin.model.District
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit


class JLBerlinSim {

    private var districts: List<District>? = null
    private var stops = emptyList<Stop>()
    private var stopId2stop = emptyMap<String, Stop>()
    private var stopTimes: List<StopTime> = emptyList()
    private var stationId2stopTimes: Map<String, List<StopTime>> = emptyMap()
    private var tripId2stopTimes: Map<Long, List<StopTime>> = emptyMap()
    private var trips: List<Trip> = emptyList()
    private var tripId2trip: Map<Long, Trip> = emptyMap()
    private var calendar: List<Calendar> = emptyList()
    private var serviceId2calendar: Map<Int, Calendar> = emptyMap()
    private var calendarDates: List<CalendarDate> = emptyList()
    private var serviceId2calendarDates: Map<Int, List<CalendarDate>> = emptyMap()
    private var routes: List<Route> = emptyList()
    private var routeId2routes: Map<String, Route> = emptyMap()
    private var stationId2departurePercentage: Map<String, Double> = emptyMap()

    private val maxMinutesUntilNextDeparture = 30L
    private val timeToClaimDistrictInSeconds = 60L
    private val timeToChangeInSeconds = 30L

    private val scorePenaltyPerMinute = 1
    private val scoreForNewDistrict = 30
    private val scoreForNextDeparturesPercentage = 10

    private val simulationTimeInHours = 12L
    private val simulationRecursionDepth = 2

    private var claimedDistricts: List<District> = listOf()

    inline fun <T> logTime(desc: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000
        println("$desc took $duration ms")
        return result
    }

    data class PotentialNextStop (
        var stop: Stop,
        var stopTime: StopTime,
        var departureStopTime: StopTime,
        var route: Route,
        var score: Long
    )

    fun test() {
        println("Starting simulation")

        prepareDate()

        // prepare
        var currentTime = LocalDateTime.of(LocalDate.of(2024, 12, 27), LocalTime.of(6, 0))
        val endTime = currentTime.plusHours(simulationTimeInHours)
//        var currentStop = stopId2stop["126471"] // S Köpenick (Berlin)
//        var currentStop = stopId2stop["111445"] // S+U Berlin Hauptbahnhof
        var currentStop = stopId2stop["133135"] // S+U Alexanderplatz

        stopTimes = logTime("filterCalendar") {
            stopTimes.filter { stopTime ->
                val trip = tripId2trip[stopTime.tripId]
                isTripRunningAtDate(trip, currentTime.toLocalDate())
            }
        }

        // run simulation
        currentTime = logTime("runSimulation") { runSimulation(currentTime, endTime, currentStop) }

        println("Simulation finished @ ${currentTime.toLocalTime()}" )
        println("Claimed a total of ${claimedDistricts.size} districts.")
        println("Districts: ${claimedDistricts.map { it.name }.joinToString(", ")}")
    }

    private fun runSimulation(
        currentTime: LocalDateTime,
        endTime: LocalDateTime?,
        currentStop: Stop?
    ): LocalDateTime {
        var currentTime1 = currentTime
        var currentStop1 = currentStop
        while (currentTime1 < endTime) {

            if (!claimedDistricts.contains(currentStop1!!.district)) {
                claimedDistricts += currentStop1!!.district!!
                println("SIM ${currentTime1.toLocalTime()}: Claimed district ${currentStop1.district!!.name} (${claimedDistricts.size})")
                currentTime1 = currentTime1.plusSeconds(timeToClaimDistrictInSeconds)
            }

            var bestNextStop: PotentialNextStop = findBestNextStop(
                currentStop1,
                currentTime1,
                claimedDistricts,
                simulationRecursionDepth
            )!!
            println("SIM ${currentTime1.toLocalTime()}: Next stop is ${bestNextStop!!.stop.stopName} @ ${bestNextStop!!.stopTime?.arrivalTime} (from ${currentStop1?.stopName} @ ${bestNextStop!!.departureStopTime?.departureTime}) with ${bestNextStop!!.route?.routeShortName} ")

            currentTime1 = currentTime1.with(bestNextStop!!.stopTime!!.arrivalTime)
                .plusSeconds(timeToChangeInSeconds)
            currentStop1 = bestNextStop!!.stop
        }
        return currentTime1
    }

    private fun JLBerlinSim.findBestNextStop(
        currentStop: Stop?,
        currentTime: LocalDateTime,
        claimedDistricts: List<District>,
        recursionDepth: Int
    ): PotentialNextStop? {
        var bestNextStop: PotentialNextStop? = null
        var currentBestNextStopScore = Long.MIN_VALUE

        stationId2stopTimes[currentStop!!.getStationId()]!!
            .filter {
                val lastDepartureTime =
                    currentTime.plusMinutes(maxMinutesUntilNextDeparture).toLocalTime()
                currentTime.toLocalTime() <= it.departureTime && it.departureTime <= lastDepartureTime
            }
            .sortedBy { it.departureTime }!!
            .parallelStream()
            .forEach { currentStopTime ->
                val trip = tripId2trip[currentStopTime.tripId]
                val route = routeId2routes[trip!!.routeId]
                val nextStopTimes = findStopsTimesForTrip(trip, currentStopTime.departureTime)

                nextStopTimes.forEach {
                    val stop = stopId2stop[it.stopId]

                    // penalty for travel time
                    var score = -currentTime.toLocalTime().until(
                        it.arrivalTime,
                        ChronoUnit.MINUTES
                    ) * scorePenaltyPerMinute

                    // bonus for new district
                    if (!claimedDistricts.contains(stop!!.district)) {
                        score += scoreForNewDistrict
                    }

                    // bonus for next departures from stop
                    score += (stationId2departurePercentage.get(stop.getStationId())!! * scoreForNextDeparturesPercentage).toInt()


                    // bonus for next stop (recursive)
                    if (recursionDepth > 0) {
                        var timeForNextStop = currentTime.with(it.arrivalTime).plusSeconds(timeToChangeInSeconds)
                        val claimedDistrictsInNextStop = claimedDistricts.toMutableList()
                        if (!claimedDistrictsInNextStop.contains(stop.district)) {
                            claimedDistrictsInNextStop.add(stop.district!!)
                            timeForNextStop = timeForNextStop.plusSeconds(timeToClaimDistrictInSeconds)
                        }
                        val nextStop = findBestNextStop(stop, currentTime.with(timeForNextStop), claimedDistrictsInNextStop, recursionDepth - 1)
                        if (nextStop != null) {
                            score += nextStop.score
                        } else {
                            score = Long.MIN_VALUE
                        }
                    }

                    synchronized(this) {
                        if (score > currentBestNextStopScore) {
                            bestNextStop = PotentialNextStop(
                                stop = stop,
                                stopTime = it,
                                departureStopTime = currentStopTime,
                                route = route!!,
                                score = score
                            )
                            currentBestNextStopScore = score
                        }
                    }

                    // println("  - ${stop?.stopName} @ ${it.arrivalTime}, score = ${score}" )
                }
            }
        return bestNextStop
    }

    fun findStopsTimesForTrip(trip: Trip, departureTime: LocalTime): List<StopTime> {
        return tripId2stopTimes[trip.tripId]!!.filter {
            try {
                val stopDepartureTime = it.departureTime
                it.tripId == trip.tripId && departureTime < stopDepartureTime
            } catch (e: DateTimeParseException) {
                false // for invalid times (e. g. due to time zone issues, e. g. 24:01:00) we just ignore the stop
            }
        }
    }


    private fun isTripRunningAtDate(
        trip: Trip?,
        date: LocalDate
    ): Boolean {
        val calendarDates = serviceId2calendarDates[trip!!.serviceId]
        if (calendarDates != null) {
            val exception = calendarDates.find {
                val exceptionDate =
                    LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                exceptionDate == date
            }
            if (exception != null) {
                if (exception.exceptionType == 1) {
                    // service has been added for the specified date
                    return true
                } else {
                    // service not available for the specified date
                    return false
                }
            }
        }


        val calendar = serviceId2calendar[trip!!.serviceId]
        if (calendar != null) {
            return calendar!!.isRunningOnDate(date)
        } else {
            return false // date neiter in calendar_dates for the given date, nor in calendar with regular entry
        }
    }

    private fun prepareDate() {
        val inputStream =
            FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\app\\src\\main\\res\\raw\\lor_ortsteile.geojson")
        districts = GeoJsonParser().parseGeoJson(inputStream)

        stops = logTime("stops") { GtfsParser().parseStopsCsv() }
        stops = setDistrictForStops(stops, districts!!).filter { it.district != null }
        stopId2stop = logTime("stopId2stop") { stops.associateBy { it.stopId } }

        stopTimes = logTime("stopTimes") { GtfsParser().parseStopTimesCsv(stopId2stop) }
        stopTimes = stopTimes.filter { stopId2stop.containsKey(it.stopId) }
        tripId2stopTimes = stopTimes.groupBy { it.tripId }
        stationId2stopTimes = stopTimes.groupBy { it!!.stationId!! }

        trips = logTime("trips") { GtfsParser().parseTripsCsv() }
        tripId2trip = trips.associateBy { it.tripId }

        calendar = logTime("calendar") { GtfsParser().parseCalendarCsv() }
        calendarDates = logTime("calendarDates") { GtfsParser().parseCalendarDatesCsv() }
        serviceId2calendar = calendar.associateBy { it.serviceId }
        serviceId2calendarDates = calendarDates.groupBy { it.serviceId }

        routes = logTime("routes") { GtfsParser().parseRoutesCsv() }
        routeId2routes = routes.associateBy { it.routeId }

        stops = setDistrictForStops(stops, districts!!)

        val stationId2departureCounts = stationId2stopTimes.mapValues { it.value.size}
        val maxDepartures = stationId2departureCounts.values.maxOrNull() ?: 1
        stationId2departurePercentage = stationId2departureCounts.mapValues { it.value / maxDepartures.toDouble() }
    }

    fun setDistrictForStops(stops: List<Stop>, districts: List<District>): List<Stop> {
        val allCoordinates = districts.map { it.coordinates.flatten() }.flatten()
        val allLongitudes = allCoordinates.map { it.longitude }
        val allLatitudes = allCoordinates.map { it.latitude }

        val minLongitude = allLongitudes.min()
        val maxLongitude = allLongitudes.max()
        val minLatitude = allLatitudes.min()
        val maxLatitude = allLatitudes.max()

        return stops.map { stop ->
            val stopCoordinate = Coordinate(stop.stopLat, stop.stopLon)
            var district: District? = null

            if (stop.stopLon >= minLongitude && stop.stopLon <= maxLongitude && stop.stopLat >= minLatitude && stop.stopLat <= maxLatitude) {
                district = districts.find { it.isCoordinateInDistrict(stopCoordinate) }
            }
            stop.copy(district = district)
        }
    }


}

fun main() {
    JLBerlinSim().test()
    println("done.")
}