package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;

public class RenderSystem extends EntitySystem {

	private static ComponentMapper<PositionComponent> positionMapper = ComponentMapper.getFor(PositionComponent.class);
	private static ComponentMapper<SizeComponent> sizeMapper = ComponentMapper.getFor(SizeComponent.class);
	private static ComponentMapper<VisualComponent> visualMapper = ComponentMapper.getFor(VisualComponent.class);
	private static ComponentMapper<RotationComponent> rotationMapper = ComponentMapper.getFor(RotationComponent.class);

	private SpriteBatch batch;
	private OrthographicCamera camera;

	private ImmutableArray<Entity> entities;

	public RenderSystem(OrthographicCamera camera) {
		super(0);
		this.camera = camera;
		batch = new SpriteBatch();
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(Family.all(VisualComponent.class, SizeComponent.class, PositionComponent.class).get());
	}

	@Override
	public void removedFromEngine (Engine engine) {
		entities = null;
	}

	@Override
	public void update(float deltaTime) {
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();

		PositionComponent posVar;
		SizeComponent sizeVar;
		VisualComponent visVar;
		
		RotationComponent rotVar;

		for (Entity entity : entities) {
			posVar = positionMapper.get(entity);
			sizeVar = sizeMapper.get(entity);
			visVar = visualMapper.get(entity);
			
			if (rotationMapper.has(entity)) {
				rotVar = rotationMapper.get(entity); 
				batch.draw(visVar.region, posVar.x-rotVar.originX, posVar.y-rotVar.originY, rotVar.originX, rotVar.originY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
			} else {
				batch.draw(visVar.region, posVar.x, posVar.y, sizeVar.w, sizeVar.h);
			}

		}

		batch.end();
	}
}
