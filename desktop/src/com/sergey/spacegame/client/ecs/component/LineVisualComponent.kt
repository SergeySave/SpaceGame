package com.sergey.spacegame.client.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.graphics.Color

/**
 * @author sergeys
 */
class LineVisualComponent(var x: Float, var y: Float, var x2: Float, var y2: Float, var thickness: Float,
                          var color: Int, var life: Float) : Component {
    
    var originalLife = life
    
    fun getColorWithOpacity(): Color = CLR.apply { set(color).a = life / originalLife }
    
    companion object Map {
        @JvmField
        val MAPPER = ComponentMapper.getFor(LineVisualComponent::class.java)
    
        private val CLR: Color = Color()
    }
}