package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.google.gson.GsonBuilder
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.orders.IOrder
import org.luaj.vm2.LuaValue

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
    
    lateinit var selected: List<Entity>
    
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
    lateinit var targets: Iterator<Entity>
    var count: Int = 0
    lateinit var id: String
    
    class Builder {
        private val event = CommandIssuedEvent()
    
        operator fun get(targets: Iterator<Entity>, id: String, count: Int): CommandIssuedEvent {
            event.targets = targets
            event.id = id
            event.count = count
            
            return event
        }
    }
}

/**
 * Order Initialized Event done in a way that the event is created through a builder that uses a single instance
 */
class OrderInitializedEvent : Event() {
    
    lateinit var order: IOrder
    
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
