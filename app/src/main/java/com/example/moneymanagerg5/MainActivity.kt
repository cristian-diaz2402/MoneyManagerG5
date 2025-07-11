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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagerg5.ui.LoginScreen
import com.example.moneymanagerg5.GastoService

sealed class Screen(val route: String, val label: String, val icon: Any) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home)
    object Dashboard : Screen("dashboard", "Estadísticas", R.drawable.bar_chart_4_bars_24px)
    object Notifications : Screen("notifications", "Ajustes", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar GastoService
        GastoService.initialize(this)
        
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
            } else if (currentRoute != "login") {
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
            if (currentRoute != "profile" && currentRoute != "login") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") { LoginScreen(navController = navController) }
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
    
    val notifications by NotificationService.notifications.collectAsState()
    val unreadCount = notifications.count { !it.leida }
    
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Box {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            when (screen) {
                                is Screen.Dashboard -> Icon(
                                    painter = painterResource(id = screen.icon as Int),
                                    contentDescription = screen.label
                                )
                                else -> Icon(
                                    imageVector = screen.icon as ImageVector,
                                    contentDescription = screen.label
                                )
                            }
                            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                        }
                        
                        // Badge de notificaciones no leídas solo para la pestaña de Ajustes/Notificaciones
                        if (screen is Screen.Notifications && unreadCount > 0) {
                            Box(
                                modifier = androidx.compose.ui.Modifier
                                    .offset(x = 12.dp, y = (-4).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color.Red),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
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