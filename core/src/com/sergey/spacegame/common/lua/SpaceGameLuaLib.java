package com.sergey.spacegame.common.lua;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.ecs.component.TagComponent;
import com.sergey.spacegame.common.event.BeginLevelEvent;
import com.sergey.spacegame.common.event.EventHandle;
import com.sergey.spacegame.common.event.LuaDelayEvent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.Objective;
import com.sergey.spacegame.common.game.orders.BuildBuildingOrder;
import com.sergey.spacegame.common.game.orders.BuildShipOrder;
import com.sergey.spacegame.common.game.orders.FaceOrder;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.game.orders.MoveOrder;
import com.sergey.spacegame.common.game.orders.TimeMoveOrder;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.HashSet;
import java.util.Set;

public class SpaceGameLuaLib extends TwoArgFunction {
    
    public static final SpaceGameLuaLib INSTANCE = new SpaceGameLuaLib();
    
    private static final Set<Class<? extends IOrder>> ORDERS;
    
    static {
        ORDERS = new HashSet<>();
        ORDERS.add(BuildShipOrder.class);
        ORDERS.add(BuildBuildingOrder.class);
        ORDERS.add(FaceOrder.class);
        ORDERS.add(MoveOrder.class);
        ORDERS.add(TimeMoveOrder.class);
    }
    
    private Level currLevel;
    
    private SpaceGameLuaLib() {}
    
    public LuaValue call(LuaValue modname, LuaValue env) {
        env.set("addOrder", new AddOrder());
        env.set("postDelayEvent", new Lua3Arg((millis, id, parameter) -> {
            SpaceGame.getInstance().dispatchDelayedEvent(millis.checklong(), new LuaDelayEvent(id, parameter));
            return NIL;
        }));
        env.set("addObjective", new Lua3Arg((id, title, description) -> {
            currLevel.getObjectives()
                    .add(new Objective(id.checkjstring(), title.checkjstring(), description.checkjstring(), false));
            return NIL;
        }));
        env.set("getObjective", new Lua1Arg((id) -> currLevel.getObjectives()
                .stream()
                .filter((c) -> c.getId().equals(id.checkjstring()))
                .findAny()
                .map(CoerceJavaToLua::coerce)
                .orElse(NIL)));
        env.set("removeObjective", new Lua1Arg((objective) -> {
            //noinspection RedundantCast
            currLevel.getObjectives().remove((Objective) CoerceLuaToJava.coerce(objective, Objective.class));
            return NIL;
        }));
        env.set("getMoney", new Lua0Arg(() -> LuaValue.valueOf(currLevel.getMoney())));
        env.set("setMoney", new Lua1Arg((money) -> {
            currLevel.setMoney(money.checkdouble());
            return NIL;
        }));
        env.set("getTag", new Lua1Arg((entity) -> {
            Entity entity1 = (Entity) CoerceLuaToJava.coerce(entity, Entity.class);
            if (TagComponent.MAPPER.has(entity1)) {
                return LuaValue.valueOf(TagComponent.MAPPER.get(entity1).getTag());
            } else {
                return NIL;
            }
        }));
        env.set("spawnEntity", new Lua1Arg((entityName) -> {
            Entity entity = currLevel.getEntities().get(entityName.checkjstring()).createEntity(currLevel);
            currLevel.getECS().addEntity(entity);
            return CoerceJavaToLua.coerce(entity);
        }));
        LuaTable dataTable = new LuaTable();
        for (int i = 0; i < 10; i++) {
            final int n = i;
            dataTable.set("get" + n, new Lua0Arg(() -> currLevel.getLuaStores()[n])); //currLevel.getLuaStores()[i]
            dataTable.set("set" + n, new Lua1Arg((v) -> {
                currLevel.getLuaStores()[n] = v;
                return NIL;
            })); //currLevel.getLuaStores()[i]
        }
        env.set("data", dataTable);
        
        LuaTable ordersTable = new LuaTable();
        for (Class<? extends IOrder> clazz : ORDERS) {
            ordersTable.set(clazz.getSimpleName(), CoerceJavaToLua.coerce(clazz));
        }
        env.set("orders", ordersTable);
    
        return NIL;
    }
    
    @EventHandle
    public void onLevelStart(BeginLevelEvent event) {
        currLevel = event.getLevel();
    }
    
    
    public class AddOrder extends ThreeArgFunction {
        
        @SuppressWarnings("rawtypes")
        @Override
        public LuaValue call(LuaValue entityLua, LuaValue order, LuaValue className) {
            Entity         entity = (Entity) CoerceLuaToJava.coerce(entityLua, Entity.class);
            OrderComponent orderComp;
            if (OrderComponent.MAPPER.has(entity)) {
                orderComp = OrderComponent.MAPPER.get(entity);
            } else {
                orderComp = new OrderComponent();
                entity.add(orderComp);
            }
            IOrder orderObj = (IOrder) CoerceLuaToJava.coerce(order, (Class) CoerceLuaToJava.coerce(className, Class.class));
            orderComp.addOrder(orderObj);
            return NIL;
        }
    }
    
    
    public static class Lua0Arg extends ZeroArgFunction {
        
        private Function function;
        
        public Lua0Arg(Function function) {
            this.function = function;
        }
        
        @Override
        public LuaValue call() {
            return function.call();
        }
        
        @FunctionalInterface
        private interface Function {
            
            LuaValue call();
        }
    }
    
    public static class Lua1Arg extends OneArgFunction {
        
        private Function function;
        
        public Lua1Arg(Function function) {
            this.function = function;
        }
        
        @Override
        public LuaValue call(LuaValue v) {
            return function.call(v);
        }
        
        @FunctionalInterface
        private interface Function {
            
            LuaValue call(LuaValue v);
        }
    }
    
    
    public static class Lua3Arg extends ThreeArgFunction {
        
        private Function function;
        
        public Lua3Arg(Function function) {
            this.function = function;
        }
        
        @Override
        public LuaValue call(LuaValue v1, LuaValue v2, LuaValue v3) {
            return function.call(v1, v2, v3);
        }
        
        @FunctionalInterface
        private interface Function {
            
            LuaValue call(LuaValue v1, LuaValue v2, LuaValue v3);
        }
    }
}
