package com.aj.cardvault.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aj.cardvault.data.dao.CardDao
import com.aj.cardvault.data.entity.CardEntity

@Database(
    entities = [CardEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CardVaultDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao

    companion object {
        private const val DB_NAME = "cardvault.db"

        @Volatile
        private var INSTANCE: CardVaultDatabase? = null

        fun getInstance(context: Context): CardVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CardVaultDatabase::class.java,
                    DB_NAME
                )
                    // No destructive fallback in a financial-data app; migrations must be explicit.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
