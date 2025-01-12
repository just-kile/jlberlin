package de.justkile.jlberlin.ui.mapcontrol

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.ui.TextWithLabel
import de.justkile.jlberlinmodel.Team


@Composable
fun SelectedDistrict(
    district: District,
    claimedBy: Team?,
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
                    value = claimedBy?.name ?: "No One"
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

    SelectedDistrict(
        district = sampleDistrict,
        claimedBy = sampleTeam,
        onClose = {}
    )
}

