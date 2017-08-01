package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.scene2d.RadialDrawingBatchSprite;
import com.sergey.spacegame.common.ecs.component.HealthComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.component.Team1Component;
import com.sergey.spacegame.common.ecs.component.Team2Component;

public class MainRenderSystem extends EntitySystem {
    
    private static final float TEAM1COLOR   = Color.valueOf("1A60FF00").toFloatBits();
    private static final float NEUTRALCOLOR = Color.valueOf("FFFF0000").toFloatBits();
    private static final float TEAM2COLOR   = Color.valueOf("FF000000").toFloatBits();
    private static final float MULTCOLOR    = Color.toFloatBits(0f, 0f, 0f, 0.5f);
    
    private DrawingBatch batch;
    
    private ImmutableArray<Entity> entities;
    
    private RadialDrawingBatchSprite rdbs;
    
    public MainRenderSystem(DrawingBatch batch) {
        super(2);
        this.batch = batch;
        this.rdbs = new RadialDrawingBatchSprite(SpaceGame.getInstance().getRegion("uncolored"));
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
        HealthComponent   healthComponent;
        
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
    
            if (HealthComponent.MAPPER.has(entity)) {
                healthComponent = HealthComponent.MAPPER.get(entity);
                batch.setMultTint(MULTCOLOR);
                batch.setAddTint(Team1Component.MAPPER.has(entity) ?
                                         TEAM1COLOR :
                                         (Team2Component.MAPPER.has(entity) ? TEAM2COLOR : NEUTRALCOLOR));
        
                rdbs.draw(batch,
                          posVar.getX() - sizeVar.w * 0.75f,
                          posVar.getY() - sizeVar.h * 0.75f,
                          sizeVar.w * 1.5f,
                          sizeVar.h * 1.5f,
                          360 - (float) (360 * healthComponent.getHealth() / healthComponent.getMaxHealth()));
            }
        }
    }
}
