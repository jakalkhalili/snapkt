package pl.alkhalili.snapkt.common

import com.google.gson.Gson

data class EventBusMessage(val type: String, val message: String)

fun <T> eventBusMessageOf(message: T, clazz: Class<T>): EventBusMessage {
    return EventBusMessage(
        clazz.simpleName,
        Gson().toJson(message)
    )
}

fun EventBusMessage.toJson(): String {
    return Gson().toJson(this)
}
