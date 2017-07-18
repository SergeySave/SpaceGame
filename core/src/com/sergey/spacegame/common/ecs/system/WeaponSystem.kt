package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.ecs.component.WeaponComponent
import com.sergey.spacegame.common.game.Level

class WeaponSystem(
        val level: Level) : IteratingSystem((Family.all(WeaponComponent::class.java, PositionComponent::class.java).one(Team1Component::class.java, Team2Component::class.java).get())) {
    
    val TMP = Vector2()
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val weaponComponent = WeaponComponent.MAPPER.get(entity)
        val positionComponent = PositionComponent.MAPPER.get(entity)
        val team1 = Team1Component.MAPPER.has(entity) //Since it has to have one of Team1 or Team2 components if it doesnt have Team1 that means it must have Team2
        
        if (weaponComponent.target != null && PositionComponent.MAPPER.get(weaponComponent.target).setVector(TMP).dst2(positionComponent.x, positionComponent.y) > weaponComponent.maxRange2) {
            weaponComponent.target = null
        }
        
        for (i in weaponComponent.timers.indices) {
            if (weaponComponent.timers[i] > 0) {
                weaponComponent.timers[i] -= deltaTime
            } else {
                if (weaponComponent.target != null) {
                    if (HealthComponent.MAPPER.get(weaponComponent.target).health > 0 && PositionComponent.MAPPER.get(weaponComponent.target).setVector(TMP).dst2(positionComponent.x, positionComponent.y) <= weaponComponent.weapons[i].range2) {
                        weaponComponent.timers[i] += weaponComponent.weapons[i].reloadTime
                        weaponComponent.weapons[i].fire(positionComponent.x, positionComponent.y, weaponComponent.target!!, level)
                    }
                    if (HealthComponent.MAPPER.get(weaponComponent.target).health <= 0) {
                        level.ecs.removeEntity(weaponComponent.target)
                        weaponComponent.target = null
                    }
                } else {
                    //if (weaponComponent.target == null || HealthComponent.MAPPER.get(weaponComponent.target).health < 0) {
                    weaponComponent.target = (if (team1) level.team2 else level.team1).queryNearest(positionComponent.setVector(TMP), weaponComponent.maxRange).first(HealthComponent.MAPPER::has)
                    //}
                }
            }
        }
    }
    
    private fun <T> Iterator<T>.first(predicate: (T) -> Boolean): T? {
        while (hasNext()) {
            val obj = next()
            if (predicate(obj))
                return obj
        }
        return null
    }
}