package com.sergey.spacegame.client.event

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.sergey.spacegame.common.event.EventHandle


class BaseEventHandler {

    @EventHandle
    fun onAtlasRegistry(event: AtlasRegistryEvent) {
        event.packer.load("missingTexture");
        event.packer.load("building/factory");
        event.packer.load("icons/gotoarrow");
        event.packer.load("planets/1");
        event.packer.load("ships/pew");
    }

    private fun PixmapPacker.load(name: String) {
        this.pack(name, Pixmap(Gdx.files.internal("$name.png")))
    }
}