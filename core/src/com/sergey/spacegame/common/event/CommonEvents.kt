package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.google.gson.GsonBuilder
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.orders.IOrder
import org.luaj.vm2.LuaValue
import kotlin.properties.Delegates

//-------------------------------------------
//Simple events

class GsonRegisterEvent(val gson: GsonBuilder) : Event()

class BeginLevelEvent(val level: Level) : Event()

class LuaDelayEvent(val id: LuaValue, val parameter: LuaValue) : Event()

//-------------------------------------------
//Single instance per builder events

/**
 * Selection Change Event done in a way that the event is created through a builder that uses a single instance
 */
class SelectionChangeEvent : Event() {
    
    var selected by Delegates.notNull<List<Entity>>()
    
    class Builder {
        private val selectionChangeEvent = SelectionChangeEvent()
    
        operator fun get(selected: List<Entity>): SelectionChangeEvent {
            selectionChangeEvent.selected = selected
        
            return selectionChangeEvent
        }
    }
}

/**
 * Selection Change Event done in a way that the event is created through a builder that uses a single instance
 */
class CommandIssuedEvent : Event() {
    var targets by Delegates.notNull<Iterator<Entity>>()
    var id by Delegates.notNull<String>()
    
    class Builder {
        private val event = CommandIssuedEvent()
        
        operator fun get(targets: Iterator<Entity>, id: String): CommandIssuedEvent {
            event.targets = targets
            event.id = id
            
            return event
        }
    }
}

/**
 * Order Initialized Event done in a way that the event is created through a builder that uses a single instance
 */
class OrderInitializedEvent : Event() {
    
    var order by Delegates.notNull<IOrder>()
    
    class Builder {
        private val event = OrderInitializedEvent()
    
        operator fun get(order: IOrder): OrderInitializedEvent {
            event.order = order
        
            return event
        }
    }
}

//-------------------------------------------
//Other events
