package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.BuildingComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.RotationComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent

/**
 * This system is in charge of updating building positions
 *
 * @author sergeys
 *
 * @constructor Creates a new BuildingSystem
 */
class BuildingSystem : IteratingSystem((Family.all(BuildingComponent::class.java, PositionComponent::class.java, SizeComponent::class.java).get())) {
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val building = BuildingComponent.MAPPER.get(entity)
        doSetBuildingPosition(entity, building.planet, building.position)
    }
    
    companion object Helper {
        @JvmStatic
        fun doSetBuildingPosition(entity: Entity, planet: Entity, position: Float) {
            val pos = PositionComponent.MAPPER.get(entity) ?: PositionComponent().apply { entity.add(this) }
            val size = SizeComponent.MAPPER.get(entity)
            val rot = RotationComponent.MAPPER.get(entity)
            
            val planetPos = PositionComponent.MAPPER.get(planet)
            val planetSize = SizeComponent.MAPPER.get(planet)
            val rotatedBuildingVector = Vector2(1f, 0f).rotate(position + (RotationComponent.MAPPER.get(planet)?.r ?: 0f))
            pos.setFrom(planetPos.createVector().add(rotatedBuildingVector.cpy().scl(planetSize.w / 2 + size.w / 3, planetSize.h / 2 + size.w / 3)))
            rot?.r = rotatedBuildingVector.scl(planetSize.h, planetSize.w).angle() //Don't care about modifying rotatedBuildingVector here
        }
    }
}