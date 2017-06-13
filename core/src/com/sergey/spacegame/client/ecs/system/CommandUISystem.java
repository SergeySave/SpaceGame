package com.sergey.spacegame.client.ecs.system;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.command.Command;

public class CommandUISystem extends EntitySystem {

	private OrthographicCamera camera;
	private ShapeRenderer shape;

	private Vector2 orderCenter;
	
	private Command command;

	private ImmutableArray<Entity> selectedEntities;

	private Level level;
	
	public CommandUISystem(OrthographicCamera camera, Level level) {
		super(5);
		this.camera = camera;
		this.level = level;
	}

	@Override
	public void addedToEngine (Engine engine) {
		selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class, ControllableComponent.class).get());
		shape = new ShapeRenderer();
	}

	@Override
	public void removedFromEngine (Engine engine) {
		selectedEntities = null;
		shape.dispose();
	}

	@Override
	public void update(float deltaTime) {
		if (command == null) {
			command = level.getCommands().get("move");
			if (command == null) {
				return;
			}
		}
		if (!command.isRequiresInput()) {
			List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true).filter((e)->ControllableComponent.MAPPER.get(e).commands.contains(command)).collect(Collectors.toList());
			SpaceGame.getInstance().getCommandExecutor().executeCommand(command.getExecutable(), entities, entities.size(), Vector2.Zero, Vector2.Zero, level);
			return;
		}
		
		shape.setProjectionMatrix(camera.combined);

		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			if (!command.isRequiresTwoInput()) {
				List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true).filter((e)->ControllableComponent.MAPPER.get(e).commands.contains(command)).collect(Collectors.toList());
				SpaceGame.getInstance().getCommandExecutor().executeCommand(command.getExecutable(), entities, entities.size(), new Vector2(vec.x, vec.y), Vector2.Zero, level);
				return;
			}
			orderCenter = new Vector2(vec.x, vec.y);
		}
		if (orderCenter != null) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			shape.begin(ShapeType.Line);{
				shape.setColor(Color.WHITE);
				shape.line(orderCenter.x, orderCenter.y, vec.x, vec.y);
			}shape.end();
			if (!Gdx.input.isButtonPressed(Buttons.RIGHT)) {
				List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true).filter((e)->ControllableComponent.MAPPER.get(e).commands.contains(command)).collect(Collectors.toList());
				SpaceGame.getInstance().getCommandExecutor().executeCommand(command.getExecutable(), entities, entities.size(), orderCenter, new Vector2(vec.x, vec.y), level);
				orderCenter = null;
				return;
			}
		}
	}

	public void setCommand(Command command) {
		this.command = command;
	}
	
	public Command getCommand() {
		return command;
	}
}
