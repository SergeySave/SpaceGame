
-- @author sergeys

-- id, title, description
addObjective('obj.one-ship.id', 'obj.one-ship.title', 'obj.one-ship.desc')

local enemy = spawnEntity("enemy1")

local position = component.p.get(enemy)
position.x = 100

local velocity = component.v.new()
velocity.vy = component.ship.get(enemy).moveSpeed
enemy:add(velocity)

local rotation = component.r.get(enemy)
rotation.r = 90