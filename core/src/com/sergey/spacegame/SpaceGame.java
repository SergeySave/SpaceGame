package com.sergey.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.event.AtlasRegistryEvent;
import com.sergey.spacegame.client.event.BaseEventHandler;
import com.sergey.spacegame.client.ui.BitmapFontWrapper;
import com.sergey.spacegame.client.ui.screen.LoadingScreen;
import com.sergey.spacegame.client.ui.screen.MainMenuScreen;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.event.Event;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.game.command.CommandExecutorService;

public class SpaceGame extends Game {

	private static SpaceGame instance;
	private static final int MIN_FONT_SIZE = 3;

	private CommandExecutorService commandExecutor;
	private InputMultiplexer inputMultiplexer;
	private TextureAtlas atlas;
	private Skin skin;
	private FreeTypeFontGenerator fontGenerator;
	//private boolean loaded;
	
	private BitmapFontWrapper smallFont, mediumFont, largeFont;
	
	private GsonBuilder gsonBuilder;
	private Gson gson;

	private EventBus eventBus;

	public SpaceGame() {
	}

	@Override
	public void create() {
		instance = this;

		setScreen(new LoadingScreen());

		eventBus = new EventBus();

		Thread loadingThread = new Thread(()->{
			try {
				load();
				//loaded = true;
				setScreenAndDisposeOld(new MainMenuScreen());
			} catch (Throwable throwable) {
				//If we failed to load exit the game
				throwable.printStackTrace();
				Gdx.app.exit();
			}
		}, "Loading Thread");
		loadingThread.setDaemon(true);
		loadingThread.start();
	}

	private void load() {

		//Register event handlers
		getEventBus().register(new BaseEventHandler());
		regenerateAtlas();

		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);

		Gdx.app.postRunnable(()->skin = new Skin(new TextureAtlas(Gdx.files.internal("scene2d/uiskin.atlas"))));
		Gdx.app.postRunnable(()->skin.addRegions(atlas));

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/Helvetica.ttf"));
		generateFonts();

		Gdx.app.postRunnable(()->skin.load(Gdx.files.internal("scene2d/uiskin.json")));
		
		commandExecutor = new CommandExecutorService();
		
		gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(Command.class, new Command.Adapter());
		gsonBuilder.registerTypeAdapter(EntityPrototype.class, new EntityPrototype.Adapter());
		gsonBuilder.registerTypeAdapter(VisualComponent.class, new VisualComponent.Adapter());
		gsonBuilder.registerTypeAdapter(ControllableComponent.class, new ControllableComponent.Adapter());
		
		gson = gsonBuilder.create();
	}

	public void regenerateAtlas() {
		PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
		getEventBus().post(new AtlasRegistryEvent(packer));

		Gdx.app.postRunnable(()->{
			//unload current atlas
			if (atlas != null) {
				Array<AtlasRegion> regions = atlas.getRegions();
				for (int i = 0, n = regions.size; i < n; i++) {
					AtlasRegion region = regions.get(i);
					String name = region.name;
					if (region.index != -1) {
						name += "_" + region.index;
					}
					skin.remove(name, TextureRegion.class);
				}
				atlas.dispose();
			}

			//load new atlas
			atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear, true);
			if (skin != null) skin.addRegions(atlas);
			packer.dispose();
		});
	}

	public void regenerateAtlasNow() {
		PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 0, false, new PixmapPacker.GuillotineStrategy());
		getEventBus().post(new AtlasRegistryEvent(packer));

		//unload current atlas
		if (atlas != null) {
			Array<AtlasRegion> regions = atlas.getRegions();
			for (int i = 0, n = regions.size; i < n; i++) {
				AtlasRegion region = regions.get(i);
				String name = region.name;
				if (region.index != -1) {
					name += "_" + region.index;
				}
				skin.remove(name, TextureRegion.class);
			}
			atlas.dispose();
		}

		//load new atlas
		atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear, true);
		skin.addRegions(atlas);
		packer.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Gdx.gl.
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (atlas != null) atlas.dispose();
		if (skin != null) skin.dispose();
		if (fontGenerator != null) fontGenerator.dispose();
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
	
	public Gson getGson() {
		return gson;
	}
	
	public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}

	public EventBus getEventBus() {
		return eventBus;
	}
}
