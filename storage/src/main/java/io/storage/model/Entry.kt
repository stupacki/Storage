package io.storage.model

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.storage.model.LifeTime.*
import io.storage.model.Payload.PayloadConverter
import java.util.*

@Entity
data class Entry(
    val payloadId: String = "",
    @Convert(converter = PayloadConverter::class, dbType = String::class)
    val payload: Payload = payloadOf(""),
    val collection: String = "",
    @Convert(converter = ValidityTimeConverter::class, dbType = Int::class)
    val lifeTime: LifeTime = FOREVER
) {

    @Id
    var id: Long = NEW_ID
    var creationDate: Long = newTimeStamp()
    val lastUpdate: Long = creationDate

    fun isValid(isAppStart: Boolean): Boolean =
        when {
            lifeTime == FOREVER -> true
            lastUpdate + lifeTime.time > newTimeStamp() -> true
            lifeTime == NEXT_APP_START && !isAppStart -> true
            else -> false
        }

    companion object {
        private const val NEW_ID: Long = 0L

        fun newTimeStamp(): Long = Calendar.getInstance().timeInMillis
    }
}

