package io.storage.model

import io.objectbox.converter.PropertyConverter
import java.util.concurrent.TimeUnit

enum class LifeTime(val time: Int) {

    ONE_SECOND(TimeUnit.SECONDS.toMillis(1L).toInt()),
    FIVE_SECONDS(TimeUnit.SECONDS.toMillis(5L).toInt()),
    TEN_SECONDS(TimeUnit.SECONDS.toMillis(10L).toInt()),
    ONE_MINUTE(TimeUnit.MINUTES.toMillis(1L).toInt()),
    FIFE_MINUTES(TimeUnit.MINUTES.toMillis(5L).toInt()),
    TEN_MINUTES(TimeUnit.MINUTES.toMillis(10L).toInt()),
    ONE_HOUR(TimeUnit.HOURS.toMillis(1L).toInt()),
    ONE_DAY(TimeUnit.DAYS.toMillis(1L).toInt()),
    FOREVER(0),
    NEXT_APP_START(-1);

    class ValidityTimeConverter : PropertyConverter<LifeTime, Int> {

        override fun convertToDatabaseValue(entityProperty: LifeTime): Int =
            entityProperty.time

        override fun convertToEntityProperty(databaseValue: Int): LifeTime =
            LifeTime.values().first { databaseValue == it.time }
    }
}
