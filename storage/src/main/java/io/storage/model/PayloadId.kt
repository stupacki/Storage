package io.storage.model

import io.objectbox.converter.PropertyConverter

data class PayloadId(
    val id: String,
    val collection: String
) {
    class PayloadIdConverter : PropertyConverter<PayloadId, String> {

        override fun convertToDatabaseValue(entityProperty: PayloadId): String =
            toString(entityProperty)

        override fun convertToEntityProperty(databaseValue: String): PayloadId =
            fromString(databaseValue)
    }

    companion object {
        private const val DB_DIVIDER = "-@-"

        fun toString(entityProperty: PayloadId): String =
            if (entityProperty.id.isNotEmpty() || entityProperty.collection.isNotEmpty())
                "${entityProperty.id}${DB_DIVIDER}${entityProperty.collection}"
            else throw IllegalStateException("Cannot write write value with empty id or collection to database")

        fun fromString(databaseValue: String): PayloadId =
            databaseValue.split(DB_DIVIDER).let { dbValues ->
                if (dbValues.size == 2)
                    PayloadId(
                        id = dbValues.first(),
                        collection = dbValues.last()
                    )
                else throw IllegalStateException("Cannot read database value")
            }
    }
}