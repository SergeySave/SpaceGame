package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

data class BuildingComponent(var planet:Entity, var position:Float) : ClonableComponent {
	companion object Mapping {
		@JvmField
		val MAPPER = ComponentMapper.getFor(BuildingComponent::class.java)
	}
	
	override fun copy():Component {
		return BuildingComponent(this.planet, this.position);
	}
}