package com.sergey.spacegame.common.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.game.command.Command;
import org.intellij.lang.annotations.Language;

import java.util.HashMap;
import java.util.Map.Entry;

public class Level {
	private static Level _deserializing;
	
	private HashMap<String, Command> commands = new HashMap<>();
	private HashMap<String, EntityPrototype> entities = new HashMap<>();
	
	private ECSManager ecsManager;
	
	private ImmutableArray<Entity> planets;
	
	public static Level tempLevelGet() {
		@Language("JSON") String json = "{\n" +
				"\t\"commands\":{\n" +
				"\t\t\"move\":{\n" +
				"\t\t\t\"type\":\"java\",\n" +
				"\t\t\t\"class\":\"com.sergey.spacegame.common.game.command.MoveCommandExecutable\",\n" +
				"\t\t\t\"requiresInput\":true,\n" +
				"\t\t\t\"requiresTwoInput\":true,\n" +
				"\t\t\t\"name\":\"Move\",\n" +
				"\t\t\t\"iconName\":\"icons/gotoarrow\",\n" +
				"\t\t\t\"pressedIconName\":\"missingTexture\"\n" +
				"\t\t},\n" +
				"\t\t\"test\":{\n" +
				"\t\t\t\"type\":\"lua\",\n" +
				"\t\t\t\"lua\":\"local entities = selected.iterator()\\nwhile entities.hasNext() do\\n\\tlocal entity = entities.next()\\n\\taddOrder(entity, orders.BuildBuildingOrder.new('buildingTest', 5, x1, y1), orders.BuildBuildingOrder)\\nend\",\n" +
				"\t\t\t\"requiresInput\":true,\n" +
				"\t\t\t\"requiresTwoInput\":false,\n" +
				"\t\t\t\"name\":\"Test\",\n" +
				"\t\t\t\"iconName\":\"building/factory\",\n" +
				"\t\t\t\"pressedIconName\":\"missingTexture\",\n" +
				"\t\t\t\"cursor\":{\n" +
				"\t\t\t\t\"class\":\"com.sergey.spacegame.client.ui.cursor.BuildingConstructionCursorOverride\",\n" +
				"\t\t\t\t\"entity\":\"buildingTest\"}\n" +
				"\t\t}\n" +
				"\t},\n" +
				"\t\"entities\":{\n" +
				"\t\t\"shipTest1\":{\n" +
				"\t\t\t\"com.sergey.spacegame.client.ecs.component.VisualComponent\":{\"image\":\"ships/pew\"},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.PositionComponent\":{},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.VelocityComponent\":{},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.SizeComponent\":{\"w\":25,\"h\":25},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.RotationComponent\":{\"originX\":0.5,\"originY\":0.5},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.ShipComponent\":{\"moveSpeed\":200,\"rotateSpeed\":45},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.ControllableComponent\":[\"move\",\"test\"]\n" +
				"\t\t},\n" +
				"\t\t\"buildingTest\":{\n" +
				"\t\t\t\"com.sergey.spacegame.client.ecs.component.VisualComponent\":{\"image\":\"building/factory\"},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.PositionComponent\":{},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.VelocityComponent\":{},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.SizeComponent\":{\"w\":50,\"h\":50},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.RotationComponent\":{\"originX\":0.5,\"originY\":0.5},\n" +
				"\t\t\t\"com.sergey.spacegame.common.ecs.component.ControllableComponent\":[]\n" +
				"\t\t}\n" +
				"\t}\n" +
				"}";
		SpaceGame.getInstance().regenerateAtlasNow();
		@SuppressWarnings("UnnecessaryLocalVariable") Level level = deserialize(json);
		return level;
		//commands.put("move", new Command(new MoveCommandExecutable(), true, true, "Move", "missingTexture", "missingTexture"));
	}
	
	private static synchronized Level deserialize(String json) {
		Level level = SpaceGame.getInstance().getGson().fromJson(json, Level.class);
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
	
	public void init() {
		for (Entry<String, Command> cmd : commands.entrySet()) {
			cmd.getValue().setId(cmd.getKey());
		}
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
}
