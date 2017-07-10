package com.sergey.spacegame.common.event

import com.sergey.spacegame.common.lua.LuaUtils
import com.sergey.spacegame.common.lua.SpaceGameLuaLib
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class LuaEventHandler {
    private var code: LuaValue
    var original: String
        private set
    var lua: String
        private set
    
    constructor(lua: String, original: String) {
        this.code = LUA_GLOBALS.load(lua)
        this.lua = lua
        this.original = original
    }
    
    fun execute(event: Event) {
        LUA_GLOBALS.set("event", CoerceJavaToLua.coerce(event))
        code.call()
    }
    
    private companion object {
        private val LUA_GLOBALS: Globals = LuaUtils.newStandard()
        
        init {
            LUA_GLOBALS.load(SpaceGameLuaLib.INSTANCE)
        }
    }
}