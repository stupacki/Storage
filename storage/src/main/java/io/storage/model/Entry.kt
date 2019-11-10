package io.storage.model

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.storage.model.Payload.PayloadConverter
import io.storage.model.ValidityTime.ValidityTimeConverter
import java.util.*

@Entity
data class Entry(
    val payloadId: String,
    @Convert(converter = PayloadConverter::class, dbType = String::class)
    val payload: Payload,
    val collection: String,
    @Convert(converter = ValidityTimeConverter::class, dbType = Int::class)
    val validityTime: ValidityTime
) {

    @Id
    var id: Long = NEW_ID
    var creationDate: Long = newTimeStamp()
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

