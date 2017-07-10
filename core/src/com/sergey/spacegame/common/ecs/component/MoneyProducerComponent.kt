package com.sergey.spacegame.common.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper

/**
 * @author sergeys
 */
class MoneyProducerComponent @JvmOverloads constructor(val amount: Double = 0.0) : ClonableComponent {
    
    override fun copy(): Component {
        return MoneyProducerComponent(this.amount)
    }
    
    companion object MAP {
        @JvmField
        val MAPPER = ComponentMapper.getFor(MoneyProducerComponent::class.java)!!
    }
}