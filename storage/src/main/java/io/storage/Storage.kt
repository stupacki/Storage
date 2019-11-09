package io.storage

import android.content.Context
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.storage.model.MyObjectBox
import timber.log.Timber

class Storage(private val boxStore: BoxStore) {

    object Builder {

        private var appContext: Context? = null

        fun appContext(context: Context): Builder =
            apply { appContext = context }

        fun build(): Storage =
            appContext?.let { nonNullContext ->
                Storage(generateBoxStore(nonNullContext))
            } ?: throw IllegalStateException("Application context has not been set")

        private fun generateBoxStore(context: Context): BoxStore =
            MyObjectBox.builder().androidContext(context).build().apply {
                if (BuildConfig.DEBUG) {
                    AndroidObjectBrowser(this).start(context)
                    Timber.d("Started Object Box Browser")
                }
            }
    }

    fun <T> put(): T {
        TODO("implement")
    }

    fun <T> get(id: String): T {
        TODO("implement")
    }

    fun <T> getAll(collection: String): List<T> {
        TODO("implement")
    }

    fun <T> remove(id: String): Unit {
        TODO("implement")
    }

    fun <T> removeAll(collection: String): Unit {
        TODO("implement")
    }

    suspend fun <T> putAsync(): T {
        TODO("implement")
    }

    suspend fun <T> getAsync(id: String): T {
        TODO("implement")
    }

    suspend fun <T> getAllAsync(collection: String): List<T> {
        TODO("implement")
    }

    suspend fun <T> removeAsync(id: String): Unit {
        TODO("implement")
    }

    suspend fun <T> removeAllAsync(collection: String): Unit {
        TODO("implement")
    }

    private suspend fun <T> cleanup(): Unit {
        TODO("implement")
    }
}
