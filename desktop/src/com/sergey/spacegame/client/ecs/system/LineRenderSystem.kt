package com.sergey.spacegame.client.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.sergey.spacegame.client.ecs.component.LineVisualComponent
import com.sergey.spacegame.client.gl.DrawingBatch

/**
 * Represents the system for lines such as weapon fire
 *
 * @author sergeys
 *
 * @constructor Creates a new LineRenderSystem
 *
 * @property batch - the batch that this system will draw to
 */
class LineRenderSystem(private val batch: DrawingBatch) : EntitySystem(2) {
    
    private var entities: ImmutableArray<Entity>? = null
    
    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(LineVisualComponent::class.java).get())
    }
    
    override fun removedFromEngine(engine: Engine) {
        entities = null
    }
    
    override fun update(deltaTime: Float) {
        batch.enableBlending()
        
        var line: LineVisualComponent
        
        for (entity in entities!!) {
            line = LineVisualComponent.MAPPER.get(entity)
            
            batch.setLineWidth(line.thickness)
            batch.setForceColor(line.getColorWithOpacity())
            batch.line(line.x, line.y, line.x2, line.y2)
            
            line.life -= deltaTime
            if (line.life < 0)
                engine.removeEntity(entity)
        }
    }
}
