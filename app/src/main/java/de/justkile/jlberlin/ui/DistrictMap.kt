package de.justkile.jlberlin.ui

import android.annotation.SuppressLint
import android.graphics.Point
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import de.justkile.jlberlin.ui.mapcontrol.ClaimingDistrict
import de.justkile.jlberlin.ui.mapcontrol.CurrentDistrict
import de.justkile.jlberlin.ui.mapcontrol.SelectedDistrict
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.ClaimState
import de.justkile.jlberlin.viewmodel.GameViewModel
import de.justkile.jlberlin.viewmodel.TimerViewModel
import de.justkile.jlberlinmodel.Coordinate
import de.justkile.jlberlinmodel.District
import de.justkile.jlberlinmodel.Districts
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
@Composable
fun DistrictMap(
    districts: Districts,
    teams: List<Team>,
    currentTeam: Team,
    currentDistrict: District?,
    district2claimState: (District) -> StateFlow<ClaimState>,
    claimDistrict: (District, Team, Int) -> Unit,
    gameViewModel: GameViewModel
) {
    Log.i("DistrictMap", "DistrictMap(...) COMPOSE")

    val colors = TeamColors
    val team2teamColor = teams.associateWith { colors[teams.indexOf(it)] }

    var selectedDistrict by remember { mutableStateOf<District?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        DistrictControls(
            currentDistrict = currentDistrict,
            district2claimState = district2claimState,
            claimDistrict = claimDistrict,
            currentTeam = currentTeam,
            selectedDistrict = selectedDistrict,
            onUnselectDistrict = { selectedDistrict = null },
            isClaiming = gameViewModel.isClaiming.collectAsState().value,
            districtBeingClaimed = gameViewModel.districtBeingClaimed.collectAsState().value,
            timerValue = gameViewModel.timerValue.collectAsState().value,
            startClaiming = gameViewModel::startClaimingDistrict,
            stopClaiming = gameViewModel::stopClaimingDistrict
        )

        MapLayer(districts = districts,
            district2claimState = district2claimState,
            selectedDistrict = selectedDistrict,
            team2teamColor = team2teamColor,
            onSelectDistrict = {
                if(selectedDistrict == it) {
                    selectedDistrict = null
                } else {
                    selectedDistrict = it
                }
            })

    }
}

@Composable
private fun DistrictControls(
    currentDistrict: District?,
    district2claimState: (District) -> StateFlow<ClaimState>,
    claimDistrict: (District, Team, Int) -> Unit,
    currentTeam: Team,
    selectedDistrict: District?,
    onUnselectDistrict: () -> Unit,
    isClaiming: Boolean,
    districtBeingClaimed: District?,
    timerValue: Int,
    startClaiming: (District) -> Unit,
    stopClaiming: () -> Unit
) {
    if (isClaiming && districtBeingClaimed != null) {
        ClaimingDistrict(time = timerValue,
            district = districtBeingClaimed,
            claimedBy = district2claimState(districtBeingClaimed).collectAsState().value,
            onClaimCompleted = {
                claimDistrict(
                    districtBeingClaimed, currentTeam, timerValue - timerValue % 60
                )
                stopClaiming()
            },
            onClaimAborted = {
                stopClaiming()
            })
    } else if (selectedDistrict == null) {
        CurrentDistrict(district = currentDistrict,
            claimedBy = currentDistrict?.let { district2claimState(it).collectAsState().value },
            onClaim = {
                currentDistrict?.let { startClaiming(it) }
            })
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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(toLatLng(districts.center), 9.5f)
    }

    val maxEpsilon = 1e-3
    val minEpsilon = 1e-6
    val steps = 10
    var epsilon by remember { mutableStateOf(1e-3) }


    LaunchedEffect(Unit) {
        snapshotFlow {cameraPositionState.position.zoom}.collect {
            if (cameraPositionState.projection != null) {
                val accuracy = 2
                val proj1 = cameraPositionState.projection!!.fromScreenLocation(Point(0, 0))
                val proj2 = cameraPositionState.projection!!.fromScreenLocation(Point(accuracy, accuracy))
                val diff = sqrt((proj1.latitude - proj2.latitude).pow(2) + (proj1.longitude - proj2.longitude).pow(2))


                val newEpsilon = calcClosestStep(minEpsilon, maxEpsilon, diff, steps)
                if (newEpsilon < epsilon) {
                    Log.i("DistrictMap", "MapLayer(...) ADJUSTED EPSILON: $epsilon -> $newEpsilon, diff: $diff")
                    epsilon = newEpsilon
                }
            }
        }

    }


    Box(
        modifier = Modifier.fillMaxSize(),
    ) {


        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),

        ) {

            var newNumberOfRenderedPolygons = 0
            var newNumberOfRenderedPoints = 0

            districts.districts.forEach { district ->

                val reducedCoordinates = district.calcCoordinatesForLevelOfDetail(epsilon)

                reducedCoordinates.forEach { coordinates ->

                    newNumberOfRenderedPolygons++

                    val claimState by district2claimState(district).collectAsState()
                    val color =
                        if (selectedDistrict == district) Color.Yellow.copy(alpha = 0.6f) else if (claimState.isClaimed()) team2teamColor[claimState.team]!!.copy(
                            alpha = 0.4f
                        ) else Color.Black.copy(alpha = 0.4f)

                    val points = coordinates.map { toLatLng(it) }

                    newNumberOfRenderedPoints += points.size

                    Polygon(
                        points = points, fillColor = color, strokeColor = Color.Black, onClick = {
                            onSelectDistrict(district)
                        }, clickable = true
                    )

                }
            }

            Log.i("DistrictMap", "MapLayer(...) COMPOSED: $newNumberOfRenderedPolygons polygons, $newNumberOfRenderedPoints points, epsilon: $epsilon")

        }


    }

}

private fun toLatLng(coordinate: Coordinate): LatLng = LatLng(coordinate.latitude, coordinate.longitude)

private fun calcClosestStep(minValue: Double, maxValue: Double, actualValue: Double, steps: Int): Double {
    if (actualValue <= minValue) return minValue
    if (actualValue >= maxValue) return maxValue

    val stepSize = (maxValue - minValue) / steps
    val closestStep = ((actualValue - minValue) / stepSize).roundToInt() * stepSize + minValue

    return closestStep
}

private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()
