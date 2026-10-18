package za.co.maryhairsalon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import za.co.maryhairsalon.ui.auth.AuthViewModel
import za.co.maryhairsalon.ui.auth.ForgotPasswordScreen
import za.co.maryhairsalon.ui.auth.LoginScreen
import za.co.maryhairsalon.ui.auth.SignUpScreen
import za.co.maryhairsalon.ui.booking.BookingFormScreen
import za.co.maryhairsalon.ui.booking.BookingFormViewModel
import za.co.maryhairsalon.ui.components.BrandMark
import za.co.maryhairsalon.ui.customer.CategoryScreen
import za.co.maryhairsalon.ui.customer.CategoryViewModel
import za.co.maryhairsalon.ui.profile.HelpScreen
import za.co.maryhairsalon.ui.theme.Brand

/** Navigation routes. */
private object Routes {
    const val LOADING = "loading"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT = "forgot"
    const val HOME = "home"
    const val CATEGORY = "category/{categoryId}"
    const val BOOK = "book?serviceId={serviceId}&bookingId={bookingId}"
    const val HELP = "help"

    fun category(id: String) = "category/$id"
    fun book(serviceId: String? = null, bookingId: String? = null) =
        "book?serviceId=${serviceId.orEmpty()}&bookingId=${bookingId.orEmpty()}"

    val authRoutes = setOf(LOADING, LOGIN, SIGN_UP, FORGOT)
}

/**
 * The whole app. The session decides where you are (spec FR-02): signed out -> login,
 * signed in -> the home screen for your role. Logging in or out anywhere is enough;
 * this function moves you to the right place.
 */
@Composable
fun AppRoot() {
    val root: RootViewModel = appViewModel { RootViewModel(it.authRepository) }
    val session by root.session.collectAsStateWithLifecycle()
    val nav = rememberNavController()

    LaunchedEffect(session) {
        val current = nav.currentDestination?.route
        when (session) {
            SessionState.SignedOut -> if (current !in setOf(Routes.LOGIN, Routes.SIGN_UP, Routes.FORGOT)) nav.resetTo(Routes.LOGIN)
            is SessionState.SignedIn -> if (current == null || current in Routes.authRoutes) nav.resetTo(Routes.HOME)
            // Logged in but the profile (role) hasn't loaded yet: show the loader, with a way out.
            SessionState.ProfileLoading -> if (current in setOf(Routes.LOGIN, Routes.SIGN_UP, Routes.FORGOT)) nav.resetTo(Routes.LOADING)
            SessionState.Loading -> Unit
        }
    }

    NavHost(nav, startDestination = Routes.LOADING, modifier = Modifier.fillMaxSize().background(Brand.Bg)) {
        composable(Routes.LOADING) {
            Splash(
                text = if (session == SessionState.ProfileLoading) "Setting up your account…" else null,
                onSignOut = if (session == SessionState.ProfileLoading) root::signOut else null,
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                appViewModel { AuthViewModel(it.authRepository) },
                onSignUp = { nav.navigate(Routes.SIGN_UP) },
                onForgot = { nav.navigate(Routes.FORGOT) },
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(appViewModel { AuthViewModel(it.authRepository) }, onLogin = { nav.popBackStack() })
        }
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(appViewModel { AuthViewModel(it.authRepository) }, onBack = { nav.popBackStack() })
        }
        composable(Routes.HOME) {
            when (val s = session) {
                is SessionState.SignedIn -> RoleHome(
                    user = s.user,
                    onCategory = { nav.navigate(Routes.category(it)) },
                    onBook = { nav.navigate(Routes.book(serviceId = it)) },
                    onReschedule = { nav.navigate(Routes.book(bookingId = it.id)) },
                    onHelp = { nav.navigate(Routes.HELP) },
                )
                else -> Splash()
            }
        }
        composable(Routes.CATEGORY, arguments = listOf(navArgument("categoryId") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("categoryId").orEmpty()
            CategoryScreen(
                appViewModel(key = "category-$id") { CategoryViewModel(it.catalogRepository, id) },
                onBack = { nav.popBackStack() },
                onBook = { nav.navigate(Routes.book(serviceId = it)) },
            )
        }
        composable(
            Routes.BOOK,
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType; defaultValue = "" },
                navArgument("bookingId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { entry ->
            val serviceId = entry.arguments?.getString("serviceId").orEmpty().ifBlank { null }
            val bookingId = entry.arguments?.getString("bookingId").orEmpty().ifBlank { null }
            BookingFormScreen(
                appViewModel(key = "book-$serviceId-$bookingId") { BookingFormViewModel(it, serviceId, bookingId) },
                onDone = { nav.popBackStack(Routes.HOME, inclusive = false) },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.HELP) { HelpScreen(onBack = { nav.popBackStack() }) }
    }
}

/** Clears the back stack and goes to [route] (used for login/logout so Back can't undo them). */
private fun NavHostController.resetTo(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun Splash(text: String? = null, onSignOut: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().background(Brand.Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            BrandMark(large = true)
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Brand.Orange)
            if (text != null) {
                Spacer(Modifier.height(12.dp))
                Text(text, color = Brand.TextDim)
            }
            if (onSignOut != null) {
                TextButton(onClick = onSignOut) { Text("Log out", color = Brand.OrangeLight) }
            }
        }
    }
}
