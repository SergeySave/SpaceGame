package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sergey.spacegame.SpaceGame
import com.sergey.spacegame.common.game.Level

/**
 * @author sergeys
 */
class LevelSelectionScreen(private val parent: Screen,
                           private vararg val levels: Pair<String, () -> Level>) : BaseScreen() {
    
    private lateinit var stage: Stage
    
    override fun show() {
        stage = Stage(ScreenViewport())
        
        SpaceGame.getInstance().inputMultiplexer.addProcessor(stage)
        
        val skin = SpaceGame.getInstance().skin
        
        stage.addActor(Table().apply {
            val mainTable = this
            
            setFillParent(true)
            
            defaults().padTop(Value.percentHeight(0.005f, this))
            defaults().padBottom(Value.percentHeight(0.005f, this))
            defaults().padLeft(Value.percentWidth(0.005f, this))
            defaults().padRight(Value.percentWidth(0.005f, this))
            
            add(Label("levelselect.name".localize(), skin, "big")).apply {
                align(Align.center)
                expandX()
                
                padTop(Value.percentHeight(0.05f, mainTable))
                padBottom(Value.percentHeight(0.05f, mainTable))
            }
            
            row()
            
            add(ScrollPane(Table().apply {
                align(Align.left)
                
                defaults().padTop(Value.percentHeight(0.005f, mainTable))
                defaults().padBottom(Value.percentHeight(0.005f, mainTable))
                
                for ((levelName, generatorFunc) in levels) {
                    
                    add(TextButton(levelName.localize(), skin, "noBackground")).apply {
                        val button = this.actor
    
                        expandX()
                        align(Align.left)
                        addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                if (actor == button) {
                                    SpaceGame.getInstance().setScreenAndDisposeOld(GameScreen(generatorFunc()))
                                }
                            }
                        })
                    }
                    
                    row()
                }
            }, skin, "noBg")).apply {
                expand()
                align(Align.topLeft)
                
                padLeft(Value.percentWidth(0.025f, mainTable))
            }
            
            row()
            
            add(TextButton("common.button.back".localize(), skin, "noBackground")).apply {
                val button = this.actor
    
                align(Align.left)
                expandX()
    
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        if (actor == button) {
                            SpaceGame.getInstance().setScreenAndDisposeOld(this@LevelSelectionScreen.parent)
                        }
                    }
                })
            }
        })
        
        //stage.setDebugAll(true)
    }
    
    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }
    
    override fun resize(width: Int, height: Int) {
        hide()
        dispose()
        show()
    }
    
    override fun pause() {}
    
    override fun resume() {}
    
    override fun hide() {
        SpaceGame.getInstance().inputMultiplexer.removeProcessor(stage)
    }
    
    override fun dispose() {
        stage.dispose()
    }
    
    private fun String.localize() = SpaceGame.getInstance().localize(this)
}