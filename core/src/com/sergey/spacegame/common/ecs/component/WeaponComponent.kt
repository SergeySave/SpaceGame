package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.sergey.spacegame.common.game.weapon.Weapon
import java.lang.reflect.Type

/**
 * @author sergeys
 */
class WeaponComponent(val weapons: List<Weapon>, @Transient val maxRange: Float) : ClonableComponent {
    
    @Transient
    val timers = FloatArray(weapons.size) { 0f } // Set all timers to 0 by default
    
    @Transient
    var target: Entity? = null
    
    @Transient
    var maxRange2 = maxRange * maxRange
    
    override fun copy(): Component {
        return WeaponComponent(weapons, maxRange)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(WeaponComponent::class.java)!!
    }
    
    class Adapter : JsonSerializer<WeaponComponent>, JsonDeserializer<WeaponComponent> {
        override fun deserialize(json: JsonElement, typeOfT: Type,
                                 context: JsonDeserializationContext): WeaponComponent {
            val jsonArray = json.asJsonArray
            
            val weapons = mutableListOf<Weapon>()
            var maxRange = 0f
            
            jsonArray.forEach { element ->
                val jsonObject = element.asJsonObject
                val weapon = context.deserialize<Weapon>(jsonObject, Weapon::class.java)
                
                maxRange = maxOf(maxRange, weapon.range)
                
                weapons.add(weapon)
            }
            
            return WeaponComponent(weapons, maxRange)
        }
        
        override fun serialize(src: WeaponComponent, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonArray = JsonArray()
            
            src.weapons.forEach { weapon ->
                jsonArray.add(context.serialize(weapon))
            }
            
            return jsonArray
        }
    }
}