package de.justkile.jlberlin.ui.mapcontrol

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.justkile.jlberlinmodel.District
import de.justkile.jlberlin.ui.TextWithLabel
import de.justkile.jlberlin.viewmodel.ClaimState
import de.justkile.jlberlinmodel.Team


@Composable
fun SelectedDistrict(
    district: District,
    claimedBy: ClaimState?,
    onClose: () -> Unit
) {
    MapControl(
        contentArea = {
            Column() {
                TextWithLabel(
                    label = "Selected District",
                    value = district.name
                )
                TextWithLabel(
                    label = "Claimed by",
                    value = claimedBy?.team?.name ?: "No One",
                    info = claimedBy?.let {"${it.claimTimeInSeconds / 60} min" }
                )
            }
        },
        buttonArea = {
            Button(
                onClick = onClose
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectedDistrict() {
    val sampleDistrict = District(
        name = "Sample District",
        parentName = "Parent District",
        coordinates = emptyList()
    )
    val sampleTeam = Team(name = "Sample Team")
    val claimState = ClaimState(
        team = sampleTeam,
        claimTimeInSeconds = 120
    )

    SelectedDistrict(
        district = sampleDistrict,
        claimedBy = claimState,
        onClose = {}
    )
}

