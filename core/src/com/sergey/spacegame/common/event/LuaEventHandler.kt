package com.sergey.spacegame.common.event

import com.sergey.spacegame.common.lua.LuaUtils
import com.sergey.spacegame.common.lua.SpaceGameLuaLib
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua

/**
 * This class represents an event handler written in LUA
 *
 * It's execute method needs to be manually attached with a specific Event in mind to an event bus
 *
 * @author sergeys
 */
class LuaEventHandler(lua: String, original: String) {
    
    private var code: LuaValue
    private val LUA_GLOBALS: Globals = LuaUtils.newStandard()
    
    init {
        //Each event handler needs its own set of globals
        LUA_GLOBALS.load(SpaceGameLuaLib)
    }
    
    /**
     * The original JSON string that produced the lua
     */
    var original: String = original
        private set
    
    /**
     * The lua code that this is using to run
     */
    var lua: String = lua
        private set
    
    init {
        this.code = LUA_GLOBALS.load(lua)
    }
    
    /**
     * Executes the lua for this event handler with a given event
     *
     * @param event - the event to execute the LUA with
     */
    fun execute(event: Event) {
        LUA_GLOBALS.set("event", CoerceJavaToLua.coerce(event))
        code.call()
    }
}