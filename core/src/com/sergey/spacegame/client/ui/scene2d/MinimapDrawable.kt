package com.sergey.spacegame.client.ui.scene2d

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.sergey.spacegame.client.ecs.component.VisualComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.game.Level

/**
 * @author sergeys
 */
class MinimapDrawable(val team1: TextureRegion, val team2: TextureRegion, val neutral: TextureRegion,
                      val level: Level) : Drawable {
    
    private var _minHeight: Float = 1f
    private var _minWidth: Float = 1f
    private var _rightWidth: Float = 0f
    private var _leftWidth: Float = 0f
    private var _bottomHeight: Float = 0f
    private var _topHeight: Float = 0f
    
    private val team1Entities = level.ecs.getEntitiesFor(Family.all(Team1Component::class.java, VisualComponent::class.java, PositionComponent::class.java).get())
    private val team2Entities = level.ecs.getEntitiesFor(Family.all(Team2Component::class.java, VisualComponent::class.java, PositionComponent::class.java).get())
    private val neutralEntities = level.ecs.getEntitiesFor(Family.all(VisualComponent::class.java, PositionComponent::class.java).exclude(Team1Component::class.java, Team2Component::class.java).get())
    
    private val projection = Matrix3()
    private val VEC = Vector2()
    
    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        projection.setToTranslation(x - level.limits.minX, y - level.limits.minY)
        val scaleX = width / level.limits.width
        val scaleY = height / level.limits.height
        projection.scale(scaleX, scaleY)
        
        drawEntities(neutralEntities, batch, neutral, scaleX, scaleY)
        drawEntities(team2Entities, batch, team2, scaleX, scaleY)
        drawEntities(team1Entities, batch, team1, scaleX, scaleY)
    }
    
    private fun drawEntities(array: ImmutableArray<Entity>,
                             batch: Batch,
                             region: TextureRegion, scaleX: Float, scaleY: Float) {
        for (entity in array) {
            PositionComponent.MAPPER.get(entity).setVector(VEC).mul(projection)
            
            val sizeComponent = SizeComponent.MAPPER.get(entity)
            
            if (sizeComponent == null) {
                batch.draw(region, VEC.x - defaultWidth * scaleX / 2, VEC.y - defaultHeight * scaleY / 2, defaultWidth * scaleX, defaultWidth * scaleY)
            } else {
                batch.draw(region, VEC.x - sizeComponent.w * scaleX / 2, VEC.y - sizeComponent.h * scaleY / 2, sizeComponent.w * scaleX, sizeComponent.h * scaleY)
            }
        }
    }
    
    override fun setRightWidth(rightWidth: Float) {
        _rightWidth = rightWidth
    }
    
    override fun getLeftWidth(): Float = _leftWidth
    
    override fun setMinHeight(minHeight: Float) {
        _minHeight = minHeight
    }
    
    override fun setBottomHeight(bottomHeight: Float) {
        _bottomHeight = bottomHeight
    }
    
    override fun setTopHeight(topHeight: Float) {
        _topHeight = topHeight
    }
    
    override fun getBottomHeight(): Float = _bottomHeight
    
    override fun getRightWidth(): Float = _rightWidth
    
    override fun getMinWidth(): Float = _minWidth
    
    override fun getTopHeight(): Float = _topHeight
    
    override fun setMinWidth(minWidth: Float) {
        _minWidth = minWidth
    }
    
    override fun setLeftWidth(leftWidth: Float) {
        _leftWidth = leftWidth
    }
    
    override fun getMinHeight(): Float = _minHeight
    
    private companion object {
        val defaultWidth = 10f
        val defaultHeight = 10f
    }
}