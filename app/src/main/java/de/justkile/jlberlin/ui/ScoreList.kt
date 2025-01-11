package de.justkile.jlberlin.ui

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.justkile.jlberlinmodel.Team

@Composable
fun ScoreList(team2score: Map<Team, Int>) {
    Log.i("ScoreList", "COMPOSE ${team2score.size}")
    LazyColumn {
        team2score.forEach { (team, score) ->
            item {
                Text(text = "${team.name}: $score")
            }
        }
    }
}