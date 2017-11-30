package com.sergey.spacegame.common.game.orders

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent
import com.sergey.spacegame.common.ecs.component.VelocityComponent
import com.sergey.spacegame.common.game.Level

/**
 * Represents a order to stop moving
 *
 * @author sergeys
 *
 * @constructor Creates a new StopOrder
 */
object StopOrder : IOrder {
    
    override fun update(e: Entity, deltaTime: Float, level: Level) {
        if (!isValidFor(e)) return
        
        e.remove(VelocityComponent::class.java)
        e.remove(RotationVelocityComponent::class.java)
    }
    
    override fun isValidFor(
            e: Entity): Boolean = VelocityComponent.MAPPER.has(e) || RotationVelocityComponent.MAPPER.has(e)
    
    override fun completed(e: Entity): Boolean = !isValidFor(e)
}