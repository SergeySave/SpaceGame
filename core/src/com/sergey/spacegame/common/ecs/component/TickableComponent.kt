package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class TickableComponent : ClonableComponent {
    
    var id: String? = null
    
    override fun copy(): Component {
        val copy = TickableComponent()
        
        copy.id = this.id
        
        return copy
    }
    
    companion object MAP {
        @JvmStatic
        val MAPPER = ComponentMapper.getFor(TickableComponent::class.java)
    }
}