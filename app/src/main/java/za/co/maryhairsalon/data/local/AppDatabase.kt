package za.co.maryhairsalon.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import za.co.maryhairsalon.data.local.dao.BookingDao
import za.co.maryhairsalon.data.local.dao.CategoryDao
import za.co.maryhairsalon.data.local.dao.ServiceDao
import za.co.maryhairsalon.data.local.dao.UserDao
import za.co.maryhairsalon.data.local.entity.BookingEntity
import za.co.maryhairsalon.data.local.entity.CategoryEntity
import za.co.maryhairsalon.data.local.entity.ServiceEntity
import za.co.maryhairsalon.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ServiceEntity::class,
        BookingEntity::class,
    ],
    // v2: switched from Firebase to Supabase (bookings columns changed, booking_slots removed).
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao

    companion object {
        private const val NAME = "mary_hair_salon.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                    // The DB is only a cache of Supabase, so wiping it on a schema change is safe:
                    // it refills from Supabase on the next refresh.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
