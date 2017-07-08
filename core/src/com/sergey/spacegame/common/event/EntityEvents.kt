package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.ecs.component.TickableComponent

/**
 * @author sergeys
 */
open class EntityEvent : Event() {
    private var _entity: Entity? = null
    
    var entity: Entity
        get() = _entity!!
        protected set(v) {
            _entity = v
        }
}


/**
 * Entity Added Event done in a way that the event is created through a builder that uses a single instance
 */
class EntityAddedEvent : EntityEvent() {
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
class EntityRemovedEvent : EntityEvent() {
    class Builder {
        private val event = EntityRemovedEvent()
    
        operator fun get(entity: Entity): EntityRemovedEvent {
            event.entity = entity
        
            return event
        }
    }
}

/**
 * Entity Tick Event done in a way that the event is created through a builder that uses a single instance
 */
class EntityTickEvent : EntityEvent() {
    private var _id: String? = null
    private var _tickNum: Int? = null
    
    var id: String
        get() = _id!!
        protected set(v) {
            _id = v
        }
    var count: Int
        get() = _tickNum!!
        protected set(v) {
            _tickNum = v
        }
    
    class Builder {
        private val event = EntityTickEvent()
        
        operator fun get(entity: Entity, tickableComponent: TickableComponent, count: Int): EntityTickEvent {
            event.entity = entity
            event._id = tickableComponent.id
            event.count = count
            
            return event
        }
    }
}
