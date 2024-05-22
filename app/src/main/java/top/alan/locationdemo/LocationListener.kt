package top.alan.locationdemo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@Composable
fun gpsLocationListener(
    launchTime: LocalTime,
    satelliteCount: State<Int>,
    satelliteFound: State<Int>,
    onLocationChanged: ((String) -> Unit)
): LocationListener {
    val context = LocalContext.current
    val geoCoder = Geocoder(context)
    var addressInfo by remember { mutableStateOf("") }
    var timeLabel by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    val launchTime = LocalTime.now()

    return remember {
        LocationListener { location ->
            val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 5)

            addressInfo = getAddressInfo(addresses)

            timeLabel = getTime(launchTime)
            locationText = "Time: $timeLabel\n" +
                    "Source: GPS\n" +
                    "Satellite Found: ${satelliteFound.value}\n" +
                    "Connected Satellites: ${satelliteCount.value}\n" +
                    "Latitude: ${location.latitude}\n" +
                    "Longitude: ${location.longitude}\n" +
                    "Accuracy: ${location.accuracy}m\n\n$addressInfo\n" +
                    "====================================\n"

            onLocationChanged(locationText)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun networkLocationListener(
    launchTime: LocalTime,
    onLocationChanged: ((String) -> Unit)
): LocationListener {
    val context = LocalContext.current
    val geoCoder = Geocoder(context)
    var addressInfo by remember { mutableStateOf("") }
    var timeLabel by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }

    return remember {
        LocationListener { location ->
            val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 5)

            addressInfo = getAddressInfo(addresses)

            timeLabel = getTime(launchTime)
            locationText = "Time: $timeLabel\n" +
                    "Source: Network\n" +
                    "Latitude: ${location.latitude}\n" +
                    "Longitude: ${location.longitude}\n" +
                    "Accuracy: ${location.accuracy}m\n\n$addressInfo\n" +
                    "====================================\n"

            onLocationChanged(locationText)
        }
    }
}

@SuppressLint("MissingPermission")
fun gnssCallback(locationManager: LocationManager, onSatelliteStateChanged: ((Int, Int) -> Unit)): GnssStatus.Callback {
    val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val found = status.satelliteCount
            val count = (0 until status.satelliteCount).count { status.usedInFix(it) }
            onSatelliteStateChanged(found, count)
        }
    }
    locationManager.registerGnssStatusCallback(gnssCallback)
    return gnssCallback
}

private fun getAddressInfo(
    addresses: MutableList<Address>?
): String {
    var addressInfo = ""
    if (addresses!!.isNotEmpty()) {
        addresses.forEachIndexed { i, address ->
            addressInfo += "Address${i + 1}: ${address.getAddressLine(0)}\n" +
                    "SubThoroughfare: ${address.subThoroughfare}\n" +
                    "SubAdminArea: ${address.subAdminArea}\n" +
                    "SubLocality: ${address.subLocality}\n" +
                    "City: ${address.locality}\n" +
                    "State: ${address.adminArea}\n" +
                    "Country: ${address.countryName}\n" +
                    "Postal Code: ${address.postalCode}\n"
        }
    } else {
        addressInfo = "No address found for the location."
    }
    return addressInfo
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