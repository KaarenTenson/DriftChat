package com.app.driftchat.domainmodel

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.app.driftchat.domainmodel.Gender
import com.app.driftchat.data.Converters
import com.app.driftchat.ui.viewmodels.QuoteViewModel

@Entity(tableName = "user_data")
@TypeConverters(Converters::class)
data class UserData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String?,
    var quote: String?,
    var hobbies: Set<String>,
    var description: String?,
    var gender: Gender?,
)
