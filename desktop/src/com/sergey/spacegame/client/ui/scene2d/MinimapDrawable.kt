package com.sergey.spacegame.client.ui.scene2d

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent
import com.sergey.spacegame.common.ecs.component.Team1Component
import com.sergey.spacegame.common.ecs.component.Team2Component
import com.sergey.spacegame.common.ecs.component.VisualComponent
import com.sergey.spacegame.common.game.Level

/**
 * A class that represents a drawable for a minimap
 *
 * @author sergeys
 *
 * @constructor Creates a new MinimapDrawable
 * @property team1 - the texture region used to represent something from team 1
 * @property team2 - the texture region used to represent something from team 2
 * @property neutral - the texture region used to represent something not on a team
 * @property white - a texture region representing a white pixel
 * @property level - the level that this minimap should represent
 * @property screen - a rectangle representing the area of the world that the screen covers
 */
class MinimapDrawable(val team1: TextureRegion, val team2: TextureRegion, val neutral: TextureRegion,
                      val white: TextureRegion,
                      val level: Level, val screen: Rectangle) : Drawable {
    
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
    private val invProjection = Matrix3()
    private val VEC = Vector2()
    
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    
    private var dragging: Boolean = false
    
    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        //Create our projection matrix
        projection.setToTranslation(x - level.limits.minX, y - level.limits.minY)
        scaleX = width / level.limits.width
        scaleY = height / level.limits.height
        projection.scale(scaleX, scaleY)
    
        //Create the inverse of the projection matrix
        invProjection.set(projection).inv()
    
        //Calculate the position of the camera as of this frame
        val x1 = VEC.set(screen.x - screen.width / 2, screen.y - screen.height / 2).mul(projection).x
        val y1 = VEC.y
        val x2 = VEC.set(screen.x + screen.width / 2, screen.y + screen.height / 2).mul(projection).x
        val y2 = VEC.y
    
        //Render the camera's position
        batch.draw(white, x1, y1, x2 - x1, 1f) //Bottom
        batch.draw(white, x1, y1, 1f, y2 - y1) //Left
        batch.draw(white, x1, y2, x2 - x1, 1f) //Top
        batch.draw(white, x2, y1, 1f, y2 - y1) //Right
    
        //Border
        batch.draw(white, x, y, width, 1f) //Bottom
        batch.draw(white, x, y, 1f, height) //Left
        batch.draw(white, x, y + height, width, 1f) //Top
        batch.draw(white, x + width, y, 1f, height) //Right
    
        //If it is controllable then allow the user to click to move the camera
        if (level.viewport.viewportControllable) {
            if (Gdx.input.justTouched() && Gdx.input.isTouched && Gdx.input.x in x..(x + width) && (Gdx.graphics.height - Gdx.input.y) in y..(y + height)) {
                dragging = true
            }
        
            //Allow the user to drag around the position of the camera (invalid locaitons will be corrected in GameScreen#render
            if (dragging && Gdx.input.isTouched && Gdx.input.x in x..(x + width) && (Gdx.graphics.height - Gdx.input.y) in y..(y + height)) {
                VEC.set(Gdx.input.x.toFloat(), (Gdx.graphics.height - Gdx.input.y).toFloat()).mul(invProjection)
                screen.x = VEC.x
                screen.y = VEC.y
            } else {
                dragging = false
            }
        } else {
            dragging = false
        }
    
        //Draw the previous things with clipping
        batch.flush()
        //Enable clipping
        val pushed = ScissorStack.pushScissors(Rectangle(x, y, width, height))
    
        //Draw all of the entities
        drawEntities(neutralEntities, batch, neutral, scaleX, scaleY)
        drawEntities(team2Entities, batch, team2, scaleX, scaleY)
        drawEntities(team1Entities, batch, team1, scaleX, scaleY)
    
        //Push it to the screen
        batch.flush()
        //Remove the clipping
        if (pushed) ScissorStack.popScissors()
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