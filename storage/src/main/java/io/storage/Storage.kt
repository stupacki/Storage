package io.storage

import android.content.Context
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.storage.model.*
import io.storage.model.ValidityTime.SPAN_FOREVER
import timber.log.Timber

class Storage(private val entryBox: Box<Entry>) {

    object Builder {

        private var appContext: Context? = null

        fun appContext(context: Context): Builder =
            apply { appContext = context }

        fun build(): Storage =
            appContext?.let { nonNullContext ->
                Storage(entryBox = generateBoxStore(nonNullContext).boxFor(Entry::class.java))
                    .apply(Storage::appStartCleanup)
            } ?: throw IllegalStateException("Application context has not been set")

        private fun generateBoxStore(context: Context): BoxStore =
            MyObjectBox.builder().androidContext(context).build().apply {
                if (BuildConfig.DEBUG) {
                    AndroidObjectBrowser(this).start(context)
                    Timber.d("Started Object Box Browser")
                }
            }
    }

    //Sync Operations
    fun put(collection: String, id: String, payload: Payload): Payload =
        save(collection, id, payload, SPAN_FOREVER)

    fun put(collection: String, id: String, payload: Payload, validityTime: ValidityTime): Payload =
        save(collection, id, payload, validityTime)

    fun get(collection: String, id: String): Payload? =
        find(collection, id)?.payload

    fun get(collection: String): List<Payload> =
        find(collection).map(Entry::payload)

    fun remove(collection: String, id: String): Unit =
        delete(collection, id)

    fun remove(collection: String): Unit =
        delete(collection)

    fun restore(): Unit =
        delete()

    fun cleanup(): Unit {
        val removedEntries = removeOutDated(isAppStart = false)
        Timber.d("Removed $removedEntries outdated entries from storage")
    }

    //Async Operations
    suspend fun putAsync(collection: String, id: String, payload: Payload): Payload =
        save(collection, id, payload, SPAN_FOREVER)

    suspend fun putAsync(collection: String, id: String, payload: Payload, validityTime: ValidityTime): Payload =
        save(collection, id, payload, validityTime)

    suspend fun getAsync(collection: String, id: String): Payload? =
        find(collection, id)?.payload

    suspend fun getAsync(collection: String): List<Payload> =
        find(collection).map(Entry::payload)

    suspend fun removeAsync(collection: String, id: String): Unit =
        delete(collection, id)

    suspend fun removeAsync(collection: String): Unit =
        delete(collection)

    suspend fun restoreAsync(): Unit =
        delete()

    suspend fun cleanupAsync(): Unit {
        val removedEntries = removeOutDated(isAppStart = false)
        Timber.d("Removed $removedEntries outdated entries from storage")
    }

    //Private Operations
    private fun appStartCleanup(): Unit {
        val removedEntries = removeOutDated(isAppStart = true)
        Timber.d("Removed $removedEntries outdated entries from storage on app start")
    }

    private fun save(collection: String, id: String, payload: Payload, validityTime: ValidityTime): Payload =
        payload.apply {
            find(collection, id)
                ?.let { oldEntry ->
                    payload.apply {
                        if (oldEntry.id == id)
                            entryBox.put(newEntry(collection, id, payload, validityTime)
                                .apply {
                                    dbIdentifier = oldEntry.dbIdentifier
                                    creationDate = oldEntry.creationDate
                                })
                        else
                            entryBox.put(newEntry(collection, id, payload, validityTime))
                    }
                } ?: entryBox.put(newEntry(collection, id, payload, validityTime))
        }

    private fun find(collection: String, id: String): Entry? =
        entryBox.query()
            .run {
                equal(Entry_.collection, collection)
                equal(Entry_.id, id)
                build()
            }
            .findFirst()

    private fun find(collection: String): List<Entry> =
        entryBox.query()
            .run {
                equal(Entry_.collection, collection)
                build()
            }
            .find()

    private fun delete(collection: String, id: String): Unit =
        find(collection, id)?.let { entry ->
            entryBox.remove(entry.dbIdentifier)
        } ?: Unit

    private fun delete(collection: String): Unit =
        find(collection).forEach { entry ->
            entryBox.remove(entry.dbIdentifier)
        }

    private fun delete(): Unit =
        entryBox.removeAll()

    private fun removeOutDated(isAppStart: Boolean): Int =
        entryBox.all.map { entry ->
            if (!entry.isValid(isAppStart)) {
                entryBox.remove(entry.dbIdentifier)
                1
            } else 0
        }
            .sum()

    companion object {

        private fun newEntry(collection: String, id: String, payload: Payload, validityTime: ValidityTime) =
            Entry(id, payload, collection, validityTime)
    }
}

