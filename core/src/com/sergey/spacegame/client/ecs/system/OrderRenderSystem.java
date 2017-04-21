package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.orders.IOrder;
import com.sergey.spacegame.common.orders.MoveOrder;

public class OrderRenderSystem extends EntitySystem {

	private ShapeRenderer shape;
	private OrthographicCamera camera;

	private ImmutableArray<Entity> entities;

	public OrderRenderSystem(OrthographicCamera camera) {
		super(1);
		this.camera = camera;
		shape = new ShapeRenderer();
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
		camera.update();

		shape.setProjectionMatrix(camera.combined);
		shape.begin(ShapeType.Line);

		PositionComponent posVar;
		IOrder order;

		for (Entity entity : entities) {
			posVar = PositionComponent.MAPPER.get(entity);
			order = OrderComponent.MAPPER.get(entity).order;

			if (order instanceof MoveOrder) {
				MoveOrder move = (MoveOrder)order;
				shape.setColor(Color.WHITE);
				shape.line(posVar.x, posVar.y, (float)move.getX(), (float)move.getY());
			}

		}

		shape.end();
	}
}
