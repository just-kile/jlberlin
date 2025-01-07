import de.justkile.jlberlinsim.Calendar
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarTest {

    @Test
    fun testIsSameWeekday() {
        val calendar = Calendar(
            serviceId = 1,
            monday = 1,
            tuesday = 0,
            wednesday = 1,
            thursday = 0,
            friday = 1,
            saturday = 0,
            sunday = 1,
            startDate = LocalDate.of(2024,12,19),
            endDate = LocalDate.of(2025,12,13)
        )

        assertTrue(calendar.isSameWeekday(LocalDate.of(2024, 12, 23))) // Monday
        assertFalse(calendar.isSameWeekday(LocalDate.of(2024, 12, 24))) // Tuesday
        assertTrue(calendar.isSameWeekday(LocalDate.of(2024, 12, 25))) // Wednesday
        assertFalse(calendar.isSameWeekday(LocalDate.of(2024, 12, 26))) // Thursday
        assertTrue(calendar.isSameWeekday(LocalDate.of(2024, 12, 27))) // Friday
        assertFalse(calendar.isSameWeekday(LocalDate.of(2024, 12, 28))) // Saturday
        assertTrue(calendar.isSameWeekday(LocalDate.of(2024, 12, 29))) // Sunday
    }

    @Test
    fun testIsRunningOnDate() {
        val calendar = Calendar(
            serviceId = 1,
            monday = 1,
            tuesday = 0,
            wednesday = 1,
            thursday = 0,
            friday = 1,
            saturday = 0,
            sunday = 1,
            startDate = LocalDate.of(2024,12,19),
            endDate = LocalDate.of(2025,12,13)
        )

        assertTrue(calendar.isRunningOnDate(LocalDate.of(2024, 12, 23))) // Monday
        assertFalse(calendar.isRunningOnDate(LocalDate.of(2024, 12, 24))) // Tuesday
        assertTrue(calendar.isRunningOnDate(LocalDate.of(2024, 12, 25))) // Wednesday
        assertFalse(calendar.isRunningOnDate(LocalDate.of(2024, 12, 26))) // Thursday
        assertTrue(calendar.isRunningOnDate(LocalDate.of(2024, 12, 27))) // Friday
        assertFalse(calendar.isRunningOnDate(LocalDate.of(2024, 12, 28))) // Saturday
        assertTrue(calendar.isRunningOnDate(LocalDate.of(2024, 12, 29))) // Sunday
        assertFalse(calendar.isRunningOnDate(LocalDate.of(2024, 12, 18))) // Before start date
        assertFalse(calendar.isRunningOnDate(LocalDate.of(2025, 12, 14))) // After end date
    }
}