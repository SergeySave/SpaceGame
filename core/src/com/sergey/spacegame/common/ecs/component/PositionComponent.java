package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.util.Utils;

/**
 * This component holds positional information for each entity
 *
 * @author sergeys
 */
public class PositionComponent implements ClonableComponent {
    
    public static final ComponentMapper<PositionComponent> MAPPER = ComponentMapper.getFor(PositionComponent.class);
    
    private float   x;
    private float   y;
    private float   oldX;
    private float   oldY;
    private boolean dirty;
    
    /**
     * Create a new PositionComponent at 0, 0
     */
    public PositionComponent() {}
    
    /**
     * Create a new PositionComponent at the given coordinates
     *
     * @param x - the x coordinate
     * @param y - the y coordinate
     */
    public PositionComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Create a vector for the current value of this component
     *
     * @return a new vector with the coordinates of this component
     */
    public Vector2 createVector() {
        return new Vector2(x, y);
    }
    
    /**
     * Set a vector to the coordinates of this component
     *
     * @param v - the vector to set
     *
     * @return the vector that was set for chaining
     */
    public Vector2 setVector(Vector2 v) {
        return v.set(x, y);
    }
    
    /**
     * Set this component's coordinates from those of a given vector
     *
     * @param v - the vector to use
     */
    public void setFrom(Vector2 v) {
        if (v.x == this.x && v.y == this.y) return;
        x = v.x;
        y = v.y;
        setDirty();
    }
    
    /**
     * Set whether this component is dirty
     */
    public void setDirty() {
        dirty = true;
    }
    
    /**
     * Get the x coordinate
     *
     * @return the x coordinate
     */
    public float getX() {
        return x;
    }
    
    /**
     * Set the x coordinate
     *
     * @param x - the new x coordinate
     */
    public void setX(float x) {
        if (x == this.x) return;
        this.x = x;
        setDirty();
    }
    
    /**
     * Get the y coordinate
     *
     * @return - the y coordinate
     */
    public float getY() {
        return y;
    }
    
    /**
     * Set the y coordinate
     *
     * @param y - the new y coordinate
     */
    public void setY(float y) {
        if (y == this.y) return;
        this.y = y;
        setDirty();
    }
    
    /**
     * Check if the dirty flag is set
     *
     * @return whether the dirty flag is set
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Get the old x coordinate before removing the dirty flag
     *
     * @return the old x coordinate
     */
    public float getOldX() {
        return oldX;
    }
    
    /**
     * Get the old y coordinate before removing the dirty flag
     *
     * @return the old y coordinate
     */
    public float getOldY() {
        return oldY;
    }
    
    /**
     * Reset the dirty flag
     */
    public void setNotDirty() {
        dirty = false;
        oldX = x;
        oldY = y;
    }
    
    /**
     * This is an advanced method that will not update the dirty flag.
     * This should only be used if you ensure that it is set to dirty after
     *
     * @param minX the minimum x position
     * @param maxX the maximum x position
     * @param minY the mininmum y position
     * @param maxY the maximum y position
     */
    public void clamp(float minX, float maxX, float minY, float maxY) {
        x = Utils.clamp(x, minX, maxX);
        y = Utils.clamp(y, minY, maxY);
    }
    
    @Override
    public Component copy() {
        return new PositionComponent(x, y);
    }
}
