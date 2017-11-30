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
import com.sergey.spacegame.client.SpaceGameClient
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.game.Level

/**
 * Represents the level celection screen
 *
 * @author sergeys
 *
 * @constructor Creates a new LevelSelectionScreen
 * @property parent - The parent screen to this screen
 * @property levels - Pairs of level names to Level generators used both to display and create the levels
 */
class LevelSelectionScreen(private val parent: Screen,
                           private vararg val levels: Pair<String, () -> Level>) : BaseScreen() {
    
    private lateinit var stage: Stage
    
    override fun show() {
        stage = Stage(ScreenViewport())
        SpaceGameClient.inputMultiplexer.addProcessor(stage)
    
        val skin = SpaceGameClient.skin
    
        //Add the root table to the stage
        stage.addActor(Table().apply {
            val mainTable = this
            
            setFillParent(true)
            defaults().padTop(Value.percentHeight(0.005f, this))
            defaults().padBottom(Value.percentHeight(0.005f, this))
            defaults().padLeft(Value.percentWidth(0.005f, this))
            defaults().padRight(Value.percentWidth(0.005f, this))
    
            //Add the title to the screen
            add(Label("levelselect.name".localize(), skin, "big")).apply {
                align(Align.center)
                expandX()
                
                padTop(Value.percentHeight(0.05f, mainTable))
                padBottom(Value.percentHeight(0.05f, mainTable))
            }
            row()
    
            //Add a scroll pane containing a table
            add(ScrollPane(Table().apply {
                align(Align.left)
                
                defaults().padTop(Value.percentHeight(0.005f, mainTable))
                defaults().padBottom(Value.percentHeight(0.005f, mainTable))
    
                //Each item in the table is given by the level name
                for ((levelName, generatorFunc) in levels) {
                    
                    add(TextButton(levelName.localize(), skin, "noBackground")).apply {
                        val button = this.actor
    
                        expandX()
                        align(Align.left)
                        addListener(object : ChangeListener() {
                            override fun changed(event: ChangeEvent, actor: Actor) {
                                if (actor == button) {
                                    //When clicked we change the screen and dispose the main menu and level selection screens
                                    this@LevelSelectionScreen.parent.dispose()
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
    
            //Add a back button
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
        SpaceGameClient.inputMultiplexer.removeProcessor(stage)
    }
    
    override fun dispose() {
        stage.dispose()
    }
    
    private fun String.localize() = SpaceGameClient.localize(this)
}