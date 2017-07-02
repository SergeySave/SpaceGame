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
import com.sergey.spacegame.common.ecs.component.IDComponent
import com.sergey.spacegame.common.game.Level

import java.lang.reflect.Type
import java.util.ArrayList

class EntityJsonAdapter : JsonSerializer<Entity>, JsonDeserializer<Entity> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Entity {
        val obj = json.asJsonObject
        val components = ArrayList<Component>()

        val entries = obj.entrySet()

        for ((className, value) in entries) {
            try {
                val clazz = ClassLoader.getSystemClassLoader().loadClass(className)
                components.add(context.deserialize<Component>(value, clazz))
            } catch (e: ClassNotFoundException) {
                throw JsonParseException("Class $className not found. ", e)
            }

        }

        val entity = Level.deserializing().ecs.newEntity()

        components.forEach { entity.add(it) }

        return entity
    }

    override fun serialize(src: Entity, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()

        src.components
                .filter { it !is IDComponent }
                .forEach { obj.add(it.javaClass.name, context.serialize(it)) }

        return obj
    }

}