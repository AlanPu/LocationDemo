package top.alan.locationdemo

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.alan.locationdemo.ui.theme.LocationDemoTheme
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val viewModel: LocationViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.grantPermission(it)
        }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationApp(
                        gpsEnabled = viewModel.gpsEnabled,
                        onGpsStateChanged = { newState -> viewModel.onGpsStateChanged(newState) },
                        networkEnabled = viewModel.networkEnabled,
                        onNetworkStateChanged = { newState ->
                            viewModel.onNetworkStateChanged(
                                newState
                            )
                        },
                        locationText = viewModel.locationText
                    )
                }
            }

            requestPermission()

            if (viewModel.permissionGranted.value) {
                RegisterListeners()
            }
        }
    }

    @Composable
    private fun RegisterListeners() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val launchTime = LocalTime.now()
        viewModel.locationManager = locationManager
        viewModel.gpsLocationListener = gpsLocationListener(
            launchTime = launchTime,
            satelliteFound = viewModel.satelliteFound,
            satelliteCount = viewModel.satelliteCount,
            onLocationChanged = { it: String -> viewModel.onLocationChanged(it) })
        viewModel.networkLocationListener =
            networkLocationListener(
                launchTime = launchTime,
                onLocationChanged = { it: String -> viewModel.onLocationChanged(it) })
        viewModel.gnssCallback = gnssCallback(
            locationManager = locationManager,
            onSatelliteStateChanged = { found, count ->
                viewModel.onSatelliteStateChanged(
                    found,
                    count
                )
            })
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}


