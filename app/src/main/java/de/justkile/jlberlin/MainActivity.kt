package de.justkile.jlberlin

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.ui.theme.JLBerlinTheme
import de.justkile.jlberlin.viewmodel.GameViewModel
import de.justkile.jlberlinmodel.GeoJsonParser

import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polygon

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.google.maps.android.compose.MapProperties
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import de.justkile.jlberlin.datasource.ClaimRemoteDataSource
import de.justkile.jlberlin.datasource.DistrictLocalDataSource
import de.justkile.jlberlin.datasource.LocationRemoteDataSource
import de.justkile.jlberlin.datasource.TeamRemoteDataSource
import de.justkile.jlberlin.model.Coordinate
import de.justkile.jlberlin.model.Districts
import de.justkile.jlberlin.repository.ClaimRepository
import de.justkile.jlberlin.repository.DistrictRepository
import de.justkile.jlberlin.repository.LocationDataRepository
import de.justkile.jlberlin.repository.TeamRepository
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GameViewModel

    private val teamColors =
        listOf(Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan, Color.Yellow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        );

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = GameViewModel(
            DistrictRepository(
                DistrictLocalDataSource(this)
            ),
            TeamRepository(
                TeamRemoteDataSource()
            ),
            LocationDataRepository(
                LocationRemoteDataSource(fusedLocationClient)
            ),
            ClaimRepository(
                ClaimRemoteDataSource()
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
            startDestination = AppDestinations.HOME.name
        ) {
            composable(AppDestinations.HOME.name) {
                DistrictList(districts)
            }
            composable(AppDestinations.MAP.name) {
                DistrictMap(districts)
            }
            composable(AppDestinations.SCORE_BOARD.name) {
                ClaimDistrict(districts)
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
                    colors = ButtonDefaults.buttonColors(containerColor = teamColors[index]),
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
    fun ClaimDistrict(districts: Districts) {

        val selectedDistrict = remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        )
        {
            val field = AutoComplete(districts.districts.map { it.name }, onValueChanged = {
                selectedDistrict.value = it
                Log.i("ClaimDistrict", "Selected district: $it")
            })
            Button(
                onClick = {
                    Log.i("ClaimDistrict", "Claiming district ${selectedDistrict.value}")
                    val district = districts.districts.find { it.name == selectedDistrict.value }!!
                    viewModel.claimDistrict(district, viewModel.team.value!!)
                },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Claim District")
            }

        }
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
        val team2teamColor = teams.map { it to teamColors[teams.indexOf(it)] }.toMap()


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

        val scope = rememberCoroutineScope()
        LaunchedEffect(true) {

            scope.launch {
                while (levelOfDetail > 4) {
                    delay(500)
                    levelOfDetail /= 4
                }
            }

        }


        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                val claimedBy =
                    if (currentDistrict == null || !viewModel.district2claimState(currentDistrict!!)
                            .collectAsState().value.isClaimed()
                    ) "No One" else viewModel.district2claimState(currentDistrict!!)
                        .collectAsState().value.team!!.name
                Column(modifier = Modifier.weight(1f)) {
                    if (selectedDistrict == null) {
                        TextWithLabel("Located in district", currentDistrictName)
                        TextWithLabel("Claimed by", claimedBy)
                    } else {
                        TextWithLabel("Selected District", selectedDistrict!!.name)
                        TextWithLabel(
                            "Claimed by",
                            viewModel.district2claimState(selectedDistrict!!)
                                .collectAsState().value.team?.name ?: "No One"
                        )
                    }
                }

                if (selectedDistrict == null) {
                    Button(
                        onClick = {
                            viewModel.claimDistrict(currentDistrict!!, viewModel.team.value!!)
                        },
                        enabled = currentDistrict != null
                    ) {
                        Text(text = "Claim")
                    }
                } else {
                    Button(
                        onClick = {
                            selectedDistrict = null
                        }
                    ) {
                        Text(text = "Close")
                    }
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

    @Composable
    private fun TextWithLabel(label: String, value: String) {
        Text(buildAnnotatedString {
            append(label)
            append(": ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(value)
            }
        })
    }

    @Composable
    fun DistrictList(districts: Districts) {
        val groupedDistricts = districts.districts.groupBy { it.parentName }.toSortedMap()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            groupedDistricts.forEach { (parentName, districtList) ->
                item {
                    Text(
                        text = parentName,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                }
                items(districtList.sortedBy { it.name }) { district ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = district.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

    }

}


