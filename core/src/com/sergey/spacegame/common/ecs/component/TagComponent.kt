package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class TagComponent @JvmOverloads constructor(var tag: String = "") : ClonableComponent {
    
    override fun copy(): Component {
        return TagComponent(tag)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(TagComponent::class.java)!!
    }
}