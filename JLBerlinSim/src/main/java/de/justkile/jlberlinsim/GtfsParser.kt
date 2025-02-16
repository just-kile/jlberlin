package de.justkile.jlberlinsim

import com.opencsv.CSVReader
import de.justkile.jlberlinmodel.District
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Stop(
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val locationType: Int?,
    val parentStation: String?,

    val district: District? = null,
) {
    fun getStationId(): String {
        if(parentStation != null) {
            return parentStation
        }
        return stopId
    }
}

data class StopTime(
    val tripId: Long,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime,
    val stopId: String,
    val stopSequence: Int,
    val stopHeadsign: String?,
    val pickupType: Int?,
    val dropOffType: Int?,

    var stationId: String? = null
)

data class Trip(
    val routeId: String,
    val serviceId: Int,
    val tripId: Long,
)

data class Calendar(
    val monday: Int,
    val tuesday: Int,
    val wednesday: Int,
    val thursday: Int,
    val friday: Int,
    val saturday: Int,
    val sunday: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val serviceId: Int
) {
    fun isSameWeekday(date: LocalDate): Boolean {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> monday == 1
            DayOfWeek.TUESDAY -> tuesday == 1
            DayOfWeek.WEDNESDAY -> wednesday == 1
            DayOfWeek.THURSDAY -> thursday == 1
            DayOfWeek.FRIDAY -> friday == 1
            DayOfWeek.SATURDAY -> saturday == 1
            DayOfWeek.SUNDAY -> sunday == 1
        }
    }

    fun isRunningOnDate(date: LocalDate): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return isSameWeekday(date) && date.isAfter(startDate) && date.isBefore(endDate)
    }
}

data class Route(
    val routeLongName: String?,
    val routeShortName: String,
    val agencyId: Int,
    val routeDesc: String?,
    val routeType: Int,
    val routeId: String
)

data class CalendarDate(
    val serviceId: Int,
    val exceptionType: Int,
    val date: String
)

class GtfsParser {
    fun parseStopsCsv(): List<Stop> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\filtered\\stops.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val stops = mutableListOf<Stop>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val stop = Stop(
                        stopName = it[0],
                        parentStation = it[1].takeIf { it.isNotEmpty() },
                        stopId = it[2],
                        stopLat = it[3].toDouble(),
                        stopLon = it[4].toDouble(),
                        locationType = it[5].takeIf { it.isNotEmpty() }?.toInt()
                    )
                    stops.add(stop)
                }
            }
        }

        return stops
    }

    fun toCvsInt(o: Int?):String {
        if(o == null) {
            return ""
        }
        return o.toString()
    }

    fun toCvsString(s: String?):String {
        if(s == null) {
            return ""
        } else if (s.contains(",")) {
            return "\"$s\""
        }
        return s
    }

    fun saveStopsCsv(stops: List<Stop>) {
        val file = File("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\filtered\\stops.txt")
        val writer = file.bufferedWriter()
        writer.use {
            it.write("stop_name,parent_station,stop_id,stop_lat,stop_lon,location_type\n")
            stops.forEach { stop ->
                it.write("${toCvsString(stop.stopName)},${toCvsString(stop.parentStation)},${stop.stopId},${stop.stopLat},${stop.stopLon},${toCvsInt(stop.locationType)}\n")
            }
        }
    }

    fun parseStopTimesCsv(stops: Map<String, Stop>): List<StopTime> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\filtered\\stop_times.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val stopTimes = mutableListOf<StopTime>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val trip = StopTime(
                        tripId = it[0].toLong(),
                        arrivalTime = parseTime(it[1]),
                        departureTime = parseTime(it[2]),
                        stopId = it[3],
                        stopSequence = it[4].toInt(),
                        stopHeadsign = it[5].takeIf { it.isNotEmpty() },
                        pickupType = it[6].takeIf { it.isNotEmpty() }?.toInt(),
                        dropOffType = it[7].takeIf { it.isNotEmpty() }?.toInt()

                    )
                    stopTimes.add(trip)
                }
            }
        }

        stopTimes.forEach { stopTime ->
            val stop = stops.get(stopTime.stopId)
            if (stop != null) {
                stopTime.stationId = stop!!.getStationId()
            }
        }

        return stopTimes
    }

    fun parseTime(time: String) : LocalTime {
        try {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) {
            // time will be 24:00:00 or later for same service day but after midnight -> set to 23,59,59 as we won't need the data anyways
            return LocalTime.of(23,59,59)
        }
    }

    fun parseDate(date: String) : LocalDate {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    fun saveStopTimesCvs(stopTimes: List<StopTime>) {
        val file = File("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\filtered\\stop_times.txt")
        val writer = file.bufferedWriter()
        writer.use {
            it.write("trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type\n")
            stopTimes.forEach { stopTime ->
                it.write("${stopTime.tripId},${stopTime.arrivalTime},${stopTime.departureTime},${stopTime.stopId},${stopTime.stopSequence},${toCvsString(stopTime.stopHeadsign)},${toCvsInt(stopTime.pickupType)},${toCvsInt(stopTime.dropOffType)}\n")
            }
        }
    }

    fun parseTripsCsv(): List<Trip> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\trips.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val trips = mutableListOf<Trip>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val trip = Trip(
                        routeId = it[0],
                        serviceId = it[1].toInt(),
                        tripId = it[2].toLong(),
                    )
                    trips.add(trip)
                }
            }
        }

        return trips
    }

    fun parseCalendarCsv(): List<Calendar> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\calendar.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val calendars = mutableListOf<Calendar>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val calendar = Calendar(
                        monday = it[0].toInt(),
                        tuesday = it[1].toInt(),
                        wednesday = it[2].toInt(),
                        thursday = it[3].toInt(),
                        friday = it[4].toInt(),
                        saturday = it[5].toInt(),
                        sunday = it[6].toInt(),
                        startDate = parseDate(it[7]),
                        endDate = parseDate(it[8]),
                        serviceId = it[9].toInt()
                    )
                    calendars.add(calendar)
                }
            }
        }

        return calendars
    }

    fun parseRoutesCsv(): List<Route> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\routes.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val routes = mutableListOf<Route>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val route = Route(
                        routeLongName = it[0].takeIf { it.isNotEmpty() },
                        routeShortName = it[1],
                        agencyId = it[2].toInt(),
                        routeDesc = it[3].takeIf { it.isNotEmpty() },
                        routeType = it[4].toInt(),
                        routeId = it[5]
                    )
                    routes.add(route)
                }
            }
        }

        return routes
    }

    fun parseCalendarDatesCsv(): List<CalendarDate> {
        val inputStream = FileInputStream("D:\\workspaces\\jl-berlin\\JLBerlin\\JLBerlinSim\\src\\main\\resources\\GTFS\\calendar_dates.txt")
        val reader = CSVReader(InputStreamReader(inputStream))
        val calendarDates = mutableListOf<CalendarDate>()

        reader.use {
            val header = reader.readNext() // Skip the header row
            var line: Array<String>?

            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val calendarDate = CalendarDate(
                        serviceId = it[0].toInt(),
                        exceptionType = it[1].toInt(),
                        date = it[2]
                    )
                    calendarDates.add(calendarDate)
                }
            }
        }

        return calendarDates
    }

}