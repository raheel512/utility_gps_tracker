package com.example.gpstracker.core.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtMillis: Long,
)
