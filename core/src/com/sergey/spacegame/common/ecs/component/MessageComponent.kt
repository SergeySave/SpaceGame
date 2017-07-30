package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * @author sergeys
 */
class MessageComponent(val imageName: String, val region: TextureRegion, val message: String,
                       val endTime: Long) : Component {
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(MessageComponent::class.java)!!
    }
}