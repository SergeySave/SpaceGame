package com.sergey.spacegame.client.data

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.sergey.spacegame.client.SpaceGameClient
import com.sergey.spacegame.common.data.VisualData

/**
 * This class represents the visual data used on the desktop client side
 *
 * @author sergeys
 *
 * @constructor Create a new ClientVisualData object
 *
 * @param name - the name of the region used by this visual data
 */
class ClientVisualData(name: String) : VisualData {
    /**
     * The texture region of the thing using this visual data
     */
    var region: TextureRegion = SpaceGameClient.getRegion(name)
    
    /**
     * The multiplicative tint color
     */
    var multColor = MULT_DEFAULT
    /**
     * The additive tint color
     */
    var addColor = ADD_DEFAULT
    
    companion object {
        @JvmField
        val MULT_DEFAULT = Color.toFloatBits(1f, 1f, 1f, 1f)
        @JvmField
        val ADD_DEFAULT = Color.toFloatBits(0f, 0f, 0f, 0f)
    }
}