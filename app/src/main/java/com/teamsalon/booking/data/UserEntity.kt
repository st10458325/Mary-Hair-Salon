package com.teamsalon.booking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per account. Only two of these (see DatabaseSeeder) are the
 * "fake login" accounts used to demo the login screen; the rest pad the
 * table out to satisfy the "at least ten records per table" requirement
 * with realistic-looking sample customers.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String // plain text — fine for this fake/demo dataset only, never do this with real passwords
)
