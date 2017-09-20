-- @author sergeys

if event:getId() == 'mine1' then
    local objective = getObjective('obj.build-mine.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        setPlayer1Money(200 + getPlayer1Money())
        addObjective('obj.build-fort.id', 'obj.build-fort.title', 'obj.build-fort.desc')

        sendMessage('faces/guy', 'msg.build-fort', 5) --image, message, seconds
        playSound('voices/build-fort.wav') -- file
    end
elseif event:getId() == 'fort1' then
    local objective = getObjective('obj.build-fort.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)

        local buildingPos = component.p.get(event:getEntity())

        local enemy = spawnEntity("enemy1")

        local position = component.p.get(enemy)
        position:setX(1000)
        position:setY(1000)

        local ship = component.ship.get(enemy)

        addOrder(enemy, orders.FaceOrder.new(math.deg(math.atan2(buildingPos:getY() + 80 - 1000, buildingPos:getX() + 80 - 1000)), ship.rotateSpeed), orders.FaceOrder)
        addOrder(enemy, orders.MoveOrder.new(buildingPos:getX() + 80, buildingPos:getY() + 80, ship.moveSpeed / 5), orders.MoveOrder)

        addObjective('obj.defeat-enemies-1.id', 'obj.defeat-enemies-1.title', 'obj.defeat-enemies-1.desc')

        postDelayEvent(1000, 2, 0)
    end
elseif event:getId() == 'factory1' then
    local objective = getObjective('obj.build-factory.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        addObjective('obj.build-10ship.id', 'obj.build-10ship.title', 'obj.build-10ship.desc')

        sendMessage('faces/guy', 'msg.build-10ship', 5) --image, message, seconds
        playSound('voices/build-10ship.wav') -- file
    end
end
