package com.sergey.spacegame.client.ecs.system;

import java.util.stream.StreamSupport;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.orders.FaceOrder;
import com.sergey.spacegame.common.orders.MoveOrder;
import com.sergey.spacegame.common.orders.TimeMoveOrder;

public class SelectionControlSystem extends EntitySystem {

	private OrthographicCamera camera;
	private ShapeRenderer shape;
	private Vector2 selectionBegin;

	private Vector2 orderCenter;

	private ImmutableArray<Entity> selectedEntities;
	private ImmutableArray<Entity> controllableEntities;

	public SelectionControlSystem(OrthographicCamera camera) {
		super(4);
		this.camera = camera;
	}

	@Override
	public void addedToEngine (Engine engine) {
		selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class).get());
		controllableEntities = engine.getEntitiesFor(Family.all(ControllableComponent.class, PositionComponent.class).get());
		shape = new ShapeRenderer();
	}

	@Override
	public void removedFromEngine (Engine engine) {
		selectedEntities = null;
		controllableEntities = null;
		shape.dispose();
	}

	@Override
	public void update(float deltaTime) {
		shape.setProjectionMatrix(camera.combined);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			selectionBegin = new Vector2(vec.x, vec.y);
		}
		if (selectionBegin != null) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			shape.begin(ShapeType.Filled);{
				shape.setColor(0.4f, 0.4f, 1f, 0.5f);
				shape.rect(selectionBegin.x, selectionBegin.y, vec.x-selectionBegin.x, vec.y-selectionBegin.y);
			}shape.end();
			if (!Gdx.input.isButtonPressed(Buttons.LEFT)) {
				selectedEntities.forEach((e)->e.remove(SelectedComponent.class));
				Rectangle rect = new Rectangle(Math.min(vec.x, selectionBegin.x), Math.min(vec.y, selectionBegin.y), Math.abs(vec.x-selectionBegin.x), Math.abs(vec.y-selectionBegin.y));
				controllableEntities.forEach((e)->{
					PositionComponent pos = PositionComponent.MAPPER.get(e);
					if (SizeComponent.MAPPER.has(e)) {
						SizeComponent size = SizeComponent.MAPPER.get(e);
						if (rect.overlaps(new Rectangle(pos.x, pos.y, size.w, size.h))) {
							e.add(new SelectedComponent());
						}
					} else {
						if (rect.contains(new Vector2(pos.x, pos.y))) {
							e.add(new SelectedComponent());
						}
					}
				});
				selectionBegin = null;
			}
		}
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			orderCenter = new Vector2(vec.x, vec.y);
		}
		if (orderCenter != null) {
			Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			shape.begin(ShapeType.Line);{
				shape.setColor(Color.WHITE);
				shape.line(orderCenter.x, orderCenter.y, vec.x, vec.y);
			}shape.end();
			if (!Gdx.input.isButtonPressed(Buttons.RIGHT)) {
				if (selectedEntities.size() > 0) {
					Vector2 center = StreamSupport.stream(selectedEntities.spliterator(), true).collect(Vector2::new, (v,s)->v.add(new Vector2(PositionComponent.MAPPER.get(s).x,PositionComponent.MAPPER.get(s).y)), (v1,v2)->v1.add(v2));
					center.scl(1f/selectedEntities.size());
					float desiredFleetDir = orderCenter.cpy().sub(center).angle();
					float dx = orderCenter.x-center.x;
					float dy = orderCenter.y-center.y;
					double ds = Math.sqrt(dx*dx + dy*dy);
					float dr = new Vector2(vec.x-orderCenter.x, vec.y-orderCenter.y).angle();
					boolean fleetOrder = selectedEntities.size()>1;
					if (fleetOrder) {
						Vector2 farCenter = center.cpy().add(dx, dy);
						
						//Average Angle
						float fleetDir = StreamSupport.stream(selectedEntities.spliterator(), true).filter(RotationComponent.MAPPER::has).map((e)->new Vector2(1,0).rotate(RotationComponent.MAPPER.get(e).r)).collect(Vector2::new, (v1,v2)->v1.add(v2), (v1,v2)->v1.add(v2)).angle();
						//float movementDir = orderCenter.cpy().sub(center).angle();
						float dr1 = desiredFleetDir-fleetDir;
						float dr2 = dr-desiredFleetDir;
						
						//Time to rotate fleet, move fleet, and rotate the fleet again
						double[] maxTimes = StreamSupport.stream(selectedEntities.spliterator(), true).map((e)->{
							boolean doesTurn = RotationComponent.MAPPER.has(e);
							float ang;
							ShipComponent ship = ShipComponent.MAPPER.get(e);
							Vector2 startPos, endPos, deltaPos;
							double[] times = new double[3];
							
							startPos = PositionComponent.MAPPER.get(e).createVector();
							endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
							deltaPos = endPos.cpy().sub(startPos);
							ang = deltaPos.angle();
							//Turn time 1
							times[0] = (doesTurn ? getThroughRotateDistance(ang,RotationComponent.MAPPER.get(e).r)/ship.rotateSpeed : 0) + deltaPos.len()/ship.moveSpeed + (doesTurn ? getThroughRotateDistance(desiredFleetDir,ang)/ship.rotateSpeed : 0);
							
							//startPos = endPos;
							//endPos = startPos.cpy().add(dx, dy);
							//deltaPos = endPos.cpy().sub(startPos);
							startPos.add(dx, dy);
							//Move time
							times[1] = ds/ship.moveSpeed;
							
							startPos = endPos;
							endPos = startPos.cpy().sub(center).rotate(dr2).add(center);
							deltaPos = endPos.cpy().sub(startPos);
							ang = deltaPos.angle();
							//Turn time 2
							times[2] = (doesTurn ? getThroughRotateDistance(ang,desiredFleetDir)/ship.rotateSpeed : 0) + deltaPos.len()/ship.moveSpeed + (doesTurn ? getThroughRotateDistance(dr,ang)/ship.rotateSpeed : 0);
							
							return times;
						}).reduce(new double[3], (r,e)->new double[]{Math.max(r[0], e[0]),Math.max(r[1], e[1]),Math.max(r[2], e[2])});
						
						float time = (float) (maxTimes[1]);
						
						selectedEntities.forEach((e)->{
							boolean doesTurn = RotationComponent.MAPPER.has(e);
							ShipComponent ship = ShipComponent.MAPPER.get(e);
							float ang;
							OrderComponent ord = new OrderComponent();
							Vector2 startPos, endPos, deltaPos;
							
							if (doesTurn) {
								startPos = PositionComponent.MAPPER.get(e).createVector();
								endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
								deltaPos = endPos.cpy().sub(startPos);
								ang = deltaPos.angle();
								
								//Fleet rotate 1
								ord.orders.add(new FaceOrder(ang, ship.rotateSpeed));
								double maxTime = maxTimes[0];
								float d1 = getThroughRotateDistance(ang,desiredFleetDir);
								float d2 = getThroughRotateDistance(ang,RotationComponent.MAPPER.get(e).r);
								float dt = d1 + d2;
								float tr = dt/ship.rotateSpeed;
								float mT = (float) (maxTime - tr);
								ord.orders.add(new TimeMoveOrder(endPos.x, endPos.y, mT));
								ord.orders.add(new FaceOrder(desiredFleetDir, ship.rotateSpeed)); 

								//Fleet move
								ord.orders.add(new TimeMoveOrder(endPos.x+dx, endPos.y+dy, time));

								startPos = endPos.add(dx, dy);
								endPos = startPos.cpy().sub(farCenter).rotate(dr2).add(farCenter);
								deltaPos = endPos.cpy().sub(startPos);
								ang = deltaPos.angle();

								//Fleet rotate 2
								ord.orders.add(new FaceOrder(ang, ship.rotateSpeed));
								mT = (float) (maxTimes[2]-(getThroughRotateDistance(ang,desiredFleetDir)+getThroughRotateDistance(ang,dr))/ship.rotateSpeed);
								ord.orders.add(new TimeMoveOrder(endPos.x, endPos.y, mT));
								ord.orders.add(new FaceOrder(dr, ship.rotateSpeed));
							} else {
								startPos = PositionComponent.MAPPER.get(e).createVector();
								endPos = startPos.cpy().sub(center).rotate(dr1).add(center);
								deltaPos = endPos.cpy().sub(startPos);
								
								ord.orders.add(new TimeMoveOrder(endPos.x, endPos.y, (float) (maxTimes[0])));
								ord.orders.add(new TimeMoveOrder(endPos.x+dx, endPos.y+dy, time));
								
								startPos = endPos.add(dx, dy);
								endPos = startPos.cpy().sub(farCenter).rotate(dr2).add(farCenter);
								deltaPos = endPos.cpy().sub(startPos);
								
								ord.orders.add(new TimeMoveOrder(endPos.x, endPos.y, (float) (maxTimes[2])));
							}

							e.add(ord);
						});
					} else {
						float speed = (float) StreamSupport.stream(selectedEntities.spliterator(), true).mapToDouble((e)->ShipComponent.MAPPER.get(e).moveSpeed).min().getAsDouble();
						selectedEntities.forEach((e)->{
							OrderComponent ord = new OrderComponent(
									new FaceOrder(desiredFleetDir, 45),
									new MoveOrder(PositionComponent.MAPPER.get(e).x+dx, PositionComponent.MAPPER.get(e).y+dy, speed),
									new FaceOrder(dr, 45));

							e.add(ord);
						});
					}
				}
				orderCenter = null;
			}
		}
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	private static float getThroughRotateDistance(float angle1, float angle2) {
		float dr = Math.abs(angle1 - angle2)%360;
		if (dr > 180) return 360-dr;
		return dr;
	}
}
