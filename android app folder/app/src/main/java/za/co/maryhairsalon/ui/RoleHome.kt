package za.co.maryhairsalon.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.ui.components.BrandMark
import za.co.maryhairsalon.ui.customer.MyBookingsTab
import za.co.maryhairsalon.ui.customer.MyBookingsViewModel
import za.co.maryhairsalon.ui.customer.ServicesTab
import za.co.maryhairsalon.ui.customer.ServicesViewModel
import za.co.maryhairsalon.ui.employee.ScheduleTab
import za.co.maryhairsalon.ui.employee.ScheduleViewModel
import za.co.maryhairsalon.ui.manager.CatalogTab
import za.co.maryhairsalon.ui.manager.CatalogViewModel
import za.co.maryhairsalon.ui.manager.ManagerBookingsTab
import za.co.maryhairsalon.ui.manager.ManagerBookingsViewModel
import za.co.maryhairsalon.ui.manager.StaffTab
import za.co.maryhairsalon.ui.manager.StaffViewModel
import za.co.maryhairsalon.ui.profile.SettingsTab
import za.co.maryhairsalon.ui.profile.SettingsViewModel
import za.co.maryhairsalon.ui.theme.Brand

private enum class Tab(val label: String, val icon: ImageVector) {
    SERVICES("Services", Icons.Default.Home),
    BOOKINGS("Bookings", Icons.Default.DateRange),
    SCHEDULE("Schedule", Icons.Default.DateRange),
    ALL_BOOKINGS("Bookings", Icons.AutoMirrored.Filled.List),
    STAFF("Staff", Icons.Default.Face),
    CATALOG("Catalogue", Icons.Default.ShoppingCart),
    PROFILE("Profile", Icons.Default.Person),
}

/** Each role gets its own set of tabs (spec 4.1: no shared home screen across roles). */
private fun tabsFor(role: Role): List<Tab> = when (role) {
    Role.CUSTOMER -> listOf(Tab.SERVICES, Tab.BOOKINGS, Tab.PROFILE)
    Role.EMPLOYEE -> listOf(Tab.SCHEDULE, Tab.PROFILE)
    Role.MANAGER -> listOf(Tab.ALL_BOOKINGS, Tab.STAFF, Tab.CATALOG, Tab.PROFILE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleHome(
    user: User,
    onCategory: (String) -> Unit,
    onBook: (serviceId: String) -> Unit,
    onReschedule: (Booking) -> Unit,
    onHelp: () -> Unit,
) {
    val tabs = tabsFor(user.role)
    var selected by rememberSaveable(user.role) { mutableIntStateOf(0) }
    val tab = tabs[selected.coerceIn(0, tabs.lastIndex)]
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Brand.Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { BrandMark() },
                actions = { IconButton(onClick = onHelp) { Icon(Icons.Default.Info, contentDescription = "Help") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Brand.Bg),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Brand.BgElevated) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = i == selected,
                        onClick = { selected = i },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Brand.OrangeLight,
                            indicatorColor = Brand.Orange,
                            unselectedIconColor = Brand.TextFaint,
                            unselectedTextColor = Brand.TextFaint,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.SERVICES -> ServicesTab(
                    appViewModel { ServicesViewModel(it.catalogRepository) },
                    snackbar, onCategory, onBook,
                )
                Tab.BOOKINGS -> MyBookingsTab(
                    appViewModel(key = "my-${user.id}") { MyBookingsViewModel(it.bookingRepository, user.id) },
                    snackbar, onReschedule,
                    onBrowse = { selected = tabs.indexOf(Tab.SERVICES) },
                )
                Tab.SCHEDULE -> ScheduleTab(
                    appViewModel(key = "schedule-${user.id}") { ScheduleViewModel(it.bookingRepository, user.id) },
                    snackbar,
                )
                Tab.ALL_BOOKINGS -> ManagerBookingsTab(
                    appViewModel { ManagerBookingsViewModel(it.bookingRepository, it.userRepository) },
                    snackbar,
                )
                Tab.STAFF -> StaffTab(appViewModel { StaffViewModel(it.userRepository) }, snackbar)
                Tab.CATALOG -> CatalogTab(appViewModel { CatalogViewModel(it.catalogRepository) }, snackbar)
                Tab.PROFILE -> SettingsTab(appViewModel { SettingsViewModel(it.authRepository) }, snackbar, onHelp)
            }
        }
    }
}
