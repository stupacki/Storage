package io.storage.model

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
data class Entry(
    val id: String = "",
    val payload: String = "",
    val collection: String = "",
    @Convert(converter = ValidityTime.Converter::class, dbType = Int::class)
    val validityTime: ValidityTime = ValidityTime.SPAN_FOREVER
) {

    @Id
    var dbIdentifier: Long = NEW_ID
    val creationDate: Long = newTimeStamp()
    val lastUpdate: Long = newTimeStamp()

    fun isValid(isAppStart: Boolean): Boolean =
        when {
            validityTime == ValidityTime.SPAN_FOREVER -> true
            lastUpdate + validityTime.time > newTimeStamp() -> true
            validityTime == ValidityTime.SPAN_NEXT_APP_START && !isAppStart -> true
            else -> false
        }

    companion object {
        private const val NEW_ID: Long = 0L

        fun newTimeStamp(): Long = Calendar.getInstance().timeInMillis
    }
}

