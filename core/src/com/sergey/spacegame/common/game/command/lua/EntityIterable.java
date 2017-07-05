package com.sergey.spacegame.common.game.command.lua;

import com.badlogic.ashley.core.Entity;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Iterator;

public class EntityIterable {
    
    public final IteratorGetter iterator = new IteratorGetter();
    private Iterable<Entity> backing;
    
    public EntityIterable() {}
    
    public Iterable<Entity> getBacking() {
        return backing;
    }
    
    public void setBacking(Iterable<Entity> backing) {
        this.backing = backing;
    }
    
    
    public class IteratorGetter extends ZeroArgFunction {
        
        @Override
        public LuaValue call() {
            return CoerceJavaToLua.coerce(new EntityIterator(backing.iterator()));
        }
    }
    
    
    public class EntityIterator {
        
        public final HasNext hasNext = new HasNext();
        public final GetNext next    = new GetNext();
        
        private Iterator<Entity> iter;
        
        public EntityIterator(Iterator<Entity> iter) {
            this.iter = iter;
        }
        
        
        public class HasNext extends ZeroArgFunction {
            
            @Override
            public LuaValue call() {
                return iter.hasNext() ? LuaValue.TRUE : LuaValue.FALSE;
            }
        }
        
        
        public class GetNext extends ZeroArgFunction {
            
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(iter.next());
            }
        }
    }
}
