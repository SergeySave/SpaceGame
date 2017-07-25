package com.sergey.spacegame.common.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
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
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.event.AtlasRegistryEvent;
import com.sergey.spacegame.client.event.LocalizationRegistryEvent;
import com.sergey.spacegame.common.data.AudioPlayData;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MoneyProducerSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.ecs.system.SpacialQuadtreeSystem;
import com.sergey.spacegame.common.ecs.system.TickableSystem;
import com.sergey.spacegame.common.ecs.system.WeaponSystem;
import com.sergey.spacegame.common.event.Event;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.event.EventHandle;
import com.sergey.spacegame.common.event.LuaEventHandler;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.io.PathFileHandle;
import com.sergey.spacegame.common.lua.LuaUtils;
import com.sergey.spacegame.common.math.SpatialQuadtree;
import org.luaj.vm2.LuaValue;

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

public class Level {
    
    private static Level      _deserializing;
    private static FileSystem levelFile;
    
    private LevelLimits limits;
    private HashMap<String, Command>                         commands     = new HashMap<>();
    private HashMap<String, EntityPrototype>                 entities     = new HashMap<>();
    private HashMap<Class<? extends Event>, LuaEventHandler> events       = new HashMap<>();
    private HashMap<String, String>                          localization = new HashMap<>();
    private HashMap<String, Sound> soundEffects;
    
    private transient Random                  random;
    private transient LuaValue[]              luaStores;
    private transient double                  money;
    private transient List<Objective>         objectives;
    private transient ECSManager              ecsManager;
    private transient ImmutableArray<Entity>  planets;
    private transient ImmutableArray<Entity>  buildingsInConstruction;
    private transient LevelEventRegistry      levelEventRegistry;
    private transient SpatialQuadtree<Entity> friendlyTeam;
    private transient SpatialQuadtree<Entity> enemyTeam;
    
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
        
        planets = ecsManager.getEntitiesFor(Family.all(PlanetComponent.class).get());
        buildingsInConstruction = ecsManager.getEntitiesFor(Family.all(BuildingComponent.class, InContructionComponent.class)
                                                                    .get());
    }
    
    public static Level tempLevelGet() {
        Path levelZip = SpaceGame.getInstance().getAsset("level.sgl");
    
        try {
            Path tempFile = Files.createTempFile("tmp.", ".sgl");
    
            System.out.println(tempFile);
    
            Files.copy(levelZip, Files.newOutputStream(tempFile));
    
            Level level = deserialize(tempFile);
    
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
        
        EventBus           eventBus = SpaceGame.getInstance().getEventBus();
        LevelEventRegistry ler      = new LevelEventRegistry(fileSystem);
        eventBus.registerAnnotated(ler);
        
        SpaceGame.getInstance().regenerateAtlasNow();
        SpaceGame.getInstance().reloadLocalizations();
        
        Level level = SpaceGame.getInstance().getGson().fromJson(Files.newBufferedReader(jsonPath), Level.class);
        _deserializing = null;
        levelFile = null;
        level.init(ler);
        return level;
    }
    
    public void init(LevelEventRegistry ler) {
        levelEventRegistry = ler;
    }
    
    public static Level deserializing() {
        return _deserializing;
    }
    
    public static FileSystem deserializingFileSystem() {
        return levelFile;
    }
    
    public void deinit() {
        SpaceGame.getInstance().getEventBus().unregisterAll(levelEventRegistry);
        for (Sound sound : soundEffects.values()) {
            sound.dispose();
        }
    }
    
    public HashMap<String, Command> getCommands() {
        return commands;
    }
    
    public HashMap<String, EntityPrototype> getEntities() {
        return entities;
    }
    
    public ECSManager getECS() {
        return ecsManager;
    }
    
    public ImmutableArray<Entity> getPlanets() {
        return planets;
    }
    
    public ImmutableArray<Entity> getBuildingsInConstruction() {
        return buildingsInConstruction;
    }
    
    public List<Objective> getObjectives() {
        return objectives;
    }
    
    public double getMoney() {
        return money;
    }
    
    public void setMoney(double money) {
        this.money = money;
    }
    
    public LuaValue[] getLuaStores() {
        return luaStores;
    }
    
    public LevelLimits getLimits() {
        return limits;
    }
    
    public SpatialQuadtree<Entity> getTeam1() {
        return friendlyTeam;
    }
    
    public SpatialQuadtree<Entity> getTeam2() {
        return enemyTeam;
    }
    
    public Random getRandom() {
        return random;
    }
    
    public long playSound(AudioPlayData audio) {
        return soundEffects.get(audio.getFileName())
                .play(audio.getVolume(), audio.getPitch(), audio.getPan()); //If NPE allow it to propogate
    }
    
    public static class LevelEventRegistry {
        
        private FileSystem fileSystem;
        
        public LevelEventRegistry(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }
        
        @EventHandle
        public void onAtlasRegistry(AtlasRegistryEvent event) {
            try {
                Files.walkFileTree(fileSystem.getPath("images"), new ImageFileWalker(event.getPacker()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @EventHandle
        public void onLocalizationRegistry(LocalizationRegistryEvent event) {
            try {
                Files.lines(fileSystem.getPath("localization", event.getLocale() + ".loc"))
                        .filter(s -> !s.startsWith("#") && s.matches("([^=]+)=([^=]+)"))
                        .forEach(s -> {
                            String[] parts = s.split("=");
                            event.getLocalizationMap().put(parts[0], parts[1]);
                        });
            } catch (IOException e) {
                System.out.println("Localization file not found: " + event.getLocale());
            }
        }
    
        private static class ImageFileWalker implements FileVisitor<Path> {
            
            private PixmapPacker packer;
        
            public ImageFileWalker(PixmapPacker packer) {
                this.packer = packer;
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
                                .substring(0, file.toString().lastIndexOf('.'))
                                .replaceFirst("/images/", "");
                        byte[] bytes = Files.readAllBytes(file);
                        packer.pack(name, new Pixmap(bytes, 0, bytes.length));
                    } catch (GdxRuntimeException e) {
                        System.err.println("Failed to load file: " + file + " as an image.");
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
    
    
    public static class Adapter implements JsonSerializer<Level>, JsonDeserializer<Level> {
        
        @Override
        public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            
            Level level = new Level();
            
            level.soundEffects = new HashMap<>();
            try {
                Files.walkFileTree(levelFile.getPath("sounds"), new SoundFileWalker(level.soundEffects::put));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            level.limits = context.deserialize(obj.get("levelLimits"), LevelLimits.class);
            
            level.friendlyTeam = new SpatialQuadtree<>(level.limits.getMinX(), level.limits.getMinY(), level.limits
                    .getMaxX(), level.limits
                                                               .getMaxY(), 9);
            level.enemyTeam = new SpatialQuadtree<>(level.limits.getMinX(), level.limits.getMinY(), level.limits.getMaxX(), level.limits
                    .getMaxY(), 9);
            
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
