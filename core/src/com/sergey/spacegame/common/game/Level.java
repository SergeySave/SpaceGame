package com.sergey.spacegame.common.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.data.AudioPlayData;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MessageSystem;
import com.sergey.spacegame.common.ecs.system.MoneyProducerSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.ParticleSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.ecs.system.SpacialQuadtreeSystem;
import com.sergey.spacegame.common.ecs.system.TickableSystem;
import com.sergey.spacegame.common.ecs.system.WeaponSystem;
import com.sergey.spacegame.common.event.Event;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.event.LuaEventHandler;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.io.PathFileHandle;
import com.sergey.spacegame.common.lua.LuaUtils;
import com.sergey.spacegame.common.math.SpatialQuadtree;
import com.sergey.spacegame.common.ui.IViewport;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Represents a single level
 * This is a main hub for all things related to the level as well as the loader and unloader for the level
 *
 * @author sergeys
 */
public class Level {
    
    private static Level      _deserializing;
    private static FileSystem levelFile;
    
    private LevelLimits limits;
    private Background  background;
    private HashMap<String, Command>                         commands     = new HashMap<>();
    private HashMap<String, EntityPrototype>                 entities     = new HashMap<>();
    private HashMap<Class<? extends Event>, LuaEventHandler> events       = new HashMap<>();
    private HashMap<String, String>                          localization = new HashMap<>();
    private HashMap<String, Sound> soundEffects;
    
    private transient String    parentDirectory;
    private transient IViewport viewport;
    private transient boolean isControllable = true;
    private transient Random                 random;
    private transient LuaValue[]             luaStores;
    private transient List<Objective>        objectives;
    private transient ECSManager             ecsManager;
    private transient ImmutableArray<Entity> planets;
    private transient ImmutableArray<Entity> buildingsInConstruction;
    private transient Object                 levelEventRegistry;
    
    private transient Player player1;
    private transient Player player2;
    
    private Level() {
        _deserializing = this;
        
        random = new Random();
        
        luaStores = new LuaValue[10];
        for (int i = 0; i < luaStores.length; i++) {
            luaStores[i] = LuaValue.NIL;
        }
        
        objectives = new ArrayList<>();
        
        ecsManager = new ECSManager();
        ecsManager.addSystem(new MovementSystem());
        ecsManager.addSystem(new RotationSystem());
        ecsManager.addSystem(new BuildingSystem());
        ecsManager.addSystem(new PlanetSystem());
        ecsManager.addSystem(new TickableSystem());
        ecsManager.addSystem(new MoneyProducerSystem(this));
        ecsManager.addSystem(new SpacialQuadtreeSystem(this));
        ecsManager.addSystem(new WeaponSystem(this));
        ecsManager.addSystem(new MessageSystem());
        ecsManager.addSystem(new ParticleSystem(this));
        
        planets = ecsManager.getEntitiesFor(Family.all(PlanetComponent.class).get());
        buildingsInConstruction = ecsManager.getEntitiesFor(Family.all(BuildingComponent.class, InContructionComponent.class)
                                                                    .get());
    }
    
