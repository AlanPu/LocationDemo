package top.alan.locationdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationListener
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import top.alan.locationdemo.ui.theme.LocationDemoTheme
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val viewModel: LocationViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

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
                        viewModel.gpsEnabled,
                        { newState -> viewModel.onGpsStateChanged(newState) },
                        viewModel.networkEnabled,
                        { newState -> viewModel.onNetworkStateChanged(newState) },
                        viewModel.locationText
                    )
                }
            }
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            viewModel.locationManager = locationManager
            viewModel.gpsLocationListener = locationListener { it -> viewModel.onLocationChanged(it) }
            viewModel.networkLocationListener = locationListener { it -> viewModel.onLocationChanged(it) }
        }
        requestPermission()
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}


