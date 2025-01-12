package de.justkile.jlberlin.ui.mapcontrol

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapControl(
    contentArea: @Composable () -> Unit,
    buttonArea: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    )  {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            contentArea()
        }
        buttonArea()
    }
}