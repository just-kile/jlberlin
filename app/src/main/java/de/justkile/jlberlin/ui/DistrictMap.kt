package de.justkile.jlberlin.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                currentLocation: Coordinate?,
                district2claimState: (District) -> StateFlow<ClaimState>,
                claimDistrict: (District, Team, Int) -> Unit,
                ) {
    Log.i("DistrictMap", "COMPOSE")

    val colors = TeamColors
    val team2teamColor = teams.map { it to colors[teams.indexOf(it)] }.toMap()


    val berlin = LatLng(52.520008, 13.404954)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(berlin, 10f)
    }

    var selectedDistrict by remember { mutableStateOf<District?>(null) }

    val currentDistrict by remember {
        mutableStateOf(currentLocation?.let { districts.findDistrictByCoordinate(it) })
    }

    var levelOfDetail by remember { mutableStateOf(250) }

    var isClaiming by remember { mutableStateOf(false) }

    val timerViewModel = viewModel<TimerViewModel>()

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


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        if (isClaiming) {
            val time by timerViewModel.time.collectAsState()
            ClaimingDistrict(
                time = time,
                district = currentDistrict!!,
                claimedBy = currentDistrict?.let { district2claimState(it).collectAsState().value },
                onClaimCompleted = {
                    isClaiming = false
                    claimDistrict(
                        currentDistrict!!,
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
                district = selectedDistrict!!,
                claimedBy = district2claimState(selectedDistrict!!).collectAsState().value,
                onClose = { selectedDistrict = null }
            )
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
                        coordinates.filterIndexed { index, coordinate -> index % levelOfDetail == 0 }
                            .map { LatLng(it.latitude, it.longitude) }

                    Polygon(
                        points = points,
                        fillColor = color,
                        strokeColor = Color.Black,
                        onClick = {
                            // Toast.makeText(context, district.name, Toast.LENGTH_SHORT).show()
                            selectedDistrict = district
                        },
                        clickable = true
                    )

                }
            }
        }

    }
}
