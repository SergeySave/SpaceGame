package com.sergey.spacegame.common.lua;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.event.LuaDelayEvent;
import com.sergey.spacegame.common.game.orders.BuildBuildingOrder;
import com.sergey.spacegame.common.game.orders.BuildShipOrder;
import com.sergey.spacegame.common.game.orders.FaceOrder;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.game.orders.MoveOrder;
import com.sergey.spacegame.common.game.orders.TimeMoveOrder;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.HashSet;
import java.util.Set;

public class SpaceGameLuaLib extends TwoArgFunction {

	private static final Set<Class<? extends IOrder>> ORDERS;

	static {
		ORDERS = new HashSet<>();
		ORDERS.add(BuildShipOrder.class);
		ORDERS.add(BuildBuildingOrder.class);
		ORDERS.add(FaceOrder.class);
		ORDERS.add(MoveOrder.class);
		ORDERS.add(TimeMoveOrder.class);
	}

	public SpaceGameLuaLib() {}

	public LuaValue call(LuaValue modname, LuaValue env) {
		env.set("addOrder", new AddOrder());
		env.set("postDelayEvent", new PostLuaDelayEvent());

		LuaTable ordersTable = new LuaTable();
		for (Class<? extends IOrder> clazz : ORDERS){
			ordersTable.set(clazz.getSimpleName(), CoerceJavaToLua.coerce(clazz));
		}env.set("orders", ordersTable);

		return LuaValue.NIL;
	}

	public class AddOrder extends ThreeArgFunction {
		@SuppressWarnings("rawtypes")
		@Override
		public LuaValue call(LuaValue entityLua, LuaValue order, LuaValue className) {
			Entity entity = (Entity) CoerceLuaToJava.coerce(entityLua, Entity.class);
			OrderComponent orderComp;
			if (OrderComponent.MAPPER.has(entity)) {
				orderComp = OrderComponent.MAPPER.get(entity);
			} else {
				orderComp = new OrderComponent();
				entity.add(orderComp);
			}
			IOrder orderObj = (IOrder) CoerceLuaToJava.coerce(order, (Class) CoerceLuaToJava.coerce(className, Class.class));
			orderComp.addOrder(orderObj);
			return LuaValue.NIL;
		}
	}

	public class PostLuaDelayEvent extends ThreeArgFunction {

		@Override
		public LuaValue call(LuaValue millis, LuaValue id, LuaValue parameter) {
			SpaceGame.getInstance().dispatchDelayedEvent(millis.checklong(), new LuaDelayEvent(id, parameter));
			return LuaValue.NIL;
		}
	}
}
