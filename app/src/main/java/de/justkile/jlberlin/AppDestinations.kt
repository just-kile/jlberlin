package de.justkile.jlberlin

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    MAP(R.string.nav_map, Icons.Default.Home, R.string.nav_map),
    SCORE_BOARD(R.string.nav_scoreboard, Icons.Default.Email, R.string.nav_scoreboard),
    HISTORY(R.string.nav_history, Icons.Default.Info, R.string.nav_history)
}