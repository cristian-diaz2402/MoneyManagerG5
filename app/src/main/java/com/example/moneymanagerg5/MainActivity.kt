package com.example.moneymanagerg5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moneymanagerg5.ui.DashboardScreen
import com.example.moneymanagerg5.ui.NotificationsScreen
import com.example.moneymanagerg5.ui.ProfileScreen
import com.example.moneymanagerg5.ui.home.HomeScreen
import com.example.moneymanagerg5.ui.MoneyManagerG5Theme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home)
    object Dashboard : Screen("dashboard", "Estadísticas", Icons.Filled.Star)
    object Notifications : Screen("notifications", "Ajustes", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyManagerG5Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            if (currentRoute == "profile") {
                TopAppBar(
                    title = { Text("Perfil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar"
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            } else {
                TopAppBar(
                    title = { Text("Money Manager G5") },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    actions = {
                        IconButton(onClick = { 
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Perfil de usuario"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute != "profile") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Notifications.route) { NotificationsScreen() }
                composable("profile") { ProfileScreen() }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Notifications
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}