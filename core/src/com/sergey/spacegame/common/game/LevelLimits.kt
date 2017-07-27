package com.sergey.spacegame.common.game

/**
 * @author sergeys
 */
data class LevelLimits(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
    
    val width: Float
        get() = maxX - minX
    val height: Float
        get() = maxY - minY
    
    val centerX: Float
        get() = (maxX + minX) / 2f
    val centerY: Float
        get() = (maxY + minY) / 2f
}