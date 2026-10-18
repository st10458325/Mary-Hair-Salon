package za.co.maryhairsalon.data.local

import androidx.room.TypeConverter

/**
 * Stores List<String> (service tags) as one text column, separated by the ASCII
 * "unit separator" character, which never appears in normal text. Pure Kotlin so it
 * can be unit-tested on the JVM (org.json is stubbed out in local unit tests).
 */
class Converters {
    @TypeConverter
    fun fromTags(tags: List<String>): String = tags.joinToString(SEPARATOR)

    @TypeConverter
    fun toTags(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    companion object {
        const val SEPARATOR = "\u001F"
    }
}
