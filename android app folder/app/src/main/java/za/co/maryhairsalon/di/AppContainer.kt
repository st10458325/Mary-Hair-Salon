package za.co.maryhairsalon.di

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import za.co.maryhairsalon.data.local.AppDatabase
import za.co.maryhairsalon.data.remote.SupabaseProvider
import za.co.maryhairsalon.data.repository.AuthRepository
import za.co.maryhairsalon.data.repository.BookingRepository
import za.co.maryhairsalon.data.repository.CatalogRepository
import za.co.maryhairsalon.data.repository.UserRepository

/**
 * Simple manual dependency injection (no Hilt, to keep the build small).
 * One instance lives in [za.co.maryhairsalon.MaryHairSalonApp].
 *
 * In a ViewModel factory:
 *   val container = (application as MaryHairSalonApp).container
 *   BookingsViewModel(container.bookingRepository, container.authRepository)
 *
 * Everything is lazy, so nothing touches Supabase until a screen needs it.
 */
class AppContainer(context: Context) {
    val supabase: SupabaseClient by lazy { SupabaseProvider.create() }
    val database: AppDatabase by lazy { AppDatabase.get(context) }

    val authRepository by lazy { AuthRepository(supabase, database) }
    val catalogRepository by lazy { CatalogRepository(supabase, database) }
    val userRepository by lazy { UserRepository(supabase, database) }
    val bookingRepository by lazy { BookingRepository(supabase, database) { supabase.auth.currentUserOrNull()?.id } }
}
