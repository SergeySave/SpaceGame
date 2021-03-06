package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * This component represents a particle
 *
 * @author sergeys
 *
 * @constructor Create a new ParticleComponent
 *
 * @property endTime - the time at which this particle should be deleted
 */
class ParticleComponent(var endTime: Long) : Component {
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(ParticleComponent::class.java)!!
    }
}