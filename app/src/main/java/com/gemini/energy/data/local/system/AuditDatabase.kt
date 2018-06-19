package com.gemini.energy.data.local.system

import android.arch.persistence.room.*
import android.content.Context
import com.gemini.energy.data.local.dao.*
import com.gemini.energy.data.local.model.*
import com.gemini.energy.data.local.util.Converters


@Database(
        entities = [

            AuditLocalModel::class,
            ZoneLocalModel::class,
            PreAuditLocalModel::class,
            AuditZoneTypeLocalModel::class

        ],

        version = 8, exportSchema = false)
@TypeConverters(Converters::class)

abstract class AuditDatabase : RoomDatabase() {

    abstract fun auditDao(): AuditDao
    abstract fun preAuditDao(): PreAuditDao
    abstract fun zoneDao(): ZoneDao
    abstract fun auditScopeDao(): AuditZoneTypeDao

    companion object {
        fun newInstance(context: Context): AuditDatabase {
            return Room.databaseBuilder(context, AuditDatabase::class.java, "geo-audit.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // **** This is only for Development ****
                    .build()
        }
    }
}