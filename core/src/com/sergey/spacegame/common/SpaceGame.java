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

/**
 * This class represents the main game manager for all sides
 * It gives static access to all important parts of the game
 *
 * @author sergeys
 */
public class SpaceGame extends Game {
    
    //The single instance of this class that should be created in any game
    private static SpaceGame instance;
    
    //This is a service for executing Command objects on a given set of entities
    private CommandExecutorService              commandExecutor;
    //This is a path to the assets directory of this project
    private Path                                assets;
    //The sided context for the game
    private SpaceGameContext                    context;
    //A builder for Gson instances
    private GsonBuilder                         gsonBuilder;
    //The Gson instance currently being used for JSON
    private Gson                                gson;
    //An event bus to register all event listeners to
    private EventBus                            eventBus;
    //A queue of runnables to be run at a given time in the future
    private PriorityQueue<Pair<Long, Runnable>> delayedRunnables;
    
    /**
     * Create a new SpaceGame object with a given context
     *
     * @param spaceGameContext - the context that this space game is running in
     */
    public SpaceGame(SpaceGameContext spaceGameContext) {
        this.context = spaceGameContext;
    }
    
    /**
     * Get the static instance of the space game
     *
     * @return - a static instance of SpaceGame
     */
    public static SpaceGame getInstance() {
        return instance;
    }
    
    @Override
    public void create() {
        //If we fail to create the assets path there is nothing we can do and we must exit
        try {
            Path assetsPath = Paths.get(SpaceGame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            //If we are working in a development environment
            if (Files.isDirectory(assetsPath)) {
                assets = new File("").getAbsoluteFile().toPath();
            } else {
                //If we are working in a JAR file
                assets = FileSystems.newFileSystem(assetsPath, null).getPath("");
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        
        //Set the static instance
        instance = this;
        
        //Do all important context pre-loading actions
        //This should do almost nothing
        context.preload();
        
        //Initialize very important instance variables
        delayedRunnables = new PriorityQueue<>(Comparator.comparing(Pair<Long, Runnable>::getFirst));
        eventBus = new EventBus();
        
        //All actual loading should occur in a seperate thread
        //This prevents loading from blocking the OpenGL context thread
        Thread loadingThread = new Thread(() -> {
            try {
                load();
                context.postload();
            } catch (Throwable throwable) {
                //If we failed to load exit the game
                throwable.printStackTrace();
                Gdx.app.exit();
            }
        }, "Loading Thread");
        //Loading thread should not stop the program from exiting
        loadingThread.setDaemon(true);
        loadingThread.start();
    }
    
    private void load() {
        //Register event handlers
        getEventBus().registerAnnotated(new BaseCommonEventHandler());
        getEventBus().registerAnnotated(SpaceGameLuaLib.INSTANCE);
        
        //Load the context
        //This will probably take a while
        //Things that require an OpenGL context should add a runnable to the OpenGL context
        context.load();
        
        //Initialize unimportant instance variables
        commandExecutor = new CommandExecutorService();
        gsonBuilder = new GsonBuilder();
        
        //Create the Gson context from all event listeners
        getEventBus().post(new GsonRegisterEvent(gsonBuilder));
        gson = gsonBuilder.create();
    }
    
    /**
     * Get the global event bus
     *
     * @return an instance of EventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * Set a screen and then dispose of the old screen
     *
     * This can be called from any thread
     *
     * @param screen - the new screen to switch to
     */
    public void setScreenAndDisposeOld(Screen screen) {
        Gdx.app.postRunnable(() -> {
            Screen old = getScreen();
            setScreen(screen);
            old.dispose();
        });
    }
    
    @Override
    public void render() {
        //Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        //Render the game
        super.render();
        
        //Run any runnables that need to be run from the queue
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
    
    /**
     * Get the command executor service
     *
     * @return A CommandExecutorService instance for executing all commands
     */
    public CommandExecutorService getCommandExecutor() {
        return commandExecutor;
    }
    
    /**
     * Get a Gson object
     *
     * @return The most recently created Gson object
     */
    public Gson getGson() {
        return gson;
    }
    
    /**
     * Add a runnable to the queue to be executed a given number of milliseconds from now
     *
     * @param millis   - the number of milliseconds from now to be executed in
     * @param runnable - the runnable to execute
     */
    public void dispatchDelayedRunnable(long millis, Runnable runnable) {
        delayedRunnables.add(new Pair<>(System.currentTimeMillis() + millis, runnable));
    }
    
    /**
     * Adds an event to the queue to be posted to the event bus a given number of milliseconds from now
     *
     * @param millis - the number of milliseconds from now to be executed in
     * @param e      - the event to post to the event bus
     */
    public void dispatchDelayedEvent(long millis, Event e) {
        delayedRunnables.add(new Pair<>(System.currentTimeMillis() + millis, () -> eventBus.post(e)));
    }
    
    /**
     * Clears all delayed events whose execution time is supposed to be after the current time
     */
    public void clearDelayedEvents() {
        delayedRunnables.add(new Pair<>(System.currentTimeMillis(), delayedRunnables::clear));
    }
    
    /**
     * Clears all delayed events currently in the queue even if their execution time has already been met, but they have
     * not yet executed.
     *
     * Warning this needs to be run from the render thread and preferably from inside a delayed event
     */
    public void clearDelayedEventsNow() {
        delayedRunnables.clear();
    }
    
    /**
     * Get an asset of the game taking into account whether the game is running from a directory or a JAR
     *
     * @param asset - the asset's relative path
     *
     * @return a Path object representing the asset's location
     */
    public Path getAsset(String asset) {
        if (!assets.toString().equals("")) {
            return assets.resolve(asset);
        } else {
            return assets.resolveSibling(asset);
        }
    }
    
    /**
     * Get the current context for the game
     *
     * @return an instance of SpaceGameContext
     */
    public SpaceGameContext getContext() {
        return context;
    }
}
