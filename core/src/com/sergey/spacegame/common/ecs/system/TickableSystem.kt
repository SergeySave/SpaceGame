package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.TickableComponent
import com.sergey.spacegame.common.event.EntityTickEvent

class TickableSystem : IntervalSystem(0.05f) {
    
    private var entities: ImmutableArray<Entity>? = null
    private var tickBuilder = EntityTickEvent.Builder()
    private var tickNum: Int = 0
    
    override fun addedToEngine(engine: Engine?) {
        entities = engine!!.getEntitiesFor(Family.all(TickableComponent::class.java).exclude(InContructionComponent::class.java).get())
        tickNum = 0
    }
    
    override fun removedFromEngine(engine: Engine?) {
        entities = null
    }
    
    override fun updateInterval() {
        val entities = entities!!
        for (e in entities) {
            processEntity(e)
        }
        ++tickNum
    }
    
    fun processEntity(entity: Entity) {
        SpaceGame.getInstance().eventBus.post(tickBuilder.get(entity, TickableComponent.MAPPER.get(entity), tickNum))
    }
}