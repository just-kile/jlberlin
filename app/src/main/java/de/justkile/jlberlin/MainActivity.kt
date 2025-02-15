package de.justkile.jlberlin

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import de.justkile.jlberlin.datasource.ClaimRemoteDataSource
import de.justkile.jlberlin.datasource.DistrictLocalDataSource
import de.justkile.jlberlin.datasource.HistoryRemoteDataSource
import de.justkile.jlberlin.datasource.LocationRemoteDataSource
import de.justkile.jlberlin.datasource.TeamRemoteDataSource
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.model.Districts
import de.justkile.jlberlin.repository.ClaimRepository
import de.justkile.jlberlin.repository.DistrictRepository
import de.justkile.jlberlin.repository.HistoryRepository
import de.justkile.jlberlin.repository.LocationDataRepository
import de.justkile.jlberlin.repository.TeamRepository
import de.justkile.jlberlin.ui.HistoryList
import de.justkile.jlberlin.ui.ScoreList
import de.justkile.jlberlin.ui.TextWithLabel
import de.justkile.jlberlin.ui.mapcontrol.ClaimingDistrict
import de.justkile.jlberlin.ui.mapcontrol.CurrentDistrict
import de.justkile.jlberlin.ui.mapcontrol.SelectedDistrict
import de.justkile.jlberlin.ui.theme.JLBerlinTheme
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.GameViewModel
import de.justkile.jlberlin.viewmodel.TimerViewModel
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        );

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val teamRepository = TeamRepository(
            TeamRemoteDataSource()
        )
        viewModel = GameViewModel(
            DistrictRepository(
                DistrictLocalDataSource(this)
            ),
            teamRepository,
            LocationDataRepository(
                LocationRemoteDataSource(fusedLocationClient)
            ),
            ClaimRepository(
                ClaimRemoteDataSource()
            ),
            HistoryRepository(
                HistoryRemoteDataSource(),
                teamRepository
            )
        )

        setContent {
            val navController = rememberNavController()

            val team by viewModel.team.collectAsState()


            JLBerlinTheme {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = {
                        if (team != null) {
                            NavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        if (team != null) {
                            MainContent(navController, viewModel.districts)
                        } else {
                            val teams by viewModel.teams.collectAsState()
                            TeamSelection(
                                teams = teams,
                                onTeamSelected = viewModel::selectTeam,
                                onNewTeamCreated = viewModel::createNewTeam
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MainContent(
        navController: NavHostController,
        districts: Districts
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.MAP.name
        ) {
            composable(AppDestinations.SCORE_BOARD.name) {
                val team2score by viewModel.team2Score.collectAsState()
                ScoreList(team2score)
            }
            composable(AppDestinations.MAP.name) {
                DistrictMap(districts)
            }
            composable(AppDestinations.HISTORY.name) {
                val history by viewModel.history.collectAsState()
                HistoryList(history)
            }
        }
    }

    @Composable
    private fun TeamSelection(
        teams: List<Team>,
        onTeamSelected: (Team) -> Unit,
        onNewTeamCreated: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        )
        {
            Text(text = "Select your team.")

            teams.forEachIndexed { index, team ->
                Button(
                    onClick = { onTeamSelected(team) },
                    colors = ButtonDefaults.buttonColors(containerColor = TeamColors[index]),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = team.name)
                }

            }

            HorizontalDivider(thickness = 2.dp)


            var newTeamName by remember { mutableStateOf(TextFieldValue("")) }

            Text("New Team")
            OutlinedTextField(
                value = newTeamName,
                onValueChange = { newTeamName = it },
                label = { Text("Team Name") }
            )
            Button(
                onClick = {
                    onNewTeamCreated(newTeamName.text)
                    newTeamName = TextFieldValue("")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = newTeamName.text.isNotEmpty()
            ) {
                Text("Create Team")
            }

        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun TopBar() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("JL Berlin")
            }
        )
    }

    @Composable
    fun NavBar(navController: NavController) {
        var activeItemIndex by remember { mutableStateOf(0) }

        NavigationBar {
            AppDestinations.entries.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = stringResource(item.contentDescription)
                        )
                    },
                    label = { Text(stringResource(item.label)) },
                    selected = activeItemIndex == index,
                    onClick = {
                        activeItemIndex = index
                        navController.navigate(item.name)
                    }
                )
            }
        }
    }

    @Stable
    data class TestInput(
        val districts: List<District>,
        val innerPadding: PaddingValues
    )


    @SuppressLint("MissingPermission")
    @Composable
    fun DistrictMap(districts: Districts) {
        Log.i("DistrictMap", "COMPOSE")

        val teams = viewModel.teams.collectAsState().value
        val colors = TeamColors
        val team2teamColor = teams.map { it to colors[teams.indexOf(it)] }.toMap()


        val berlin = LatLng(52.520008, 13.404954)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(berlin, 10f)
        }

        val currentLocation by viewModel.currentLocation.collectAsState()
        viewModel.currentLocation.run {
            Log.i("DistrictMap", "Current Location: $currentLocation")
        }

        var selectedDistrict by remember { mutableStateOf<District?>(null) }

        var currentDistrictName by remember {
            mutableStateOf(
                currentLocation?.let { districts.findDistrictByCoordinate(it) }?.name ?: "Unknown"
            )
        }

        var currentDistrict by remember {
            mutableStateOf(currentLocation?.let { districts.findDistrictByCoordinate(it) })
        }

        var levelOfDetail by remember { mutableStateOf(250) }

        var isClaming by remember { mutableStateOf(false) }

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


            if (isClaming) {
                val time by timerViewModel.time.collectAsState()
                ClaimingDistrict (
                    time = time,
                    district = currentDistrict!!,
                    claimedBy = currentDistrict?.let { viewModel.district2claimState(it).collectAsState().value },
                    onClaimCompleted = {
                        isClaming = false
                        viewModel.claimDistrict(
                            district = currentDistrict!!,
                            team = viewModel.team.value!!,
                            claimTimeInSeconds = time - time % 60)
                        timerViewModel.stopTimer()
                    },
                    onClaimAborted = {
                        isClaming = false
                        timerViewModel.stopTimer()
                    }
                )
            }
            else if (selectedDistrict == null) {
                CurrentDistrict(
                    district = currentDistrict,
                    claimedBy = currentDistrict?.let { viewModel.district2claimState(it).collectAsState().value },
                    onClaim = {
                        isClaming = true
                        timerViewModel.startTimer()
                    }
                )
            } else {
                SelectedDistrict(
                    district = selectedDistrict!!,
                    claimedBy = viewModel.district2claimState(selectedDistrict!!).collectAsState().value,
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

                        val claimState by viewModel.district2claimState(district).collectAsState()
                        val color =
                            if (selectedDistrict == district) Color.Yellow.copy(alpha = 0.6f) else if (claimState.isClaimed()) team2teamColor[claimState.team]!!.copy(
                                alpha = 0.4f
                            ) else Color.Black.copy(alpha = 0.4f)

                        val points =
                            coordinates.filterIndexed { index, coordinate -> index % levelOfDetail == 0 }
                                .map { LatLng(it.latitude, it.longitude) }
                        val context = LocalContext.current

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


}


