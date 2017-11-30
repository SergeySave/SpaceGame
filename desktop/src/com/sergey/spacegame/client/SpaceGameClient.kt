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
import com.sergey.spacegame.client.data.ClientVisualData
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
 * This singleton object represents the context for a desktop client space game
 *
 * @author sergeys
 */
object SpaceGameClient : SpaceGameContext {
    
    //The minimum font size allowed by the font system
    private val MIN_FONT_SIZE: Int = 3
    
    //The three different font sizes
    private lateinit var smallFont: BitmapFontWrapper
    private lateinit var mediumFont: BitmapFontWrapper
    private lateinit var largeFont: BitmapFontWrapper
    
    /**
     * The input multiplexer
     */
    lateinit var inputMultiplexer: InputMultiplexer
        private set
    
    /**
     * The texture atlas
     */
    var atlas: TextureAtlas? = null
        private set
    
    /**
     * The UI skin
     */
    var skin: Skin? = null
        private set
    
    //The font generator
    private var fontGenerator: FreeTypeFontGenerator? = null
    
    /**
     * The localization mappings
     */
    var localizations = HashMap<String, String>()
        private set
    
    override fun preload() {
        //Set the screen
        SpaceGame.getInstance().screen = LoadingScreen()
    }
    
    override fun load() {
        //Register client events
        SpaceGame.getInstance().eventBus.registerAnnotated(BaseClientEventHandler())
    
        //Reload at some point in the future
        regenerateAtlas()
        reloadLocalizations()
    
        //Create our input multiplexer
        inputMultiplexer = InputMultiplexer()
        Gdx.input.inputProcessor = inputMultiplexer
    
        //Initialize our UI skin
        Gdx.app.postRunnable { skin = Skin(TextureAtlas(Gdx.files.internal("scene2d/uiskin.atlas"))) }
        Gdx.app.postRunnable { skin!!.addRegions(atlas) }
    
        //Create our fonts
        fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("font/Helvetica.ttf"))
        generateFonts()
    
        //Finish loading our skin
        Gdx.app.postRunnable { skin!!.load(Gdx.files.internal("scene2d/uiskin.json")) }
    }
    
    override fun postload() {
        //Set the screen to the main menu screen and dispose of the loading screen
        SpaceGame.getInstance().setScreenAndDisposeOld(MainMenuScreen())
    }
    
    override fun reload() {
        //Reload everything right now in the current thread
        regenerateAtlasNow()
        reloadLocalizations()
    }
    
    override fun createLevelEventHandler(fileSystem: FileSystem): LevelEventRegistry = LevelEventRegistry(fileSystem)
    
    override fun resize(width: Int, height: Int) {
        val fontGenerator = fontGenerator
        if (fontGenerator != null) {
            //If the font generator exists we want to regenerate our fonts
            smallFont.setWrapped(fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = Math.max((16 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                borderWidth = 0.005f
                borderColor = Color.BLACK
                color = Color.WHITE
            })).dispose()
            mediumFont.setWrapped(fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = Math.max((24 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                borderWidth = 0.005f
                borderColor = Color.BLACK
                color = Color.WHITE
            })).dispose()
            largeFont.setWrapped(fontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = Math.max((32 * Gdx.graphics.height / 900f).toInt(), MIN_FONT_SIZE)
                borderWidth = 0.005f
                borderColor = Color.BLACK
                color = Color.WHITE
            })).dispose()

        }
    }
    
    override fun dispose() {
        //Dispose of any native objects that we created
        if (atlas != null) atlas!!.dispose()
        if (skin != null) skin!!.dispose()
        if (fontGenerator != null) fontGenerator!!.dispose()
        
    }
    
    /**
     * Get a texture region or null given a texture name
     *
     * @param name - the name of the texture
     *
     * @return an atlas region for the given name or if not found the one for the missing texture
     */
    override fun getRegion(name: String): TextureAtlas.AtlasRegion {
        //Find the region of the atlas
        val region = atlas!!.findRegion(name)
        return region ?: atlas!!.findRegion("missingTexture")
    }
    
    /**
     * Get a visual data object or null given a texture name
     *
     * @param name - the name of the texture
     *
     * @return a ClientVisualData object representing the visual data needed for client side rendering
     */
    override fun createVisualData(name: String): ClientVisualData = ClientVisualData(name)
    
    private fun generateFonts() {
        val fontGenerator = fontGenerator!!
        //Generate the fonts some time in the future
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
