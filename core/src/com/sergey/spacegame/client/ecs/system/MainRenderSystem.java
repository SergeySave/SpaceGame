package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;

public class MainRenderSystem extends EntitySystem {
    
    private DrawingBatch batch;
    
    private ImmutableArray<Entity> entities;
    
    public MainRenderSystem(DrawingBatch batch) {
        super(2);
        this.batch = batch;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(VisualComponent.class, SizeComponent.class, PositionComponent.class)
                                                 .get());
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }
    
    @Override
    public void update(float deltaTime) {
        batch.enableBlending();
        
        PositionComponent posVar;
        SizeComponent     sizeVar;
        VisualComponent   visVar;
        
        RotationComponent rotVar;
        
        for (Entity entity : entities) {
            posVar = PositionComponent.MAPPER.get(entity);
            sizeVar = SizeComponent.MAPPER.get(entity);
            visVar = VisualComponent.MAPPER.get(entity);
    
            batch.setMultTint(visVar.getMultColor());
            batch.setAddTint(visVar.getAddColor());
            if (RotationComponent.MAPPER.has(entity)) {
                rotVar = RotationComponent.MAPPER.get(entity);
                float oX = rotVar.originX * sizeVar.w;
                float oY = rotVar.originY * sizeVar.h;
                batch.draw(visVar.getRegion(),
                           posVar.getX() - oX, posVar.getY() - oY, oX, oY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
            } else {
                batch.draw(visVar.getRegion(),
                           posVar.getX() - sizeVar.w / 2, posVar.getY() - sizeVar.h / 2, sizeVar.w, sizeVar.h);
            }
            
        }
    }
}
