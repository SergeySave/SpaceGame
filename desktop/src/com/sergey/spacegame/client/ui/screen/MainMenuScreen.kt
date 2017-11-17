package com.sergey.spacegame.client.ui.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sergey.spacegame.client.SpaceGameClient
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.game.Level

class MainMenuScreen : BaseScreen() {
    
    private lateinit var stage: Stage
    
    private var needsValidation: ArrayList<Layout> = ArrayList()
    
    override fun show() {
        stage = Stage(ScreenViewport())
    
        SpaceGameClient.inputMultiplexer.addProcessor(stage)
    
        val skin = SpaceGameClient.skin
        
        val table = Table()
        table.setFillParent(true)
        //table.setDebug(true)
        stage.addActor(table)
        table.defaults().padTop(Value.percentHeight(0.005f, table))
        table.defaults().padBottom(Value.percentHeight(0.005f, table))
        table.defaults().padLeft(Value.percentWidth(0.005f, table))
        table.defaults().padRight(Value.percentWidth(0.005f, table))
    
        val image = Image(SpaceGameClient.getRegion("screen/mainMenu_title"))
        image.setScaling(Scaling.fit)
        table.add(image).grow().padTop(Value.percentHeight(0.01f, table))
    
        val loc = SpaceGameClient.localizations
    
        val play = TextButton(loc["mainmenu.button.play"], skin, "noBackgroundLarge")
        play.pad(Value.percentHeight(0.1f))
        play.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                SpaceGame.getInstance().screen = LevelSelectionScreen(
                        this@MainMenuScreen, "level.tutorial.name" to { Level.getLevelFromInternalPath("level.sgl") })
            }
        })
        table.row()
        table.add(play).expandX().fillX()
    
        val options = TextButton(loc["mainmenu.button.options"], skin, "noBackgroundLarge")
        options.pad(Value.percentHeight(0.1f))
        options.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                println("Options Button")
            }
        })
        table.row()
        table.add(options).expandX().fillX()
        
        table.row()
        table.add().expandY()
        
        table.row()
        val exit = TextButton(loc["mainmenu.button.exit"], skin, "noBackgroundLarge")
        exit.pad(Value.percentHeight(0.1f))
        exit.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                Gdx.app.exit()
            }
        })
        table.add(exit).expandX().fillX().padBottom(Value.percentHeight(0.01f, table))
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