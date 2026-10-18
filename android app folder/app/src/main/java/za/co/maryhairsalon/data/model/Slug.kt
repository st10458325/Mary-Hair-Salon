package za.co.maryhairsalon.data.model

/** Readable ids for categories and services, e.g. "Knotless Braids!" -> "knotless-braids". */
object Slug {
    private val VALID = Regex("^[a-z0-9-]+$")

    fun from(text: String): String =
        text.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    /** Same rule as the CHECK constraint on categories.id / services.id in 001_schema.sql. */
    fun isValid(id: String): Boolean = id.matches(VALID)
}
