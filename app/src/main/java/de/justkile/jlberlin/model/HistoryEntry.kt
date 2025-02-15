package de.justkile.jlberlin.model

import de.justkile.jlberlin.viewmodel.ColoredTeam
import java.time.LocalDateTime

data class HistoryEntry(
    val team: ColoredTeam,
    val districtName: String,
    val claimTimeInSeconds: Int,
    val claimedAt: LocalDateTime
)