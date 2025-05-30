package com.utp.backgroundlocationwhatsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider                          // ← importa el proveedor correcto
import androidx.lifecycle.viewmodel.compose.viewModel
import com.utp.backgroundlocationwhatsapp.ui.theme.BackgroundLocationWhatsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackgroundLocationWhatsAppTheme {
                // Usa ViewModelProvider.AndroidViewModelFactory
                val vm: LocationViewModel = viewModel(
                    factory = ViewModelProvider
                        .AndroidViewModelFactory
                        .getInstance(application)
                )
                vm.fetchLocation()
                SendLocationScreen(vm)
            }
        }
    }
}
