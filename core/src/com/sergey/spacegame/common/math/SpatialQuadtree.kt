package com.sergey.spacegame.common.math

import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.util.CombinedIterator

/**
 * @author sergeys
 */
class SpatialQuadtree<T> @JvmOverloads constructor(private val minX: Float, private val minY: Float,
                                                   private val maxX: Float, private val maxY: Float,
                                                   private val leafSize: Int = 25) {
    
    private var root: Node? = null
    private var VEC1 = Vector2()
    private var VEC2 = Vector2()
    
    /**
     * @return the old position of the object T
     */
    fun put(obj: T, pos: Vector2) {
        var root = this.root
        if (root == null) {
            root = Leaf(minX, minY, maxX, maxY, null)
            this.root = root
        }
        
        val smallestNode = root.getSubNode(pos)
        if (smallestNode.map.size == leafSize) {
            val branch = Branch(smallestNode, smallestNode, smallestNode, smallestNode, smallestNode.bl.x, smallestNode.bl.y, smallestNode.tr.x, smallestNode.tr.y, smallestNode.parent)
            branch.topLeft = Leaf(smallestNode.tl.x, smallestNode.center.y, smallestNode.center.x, smallestNode.tr.y, branch)
            branch.topRight = Leaf(smallestNode.center.x, smallestNode.center.y, smallestNode.tr.x, smallestNode.tr.y, branch)
            branch.bottomLeft = Leaf(smallestNode.bl.x, smallestNode.bl.y, smallestNode.center.x, smallestNode.center.y, branch)
            branch.bottomRight = Leaf(smallestNode.center.x, smallestNode.br.y, smallestNode.tr.x, smallestNode.center.y, branch)
            if (smallestNode.parent == null) {
                this.root = branch
            } else {
                smallestNode.parent.replaceChild(smallestNode, branch)
            }
            
            for ((newObj, newPos) in smallestNode.map.entries) {
                branch.getSubNode(newPos).map.put(newObj, newPos)
            }
            branch.getSubNode(pos).map.put(obj, pos)
        } else {
            smallestNode.map.put(obj, pos)
        }
    }
    
    fun remove(obj: T, pos: Vector2): Vector2? {
        val root = this.root
        if (root == null) return null
        
        val smallestNode = root.getSubNode(pos)
        val removed = smallestNode.map.remove(obj)
        
        if (smallestNode.parent != null && smallestNode.parent.count() < leafSize) {
            val newLeaf = Leaf(smallestNode.parent.bl.x, smallestNode.parent.bl.y, smallestNode.parent.tr.x, smallestNode.parent.tr.y, smallestNode.parent.parent)
            if (smallestNode.parent.parent == null) {
                this.root = newLeaf
            } else {
                smallestNode.parent.parent.replaceChild(smallestNode.parent, newLeaf)
            }
            
            val parent = smallestNode.parent as Branch
            var child = parent.topLeft as Leaf
            for ((newObj, newPos) in child.map.entries) {
                newLeaf.map.put(newObj, newPos)
            }
            child = parent.topRight as Leaf
            for ((newObj, newPos) in child.map.entries) {
                newLeaf.map.put(newObj, newPos)
            }
            child = parent.bottomLeft as Leaf
            for ((newObj, newPos) in child.map.entries) {
                newLeaf.map.put(newObj, newPos)
            }
            child = parent.bottomRight as Leaf
            for ((newObj, newPos) in child.map.entries) {
                newLeaf.map.put(newObj, newPos)
            }
        } else if (smallestNode.parent == null && smallestNode.map.size == 0) {
            this.root = null
        }
        
        return removed
    }
    
    fun queryArea(start: Vector2, end: Vector2): Iterator<Map.Entry<T, Vector2>> {
        val root = this.root
        if (root == null) return emptyList<Map.Entry<T, Vector2>>().iterator()
        
        //Set vectors
        VEC1.set(start)
        VEC2.set(end)
        
        //Fix the vectors
        if (VEC1.x > VEC2.x) {
            VEC1.x = end.x
            VEC2.x = start.x
        }
        if (VEC1.y > VEC2.y) {
            VEC1.y = end.y
            VEC2.y = start.y
        }
        
        return root.queryArea(VEC1, VEC2)
    }
    
    fun queryNearest(pos: Vector2): T? {
        val root = this.root
        if (root == null) return null
        
        val smallestNode = root.getSubNode(pos) //Leaf
        val (obj, aPoint) = smallestNode.map.entries.first() //T, Vector2
        val distToFirstPoint = aPoint.dst(pos)
        
        var minDist2 = Float.MAX_VALUE
        var closest = obj
        root.queryArea(VEC1.set(pos).sub(distToFirstPoint, distToFirstPoint),
                       VEC2.set(pos).add(distToFirstPoint, distToFirstPoint)).forEach { (obj, loc) ->
            val dst2 = loc.dst2(pos)
            if (dst2 < minDist2) {
                minDist2 = dst2
                closest = obj
            }
        }
        
        return closest
    }
    
    fun count(): Int {
        val root = this.root
        if (root == null) return 0
        return root.count()
    }
    
    protected inner abstract class Node(minX: Float, minY: Float, maxX: Float, maxY: Float, val parent: Node?) {
        val tl = Vector2(minX, maxY)
        val tr = Vector2(maxX, maxY)
        val bl = Vector2(minX, minY)
        val br = Vector2(maxX, minY)
        val center = Vector2((minX + maxX) / 2, (minY + maxY) / 2)
    
        abstract fun remove(obj: T, pos: Vector2): Vector2?
    
        abstract fun queryArea(start: Vector2, end: Vector2): Iterator<Map.Entry<T, Vector2>>
    
        abstract fun rawContents(): Iterator<Map.Entry<T, Vector2>>
                
        abstract fun getSubNode(pos: Vector2): Leaf
    
        abstract fun replaceChild(old: Node, new: Node)
    
        abstract fun count(): Int
    }
    
    protected inner class Branch(var topLeft: Node, var topRight: Node, var bottomLeft: Node, var bottomRight: Node,
                                 minX: Float, minY: Float, maxX: Float, maxY: Float,
                                 parent: Node?) : Node(minX, minY, maxX, maxY, parent) {
        
        override fun remove(obj: T, pos: Vector2): Vector2? {
            if (pos.x > center.x) {
                if (pos.y > center.y) {
                    return topRight.remove(obj, pos)
                } else {
                    return bottomRight.remove(obj, pos)
                }
            } else {
                if (pos.y > center.y) {
                    return topLeft.remove(obj, pos)
                } else {
                    return bottomLeft.remove(obj, pos)
                }
            }
        }
        
        override fun queryArea(start: Vector2, end: Vector2): Iterator<Map.Entry<T, Vector2>> {
            val list = mutableListOf<Iterator<Map.Entry<T, Vector2>>>()
            if (contains(start, end, tl, center)) {
                list.add(topLeft.rawContents())
            } else if (overlaps(start, end, tl, center)) {
                list.add(topLeft.queryArea(start, end))
            }
            if (contains(start, end, tr, center)) {
                list.add(topRight.rawContents())
            } else if (overlaps(start, end, tr, center)) {
                list.add(topRight.queryArea(start, end))
            }
            if (contains(start, end, br, center)) {
                list.add(bottomRight.rawContents())
            } else if (overlaps(start, end, br, center)) {
                list.add(bottomRight.queryArea(start, end))
            }
            if (contains(start, end, bl, center)) {
                list.add(bottomLeft.rawContents())
            } else if (overlaps(start, end, bl, center)) {
                list.add(bottomLeft.queryArea(start, end))
            }
            return CombinedIterator(*list.toTypedArray())
        }
    
        override fun rawContents(): Iterator<Map.Entry<T, Vector2>> =
                CombinedIterator(*listOf<Node>(topLeft, topRight, bottomLeft, bottomRight).map { node -> node.rawContents() }.toTypedArray())
        
        override fun getSubNode(pos: Vector2): Leaf {
            if (pos.x > center.x) {
                if (pos.y > center.y) {
                    return topRight.getSubNode(pos)
                } else {
                    return bottomRight.getSubNode(pos)
                }
            } else {
                if (pos.y > center.y) {
                    return topLeft.getSubNode(pos)
                } else {
                    return bottomLeft.getSubNode(pos)
                }
            }
        }
        
        override fun replaceChild(old: Node, new: Node) {
            when (old) {
                topLeft     -> topLeft = new
                topRight    -> topRight = new
                bottomLeft  -> bottomLeft = new
                bottomRight -> bottomRight = new
            }
        }
        
        override fun count() = topLeft.count() + topRight.count() + bottomLeft.count() + bottomRight.count()
    }
    
    protected inner class Leaf(minX: Float, minY: Float, maxX: Float, maxY: Float,
                               parent: Node?) : Node(minX, minY, maxX, maxY, parent) {
        val map = HashMap<T, Vector2>()
        
        override fun remove(obj: T, pos: Vector2): Vector2? = map.remove(obj)
        
        override fun queryArea(start: Vector2,
                               end: Vector2): Iterator<Map.Entry<T, Vector2>> = map.filter { (_, value) -> value bothAxisPositiveOf start && value bothAxisNegativeOf end }.iterator()
    
        override fun rawContents(): Iterator<Map.Entry<T, Vector2>> = map.entries.iterator()
        
        override fun getSubNode(pos: Vector2): Leaf = this
        
        override fun replaceChild(old: Node,
                                  new: Node) = throw UnsupportedOperationException("Leaf cannot replace child")
        
        override fun count(): Int = map.size
    }
}

private infix fun Vector2.bothAxisPositiveOf(other: Vector2): Boolean = this.x >= other.x && this.y >= other.y
private infix fun Vector2.bothAxisNegativeOf(other: Vector2): Boolean = this.x <= other.x && this.y <= other.y
private fun overlaps(start1: Vector2, end1: Vector2, start2: Vector2,
                     end2: Vector2) = start1.x <= end2.x && end1.x >= start2.x && start1.y <= end2.y && end1.y >= start2.y

private fun contains(start1: Vector2, end1: Vector2, start2: Vector2,
                     end2: Vector2) = start1.x <= start2.x && end1.x >= end2.x && start1.y <= start2.y && end1.y >= end2.y

