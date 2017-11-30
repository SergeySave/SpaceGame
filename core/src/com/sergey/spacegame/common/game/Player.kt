package com.sergey.spacegame.common.game

import com.badlogic.ashley.core.Entity
import com.sergey.spacegame.common.math.SpatialQuadtree

/**
 * This class represents a single player of the game
 *
 * @author sergeys
 *
 * @constructor Create a new Player object
 *
 * @property money - the amount of money that this player has
 * @property team - the SpatialQuadtree for this team
 */
data class Player(var money: Double, var team: SpatialQuadtree<Entity>)