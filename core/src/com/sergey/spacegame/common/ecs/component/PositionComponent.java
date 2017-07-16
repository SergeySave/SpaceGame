package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class PositionComponent implements ClonableComponent {
    
    public static final ComponentMapper<PositionComponent> MAPPER = ComponentMapper.getFor(PositionComponent.class);
    
    private float   x;
    private float   y;
    private float   oldX;
    private float   oldY;
    private boolean dirty;
    
    public PositionComponent() {}
    
    public PositionComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2 createVector() {
        return new Vector2(x, y);
    }
    
    public Vector2 setVector(Vector2 v) {
        return v.set(x, y);
    }
    
    public void setFrom(Vector2 v) {
        if (v.x == this.x && v.y == this.y) return;
        x = v.x;
        y = v.y;
        setDirty();
    }
    
    public void setDirty() {
        dirty = true;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        if (x == this.x) return;
        this.x = x;
        setDirty();
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        if (y == this.y) return;
        this.y = y;
        setDirty();
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public float getOldX() {
        return oldX;
    }
    
    public float getOldY() {
        return oldY;
    }
    
    public void setNotDirty() {
        dirty = false;
        oldX = x;
        oldY = y;
    }
    
    @Override
    public Component copy() {
        return new PositionComponent(x, y);
    }
}
