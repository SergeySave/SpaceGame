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
import com.sergey.spacegame.common.game.orders.BuildBuildingOrder;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.game.orders.MoveOrder;
import com.sergey.spacegame.common.game.orders.TimeMoveOrder;

public class OrderRenderSystem extends EntitySystem {
	
	private static final float LINE_COLOR = Color.WHITE.toFloatBits();

	private DrawingBatch batch;

	private ImmutableArray<Entity> entities;

	public OrderRenderSystem(DrawingBatch batch) {
		super(1);
		this.batch = batch;
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(Family.all(OrderComponent.class).get());
	}

	@Override
	public void removedFromEngine (Engine engine) {
		entities = null;
	}

	@Override
	public void update(float deltaTime) {
		batch.setForceColor(LINE_COLOR);
		Vector2 posVar;

		for (Entity entity : entities) {
			posVar = PositionComponent.MAPPER.get(entity).createVector();
			for (IOrder order : OrderComponent.MAPPER.get(entity)) {
				if (order instanceof MoveOrder) {
					MoveOrder move = (MoveOrder)order;
					batch.line(posVar.x, posVar.y, (float)move.getX(), (float)move.getY());
					posVar.set((float)move.getX(), (float)move.getY());
					
				} else if (order instanceof TimeMoveOrder) {
					TimeMoveOrder move = (TimeMoveOrder)order;
					batch.line(posVar.x, posVar.y, (float)move.getX(), (float)move.getY());
					posVar.set((float)move.getX(), (float)move.getY());
					
				} else if (order instanceof BuildBuildingOrder) {
					BuildBuildingOrder move = (BuildBuildingOrder)order;
					if (move.getPosition().isPresent()) {
						Vector2 b = move.getPosition().get();
						batch.line(posVar.x, posVar.y, (float)b.x, (float)b.y);
						posVar.set((float)b.x, (float)b.y);
					}
				}
			}
		}
	}
}
