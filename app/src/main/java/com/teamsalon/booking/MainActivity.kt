package com.teamsalon.booking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teamsalon.booking.data.AppDatabase
import com.teamsalon.booking.data.BookingRepository
import com.teamsalon.booking.data.DatabaseSeeder
import com.teamsalon.booking.ui.booking.BookingScreen
import com.teamsalon.booking.ui.help.HelpScreen
import com.teamsalon.booking.ui.login.LoginScreen
import com.teamsalon.booking.ui.mybookings.MyBookingsScreen
import com.teamsalon.booking.ui.services.ServicesScreen

/**
 * Single-Activity host. Seeds the database once on first launch (see
 * DatabaseSeeder), then hands off to a simple NavHost: login, services,
 * booking, my bookings, and a help screen (the in-app "user support"
 * deliverable). Each screen is owned by whichever teammate's section is
 * noted on that screen's own file — this file is the shared glue all
 * three of you will touch, so coordinate before editing it directly.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = BookingRepository(db)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var loggedInUserId by remember { mutableStateOf<Int?>(null) }

                LaunchedEffect(Unit) {
                    DatabaseSeeder.seedIfEmpty(db)
                }

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            repository = repository,
                            onLoggedIn = { userId ->
                                loggedInUserId = userId
                                navController.navigate("services") { popUpTo("login") { inclusive = true } }
                            }
                        )
                    }
                    composable("services") {
                        ServicesScreen(
                            repository = repository,
                            onBookService = { serviceId -> navController.navigate("booking/$serviceId") },
                            onViewBookings = { navController.navigate("myBookings") },
                            onHelp = { navController.navigate("help") }
                        )
                    }
                    composable(
                        route = "booking/{serviceId}",
                        arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: return@composable
                        val userId = loggedInUserId ?: return@composable
                        BookingScreen(
                            repository = repository,
                            userId = userId,
                            serviceId = serviceId,
                            onDone = { navController.popBackStack("services", inclusive = false) }
                        )
                    }
                    composable("myBookings") {
                        val userId = loggedInUserId ?: return@composable
                        MyBookingsScreen(repository = repository, userId = userId)
                    }
                    composable("help") {
                        HelpScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
