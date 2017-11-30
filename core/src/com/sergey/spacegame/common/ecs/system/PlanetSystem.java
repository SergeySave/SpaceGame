package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;

/**
 * This system is in charge of managing what spaces on planets are empty
 *
 * @author sergeys
 */
public class PlanetSystem extends EntitySystem implements EntityListener {
    
    @Override
    public void addedToEngine(Engine engine) {
        engine.addEntityListener(Family.all(BuildingComponent.class).get(), this);
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        engine.removeEntityListener(this);
    }
    
    @Override
    public void update(float deltaTime) {
    }
    
    @Override
    public void entityAdded(Entity building) {
        BuildingComponent buildingC = BuildingComponent.MAPPER.get(building);
        Entity            planet    = buildingC.getPlanet();
        PlanetComponent   planetC   = PlanetComponent.MAPPER.get(planet);
        float[]           minMax    = getMinMax(building, planet, buildingC.getPosition());
        
        planetC.addBuildingInRange(minMax[0], minMax[1]);
    }
    
    @Override
    public void entityRemoved(Entity building) {
        BuildingComponent buildingC = BuildingComponent.MAPPER.get(building);
        Entity            planet    = buildingC.getPlanet();
        PlanetComponent   planetC   = PlanetComponent.MAPPER.get(planet);
        float[]           minMax    = getMinMax(building, planet, buildingC.getPosition());
        
        planetC.removeBuilding(minMax[0], minMax[1]);
        buildingC.reset();
    }
    
    /**
     * Get the minimum and maximum angles that are ocuppied by a building
     *
     * @param building  - the building
     * @param planet    - the planet the building is on
     * @param positionB - the angle on the planet that the building is on
     *
     * @return an array of floats where the first float is the minimum angle and the second is the maximum angle
     */
    public static float[] getMinMax(Entity building, Entity planet, float positionB) {
        SizeComponent size = SizeComponent.MAPPER.get(building);
        
        SizeComponent planetSize = SizeComponent.MAPPER.get(planet);
        //Base building position
        Vector2 rotatedBuildingVector = new Vector2(1f, 0f).rotate(positionB);
        
        //Desired building position
        Vector2 position = rotatedBuildingVector.cpy().scl(planetSize.w / 2, planetSize.h / 2);
        //Surface vector
        Vector2 surface = rotatedBuildingVector.cpy().rotate90(1);
        //Normalize and scale by half of the width
        surface.setLength(size.w / 2);
        
        float min = position.cpy().sub(surface).angle();
        float max = position.cpy().add(surface).angle();
        
        return new float[]{min, max};
    }
}
