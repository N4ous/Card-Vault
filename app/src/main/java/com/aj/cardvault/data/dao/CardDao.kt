package com.aj.cardvault.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aj.cardvault.data.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM cards ORDER BY updatedAt DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE nfcIdentifier = :identifier LIMIT 1")
    suspend fun getCardByNfcIdentifier(identifier: String): CardEntity?

    /**
     * Local, offline search across safe metadata only.
     * Never searches ciphertext or full sensitive values.
     */
    @Query(
        """
        SELECT * FROM cards
        WHERE bank LIKE '%' || :query || '%'
           OR cardName LIKE '%' || :query || '%'
           OR cardholderName LIKE '%' || :query || '%'
           OR lastFourDigits LIKE '%' || :query || '%'
           OR type LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<CardEntity>>
}
