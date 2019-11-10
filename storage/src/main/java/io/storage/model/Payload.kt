package io.storage.model

data class Payload(val json: String) {

    fun toJson(): String = json

    companion object {

        fun from(json: String): Payload =
            Payload(json = json)
    }
}