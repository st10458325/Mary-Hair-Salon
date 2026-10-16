package com.teamsalon.booking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(services: List<ServiceEntity>)

    @Query("SELECT * FROM services ORDER BY category, title")
    fun observeAll(): Flow<List<ServiceEntity>>

    @Query("SELECT COUNT(*) FROM services")
    suspend fun count(): Int
}
