package com.sergey.spacegame.common.lua

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.sergey.spacegame.common.SpaceGame
import com.sergey.spacegame.common.ecs.component.HealthComponent
import com.sergey.spacegame.common.ecs.component.MessageComponent
import com.sergey.spacegame.common.ecs.component.OrderComponent
import com.sergey.spacegame.common.ecs.component.ParticleComponent
import com.sergey.spacegame.common.ecs.component.PositionComponent
import com.sergey.spacegame.common.ecs.component.RotationComponent
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent
import com.sergey.spacegame.common.ecs.component.ShipComponent
import com.sergey.spacegame.common.ecs.component.SizeComponent
import com.sergey.spacegame.common.ecs.component.TagComponent
import com.sergey.spacegame.common.ecs.component.VelocityComponent
import com.sergey.spacegame.common.ecs.component.VisualComponent
import com.sergey.spacegame.common.ecs.component.WeaponComponent
import com.sergey.spacegame.common.event.BeginLevelEvent
import com.sergey.spacegame.common.event.EventHandle
import com.sergey.spacegame.common.event.LuaDelayEvent
import com.sergey.spacegame.common.game.Level
import com.sergey.spacegame.common.game.Objective
import com.sergey.spacegame.common.game.orders.BuildBuildingOrder
import com.sergey.spacegame.common.game.orders.BuildShipOrder
import com.sergey.spacegame.common.game.orders.FaceOrder
import com.sergey.spacegame.common.game.orders.IOrder
import com.sergey.spacegame.common.game.orders.MoveOrder
import com.sergey.spacegame.common.game.orders.StopOrder
import com.sergey.spacegame.common.game.orders.TimeMoveOrder
import com.sergey.spacegame.common.util.Quadruple
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import org.luaj.vm2.lib.jse.CoerceLuaToJava
import java.util.HashSet

/**
 * A singleton object representing the LUA library for the game
 *
 * @author sergeys
 */
object SpaceGameLuaLib : TwoArgFunction() {
    
    private lateinit var currLevel: Level
    
    //All the order classes accessable to LUA
    private val ORDERS = HashSet<Class<out IOrder>>()
    //All of the components accessable to LUA
    private val COMPONENTS = HashSet<Quadruple<String, ComponentMapper<*>, () -> Component, List<String>>>()
    
    init {
        ORDERS.add(BuildShipOrder::class.java)
        ORDERS.add(BuildBuildingOrder::class.java)
        ORDERS.add(FaceOrder::class.java)
        ORDERS.add(MoveOrder::class.java)
        ORDERS.add(TimeMoveOrder::class.java)
        ORDERS.add(StopOrder::class.java)
        
        COMPONENTS.add(Quadruple("position", PositionComponent.MAPPER, { PositionComponent() }, listOf("pos", "p")))
        COMPONENTS.add(Quadruple("rotation", RotationComponent.MAPPER, { RotationComponent() }, listOf("rot", "r")))
        COMPONENTS.add(Quadruple("rotationVelocity", RotationVelocityComponent.MAPPER, { RotationVelocityComponent() }, listOf("rotVel", "rotV", "rV")))
        COMPONENTS.add(Quadruple("size", SizeComponent.MAPPER, { SizeComponent() }, listOf("s")))
        COMPONENTS.add(Quadruple("velocity", VelocityComponent.MAPPER, { VelocityComponent() }, listOf("vel", "v")))
        COMPONENTS.add(Quadruple("ship", ShipComponent.MAPPER, { ShipComponent() }, listOf()))
        COMPONENTS.add(Quadruple("health", HealthComponent.MAPPER, { HealthComponent() }, listOf("h")))
        COMPONENTS.add(Quadruple("particle", PositionComponent.MAPPER, { ParticleComponent(System.currentTimeMillis()) }, listOf("prtl")))
        COMPONENTS.add(Quadruple("visual", VisualComponent.MAPPER, { VisualComponent("") }, listOf("vis")))
    }
    
