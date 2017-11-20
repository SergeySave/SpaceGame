package com.sergey.spacegame.client.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.google.gson.GsonBuilder
import com.sergey.spacegame.client.game.command.ClientCommand
import com.sergey.spacegame.client.game.weapon.ClientWeapon
import com.sergey.spacegame.common.event.EventHandle
import com.sergey.spacegame.common.event.GsonRegisterEvent
import com.sergey.spacegame.common.game.command.Command
import com.sergey.spacegame.common.game.weapon.Weapon


class BaseClientEventHandler {
    
    @EventHandle
    fun onAtlasRegistry(event: AtlasRegistryEvent) {
        event.packer.load("missingTexture")
        event.packer.load("radialBar")
        event.packer.load("team1")
        event.packer.load("team2")
        event.packer.load("neutral")
        event.packer.load("whitePixel")
        event.packer.load("uncolored")
        event.packer.load("upArrow")
        event.packer.load("downArrow")
    }
    
    @EventHandle
    fun onLocalizationRegistry(event: LocalizationRegistryEvent) {
        Gdx.files.internal("localization/${event.locale}.loc").readString().split("\n")
                .filter { s -> !s.startsWith("#") && s.matches("([^=]+)=([^=]+)".toRegex()) }
                .forEach { s ->
                    val parts = s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    event.localizationMap.put(parts[0], parts[1])
                }
    }
    
    private fun PixmapPacker.load(name: String) {
        this.pack(name, Pixmap(Gdx.files.internal("$name.png")))
    }
    
    
    @EventHandle
    fun onGsonRegister(event: GsonRegisterEvent) {
        val gson = event.gson
        gson.registerTypeAdapter(Weapon::class.java, ClientWeapon.Adapter())
        gson.registerTypeAdapter(Command::class.java, ClientCommand.Adapter())
    }
    
    private fun GsonBuilder.registerTypeAdapter(adapter: Any) {
        this.registerTypeAdapter(adapter::class.java.enclosingClass, adapter)
    }
    
}