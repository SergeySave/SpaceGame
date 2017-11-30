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
 * Represents a base weapon
 * It delegates firing to subclasses
 *
 * @author sergeys
 *
 * @constructor Creates a new ClientWeapon
 *
 * @param color - the rgba8888 color integer
 * @param thickness - the thickness of the line created by the weapon
 * @param reloadTime - the reload time of this weapon in seconds
 * @param range - the range of this weapon
 * @param damage - the damage of this weapon
 * @param accuracy - the accuracy of this weapon in the range [0,1]
 * @param life - the time that the line remains on screen in seconds
 */
abstract class Weapon(val color: Int, val thickness: Float, val reloadTime: Float, val range: Float,
                      val damage: Float, val accuracy: Float, val life: Float, val firingSound: AudioPlayData) {
    @Transient
    val range2 = range * range
    
    /**
     * Called to fire the weapon at a given target from a given position
     *
     * @param x - the x coordinate that the weapon is being fired from
     * @param y - the y coordinate that the weapon is being fired from
     * @param target - the target entity that the weapon is being fired at
     * @param level - the level in which the weapon was fired and the target is in
     */
    abstract fun fire(x: Float, y: Float, target: Entity, level: Level)
    
    /**
     * Represents a base serializer for all weapons
     */
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

