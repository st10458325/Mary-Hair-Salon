package za.co.maryhairsalon.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import za.co.maryhairsalon.data.local.entity.BookingEntity
import za.co.maryhairsalon.data.local.entity.CategoryEntity
import za.co.maryhairsalon.data.local.entity.ServiceEntity
import za.co.maryhairsalon.data.local.entity.UserEntity

/*
 * Each DAO exposes:
 *  - observe*()  : Flow queries the UI collects (they re-emit whenever the table changes)
 *  - replace*()  : called by the repositories after fetching from Supabase. It upserts
 *                  everything fetched and deletes cached rows that no longer exist online,
 *                  inside one transaction so the UI never sees a half-refreshed list.
 */

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun get(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role ORDER BY name")
    fun observeByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY name")
    fun observeAll(): Flow<List<UserEntity>>

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE role = :role AND id NOT IN (:keepIds)")
    suspend fun deleteByRoleExcept(role: String, keepIds: List<String>)

    @Query("DELETE FROM users WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    @Transaction
    suspend fun replaceByRole(role: String, users: List<UserEntity>) {
        deleteByRoleExcept(role, users.map { it.id })
        upsertAll(users)
    }

    @Transaction
    suspend fun replaceAll(users: List<UserEntity>) {
        deleteAllExcept(users.map { it.id })
        upsertAll(users)
    }

    @Query("DELETE FROM users")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY label")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun get(id: String): CategoryEntity?

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    @Transaction
    suspend fun replaceAll(categories: List<CategoryEntity>) {
        deleteAllExcept(categories.map { it.id })
        upsertAll(categories)
    }

    @Query("DELETE FROM categories")
    suspend fun clear()
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY title")
    fun observeAll(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE categoryId = :categoryId AND isActive = 1 ORDER BY title")
    fun observeByCategory(categoryId: String): Flow<List<ServiceEntity>>

    /** Spec FR-04: search matches title, category and tags. Tags are stored as one separated text column. */
    @Query(
        """
        SELECT * FROM services
        WHERE isActive = 1 AND (
              title LIKE '%' || :query || '%'
           OR categoryId LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%')
        ORDER BY title
        """
    )
    fun search(query: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun get(id: String): ServiceEntity?

    @Query("SELECT COUNT(*) FROM services WHERE categoryId = :categoryId")
    suspend fun countInCategory(categoryId: String): Int

    @Query("SELECT categoryId, COUNT(*) AS count FROM services WHERE isActive = 1 GROUP BY categoryId")
    fun observeCountsByCategory(): Flow<List<CategoryCount>>

    @Upsert
    suspend fun upsertAll(services: List<ServiceEntity>)

    @Query("DELETE FROM services WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    @Transaction
    suspend fun replaceAll(services: List<ServiceEntity>) {
        deleteAllExcept(services.map { it.id })
        upsertAll(services)
    }

    @Query("DELETE FROM services")
    suspend fun clear()
}

/** Spec: category "count" is computed, not stored. */
data class CategoryCount(val categoryId: String, val count: Int)

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE customerId = :uid ORDER BY date DESC, time DESC")
    fun observeForCustomer(uid: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE stylistId = :stylistId ORDER BY date ASC, time ASC")
    fun observeForStylist(stylistId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY date DESC, time DESC")
    fun observeAll(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun observe(id: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun get(id: String): BookingEntity?

    /** Live counts for the status filter chips (spec FR-06). */
    @Query("SELECT status, COUNT(*) AS count FROM bookings WHERE customerId = :uid GROUP BY status")
    fun observeStatusCountsForCustomer(uid: String): Flow<List<StatusCount>>

    @Upsert
    suspend fun upsert(booking: BookingEntity)

    @Upsert
    suspend fun upsertAll(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE customerId = :uid AND id NOT IN (:keepIds)")
    suspend fun deleteForCustomerExcept(uid: String, keepIds: List<String>)

    @Query("DELETE FROM bookings WHERE stylistId = :stylistId AND id NOT IN (:keepIds)")
    suspend fun deleteForStylistExcept(stylistId: String, keepIds: List<String>)

    @Query("DELETE FROM bookings WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    @Transaction
    suspend fun replaceForCustomer(uid: String, bookings: List<BookingEntity>) {
        deleteForCustomerExcept(uid, bookings.map { it.id })
        upsertAll(bookings)
    }

    @Transaction
    suspend fun replaceForStylist(stylistId: String, bookings: List<BookingEntity>) {
        deleteForStylistExcept(stylistId, bookings.map { it.id })
        upsertAll(bookings)
    }

    @Transaction
    suspend fun replaceAll(bookings: List<BookingEntity>) {
        deleteAllExcept(bookings.map { it.id })
        upsertAll(bookings)
    }

    @Query("DELETE FROM bookings")
    suspend fun clear()
}

data class StatusCount(val status: String, val count: Int)
