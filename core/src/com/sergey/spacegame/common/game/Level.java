package com.sergey.spacegame.common.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.event.AtlasRegistryEvent;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.event.EventBus;
import com.sergey.spacegame.common.event.EventHandle;
import com.sergey.spacegame.common.game.command.Command;

import java.io.File;
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
		FileHandle levelFolder = Gdx.files.internal("levelFolder");

		EventBus eventBus = SpaceGame.getInstance().getEventBus();
		LevelEventRegistry ler = new LevelEventRegistry(levelFolder.child("images"));
		eventBus.register(ler);

		SpaceGame.getInstance().regenerateAtlasNow();

		Level level = deserialize(levelFolder.child("level.json"));
		level.init(ler);
		return level;
	}
	
	private static synchronized Level deserialize(FileHandle jsonFile) {
		Level level = SpaceGame.getInstance().getGson().fromJson(jsonFile.reader(), Level.class);
		_deserializing = null;
		return level;
	}
	
	public Level() {
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
		SpaceGame.getInstance().getEventBus().unregister(levelEventRegistry);
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
		private FileHandle imagesFolder;

		public LevelEventRegistry(FileHandle imagesFolder) {
			this.imagesFolder = imagesFolder;
		}

		@EventHandle
		public void onAtlasRegistry(AtlasRegistryEvent event) {
			recurLoad(event.getPacker(), "", imagesFolder.list(this::fileFilter));
		}

		private void recurLoad(PixmapPacker packer, String dir, FileHandle[] files) {
			for (FileHandle file : files) {
				if (file.isDirectory()) {
					recurLoad(packer, (dir.isEmpty() ? "" : dir + "/") + file.name(), file.list(this::fileFilter));
				} else {
					try {
						packer.pack(dir + "/" + file.nameWithoutExtension(), new Pixmap(file));
					} catch (GdxRuntimeException e) {
						System.out.println("Failed to load file: " + dir + "/" + file.name() + " as an image.");
					}
				}
			}
		}

		private boolean fileFilter(File file) {
			return file.exists() && !file.isHidden();
		}
	}
}
