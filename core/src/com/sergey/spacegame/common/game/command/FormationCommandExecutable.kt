package com.sergey.spacegame.common.game.command

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.ecs.component.OrderComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.RotationComponent
import com.sergey.spacegame.common.ecs.component.ShipComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.orders.FaceOrder
import com.sergey.spacegame.common.game.orders.MoveOrder
import com.sergey.spacegame.common.util.ceil //KotlinUtils
import com.sergey.spacegame.common.util.floor //KotlinUtils

abstract class FormationCommandExecutable : CommandExecutable {
    
    override fun issue(entitySource: Iterable<Entity>, numEntities: Int, start: Vector2, end: Vector2, level: Level) {
        if (numEntities <= 1) return
        
        //Center of all of the ships
        val center = Vector2()
        //Fleet facing direction
        val facingVector = Vector2()
        
        var length = 0f
        var width = 0f
        
        entitySource.forEach { e ->
            center.add(PositionComponent.MAPPER.get(e).setVector(VEC))
            if (RotationComponent.MAPPER.has(e)) {
                facingVector.add(VEC.set(1f, 0f).rotate(RotationComponent.MAPPER.get(e).r))
            }
            if (SizeComponent.MAPPER.has(e)) {
                val size = SizeComponent.MAPPER.get(e)
                length = maxOf(length, size.w)
                width = maxOf(width, size.h)
            }
        }
        center.scl(1f / numEntities)
        
        if (length == 0f) length = 1f
        if (width == 0f) length = 1f
        
        val angle = facingVector.angle().toDouble()
    
        MTX.setToTranslation(center)
        MTX.rotate(angle.toFloat())
        MTX.scale(length, width)
        
        val entityIterator = entitySource.iterator()
        getDeltaPositions(numEntities).forEach { v ->
            v.mul(MTX) //Apply transformation
            val entity = entityIterator.next()
            
            var order = OrderComponent.MAPPER.get(entity)
            
            if (order == null) {
                order = OrderComponent()
                entity.add(order)
            }
            
            val ship = ShipComponent.MAPPER.get(entity)
            val positionComponent = PositionComponent.MAPPER.get(entity)
            val deltaPos = v.cpy().sub(positionComponent.setVector(VEC))
            order.addOrder(FaceOrder(deltaPos.angle().toDouble(), ship.rotateSpeed))
            order.addOrder(MoveOrder(v.x.toDouble(), v.y.toDouble(), ship.moveSpeed))
            order.addOrder(FaceOrder(angle, ship.rotateSpeed))
        }
    }
    
    /**
     * Get the delta positions of the entities without rotation
     *
     * +X is positive facing direction
     * +Y is 90˚ counterclockwise from +X
     *
     * A single unit on the X or Y axis is the maximum length along that axis
     */
    protected abstract fun getDeltaPositions(count: Int): Iterable<Vector2>
    
    override fun equals(other: Any?): Boolean = testEquals(other)
    
    protected abstract fun testEquals(other: Any?): Boolean
    
    override fun hashCode(): Int = javaClass.hashCode()
    
    private companion object {
        val VEC = Vector2()
        val MTX = Matrix3()
    }
}

class SquareFormationCommandExecutable : FormationCommandExecutable() {
    override fun getDeltaPositions(count: Int): Iterable<Vector2> {
        val positions = ArrayList<Vector2>()
        
        var dirX = 1
        var dirY = 0
        var length = 1
        
        var x = 0
        var y = 0
        var lenWalked = 0
        for (i in 0 until count) {
            positions.add(Vector2(x.toFloat(), y.toFloat()))
            
            x += dirX
            y += dirY
            
            if (++lenWalked == length) {
                lenWalked = 0
                
                val temp = dirX
                dirX = -dirY
                dirY = temp
                
                if (dirY == 0) {
                    ++length
                }
            }
        }
        
        return positions
    }
    
    override fun testEquals(other: Any?): Boolean = other is SquareFormationCommandExecutable
}

class TriangleFormationCommandExecutable : FormationCommandExecutable() {
    override fun getDeltaPositions(count: Int): Iterable<Vector2> {
        val positions = ArrayList<Vector2>()
        
        val levelsNeeded = -0.5 + 0.5 * Math.sqrt(8 * count.toDouble() + 1) - 1
        
        for (i in 0..levelsNeeded.floor()) {
            for (j in 0..i) {
                positions.add(Vector2(levelsNeeded.toFloat() / 2f - i, i / 2f - j))
            }
        }
        
        val remaining = count - positions.size - 1
        
        for (j in 0..remaining) {
            positions.add(Vector2(levelsNeeded.toFloat() / 2f - levelsNeeded.ceil(), remaining / 2f - j))
        }
        
        return positions
    }
    
    override fun testEquals(other: Any?): Boolean = other is TriangleFormationCommandExecutable
}