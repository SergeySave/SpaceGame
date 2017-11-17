package com.sergey.spacegame.client.game.weapon

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.sergey.spacegame.client.ecs.component.LineVisualComponent
import com.sergey.spacegame.common.data.AudioPlayData
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.weapon.Weapon
import com.sergey.spacegame.common.util.test
import java.lang.reflect.Type

/**
 * @author sergeys
 */
class ClientWeapon(color: Int, thickness: Float, reloadTime: Float, range: Float, damage: Float, accuracy: Float,
                   life: Float,
                   firingSound: AudioPlayData) : Weapon(color, thickness, reloadTime, range, damage, accuracy, life, firingSound) {
    override fun fire(x: Float, y: Float, target: Entity, level: Level) {
        val positionComponent = PositionComponent.MAPPER.get(target)
        
        val entity = level.ecs.newEntity()
        
        level.random.test(accuracy) { success ->
            if (success) {
                HealthComponent.MAPPER.get(target).health -= damage
                entity.add(LineVisualComponent(x, y, positionComponent.x, positionComponent.y, thickness, color, life))
            } else {
                entity.add(LineVisualComponent(x, y, positionComponent.x + (level.random.nextFloat() - 0.5f) * 60, positionComponent.y + (level.random.nextFloat() - 0.5f) * 60, thickness, color, life))
            }
            level.playSound(firingSound)
        }
        
        level.ecs.addEntity(entity)
    }
    
    class Adapter : JsonDeserializer<Weapon> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Weapon {
            return json.asJsonObject.run {
                ClientWeapon(Color.rgba8888(Color.valueOf(get("color").asString)),
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