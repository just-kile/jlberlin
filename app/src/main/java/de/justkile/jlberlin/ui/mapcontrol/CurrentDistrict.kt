package de.justkile.jlberlin.ui.mapcontrol


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.ui.TextWithLabel
import de.justkile.jlberlin.viewmodel.ClaimState
import de.justkile.jlberlinmodel.Team

@Composable
fun CurrentDistrict(
    district: District?,
    claimedBy: ClaimState?,
    onClaim: () -> Unit,
) {
    MapControl(

        contentArea = {
            Column() {
                TextWithLabel(
                    label = "Located in district",
                    value = district?.name ?: "Unknown"
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
                onClick = onClaim,
                enabled = district != null
            ) {
                Text(text = "Claim")
            }
        }
    )


}


@Preview(showBackground = true)
@Composable
fun PreviewCurrentDistrict() {
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

    CurrentDistrict(
        district = sampleDistrict,
        claimedBy = claimState,
        onClaim = {}
    )
}

