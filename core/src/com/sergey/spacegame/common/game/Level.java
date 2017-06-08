package com.sergey.spacegame.common.game;

import java.util.HashMap;
import java.util.Map.Entry;

import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.EntityPrototype;
import com.sergey.spacegame.common.game.command.Command;

public class Level {
	private static Level _deserializing;
	
	private HashMap<String, Command> commands = new HashMap<>();
	private HashMap<String, EntityPrototype> entities = new HashMap<>();
	
	public static Level tempLevelGet() {
		String json = "{\"commands\":{\"move\":{\"type\":\"java\",\"class\":\"com.sergey.spacegame.common.game.command.MoveCommandExecutable\",\"requiresInput\":true,\"requiresTwoInput\":true,\"name\":\"Move\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"},\"test\":{\"type\":\"lua\",\"lua\":\"local entities = selected.iterator()\nwhile entities.hasNext() do\nprint(entities.next())\nend\",\"requiresInput\":false,\"requiresTwoInput\":false,\"name\":\"Test\",\"iconName\":\"missingTexture\",\"pressedIconName\":\"missingTexture\"}},\"entities\":{\"shipTest1\":{\"com.sergey.spacegame.client.ecs.component.VisualComponent\":{\"image\":\"ship\"},\"com.sergey.spacegame.common.ecs.component.PositionComponent\":{},\"com.sergey.spacegame.common.ecs.component.VelocityComponent\":{},\"com.sergey.spacegame.common.ecs.component.SizeComponent\":{\"w\":25,\"h\":25},\"com.sergey.spacegame.common.ecs.component.RotationComponent\":{\"originX\":0.5,\"originY\":0.5},\"com.sergey.spacegame.common.ecs.component.ShipComponent\":{\"moveSpeed\":200,\"rotateSpeed\":45},\"com.sergey.spacegame.common.ecs.component.ControllableComponent\":[\"move\",\"test\"]}}}";
		Level level = SpaceGame.getInstance().getGson().fromJson(json, Level.class);
		_deserializing = null;
		return level;
		//commands.put("move", new Command(new MoveCommandExecutable(), true, true, "Move", "missingTexture", "missingTexture"));
	}
	
	public Level() {
		_deserializing = this;
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
}
