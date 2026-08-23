package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadlineDate: Long,
    val colorHex: String = "#4376F6",
    val iconKey: String = "Flag",
    val note: String? = null,
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "REACHED"
    val lastAddedAmount: Double = 0.0,
    val lastAddedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoalsFlow(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY createdAt DESC")
    fun getGoalsByStatusFlow(status: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    suspend fun getAllGoals(): List<Goal>

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}
