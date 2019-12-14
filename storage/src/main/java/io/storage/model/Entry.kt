package io.storage.model

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.storage.model.LifeTime.*
import java.util.*

@Entity
data class Entry(
    @Unique
    @Convert(converter = PayloadId.PayloadIdConverter::class, dbType = String::class)
    val payloadId: PayloadId = PayloadId("", ""),
    val payload: String = "",
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

        private fun newTimeStamp(): Long = Calendar.getInstance().timeInMillis
    }
}

