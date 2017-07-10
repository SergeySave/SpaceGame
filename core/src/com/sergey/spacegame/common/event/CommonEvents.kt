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
    private var _selected: List<Entity>? = null
    
    var selected: List<Entity>
        get() = _selected!!
        private set(v) {
            _selected = v
        }
    
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
    private var _targets: Iterator<Entity>? = null
    private var _id: String? = null
    
    var targets: Iterator<Entity>
        get() = _targets!!
        private set(v) {
            _targets = v
        }
    var id: String
        get() = _id!!
        private set(v) {
            _id = v
        }
    
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
    private var _order: IOrder? = null
    
    var order: IOrder
        get() = _order!!
        private set(v) {
            _order = v
        }
    
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
