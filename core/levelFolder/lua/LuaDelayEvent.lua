
-- @author sergeys

-- id, title, description
addObjective('obj.one-ship.id', 'obj.one-ship.title', 'obj.one-ship.desc')

local enemy = spawnEntity("enemy1")

local position = component.p.get(enemy)
position.x = 1000
position.y = 1000

local rotation = component.r.get(enemy)
rotation.r = 225

local friend = spawnEntity("ship1")

local position2 = component.p.get(friend)
position2.x = 100
position2.y = 1000

local rotation2 = component.r.get(friend)
rotation2.r = 0
