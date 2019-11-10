package io.storage

import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.storage.model.Entry
import io.storage.model.LifeTime
import io.storage.model.LifeTime.ONE_DAY
import io.storage.model.payloadOf

internal class SyncStorageSpec : StorageSpec() {

    init {

        "put()" should {

            "save a data body successfully forever" {

                saveUserToStorage(ID) shouldBe payloadOf(userJson(ID))
            }

            "save a data body successful with a lifetime" {

                saveUserWithLifeTimeToStorage(ID, ONE_DAY) shouldBe payloadOf(userJson(ID))
            }

            "update an existing data set correctly" {

                saveUserToStorage(ID) shouldBe payloadOf(userJson(ID))

                val oldCreationDate = entryBox.all.first().creationDate
                val oldLastUpdate = entryBox.all.first().lastUpdate

                oldCreationDate shouldBe oldLastUpdate

                saveUserToStorage(ID) shouldBe payloadOf(userJson(ID))

                val newCreationDate = entryBox.all.first().creationDate
                val newLastUpdate = entryBox.all.first().lastUpdate

                storage.get(USER_COLLECTION).size shouldBe 1

                newCreationDate shouldBe oldCreationDate
                newLastUpdate shouldBeGreaterThan oldLastUpdate
            }
        }

        "get()" should {

            "return a previously saved data body" {

                saveUserToStorage(ID)

                storage.get(USER_COLLECTION, ID) shouldBe payloadOf(userJson(ID))
            }

            "return null when no data can be found" {

                storage.get(USER_COLLECTION, ID) shouldBe null
            }

            "return all data sets of a collection" {

                (1..3).forEach { id -> saveUserToStorage(userJson("$id")) }

                storage.get(USER_COLLECTION).size shouldBe 3
            }

            "return an empty list when the collection is empty" {

                storage.get(USER_COLLECTION).size shouldBe 0
            }
        }
    }

    private fun saveUserToStorage(id: String) =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = id,
            payload = payloadOf(userJson(id))
        )

    private fun saveUserWithLifeTimeToStorage(id: String, validityTime: LifeTime) =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = id,
            payload = payloadOf(userJson(id)),
            lifeTime = validityTime
        )

    companion object {
        private const val USER_COLLECTION = "USER_COLLECTION"
        private const val ID = "1"

        private fun userJson(id: String) = "{'id': $id}"

        private fun userEntry(id: String): Entry =
            Entry(
                collection = USER_COLLECTION,
                payloadId = id,
                payload = payloadOf(userJson(id)),
                lifeTime = ONE_DAY
            )
    }
}