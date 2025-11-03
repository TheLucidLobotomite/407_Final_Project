// MainActivity.kt  (replace onCreate with this version)
package com.cs407.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.cs407.location.uiScreens.qrCameraScreen
import com.cs407.location.viewModels.qrViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm by viewModels<com.cs407.location.viewModels.callLocationVM>()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val qrVm: qrViewModel by viewModels()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Register BEFORE the Activity is started/resumed
            // Render your camera screen and receive scan results here
            setContent {
                qrCameraScreen(viewModel = qrVm) { scannedValue ->
                    // Do whatever you need with the result
                    Toast.makeText(this, "QR: $scannedValue", Toast.LENGTH_LONG).show()
                    // e.g., navigate, save to state, etc.
                    finish()
                }
            }

//        permissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) { perms ->
//            val granted = perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//                    perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
//            vm.updatePermission(granted)
//
//            lifecycleScope.launch {
//                val city = vm.resolveCityAssumingPermission(
//                    appContext = applicationContext,
//                    geoapifyApiKey = BuildConfig.GEOAPIFY_API_KEY // <-- your key, plain
//                )
//                Toast.makeText(this@MainActivity, city, Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // 2) Init VM with key + location client immediately
//        vm.initialize(applicationContext, BuildConfig.GEOAPIFY_API_KEY )
//
//        // 3) Ask permission or go straight to work
//        if (!vm.hasLocationPermission(this)) {
//            permissionLauncher.launch(
//                arrayOf(
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        } else {
//            vm.updatePermission(true)
//            lifecycleScope.launch {
//                val city = vm.resolveCityAssumingPermission(
//                    appContext = applicationContext,
//                    geoapifyApiKey = BuildConfig.GEOAPIFY_API_KEY
//                )
//                Log.d("CITY", city)
//                Toast.makeText(this@MainActivity, city, Toast.LENGTH_SHORT).show()
//            }
//
//        }

        // Your UI (optional)

    }

}
