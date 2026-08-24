package com.example.data.local.impact

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImpactDao {
    @Query("SELECT * FROM impact_initiatives")
    fun getAllInitiatives(): Flow<List<ImpactEntity>>

    @Query("SELECT * FROM impact_initiatives WHERE id = :id")
    suspend fun getInitiativeById(id: String): ImpactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInitiatives(initiatives: List<ImpactEntity>)

    @Query("UPDATE impact_initiatives SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)
}
