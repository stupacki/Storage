package io.storage

import io.kotlintest.shouldBe
import io.storage.model.ValidityTime
import io.storage.model.payloadOf

internal class SyncStorageSpec : StorageSpec() {

    init {

        "put()" should {

            "save a data body successfully forever" {

                saveUserToStorage() shouldBe payloadOf(JSON_DATA)
            }

            "save a data body successful with a lifetime" {

                saveUserWithLifeTimeToStorage() shouldBe payloadOf(JSON_DATA)
            }
        }
    }

    private fun saveUserToStorage() =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = ID,
            payload = payloadOf(JSON_DATA)
        )

    private fun saveUserWithLifeTimeToStorage() =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = ID,
            payload = payloadOf(JSON_DATA),
            validityTime = ValidityTime.SPAN_ONE_DAY
        )

    companion object {
        private const val USER_COLLECTION = "USER_COLLECTION"
        private const val ID = "1"
        private const val JSON_DATA = "{'id': $ID}"
    }
}