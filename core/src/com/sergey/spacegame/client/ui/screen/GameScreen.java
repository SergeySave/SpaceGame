package com.sergey.spacegame.client.ui.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.ecs.system.CommandUISystem;
import com.sergey.spacegame.client.ecs.system.HUDSystem;
import com.sergey.spacegame.client.ecs.system.InConstructionRenderSystem;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectedRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectionSystem;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.game.Level;

public class GameScreen extends BaseScreen {

	private OrthographicCamera camera;

	private OrderSystem orderSystem;
	private MainRenderSystem mainRenderSystem;
	private OrderRenderSystem orderRenderSystem;
	private SelectedRenderSystem selectedRenderSystem;
	private InConstructionRenderSystem inConstructionRenderSystem;
	private SelectionSystem selectionControlSystem;
	private CommandUISystem commandUISystem;
	private HUDSystem hudSystem;

	private ECSManager ecsManager;

	private Entity e;
	
	private BuildingComponent building;
	
	private Level level;
	
	public GameScreen(Level level) {
		this.level = level;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		ecsManager = level.getECS();
		ecsManager.getEngine().addSystem(orderSystem = new OrderSystem(level));
		ecsManager.getEngine().addSystem(mainRenderSystem = new MainRenderSystem(camera));
		ecsManager.getEngine().addSystem(orderRenderSystem = new OrderRenderSystem(camera));
		ecsManager.getEngine().addSystem(selectedRenderSystem = new SelectedRenderSystem(camera));
		ecsManager.getEngine().addSystem(selectionControlSystem = new SelectionSystem(camera));
		ecsManager.getEngine().addSystem(inConstructionRenderSystem = new InConstructionRenderSystem(camera));
		ecsManager.getEngine().addSystem(commandUISystem = new CommandUISystem(camera, level));
		ecsManager.getEngine().addSystem(hudSystem = new HUDSystem(commandUISystem));

		{
			e = ecsManager.newEntity();
			ecsManager.getEngine().addEntity(e);
			e.add(new VisualComponent("planets/1"));
			e.add(new PositionComponent(250, 250));
			e.add(new SizeComponent(200, 200));
			e.add(new PlanetComponent());
			
			//e = ecsManager.newEntity();
			//ecsManager.getEngine().addEntity(e);
			//e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("building/factory")));
			//e.add(new PositionComponent());
			//e.add(new SizeComponent(100,100));
			//e.add(new RotationComponent(0, 0.5f, 0.5f));
			//e.add(new BuildingComponent(planet, 0));
			//e.add(new ControllableComponent());
		}
		/*
		Command moveCommand = level.getCommands().get("move");
		Command testCommand = level.getCommands().get("test");
		
		e = ecsManager.newEntity();
		ecsManager.getEngine().addEntity(e);

		e.add(new VisualComponent("ships/pew"));
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
		e.add(new ControllableComponent(moveCommand, testCommand));

		e = ecsManager.newEntity();
		ecsManager.getEngine().addEntity(e);

		e.add(new VisualComponent("ships/pew"));
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
		e.add(new ControllableComponent(moveCommand, testCommand));*/
		
		e = level.getEntities().get("shipTest1").createEntity(level);
		e.add(new PositionComponent(150, 150));
		ecsManager.getEngine().addEntity(e);
		
		e = level.getEntities().get("shipTest1").createEntity(level);
		e.add(new PositionComponent(200, 150));
		ecsManager.getEngine().addEntity(e);
	}

	@Override
	public void render(float delta) {
		ecsManager.getEngine().update(Gdx.graphics.getDeltaTime());
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
		ecsManager.getEngine().removeSystem(orderSystem);
		ecsManager.getEngine().removeSystem(mainRenderSystem);
		ecsManager.getEngine().removeSystem(orderRenderSystem);
		ecsManager.getEngine().removeSystem(selectedRenderSystem);
		ecsManager.getEngine().removeSystem(selectionControlSystem);
		ecsManager.getEngine().removeSystem(inConstructionRenderSystem);
		ecsManager.getEngine().removeSystem(commandUISystem);
		ecsManager.getEngine().removeSystem(hudSystem);
	}

	@Override
	public void dispose() {
	}
}
