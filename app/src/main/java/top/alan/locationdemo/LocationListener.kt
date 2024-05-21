package top.alan.locationdemo

import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun locationListener(onLocationChanged: ((String) -> Unit)): LocationListener {
    val context = LocalContext.current
    val geoCoder = Geocoder(context)
    var addressInfo by remember { mutableStateOf("") }
    var timeLabel by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var satelliteCount by remember { mutableIntStateOf(0) }
    var satelliteFound by remember { mutableIntStateOf(0) }
    val launchTime = LocalTime.now()

    return remember {
        LocationListener { location ->
            val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 5)
            addressInfo = ""
            if (addresses!!.isNotEmpty()) {
                addresses.forEachIndexed { i, address ->
                    addressInfo += "Address${i + 1}: ${address.getAddressLine(0)}\n" +
                            "SubThoroughfare: ${address.subThoroughfare}\n" +
                            "SubAdminArea: ${address.subAdminArea}\n" +
                            "SubLocality: ${address.subLocality}\n" +
                            "City: ${address.locality}\n" +
                            "State: ${address.adminArea}\n" +
                            "Country: ${address.countryName}\n" +
                            "Postal Code: ${address.postalCode}\n\n"
                }
            } else {
                addressInfo = "No address found for the location."
            }

            val source = when (location.provider) {
                LocationManager.NETWORK_PROVIDER -> "Network"
                LocationManager.GPS_PROVIDER -> "GPS"
                else -> "N/A"
            }

            timeLabel = getTime(launchTime)
            locationText = "Time: $timeLabel\n" +
                    "Source: $source\n" +
                    "Satellite Found: $satelliteFound\n" +
                    "Connected Satellites: $satelliteCount\n" +
                    "Latitude: ${location.latitude}\n" +
                    "Longitude: ${location.longitude}\n" +
                    "Accuracy: ${location.accuracy}m\n\n$addressInfo\n" +
                    "====================================\n\n"

            onLocationChanged(locationText)
        }
    }
}

private fun getTime(launchTime: LocalTime): String {
    val currentTime = LocalTime.now()
    val duration = Duration.between(launchTime, currentTime)
    val hoursDifference = duration.toHours()
    val minutesDifference = duration.toMinutes() % 60
    val secondsDifference = duration.seconds % 60
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val timeDifference =
        String.format("%02d:%02d:%02d", hoursDifference, minutesDifference, secondsDifference)
    return "${currentTime.format(formatter)} (Elapsed: $timeDifference)"
}