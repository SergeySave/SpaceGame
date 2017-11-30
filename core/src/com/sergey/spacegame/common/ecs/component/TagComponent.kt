package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * This component represents an entity with a tag
 *
 * @author sergeys
 *
 * @constructor Create a new TagComponent
 *
 * @property tag - the tag given to the entity
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