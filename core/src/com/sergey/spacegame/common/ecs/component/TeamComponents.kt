package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.google.gson.InstanceCreator
import java.lang.reflect.Type

/**
 * The Team1Component is a singleton json loaded component
 *
 * @author sergeys
 */
object Team1Component : ClonableComponent {
    override fun copy(): Component = Team1Component
    
    @JvmField
    val MAPPER = ComponentMapper.getFor(Team1Component::class.java)!!
    
    class Adapter : InstanceCreator<Team1Component> {
        override fun createInstance(type: Type): Team1Component = Team1Component
    }
}

/**
 * The Team2Component is a singleton json loaded component
 *
 * @author sergeys
 */
object Team2Component : ClonableComponent {
    override fun copy(): Component = Team2Component
    
    @JvmField
    val MAPPER = ComponentMapper.getFor(Team2Component::class.java)!!
    
    class Adapter : InstanceCreator<Team2Component> {
        override fun createInstance(type: Type): Team2Component = Team2Component
    }
}