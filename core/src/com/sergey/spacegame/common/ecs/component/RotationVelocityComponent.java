package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents the rotational velocity of an entity
 *
 * @author sergeys
 */
public class RotationVelocityComponent implements ClonableComponent {
    
    public static final ComponentMapper<RotationVelocityComponent> MAPPER = ComponentMapper.getFor(RotationVelocityComponent.class);
    
    /**
     * The rotation velocity
     */
    public float vr;
    
    /**
     * Creates a new RotationVelocityComponent with a rotational velocity of 0 degrees/second
     */
    public RotationVelocityComponent() {}
    
    /**
     * Creates a new RotationVelocityComponent with the given rotational velocity
     *
     * @param vr - the rotational velocity in degrees/second
     */
    public RotationVelocityComponent(float vr) {
        this.vr = vr;
    }
    
    @Override
    public Component copy() {
        return new RotationVelocityComponent(vr);
    }
}
