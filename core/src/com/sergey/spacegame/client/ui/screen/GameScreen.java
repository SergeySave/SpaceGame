package com.sergey.spacegame.client.ui.screen;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.ecs.component.VisualComponent;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectedRenderSystem;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.orders.MoveOrder;

public class GameScreen implements Screen {

	private OrthographicCamera camera;
	
	private MainRenderSystem mainRenderSystem;
	private OrderRenderSystem orderRenderSystem;
	private SelectedRenderSystem selectedRenderSystem;
	
	private ECSManager ecsManager;
	
	private Entity e;
	
	private ShapeRenderer shape;

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		ecsManager = SpaceGame.getInstance().getECSManager();
		ecsManager.getEngine().addSystem(mainRenderSystem = new MainRenderSystem(camera));
		ecsManager.getEngine().addSystem(orderRenderSystem = new OrderRenderSystem(camera));
		ecsManager.getEngine().addSystem(selectedRenderSystem = new SelectedRenderSystem(camera));
		
		e = new Entity();
		ecsManager.getEngine().addEntity(e);
		
		e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("ships/pew")));
		e.add(new PositionComponent(50, 50));
		e.add(new VelocityComponent());
		e.add(new SizeComponent(100, 100));
		e.add(new RotationComponent(0, 0.5f, 0.5f));
		
		e = new Entity();
		ecsManager.getEngine().addEntity(e);
		
		e.add(new VisualComponent(SpaceGame.getInstance().getAtlas().findRegion("ships/pew")));
		e.add(new PositionComponent(50, 50));
		e.add(new VelocityComponent());
		e.add(new SizeComponent(100, 100));
		e.add(new RotationComponent(0, 0.5f, 0.5f));
		ShipComponent ship = new ShipComponent();
		ship.moveSpeed = 100;
		e.add(ship);
		e.add(new ControllableComponent());
		e.add(new SelectedComponent());

		shape = new ShapeRenderer();
	}

	@Override
	public void render(float delta) {
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			e.add(new OrderComponent(new MoveOrder(vec.x, vec.y)));
		}
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
	}

	@Override
	public void dispose() {
		shape.dispose();
	}
}