    /**
     * A factory method for creating levels from internal path strings
     *
     * @param internalPath - the internal path string
     *
     * @return a new Level for the given level file
     */
    public static Level getLevelFromInternalPath(String internalPath) {
        //Get the level path
        Path levelZip = SpaceGame.getInstance().getAsset(internalPath);
        
        try {
            //Create a temporary file
            Path tempFile = Files.createTempFile("tmp.", ".sgl");
            
            System.out.println("Temporary file created for internal sgl: " + tempFile);
            
            //Copy it over to the new file
            Files.copy(levelZip, Files.newOutputStream(tempFile));
            
            //Deserialize the level
            Level level = deserialize(tempFile);
            
            //Clean up
            Files.delete(tempFile);
            
            return level;
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        return null;
    }
    
    private static synchronized Level deserialize(Path levelZip) throws IOException {
        FileSystem fileSystem = FileSystems.newFileSystem(levelZip, null);
        levelFile = fileSystem;
        
        Path jsonPath = fileSystem.getPath("level.json");
        
        EventBus eventBus = SpaceGame.getInstance().getEventBus();
        Object   ler      = SpaceGame.getInstance().getContext().createLevelEventHandler(fileSystem);
        eventBus.registerAnnotated(ler);
        
        SpaceGame.getInstance().getContext().reload();
        
        Level level = SpaceGame.getInstance().getGson().fromJson(Files.newBufferedReader(jsonPath), Level.class);
        _deserializing = null;
        levelFile = null;
        level.init(ler);
        return level;
    }
    
    private void init(Object ler) {
        levelEventRegistry = ler;
    }
    
    /**
     * A factory method for creating levels from absolute path strings
     *
     * @param absolutePath - the absolute path string
     *
     * @return a new level for the given level file
     */
    public static Level getLevelFromAbsolutePath(String absolutePath) {
        try {
            return deserialize((new File(absolutePath)).toPath());
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        return null;
    }
    
    /**
     * Get the currently deserializing level or null if no level is deserializing
     * This is used by things that need to access data from the level in order to deserialize
     *
     * @return the currently deserializing level
     */
    public static Level deserializing() {
        return _deserializing;
    }
    
    /**
     * Get the filesystem of the currently deserializing level or null if no level is deserializing
     * This is used by things that need to access data from the filesystem in order to deserialize
     *
     * @return the currently deserializing level's filesystem
     */
    public static FileSystem deserializingFileSystem() {
        return levelFile;
    }
    
    /**
     * Clean up any resources used by this level
     */
    public void deinit() {
        ecsManager.removeAllSystems();
        
        EventBus eventBus = SpaceGame.getInstance().getEventBus();
        
        eventBus.unregisterAll(levelEventRegistry);
        
        //Lua events are registered under this level as the handler
        eventBus.unregisterAll(this);
        
        for (Sound sound : soundEffects.values()) {
            sound.dispose();
        }
        
    }
    
    /**
     * Get a map of all commands
     *
     * @return a map of command ids to their respective commands
     */
    public HashMap<String, Command> getCommands() {
        return commands;
    }
    
    /**
     * Get a map of all defined entity types
     *
     * @return a map of prototype ids to their respective prototypes
     */
    public HashMap<String, EntityPrototype> getEntities() {
        return entities;
    }
    
    /**
     * Get the ECS manager for the level
     *
     * @return the ecs manager
     */
    public ECSManager getECS() {
        return ecsManager;
    }
    
    /**
     * Get all of the planets in the level
     *
     * @return all planets in the level
     */
    public ImmutableArray<Entity> getPlanets() {
        return planets;
    }
    
    /**
     * Get all of the buildings that are currently in construction
     *
     * @return all buildings that are in construction
     */
    public ImmutableArray<Entity> getBuildingsInConstruction() {
        return buildingsInConstruction;
    }
    
    /**
     * Get a list of all objectives for the current level
     *
     * @return the objectives for the current level
     */
    public List<Objective> getObjectives() {
        return objectives;
    }
    
    /**
     * Get the lua data storage array
     *
     * @return the lua data storage array
     */
    public LuaValue[] getLuaStores() {
        return luaStores;
    }
    
    /**
     * Get the limits of the level
     *
     * @return the limits of the level
     */
    public LevelLimits getLimits() {
        return limits;
    }
    
    /**
     * Get the level's random object
     *
     * @return the level's random object
     */
    public Random getRandom() {
        return random;
    }
    
    /**
     * Get the level's background
     *
     * @return the level's background
     */
    public Background getBackground() {
        return background;
    }
    
    /**
     * Play a sound given an AudioPlayData object
     *
     * @param audio - the AudioPlayData object
     * @return the sound's currently playing id
     */
    public long playSound(AudioPlayData audio) {
        return soundEffects.get(audio.getFileName())
                .play(audio.getVolume(), audio.getPitch(), audio.getPan()); //If NPE allow it to propogate
    }
    
    /**
     * Play a sound given a file name
     *
     * @param fileName - the name of the sound file to play
     * @return the sound's currently playing id
     */
    public long playSound(String fileName) {
        return soundEffects.get(fileName).play(1, 1, 0); //If NPE allow it to propogate
    }
    
    /**
     * Play a sound given a file name and a volume
     *
     * @param fileName - the name of the sound file to play
     * @param volume - the volume to play the sound at
     * @return the sound's currently playing id
     */
    public long playSound(String fileName, float volume) {
        return soundEffects.get(fileName).play(volume, 1, 0); //If NPE allow it to propogate
    }
    
    /**
     * Play a sound given a file name, volume, and pitch
     *
     * @param fileName - the name of the sound file to play
     * @param volume - the volume to play the sound at
     * @param pitch - the pitch to play the sound at
     * @return the sound's currently playing id
     */
    public long playSound(String fileName, float volume, float pitch) {
        return soundEffects.get(fileName).play(volume, pitch, 0); //If NPE allow it to propogate
    }
    
    /**
     * Play a sound given a file name, volume, pitch, and pan
     *
     * @param fileName - the name of the sound file to play
     * @param volume - the volume to play the sound at
     * @param pitch - the pitch to play the sound at
     * @param pan - the pan to play the sound with
     * @return the sound's currently playing id
     */
    public long playSound(String fileName, float volume, float pitch, float pan) {
        return soundEffects.get(fileName).play(volume, pitch, pan); //If NPE allow it to propogate
    }
    
    /**
     * Is this level currently controllable by the player
     *
     * @return is this level currently controllable by the player
     */
    public boolean isControllable() {
        return isControllable;
    }
    
    /**
     * Set whether this level is currently controllable by the player
     *
     * @param controllable - should this level be controllable
     */
    public void setControllable(boolean controllable) {
        isControllable = controllable;
    }
    
    /**
     * Get the viewport displaying this level
     *
     * @return the viewport displaying this level
     */
    public IViewport getViewport() {
        return viewport;
    }
    
    /**
     * Set the viewport that is displaying this level
     *
     * @param viewport - the viewport that should display this level
     */
    public void setViewport(IViewport viewport) {
        this.viewport = viewport;
    }
    
    /**
     * Get the parent directory of the level
     *
     * @return the parent directory of the level
     */
    public String getParentDirectory() {
        return parentDirectory;
    }
    
    /**
     * Get the first player
     *
     * @return the first player
     */
    public Player getPlayer1() {
        return player1;
    }
    
    /**
     * Get the second player
     *
     * @return the second player
     */
    public Player getPlayer2() {
        return player2;
    }
    
    /**
     * This class acts as the JSON serializer and deserializer for level objects
     *
     * @author sergeys
     */
    public static class Adapter implements JsonSerializer<Level>, JsonDeserializer<Level> {
        
        @Override
        public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            
            Level level = new Level();
            
            level.soundEffects = new HashMap<>();
            try {
                
                Path sounds = levelFile.getPath("sounds");
                if (Files.exists(sounds)) {
                    Files.walkFileTree(sounds, new SoundFileWalker(level.soundEffects::put));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            level.limits = context.deserialize(obj.get("levelLimits"), LevelLimits.class);
            level.background = context.deserialize(obj.get("background"), Background.class);
            
            level.player1 = new Player(0, new SpatialQuadtree<>(level.limits.getMinX(), level.limits.getMinY(), level.limits
                    .getMaxX(), level.limits.getMaxY(), 9));
            level.player2 = new Player(0, new SpatialQuadtree<>(level.limits.getMinX(), level.limits.getMinY(), level.limits
                    .getMaxX(), level.limits.getMaxY(), 9));
            
            //level.commands = context.deserialize(obj.get("commands"), new TypeToken<HashMap<String, Command>>() {}.getType());
            Set<Entry<String, JsonElement>> commands = obj.get("commands").getAsJsonObject().entrySet();
            level.commands = new HashMap<>(commands.size());
            for (Entry<String, JsonElement> entry : commands) {
                JsonObject commandJson = entry.getValue().getAsJsonObject();
                commandJson.addProperty("id", entry.getKey());
                level.commands.put(entry.getKey(), context.deserialize(commandJson, Command.class));
            }
            
            level.entities = context.deserialize(obj.get("entities"), new TypeToken<HashMap<String, EntityPrototype>>() {}
                    .getType());
            
            JsonArray levelDefaults = obj.getAsJsonArray("state");
            if (levelDefaults != null) {
                for (JsonElement element : levelDefaults) {
                    Entity entity = context.deserialize(element, Entity.class);
                    level.getECS().addEntity(entity);
                }
            }
            
            if (obj.has("events")) {
                for (Entry<String, JsonElement> event : obj.getAsJsonObject("events").entrySet()) {
                    String eventClassStr = event.getKey();
                    String string        = event.getValue().getAsString();
                    
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventClassStr);
                        
                        String lua = LuaUtils.getLUACode(string, levelFile);
                        
                        LuaEventHandler handler = new LuaEventHandler(lua, string);
                        
                        //Level event handlers are registered on the level to simplify deregistration later on
                        SpaceGame.getInstance().getEventBus().registerSpecific(level, eventClass, handler::execute);
                        level.events.put(eventClass, handler);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Failed to find event " + eventClassStr + " for event handler. Not loaded.");
                    } catch (ClassCastException e) {
                        System.out.println("Key " + eventClassStr + " must be a type of event.");
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            //SpaceGame.getInstance().getEventBus().registerSpecific(level, SelectionChangeEvent.class, new LuaEventHandler("print(event:getSelected())")::execute);
            
            return level;
        }
        
        @Override
        public JsonElement serialize(Level src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            
            obj.add("commands", context.serialize(src.commands, new TypeToken<HashMap<String, Command>>() {}.getType()));
            obj.add("entities", context.serialize(src.entities, new TypeToken<HashMap<String, EntityPrototype>>() {}.getType()));
            
            JsonArray state = new JsonArray();
            
            for (Entity entity : src.getECS().getEntities()) {
                state.add(context.serialize(entity));
            }
            
            obj.add("state", state);
            
            JsonObject events = new JsonObject();
            
            for (Entry<Class<? extends Event>, LuaEventHandler> eventHandler : src.events.entrySet()) {
                events.addProperty(eventHandler.getKey().getName(), eventHandler.getValue().getOriginal());
            }
            
            obj.add("events", events);
            
            return obj;
        }
        
    }
    
    
    private static class SoundFileWalker implements FileVisitor<Path> {
        
        private BiConsumer<String, Sound> consumer;
        
        public SoundFileWalker(BiConsumer<String, Sound> consumer) {
            this.consumer = consumer;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isHidden(file) && Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                try {
                    String name = file.toString()
                            .replaceFirst("/sounds/", "");
                    int lastIndex = name.lastIndexOf('/');
                    consumer.accept(name, Gdx.audio.newSound(new PathFileHandle(
                            lastIndex >= 0 ? name.substring(lastIndex) : name, file)));
                } catch (GdxRuntimeException e) {
                    System.err.println("Failed to load file: " + file + " as a sound.");
                }
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.err.println("ERROR: Cannot visit path: " + file);
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
