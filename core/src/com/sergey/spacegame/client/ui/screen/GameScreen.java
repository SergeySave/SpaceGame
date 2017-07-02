package com.sergey.spacegame.client.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.client.ecs.system.CommandUISystem;
import com.sergey.spacegame.client.ecs.system.HUDSystem;
import com.sergey.spacegame.client.ecs.system.InConstructionRenderSystem;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectedRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectionSystem;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.UIUtil;
import com.sergey.spacegame.common.ecs.ECSManager;
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

	private Level level;
	private DrawingBatch batch;
	
	public GameScreen(Level level) {
		this.level = level;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new DrawingBatch(1000, UIUtil.compileShader(Gdx.files.internal("shaders/basic.vertex.glsl"), Gdx.files.internal("shaders/basic.fragment.glsl")), true);
		batch.setLineWidth(1f);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ecsManager = level.getECS();
		ecsManager.getEngine().addSystem(orderSystem = new OrderSystem(level));
		
		ecsManager.getEngine().addSystem(mainRenderSystem = new MainRenderSystem(batch));
		ecsManager.getEngine().addSystem(orderRenderSystem = new OrderRenderSystem(batch));
		ecsManager.getEngine().addSystem(selectedRenderSystem = new SelectedRenderSystem(batch));
		ecsManager.getEngine().addSystem(inConstructionRenderSystem = new InConstructionRenderSystem(batch));
		ecsManager.getEngine().addSystem(commandUISystem = new CommandUISystem(camera, batch, level));
		ecsManager.getEngine().addSystem(selectionControlSystem = new SelectionSystem(camera, batch, commandUISystem));
		ecsManager.getEngine().addSystem(hudSystem = new HUDSystem(batch, commandUISystem));

		/*Entity e;
		{
			e = ecsManager.newEntity();
			ecsManager.getEngine().addEntity(e);
			e.add(new VisualComponent("planets/1"));
			e.add(new PositionComponent(250, 250));
			e.add(new SizeComponent(200, 200));
			e.add(new PlanetComponent());
			
			e = ecsManager.newEntity();
			ecsManager.getEngine().addEntity(e);
			e.add(new VisualComponent("planets/1"));
			e.add(new PositionComponent(750, 750));
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
		}*/
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
		/*
		e = level.getEntities().get("shipTest1").createEntity(level);
		e.add(new PositionComponent(150, 150));
		ecsManager.getEngine().addEntity(e);
		
		e = level.getEntities().get("shipTest1").createEntity(level);
		e.add(new PositionComponent(200, 150));
		ecsManager.getEngine().addEntity(e);*/
	}

	@Override
	public void render(float delta) {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ecsManager.getEngine().update(Gdx.graphics.getDeltaTime());
		batch.end();
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
		
		batch.dispose();
	}

	@Override
	public void dispose() {}
}
