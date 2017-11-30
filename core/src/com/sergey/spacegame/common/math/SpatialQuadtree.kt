package com.sergey.spacegame.common.math

import com.badlogic.gdx.math.Vector2
import com.sergey.spacegame.common.util.CombinedIterator
import java.util.PriorityQueue

/**
 * Represents a map of objects to their positions that, by using a quadtree over the space of the level,
 * is able to quickly query for all objects in a certain area or for the nearest objects to a certain point.
 *
 * @param T - The type of object stored in the mapping
 *
 * @author sergeys
 *
 * @constructor Creates a new SpatialQuadtree object
 *
 * @property minX - the minimum X position of the level
 * @property minY - the minimum Y position of the level
 * @property maxX - the maximum X position of the level
 * @property maxY - the maximum Y position of the level
 * @property leafSize - the maximum amount of objects stored in each leaf of the quadtree
 */
class SpatialQuadtree<T> @JvmOverloads constructor(private val minX: Float, private val minY: Float,
                                                   private val maxX: Float, private val maxY: Float,
                                                   private val leafSize: Int = 25) {
    
    private var root: Node? = null
    private var VEC1 = Vector2()
    private var VEC2 = Vector2()
    
    /**
     * Put an object into the quadtree mapping with a given position
     *
     * @param obj - the object to put into the mapping
     * @param pos - the position at which to insert the object into the map
     */
    fun put(obj: T, pos: Vector2) {
        var root = this.root
        if (root == null) {
            root = Leaf(minX, minY, maxX, maxY, null)
            this.root = root
        }
    
        //Get the smallest node that has the given position inside of it
        val smallestNode = root.getSubNode(pos)
    
        //If the node needs to be divided
        if (smallestNode.map.size == leafSize) {
            //Make a replacement node for the node that was there
            val branch = Branch(smallestNode, smallestNode, smallestNode, smallestNode, smallestNode.bl.x, smallestNode.bl.y, smallestNode.tr.x, smallestNode.tr.y, smallestNode.parent)
            //Make it a new set of leaf nodes
            branch.topLeft = Leaf(smallestNode.bl.x, smallestNode.center.y, smallestNode.center.x, smallestNode.tr.y, branch)
            branch.topRight = Leaf(smallestNode.center.x, smallestNode.center.y, smallestNode.tr.x, smallestNode.tr.y, branch)
            branch.bottomLeft = Leaf(smallestNode.bl.x, smallestNode.bl.y, smallestNode.center.x, smallestNode.center.y, branch)
            branch.bottomRight = Leaf(smallestNode.center.x, smallestNode.bl.y, smallestNode.tr.x, smallestNode.center.y, branch)
    
            //Set the parent of this new node
            if (smallestNode.parent == null) {
                this.root = branch
            } else {
                smallestNode.parent.replaceChild(smallestNode, branch)
            }
    
            //Add the objects from the old node into the new nodes
            for ((newObj, newPos) in smallestNode.map.entries) {
                branch.getSubNode(newPos).map.put(newObj, newPos)
            }
    
            //Add the new object into the node
            branch.getSubNode(pos).map.put(obj, pos)
        } else {
            //Put it into the node
            smallestNode.map.put(obj, pos)
        }
    }
    
    /**
     * Remove an object from the quadtree mapping with a given position
     *
     * @param obj - the object to put into the mapping
     * @param pos - the position that the object is in
     *
     * @return the position that the object had before it was removed
     */
    fun remove(obj: T, pos: Vector2): Vector2? {
        val root = this.root ?: return null
        
        val smallestNode = root.getSubNode(pos)
        //Remove the object from the node it is contained in
        val removed = smallestNode.map.remove(obj)
        
        //If the node isnt the root node and its siblings can be collapsed back into its parent
        if (smallestNode.parent != null && smallestNode.parent.count() < leafSize) {
            //Create the leaf for replacing the parent
            val newLeaf = Leaf(smallestNode.parent.bl.x, smallestNode.parent.bl.y, smallestNode.parent.tr.x, smallestNode.parent.tr.y, smallestNode.parent.parent)
            //Update the graph structure
            if (smallestNode.parent.parent == null) {
                this.root = newLeaf
            } else {
                smallestNode.parent.parent.replaceChild(smallestNode.parent, newLeaf)
            }
            
            //Add the siblings objects into the new parent
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
    
    /**
     * Query an area for all of the objects contained inside of it
     *
     * @param start - one corner of the area that should be queried
     * @param end - the other corner of the area that should be queried
     *
     * @return an iterator of all all of the object, position pairs in this
     */
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
    
    /**
     * Query for all objects in order of increasing distance to the next one from a given point
     *
     * @param pos - the position to query from
     * @param maxRange - the maximum range to allow a query to go
     *
     * @return an iterator iterating over the the objects in the quadtree in order of increasing distance
     */
    @JvmOverloads
    fun queryNearest(pos: Vector2, maxRange: Float = Float.MAX_VALUE): Iterator<T> {
        val root = this.root ?: return emptySequence<T>().iterator()
        
        //Get the smallest node
        val smallestNode = root.getSubNode(pos) //Leaf
        //Get the contents of either the smallest node or its parent
        val contents = if (smallestNode.count() == 0 && smallestNode.parent != null) {
            //If the smallest node is empty then get it's parents contents
            smallestNode.parent.rawContents()
        } else {
            //If the smallest node isn't empty or it is the root node return it's contents
            //However technically that check isn't needed as if the root node exists then there must be items in it
            //So if count == 0 it is by definition not the root node so it must have a parent
            smallestNode.rawContents()
        }
        //If there are no contents return an empty iterator
        if (!contents.hasNext()) return emptySequence<T>().iterator()
        
        //Get the first item in the contents (as that's all we ever cared about)
        val (_, aPoint) = contents.next() //T, Vector2
        //Get the distance squared to that point
        val dist2ToFirstPoint = aPoint.dst2(pos)
        
        //Return an iterator to loop through the points in increasing order
        return NearestIterator(pos, Math.sqrt(dist2ToFirstPoint.toDouble()).toFloat(), dist2ToFirstPoint, root, maxRange)
    }
    
    /**
     * Get the nearest object to a given point
     *
     * @param pos - the point to start the search from
     *
     * @return the nearest point or null if the quadtree is empty
     */
    fun getSingleNearest(pos: Vector2): T? {
        val iterator = queryNearest(pos)
        if (!iterator.hasNext()) return null
        return iterator.next()
    }
    
    /**
     * Get the number of objects stored in the quadtree
     *
     * @return the number of objects in this quadtree
     */
    fun count(): Int {
        //Note this could be done by caching the size rather than recalculating every time
        val root = this.root
        if (root == null) return 0
        return root.count()
    }
    
    private inner abstract class Node(minX: Float, minY: Float, maxX: Float, maxY: Float, val parent: Node?) {
        val tr = Vector2(maxX, maxY)
        val bl = Vector2(minX, minY)
        val center = Vector2((minX + maxX) / 2, (minY + maxY) / 2)
        val bc = Vector2(center.x, minY)
        val tc = Vector2(center.x, maxY)
        val lc = Vector2(minX, center.y)
        val rc = Vector2(maxX, center.y)
    
        abstract fun remove(obj: T, pos: Vector2): Vector2?
    
        abstract fun queryArea(start: Vector2, end: Vector2): Iterator<Map.Entry<T, Vector2>>
    
        abstract fun rawContents(): Iterator<Map.Entry<T, Vector2>>
                
        abstract fun getSubNode(pos: Vector2): Leaf
    
        abstract fun replaceChild(old: Node, new: Node)
    
        abstract fun count(): Int
    }
    
    private inner class Branch(var topLeft: Node, var topRight: Node, var bottomLeft: Node, var bottomRight: Node,
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
            if (contains(start, end, lc, tc)) {
                list.add(topLeft.rawContents())
            } else if (overlaps(start, end, lc, tc)) {
                list.add(topLeft.queryArea(start, end))
            }
            if (contains(start, end, center, tr)) {
                list.add(topRight.rawContents())
            } else if (overlaps(start, end, center, tr)) {
                list.add(topRight.queryArea(start, end))
            }
            if (contains(start, end, bc, rc)) {
                list.add(bottomRight.rawContents())
            } else if (overlaps(start, end, bc, rc)) {
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
                CombinedIterator(topLeft.rawContents(), topRight.rawContents(),
                                 bottomLeft.rawContents(), bottomRight.rawContents())
        
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
    
    private inner class Leaf(minX: Float, minY: Float, maxX: Float, maxY: Float,
                             parent: Node?) : Node(minX, minY, maxX, maxY, parent) {
        val map = HashMap<T, Vector2>()
        
        override fun remove(obj: T, pos: Vector2): Vector2? = map.remove(obj)
        
        override fun queryArea(start: Vector2,
                               end: Vector2): Iterator<Map.Entry<T, Vector2>> = map.filter { (_, value) -> contains(start, end, value) }.iterator()
    
        override fun rawContents(): Iterator<Map.Entry<T, Vector2>> = map.entries.iterator()
        
        override fun getSubNode(pos: Vector2): Leaf = this
        
        override fun replaceChild(old: Node,
                                  new: Node) = throw UnsupportedOperationException("Leaf cannot replace child")
        
        override fun count(): Int = map.size
    }
    
    private inner class NearestIterator(private val center: Vector2, private var radius: Float, rad2: Float,
                                        private val root: Node, private val maxRange: Float) : Iterator<T> {
        private val queue = PriorityQueue<Pair<T, Float>>({ o1, o2 -> o1.second.compareTo(o2.second) })
        private var VEC1 = Vector2()
        private var VEC2 = Vector2()
    
        init {
            var radius2 = rad2
            if (radius > maxRange) {
                radius = maxRange
                radius2 = radius * radius
            }
            root.queryArea(VEC1.set(center).sub(radius, radius),
                           VEC2.set(center).add(radius, radius)).forEach { (obj, loc) ->
            
                val dst = loc.dst2(center)
                if (dst <= radius2) {
                    queue.add(Pair(obj, dst))
                }
            }
        }
        
        override fun hasNext(): Boolean {
            while (queue.isEmpty()) {
                if (radius >= maxRange) return false
                
                val oldRadius = radius
                radius *= 1.5f
        
                if (radius > maxRange) radius = maxRange
                
                val oldRadius2 = oldRadius * oldRadius
                val radius2 = radius * radius
                root.queryArea(VEC1.set(center).sub(radius, radius),
                               VEC2.set(center).add(radius, radius)).forEach { (obj, loc) ->
                    
                    val dst = loc.dst2(center)
                    if (dst <= radius2 && dst > oldRadius2) {
                        queue.add(Pair(obj, dst))
                    }
                }
            }
    
            return true
        }
        
        override fun next() = queue.poll().first
        
    }
}

private fun overlaps(start1: Vector2, end1: Vector2, start2: Vector2,
                     end2: Vector2) = start1.x <= end2.x && end1.x >= start2.x && start1.y <= end2.y && end1.y >= start2.y

private fun contains(start1: Vector2, end1: Vector2, start2: Vector2,
                     end2: Vector2) = start1.x <= start2.x && end1.x >= end2.x && start1.y <= start2.y && end1.y >= end2.y

private fun contains(start: Vector2, end: Vector2,
                     pos: Vector2) = start.x <= pos.x && end.x >= pos.x && start.y <= pos.y && end.y >= pos.y



