package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.MoneyProducerComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.game.Level

class MoneyProducerSystem(val level: Level) : IntervalSystem(deltaTime.toFloat()) {
    
    private var perUpdatePlayer1: Double = 0.0
    private lateinit var player1Listener: EntityListener
    
    private var perUpdatePlayer2: Double = 0.0
    private lateinit var player2Listener: EntityListener
    
    override fun updateInterval() {
        level.player1.money += perUpdatePlayer1
        level.player2.money += perUpdatePlayer2
    }
    
    override fun addedToEngine(engine: Engine) {
        val player1Family = Family.all(MoneyProducerComponent::class.java, Team1Component::class.java).exclude(InContructionComponent::class.java).get()
        perUpdatePlayer1 = engine.getEntitiesFor(player1Family).sumByDouble { e -> MoneyProducerComponent.MAPPER.get(e).amount } * deltaTime
        engine.addEntityListener(player1Family, object : EntityListener {
            override fun entityAdded(entity: Entity) {
                perUpdatePlayer1 += MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
            }
        
            override fun entityRemoved(entity: Entity) {
                perUpdatePlayer1 -= MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
            }
        }.also { listener -> player1Listener = listener })
    
        val player2Family = Family.all(MoneyProducerComponent::class.java, Team2Component::class.java).exclude(InContructionComponent::class.java).get()
        perUpdatePlayer2 = engine.getEntitiesFor(player2Family).sumByDouble { e -> MoneyProducerComponent.MAPPER.get(e).amount } * deltaTime
        engine.addEntityListener(player2Family, object : EntityListener {
            override fun entityAdded(entity: Entity) {
                perUpdatePlayer2 += MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
            }
        
            override fun entityRemoved(entity: Entity) {
                perUpdatePlayer2 -= MoneyProducerComponent.MAPPER.get(entity).amount * deltaTime
            }
        }.also { listener -> player2Listener = listener })
    }
    
    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(player1Listener)
        engine.removeEntityListener(player2Listener)
    }
    
    private companion object {
        private val deltaTime: Double = 1 / 16.0 //Update 16 times a second
    }
}