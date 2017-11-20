package com.sergey.spacegame.common

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.sergey.spacegame.common.data.VisualData
import java.nio.file.FileSystem

/**
 * @author sergeys
 */
interface SpaceGameContext {
    
    fun preload()
    fun load()
    fun postload()
    
    fun reload()
    fun createLevelEventHandler(fileSystem: FileSystem): Any
    
    fun resize(width: Int, height: Int)
    fun dispose()
    
    fun getRegion(name: String): TextureAtlas.AtlasRegion?
    fun createVisualData(name: String): VisualData?
}