package com.sergey.spacegame.client.ui.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.ecs.system.CommandUISystem;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectedRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectionSystem;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.game.command.MoveCommand;

public class GameScreen extends BaseScreen {

	private OrthographicCamera camera;

	private MainRenderSystem mainRenderSystem;
	private OrderRenderSystem orderRenderSystem;
	private SelectedRenderSystem selectedRenderSystem;
	private SelectionSystem selectionControlSystem;
	private CommandUISystem commandUISystem;

	private ECSManager ecsManager;

	private Entity e;
	
	private BuildingComponent building;

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		ecsManager = SpaceGame.getInstance().getECSManager();
		ecsManager.getEngine().addSystem(mainRenderSystem = new MainRenderSystem(camera));
		ecsManager.getEngine().addSystem(orderRenderSystem = new OrderRenderSystem(camera));
		ecsManager.getEngine().addSystem(selectedRenderSystem = new SelectedRenderSystem(camera));
		ecsManager.getEngine().addSystem(selectionControlSystem = new SelectionSystem(camera));
		ecsManager.getEngine().addSystem(commandUISystem = new CommandUISystem(camera));

		/*{
			Entity planet;
			e = new Entity();
			ecsManager.getEngine().addEntity(e);
			e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("planets/1")));
			e.add(new PositionComponent(250, 250));
			e.add(new SizeComponent(200, 200));
			planet = e;
			
			e = new Entity();
			ecsManager.getEngine().addEntity(e);
			e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("building/factory")));
			e.add(new PositionComponent());
			e.add(new SizeComponent(100,100));
			e.add(new RotationComponent(0, 0.5f, 0.5f));
			e.add(new BuildingComponent(planet, 0));
			e.add(new ControllableComponent());
		}*/
		
		Command moveCommand = new Command(new MoveCommand(), true, true);
		
		e = new Entity();
		ecsManager.getEngine().addEntity(e);

		e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("ships/pew")));
		e.add(new PositionComponent(50, 50));
		e.add(new VelocityComponent());
		e.add(new SizeComponent(25, 25));
		e.add(new RotationComponent(90, 0.5f, 0.5f));
		{
			ShipComponent ship = new ShipComponent();
			ship.moveSpeed = 200;
			ship.rotateSpeed = 22.5f;
			e.add(ship);
		}
		e.add(new ControllableComponent(moveCommand));

		e = new Entity();
		ecsManager.getEngine().addEntity(e);

		e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("ships/pew")));
		e.add(new PositionComponent(100, 50));
		e.add(new VelocityComponent());
		e.add(new SizeComponent(25, 25));
		e.add(new RotationComponent(90, 0.5f, 0.5f));
		{
			ShipComponent ship = new ShipComponent();
			ship.moveSpeed = 100;
			ship.rotateSpeed = 45;
			e.add(ship);
		}
		e.add(new ControllableComponent(moveCommand));
		
	}

	@Override
	public void render(float delta) {
		if (Gdx.input.isKeyPressed(Keys.W)) building.setPosition(building.getPosition()+1);
		if (Gdx.input.isKeyPressed(Keys.S)) building.setPosition(building.getPosition()-1);
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		ecsManager.getEngine().removeSystem(mainRenderSystem);
		ecsManager.getEngine().removeSystem(orderRenderSystem);
		ecsManager.getEngine().removeSystem(selectedRenderSystem);
		ecsManager.getEngine().removeSystem(selectionControlSystem);
		ecsManager.getEngine().removeSystem(commandUISystem);
	}

	@Override
	public void dispose() {
	}
}
