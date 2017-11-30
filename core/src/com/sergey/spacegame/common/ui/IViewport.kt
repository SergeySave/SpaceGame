package com.sergey.spacegame.common.ui

import com.sergey.spacegame.common.game.Level

/**
 * Represents a viewport and the methods that can be called on one
 *
 * @author sergeys
 */
interface IViewport {
    
    /**
     * Is this viewport controllable
     */
    var viewportControllable: Boolean
    
    /**
     * The X coordinate of this viewport
     */
    var viewportX: Float
    
    /**
     * The Y coordinate of this viewport
     */
    var viewportY: Float
    
    /**
     * The width of this viewport
     */
    var viewportWidth: Float
    
    /**
     * The height of this viewport
     */
    var viewportHeight: Float
    
    /**
     * Change the level that this viewport is displaying
     *
     * @param level - the new level to display
     */
    fun setLevel(level: Level)
    
    /**
     * Close this viewport
     */
    fun close()
}