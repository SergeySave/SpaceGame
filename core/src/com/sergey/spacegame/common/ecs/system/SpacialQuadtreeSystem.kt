package com.sergey.spacegame.common.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.game.Level

/**
 * @author sergeys
 *
 * AKA. PositionStstem
 *
 */
class SpacialQuadtreeSystem(private val level: Level) : EntitySystem(), EntityListener {
    override fun entityAdded(entity: Entity) {
        PositionComponent.MAPPER.get(entity).setDirty()
    }
    
    override fun entityRemoved(entity: Entity) {
        //PositionComponent.MAPPER.get(entity).setDirty()
    }
    
    private val TMP = Vector2()
    private var team1: ImmutableArray<Entity>? = null
    private var team2: ImmutableArray<Entity>? = null
    
    override fun addedToEngine(engine: Engine) {
        team1 = engine.getEntitiesFor(Family.all(PositionComponent::class.java, Team1Component::class.java).get())
        team2 = engine.getEntitiesFor(Family.all(PositionComponent::class.java, Team2Component::class.java).get())
        
        engine.addEntityListener(Family.all(PositionComponent::class.java).get(), this)
    }
    
    override fun removedFromEngine(engine: Engine) {
        team1 = null
        team2 = null
        
        engine.removeEntityListener(this)
    }
    
    override fun update(deltaTime: Float) {
        val team1 = this.team1
        val team2 = this.team2
        if (team1 == null || team2 == null) return
    
        val levelLimits = level.limits
        
        team1.forEach { entity ->
            val position = PositionComponent.MAPPER.get(entity)
            if (position.isDirty) {
    
                position.clamp(levelLimits.minX, levelLimits.maxX, levelLimits.minY, levelLimits.maxY)
                
                TMP.set(position.oldX, position.oldY)
                level.team1.remove(entity, TMP)
                
                position.setVector(TMP)
                level.team1.put(entity, TMP.cpy())
                
                position.setNotDirty()
            }
        }
        
        team2.forEach { entity ->
            val position = PositionComponent.MAPPER.get(entity)
            if (position.isDirty) {
    
                position.clamp(levelLimits.minX, levelLimits.maxX, levelLimits.minY, levelLimits.maxY)

                TMP.set(position.oldX, position.oldY)
                level.team2.remove(entity, TMP)
                
                position.setVector(TMP)
                level.team2.put(entity, TMP.cpy())
                
                position.setNotDirty()
            }
        }
    }
}