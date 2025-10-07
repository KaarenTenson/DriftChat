package com.app.driftchat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.driftchat.domainmodel.UserData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserData)

    @Query("SELECT * FROM user_data WHERE id = :id")
    fun getUserDataById(id: Int): Flow<UserData?>

    @Query("SELECT * FROM user_data")
    fun getAllUserData(): Flow<List<UserData>>
}
