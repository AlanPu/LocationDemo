package top.alan.locationdemo

import android.annotation.SuppressLint
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.LinkedList

class LocationViewModel : ViewModel() {
    lateinit var locationManager: LocationManager
    lateinit var gpsLocationListener: LocationListener
    lateinit var networkLocationListener: LocationListener
    private val _gpsEnabled = mutableStateOf(false)
    private val _networkEnabled = mutableStateOf(false)
    private val _locations = LinkedList<String>()
    private val _locationText = mutableStateOf("")
    val gpsEnabled: State<Boolean> = _gpsEnabled
    val networkEnabled: State<Boolean> = _networkEnabled
    val locationText: State<String> = _locationText

    @SuppressLint("MissingPermission")
    fun onGpsStateChanged(checked: Boolean) {
        _gpsEnabled.value = checked

        if (_gpsEnabled.value) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3000,
                1f,
                gpsLocationListener
            )
        } else {
            locationManager.removeUpdates(gpsLocationListener)
        }
    }

    @SuppressLint("MissingPermission")
    fun onNetworkStateChanged(checked: Boolean) {
        _networkEnabled.value = checked

        if (_networkEnabled.value) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                3000,
                1f,
                networkLocationListener
            )
        } else {
            locationManager.removeUpdates(networkLocationListener)
        }
    }

    fun onLocationChanged(locationText: String) {
        _locations.addFirst(locationText)
        _locationText.value = _locations.joinToString("\n\n")
    }
}