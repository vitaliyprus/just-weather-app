package prus.justweatherapp.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import prus.justweatherapp.local.db.dao.LocationsDao
import prus.justweatherapp.local.db.dao.UserLocationsDao
import prus.justweatherapp.local.db.entity.LocationEntity
import prus.justweatherapp.local.db.entity.UserLocationEntity

@Database(
    entities = [LocationEntity::class, UserLocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationsDao(): LocationsDao
    abstract fun userLocationsDao(): UserLocationsDao

    companion object {

        private const val DATABASE_NAME = "just-weather-app-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    }
                )
                .build()
        }

    }
}