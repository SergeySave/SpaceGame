package com.sergey.spacegame.client.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.Color
import com.sergey.spacegame.client.ecs.component.SelectedComponent
import com.sergey.spacegame.client.ecs.component.VisualComponent
import com.sergey.spacegame.common.ecs.component.InContructionComponent

class VisualUpdateSystem : EntitySystem(2) {
    
    lateinit var selectedListener: EntityListener
    lateinit var inConstructionListener: EntityListener
    
    init {
        setProcessing(false)
    }
    
    override fun addedToEngine(engine: Engine) {
        selectedListener = Colorizer(SELECTED_MULT, SELECTED_ADD).also { listener ->
            engine.addEntityListener(Family.all(VisualComponent::class.java, SelectedComponent::class.java).get(), listener)
        }
        
        
        inConstructionListener = Colorizer(INCONSTRUCTION_MULT, INCONSTRUCTION_ADD).also { listener ->
            engine.addEntityListener(Family.all(VisualComponent::class.java, InContructionComponent::class.java).get(), listener)
        }
    }
    
    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(selectedListener)
        engine.removeEntityListener(inConstructionListener)
    }
    
    override fun update(deltaTime: Float) {
        //Should not run
    }
    
    private companion object {
        
        val SELECTED_MULT = Color.toFloatBits(0.5f, 0.5f, 0.5f, 1.0f)
        val SELECTED_ADD = Color.toFloatBits(0.5f, 0.5f, 0.5f, 0.0f)
        
        val INCONSTRUCTION_MULT = Color.toFloatBits(1f, 1f, 1f, 0.5f)
        val INCONSTRUCTION_ADD = Color.toFloatBits(0f, 0f, 0f, 0f)
    }
    
    private class Colorizer(val mult: Float, val add: Float) : EntityListener {
        override fun entityAdded(entity: Entity) {
            val visualComponent = VisualComponent.MAPPER.get(entity)
            visualComponent.multColor = mult
            visualComponent.addColor = add
        }
        
        override fun entityRemoved(entity: Entity) {
            val visualComponent = VisualComponent.MAPPER.get(entity)
            visualComponent.multColor = VisualComponent.MULT_DEFAULT
            visualComponent.addColor = VisualComponent.ADD_DEFAULT
        }
    }
}
