package top.alan.locationdemo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch
import top.alan.locationdemo.ui.theme.LocationDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationApp()
                }
            }
        }
    }
}

@Composable
fun LocationApp() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val geoCoder = Geocoder(context)
    var locationText by remember { mutableStateOf("Location data not available") }
    var addressInfo by remember { mutableStateOf("") }
    var satelliteCount by remember { mutableIntStateOf(0) }
    var satelliteFound by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationText = ""
                addressInfo = ""
                val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 5)
                if (addresses!!.isNotEmpty()) {
                    addresses.forEachIndexed { i, address ->
                        addressInfo += "Address${i + 1}: ${address.getAddressLine(0)}\n" +
                                "SubThoroughfare: ${address.subThoroughfare}\n" +
                                "SubAdminArea: ${address.subAdminArea}\n" +
                                "SubLocality: ${address.subLocality}\n" +
                                "City: ${address.locality}\n" +
                                "State: ${address.adminArea}\n" +
                                "Country: ${address.countryName}\n" +
                                "Postal Code: ${address.postalCode}\n" +
                                "------------------------------------\n"
                    }
                } else {
                    addressInfo = "No address found for the location."
                }

                val source =
                    when (location.provider) {
                        LocationManager.NETWORK_PROVIDER -> "Network"
                        LocationManager.GPS_PROVIDER -> "GPS"
                        else -> "N/A"
                    }

                locationText = "Source: $source\n" +
                    "Satellite Found: $satelliteFound\n" +
                    "Connected Satellites: $satelliteCount\n" +
                        "Latitude: ${location.latitude}\n" +
                        "Longitude: ${location.longitude}\n" +
                        "Accuracy: ${location.accuracy}m\n\n$addressInfo"
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    DisposableEffect(key1 = locationManager) {
        val gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                satelliteFound = status.satelliteCount
                satelliteCount = (0 until status.satelliteCount).count { status.usedInFix(it) }
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

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(scrollState)) {
        Text(text = locationText, modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = {
            coroutineScope.launch {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        1
                    )
                } else {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        10f,
                        locationListener
                    )
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        10f,
                        locationListener
                    )
                }
            }
        }) {
            Text("Start Location Updates")
        }
    }
}