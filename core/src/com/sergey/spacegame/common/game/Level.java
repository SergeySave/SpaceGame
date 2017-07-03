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
import java.util.HashMap;
import java.util.Map.Entry;

public class Level {
	private static Level _deserializing;
	
	private HashMap<String, Command> commands = new HashMap<>();
	private HashMap<String, EntityPrototype> entities = new HashMap<>();
	
	private transient ECSManager ecsManager;
	
	private transient ImmutableArray<Entity> planets;

	private transient LevelEventRegistry levelEventRegistry;
	
	public static Level tempLevelGet() {
		FileHandle levelZip = Gdx.files.internal("level.sgl");

		try {
			FileSystem fileSystem = FileSystems.newFileSystem(levelZip.file().toPath(), null);

			Path jsonPath = fileSystem.getPath("level.json");

			EventBus eventBus = SpaceGame.getInstance().getEventBus();
			LevelEventRegistry ler = new LevelEventRegistry(fileSystem.getPath("images"));
			eventBus.registerAnnotated(ler);

			SpaceGame.getInstance().regenerateAtlasNow();

			Level level = deserialize(jsonPath);
			level.init(ler);
			return level;
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
		return null;
	}
	
	private static synchronized Level deserialize(Path jsonFile) throws IOException {
		Level level = SpaceGame.getInstance().getGson().fromJson(Files.newBufferedReader(jsonFile), Level.class);
		_deserializing = null;
		return level;
	}
	
	private Level() {
		_deserializing = this;
		
		ecsManager = new ECSManager();
		ecsManager.getEngine().addSystem(new MovementSystem());
		ecsManager.getEngine().addSystem(new RotationSystem());
		ecsManager.getEngine().addSystem(new BuildingSystem());
		ecsManager.getEngine().addSystem(new PlanetSystem());
		
		planets = ecsManager.getEngine().getEntitiesFor(Family.all(PlanetComponent.class).get());
	}
	
	public void init(LevelEventRegistry ler) {
		levelEventRegistry = ler;

		for (Entry<String, Command> cmd : commands.entrySet()) {
			cmd.getValue().setId(cmd.getKey());
		}
	}

	public void deinit() {
		SpaceGame.getInstance().getEventBus().unregisterAll(levelEventRegistry);
	}

	public static Level deserializing() {
		return _deserializing;
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

	public static class LevelEventRegistry {
		private Path imagesFolder;

		public LevelEventRegistry(Path imagesFolder) {
			this.imagesFolder = imagesFolder;
		}

		@EventHandle
		public void onAtlasRegistry(AtlasRegistryEvent event) {
			try {
				Files.walkFileTree(imagesFolder, new FileWalker(event.getPacker()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private static class FileWalker implements FileVisitor<Path> {
			private  PixmapPacker packer;

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
						String name = file.toString().substring(0, file.toString().lastIndexOf('.')).replaceFirst("/images/", "");
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
		public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();

			Level level = new Level();

			level.commands = context.deserialize(obj.get("commands"), new TypeToken<HashMap<String, Command>>(){}.getType());
			level.entities = context.deserialize(obj.get("entities"), new TypeToken<HashMap<String, EntityPrototype>>(){}.getType());

			JsonArray levelDefaults = obj.getAsJsonArray("state");
			if (levelDefaults != null) {
				for (JsonElement element : levelDefaults) {
					Entity entity = context.deserialize(element, Entity.class);
					level.getECS().getEngine().addEntity(entity);
				}
			}

			if (obj.has("events")) {
				for (Entry<String, JsonElement> event : obj.getAsJsonObject("events").entrySet()) {
					String eventClassStr = event.getKey();
					String lua = event.getValue().getAsString();

					try {
						@SuppressWarnings("unchecked")
						Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventClassStr);

						LuaEventHandler handler = new LuaEventHandler(lua);

						//Level event handlers are registered on the level to simplify deregistration later on
						SpaceGame.getInstance().getEventBus().registerSpecific(level, eventClass, handler::execute);
					} catch (ClassNotFoundException e) {
						System.out.println("Failed to find event " + eventClassStr + " for event handler. Not loaded.");
					} catch (ClassCastException e) {
						System.out.println("Key " + eventClassStr + " must be a type of event.");
					}
				}
			}
			//SpaceGame.getInstance().getEventBus().registerSpecific(level, SelectionChangeEvent.class, new LuaEventHandler("print(event:getSelected())")::execute);

			return level;
		}

		@Override
		public JsonElement serialize(Level src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();

			obj.add("commands", context.serialize(src.commands, new TypeToken<HashMap<String, Command>>(){}.getType()));
			obj.add("entities", context.serialize(src.entities, new TypeToken<HashMap<String, EntityPrototype>>(){}.getType()));

			JsonArray state = new JsonArray();

			for (Entity entity : src.getECS().getEngine().getEntities()) {
				state.add(context.serialize(entity));
			}

			obj.add("state", state);

			return obj;
		}

	}
}
