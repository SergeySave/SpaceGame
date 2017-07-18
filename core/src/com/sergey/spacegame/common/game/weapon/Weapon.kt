package com.sergey.spacegame.common.game.weapon

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.NumberUtils
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.client.ecs.component.LineVisualComponent
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.game.Level
import java.lang.reflect.Type

/**
 * @author sergeys
 */
class Weapon(val color: Float, val thickness: Float, val reloadTime: Float, val range: Float,
             val damage: Float) {
    @Transient
    val range2 = range * range
    
    fun fire(x: Float, y: Float, target: Entity, level: Level) {
        val positionComponent = PositionComponent.MAPPER.get(target)
        
        val entity = level.ecs.newEntity()
        
        entity.add(LineVisualComponent(x, y, positionComponent.x, positionComponent.y, thickness, color, 0.25f))
        HealthComponent.MAPPER.get(target).health -= damage
        
        level.ecs.addEntity(entity)
    }
    
    class Adapter : JsonSerializer<Weapon>, JsonDeserializer<Weapon> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Weapon {
            return json.asJsonObject.run {
                Weapon(Color.valueOf(get("color").asString).toFloatBits(),
                       get("thickness").asFloat,
                       get("reloadTime").asFloat,
                       get("range").asFloat,
                       get("damage").asFloat)
            }
        }
        
        override fun serialize(src: Weapon, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject().apply {
                addProperty("color", Color(NumberUtils.floatToIntColor(src.color)).toString())
                addProperty("thickness", src.thickness)
                addProperty("reloadTime", src.reloadTime)
                addProperty("range", src.range)
                addProperty("damage", src.damage)
            }
            
            return jsonObject
        }
    }
    
}
