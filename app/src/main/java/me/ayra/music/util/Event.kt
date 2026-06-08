package me.ayra.gallery.shared.utils

class Event<T> {
    private val observers = mutableSetOf<(T) -> Unit>()

    val size: Int get() = synchronized(observers) { observers.size }

    operator fun plusAssign(observer: (T) -> Unit) {
        synchronized(observers) {
            observers.add(observer)
        }
    }

    operator fun minusAssign(observer: (T) -> Unit) {
        synchronized(observers) {
            observers.remove(observer)
        }
    }

    operator fun invoke(value: T) {
        val snapshot = synchronized(observers) { observers.toList() }
        snapshot.forEach { observer -> observer(value) }
    }
}
