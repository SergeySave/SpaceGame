package com.sergey.spacegame.common

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.sergey.spacegame.common.data.VisualData
import java.nio.file.FileSystem

/**
 * This interface represents a context that the game is running in
 * It is in charge of determining how assets for levels are loaded and how the game uses visual assets and data
 *
 * @author sergeys
 */
interface SpaceGameContext {
    
    /**
     * Called before loading begins
     * This method should be extremely quick
     */
    fun preload()
    
    /**
     * Called during the beginning of the loading sequence
     * This method is called outside of the OpenGL context, but can take as long as it needs
     */
    fun load()
    
    /**
     * Called after the loading sequence has finished
     * This method is called outside of the OpenGL context and should be quick, but can take as long as it needs
     */
    fun postload()
    
    /**
     * Reload things that are level dependent
     *
     * This will be called after loading or unloading a level
     */
    fun reload()
    
    /**
     * Create an event handler for a level
     *
     * It will be registed to the event bus and unregistered when the level unloads
     *
     * It is expected to deal with loading images into the atlas and localizations into the localization mappings
     *
     * @param fileSystem - the filesystem in which the level handler is expected to operate
     *
     * @return An object that will be registered to the event bus under the level's name
     */
    fun createLevelEventHandler(fileSystem: FileSystem): Any
    
    /**
     * Called after the screen resizes
     *
     * @param width - the new width of the screen in pixels
     * @param height - the new height of the screen in pixels
     */
    fun resize(width: Int, height: Int)
    
    /**
     * Called to dispose any resources used by this context
     */
    fun dispose()
    
    /**
     * Get a texture region or null given a texture name
     *
     * @param name - the name of the texture
     *
     * @return a texture region or null representing for the given name
     */
    fun getRegion(name: String): TextureRegion?
    
    /**
     * Get a visual data object or null given a texture name
     *
     * @param name - the name of the texture
     *
     * @return a visual data object representing the visual data needed for this side
     */
    fun createVisualData(name: String): VisualData?
}