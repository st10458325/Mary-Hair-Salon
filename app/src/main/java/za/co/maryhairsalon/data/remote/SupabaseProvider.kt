package za.co.maryhairsalon.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import za.co.maryhairsalon.BuildConfig

object SupabaseProvider {

    /** False when SUPABASE_URL / SUPABASE_ANON_KEY were not set in local.properties. */
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    /**
     * One shared client for the whole app. Uses the public anon key: everything it can
     * do is limited by the Row Level Security policies in 002_security.sql.
     */
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    fun create(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true   // null from the database -> the field's default value
            explicitNulls = false
            encodeDefaults = true      // always send every column on insert/upsert
        })
        install(Auth)       // login, sign-up, Google SSO; session is saved on the device
        install(Postgrest)  // tables, views and API functions
    }
}

private const val TAG = "SupabaseSync"

/**
 * Offline-first read pattern used by every repository:
 *
 *  1. The UI collects a Room Flow, so it shows cached data instantly (even offline).
 *  2. At the same time [refresh] fetches the latest rows from Supabase and writes them
 *     into Room, which makes the Room Flow emit again with fresh data.
 *
 * If the refresh fails (offline, permission denied) the error is logged and the cached
 * data keeps showing instead of the screen crashing.
 */
fun <T> offlineFirst(name: String, refresh: suspend () -> Unit, fromRoom: Flow<T>): Flow<T> = channelFlow {
    launch {
        try {
            refresh()
            Log.d(TAG, "Refreshed '$name' from Supabase")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Refresh '$name' failed; showing cached data", e)
        }
    }
    fromRoom.collect { send(it) }
}
