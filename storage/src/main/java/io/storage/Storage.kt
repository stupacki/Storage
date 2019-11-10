package io.storage

import android.content.Context
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.storage.model.*
import io.storage.model.LifeTime.FOREVER
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
            } ?: throw IllegalStateException("Application context has not been set for storage")

        private fun generateBoxStore(context: Context): BoxStore =
            MyObjectBox.builder().androidContext(context).build().apply {
                if (BuildConfig.DEBUG) {
                    AndroidObjectBrowser(this).start(context)
                    Timber.d("Started Object Box Browser")
                }
            }
    }

    fun put(collection: String, payloadId: String, payload: Payload): Payload =
        save(collection, payloadId, payload, FOREVER)

    fun put(collection: String, payloadId: String, payload: Payload, lifeTime: LifeTime): Payload =
        save(collection, payloadId, payload, lifeTime)

    fun get(collection: String, payloadId: String): Payload? =
        find(collection, payloadId)?.payload

    fun get(collection: String): List<Payload> =
        find(collection).map(Entry::payload)

    fun remove(collection: String, payloadId: String) =
        delete(collection, payloadId)

    fun remove(collection: String) =
        delete(collection)

    fun restore() = delete()

    fun cleanup() {
        val removedEntries = removeOutDated(isAppStart = false)
        Timber.d("Removed $removedEntries outdated entries from storage")
    }

    private fun appStartCleanup() {
        val removedEntries = removeOutDated(isAppStart = true)
        Timber.d("Removed $removedEntries outdated entries from storage on app start")
    }

    private fun save(collection: String, payloadId: String, payload: Payload, lifeTime: LifeTime): Payload =
        payload.apply {
            find(collection, payloadId)
                ?.let { oldEntry ->
                    payload.apply {
                        entryBox.put(newEntry(collection, payloadId, payload, lifeTime)
                            .apply {
                                this.id = oldEntry.id
                                creationDate = oldEntry.creationDate
                            })
                    }
                } ?: entryBox.put(newEntry(collection, payloadId, payload, lifeTime))
        }

    private fun find(collection: String, payloadId: String): Entry? =
        entryBox.query()
            .run {
                equal(Entry_.collection, collection)
                equal(Entry_.payloadId, payloadId)
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

    private fun delete(collection: String, payloadId: String): Unit =
        find(collection, payloadId)?.let { entry ->
            entryBox.remove(entry.id)
        } ?: Unit

    private fun delete(collection: String): Unit =
        find(collection).forEach { entry ->
            entryBox.remove(entry.id)
        }

    private fun delete(): Unit =
        entryBox.removeAll()

    private fun removeOutDated(isAppStart: Boolean): Int =
        entryBox.all.map { entry ->
            if (!entry.isValid(isAppStart)) {
                entryBox.remove(entry.id)
                1
            } else 0
        }
            .sum()

    companion object {

        private fun newEntry(collection: String, payloadId: String, payload: Payload, validityTime: LifeTime) =
            Entry(payloadId, payload, collection, validityTime)
    }
}

