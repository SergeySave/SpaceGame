package com.sergey.spacegame.client.event

import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.sergey.spacegame.common.event.Event

/**
 * Represents a registry event for registering images onto the texture atlas
 *
 * @author sergeys
 *
 * @constructor Creates a new AtlasRegistrEvent
 *
 * @property packer - the packer for the atlas that images should be added to
 */
class AtlasRegistryEvent(val packer: PixmapPacker) : Event()

/**
 * Represents a registry event for registering localizations into the localization map
 *
 * @author sergeys
 *
 * @constructor Creates a new LocalizationRegistryEvent
 *
 * @property localizationMap - the map of unlocalized names to their localized forms
 * @property locale - the locale whose localizations to add to the map
 */
class LocalizationRegistryEvent(val localizationMap: MutableMap<String, String>, val locale: String) : Event()