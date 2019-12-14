package io.storage

import com.squareup.moshi.Moshi
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.storage.model.LifeTime
import io.storage.model.LifeTime.*
import java.lang.Thread.sleep

internal class StorageSpec : AbstractStorageSpec() {

    init {

        "put()" should {

            "save a data body successfully forever" {

                saveData(USER_COLLECTION, TEST_USER) shouldBe TestUser(ID)
            }

            "save a data body successful with a lifetime" {

                saveDataWithLifeTime(TEST_USER, ONE_DAY) shouldBe TestUser(ID)
            }

            "update an existing data set correctly" {

                saveData(USER_COLLECTION, TEST_USER) shouldBe TestUser(ID)

                val oldCreationDate = entryBox.all.first().creationDate
                val oldLastUpdate = entryBox.all.first().lastUpdate

                oldCreationDate shouldBe oldLastUpdate

                saveData(USER_COLLECTION, TEST_USER) shouldBe TestUser(ID)

                val newCreationDate = entryBox.all.first().creationDate
                val newLastUpdate = entryBox.all.first().lastUpdate

                storage.get(USER_COLLECTION, reader).size shouldBe 1

                newCreationDate shouldBe oldCreationDate
                newLastUpdate shouldBeGreaterThan oldLastUpdate
            }
        }

        "get()" should {

            "return a previously saved data body" {

                saveData(USER_COLLECTION, TEST_USER)

                storage.get(USER_COLLECTION, ID, reader) shouldBe TestUser(ID)
            }

            "return null when no data can be found" {

                storage.get(USER_COLLECTION, ID, reader) shouldBe null
            }

            "return all data sets of a collection" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, TestUser("$id")) }

                storage.get(USER_COLLECTION, reader).size shouldBe 3
            }

            "return an empty list when the collection is empty" {

                storage.get(USER_COLLECTION, reader).size shouldBe 0
            }
        }

        "remove()" should {

            "delete a previously stored data set" {

                saveData(USER_COLLECTION, TEST_USER)

                storage.remove(USER_COLLECTION, TEST_USER.id)

                storage.get(USER_COLLECTION, reader) shouldBe emptyList()
            }

            "delete all entries from a collection" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, TestUser("$id")) }

                storage.get(USER_COLLECTION, reader).size shouldBe 3

                storage.remove(USER_COLLECTION)

                storage.get(USER_COLLECTION, reader).size shouldBe 0
            }
        }

        "restore()" should {

            "delete all data across collections" {

                (1..3).forEach { id -> saveData(USER_COLLECTION, TestUser("$id")) }
                (1..3).forEach { id -> saveData(MESSAGES_COLLECTION, TestUser("$id")) }

                storage.get(USER_COLLECTION, reader).size shouldBe 3
                storage.get(MESSAGES_COLLECTION, reader).size shouldBe 3

                storage.restore()

                storage.get(USER_COLLECTION, reader).size shouldBe 0
                storage.get(MESSAGES_COLLECTION, reader).size shouldBe 0
            }
        }

        "cleanup()" should {

            "remove outdated data entries and leave valid ones in the db" {

                val firstUser = saveDataWithLifeTime(TestUser("1"), ONE_SECOND)
                val secondUser = saveDataWithLifeTime(TestUser("2"), ONE_HOUR)
                val thirdUser = saveDataWithLifeTime(TestUser("3"), NEXT_APP_START)

                sleep(1000L)

                storage.cleanup()

                storage.get(USER_COLLECTION, reader).let { userData ->
                    userData.size shouldBe 2
                    userData shouldNotContain firstUser
                    userData shouldContain secondUser
                    userData shouldContain thirdUser
                }
            }
        }

        "Builder" should {

            "throw an exception when a build has been started without app context" {

                shouldThrow<IllegalStateException> {
                    Storage.Builder.build()
                }
                    .message shouldBe "Application context has not been set for storage"
            }
        }
    }

    private fun saveData(collection: String, user: TestUser) =
        storage.put(
            collection = collection,
            payloadId = user.id,
            payload = user,
            writer = writer)

    private fun saveDataWithLifeTime(user: TestUser, validityTime: LifeTime) =
        storage.put(
            collection = USER_COLLECTION,
            payloadId = user.id,
            payload = user,
            lifeTime = validityTime,
            writer = writer
        )

    data class TestUser(val id: String)

    companion object {
        private const val USER_COLLECTION = "USER_COLLECTION"
        private const val MESSAGES_COLLECTION = "MESSAGES_COLLECTION"
        private const val ID = "1"

        private val TEST_USER = TestUser("1")

        private val testUserAdapter = Moshi.Builder().build().adapter(TestUser::class.java)

        private val reader: (String?) -> TestUser? = { json -> json?.let { testUserAdapter.fromJson(json) } }
        private val writer: (TestUser) -> String = { obj -> testUserAdapter.toJson(obj) }
    }
}