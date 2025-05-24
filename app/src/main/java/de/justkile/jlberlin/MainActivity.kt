package de.justkile.jlberlin

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import de.justkile.jlberlin.datasource.ClaimRemoteDataSource
import de.justkile.jlberlin.datasource.DistrictLocalDataSource
import de.justkile.jlberlin.datasource.HistoryRemoteDataSource
import de.justkile.jlberlin.datasource.LocationRemoteDataSource
import de.justkile.jlberlin.datasource.TeamRemoteDataSource
import de.justkile.jlberlinmodel.Districts
import de.justkile.jlberlin.repository.ClaimRepository
import de.justkile.jlberlin.repository.DistrictRepository
import de.justkile.jlberlin.repository.HistoryRepository
import de.justkile.jlberlin.repository.LocationDataRepository
import de.justkile.jlberlin.repository.TeamRepository
import de.justkile.jlberlin.ui.DistrictMap
import de.justkile.jlberlin.ui.HistoryList
import de.justkile.jlberlin.ui.ScoreList
import de.justkile.jlberlin.ui.theme.JLBerlinTheme
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlin.viewmodel.GameViewModel
import de.justkile.jlberlinmodel.Team


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val teamRepository = TeamRepository(
            TeamRemoteDataSource()
        )


        setContent {
            viewModel = viewModel { GameViewModel(
                    DistrictRepository(
                        DistrictLocalDataSource(this@MainActivity)
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
            }

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
                val teams by viewModel.teams.collectAsState()
                val currentTeam by viewModel.team.collectAsState()
                val currentDistrict by viewModel.currentDistrict.collectAsState()
                DistrictMap(
                    districts = districts,
                    teams = teams,
                    currentTeam = currentTeam!!,
                    currentDistrict = currentDistrict,
                    district2claimState = viewModel::district2claimState,
                    claimDistrict = viewModel::claimDistrict,
                    gameViewModel = viewModel
                )
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



}


