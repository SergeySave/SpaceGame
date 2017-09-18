package com.sergey.spacegame.common.ui

/**
 * @author sergeys
 */
interface IViewport {
    fun setViewportControllable(controllable: Boolean)
    fun isViewportControllable(): Boolean
    
    fun setViewportX(x: Float)
    fun getViewportX(): Float
    
    fun setViewportY(y: Float)
    fun getViewportY(): Float
    
    fun setViewportWidth(width: Float)
    fun getViewportWidth(): Float
    
    fun setViewportHeight(height: Float)
    fun getViewportHeight(): Float
}