package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen

/**
 * This class is mostly useless but it acts as a base for other screens
 *
 * @author sergeys
 *
 * @constructor Creates a new BaseScreen
 */
abstract class BaseScreen : Screen {
    fun getWidth(): Int {
        return Gdx.graphics.width
    }
    
    fun getHeight(): Int {
        return Gdx.graphics.height
    }
}