package com.sergey.spacegame.client.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.graphics.Color

/**
 * This component represents an entity that should be drawn as a line
 *
 * @author sergeys
 *
 * @constructor Creates a new LineVisualComponent
 *
 * @property x - the x coordinate of the first point
 * @property y - the y coordinate of the first point
 * @property x2 - the x coordinate of the second point
 * @property y2 - the y coordinate of the second point
 * @property thickness - the thickness of this line
 * @property color - a rgba8888 integer representation of the color of this line
 * @property life - the amount of time this line should exist for in seconds
 */
class LineVisualComponent(var x: Float, var y: Float, var x2: Float, var y2: Float, var thickness: Float,
                          var color: Int, var life: Float) : Component {
    
    var originalLife = life
    
    /**
     * Get the color of this line after giving it an opacity proportional to its life
     *
     * This will always return the same color object
     *
     * @return a Color object with the same color as the line but with the opacity set to the current life fraction
     */
    fun getColorWithOpacity(): Color = CLR.apply { set(color).a = life / originalLife }
    
    companion object Map {
        @JvmField
        val MAPPER = ComponentMapper.getFor(LineVisualComponent::class.java)!!
    
        private val CLR: Color = Color()
    }
}