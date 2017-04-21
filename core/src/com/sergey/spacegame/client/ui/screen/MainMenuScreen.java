package com.sergey.spacegame.client.ui.screen;

import com.badlogic.gdx.Screen;
import com.sergey.spacegame.SpaceGame;

public class MainMenuScreen implements Screen {

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		SpaceGame.getInstance().setScreen(new GameScreen());
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}
}