    override fun call(modName: LuaValue, env: LuaValue): LuaValue {
        env.apply {
            set("postDelayEvent", lFuncU { millis, id, parameter ->
                SpaceGame.getInstance().dispatchDelayedEvent(millis.checklong(), LuaDelayEvent(id, parameter))
            })
            
            //Objectives
            set("addObjective", lFuncU { id, title, description ->
                currLevel.objectives.add(Objective(id.checkjstring(), title.checkjstring(), description.checkjstring(), false))
            })
            set("getObjective", lFunc { id ->
                currLevel.objectives
                        .find { (id1) -> id1 == id.checkjstring() }
                        .let { obj -> CoerceJavaToLua.coerce(obj) } ?: NIL
            })
            set("removeObjective", lFuncU({ objective ->
                                              currLevel.objectives.remove(CoerceLuaToJava.coerce(objective, Objective::class.java) as Objective)
                                          }))
            
            //Money
            set("getPlayer1Money", lFunc { -> LuaValue.valueOf(currLevel.player1.money) })
            set("setPlayer1Money", lFuncU { money -> currLevel.player1.money = money.checkdouble() })
            set("getPlayer2Money", lFunc { -> LuaValue.valueOf(currLevel.player2.money) })
            set("setPlayer2Money", lFuncU { money -> currLevel.player2.money = money.checkdouble() })
            
            //ECS Helper
            set("getTag", lFunc { entity ->
                val entity1 = CoerceLuaToJava.coerce(entity, Entity::class.java) as Entity
                TagComponent.MAPPER.get(entity1)?.let { tag -> LuaValue.valueOf(tag.tag) } ?: NIL
            })
            set("spawnEntity", lFunc { entityName ->
                val entity = currLevel.entities[entityName.checkjstring()]!!.createEntity(currLevel)
                currLevel.ecs.addEntity(entity)
                CoerceJavaToLua.coerce(entity)
            })
            set("removeEntity", lFuncU { entity ->
                val jEntity = CoerceLuaToJava.coerce(entity, Entity::class.java) as Entity
                currLevel.ecs.removeEntity(jEntity)
                if (HealthComponent.MAPPER.has(jEntity)) {
                    //Deregister it as a target
                    HealthComponent.MAPPER.get(jEntity).health = 0.0
                }
            })
            set("addOrder", lFuncU { entityLua, order, className ->
                val entity = CoerceLuaToJava.coerce(entityLua, Entity::class.java) as Entity
                val orderComp: OrderComponent
                if (OrderComponent.MAPPER.has(entity)) {
                    orderComp = OrderComponent.MAPPER.get(entity)
                } else {
                    orderComp = OrderComponent()
                    entity.add(orderComp)
                }
                val orderObj = CoerceLuaToJava.coerce(order, CoerceLuaToJava.coerce(className, Class::class.java) as Class<*>) as IOrder
                orderComp.addOrder(orderObj)
            })
            set("spawnParticle", object : VarArgFunction() {
                override fun invoke(args: Varargs): Varargs {
                    if (args == LuaValue.NONE || args.narg() != 8) {
                        return argerror("spawnParticle needs 8 arguments")
                    }
            
                    val imageName = args.arg(1).checkjstring()
                    val x = args.arg(2).checkdouble().toFloat()
                    val y = args.arg(3).checkdouble().toFloat()
                    val w = args.arg(4).checkdouble().toFloat()
                    val h = args.arg(5).checkdouble().toFloat()
                    val vx = args.arg(6).checkdouble().toFloat()
                    val vy = args.arg(7).checkdouble().toFloat()
                    val life = args.arg(8).checklong()
            
                    val entity = currLevel.ecs.newEntity()
            
                    entity.add(VisualComponent(imageName))
                    entity.add(PositionComponent(x, y))
                    entity.add(SizeComponent(w, h))
                    entity.add(VelocityComponent(vx, vy))
                    entity.add(ParticleComponent(System.currentTimeMillis() + life))
            
                    currLevel.ecs.addEntity(entity)
                    return CoerceJavaToLua.coerce(entity)
                }
            })
            
            //Lua Global data
            val dataTable = LuaTable()
            for (i in 0..9) {
                dataTable.set("get" + i, lFunc { -> currLevel.luaStores[i] })
                dataTable.set("set" + i, lFuncU { v -> currLevel.luaStores[i] = v })
            }
            set("data", dataTable)
            
            //Orders
            val ordersTable = LuaTable()
            for (clazz in ORDERS) {
                ordersTable.set(clazz.simpleName, CoerceJavaToLua.coerce(clazz))
            }
            set("orders", ordersTable)
            
            //Components
            val components = LuaTable()
            for ((name, mapper, constructor, aliases) in COMPONENTS) {
                val component = LuaTable()
                
                component.set("has", lFunc { entity -> LuaValue.valueOf(mapper.has(CoerceLuaToJava.coerce(entity, Entity::class.java) as Entity)) })
                component.set("get", lFunc { entity -> CoerceJavaToLua.coerce(mapper.get(CoerceLuaToJava.coerce(entity, Entity::class.java) as Entity)) })
                component.set("new", lFunc { -> CoerceJavaToLua.coerce(constructor()) })
                
                components.set(name, component)
                
                for (alias in aliases) {
                    val aliasEntry = components.get(alias)
                    if (aliasEntry == null || aliasEntry == NIL) {
                        components.set(alias, component)
                    }
                }
            }
            set("component", components)
    
            //Audio
            set("playSound", object : VarArgFunction() {
                override fun invoke(args: Varargs): Varargs {
                    if (args == LuaValue.NONE || args.narg() == 0) {
                        return argerror("Play sound needs at least one argument")
                    }
                    try {
                        when (args.narg()) {
                            1 -> currLevel.playSound(args.arg1().checkjstring())
                            2 -> currLevel.playSound(args.arg1().checkjstring(), args.arg(2).checkdouble().toFloat())
                            3 -> currLevel.playSound(args.arg1().checkjstring(), args.arg(2).checkdouble().toFloat(), args.arg(3).checkdouble().toFloat())
                            4 -> currLevel.playSound(args.arg1().checkjstring(), args.arg(2).checkdouble().toFloat(), args.arg(3).checkdouble().toFloat(), args.arg(4).checkdouble().toFloat())
                        }
                    } catch (e: NullPointerException) {
                        System.err.println("Unable to play sound: \"${args.arg1().checkjstring()}\". Sound not found.")
                    }
                    return NIL
                }
            })
    
            //Messages
            set("sendMessage", lFuncU { imageL, messageL, timeL ->
                val textureName = imageL.checkjstring()
                val region = SpaceGame.getInstance().context.getRegion(textureName)
                val message = messageL.checkjstring()
                val millis = (timeL.checkdouble() * 1000).toLong() + System.currentTimeMillis()
        
                val entity = currLevel.ecs.newEntity()
                entity.add(MessageComponent(textureName, region, message, millis))
                currLevel.ecs.addEntity(entity)
            })
    
            //Viewport
            val viewport = LuaTable().apply {
                set("setControllable", lFuncU { isControllable ->
                    currLevel.viewport.viewportControllable = isControllable.checkboolean()
                })
                set("isControllable", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.viewportControllable)
                })
                set("setX", lFuncU { x ->
                    currLevel.viewport.viewportX = x.checkdouble().toFloat()
                })
                set("getX", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.viewportX.toDouble())
                })
                set("setY", lFuncU { y ->
                    currLevel.viewport.viewportY = y.checkdouble().toFloat()
                })
                set("getY", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.viewportY.toDouble())
                })
                set("setWidth", lFuncU { w ->
                    currLevel.viewport.viewportWidth = w.checkdouble().toFloat()
                })
                set("getWidth", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.viewportWidth.toDouble())
                })
                set("setHeight", lFuncU { h ->
                    currLevel.viewport.viewportHeight = h.checkdouble().toFloat()
                })
                set("getHeight", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.viewportHeight.toDouble())
                })
                set("setHiddenUI", lFuncU { isHidden ->
                    currLevel.viewport.hiddenUI = isHidden.checkboolean()
                })
                set("getHiddenUI", lFunc { ->
                    LuaValue.valueOf(currLevel.viewport.hiddenUI)
                })
            }
            set("viewport", viewport)
    
            //Is controllable
            set("setControllable", lFuncU { isControllable ->
                currLevel.isControllable = isControllable.checkboolean()
            })
            set("isControllable", lFunc { ->
                LuaValue.valueOf(currLevel.isControllable)
            })
    
            //Switch levels
            set("switchLevel", lFuncU { level ->
                if (level == LuaValue.NIL) {
                    SpaceGame.getInstance().dispatchDelayedRunnable(0) {
                        SpaceGame.getInstance().clearDelayedEventsNow()
                        currLevel.deinit()
                        currLevel.viewport.close()
                    }
                } else {
                    val levelString = level.checkjstring()
                    if (levelString.startsWith("internal://")) {
                        SpaceGame.getInstance().dispatchDelayedRunnable(0) {
                            SpaceGame.getInstance().clearDelayedEventsNow()
                            currLevel.deinit()
                            val newLevel = Level.getLevelFromInternalPath(levelString.substring(11))
                            currLevel.viewport.setLevel(newLevel)
                        }
                    } else {
                        SpaceGame.getInstance().dispatchDelayedRunnable(0) {
                            SpaceGame.getInstance().clearDelayedEventsNow()
                            currLevel.deinit()
                            val newLevel = Level.getLevelFromAbsolutePath("${currLevel.parentDirectory}/$levelString")
                            currLevel.viewport.setLevel(newLevel)
                        }
                    }
                }
            })
    
            set("colorToFloat", object : VarArgFunction() {
                override fun invoke(args: Varargs): Varargs {
                    if (args == LuaValue.NONE || (args.narg() != 3 && args.narg() != 4)) {
                        return argerror("colorToFloat needs 3 or 4 arguments")
                    }
            
                    val r = args.arg(1).checkint()
                    val g = args.arg(2).checkint()
                    val b = args.arg(3).checkint()
                    val a = if (args.narg() == 4) args.arg(4).checkint() else 255
            
                    return LuaValue.valueOf(Color.toFloatBits(r, g, b, a).toDouble())
                }
            })
    
            set("randomizeWeaponTimers", lFuncU { e, mtv ->
                val entity = CoerceLuaToJava.coerce(e, Entity::class.java) as Entity
                val maxTimerVal = mtv.checkdouble().toFloat()
        
                WeaponComponent.MAPPER.get(entity)?.let { weaponComp ->
                    for (i in 0 until weaponComp.timers.size) {
                        weaponComp.timers[i] = currLevel.random.nextFloat() * maxTimerVal
                    }
                }
            })
        }
        
        return NIL
    }
    
    @EventHandle
    fun onLevelStart(event: BeginLevelEvent) {
        currLevel = event.level
    }
    
    private inline fun lFuncU(crossinline body: () -> Unit) = object : ZeroArgFunction() {
        override fun call(): LuaValue {
            body(); return NIL
        }
    }
    
    private inline fun lFunc(crossinline body: () -> LuaValue) = object : ZeroArgFunction() {
        override fun call(): LuaValue = body()
    }
    
    private inline fun lFuncU(crossinline body: (LuaValue) -> Unit) = object : OneArgFunction() {
        override fun call(v: LuaValue): LuaValue {
            body(v); return NIL
        }
    }
    
    private inline fun lFunc(crossinline body: (LuaValue) -> LuaValue) = object : OneArgFunction() {
        override fun call(v: LuaValue): LuaValue = body(v)
    }
    
    private inline fun lFuncU(crossinline body: (LuaValue, LuaValue) -> Unit) = object : TwoArgFunction() {
        override fun call(v1: LuaValue, v2: LuaValue): LuaValue {
            body(v1, v2); return NIL
        }
    }
    
    private inline fun lFunc(crossinline body: (LuaValue, LuaValue) -> LuaValue) = object : TwoArgFunction() {
        override fun call(v1: LuaValue, v2: LuaValue): LuaValue = body(v1, v2)
    }
    
    private inline fun lFuncU(crossinline body: (LuaValue, LuaValue, LuaValue) -> Unit) = object : ThreeArgFunction() {
        override fun call(v1: LuaValue, v2: LuaValue, v3: LuaValue): LuaValue {
            body(v1, v2, v3); return NIL
        }
    }
    
    private inline fun lFunc(
            crossinline body: (LuaValue, LuaValue, LuaValue) -> LuaValue) = object : ThreeArgFunction() {
        override fun call(v1: LuaValue, v2: LuaValue, v3: LuaValue): LuaValue = body(v1, v2, v3)
    }
}
