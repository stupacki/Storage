package io.example.app.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.example.app.ExampleApp
import io.example.app.R
import io.storage.model.Payload

class MainActivity : AppCompatActivity() {

    private val storage by lazy { ExampleApp.storage }
    private val moshi by lazy { ExampleApp.moshi }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        saveData()

        Toast.makeText(this, getData(), Toast.LENGTH_SHORT).show()
    }

    private fun saveData() {
        storage.put(COLLECTION, ID, Payload("I'm a string"))
    }

    private fun getData(): String =
        storage.get(COLLECTION, ID)?.json ?: ""

    companion object {
        private const val COLLECTION = "Strings"
        private const val ID = "STRING_ID"
    }
}
