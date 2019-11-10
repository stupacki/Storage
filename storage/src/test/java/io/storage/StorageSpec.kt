package io.storage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.WordSpec
import io.objectbox.Box
import io.objectbox.BoxStore
import io.storage.model.Entry
import io.storage.model.MyObjectBox
import org.junit.Rule
import org.junit.rules.TestRule
import java.io.File

internal abstract class StorageSpec : WordSpec() {

    lateinit var storage: Storage
    private lateinit var boxStore: BoxStore
    private lateinit var boxStoreDir: File
    lateinit var entryBox: Box<Entry>

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        boxStoreDir = File.createTempFile("test-storage", "").apply { delete() }
        boxStore = MyObjectBox.builder().directory(boxStoreDir).build()
        entryBox = boxStore.boxFor(Entry::class.java)

        storage = Storage(entryBox)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)

        boxStore.apply {
            close()
            deleteAllFiles()
        }
    }
}
