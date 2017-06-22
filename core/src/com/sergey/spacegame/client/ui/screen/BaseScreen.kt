package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

abstract class BaseScreen : Screen {
	final fun getWidth():Int {
		return Gdx.graphics.getWidth()
	}
	
	final fun getHeight():Int {
		return Gdx.graphics.getHeight()
	}
}