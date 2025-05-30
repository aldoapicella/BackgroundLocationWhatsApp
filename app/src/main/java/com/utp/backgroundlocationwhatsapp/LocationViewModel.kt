package com.utp.backgroundlocationwhatsapp

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location // Importar android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri // Importar para la extensión .toUri()
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    var latitude by mutableDoubleStateOf(0.0)
        private set
    var longitude by mutableDoubleStateOf(0.0)
        private set

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())

    fun fetchLocation() {
        val app = getApplication<Application>()
        if (ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Note: Proper permission handling should be done in the UI layer,
            // requesting permissions if not granted.
            Toast.makeText(app, "Location permission not granted. Please grant it in app settings.", Toast.LENGTH_LONG).show()
            latitude = 0.0
            longitude = 0.0
            return
        }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? -> // Tipo explícito Location?
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    } else {
                        Toast.makeText(app, "Failed to get location. Ensure location services are enabled.", Toast.LENGTH_LONG).show()
                        latitude = 0.0
                        longitude = 0.0
                    }
                }
                .addOnFailureListener { e: Exception -> // Tipo explícito Exception
                    Toast.makeText(app, "Error getting location: ${e.message}", Toast.LENGTH_LONG).show()
                    latitude = 0.0
                    longitude = 0.0
                }
        } catch (e: SecurityException) {
             Toast.makeText(app, "Location permission error: ${e.message}", Toast.LENGTH_LONG).show()
            latitude = 0.0
            longitude = 0.0
        }
    }

    fun sendViaWhatsApp(context: Context, phone: String, customMessage: String) {
        val fullMessage = if (latitude != 0.0 || longitude != 0.0) {
            "$customMessage\n\nMy current location: https://maps.google.com/?q=$latitude,$longitude"
        } else {
            "$customMessage\n\nLocation not available."
        }

        try {
            val whatsappUri = "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(fullMessage)}".toUri() // Usar .toUri()
            val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
            // Check if WhatsApp is installed using packageManager and <queries> in Manifest
            if (context.packageManager.getLaunchIntentForPackage("com.whatsapp") != null) {
                 intent.setPackage("com.whatsapp")
                 context.startActivity(intent)
            } else {
                Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
                // Fallback: Try a generic share intent for messaging apps
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, fullMessage)
                    // Optionally, you can try to target SMS apps if no other handler is found for ACTION_SEND
                    // putExtra("address", phone.replace("+","")) // For SMS, might need number only
                }
                if (genericIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(genericIntent, "Send message via:"))
                } else {
                    Toast.makeText(context, "No messaging app found.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error sending message: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
