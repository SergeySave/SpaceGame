package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.ecs.component.TickableComponent
import kotlin.properties.Delegates

/**
 * An event that took place for a given entity
 *
 * @author sergeys
 */
open class EntityEvent : Event() {
    lateinit var entity: Entity
}

/**
 * Entity Added Event done in a way that the event is created through a builder that uses a single instance
 *
 * @author sergeys
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
 * Building Built Event done in a way that the event is created through a builder that uses a single instance
 *
 * @author sergeys
 */
class BuildingConstructedEvent : EntityEvent() {
    
    lateinit var id: String
    
    class Builder {
        private val event = BuildingConstructedEvent()
        
        operator fun get(entity: Entity, id: String): BuildingConstructedEvent {
            event.entity = entity
            event.id = id
            
            return event
        }
    }
}

/**
 * Entity Removed Event done in a way that the event is created through a builder that uses a single instance
 *
 * @author sergeys
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
 *
 * @author sergeys
 */
class EntityTickEvent : EntityEvent() {
    
    lateinit var id: String
    var count by Delegates.notNull<Int>()
    
    class Builder {
        private val event = EntityTickEvent()
        
        operator fun get(entity: Entity, tickableComponent: TickableComponent, count: Int): EntityTickEvent {
            event.entity = entity
            event.id = tickableComponent.id
            event.count = count
            
            return event
        }
    }
}
