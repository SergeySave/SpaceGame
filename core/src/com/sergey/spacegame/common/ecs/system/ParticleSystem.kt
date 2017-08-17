package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.sergey.spacegame.common.ecs.component.ParticleComponent
import com.sergey.spacegame.common.game.Level

class ParticleSystem(val level: Level) : IteratingSystem((Family.all(ParticleComponent::class.java).get())) {
    private var time = 0L
    
    override fun update(deltaTime: Float) {
        time = System.currentTimeMillis()
        super.update(deltaTime)
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (ParticleComponent.MAPPER.get(entity).endTime < time) {
            level.ecs.removeEntity(entity)
        }
    }
}