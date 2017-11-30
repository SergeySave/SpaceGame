package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents the size of an entity
 *
 * @author sergeys
 */
public class SizeComponent implements ClonableComponent {
    
    public static final ComponentMapper<SizeComponent> MAPPER = ComponentMapper.getFor(SizeComponent.class);
    
    /**
     * The width of this entity
     */
    public float w;
    
    /**
     * The heigh of this entity
     */
    public float h;
    
    /**
     * Create a new SizeComponent with a size of 0, 0
     */
    public SizeComponent() {}
    
    /**
     * Create a new SizeComponent with a given size
     *
     * @param w - the width
     * @param h - the height
     */
    public SizeComponent(float w, float h) {
        this.w = w;
        this.h = h;
    }
    
    @Override
    public Component copy() {
        return new SizeComponent(w, h);
    }
}
