package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.Team1Component;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.game.orders.MovingOrder;

/**
 * Represents the system for rendering entity orders
 *
 * @author sergeys
 */
public class OrderRenderSystem extends EntitySystem {
    
    private static final float LINE_COLOR = Color.WHITE.toFloatBits();
    
    private DrawingBatch batch;
    
    private ImmutableArray<Entity> entities;
    
    public OrderRenderSystem(DrawingBatch batch) {
        super(1);
        this.batch = batch;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(OrderComponent.class, Team1Component.class).get());
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }
    
    @Override
    public void update(float deltaTime) {
        batch.setForceColor(LINE_COLOR);
        batch.setLineWidth(1f);
        Vector2 posVar;
        
        //Draw the order's lines if needed
        for (Entity entity : entities) {
            posVar = PositionComponent.MAPPER.get(entity).createVector();
            for (IOrder order : OrderComponent.MAPPER.get(entity)) {
                if (order instanceof MovingOrder) {
                    MovingOrder movingOrder = (MovingOrder) order;
                    if (movingOrder.doDraw()) {
                        batch.line(posVar.x, posVar.y, movingOrder.getPositionX(), movingOrder.getPositionY());
                        posVar.set(movingOrder.getPositionX(), movingOrder.getPositionY());
                    }
                }
            }
        }
    }
}
