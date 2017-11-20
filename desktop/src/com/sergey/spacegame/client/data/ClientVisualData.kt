package com.sergey.spacegame.client.data

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.sergey.spacegame.client.SpaceGameClient
import com.sergey.spacegame.common.data.VisualData

/**
 * @author sergeys
 */
class ClientVisualData(name: String) : VisualData {
    var region: TextureRegion? = SpaceGameClient.getRegion(name)
    
    var multColor = MULT_DEFAULT
    var addColor = ADD_DEFAULT
    
    companion object {
        @JvmField
        val MULT_DEFAULT = Color.toFloatBits(1f, 1f, 1f, 1f)
        @JvmField
        val ADD_DEFAULT = Color.toFloatBits(0f, 0f, 0f, 0f)
    }
}