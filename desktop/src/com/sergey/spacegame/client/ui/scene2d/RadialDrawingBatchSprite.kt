package com.sergey.spacegame.client.ui.scene2d

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.sergey.spacegame.client.gl.DrawingBatch

/**
 * Represents a sprite that is drawn such that only a certain angle of it is visible using my custom drawing batch
 *
 * @see RadialDrawingBatchSprite
 *
 * @author sergeys
 *
 * @constructor Creates a new RadialDrawingBatchSprite
 * @param textureRegion - the texture region that this sprite will use
 */
class RadialDrawingBatchSprite(textureRegion: TextureRegion) {
    
    /**
     * The texture that the sprite uses to draw
     */
    var texture: Texture? = null
        private set
    
    //Verts: x,y,u,v,mult,add
    private val verts = FloatArray(72)
    
    private var x: Float = 0f
    private var y: Float = 0f
    /**
     * The angle that the sprite should be draw to
     */
    var angle: Float = 0f
        set(value) {
            if (field == value)
                return
            field = value
            dirty = true
        }
    private var width: Float = 0f
    private var height: Float = 0f
    private var u1: Float = 0f
    private var u2: Float = 0f
    private var v1: Float = 0f
    private var v2: Float = 0f
    private val du: Float
    private val dv: Float
    private var dirty = true
    private var draw = 0
    private var originX: Float = 0f
    private var originY: Float = 0f
    private var scaleX = 1f
    private var scaleY = 1f
    private var lastMult = 0f
    private var lastAdd = 0f
    
    var leftWidth = 0f
    var rightWidth = 0f
    var topHeight = 0f
    var bottomHeight = 0f
    var minWidth = 0f
    var minHeight = 0f
    
    init {
        this.texture = textureRegion.texture
        this.u1 = textureRegion.u
        this.v1 = textureRegion.v
        this.u2 = textureRegion.u2
        this.v2 = textureRegion.v2
        this.du = u2 - u1
        this.dv = v2 - v1
        this.width = textureRegion.regionWidth.toFloat()
        this.height = textureRegion.regionHeight.toFloat()
    }
    
    /**
     * Set the multiplicative and additive color of the batch
     *
     * @param mult - the multiplicative color in packed float form
     * @param add - the additive color in packed float form
     */
    fun setColor(mult: Float, add: Float) {
        if (mult != lastMult) {
            lastMult = mult
            for (i in 0..11)
                verts[i * 6 + 4] = mult
        }
        if (add != lastAdd) {
            lastAdd = add
            for (i in 0..11)
                verts[i * 6 + 5] = add
        }
    }
    
    private fun vert(verts: FloatArray, offset: Int, x: Float, y: Float) {
        val u = u1 + du * ((x - this.x) / this.width)
        val v = v1 + dv * (1f - (y - this.y) / this.height)
        vert(verts, offset, x, y, u, v)
    }
    
    private fun vert(verts: FloatArray, offset: Int, x: Float, y: Float, u: Float, v: Float) {
        verts[offset] = this.x + originX + (x - this.x - originX) * scaleX
        verts[offset + 1] = this.y + originY + (y - this.y - originY) * scaleY
        verts[offset + 2] = u
        verts[offset + 3] = v
    }
    
