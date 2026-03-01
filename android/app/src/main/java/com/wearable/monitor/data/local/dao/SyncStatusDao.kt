package com.wearable.monitor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wearable.monitor.data.local.entity.SyncStatusEntity

@Dao
interface SyncStatusDao {

    @Query("SELECT * FROM sync_status WHERE item_code = :itemCode")
    suspend fun getByItemCode(itemCode: String): SyncStatusEntity?

    @Query("SELECT * FROM sync_status")
    suspend fun getAll(): List<SyncStatusEntity>

    @Upsert
    suspend fun upsert(entity: SyncStatusEntity)
}
