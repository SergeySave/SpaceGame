package com.sergey.spacegame.common.event

import com.sergey.spacegame.common.lua.LuaUtils
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class LuaEventHandler {
    private var code: LuaValue
    var lua: String
        get() = field
        private set(value) {field = value}

    constructor(lua: String) {
        this.code = LUA_GLOBALS.load(lua)
        this.lua = lua
    }

    fun execute(event: Event) {
        LUA_GLOBALS.set("event", CoerceJavaToLua.coerce(event))
        code.call()
    }

    private companion object {
        private val LUA_GLOBALS: Globals

        init {
            LUA_GLOBALS = LuaUtils.newStandard()
        }
    }
}