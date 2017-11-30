package com.sergey.spacegame.common.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.game.Level
import java.lang.reflect.Type
import java.util.ArrayList

/**
 * This class is a JSON serializer and deserializer for entities
 *
 * @author
 *
 * @constructor Create a new EntityJsonAdapter
 */
class EntityJsonAdapter : JsonSerializer<Entity>, JsonDeserializer<Entity> {
    
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Entity {
        val obj = json.asJsonObject
        val components = ArrayList<Component>()
        
        val entries = obj.entrySet()
        
        var hasTeam = false
        var hasHealth = false
        
        for ((className, value) in entries) {
            try {
                val clazz = ClassLoader.getSystemClassLoader().loadClass(className)
                val component = context.deserialize<Component>(value, clazz)
                
                when (component) {
                    is HealthComponent -> hasHealth = true
                    is Team1Component  -> hasTeam = true
                    is Team2Component  -> hasTeam = true
                }
                
                components.add(component)
            } catch (e: ClassNotFoundException) {
                throw JsonParseException("Class $className not found. ", e)
            }
            
        }
        
        if (hasHealth xor hasTeam) {
            System.err.println("Entity has only one of HealthComponent and TeamComponent")
        }
        
        val entity = Level.deserializing().ecs.newEntity()
        
        components.forEach { entity.add(it) }
        
        return entity
    }
    
    override fun serialize(src: Entity, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        
        src.components.forEach { obj.add(it.javaClass.name, context.serialize(it)) }
        
        return obj
    }
    
}