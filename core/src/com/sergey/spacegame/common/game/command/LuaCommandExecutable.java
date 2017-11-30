package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.command.lua.EntityIterable;
import com.sergey.spacegame.common.lua.LuaUtils;
import com.sergey.spacegame.common.lua.SpaceGameLuaLib;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * A command executable responsible for a LUA defined command
 *
 * It delegates the command execution to LUA
 *
 * LuaCommandExecutables with the same original JSON string are equal
 *
 * @author sergeys
 */
public final class LuaCommandExecutable implements CommandExecutable {
    
    private final static Globals        LUA_GLOBALS;
    private static       EntityIterable iterable;
    
    static {
        LUA_GLOBALS = LuaUtils.newStandard();
        LUA_GLOBALS.load(SpaceGameLuaLib.INSTANCE);
        
        iterable = new EntityIterable();
        
        LUA_GLOBALS.set("selected", CoerceJavaToLua.coerce(iterable));
        LUA_GLOBALS.set("count", 0);
        LUA_GLOBALS.set("x1", 0f);
        LUA_GLOBALS.set("y1", 0f);
        LUA_GLOBALS.set("x2", 0f);
        LUA_GLOBALS.set("y2", 0f);
    }
    
    private LuaValue code;
    private String   lua;
    private String   original;
    
    /**
     * Create a new LuaCommandExecutable
     *
     * @param lua      - the lua code to load
     * @param original - the original JSON string
     */
    public LuaCommandExecutable(String lua, String original) {
        code = LUA_GLOBALS.load(lua);
        this.lua = lua;
        this.original = original;
    }
    
    @Override
    public void issue(Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end, Level level) {
        iterable.setBacking(entitySource);
        LUA_GLOBALS.set("count", numEntities);
        LUA_GLOBALS.set("x1", start.x);
        LUA_GLOBALS.set("y1", start.y);
        LUA_GLOBALS.set("x2", end.x);
        LUA_GLOBALS.set("y2", end.y);
        code.call();
    }
    
    /**
     * Get the lua code
     *
     * @return the lua code
     */
    public String getLua() {
        return lua;
    }
    
    /**
     * Get the original JSON string
     *
     * @return the original JSON string
     */
    public String getOriginal() {
        return original;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LuaCommandExecutable)) return false;
        
        LuaCommandExecutable that = (LuaCommandExecutable) o;
        
        return original.equals(that.original);
    }
    
    @Override
    public int hashCode() {
        return original.hashCode();
    }
}