    protected fun calculate(x: Float, y: Float, width: Float, height: Float, angle: Float, u0: Float, v0: Float,
                            u1: Float, v1: Float) {
        if (!this.dirty && this.x == x && this.y == y && this.angle == angle && this.width == width && this.height == height
            && this.u1 == u0 && this.v2 == v1 && this.u2 == u1 && this.v2 == v1)
            return
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.angle = angle
        this.u1 = u0
        this.v1 = v0
        this.u2 = u1
        this.v2 = v1
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val x2 = x + width
        val y2 = y + height
        val xc = x + centerX
        val yc = y + centerY
        val ax = MathUtils.cosDeg(angle + 270) // positive right, negative left
        val ay = MathUtils.sinDeg(angle + 270) // positive top, negative bottom
        val txa = if (ax != 0f) Math.abs(centerX / ax) else 99999999f // intersection on left or right "wall"
        val tya = if (ay != 0f) Math.abs(centerY / ay) else 99999999f // intersection on top or bottom "wall"
        val t = Math.min(txa, tya)
        // tx and ty are the intersection points relative to centerX and centerY.
        val tx = t * ax
        val ty = t * ay
        
        vert(verts, BOTTOMRIGHT1, xc, y2)
        if (ax >= 0f) {
            // rotation on the rights half
            vert(verts, TOPLEFT1, x, y)
            vert(verts, TOPRIGHT1, xc, y)
            vert(verts, BOTTOMLEFT1, x, y2)
            vert(verts, BOTTOMLEFT2, xc, yc)
            vert(verts, TOPLEFT2, xc, y)
            if (txa < tya) {
                // rotation on the right side
                vert(verts, TOPRIGHT2, x2, y)
                vert(verts, BOTTOMRIGHT2, x2, yc - ty)
                draw = 2
            } else if (ay > 0f) {
                // rotation on the top side
                vert(verts, BOTTOMRIGHT2, xc + tx, y)
                vert(verts, TOPRIGHT2, xc + tx * 0.5f, y)
                draw = 2
            } else {
                // rotation on the bottom side
                vert(verts, TOPRIGHT2, x2, y)
                vert(verts, BOTTOMRIGHT2, x2, y2)
                vert(verts, TOPLEFT3, xc, yc)
                vert(verts, TOPRIGHT3, x2, y2)
                vert(verts, BOTTOMLEFT3, xc + tx, y2)
                vert(verts, BOTTOMRIGHT3, xc + tx * 0.5f, y2)
                draw = 3
            }
        } else {
            // rotation on the left half
            vert(verts, TOPRIGHT1, x + centerX, y2 - centerY)
            if (txa < tya) {
                // rotation on the left side
                vert(verts, BOTTOMLEFT1, x, y2)
                vert(verts, TOPLEFT1, x, yc - ty)
                draw = 1
            } else if (ay < 0f) {
                // rotation on the bottom side
                vert(verts, TOPLEFT1, xc + tx, y2)
                vert(verts, BOTTOMLEFT1, xc + tx * 0.5f, y2)
                draw = 1
            } else {
                // rotation on the top side
                vert(verts, TOPLEFT1, x, y)
                vert(verts, BOTTOMLEFT1, x, y2)
                vert(verts, BOTTOMRIGHT2, xc, yc)
                vert(verts, BOTTOMLEFT2, x, y)
                vert(verts, TOPLEFT2, xc + tx * 0.5f, y)
                vert(verts, TOPRIGHT2, xc + tx, y)
                draw = 2
            }
        }
        this.dirty = false
    }
    
    /**
     * Draw this sprite at a given angle and size
     *
     * @param batch - the batch to draw the sprite to
     * @param x - the x coordinate to draw the sprite at
     * @param y - the y coordinate to draw the sprite at
     * @param width_ - the width of the sprite
     * @param height_ - the height of the sprite
     * @param angle - the angle to draw the sprite at
     */
    fun draw(batch: DrawingBatch, x: Float, y: Float, width_: Float, height_: Float, angle: Float) {
        var width = width_
        var height = height_
        if (width < 0) {
            scaleX = -1f
            width = -width
        }
        if (height < 0) {
            scaleY = -1f
            height = -height
        }
        setColor(batch.multTint, batch.addTint)
        calculate(x, y, width, height, angle, u1, v1, u2, v2)
        batch.draw(texture, verts, 0, 24 * draw)
    }
    
    /**
     * Draw this sprite at a given angle
     *
     * @param batch - the batch to draw the sprite to
     * @param x - the x coordinate to draw the sprite at
     * @param y - the y coordinate to draw the sprite at
     * @param angle - the angle to draw the sprite at
     */
    fun draw(batch: DrawingBatch, x: Float, y: Float, angle: Float) {
        draw(batch, x, y, width, height, angle)
    }
    
    fun setOrigin(x: Float, y: Float) {
        if (originX == x && originY == y)
            return
        originX = x
        originY = y
        dirty = true
    }
    
    fun setScale(x: Float, y: Float) {
        if (scaleX == x && scaleY == y)
            return
        scaleX = x
        scaleY = y
        dirty = true
    }
    
    fun draw(batch: DrawingBatch, x: Float, y: Float, width: Float, height: Float) {
        draw(batch, x, y, width, height, this.angle)
    }
    
    fun setTextureRegion(textureRegion: TextureRegion) {
        this.texture = textureRegion.texture
        this.u1 = textureRegion.u
        this.v1 = textureRegion.v
        this.u2 = textureRegion.u2
        this.v2 = textureRegion.v2
        this.dirty = true
    }
    
    private companion object {
        val TOPRIGHT1 = 0
        val BOTTOMRIGHT1 = 6
        val BOTTOMLEFT1 = 12
        val TOPLEFT1 = 18
        
        val TOPRIGHT2 = 24
        val BOTTOMRIGHT2 = 30
        val BOTTOMLEFT2 = 36
        val TOPLEFT2 = 42
        
        val TOPRIGHT3 = 48
        val BOTTOMRIGHT3 = 54
        val BOTTOMLEFT3 = 60
        val TOPLEFT3 = 66
    }
}