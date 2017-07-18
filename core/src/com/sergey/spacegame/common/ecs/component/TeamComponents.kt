package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.google.gson.InstanceCreator
import java.lang.reflect.Type

/**
 * @author sergeys
 *
 * The Team1Component is a singleton json loaded component
 */
class Team1Component : ClonableComponent {
    override fun copy(): Component = INSTANCE
    
    companion object {
        @JvmField
        val INSTANCE = Team1Component()
    }
    
    class Adapter : InstanceCreator<Team1Component> {
        override fun createInstance(type: Type): Team1Component = INSTANCE
    }
}

/**
 * @author sergeys
 *
 * The Team2Component is a singleton json loaded component
 */
class Team2Component : ClonableComponent {
    override fun copy(): Component = INSTANCE
    
    companion object {
        @JvmField
        val INSTANCE = Team2Component()
    }
    
    class Adapter : InstanceCreator<Team2Component> {
        override fun createInstance(type: Type): Team2Component = INSTANCE
    }
}