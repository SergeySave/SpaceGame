package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

abstract class BaseScreen : Screen {
	
	protected var textButtons:ArrayList<TextButton> = ArrayList()
			private set(v) {field = v}
		
	override open fun resize(width:Int, height:Int) {
		textButtons.forEach({ button -> 
			button.setStyle(button.getStyle())
			button.invalidateHierarchy()
		})
	}
	
	fun getWidth():Int {
		return Gdx.graphics.getWidth()
	}
	
	fun getHeight():Int {
		return Gdx.graphics.getHeight()
	}
}