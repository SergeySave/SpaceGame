package com.sergey.spacegame.common.game

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.math.SpatialQuadtree

/**
 * @author sergeys
 */
data class Player(var money: Double, var team: SpatialQuadtree<Entity>)