package top.alan.locationdemo

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.LinkedList

class LocationViewModel : ViewModel() {
    lateinit var locationManager: LocationManager
    lateinit var gpsLocationListener: LocationListener
    lateinit var networkLocationListener: LocationListener
    lateinit var gnssCallback: GnssStatus.Callback
    private val _gpsEnabled = mutableStateOf(false)
    private val _networkEnabled = mutableStateOf(false)
    private val _locations = LinkedList<String>()
    private val _locationText = mutableStateOf("")
    private val _satelliteFound = mutableIntStateOf(0)
    private val _satelliteCount = mutableIntStateOf(0)
    private val _permissionGranted = mutableStateOf(false)
    val gpsEnabled: State<Boolean> = _gpsEnabled
    val networkEnabled: State<Boolean> = _networkEnabled
    val locationText: State<String> = _locationText
    val satelliteFound: State<Int> = _satelliteFound
    val satelliteCount: State<Int> = _satelliteCount
    val permissionGranted: State<Boolean> = _permissionGranted

    fun shareData() {
    }
    fun grantPermission(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun removeListeners() {
        if (!_permissionGranted.value) {
            locationManager.removeUpdates(gpsLocationListener)
            locationManager.removeUpdates(networkLocationListener)
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
    }

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

    fun onSatelliteStateChanged(satelliteFound: Int, satelliteCount: Int) {
        _satelliteFound.intValue = satelliteFound
        _satelliteCount.intValue = satelliteCount
    }

    fun onLocationChanged(locationText: String) {
        _locations.addFirst(locationText)
        _locationText.value = _locations.joinToString("\n")
    }
}