package com.sergey.spacegame.common.game.weapon

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.sergey.spacegame.common.data.AudioPlayData
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.util.test
import java.lang.reflect.Type

class ServerWeapon(color: Int, thickness: Float, reloadTime: Float, range: Float, damage: Float, accuracy: Float,
                   life: Float,
                   firingSound: AudioPlayData) : Weapon(color, thickness, reloadTime, range, damage, accuracy, life, firingSound) {
    override fun fire(x: Float, y: Float, target: Entity, level: Level) {
        level.random.test(accuracy) { success ->
            if (success) {
                HealthComponent.MAPPER.get(target).health -= damage
            }
            level.playSound(firingSound)
        }
    }
    
    class Adapter : JsonDeserializer<Weapon> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Weapon {
            return json.asJsonObject.run {
                ServerWeapon(Color.rgba8888(Color.valueOf(get("color").asString)),
                             get("thickness").asFloat,
                             get("reloadTime").asFloat,
                             get("range").asFloat,
                             get("damage").asFloat,
                             get("accuracy").asFloat,
                             get("life").asFloat,
                             context.deserialize(get("sound"), AudioPlayData::class.java)
                )
            }
        }
    }
}