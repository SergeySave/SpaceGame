package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen

abstract class BaseScreen : Screen {
    fun getWidth(): Int {
        return Gdx.graphics.width
    }
    
    fun getHeight(): Int {
        return Gdx.graphics.height
    }
}