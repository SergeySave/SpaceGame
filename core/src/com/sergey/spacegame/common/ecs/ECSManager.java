package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.event.EntityAddedEvent;
import com.sergey.spacegame.common.event.EntityRemovedEvent;

public final class ECSManager {
    
    private Engine engine;
    private EntityAddedEvent.Builder   addedEvent   = new EntityAddedEvent.Builder();
    private EntityRemovedEvent.Builder removedEvent = new EntityRemovedEvent.Builder();
    
    public ECSManager() {
        engine = new Engine();
    }
    
    public void update(float delta) {
        engine.update(delta);
    }
    
    public void addSystem(EntitySystem system) {
        engine.addSystem(system);
    }
    
    public void removeSystem(EntitySystem system) {
        engine.removeSystem(system);
    }
    
    public Entity newEntity() {
        return new Entity();
    }
    
    public ImmutableArray<Entity> getEntities() {
        return engine.getEntities();
    }
    
    public ImmutableArray<Entity> getEntitiesFor(Family family) {
        return engine.getEntitiesFor(family);
    }
    
    public void addEntity(Entity entity) {
        SpaceGame.getInstance().getEventBus().post(addedEvent.get(entity));
        engine.addEntity(entity);
    }
    
    public void removeEntity(Entity entity) {
        SpaceGame.getInstance().getEventBus().post(removedEvent.get(entity));
        engine.removeEntity(entity);
    }
    
    public void removeAllSystems() {
        for (EntitySystem system : engine.getSystems()) {
            engine.removeSystem(system);
        }
    }
}
