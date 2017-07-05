package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.Level;

public class TimeMoveOrder implements IOrder {
    
    private double  x;
    private double  y;
    private float   time;
    private boolean done;
    
    public TimeMoveOrder(double x, double y, float time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (done) {
            return;
        }
        
        PositionComponent pos = PositionComponent.MAPPER.get(e);
        VelocityComponent vel = VelocityComponent.MAPPER.get(e);
        if (vel == null) {
            vel = new VelocityComponent();
            e.add(vel);
        }
        //ShipComponent ship = ShipComponent.MAPPER.get(e);
        
        double dx = x - pos.x;
        double dy = y - pos.y;
        if (time < 0) {
            done = true;
            pos.x = (float) x;
            pos.y = (float) y;
            e.remove(VelocityComponent.class);
        } else {
            vel.vx = (float) (dx / time);
            vel.vy = (float) (dy / time);
            time -= deltaTime;
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return PositionComponent.MAPPER.has(e) && ShipComponent.MAPPER.has(e);
    }
    
    @Override
    public boolean completed() {
        return done;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}
