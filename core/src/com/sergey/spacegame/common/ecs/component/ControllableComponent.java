package com.sergey.spacegame.common.ecs.component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.sergey.spacegame.common.game.command.Command;

public class ControllableComponent implements Component {
	public static final ComponentMapper<ControllableComponent> MAPPER = ComponentMapper.getFor(ControllableComponent.class);
	
	public List<Command> commands;
	
	public ControllableComponent() {
		this.commands = new LinkedList<Command>();
	}

	public ControllableComponent(Command... commands) {
		this();
		this.commands.addAll(Arrays.asList(commands));
	}
}
