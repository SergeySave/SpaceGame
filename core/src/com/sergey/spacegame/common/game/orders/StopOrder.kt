package com.sergey.spacegame.common.game.orders

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent
import com.sergey.spacegame.common.ecs.component.VelocityComponent
import com.sergey.spacegame.common.game.Level

/**
 * @author sergeys
 */
class StopOrder : IOrder {
    
    private var done = false
    
    override fun update(e: Entity, deltaTime: Float, level: Level) {
        if (done) return
        
        e.remove(VelocityComponent::class.java)
        e.remove(RotationVelocityComponent::class.java)
        done = true
    }
    
    override fun isValidFor(
            e: Entity): Boolean = VelocityComponent.MAPPER.has(e) || RotationVelocityComponent.MAPPER.has(e)
    
    override fun completed(): Boolean = done
}