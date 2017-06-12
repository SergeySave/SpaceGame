package com.sergey.spacegame.common.game;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.MovementSystem;
import com.sergey.spacegame.common.ecs.system.RotationSystem;
import com.sergey.spacegame.common.game.command.Command;

public class Level {
	private static Level _deserializing;
	
	private HashMap<String, Command> commands = new HashMap<>();
	private HashMap<String, EntityPrototype> entities = new HashMap<>();
	
	private ECSManager ecsManager;
	
	private ImmutableArray<Entity> planets;
	
	public static Level tempLevelGet() {
		String json = "{\"commands\":{\"move\":{\"type\":\"java\",\"class\":\"com.sergey.spacegame.common.game.command.MoveCommandExecutable\",\"requiresInput\":true,\"requiresTwoInput\":true,\"name\":\"Move\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"},\"test\":{\"type\":\"lua\",\"lua\":\"local entities = selected.iterator()\nwhile entities.hasNext() do\n\tlocal entity = entities.next()\n\taddOrder(entity, orders.BuildBuildingOrder.new('buildingTest', 5, x1, y1), orders.BuildBuildingOrder)\nend\n\",\"requiresInput\":true,\"requiresTwoInput\":false,\"name\":\"Test\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"}},\"entities\":{\"shipTest1\":{\"com.sergey.spacegame.client.ecs.component.VisualComponent\":{\"image\":\"ships/pew\"},\"com.sergey.spacegame.common.ecs.component.PositionComponent\":{},\"com.sergey.spacegame.common.ecs.component.VelocityComponent\":{},\"com.sergey.spacegame.common.ecs.component.SizeComponent\":{\"w\":25,\"h\":25},\"com.sergey.spacegame.common.ecs.component.RotationComponent\":{\"originX\":0.5,\"originY\":0.5},\"com.sergey.spacegame.common.ecs.component.ShipComponent\":{\"moveSpeed\":200,\"rotateSpeed\":45},\"com.sergey.spacegame.common.ecs.component.ControllableComponent\":[\"move\",\"test\"]},\"buildingTest\":{\"com.sergey.spacegame.client.ecs.component.VisualComponent\":{\"image\":\"building/factory\"},\"com.sergey.spacegame.common.ecs.component.PositionComponent\":{},\"com.sergey.spacegame.common.ecs.component.VelocityComponent\":{},\"com.sergey.spacegame.common.ecs.component.SizeComponent\":{\"w\":250,\"h\":250},\"com.sergey.spacegame.common.ecs.component.RotationComponent\":{\"originX\":0.5,\"originY\":0.5},\"com.sergey.spacegame.common.ecs.component.ControllableComponent\":[]}}}";
		return deserialize(json);
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
