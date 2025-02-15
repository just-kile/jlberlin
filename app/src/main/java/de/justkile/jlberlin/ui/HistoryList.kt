package de.justkile.jlberlin.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.justkile.jlberlin.model.HistoryEntry
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ColoredTeam
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HistoryList(entries: List<HistoryEntry>) {
    LazyColumn {
        item {
            entries.forEach { entry ->
                TeamCard(entry.team) {
                    val time = entry.claimedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val minutes = entry.claimTimeInSeconds / 60
                    Text(
                        style = MaterialTheme.typography.headlineMedium, text = "${entry.team.name} claimed ${entry.districtName}"
                    )
                    Text(
                        text = "Claimed at $time for $minutes minutes",
                    )
                }
            }
        }

    }
}

@Preview
@Composable
fun PreviewHistoryList() {
    HistoryList(
        listOf(
            HistoryEntry(
                team = ColoredTeam("Team A", TeamColors[0]),
                districtName = "District 1",
                claimTimeInSeconds = 60,
                claimedAt = LocalDateTime.now().minusMinutes(5)
            ), HistoryEntry(
                team = ColoredTeam("Team B", TeamColors[1]),
                districtName = "District 2",
                claimTimeInSeconds = 120,
                claimedAt = LocalDateTime.now().minusHours(1)
            ), HistoryEntry(
                team = ColoredTeam("Team C", TeamColors[2]),
                districtName = "District 3 Is Very Long",
                claimTimeInSeconds = 180,
                claimedAt = LocalDateTime.now().minusHours(2)
            )
        )
    )
}

