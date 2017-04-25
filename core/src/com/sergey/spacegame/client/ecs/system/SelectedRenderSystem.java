package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.ui.UIUtil;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;

public class SelectedRenderSystem extends EntitySystem {

	private ShaderProgram program;
	private SpriteBatch batch;
	private OrthographicCamera camera;

	private ImmutableArray<Entity> entities;

	public SelectedRenderSystem(OrthographicCamera camera) {
		super(2);
		this.camera = camera;
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(Family.all(VisualComponent.class, SizeComponent.class, PositionComponent.class, SelectedComponent.class).get());
		program = UIUtil.compileShader(Gdx.files.internal("shaders/selectedShader.vertex.glsl"), Gdx.files.internal("shaders/selectedShader.fragment.glsl"));
		batch = new SpriteBatch();
	}

	@Override
	public void removedFromEngine (Engine engine) {
		entities = null;
		batch.dispose();
		program.dispose();
	}

	@Override
	public void update(float deltaTime) {
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setShader(program);
		batch.begin();

		PositionComponent posVar;
		SizeComponent sizeVar;
		VisualComponent visVar;

		RotationComponent rotVar;

		for (Entity entity : entities) {
			posVar = PositionComponent.MAPPER.get(entity);
			sizeVar = SizeComponent.MAPPER.get(entity);
			visVar = VisualComponent.MAPPER.get(entity);

			if (RotationComponent.MAPPER.has(entity)) {
				rotVar = RotationComponent.MAPPER.get(entity); 
				float oX = rotVar.originX * sizeVar.w;
				float oY = rotVar.originY * sizeVar.h;
				batch.draw(visVar.region, posVar.x-oX, posVar.y-oY, oX, oY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
			} else {
				batch.draw(visVar.region, posVar.x, posVar.y, sizeVar.w, sizeVar.h);
			}

		}
		batch.end();
	}
}
