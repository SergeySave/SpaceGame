package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.ecs.component.WeaponComponent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.math.SpatialQuadtree
import com.sergey.spacegame.common.util.first //KotlinUtils

class WeaponSystem(
        val level: Level) : IteratingSystem((Family.all(WeaponComponent::class.java, PositionComponent::class.java).one(Team1Component::class.java, Team2Component::class.java).exclude(InContructionComponent::class.java).get())) {
    
    val TMP = Vector2()
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val weaponComponent = WeaponComponent.MAPPER.get(entity)
        val positionComponent = PositionComponent.MAPPER.get(entity)
        //Since it has to have one of Team1 or Team2 components if it doesnt have Team1 that means it must have Team2
        val quadtree = if (Team1Component.MAPPER.has(entity)) level.team2 else level.team1
        
        if (weaponComponent.target != null && PositionComponent.MAPPER.get(weaponComponent.target).setVector(TMP).dst2(positionComponent.x, positionComponent.y) > weaponComponent.maxRange2) {
            weaponComponent.target = null
        }
        
        for (i in weaponComponent.timers.indices) {
            if (weaponComponent.timers[i] > 0) {
                weaponComponent.timers[i] -= deltaTime
            }
            if (weaponComponent.target == null) {
                weaponComponent.target = quadtree.nearestHealthy(positionComponent.setVector(TMP), weaponComponent.maxRange)
                //If no targets skip the rest of the weapon system
                if (weaponComponent.target == null) return
            }
            while (weaponComponent.timers[i] <= 0 && weaponComponent.target != null) {
                if (HealthComponent.MAPPER.get(weaponComponent.target).health > 0) {
                    //Check if in range
                    if (PositionComponent.MAPPER.get(weaponComponent.target).setVector(TMP).dst2(positionComponent.x, positionComponent.y) <= weaponComponent.weapons[i].range2) {
                        //Fire
                        weaponComponent.timers[i] += weaponComponent.weapons[i].reloadTime
                        weaponComponent.weapons[i].fire(positionComponent.x, positionComponent.y, weaponComponent.target!!, level)
                
                        if (HealthComponent.MAPPER.get(weaponComponent.target).health <= 0) {
                            level.ecs.removeEntity(weaponComponent.target)
                            weaponComponent.target = quadtree.nearestHealthy(positionComponent.setVector(TMP), weaponComponent.maxRange)
                        }
                    } else {
                        //Target out of range of this weapon
                        //Try to find a closer target so this weapon can continue firing
                        val newClosest = quadtree.nearestHealthy(positionComponent.setVector(TMP), weaponComponent.weapons[i].range)
                        if (newClosest != null) {
                            weaponComponent.target = newClosest
                        } else {
                            break
                        }
                    }
                } else {
                    weaponComponent.target = quadtree.nearestHealthy(positionComponent.setVector(TMP), weaponComponent.maxRange)
                }
            }
        }
    }
    
    /**
     * This is inlined because i do not want to have the overhead of calling this function but i wanted to make it easy to edit
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun SpatialQuadtree<Entity>.nearestHealthy(pos: Vector2,
                                                              range: Float) = this.queryNearest(pos, range).first { e -> HealthComponent.MAPPER.has(e) && HealthComponent.MAPPER.get(e).health > 0f }
}