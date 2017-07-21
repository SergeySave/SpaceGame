package com.sergey.spacegame.common.game

/**
 * @author sergeys
 */
data class LevelLimits(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
    fun getWidth() = maxX - minX
    fun getHeight() = maxY - minY
    
    fun getCenterX() = (maxX + minX) / 2f
    fun getCenterY() = (maxY + minY) / 2f
}