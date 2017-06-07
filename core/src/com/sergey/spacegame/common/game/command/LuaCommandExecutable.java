package com.sergey.spacegame.common.game.command;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.game.command.lua.CommandLuaLib;
import com.sergey.spacegame.common.game.command.lua.EntityIterable;
import com.sergey.spacegame.common.lua.LuaUtils;

public final class LuaCommandExecutable implements CommandExecutable {
	
	private final static Globals LUA_GLOBALS;
	private static EntityIterable iterable;
	
	static {
		LUA_GLOBALS = LuaUtils.newStandard();
		LUA_GLOBALS.load(new CommandLuaLib());
		
		iterable = new EntityIterable();
		
		LUA_GLOBALS.set("selected", CoerceJavaToLua.coerce(iterable));
		LUA_GLOBALS.set("count", 0);
		LUA_GLOBALS.set("x1", 0f);
		LUA_GLOBALS.set("y1", 0f);
		LUA_GLOBALS.set("x2", 0f);
		LUA_GLOBALS.set("y2", 0f);
	}
	
	private LuaValue code;
	private String lua;
	
	public LuaCommandExecutable(String lua) {
		code = LUA_GLOBALS.load(lua);
		this.lua = lua;
	}

	@Override
	public void issue(Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end) {
		iterable.setBacking(entitySource);
		LUA_GLOBALS.set("count", numEntities);
		LUA_GLOBALS.set("x1", start.x);
		LUA_GLOBALS.set("y1", start.y);
		LUA_GLOBALS.set("x2", end.x);
		LUA_GLOBALS.set("y2", end.y);
		code.call();
	}
	
	public String getLua() {
		return lua;
	}
}