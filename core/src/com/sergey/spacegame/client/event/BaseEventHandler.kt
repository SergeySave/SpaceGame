package com.sergey.spacegame.client.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.sergey.spacegame.common.event.EventHandle


class BaseEventHandler {

    @EventHandle
    fun onAtlasRegistry(event: AtlasRegistryEvent) {
        event.packer.load("missingTexture")
    }

    private fun PixmapPacker.load(name: String) {
        this.pack(name, Pixmap(Gdx.files.internal("$name.png")))
    }
}