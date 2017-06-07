package com.sergey.spacegame.common.game.command.lua;

import java.util.Iterator;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.IDComponent;

public class EntityIterable {
	
	private Iterable<Entity> backing;
	public final IteratorGetter iterator = new IteratorGetter();
	
	public EntityIterable() {}
	
	public void setBacking(Iterable<Entity> backing) {
		this.backing = backing;
	}
	
	public Iterable<Entity> getBacking() {
		return backing;
	}
	
	public class IteratorGetter extends ZeroArgFunction {

		@Override
		public LuaValue call() {
			return CoerceJavaToLua.coerce(new EntityIterator(backing.iterator()));
		}
	}
	
	public class EntityIterator {
		
		private Iterator<Entity> iter;
		
		public EntityIterator(Iterator<Entity> iter) {
			this.iter = iter;
		}
		
		public final HasNext hasNext = new HasNext();
		public final GetNext next = new GetNext();
		
		public class HasNext extends ZeroArgFunction {
			@Override
			public LuaValue call() {
				return iter.hasNext() ? LuaValue.TRUE : LuaValue.FALSE;
			}
		}
		public class GetNext extends ZeroArgFunction {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(IDComponent.MAPPER.get(iter.next()).id);
			}
		}
	}
}
