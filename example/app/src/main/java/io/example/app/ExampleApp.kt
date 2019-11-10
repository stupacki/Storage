package io.example.app

import android.app.Application
import com.squareup.moshi.Moshi
import io.storage.Storage

class ExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        moshi = Moshi.Builder().build()

        storage = Storage.Builder.appContext(this@ExampleApp).build()
    }

    companion object {
        lateinit var storage: Storage

        lateinit var moshi: Moshi
    }
}
