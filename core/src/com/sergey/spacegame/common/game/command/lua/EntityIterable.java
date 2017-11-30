package com.sergey.spacegame.common.game.command.lua;

import com.badlogic.ashley.core.Entity;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Iterator;

/**
 * An entity iterable used for LUA commands
 *
 * It provides a limited api for iterating over a collection of entities
 *
 * @author sergeys
 */
public class EntityIterable {
    
    /**
     * The LUA iterator() method
     */
    public final IteratorGetter iterator = new IteratorGetter();
    private Iterable<Entity> backing;
    
    /**
     * Get the backing iterable
     *
     * @return the backing iterable
     */
    public Iterable<Entity> getBacking() {
        return backing;
    }
    
    /**
     * Set the backing iterable
     *
     * @param backing the new backing iterable
     */
    public void setBacking(Iterable<Entity> backing) {
        this.backing = backing;
    }
    
    /**
     * This class returns a LUA iterator over the current backing iterator
     */
    public class IteratorGetter extends ZeroArgFunction {
        
        @Override
        public LuaValue call() {
            return CoerceJavaToLua.coerce(new EntityIterator(backing.iterator()));
        }
    }
    
    
    /**
     * This class is the actual iterator used for iterating
     */
    public class EntityIterator {
        
        /**
         * The LUA hasNext() method
         */
        public final HasNext hasNext = new HasNext();
        /**
         * The LUA next() method
         */
        public final GetNext next    = new GetNext();
        
        private Iterator<Entity> iter;
        
        /**
         * Create a new EntityIterator
         *
         * @param iter - the iterator that the entity iterator should loop over
         */
        public EntityIterator(Iterator<Entity> iter) {
            this.iter = iter;
        }
        
        /**
         * This class returns whether the entity iterator has more entities to iterate over
         */
        public class HasNext extends ZeroArgFunction {
            
            @Override
            public LuaValue call() {
                return iter.hasNext() ? LuaValue.TRUE : LuaValue.FALSE;
            }
        }
        
        
        /**
         * This class returns the next entity in the entity iterator
         */
        public class GetNext extends ZeroArgFunction {
            
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(iter.next());
            }
        }
    }
}
