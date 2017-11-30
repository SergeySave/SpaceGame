package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * This component represents a tickable component and its id
 *
 * @author sergeys
 *
 * @constructor Create a new TickableComponent
 *
 * @property id - the id of the tickable component
 */
class TickableComponent @JvmOverloads constructor(var id: String = "") : ClonableComponent {
    
    override fun copy(): Component {
        return TickableComponent(id)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(TickableComponent::class.java)!!
    }
}