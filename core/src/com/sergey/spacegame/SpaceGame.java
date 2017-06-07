package com.sergey.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.sergey.spacegame.client.ui.BitmapFontWrapper;
import com.sergey.spacegame.client.ui.screen.LoadingScreen;
import com.sergey.spacegame.client.ui.screen.MainMenuScreen;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.game.command.CommandExecutorService;

public class SpaceGame extends Game {

	private static SpaceGame instance;
	private static final int MIN_FONT_SIZE = 3;

	private CommandExecutorService commandExecutor;
	private InputMultiplexer inputMultiplexer;
	private ECSManager ecsManager;
	private TextureAtlas atlas;
	private Skin skin;
	private FreeTypeFontGenerator fontGenerator;
	private boolean loaded;
	
	private BitmapFontWrapper smallFont, mediumFont, largeFont;

	@Override
	public void create() {
		instance = this;

		setScreen(new LoadingScreen());

		atlas = new TextureAtlas(Gdx.files.internal("atlas.atlas"));

		Thread loadingThread = new Thread(()->{
			load();
			loaded = true;
			setScreenAndDisposeOld(new MainMenuScreen());
		}, "Loading Thread");
		loadingThread.setDaemon(true);
		loadingThread.start();
	}

	private void load() {
		ecsManager = new ECSManager();
		ecsManager.getEngine().addSystem(new MovementSystem());
		ecsManager.getEngine().addSystem(new RotationSystem());
		ecsManager.getEngine().addSystem(new OrderSystem());
		ecsManager.getEngine().addSystem(new BuildingSystem());

		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);

		Gdx.app.postRunnable(()->skin = new Skin(new TextureAtlas(Gdx.files.internal("scene2d/uiskin.atlas"))));
		Gdx.app.postRunnable(()->skin.addRegions(atlas));

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/Helvetica.ttf"));
		generateFonts();

		Gdx.app.postRunnable(()->skin.load(Gdx.files.internal("scene2d/uiskin.json")));
		
		commandExecutor = new CommandExecutorService();
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
		atlas.dispose();
		skin.dispose();
		fontGenerator.dispose();
	}

	@Override
	public void resize(int width, int height) {

		if (fontGenerator != null) {
			{
				FreeTypeFontParameter parameter = new FreeTypeFontParameter();
				parameter.size = Math.max((int) (16 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
				parameter.borderWidth = 0.005f;
				parameter.borderColor = Color.BLACK;
				parameter.color = Color.WHITE;

				BitmapFont font = fontGenerator.generateFont(parameter);
				smallFont.setWrapped(font).dispose();
			}
			{
				FreeTypeFontParameter parameter = new FreeTypeFontParameter();
				parameter.size = Math.max((int) (24 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
				parameter.borderWidth = 0.005f;
				parameter.borderColor = Color.BLACK;
				parameter.color = Color.WHITE;

				BitmapFont font = fontGenerator.generateFont(parameter);
				mediumFont.setWrapped(font).dispose();
			}
			{
				FreeTypeFontParameter parameter = new FreeTypeFontParameter();
				parameter.size = Math.max((int) (32 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
				parameter.borderWidth = 0.005f;
				parameter.borderColor = Color.BLACK;
				parameter.color = Color.WHITE;

				BitmapFont font = fontGenerator.generateFont(parameter);
				largeFont.setWrapped(font).dispose();
			}
		}
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
	
	private void generateFonts() {
		Gdx.app.postRunnable(() -> {
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = Math.max((int) (16 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
			parameter.borderWidth = 0.005f;
			parameter.borderColor = Color.BLACK;
			parameter.color = Color.WHITE;

			BitmapFont font = fontGenerator.generateFont(parameter);
			smallFont = new BitmapFontWrapper(font);
			skin.add("font_small", smallFont, BitmapFont.class);
		});
		Gdx.app.postRunnable(() -> {
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = Math.max((int) (24 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
			parameter.borderWidth = 0.005f;
			parameter.borderColor = Color.BLACK;
			parameter.color = Color.WHITE;

			BitmapFont font = fontGenerator.generateFont(parameter);
			mediumFont = new BitmapFontWrapper(font);
			skin.add("font_medium", mediumFont, BitmapFont.class);
		});
		Gdx.app.postRunnable(() -> {
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = Math.max((int) (32 * Gdx.graphics.getHeight() / 900f), MIN_FONT_SIZE);
			parameter.borderWidth = 0.005f;
			parameter.borderColor = Color.BLACK;
			parameter.color = Color.WHITE;

			BitmapFont font = fontGenerator.generateFont(parameter);
			largeFont = new BitmapFontWrapper(font);
			skin.add("font_large", largeFont, BitmapFont.class);
		});
	}

	public void setScreenAndDisposeOld(Screen screen) {
		Gdx.app.postRunnable(()->{
			Screen old = getScreen();
			setScreen(screen);
			old.dispose();
		});
	}

	public static SpaceGame getInstance() {
		return instance;
	}

	public ECSManager getECSManager() {
		return ecsManager;
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public AtlasRegion getRegion(String name) {
		AtlasRegion region = atlas.findRegion(name);
		if (region != null) return region;
		return atlas.findRegion("missingTexture");
	}

	public InputMultiplexer getInputMultiplexer() {
		return inputMultiplexer;
	}

	public Skin getSkin() {
		return skin;
	}
	
	public CommandExecutorService getCommandExecutor() {
		return commandExecutor;
	}
}
