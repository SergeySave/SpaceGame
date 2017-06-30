package com.sergey.spacegame.common.game.command.lua;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.IDComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
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

public class CommandLuaLib extends TwoArgFunction {

	public CommandLuaLib() {}

	public LuaValue call(LuaValue modname, LuaValue env) {
		env.set("addOrder", new AddOrder());
		
		LuaTable ordersTable = new LuaTable(); {
			ordersTable.set("BuildShipOrder", CoerceJavaToLua.coerce(BuildShipOrder.class));
			ordersTable.set("BuildBuildingOrder", CoerceJavaToLua.coerce(BuildBuildingOrder.class));
			ordersTable.set("FaceOrder", CoerceJavaToLua.coerce(FaceOrder.class));
			ordersTable.set("MoveOrder", CoerceJavaToLua.coerce(MoveOrder.class));
			ordersTable.set("TimeMoveOrder", CoerceJavaToLua.coerce(TimeMoveOrder.class));
		}env.set("orders", ordersTable);
		
		//env.set("FaceOrder", CoerceJavaToLua.coerce(FaceOrder.class));
		return LuaValue.NIL;
	}

	public class AddOrder extends ThreeArgFunction {
		@SuppressWarnings("rawtypes")
		@Override
		public LuaValue call(LuaValue entityID, LuaValue order, LuaValue className) {
			Entity entity = IDComponent.entities.get(entityID.checkint());
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
}
