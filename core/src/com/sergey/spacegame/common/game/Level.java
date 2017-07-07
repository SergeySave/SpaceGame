package com.sergey.spacegame.common.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.event.Event;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.event.EventHandle;
import com.sergey.spacegame.common.event.LuaEventHandler;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.lua.LuaUtils;

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

public class Level {
    
    private static Level      _deserializing;
    private static FileSystem levelFile;
    
    private HashMap<String, Command>                         commands     = new HashMap<>();
    private HashMap<String, EntityPrototype>                 entities     = new HashMap<>();
    private HashMap<Class<? extends Event>, LuaEventHandler> events       = new HashMap<>();
    private HashMap<String, String>                          localization = new HashMap<>();
    
    private transient double                 money;
    private transient List<Objective>        objectives;
    private transient ECSManager             ecsManager;
    private transient ImmutableArray<Entity> planets;
    private transient LevelEventRegistry     levelEventRegistry;
    
    private Level() {
        _deserializing = this;
    
        objectives = new ArrayList<>();
        
        ecsManager = new ECSManager();
        ecsManager.addSystem(new MovementSystem());
        ecsManager.addSystem(new RotationSystem());
        ecsManager.addSystem(new BuildingSystem());
        ecsManager.addSystem(new PlanetSystem());
        
        planets = ecsManager.getEntitiesFor(Family.all(PlanetComponent.class).get());
    }
    
    public static Level tempLevelGet() {
        FileHandle levelZip = Gdx.files.internal("level.sgl");
        
        try {
            Level level = deserialize(levelZip);
            return level;
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        return null;
    }
    
    private static synchronized Level deserialize(FileHandle levelZip) throws IOException {
        FileSystem fileSystem = FileSystems.newFileSystem(levelZip.file().toPath(), null);
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
        
        for (Entry<String, Command> cmd : commands.entrySet()) {
            cmd.getValue().setId(cmd.getKey());
        }
    }
    
    public static Level deserializing() {
        return _deserializing;
    }
    
    public static FileSystem deserializingFileSystem() {
        return levelFile;
    }
    
    public void deinit() {
        SpaceGame.getInstance().getEventBus().unregisterAll(levelEventRegistry);
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
    
    public List<Objective> getObjectives() {
        return objectives;
    }
    
    public double getMoney() {
        return money;
    }
    
    public void setMoney(double money) {
        this.money = money;
    }
    
    public static class LevelEventRegistry {
        
        private FileSystem fileSystem;
        
        public LevelEventRegistry(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }
        
        @EventHandle
        public void onAtlasRegistry(AtlasRegistryEvent event) {
            try {
                Files.walkFileTree(fileSystem.getPath("images"), new FileWalker(event.getPacker()));
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
        
        private static class FileWalker implements FileVisitor<Path> {
            
            private PixmapPacker packer;
            
            public FileWalker(PixmapPacker packer) {
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
            
            level.commands = context.deserialize(obj.get("commands"), new TypeToken<HashMap<String, Command>>() {}.getType());
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
}
