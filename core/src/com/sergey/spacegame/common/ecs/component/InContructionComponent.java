package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class InContructionComponent implements Component {
    
    public static final ComponentMapper<InContructionComponent> MAPPER = ComponentMapper.getFor(InContructionComponent.class);
    
    public float  timeRemaining;
    public String entityID;
    
    public InContructionComponent(String entityID) {
        this.entityID = entityID;
    }
}
