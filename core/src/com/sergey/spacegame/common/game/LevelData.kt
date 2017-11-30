package com.sergey.spacegame.common.game

/**
 * Represents the dimensional limits of a level
 *
 * @author sergeys
 *
 * @constructor Creates a new LevelLimits object
 *
 * @property minX - the minimum x coordinate
 * @property maxX - the maximum x coordinate
 * @property minY - the minimum y coordinate
 * @property maxY - the maximum y coordinate
 */
data class LevelLimits(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
    
    /**
     * The width of the level
     */
    val width: Float
        get() = maxX - minX
    /**
     * The height of the level
     */
    val height: Float
        get() = maxY - minY
    
    /**
     * The center x coordinate of the level
     */
    val centerX: Float
        get() = (maxX + minX) / 2f
    
    /**
     * The center y coordinate of the level
     */
    val centerY: Float
        get() = (maxY + minY) / 2f
}

/**
 * Represents the background of a level
 *
 * @author sergeys
 *
 * @constructor Creates a new Background object
 *
 * @property image - the name of the image to use for the background
 */
data class Background(val image: String)