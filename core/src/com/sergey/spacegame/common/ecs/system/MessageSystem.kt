package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.sergey.spacegame.common.ecs.component.MessageComponent
import java.util.PriorityQueue

/**
 * This system is in charge of updating message entities
 *
 * @author sergeys
 *
 * @constructor Create a new MessageSystem
 */
class MessageSystem : EntitySystem(), EntityListener {
    
    val queue = PriorityQueue<Entity> { e1, e2 -> MessageComponent.MAPPER.get(e1).endTime.compareTo(MessageComponent.MAPPER.get(e2).endTime) }
    
    override fun addedToEngine(engine: Engine) {
        engine.addEntityListener(Family.all(MessageComponent::class.java).get(), this)
    }
    
    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(this)
    }
    
    override fun update(deltaTime: Float) {
        while (queue.isNotEmpty() && MessageComponent.MAPPER.get(queue.peek()).endTime <= System.currentTimeMillis()) {
            engine.removeEntity(queue.poll())
        }
    }
    
    override fun entityAdded(entity: Entity) {
        queue.add(entity)
    }
    
    override fun entityRemoved(entity: Entity) {
        queue.remove(entity)
    }
}