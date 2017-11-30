package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

/**
 * This component represents a building that is on a planet
 *
 * @author sergeys
 */
public class BuildingComponent implements Component {
    
    public static final ComponentMapper<BuildingComponent> MAPPER = ComponentMapper.getFor(BuildingComponent.class);
    
    private Entity planet;
    private float  position;
    
    /**
     * Initialize this BuildingComponent
     *
     * @param planet   - the planet this building is attached to
     * @param position - the angle relative to the planet's facing direction that this building is on
     */
    public void init(Entity planet, float position) {
        if (planet == null) throw new NullPointerException("Planet must not be null");
        this.planet = planet;
        this.position = position;
        //Registration of the building should have occurred right
        //after this when this component was added in the PlanetSystem
    }
    
    public float getPosition() {
        return position;
    }
    
    public void reset() {
        //This will be called when the building is unregistered by the PlanetSystem
        //In order to reuse this it must be reinitialized
        
        planet = null;
    }
    
    public Entity getPlanet() {
        return planet;
    }
}
