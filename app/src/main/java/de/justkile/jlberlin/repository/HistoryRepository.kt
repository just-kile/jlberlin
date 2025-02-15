package de.justkile.jlberlin.repository


import android.util.Log
import androidx.compose.ui.graphics.Color
import de.justkile.jlberlin.datasource.HistoryData
import de.justkile.jlberlin.datasource.HistoryRemoteDataSource
import de.justkile.jlberlin.model.HistoryEntry
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ColoredTeam
import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.ZoneOffset

class HistoryRepository(
    private val historyRemoteDateSource: HistoryRemoteDataSource,
    private val teamRepository: TeamRepository
) {

    private val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())

    private var _history = MutableStateFlow(emptyList<HistoryEntry>())
    val history = _history.asStateFlow()

    fun listenForUpdates() {
        historyRemoteDateSource.addListener(
            onHistoriesChanged = {
                val teams = teamRepository.teams.value
                _history.value = mapHistoryEntries(it, teams)
            },
            onError = { Log.e("HistoryRepository","Error while listening for new history entries", it) }
        )
    }

    suspend fun getHistory(): List<HistoryEntry> {
        val teams = teamRepository.teams.value

        val historyEntries = historyRemoteDateSource.getHistory()
        return mapHistoryEntries(historyEntries, teams)
    }

    private fun mapHistoryEntries(
        historyEntries: List<HistoryData>,
        teams: List<Team>
    ) = historyEntries.map { entry ->

        HistoryEntry(
            team = ColoredTeam(
                name = entry.teamName,
                color = findColorForTeam(teams, entry)
            ),
            districtName = entry.districtName,
            claimTimeInSeconds = entry.claimTimeInSeconds,
            claimedAt = LocalDateTime.ofEpochSecond(entry.claimedAt, 0, zoneOffset)
        )
    }

    fun createHistory(teamName: String, districtName: String, claimTimeInSeconds: Int) =
        historyRemoteDateSource.createNewHistoryEntry(
            HistoryData(
                teamName = teamName,
                districtName = districtName,
                claimTimeInSeconds = claimTimeInSeconds,
                claimedAt = LocalDateTime.now().toEpochSecond(zoneOffset)
            )
        )
    }


    private fun findColorForTeam(
        teams: List<Team>,
        entry: HistoryData
    ): Color {
        val team = teams.find { it.name == entry.teamName }
        var color = Color.Black
        if (team != null && teams.indexOf(team) < TeamColors.size) {
            color = TeamColors[teams.indexOf(team)]
        }
        return color
    }



