package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.data.VisualData
import java.lang.reflect.Type

/**
 * This component contains the visual data
 *
 * @author sergeys
 *
 * @constructor Create a new VisualComponent object
 *
 * @param regionName - the name of the texture region
 */
class VisualComponent(regionName: String) : ClonableComponent {
    
    /**
     * The texture region's name
     */
    var regionName: String = regionName
        set(value) {
            visualData = SpaceGame.getInstance().context.createVisualData(value)
            field = value
        }
    
    /**
     * The visual data object used for client side rendering
     */
    var visualData: VisualData? = SpaceGame.getInstance().context.createVisualData(regionName)
        private set
    
    override fun copy(): Component {
        return VisualComponent(regionName)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(VisualComponent::class.java)!!
    }
    
    class Adapter : JsonSerializer<VisualComponent>, JsonDeserializer<VisualComponent> {
        
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type,
                                 context: JsonDeserializationContext): VisualComponent {
            val obj = json.asJsonObject
            
            return VisualComponent(obj.get("image").asString)
        }
        
        override fun serialize(src: VisualComponent, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()
            
            obj.addProperty("image", src.regionName)
            
            return obj
        }
    }
}