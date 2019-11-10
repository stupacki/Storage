package io.storage.model

import io.objectbox.converter.PropertyConverter

data class Payload(val json: String) {

    internal class PayloadConverter : PropertyConverter<Payload, String> {

        override fun convertToDatabaseValue(entityProperty: Payload): String =
            entityProperty.json

        override fun convertToEntityProperty(databaseValue: String): Payload =
            Payload(databaseValue)
    }
}

fun payloadOf(json: String): Payload =
    Payload(json)
