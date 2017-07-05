package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;

public class MainRenderSystem extends EntitySystem {
    
    private static final float MULT = Color.toFloatBits(1f, 1f, 1f, 1f);
    private static final float ADD  = Color.toFloatBits(0f, 0f, 0f, 0f);
    
    private DrawingBatch batch;
    
    private ImmutableArray<Entity> entities;
    
    public MainRenderSystem(DrawingBatch batch) {
        super(2);
        this.batch = batch;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(VisualComponent.class, SizeComponent.class, PositionComponent.class)
                                                 .exclude(SelectedComponent.class, InContructionComponent.class)
                                                 .get());
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }
    
    @Override
    public void update(float deltaTime) {
        batch.enableBlending();
        batch.setMultTint(MULT);
        batch.setAddTint(ADD);
        
        PositionComponent posVar;
        SizeComponent     sizeVar;
        VisualComponent   visVar;
        
        RotationComponent rotVar;
        
        for (Entity entity : entities) {
            posVar = PositionComponent.MAPPER.get(entity);
            sizeVar = SizeComponent.MAPPER.get(entity);
            visVar = VisualComponent.MAPPER.get(entity);
            
            if (RotationComponent.MAPPER.has(entity)) {
                rotVar = RotationComponent.MAPPER.get(entity);
                float oX = rotVar.originX * sizeVar.w;
                float oY = rotVar.originY * sizeVar.h;
                batch.draw(visVar.getRegion(),
                           posVar.x - oX, posVar.y - oY, oX, oY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
            } else {
                batch.draw(visVar.getRegion(),
                           posVar.x - sizeVar.w / 2, posVar.y - sizeVar.h / 2, sizeVar.w, sizeVar.h);
            }
            
        }
    }
}
