package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component represents a ship
 *
 * @author sergeys
 */
public class ShipComponent implements ClonableComponent {
    
    public static final ComponentMapper<ShipComponent> MAPPER = ComponentMapper.getFor(ShipComponent.class);
    
    /**
     * The maximum move speed of this ship
     */
    public float moveSpeed;
    
    /**
     * The maximum rotation speed of this ship
     */
    public float rotateSpeed;
    
    @Override
    public Component copy() {
        ShipComponent ship = new ShipComponent();
        ship.moveSpeed = moveSpeed;
        ship.rotateSpeed = rotateSpeed;
        return ship;
    }
}
