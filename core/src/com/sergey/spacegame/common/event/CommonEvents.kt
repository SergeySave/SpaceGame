package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.google.gson.GsonBuilder

class GsonRegisterEvent(val gson: GsonBuilder) : Event()

/**
 * Selection Change Event done in a way that the event is created through a builder that uses a single instance
 */
class SelectionChangeEvent : Event() {
    private var _selected: List<Entity>? = null

    var selected: List<Entity>
        get() {return _selected!!}
        private set(v) {_selected = v}

    class Builder {
        private val selectionChangeEvent = SelectionChangeEvent()

        operator fun get(selected: List<Entity>): SelectionChangeEvent {
            selectionChangeEvent.selected = selected

            return selectionChangeEvent
        }
    }
}

class BeginLevelEvent : Event()
