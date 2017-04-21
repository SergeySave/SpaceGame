package com.sergey.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.sergey.spacegame.client.ui.screen.LoadingScreen;
import com.sergey.spacegame.client.ui.screen.MainMenuScreen;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;

public class SpaceGame extends Game {

	private static SpaceGame instance;

	private ECSManager ecsManager;
	private boolean loaded;

	@Override
	public void create() {
		instance = this;

		setScreen(new LoadingScreen());
		Thread loadingThread = new Thread(()->{
			load();
			setScreenAndDisposeOld(new MainMenuScreen());
			loaded = true;
		}, "Loading Thread");
		loadingThread.setDaemon(true);
		loadingThread.start();
	}

	private void load() {
		ecsManager = new ECSManager();
		ecsManager.getEngine().addSystem(new MovementSystem());
		ecsManager.getEngine().addSystem(new RotationSystem());
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Gdx.gl.
		if (loaded) {
			ecsManager.getEngine().update(Gdx.graphics.getDeltaTime());
		}
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	public void setScreenAndDisposeOld(Screen screen) {
		Screen old = getScreen();
		setScreen(screen);
		old.dispose();
	}

	public static SpaceGame getInstance() {
		return instance;
	}

	public ECSManager getECSManager() {
		return ecsManager;
	}
}
