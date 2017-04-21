package com.sergey.spacegame.client.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.system.RenderSystem;

public class GameScreen implements Screen {

	private OrthographicCamera camera;
	private RenderSystem renderSystem;

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		SpaceGame.getInstance().getECSManager().getEngine().addSystem(renderSystem = new RenderSystem(camera));
	}

	@Override
	public void render(float delta) {
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		SpaceGame.getInstance().getECSManager().getEngine().removeSystem(renderSystem);
	}

	@Override
	public void dispose() {
	}
}
