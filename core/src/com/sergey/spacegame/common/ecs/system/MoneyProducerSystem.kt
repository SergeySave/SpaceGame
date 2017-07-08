package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.MoneyProducerComponent
import com.sergey.spacegame.common.game.Level

class MoneyProducerSystem(private val level: Level) : IntervalSystem(1f), EntityListener {
    
    private var perSecond: Double = 0.0
    
    override fun updateInterval() {
        level.money += perSecond
    }
    
    override fun addedToEngine(engine: Engine) {
        val family = Family.all(MoneyProducerComponent::class.java).exclude(InContructionComponent::class.java).get()
        perSecond = engine.getEntitiesFor(family).sumByDouble { e -> MoneyProducerComponent.MAPPER.get(e).amount }
        engine.addEntityListener(family, this)
    }
    
    override fun entityAdded(entity: Entity) {
        perSecond += MoneyProducerComponent.MAPPER.get(entity).amount
    }
    
    override fun entityRemoved(entity: Entity) {
        perSecond -= MoneyProducerComponent.MAPPER.get(entity).amount
    }
}