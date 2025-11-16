package com.cs407.brickcollector

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.cs407.brickcollector.ui.screens.BuyScreen
import com.cs407.brickcollector.ui.screens.MySetsScreen
import com.cs407.brickcollector.ui.screens.SellScreen
import com.cs407.brickcollector.ui.screens.SettingsScreen
import com.cs407.brickcollector.ui.screens.WantListScreen
import com.cs407.location.uiScreens.qrCameraScreen
import com.cs407.location.viewModels.callLocationVM
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: callLocationVM by viewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val granted =
                perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

            vm.updatePermission(granted)

            if (granted) {
                // We have permission -> resolve city
                lifecycleScope.launch {
                    val city = vm.resolveCityAssumingPermission(
                        appContext = applicationContext,
                        geoapifyApiKey = BuildConfig.GEOAPIFY_API_KEY
                    )
                    Log.d("CITY", "Resolved city: $city")
                    Toast.makeText(this@MainActivity, "City: $city", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 4) Initialize VM with context + API key
        vm.initialize(applicationContext, BuildConfig.GEOAPIFY_API_KEY)

        // 5) If no permission yet, ask. Otherwise go ahead and resolve.
        if (!vm.hasLocationPermission(this)) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            vm.updatePermission(true)
            lifecycleScope.launch {
                val city = vm.resolveCityAssumingPermission(
                    appContext = applicationContext,
                    geoapifyApiKey = BuildConfig.GEOAPIFY_API_KEY
                )
                Log.d("CITY", "Resolved city (already had perm): $city")
                Toast.makeText(this@MainActivity, "City: $city", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("my_sets", "My Sets", Icons.Default.Home),
        BottomNavItem("want_list", "Want List", Icons.AutoMirrored.Filled.List),
        BottomNavItem("buy", "Buy", Icons.Default.ShoppingCart),
        BottomNavItem("sell", "Sell", Icons.Default.Share)
    )

    val showBars = currentRoute != "settings"

    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("qrScanner")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan Barcode"
                            )
                        }
                    }
                )
            }
            /*
            else {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
            */
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(navController = navController, items = bottomNavItems)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "my_sets",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("my_sets") {
                MySetsScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("want_list") {
                WantListScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("buy") {
                BuyScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("sell") {
                SellScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("qrScanner") {
                qrCameraScreen { scannedValue ->
                    navController.popBackStack()
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}