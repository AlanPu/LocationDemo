package top.alan.locationdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
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
            Spacer(modifier = Modifier.weight(1f))
            Button(modifier = Modifier.padding(horizontal = 15.dp),
                onClick = { shareFile(context, "${getDeviceInfo()}\n\n${locationText.value}") }) {
                Text(text = "Share")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = getDeviceInfo())
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(text = locationText.value, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

private fun getDeviceInfo(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"
}

private fun saveToFile(content: String, fileDir: File, fileName: String): File {
    val file = File(fileDir, fileName)
    FileOutputStream(file).use { stream ->
        stream.write(content.toByteArray())
    }
    return file
}

private fun shareFile(context: Context, content: String) {
    val file = saveToFile(content, context.filesDir, getFileName())
    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, fileUri)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share File"))
}

private fun getFileName(): String {
    val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
    return "${sdf.format(Date())}.txt"
}
