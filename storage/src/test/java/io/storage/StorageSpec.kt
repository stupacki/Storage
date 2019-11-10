package io.storage

import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.WordSpec
import io.objectbox.BoxStore
import io.storage.model.Entry
import io.storage.model.MyObjectBox
import java.io.File

internal abstract class StorageSpec : WordSpec() {

    lateinit var storage: Storage
    private lateinit var boxStore: BoxStore
    private lateinit var boxStoreDir: File

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        boxStoreDir = File.createTempFile("test-storage-db", "").apply { delete() }
        boxStore = MyObjectBox.builder().directory(boxStoreDir).build()

        storage = Storage(boxStore.boxFor(Entry::class.java))
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)

        boxStore.apply {
            close()
            deleteAllFiles()
        }
    }
}
