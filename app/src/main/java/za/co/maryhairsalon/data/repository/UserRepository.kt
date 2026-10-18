package za.co.maryhairsalon.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import za.co.maryhairsalon.data.local.AppDatabase
import za.co.maryhairsalon.data.local.toDomain
import za.co.maryhairsalon.data.local.toEntity
import za.co.maryhairsalon.data.model.Role
import za.co.maryhairsalon.data.model.User
import za.co.maryhairsalon.data.remote.Db
import za.co.maryhairsalon.data.remote.ProfileDto
import za.co.maryhairsalon.data.remote.offlineFirst

/** Stylist directory (spec FR-11) and manager staff management (spec FR-09). */
class UserRepository(
    private val supabase: SupabaseClient,
    private val db: AppDatabase,
) {
    /** Stylists = profiles with role "employee". Used by the booking form and the About screen. */
    fun observeStylists(): Flow<List<User>> = offlineFirst(
        name = "stylists",
        refresh = ::refreshStylists,
        fromRoom = db.userDao().observeByRole(Role.EMPLOYEE.dbValue).map { list -> list.map { it.toDomain() } },
    )

    /** Manager only: every account, for the Staff tab. Other roles only get rows RLS allows. */
    fun observeAllUsers(): Flow<List<User>> = offlineFirst(
        name = "allUsers",
        refresh = ::refreshAllUsers,
        fromRoom = db.userDao().observeAll().map { list -> list.map { it.toDomain() } },
    )

    private suspend fun refreshStylists() {
        val rows = supabase.from(Db.PROFILES)
            .select { filter { eq("role", Role.EMPLOYEE.dbValue) } }
            .decodeList<ProfileDto>()
        db.userDao().replaceByRole(Role.EMPLOYEE.dbValue, rows.map { it.toDomain().toEntity() })
    }

    private suspend fun refreshAllUsers() {
        val rows = supabase.from(Db.PROFILES).select().decodeList<ProfileDto>()
        db.userDao().replaceAll(rows.map { it.toDomain().toEntity() })
    }

    /**
     * Manager only: promote a customer to employee (with a job title) or revert an employee
     * to customer. Runs the set_user_role() database function, which re-checks that the
     * caller really is a manager.
     */
    suspend fun setRole(uid: String, newRole: Role, title: String = ""): Result<Unit> = safeCall<Unit>(TAG, "setRole") {
        require(newRole != Role.MANAGER) { "Manager accounts can only be created in the Supabase dashboard." }
        supabase.postgrest.rpc(Db.RPC_SET_USER_ROLE, buildJsonObject {
            put("p_user_id", uid)
            put("p_role", newRole.dbValue)
            put("p_title", title.trim())
        })
        refreshAllUsers()
        Log.i(TAG, "Set role of $uid to ${newRole.dbValue}")
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
