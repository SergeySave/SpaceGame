package com.sergey.spacegame.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.sergey.spacegame.client.event.AtlasRegistryEvent
import com.sergey.spacegame.client.event.BaseClientEventHandler
import com.sergey.spacegame.client.event.LocalizationRegistryEvent
import com.sergey.spacegame.client.game.LevelEventRegistry
import com.sergey.spacegame.client.ui.BitmapFontWrapper
import com.sergey.spacegame.client.ui.screen.LoadingScreen
import com.sergey.spacegame.client.ui.screen.MainMenuScreen
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.SpaceGameContext
import java.nio.file.FileSystem
import java.util.HashMap

/**
 * @author sergeys
 */
object SpaceGameClient : SpaceGameContext {
    
    private val MIN_FONT_SIZE: Int = 3
    
    private lateinit var smallFont: BitmapFontWrapper
    private lateinit var mediumFont: BitmapFontWrapper
    private lateinit var largeFont: BitmapFontWrapper
    
    lateinit var inputMultiplexer: InputMultiplexer
        private set
    var atlas: TextureAtlas? = null
        private set
    var skin: Skin? = null
        private set
    private var fontGenerator: FreeTypeFontGenerator? = null
    
    var localizations = HashMap<String, String>()
        private set
    
    override fun preload() {
        SpaceGame.getInstance().screen = LoadingScreen()
    }
    
    override fun load() {
        SpaceGame.getInstance().eventBus.registerAnnotated(BaseClientEventHandler())
        
        regenerateAtlas()
        reloadLocalizations()
        
        inputMultiplexer = InputMultiplexer()
        Gdx.input.inputProcessor = inputMultiplexer
        
        Gdx.app.postRunnable { skin = Skin(TextureAtlas(Gdx.files.internal("scene2d/uiskin.atlas"))) }
        Gdx.app.postRunnable { skin!!.addRegions(atlas) }
        
        fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("font/Helvetica.ttf"))
        generateFonts()
        
        Gdx.app.postRunnable { skin!!.load(Gdx.files.internal("scene2d/uiskin.json")) }
    }
    
    override fun postload() {
        SpaceGame.getInstance().setScreenAndDisposeOld(MainMenuScreen())
    }
    
    override fun reload() {
        regenerateAtlasNow()
        reloadLocalizations()
    }
    
    override fun createLevelEventHandler(fileSystem: FileSystem): Any = LevelEventRegistry(fileSystem)
    
    override fun resize(width: Int, height: Int) {
        val fontGenerator = fontGenerator
        if (fontGenerator != null) {
            run {
                val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
                parameter.size = Math.max((16 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                parameter.borderWidth = 0.005f
                parameter.borderColor = Color.BLACK
                parameter.color = Color.WHITE
                
                val font = fontGenerator.generateFont(parameter)
                smallFont.setWrapped(font).dispose()
            }
            run {
                val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
                parameter.size = Math.max((24 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                parameter.borderWidth = 0.005f
                parameter.borderColor = Color.BLACK
                parameter.color = Color.WHITE
                
                val font = fontGenerator.generateFont(parameter)
                mediumFont.setWrapped(font).dispose()
            }
            run {
                val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
                parameter.size = Math.max((32 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                parameter.borderWidth = 0.005f
                parameter.borderColor = Color.BLACK
                parameter.color = Color.WHITE
                
                val font = fontGenerator.generateFont(parameter)
                largeFont.setWrapped(font).dispose()
            }
        }
    }
    
    override fun dispose() {
        if (atlas != null) atlas!!.dispose()
        if (skin != null) skin!!.dispose()
        if (fontGenerator != null) fontGenerator!!.dispose()
        
    }
    
    override fun getRegion(name: String): TextureAtlas.AtlasRegion {
        val region = atlas!!.findRegion(name)
        return region ?: atlas!!.findRegion("missingTexture")
    }
    
    private fun generateFonts() {
        val fontGenerator = fontGenerator!!
        Gdx.app.postRunnable {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
            parameter.size = Math.max((16 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
            parameter.borderWidth = 0.005f
            parameter.borderColor = Color.BLACK
            parameter.color = Color.WHITE
            
            val font = fontGenerator.generateFont(parameter)
            smallFont = BitmapFontWrapper(font)
            skin!!.add("font_small", smallFont, BitmapFont::class.java)
        }
        Gdx.app.postRunnable {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
            parameter.size = Math.max((24 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
            parameter.borderWidth = 0.005f
            parameter.borderColor = Color.BLACK
            parameter.color = Color.WHITE
            
            val font = fontGenerator.generateFont(parameter)
            mediumFont = BitmapFontWrapper(font)
            skin!!.add("font_medium", mediumFont, BitmapFont::class.java)
        }
        Gdx.app.postRunnable {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
            parameter.size = Math.max((32 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
            parameter.borderWidth = 0.005f
            parameter.borderColor = Color.BLACK
            parameter.color = Color.WHITE
            
            val font = fontGenerator.generateFont(parameter)
            largeFont = BitmapFontWrapper(font)
            skin!!.add("font_large", largeFont, BitmapFont::class.java)
        }
    }
    
    
    fun regenerateAtlas() {
        val packer = PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 0, false, PixmapPacker.SkylineStrategy())
        SpaceGame.getInstance().eventBus.post(AtlasRegistryEvent(packer))
        
        Gdx.app.postRunnable {
            //unload current atlas
            var atlas = atlas
            if (atlas != null) {
                val regions = atlas.regions
                var i = 0
                val n = regions.size
                while (i < n) {
                    val region = regions.get(i)
                    var name = region.name
                    if (region.index != -1) {
                        name += "_" + region.index
                    }
                    skin!!.remove(name, TextureRegion::class.java)
                    i++
                }
                atlas.dispose()
            }
            
            //load new atlas
            atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear, true)
            skin?.addRegions(atlas)
            
            Gdx.files.local("atlas").deleteDirectory()
            val pages = packer.pages
            for (i in 0 until pages.size) {
                val page = pages.get(i)
                PixmapIO.writePNG(Gdx.files.local("atlas/$i.png"), page.pixmap)
            }
            
            this.atlas = atlas
            packer.dispose()
        }
    }
    
    fun reloadLocalizations() {
        val localization = HashMap<String, String>()
        
        val localizationRegistryEvent = LocalizationRegistryEvent(localization, "en_US")
        SpaceGame.getInstance().eventBus.post(localizationRegistryEvent)
        
        this.localizations = localization
    }
    
    fun regenerateAtlasNow() {
        val packer = PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 0, false, PixmapPacker.SkylineStrategy())
        SpaceGame.getInstance().eventBus.post(AtlasRegistryEvent(packer))
        
        //unload current atlas
        var atlas = atlas
        if (atlas != null) {
            val regions = atlas.regions
            var i = 0
            val n = regions.size
            while (i < n) {
                val region = regions.get(i)
                var name = region.name
                if (region.index != -1) {
                    name += "_" + region.index
                }
                skin!!.remove(name, TextureRegion::class.java)
                i++
            }
            atlas.dispose()
        }
        
        //load new atlas
        atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear, true)
        skin!!.addRegions(atlas)
        
        Gdx.files.local("atlas").deleteDirectory()
        val pages = packer.pages
        for (i in 0 until pages.size) {
            val page = pages.get(i)
            PixmapIO.writePNG(Gdx.files.local("atlas/$i.png"), page.pixmap)
        }
        
        this.atlas = atlas
        packer.dispose()
    }
    
    fun localize(str: String): String = localizations.getOrDefault(str, str)
}
