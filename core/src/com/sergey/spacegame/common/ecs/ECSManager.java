package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.event.EntityAddedEvent;
import com.sergey.spacegame.common.event.EntityRemovedEvent;

/**
 * This class is a manager for the ashley ECS engine
 *
 * @author sergeys
 */
public final class ECSManager {
    
    private Engine engine;
    private EntityAddedEvent.Builder   addedEvent   = new EntityAddedEvent.Builder();
    private EntityRemovedEvent.Builder removedEvent = new EntityRemovedEvent.Builder();
    
    /**
     * Create a new ECSManager
     */
    public ECSManager() {
        engine = new Engine();
    }
    
    /**
     * Update the ecs systems
     *
     * @param delta - the amount of time that has passed since the last update
     */
    public void update(float delta) {
        engine.update(delta);
    }
    
    /**
     * Add an entity system to the ecs engine
     *
     * @param system - the entity system to add
     */
    public void addSystem(EntitySystem system) {
        engine.addSystem(system);
    }
    
    /**
     * Remove an entity system from the ecs engine
     *
     * @param system - the entity system to remove
     */
    public void removeSystem(EntitySystem system) {
        engine.removeSystem(system);
    }
    
    /**
     * Create a new entity
     *
     * @return a new entiy
     */
    public Entity newEntity() {
        return new Entity();
    }
    
    /**
     * Get all entities in the ecs engine
     *
     * @return all entities in the ecs engine
     */
    public ImmutableArray<Entity> getEntities() {
        return engine.getEntities();
    }
    
    /**
     * Get all entities for a given family
     *
     * @param family - the family for which to get entities
     *
     * @return all entities that meet the requirements for a given family
     */
    public ImmutableArray<Entity> getEntitiesFor(Family family) {
        return engine.getEntitiesFor(family);
    }
    
    /**
     * Add an entity to the ecs engine
     * This will also post a entity added event
     *
     * @param entity - the entity to add
     */
    public void addEntity(Entity entity) {
        SpaceGame.getInstance().getEventBus().post(addedEvent.get(entity));
        engine.addEntity(entity);
    }
    
    /**
     * Remove an entity from the ecs engine
     * This will also post a entity removed event
     *
     * @param entity - the entity to remove
     */
    public void removeEntity(Entity entity) {
        SpaceGame.getInstance().getEventBus().post(removedEvent.get(entity));
        engine.removeEntity(entity);
    }
    
    /**
     * Remove all systems from the ecs engine
     */
    public void removeAllSystems() {
        for (EntitySystem system : engine.getSystems()) {
            engine.removeSystem(system);
        }
    }
}
