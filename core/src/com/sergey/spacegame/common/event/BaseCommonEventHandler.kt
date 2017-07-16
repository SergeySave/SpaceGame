package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.client.ecs.component.VisualComponent
import com.sergey.spacegame.common.ecs.EntityJsonAdapter
import com.sergey.spacegame.common.ecs.EntityPrototype
import com.sergey.spacegame.common.ecs.component.ControllableComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.command.Command


class BaseCommonEventHandler {
    
    @EventHandle
    fun onGsonRegister(event: GsonRegisterEvent) {
        val gson = event.gson
        gson.registerTypeAdapter(Command::class.java, Command.Adapter())
        gson.registerTypeAdapter(EntityPrototype::class.java, EntityPrototype.Adapter())
        gson.registerTypeAdapter(VisualComponent::class.java, VisualComponent.Adapter())
        gson.registerTypeAdapter(ControllableComponent::class.java, ControllableComponent.Adapter())
        gson.registerTypeAdapter(Level::class.java, Level.Adapter())
        gson.registerTypeAdapter(Entity::class.java, EntityJsonAdapter())
        gson.registerTypeAdapter(Team1Component::class.java, Team1Component.Adapter())
        gson.registerTypeAdapter(Team2Component::class.java, Team2Component.Adapter())
    }
}