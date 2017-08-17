package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class ParticleComponent(var endTime: Long) : Component {
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(ParticleComponent::class.java)!!
    }
}