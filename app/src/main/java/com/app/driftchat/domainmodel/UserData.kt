package com.app.driftchat.domainmodel

class UserData(
    var name: String?,
    var hobbies: Array<String>?,
    var description: String?,
    var gender: Gender?,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserData

        if (name != other.name) return false
        if (!hobbies.contentEquals(other.hobbies)) return false
        if (description != other.description) return false
        if (gender != other.gender) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + hobbies.contentHashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + gender.hashCode()
        return result
    }
};