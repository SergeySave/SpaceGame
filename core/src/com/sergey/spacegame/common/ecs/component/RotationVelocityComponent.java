package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class RotationVelocityComponent implements ClonableComponent {
    
    public static final ComponentMapper<RotationVelocityComponent> MAPPER = ComponentMapper.getFor(RotationVelocityComponent.class);
    
    public float vr;
    
    public RotationVelocityComponent() {}
    
    public RotationVelocityComponent(float vr) {
        this.vr = vr;
    }
    
    @Override
    public Component copy() {
        return new RotationVelocityComponent(vr);
    }
}
