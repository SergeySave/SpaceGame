package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.google.gson.GsonBuilder
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.orders.IOrder
import org.luaj.vm2.LuaValue

//-------------------------------------------
//Simple events

/**
 * An event to register things to the Gson before it is built
 *
 * @author sergeys
 */
class GsonRegisterEvent(val gson: GsonBuilder) : Event()

/**
 * An event that is called when a level is begun
 *
 * @author sergeys
 */
class BeginLevelEvent(val level: Level) : Event()

/**
 * An event that is called after lua posted a delay event with a given id and value
 *
 * @author sergeys
 */
class LuaDelayEvent(val id: LuaValue, val parameter: LuaValue) : Event()

//-------------------------------------------
//Single instance per builder events

/**
 * Selection Change Event done in a way that the event is created through a builder that uses a single instance
 *
 * @author sergeys
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
 * Command Issued Event done in a way that the event is created through a builder that uses a single instance
 *
 * @author sergeys
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
 *
 * @author sergeys
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
