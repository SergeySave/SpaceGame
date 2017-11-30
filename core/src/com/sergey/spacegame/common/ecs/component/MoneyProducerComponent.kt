package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * This component represents a money producer
 *
 * @author sergeys
 *
 * @constructor Create a new MoneyProducerComponent
 *
 * @property amount - the amount of money to produce every second
 */
class MoneyProducerComponent @JvmOverloads constructor(val amount: Double = 0.0) : ClonableComponent {
    
    override fun copy(): Component {
        //Since amount is a constant I can just return this and save on memory
        return this
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(MoneyProducerComponent::class.java)!!
    }
}