package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.MoneyProducerComponent
import com.sergey.spacegame.common.game.Level

class MoneyProducerSystem(private val level: Level) : IntervalSystem(deltaTime.toFloat()), EntityListener {
    
    private var perUpdate: Double = 0.0
    
    override fun updateInterval() {
        level.money += perUpdate
    }
    
    override fun addedToEngine(engine: Engine) {
        val family = Family.all(MoneyProducerComponent::class.java).exclude(InContructionComponent::class.java).get()
        perUpdate = engine.getEntitiesFor(family).sumByDouble { e -> MoneyProducerComponent.MAPPER.get(e).amount } * deltaTime
        engine.addEntityListener(family, this)
    }
    
    override fun entityAdded(entity: Entity) {
        perUpdate += MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
    }
    
    override fun entityRemoved(entity: Entity) {
        perUpdate -= MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
    }
    
    private companion object {
        private val deltaTime: Double = 1 / 16.0 //Update 16 times a second
    }
}