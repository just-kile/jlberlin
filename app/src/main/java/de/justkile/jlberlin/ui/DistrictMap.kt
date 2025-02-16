package de.justkile.jlberlin.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import de.justkile.jlberlin.model.Coordinate
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.model.Districts
import de.justkile.jlberlin.ui.mapcontrol.ClaimingDistrict
import de.justkile.jlberlin.ui.mapcontrol.CurrentDistrict
import de.justkile.jlberlin.ui.mapcontrol.SelectedDistrict
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ClaimState
import de.justkile.jlberlin.viewmodel.TimerViewModel
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun DistrictMap(districts: Districts,
                teams: List<Team>,
                currentTeam: Team,
                currentDistrict: District?,
                district2claimState: (District) -> StateFlow<ClaimState>,
                claimDistrict: (District, Team, Int) -> Unit,
                ) {
    Log.i("DistrictMap", "DistrictMap(...) COMPOSE")

    val colors = TeamColors
    val team2teamColor = teams.associateWith { colors[teams.indexOf(it)] }

    var selectedDistrict by remember { mutableStateOf<District?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        DistrictControls(
            currentDistrict = currentDistrict,
            district2claimState = district2claimState,
            claimDistrict = claimDistrict,
            currentTeam = currentTeam,
            selectedDistrict = selectedDistrict,
            onUnselectDistrict = { selectedDistrict = null }
        )

        MapLayer(
            districts = districts,
            district2claimState = district2claimState,
            selectedDistrict = selectedDistrict,
            team2teamColor = team2teamColor,
            onSelectDistrict = { selectedDistrict = it}
        )

    }
}

@Composable
private fun DistrictControls(
    currentDistrict: District?,
    district2claimState: (District) -> StateFlow<ClaimState>,
    claimDistrict: (District, Team, Int) -> Unit,
    currentTeam: Team,
    selectedDistrict: District?,
    onUnselectDistrict: () -> Unit
) {
    var isClaiming by remember { mutableStateOf(false) }
    val timerViewModel = viewModel<TimerViewModel>()

    if (isClaiming) {
        val time by timerViewModel.time.collectAsState()
        ClaimingDistrict(
            time = time,
            district = currentDistrict!!,
            claimedBy = currentDistrict.let { district2claimState(it).collectAsState().value },
            onClaimCompleted = {
                isClaiming = false
                claimDistrict(
                    currentDistrict,
                    currentTeam,
                    time - time % 60
                )
                timerViewModel.stopTimer()
            },
            onClaimAborted = {
                isClaiming = false
                timerViewModel.stopTimer()
            }
        )
    } else if (selectedDistrict == null) {
        CurrentDistrict(
            district = currentDistrict,
            claimedBy = currentDistrict?.let { district2claimState(it).collectAsState().value },
            onClaim = {
                isClaiming = true
                timerViewModel.startTimer()
            }
        )
    } else {
        SelectedDistrict(
            district = selectedDistrict,
            claimedBy = district2claimState(selectedDistrict).collectAsState().value,
            onClose = onUnselectDistrict
        )
    }
}

@Composable
private fun MapLayer(
    districts: Districts,
    district2claimState: (District) -> StateFlow<ClaimState>,
    selectedDistrict: District?,
    team2teamColor: Map<Team, Color>,
    onSelectDistrict: (District) -> Unit
) {
    Log.i("DistrictMap", "MapLayer(...) COMPOSE")

    val berlin = LatLng(52.520008, 13.404954)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(berlin, 10f)
    }

    var levelOfDetail by remember { mutableIntStateOf(250) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(true) {
        scope.launch {
            while (levelOfDetail > 4) {
                delay(500)
                levelOfDetail /= 4
            }
            levelOfDetail = 1
        }

    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true)

    ) {

        districts.districts.forEach { district ->
            district.coordinates.forEach { coordinates ->

                val claimState by district2claimState(district).collectAsState()
                val color =
                    if (selectedDistrict == district) Color.Yellow.copy(alpha = 0.6f) else if (claimState.isClaimed()) team2teamColor[claimState.team]!!.copy(
                        alpha = 0.4f
                    ) else Color.Black.copy(alpha = 0.4f)

                val points =
                    coordinates.filterIndexed { index, _ -> index % levelOfDetail == 0 }
                        .map { LatLng(it.latitude, it.longitude) }

                Polygon(
                    points = points,
                    fillColor = color,
                    strokeColor = Color.Black,
                    onClick = {
                        onSelectDistrict(district)
                    },
                    clickable = true
                )

            }
        }
    }
}
