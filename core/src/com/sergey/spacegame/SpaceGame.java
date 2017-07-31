package com.sergey.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
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
import com.sergey.spacegame.client.event.AtlasRegistryEvent;
import com.sergey.spacegame.client.event.BaseClientEventHandler;
import com.sergey.spacegame.client.event.LocalizationRegistryEvent;
import com.sergey.spacegame.client.ui.BitmapFontWrapper;
import com.sergey.spacegame.client.ui.screen.LoadingScreen;
import com.sergey.spacegame.client.ui.screen.MainMenuScreen;
import com.sergey.spacegame.common.event.BaseCommonEventHandler;
import com.sergey.spacegame.common.event.Event;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.event.GsonRegisterEvent;
import com.sergey.spacegame.common.game.command.CommandExecutorService;
import com.sergey.spacegame.common.lua.SpaceGameLuaLib;
import kotlin.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.PriorityQueue;

public class SpaceGame extends Game {
    
    private static final int MIN_FONT_SIZE = 3;
    private static SpaceGame              instance;
    private        CommandExecutorService commandExecutor;
    private        InputMultiplexer       inputMultiplexer;
    private        TextureAtlas           atlas;
    private        Skin                   skin;
    private        FreeTypeFontGenerator  fontGenerator;
    private        Path                   assets;
    
    private BitmapFontWrapper smallFont, mediumFont, largeFont;
    
    private GsonBuilder gsonBuilder;
    private Gson        gson;
    
    private EventBus eventBus;
    
    private PriorityQueue<Pair<Long, Event>> delayedEvents;
    
    private HashMap<String, String> localizations = new HashMap<>();
    
    public SpaceGame() {
    }
    
    public static SpaceGame getInstance() {
        return instance;
    }
    
    @Override
    public void create() {
        try {
            Path assetsPath = Paths.get(SpaceGame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(assetsPath)) {
                assets = new File("").getAbsoluteFile().toPath();
            } else {
                assets = FileSystems.newFileSystem(assetsPath, null).getPath("");
                //assets = FileSystems.newFileSystem(assetsPath, null);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    
        instance = this;
        
        setScreen(new LoadingScreen());
        
        delayedEvents = new PriorityQueue<>();
        
        eventBus = new EventBus();
        
        Thread loadingThread = new Thread(() -> {
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
        getEventBus().registerAnnotated(new BaseClientEventHandler());
        getEventBus().registerAnnotated(new BaseCommonEventHandler());
        getEventBus().registerAnnotated(SpaceGameLuaLib.INSTANCE);
        regenerateAtlas();
        reloadLocalizations();
        
        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        Gdx.app.postRunnable(() -> skin = new Skin(new TextureAtlas(Gdx.files.internal("scene2d/uiskin.atlas"))));
        Gdx.app.postRunnable(() -> skin.addRegions(atlas));
        
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/Helvetica.ttf"));
        generateFonts();
        
        Gdx.app.postRunnable(() -> skin.load(Gdx.files.internal("scene2d/uiskin.json")));
        
        commandExecutor = new CommandExecutorService();
        
        gsonBuilder = new GsonBuilder();
        
        getEventBus().post(new GsonRegisterEvent(gsonBuilder));
        
        gson = gsonBuilder.create();
    }
    
    public void setScreenAndDisposeOld(Screen screen) {
        Gdx.app.postRunnable(() -> {
            Screen old = getScreen();
            setScreen(screen);
            old.dispose();
        });
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public void regenerateAtlas() {
        PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.GuillotineStrategy());
        getEventBus().post(new AtlasRegistryEvent(packer));
        
        Gdx.app.postRunnable(() -> {
            //unload current atlas
            if (atlas != null) {
                Array<AtlasRegion> regions = atlas.getRegions();
                for (int i = 0, n = regions.size; i < n; i++) {
                    AtlasRegion region = regions.get(i);
                    String      name   = region.name;
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
            
            Gdx.files.local("atlas").deleteDirectory();
            Array<PixmapPacker.Page> pages = packer.getPages();
            for (int i = 0; i < pages.size; i++) {
                PixmapPacker.Page page = pages.get(i);
                PixmapIO.writePNG(Gdx.files.local("atlas/" + i + ".png"), page.getPixmap());
            }
            
            packer.dispose();
        });
    }
    
    public void reloadLocalizations() {
        HashMap<String, String> localization = new HashMap<>();
        
        LocalizationRegistryEvent localizationRegistryEvent = new LocalizationRegistryEvent(localization, "en_US");
        getEventBus().post(localizationRegistryEvent);
        
        this.localizations = localization;
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
    
    public void regenerateAtlasNow() {
        PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 1, false, new PixmapPacker.GuillotineStrategy());
        getEventBus().post(new AtlasRegistryEvent(packer));
        
        //unload current atlas
        if (atlas != null) {
            Array<AtlasRegion> regions = atlas.getRegions();
            for (int i = 0, n = regions.size; i < n; i++) {
                AtlasRegion region = regions.get(i);
                String      name   = region.name;
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
        
        Gdx.files.local("atlas").deleteDirectory();
        Array<PixmapPacker.Page> pages = packer.getPages();
        for (int i = 0; i < pages.size; i++) {
            PixmapPacker.Page page = pages.get(i);
            PixmapIO.writePNG(Gdx.files.local("atlas/" + i + ".png"), page.getPixmap());
        }
        
        packer.dispose();
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Gdx.gl.
        super.render();
        
        while (!delayedEvents.isEmpty() && System.currentTimeMillis() >= delayedEvents.peek().component1()) {
            eventBus.post(delayedEvents.poll().component2());
        }
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
    
    public void dispatchDelayedEvent(long millis, Event e) {
        delayedEvents.add(new Pair<>(System.currentTimeMillis() + millis, e));
    }
    
    public HashMap<String, String> getLocalizations() {
        return localizations;
    }
    
    public String localize(String str) {
        return localizations.getOrDefault(str, str);
    }
    
    public Path getAsset(String asset) {
        if (!assets.toString().equals("")) {
            return assets.resolve(asset);
        } else {
            return assets.resolveSibling(asset);
        }
    }
}
