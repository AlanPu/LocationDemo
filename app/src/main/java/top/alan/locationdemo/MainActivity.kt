package top.alan.locationdemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import top.alan.locationdemo.ui.theme.LocationDemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LocationViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    @Composable
    private fun doSth() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val context = LocalContext.current
        DisposableEffect(key1 = locationManager) {
            val gnssCallback = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    val found = status.satelliteCount
                    val count = (0 until status.satelliteCount).count { status.usedInFix(it) }
                    viewModel.onSatelliteStateChanged(
                        found,
                        count
                    )
                }
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.registerGnssStatusCallback(gnssCallback)
            }

            onDispose {
                locationManager.unregisterGnssStatusCallback(gnssCallback)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
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
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            viewModel.locationManager = locationManager
            viewModel.gpsLocationListener = locationListener(
                satelliteFound = viewModel.satelliteFound,
                satelliteCount = viewModel.satelliteCount,
                onLocationChanged = { it: String -> viewModel.onLocationChanged(it) })
            viewModel.networkLocationListener =
                locationListener(
                    satelliteFound = viewModel.satelliteFound,
                    satelliteCount = viewModel.satelliteCount,
                    onLocationChanged = { it: String -> viewModel.onLocationChanged(it) })
            requestPermission()
            doSth()
        }

    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}


