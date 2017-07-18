package com.sergey.spacegame.common.event

import com.badlogic.ashley.core.Entity
import com.google.gson.GsonBuilder
import com.sergey.spacegame.client.ecs.component.VisualComponent
import com.sergey.spacegame.common.ecs.EntityJsonAdapter
import com.sergey.spacegame.common.ecs.EntityPrototype
import com.sergey.spacegame.common.ecs.component.ControllableComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.ecs.component.WeaponComponent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.command.Command
import com.sergey.spacegame.common.game.weapon.Weapon


class BaseCommonEventHandler {
    
    @EventHandle
    fun onGsonRegister(event: GsonRegisterEvent) {
        val gson = event.gson
        gson.registerTypeAdapter(Command.Adapter())
        gson.registerTypeAdapter(EntityPrototype.Adapter())
        gson.registerTypeAdapter(VisualComponent.Adapter())
        gson.registerTypeAdapter(ControllableComponent.Adapter())
        gson.registerTypeAdapter(Level.Adapter())
        gson.registerTypeAdapter(Entity::class.java, EntityJsonAdapter())
        gson.registerTypeAdapter(Team1Component.Adapter())
        gson.registerTypeAdapter(Team2Component.Adapter())
        gson.registerTypeAdapter(WeaponComponent.Adapter())
        gson.registerTypeAdapter(Weapon.Adapter())
    }
    
    private fun GsonBuilder.registerTypeAdapter(adapter: Any) {
        this.registerTypeAdapter(adapter::class.java.enclosingClass, adapter)
    }
}