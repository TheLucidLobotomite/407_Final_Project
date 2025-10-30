package com.cs407.brickcollector

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.cs407.brickcollector.ui.screens.BuyScreen
import com.cs407.brickcollector.ui.screens.MySetsScreen
import com.cs407.brickcollector.ui.screens.SellScreen
import com.cs407.brickcollector.ui.screens.SettingsScreen
import com.cs407.brickcollector.ui.screens.WantListScreen


import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.cs407.brickcollector.api.BrickEconomyAPI
import kotlinx.coroutines.launch

import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.cs407.brickcollector.api.LegoDatabase
import com.cs407.brickcollector.api.exampleUsage
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if database file exists in assets
        try {
            val assetFiles = assets.list("")
            Log.d("LegoTest", "Files in assets: ${assetFiles?.joinToString()}")

            val hasDb = assetFiles?.contains("lego_sets.db") ?: false
            Log.d("LegoTest", "Database in assets: $hasDb")
        } catch (e: Exception) {
            Log.e("LegoTest", "Error checking assets: ${e.message}")
        }

        // Now try to access database
        val legoDb = LegoDatabase.getInstance(this)
        val set = legoDb.getSetByUPC("673419266192")
        Log.d("LegoTest", "Found: ${set?.name ?: "Not found"}")

        setContent {
            AppNavigation()
        }
    }
}

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Call the example usage function
        lifecycleScope.launch {
            exampleUsage()
        }

        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Testing BrickEconomy API\nCheck Logcat for results",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

     */

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
                            // TODO... handle the barcode
                        }) {
                            Icon(Icons.Default.Star, contentDescription = "Scan Barcode")
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