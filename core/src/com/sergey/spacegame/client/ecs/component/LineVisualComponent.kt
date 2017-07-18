package com.sergey.spacegame.client.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class LineVisualComponent(var x: Float, var y: Float, var x2: Float, var y2: Float, var thickness: Float,
                          var color: Float, var life: Float) : Component {
    
    companion object Map {
        @JvmField
        val MAPPER = ComponentMapper.getFor(LineVisualComponent::class.java)
    }
}