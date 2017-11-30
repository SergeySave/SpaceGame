package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * This component represents a message
 *
 * @author sergeys
 *
 * @constructor Create a new MessageComponent
 *
 * @property imageName - the name of the image to be used by this component
 * @property region - the texture region to use for this component
 * @property message - the message to display
 * @property endTime - the time to delete the entity containing the component
 */
class MessageComponent(val imageName: String, val region: TextureRegion?, val message: String,
                       val endTime: Long) : Component {
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(MessageComponent::class.java)!!
    }
}