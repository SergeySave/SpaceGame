package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sergey.spacegame.client.SpaceGameClient
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.game.Level

/**
 * A class representing the Main Menu screen on the desktop client game
 *
 * @author sergeys
 *
 * @constructor Creates a new MainMenuScreen
 */
class MainMenuScreen : BaseScreen() {
    
    //The stage
    private lateinit var stage: Stage
    
    override fun show() {
        //Creates a new stage and attaches it to the input processor
        stage = Stage(ScreenViewport())
        SpaceGameClient.inputMultiplexer.addProcessor(stage)
        
        //A local copy of the UI skin
        val skin = SpaceGameClient.skin
        
        //Add the root table to the stage
        stage.addActor(Table().apply {
            setFillParent(true)
            defaults().also {
                it.padTop(Value.percentHeight(0.005f, this))
                it.padBottom(Value.percentHeight(0.005f, this))
                it.padLeft(Value.percentWidth(0.005f, this))
                it.padRight(Value.percentWidth(0.005f, this))
            }
            
            //Main menu image
            add(Image(SpaceGameClient.getRegion("screen/mainMenu_title")).apply {
                setScaling(Scaling.fit)
            }).grow().padTop(Value.percentHeight(0.01f, this))
            
            val loc = SpaceGameClient.localizations
            row()
            
            //Play game button
            add(TextButton(loc["mainmenu.button.play"], skin, "noBackgroundLarge").apply {
                pad(Value.percentHeight(0.1f))
                
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        SpaceGame.getInstance().screen = LevelSelectionScreen(
                                this@MainMenuScreen, "level.tutorial.name" to { Level.getLevelFromInternalPath("level.sgl") })
                    }
                })
            }).expandX().fillX()
            row()
            
            //Options button
            add(TextButton(loc["mainmenu.button.options"], skin, "noBackgroundLarge").apply {
                pad(Value.percentHeight(0.1f))
                
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        println("Options Button")
                    }
                })
            }).expandX().fillX()
            row()
            
            //Tall empty space
            add().expandY()
            row()
            
            //Exit button
            add(TextButton(loc["mainmenu.button.exit"], skin, "noBackgroundLarge").apply {
                pad(Value.percentHeight(0.1f))
                
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        Gdx.app.exit()
                    }
                })
            }).expandX().fillX().padBottom(Value.percentHeight(0.01f, this))
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
    
    override fun pause() {
    }
    
    override fun resume() {
    }
    
    override fun hide() {
        SpaceGameClient.inputMultiplexer.removeProcessor(stage)
    }
    
    override fun dispose() {
        stage.dispose()
    }
}