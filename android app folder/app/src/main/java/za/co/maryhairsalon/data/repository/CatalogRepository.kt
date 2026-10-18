package za.co.maryhairsalon.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import za.co.maryhairsalon.data.local.AppDatabase
import za.co.maryhairsalon.data.local.toDomain
import za.co.maryhairsalon.data.local.toEntity
import za.co.maryhairsalon.data.model.Category
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.model.Slug
import za.co.maryhairsalon.data.remote.CategoryDto
import za.co.maryhairsalon.data.remote.Db
import za.co.maryhairsalon.data.remote.ServiceDto
import za.co.maryhairsalon.data.remote.offlineFirst

/** A category plus how many services it has (count is computed, not stored). */
data class CategoryWithCount(val category: Category, val serviceCount: Int)

/**
 * Service catalogue: categories and services.
 * Anyone can read; only managers can create/edit/delete (spec FR-03, enforced by the
 * Row Level Security policies in 002_security.sql).
 */
class CatalogRepository(
    private val supabase: SupabaseClient,
    private val db: AppDatabase,
) {
    // ---------------- Reads (offline-first) ----------------

    fun observeCategories(): Flow<List<Category>> =
        offlineFirst("categories", ::refreshCategories, db.categoryDao().observeAll().map { list -> list.map { it.toDomain() } })

    fun observeServices(): Flow<List<Service>> =
        offlineFirst("services", ::refreshServices, db.serviceDao().observeAll().map { list -> list.map { it.toDomain() } })

    /** Category grid with live service counts. */
    fun observeCategoriesWithCounts(): Flow<List<CategoryWithCount>> =
        combine(observeCategories(), observeServices()) { cats, svcs ->
            val counts = svcs.groupingBy { it.categoryId }.eachCount()
            cats.map { CategoryWithCount(it, counts[it.id] ?: 0) }
        }

    fun observeServicesInCategory(categoryId: String): Flow<List<Service>> =
        offlineFirst("services", ::refreshServices, db.serviceDao().observeByCategory(categoryId).map { list -> list.map { it.toDomain() } })

    /** Spec FR-04: keyword search over title, category and tags. Runs against the local cache. */
    fun search(query: String): Flow<List<Service>> {
        val q = query.trim()
        return if (q.isEmpty()) observeServices()
        else offlineFirst("services", ::refreshServices, db.serviceDao().search(q).map { list -> list.map { it.toDomain() } })
    }

    suspend fun getService(id: String): Service? = db.serviceDao().get(id)?.toDomain()

    /** Pull-to-refresh. */
    suspend fun refresh(): Result<Unit> = safeCall<Unit>(TAG, "refreshCatalog") {
        refreshCategories()
        refreshServices()
    }

    private suspend fun refreshCategories() {
        val rows = supabase.from(Db.CATEGORIES).select().decodeList<CategoryDto>()
        db.categoryDao().replaceAll(rows.map { it.toDomain().toEntity() })
    }

    private suspend fun refreshServices() {
        val rows = supabase.from(Db.SERVICES).select().decodeList<ServiceDto>()
        db.serviceDao().replaceAll(rows.map { it.toDomain().toEntity() })
    }

    // ---------------- Manager CRUD ----------------

    /** Creates or updates a category. id is a slug such as "braids" (lowercase letters, digits, dashes). */
    suspend fun saveCategory(category: Category): Result<Unit> = safeCall<Unit>(TAG, "saveCategory") {
        require(category.label.isNotBlank()) { "Category name can't be empty." }
        val id = category.id.ifBlank { Slug.from(category.label) }
        require(Slug.isValid(id)) { "Category id may only contain lowercase letters, numbers and dashes." }
        supabase.from(Db.CATEGORIES).upsert(CategoryDto.from(category.copy(id = id)))
        refreshCategories()
        Log.i(TAG, "Saved category $id")
    }

    /** The database refuses to delete a category that still has services (foreign key). */
    suspend fun deleteCategory(id: String): Result<Unit> = safeCall<Unit>(TAG, "deleteCategory") {
        if (db.serviceDao().countInCategory(id) > 0) {
            throw UserFacingException("Move or delete this category's services first.")
        }
        supabase.from(Db.CATEGORIES).delete { filter { eq("id", id) } }
        refreshCategories()
        Log.i(TAG, "Deleted category $id")
    }

    suspend fun saveService(service: Service): Result<Unit> = safeCall<Unit>(TAG, "saveService") {
        require(service.title.isNotBlank()) { "Service name can't be empty." }
        require(service.categoryId.isNotBlank()) { "Please choose a category." }
        require(service.price > 0) { "Price must be more than R0." }
        require(service.discount in 0.0..100.0) { "Discount must be between 0% and 100%." }
        val id = service.id.ifBlank { Slug.from(service.title) }
        require(Slug.isValid(id)) { "Service id may only contain lowercase letters, numbers and dashes." }
        supabase.from(Db.SERVICES).upsert(ServiceDto.from(service.copy(id = id)))
        refreshServices()
        Log.i(TAG, "Saved service $id")
    }

    /**
     * Services that have bookings can't be deleted (the booking history needs them), so
     * this hides the service instead (is_active = false). Unused services are deleted.
     */
    suspend fun deleteService(id: String): Result<Unit> = safeCall<Unit>(TAG, "deleteService") {
        try {
            supabase.from(Db.SERVICES).delete { filter { eq("id", id) } }
        } catch (e: io.github.jan.supabase.postgrest.exception.PostgrestRestException) {
            if (e.code != "23503") throw e
            supabase.from(Db.SERVICES).update({ set("is_active", false) }) { filter { eq("id", id) } }
            Log.i(TAG, "Service $id has bookings; hid it instead of deleting")
        }
        refreshServices()
    }

    companion object {
        private const val TAG = "CatalogRepository"
    }
}
