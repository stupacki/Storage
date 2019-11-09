package io.example.app

import android.app.Application
import io.storage.Storage

class ExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        storage = Storage.Builder().appContext(this@ExampleApp).build()
    }

    companion object {
        lateinit var storage: Storage
    }
}
