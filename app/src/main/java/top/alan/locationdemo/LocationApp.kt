package top.alan.locationdemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch

@Composable
fun LocationApp(
    gpsEnabled: State<Boolean>,
    onGpsStateChanged: ((Boolean) -> Unit),
    networkEnabled: State<Boolean>,
    onNetworkStateChanged: ((Boolean) -> Unit),
    locationText: State<String>
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var satelliteCount by remember { mutableIntStateOf(0) }
    var satelliteFound by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = gpsEnabled.value, onCheckedChange = {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    coroutineScope.launch { onGpsStateChanged(it) }
                }
            })
            Text("GPS")
            Checkbox(
                checked = networkEnabled.value,
                onCheckedChange = {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            context,
                            "Location permission not granted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        coroutineScope.launch { onNetworkStateChanged(it) }
                    }
                })
            Text("Network")
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(text = locationText.value, modifier = Modifier.padding(bottom = 8.dp))
        }
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
}
