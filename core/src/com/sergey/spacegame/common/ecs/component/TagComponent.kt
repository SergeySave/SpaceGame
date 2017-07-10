package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class TagComponent : ClonableComponent {
    
    var tag: String = ""
    
    override fun copy(): Component {
        val component = TagComponent()
        
        component.tag = this.tag
        
        return component
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(TagComponent::class.java)!!
    }
}