package com.hugowesseling.photobackup

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationDao {
    @Query("SELECT * FROM configurations")
    fun getAll(): Flow<List<Configuration>>

    @Query("SELECT * FROM configurations WHERE id = :id")
    suspend fun getById(id: Int): Configuration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(configuration: Configuration)

    @Update
    suspend fun update(configuration: Configuration)

    @Delete
    suspend fun delete(configuration: Configuration)
}
