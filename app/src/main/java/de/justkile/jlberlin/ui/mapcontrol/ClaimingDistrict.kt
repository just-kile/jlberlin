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
fun ClaimingDistrict(
    time: Int,
    district: District,
    claimedBy: ClaimState?,
    onClaimAborted: () -> Unit,
    onClaimCompleted: () -> Unit
) {
    MapControl(
        contentArea = {
            Column  {
                TextWithLabel(
                    label = "Claiming ${district.name}",
                    value = "%01d:%02d".format(time / 60, time % 60),
                    info = "Don't move!"
                )
                TextWithLabel(
                    label = "Claimed by",
                    value = claimedBy?.team?.name ?: "No One",
                    info = claimedBy?.let {"${it.claimTimeInSeconds / 60} min" }
                )

            }
        },
        buttonArea = {
            if (time / 60 <=  (claimedBy?.claimTimeInSeconds ?: 0) / 60) {
                Button (
                    onClick = onClaimAborted,
                ) {
                    Text(text = "Abort")
                }
            } else {
                Button (
                    onClick = onClaimCompleted,
                ) {
                    Text(text = "Complete")
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewClaimingDistrict() {
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

    ClaimingDistrict(
        time = 125,
        district = sampleDistrict,
        claimedBy = claimState,
        onClaimAborted = {},
        onClaimCompleted = {}
    )
}