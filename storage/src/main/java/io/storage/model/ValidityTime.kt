@file:Suppress("MagicNumber")

package io.storage.model

import io.objectbox.converter.PropertyConverter
import java.util.concurrent.TimeUnit

/**
 * Use this validity time constants to define how long data should be stored in the storage
 * @value SPAN_ONE_SECOND Deletion after 1 seconds (Use it for test)
 * @value SPAN_FIVE_SECONDS Deletion after 5 seconds
 * @value SPAN_TEN_SECONDS Deletion after 10 seconds
 * @value SPAN_ONE_MINUTE Deletion after 1 minute
 * @value SPAN_ONE_HOUR Deletion after 1 hour
 * @value SPAN_ONE_DAY Deletion after 1 day
 * @value SPAN_FOREVER Should not be deleted
 */
enum class ValidityTime(val time: Int) {

    SPAN_ONE_SECOND(TimeUnit.SECONDS.toMillis(1L).toInt()), //use only to make tests shorter
    SPAN_FIVE_SECONDS(TimeUnit.SECONDS.toMillis(5L).toInt()), //5 seconds
    SPAN_TEN_SECONDS(TimeUnit.SECONDS.toMillis(10L).toInt()), //10 seconds
    SPAN_ONE_MINUTE(TimeUnit.MINUTES.toMillis(1L).toInt()), //1 minute
    SPAN_FIFE_MINUTES(TimeUnit.MINUTES.toMillis(5L).toInt()), //10 minutes
    SPAN_TEN_MINUTES(TimeUnit.MINUTES.toMillis(10L).toInt()), //10 minutes
    SPAN_ONE_HOUR(TimeUnit.HOURS.toMillis(1L).toInt()), //1 hour
    SPAN_ONE_DAY(TimeUnit.DAYS.toMillis(1L).toInt()), //1 day
    SPAN_FOREVER(0), //forever
    SPAN_NEXT_APP_START(-1);

    class Converter : PropertyConverter<ValidityTime, Int> {

        override fun convertToDatabaseValue(entityProperty: ValidityTime): Int =
            entityProperty.time

        override fun convertToEntityProperty(databaseValue: Int): ValidityTime =
            ValidityTime.values().first { databaseValue == it.time }
    }
}
