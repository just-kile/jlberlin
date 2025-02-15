package de.justkile.jlberlin.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ColoredTeam
import de.justkile.jlberlinmodel.Team

@Composable
fun ScoreList(team2score: Map<ColoredTeam, Int>) {
    Log.i("ScoreList", "COMPOSE ${team2score.size}")
    LazyColumn {
        team2score.forEach { (team, score) ->
            item {
                TeamCard(team) {
                    Text(
                        style = MaterialTheme.typography.headlineMedium,
                        text = "${team.name}: $score"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScoreList() {
    val sampleTeams = listOf(
        ColoredTeam(name = "Team A", TeamColors[0]),
        ColoredTeam(name = "Team B", TeamColors[1]),
        ColoredTeam(name = "Team C", TeamColors[2])
    )
    val sampleScores = mapOf(
        sampleTeams[0] to 20,
        sampleTeams[1] to 15,
        sampleTeams[2] to 8
    )
    ScoreList(team2score = sampleScores)
}

