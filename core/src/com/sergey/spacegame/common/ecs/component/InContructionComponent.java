package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents a component for buildings that are in construction
 *
 * @author sergeys
 */
public class InContructionComponent implements Component {
    
    public static final ComponentMapper<InContructionComponent> MAPPER = ComponentMapper.getFor(InContructionComponent.class);
    
    /**
     * The final health that this entity should have
     */
    public double finalHealth;
    
    /**
     * The amount of construction time that the building started off with
     */
    public float originalTimeRemaining;
    
    /**
     * The current amount of time the building's construction has remaining
     */
    public float timeRemaining;
    
    /**
     * The entity id of the building that is under construction
     */
    public String entityID;
    
    /**
     * The price of this building
     */
    public double price;
    
    /**
     * The number of objects that are currently helping build this building
     */
    public int building;
    
    /**
     * Create a new InConstructionComponent
     *
     * @param entityID - the entity id of the building that is under construction
     * @param price    - the price of the building under construction
     */
    public InContructionComponent(String entityID, double price) {
        this.entityID = entityID;
        this.price = price;
    }
}
