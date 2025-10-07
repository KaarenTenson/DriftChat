package com.app.driftchat.data

import androidx.room.TypeConverter
import com.app.driftchat.domainmodel.Gender
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Converter for Set<String>
    @TypeConverter
    fun fromHobbiesSet(hobbies: Set<String>): String {
        return gson.toJson(hobbies)
    }

    @TypeConverter
    fun toHobbiesSet(hobbiesString: String): Set<String> {
        val setType = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(hobbiesString, setType)
    }

    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return gender?.name
    }

    @TypeConverter
    fun toGender(genderString: String?): Gender? {
        return genderString?.let { enumValueOf<Gender>(it) }
    }
}
