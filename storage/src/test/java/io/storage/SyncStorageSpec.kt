package io.storage

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.storage.model.LifeTime
import io.storage.model.LifeTime.*
import io.storage.model.payloadOf
import java.lang.Thread.sleep

internal class SyncStorageSpec : StorageSpec() {

    init {

        "put()" should {

            "save a data body successfully forever" {

                saveData(USER_COLLECTION, ID) shouldBe payloadOf(genJson(ID))
            }

            "save a data body successful with a lifetime" {

                saveDataWithLifeTime(ID, ONE_DAY) shouldBe payloadOf(genJson(ID))
            }

            "update an existing data set correctly" {

                saveData(USER_COLLECTION, ID) shouldBe payloadOf(genJson(ID))

                val oldCreationDate = entryBox.all.first().creationDate
                val oldLastUpdate = entryBox.all.first().lastUpdate

                oldCreationDate shouldBe oldLastUpdate

                saveData(USER_COLLECTION, ID) shouldBe payloadOf(genJson(ID))

                val newCreationDate = entryBox.all.first().creationDate
                val newLastUpdate = entryBox.all.first().lastUpdate

                storage.get(USER_COLLECTION).size shouldBe 1

                newCreationDate shouldBe oldCreationDate
                newLastUpdate shouldBeGreaterThan oldLastUpdate
            }
        }

        "get()" should {

            "return a previously saved data body" {

                saveData(USER_COLLECTION, ID)

                storage.get(USER_COLLECTION, ID) shouldBe payloadOf(genJson(ID))
            }

            "return null when no data can be found" {

                storage.get(USER_COLLECTION, ID) shouldBe null
            }

            "return all data sets of a collection" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, genJson("$id")) }

                storage.get(USER_COLLECTION).size shouldBe 3
            }

            "return an empty list when the collection is empty" {

                storage.get(USER_COLLECTION).size shouldBe 0
            }
        }

        "remove()" should {

            "delete a previously stored data set" {

                saveData(USER_COLLECTION, ID)

                storage.remove(USER_COLLECTION, ID)

                storage.get(USER_COLLECTION, ID) shouldBe null
            }

            "delete all entries from a collection" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, genJson("$id")) }

                storage.get(USER_COLLECTION).size shouldBe 3

                storage.remove(USER_COLLECTION)

                storage.get(USER_COLLECTION).size shouldBe 0
            }
        }

        "restore()" should {

            "delete all data across collections" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, genJson("$id")) }
                (1..3).forEach { id -> saveData(MESSAGES_COLLECTION, genJson("$id")) }

                storage.get(USER_COLLECTION).size shouldBe 3
                storage.get(MESSAGES_COLLECTION).size shouldBe 3

                storage.restore()

                storage.get(USER_COLLECTION).size shouldBe 0
                storage.get(MESSAGES_COLLECTION).size shouldBe 0
            }
        }

        "cleanup()" should {

            "remove outdated data entries and leave valid ones in the db" {

                val firstUser = saveDataWithLifeTime("1", ONE_SECOND)
                val secondUser = saveDataWithLifeTime("2", ONE_HOUR)
                val thirdUser = saveDataWithLifeTime("3", NEXT_APP_START)

                sleep(1000L)

                storage.cleanup()

                storage.get(USER_COLLECTION).size shouldBe 2
                storage.get(USER_COLLECTION) shouldNotContain firstUser
                storage.get(USER_COLLECTION) shouldContain secondUser
                storage.get(USER_COLLECTION) shouldContain thirdUser

            }
        }
    }

    private fun saveData(collection: String, id: String) =
        storage.put(
            collection = collection,
            payloadId = id,
            payload = payloadOf(genJson(id))
        )

    private fun saveDataWithLifeTime(id: String, validityTime: LifeTime) =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = id,
            payload = payloadOf(genJson(id)),
            lifeTime = validityTime
        )

    companion object {
        private const val USER_COLLECTION = "USER_COLLECTION"
        private const val MESSAGES_COLLECTION = "MESSAGES_COLLECTION"
        private const val ID = "1"

        private fun genJson(id: String) = "{'id': $id}"
    }
}