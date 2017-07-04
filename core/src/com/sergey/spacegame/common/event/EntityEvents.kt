package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity

/**
 * @author sergeys
 */
open class EntityEvent: Event() {
    private var _entity: Entity? = null

    var entity: Entity
        get() {return _entity!!}
        protected set(v) {_entity = v}
}


/**
 * Entity Added Event done in a way that the event is created through a builder that uses a single instance
 */
class EntityAddedEvent: EntityEvent() {
    class Builder {
        private val event = EntityAddedEvent()

        operator fun get(entity: Entity): EntityAddedEvent {
            event.entity = entity

            return event
        }
    }
}

/**
 * Entity Removed Event done in a way that the event is created through a builder that uses a single instance
 */
class EntityRemovedEvent: EntityEvent() {
    class Builder {
        private val event = EntityRemovedEvent()

        operator fun get(entity: Entity): EntityRemovedEvent {
            event.entity = entity

            return event
        }
    }
}
