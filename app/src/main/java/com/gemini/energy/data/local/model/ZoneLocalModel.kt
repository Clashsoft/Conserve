package com.gemini.energy.data.local.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "Zone", foreignKeys = [ForeignKey(
                entity = AuditLocalModel::class,
                parentColumns = ["id"],
                childColumns = ["audit_id"])])

data class ZoneLocalModel(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var zoneId: Int?,
        var name: String,
        var type: String,
        var usn: Int,

        @ColumnInfo(name = "audit_id")
        var auditId: Long,

        @ColumnInfo(name = "created_at")
        var createdAt: Date,
        @ColumnInfo(name = "updated_at")
        var updatedAt: Date
)


