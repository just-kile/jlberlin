package de.justkile.jlberlin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ColoredTeam

@Composable
fun TeamCard(team: ColoredTeam, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(2.dp, team.color),
    ) {
        Column (modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewTeamCard() {
    TeamCard(ColoredTeam("Team A", TeamColors[0])) {
        Text("Content")
    }
}