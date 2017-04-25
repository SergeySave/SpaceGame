package com.sergey.spacegame.client.ecs.system;

import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.orders.MoveOrder;

public class SelectionControlSystem extends EntitySystem {

	private OrthographicCamera camera;
	private ShapeRenderer shape;
	private Vector2 selectionBegin;

	private ImmutableArray<Entity> selectedEntities;
	private ImmutableArray<Entity> controllableEntities;

	public SelectionControlSystem(OrthographicCamera camera) {
		super(4);
		this.camera = camera;
	}

	@Override
	public void addedToEngine (Engine engine) {
		selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class).get());
		controllableEntities = engine.getEntitiesFor(Family.all(ControllableComponent.class, PositionComponent.class).get());
		shape = new ShapeRenderer();
	}

	@Override
	public void removedFromEngine (Engine engine) {
		selectedEntities = null;
		controllableEntities = null;
		shape.dispose();
	}

	@Override
	public void update(float deltaTime) {
		shape.setProjectionMatrix(camera.combined);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			selectionBegin = new Vector2(vec.x, vec.y);
		}
		if (selectionBegin != null) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			shape.begin(ShapeType.Filled);{
				shape.setColor(0.4f, 0.4f, 1f, 0.5f);
				shape.rect(selectionBegin.x, selectionBegin.y, vec.x-selectionBegin.x, vec.y-selectionBegin.y);
			}shape.end();
			if (!Gdx.input.isButtonPressed(Buttons.LEFT)) {
				selectedEntities.forEach((e)->e.remove(SelectedComponent.class));
				Rectangle rect = new Rectangle(Math.min(vec.x, selectionBegin.x), Math.min(vec.y, selectionBegin.y), Math.abs(vec.x-selectionBegin.x), Math.abs(vec.y-selectionBegin.y));
				controllableEntities.forEach((e)->{
					PositionComponent pos = PositionComponent.MAPPER.get(e);
					if (SizeComponent.MAPPER.has(e)) {
						SizeComponent size = SizeComponent.MAPPER.get(e);
						if (rect.overlaps(new Rectangle(pos.x, pos.y, size.w, size.h))) {
							e.add(new SelectedComponent());
						}
					} else {
						if (rect.contains(new Vector2(pos.x, pos.y))) {
							e.add(new SelectedComponent());
						}
					}
				});
				selectionBegin = null;
			}
		}
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			Vector2 center = StreamSupport.stream(selectedEntities.spliterator(), true).collect(Vector2::new, (v,s)->v.add(new Vector2(PositionComponent.MAPPER.get(s).x,PositionComponent.MAPPER.get(s).y)), (v1,v2)->v1.add(v2));
			float speed = (float) StreamSupport.stream(selectedEntities.spliterator(), true).mapToDouble((e)->ShipComponent.MAPPER.get(e).moveSpeed).min().getAsDouble();
			float dx = vec.x-center.x/selectedEntities.size();
			float dy = vec.y-center.y/selectedEntities.size();
			selectedEntities.forEach((e)->{/*
				if (OrderComponent.MAPPER.has(e)) {
					MoveOrder order = new MoveOrder(OrderComponent.MAPPER.get(e).order.+dx, PositionComponent.MAPPER.get(e).y+dy);
					if (order.isValidFor(e))
						e.add(new OrderComponent(order));
				} else {*/
					MoveOrder order = new MoveOrder(PositionComponent.MAPPER.get(e).x+dx, PositionComponent.MAPPER.get(e).y+dy, speed);
					if (order.isValidFor(e))
						e.add(new OrderComponent(order));
				//}
			});
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}
