package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class HealthComponent @JvmOverloads constructor(var health: Double = 0.0,
                                                var maxHealth: Double = health) : ClonableComponent {
    
    override fun copy(): Component {
        return HealthComponent(health, maxHealth)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(HealthComponent::class.java)!!
    }
}