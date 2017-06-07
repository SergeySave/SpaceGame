package com.sergey.spacegame.common.game.command.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.IDComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.game.orders.FaceOrder;
import com.sergey.spacegame.common.game.orders.IOrder;

public class CommandLuaLib extends TwoArgFunction {

	public CommandLuaLib() {}

	public LuaValue call(LuaValue modname, LuaValue env) {
		env.set("addOrder", new AddOrder());
		env.set("FaceOrder", CoerceJavaToLua.coerce(FaceOrder.class));
		return LuaValue.NIL;
	}
	
	public class AddOrder extends ThreeArgFunction {
		@SuppressWarnings("rawtypes")
		@Override
		public LuaValue call(LuaValue entityID, LuaValue order, LuaValue className) {
			Entity entity = IDComponent.entities.get(entityID.checkint());
			OrderComponent orderComp;
			if (OrderComponent.MAPPER.has(entity)) {
				orderComp = new OrderComponent();
				entity.add(orderComp);
			} else {
				orderComp = OrderComponent.MAPPER.get(entity);
			}
			IOrder orderObj = (IOrder) CoerceLuaToJava.coerce(order, (Class) CoerceLuaToJava.coerce(className, Class.class));
			orderComp.orders.add(orderObj);
			return LuaValue.NIL;
		}
	}
}