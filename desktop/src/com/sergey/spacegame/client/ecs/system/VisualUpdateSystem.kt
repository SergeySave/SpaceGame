package com.sergey.spacegame.client.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.Color
import com.sergey.spacegame.client.data.ClientVisualData
import com.sergey.spacegame.client.ecs.component.SelectedComponent
import com.sergey.spacegame.common.ecs.component.InContructionComponent
import com.sergey.spacegame.common.ecs.component.VisualComponent

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
    
    override fun update(deltaTime: Float) = Unit
    
    private companion object {
        
        val SELECTED_MULT = Color.toFloatBits(0.5f, 0.5f, 0.5f, 1.0f)
        val SELECTED_ADD = Color.toFloatBits(0.5f, 0.5f, 0.5f, 0.0f)
        
        val INCONSTRUCTION_MULT = Color.toFloatBits(1f, 1f, 1f, 0.5f)
        val INCONSTRUCTION_ADD = Color.toFloatBits(0f, 0f, 0f, 0f)
    }
    
    private class Colorizer(val mult: Float, val add: Float) : EntityListener {
        override fun entityAdded(entity: Entity) {
            val visualData = VisualComponent.MAPPER.get(entity).visualData
            if (visualData is ClientVisualData) {
                visualData.multColor = mult
                visualData.addColor = add
            }
        }
        
        override fun entityRemoved(entity: Entity) {
            val visualData = VisualComponent.MAPPER.get(entity).visualData
            if (visualData is ClientVisualData) {
                visualData.multColor = ClientVisualData.MULT_DEFAULT
                visualData.addColor = ClientVisualData.ADD_DEFAULT
            }
        }
    }
}
