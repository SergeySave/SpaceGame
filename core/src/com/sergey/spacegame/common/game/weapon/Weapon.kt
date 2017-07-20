package com.sergey.spacegame.common.game.weapon

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
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
import com.sergey.spacegame.common.util.test //KotlinUtils.kt
import java.lang.reflect.Type

/**
 * @author sergeys
 */
class Weapon(val color: Int, val thickness: Float, val reloadTime: Float, val range: Float,
             val damage: Float, val accuracy: Float) {
    @Transient
    val range2 = range * range
    
    fun fire(x: Float, y: Float, target: Entity, level: Level) {
        val positionComponent = PositionComponent.MAPPER.get(target)
        
        val entity = level.ecs.newEntity()
        
        level.random.test(accuracy) { success ->
            if (success) {
                HealthComponent.MAPPER.get(target).health -= damage
                entity.add(LineVisualComponent(x, y, positionComponent.x, positionComponent.y, thickness, color, 0.25f))
            } else {
                entity.add(LineVisualComponent(x, y, positionComponent.x + (level.random.nextFloat() - 0.5f) * 60, positionComponent.y + (level.random.nextFloat() - 0.5f) * 60, thickness, color, 0.25f))
            }
        }
        
        level.ecs.addEntity(entity)
    }
    
    class Adapter : JsonSerializer<Weapon>, JsonDeserializer<Weapon> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Weapon {
            return json.asJsonObject.run {
                Weapon(Color.rgba8888(Color.valueOf(get("color").asString)),
                       get("thickness").asFloat,
                       get("reloadTime").asFloat,
                       get("range").asFloat,
                       get("damage").asFloat,
                       get("accuracy").asFloat
                )
            }
        }
        
        override fun serialize(src: Weapon, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject().apply {
                addProperty("color", Color(src.color).toString())
                addProperty("thickness", src.thickness)
                addProperty("reloadTime", src.reloadTime)
                addProperty("range", src.range)
                addProperty("damage", src.damage)
                addProperty("accuracy", src.accuracy)
            }
            
            return jsonObject
        }
    }
    
}
