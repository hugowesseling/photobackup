package com.hugowesseling.photobackup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class Configuration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val server: String,
    val backupFolder: String,
    val localFolder: String,
    val username: String,
    val password: String
)
