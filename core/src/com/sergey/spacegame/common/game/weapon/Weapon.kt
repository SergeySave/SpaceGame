package com.sergey.spacegame.common.game.weapon

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.common.data.AudioPlayData
import com.sergey.spacegame.common.game.Level
import java.lang.reflect.Type

/**
 * @author sergeys
 */
abstract class Weapon(val color: Int, val thickness: Float, val reloadTime: Float, val range: Float,
                      val damage: Float, val accuracy: Float, val life: Float, val firingSound: AudioPlayData) {
    @Transient
    val range2 = range * range
    
    abstract fun fire(x: Float, y: Float, target: Entity, level: Level)
    
    class Adapter : JsonSerializer<Weapon> {
        
        override fun serialize(src: Weapon, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject().apply {
                addProperty("color", Color(src.color).toString())
                addProperty("thickness", src.thickness)
                addProperty("reloadTime", src.reloadTime)
                addProperty("range", src.range)
                addProperty("damage", src.damage)
                addProperty("accuracy", src.accuracy)
                addProperty("life", src.life)
                add("sound", context.serialize(src.firingSound))
            }
            
            return jsonObject
        }
    }
    
}

