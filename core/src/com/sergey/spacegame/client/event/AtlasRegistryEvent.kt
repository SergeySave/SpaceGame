package com.sergey.spacegame.client.event

import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.sergey.spacegame.common.event.Event

class AtlasRegistryEvent(val packer: PixmapPacker) : Event()