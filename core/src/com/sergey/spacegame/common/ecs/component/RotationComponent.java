package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents the current rotational state of the entity
 *
 * @author sergeys
 */
public class RotationComponent implements ClonableComponent {
    
    public static final ComponentMapper<RotationComponent> MAPPER = ComponentMapper.getFor(RotationComponent.class);
    
    /**
     * The angle of rotation
     */
    public float r;
    
    /**
     * The fractional origin X coordinate
     */
    public float originX;
    
    /**
     * The fractional origin Y coordinate
     */
    public float originY;
    
    /**
     * Create a new RotationComponent with an angle of 0 degrees and an origin of 0, 0
     */
    public RotationComponent() {}
    
    /**
     * Create a new RotationComponent with a given angle and origin
     *
     * @param r       - the angle of rotation
     * @param originX - the x coordinate of the origin
     * @param originY - the y coordinate of the origin
     */
    public RotationComponent(float r, float originX, float originY) {
        this.r = r;
        this.originX = originX;
        this.originY = originY;
    }
    
    @Override
    public Component copy() {
        return new RotationComponent(r, originX, originY);
    }
}
