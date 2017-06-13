package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.BuildingComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.RotationComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent

class BuildingSystem : IteratingSystem {
	
	constructor() : super((Family.all(BuildingComponent::class.java, PositionComponent::class.java, SizeComponent::class.java).get()))
	
	override fun processEntity(entity:Entity, deltaTime:Float) {
		doSetBuildingPosition(entity)
	}
	
	companion object Helper {
		@JvmStatic
		fun doSetBuildingPosition(entity:Entity) {
			val building = BuildingComponent.MAPPER.get(entity)
			val pos = PositionComponent.MAPPER.get(entity)
			val size = SizeComponent.MAPPER.get(entity)
			val rot = if (RotationComponent.MAPPER.has(entity)) RotationComponent.MAPPER.get(entity) else null
			
			val planet = building.planet
			val planetPos = PositionComponent.MAPPER.get(planet)
			val planetSize = SizeComponent.MAPPER.get(planet)
			val rotatedBuildingVector = Vector2(1f,0f).rotate(building.position)
			pos.setFrom(planetPos.createVector().add(rotatedBuildingVector.cpy().scl(planetSize.w/2+size.w/3, planetSize.h/2+size.w/3)))
			rot?.r = rotatedBuildingVector.scl(planetSize.h/2, planetSize.w/2).angle() //Don't care about modifying rotatedBuildingVector here
		}
	}
}