package com.sergey.spacegame.common;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Comparator;
import java.util.PriorityQueue;

public class SpaceGame extends Game {
    
    private static SpaceGame              instance;
    private        CommandExecutorService commandExecutor;
    private        Path                   assets;
    
    private SpaceGameContext context;
    
    private GsonBuilder gsonBuilder;
    private Gson        gson;
    
    private EventBus eventBus;
    
    private PriorityQueue<Pair<Long, Runnable>> delayedRunnables;
    
    public SpaceGame(SpaceGameContext spaceGameContext) {
        this.context = spaceGameContext;
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
        
        context.preload();
        
        delayedRunnables = new PriorityQueue<>(Comparator.comparing(Pair<Long, Runnable>::getFirst));
        
        eventBus = new EventBus();
        
        Thread loadingThread = new Thread(() -> {
            try {
                load();
                //loaded = true;
                context.postload();
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
        getEventBus().registerAnnotated(new BaseCommonEventHandler());
        getEventBus().registerAnnotated(SpaceGameLuaLib.INSTANCE);
        context.load();
        
        commandExecutor = new CommandExecutorService();
        
        gsonBuilder = new GsonBuilder();
        
        getEventBus().post(new GsonRegisterEvent(gsonBuilder));
        
        gson = gsonBuilder.create();
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public void setScreenAndDisposeOld(Screen screen) {
        Gdx.app.postRunnable(() -> {
            Screen old = getScreen();
            setScreen(screen);
            old.dispose();
        });
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Gdx.gl.
        super.render();
        
        while (!delayedRunnables.isEmpty() && System.currentTimeMillis() >= delayedRunnables.peek().component1()) {
            delayedRunnables.poll().component2().run();
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        context.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        context.resize(width, height);
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
        delayedRunnables.add(new Pair<>(System.currentTimeMillis() + millis, () -> eventBus.post(e)));
    }
    
    public void dispatchDelayedRunnable(long millis, Runnable runnable) {
        delayedRunnables.add(new Pair<>(System.currentTimeMillis() + millis, runnable));
    }
    
    public void clearDelayedEvents() {
        delayedRunnables.add(new Pair<>(System.currentTimeMillis(), delayedRunnables::clear));
    }
    
    /**
     * Warning this needs to be run from the render thread and preferably from inside a delayed event
     */
    public void clearDelayedEventsNow() {
        delayedRunnables.clear();
    }
    
    public Path getAsset(String asset) {
        if (!assets.toString().equals("")) {
            return assets.resolve(asset);
        } else {
            return assets.resolveSibling(asset);
        }
    }
    
    public SpaceGameContext getContext() {
        return context;
    }
}
