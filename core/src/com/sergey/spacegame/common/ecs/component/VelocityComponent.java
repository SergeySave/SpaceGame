package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents the velocity of an entity
 *
 * @author sergeys
 */
public class VelocityComponent implements ClonableComponent {
    
    public static final ComponentMapper<VelocityComponent> MAPPER = ComponentMapper.getFor(VelocityComponent.class);
    
    /**
     * The X component of the velocity
     */
    public float vx;
    
    /**
     * The Y component of the velocity
     */
    public float vy;
    
    /**
     * Create a new VelocityComponent with a velocity of 0, 0
     */
    public VelocityComponent() {}
    
    /**
     * Create a new VelocityComponent with a given velocity
     *
     * @param vx - the x component of the velocity
     * @param vy - the y component of the velocity
     */
    public VelocityComponent(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
    
    @Override
    public Component copy() {
        return new VelocityComponent(vx, vy);
    }
}
