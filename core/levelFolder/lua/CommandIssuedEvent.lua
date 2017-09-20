-- @author sergeys

if event:getId() == 'default' then
    if event:getCount() == 1 then
        local objective = getObjective('obj.move-command.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)
            setPlayer1Money(250)
            addObjective('obj.build-mine.id', 'obj.build-mine.title', 'obj.build-mine.desc')

            sendMessage('faces/guy', 'msg.build-mine', 5) --image, message, seconds
            playSound('voices/build-mine.wav') -- file
        end
    elseif event:getCount() == 10 then
        local objective = getObjective('obj.move-fleet-command.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)

            for i = 0, 8, 1 do
                local x = 1000 - 50 * math.floor(i / 3)
                local y = 1000 - 50 * (i % 3)

                local enemy = spawnEntity("enemy1")

                local position = component.p.get(enemy)
                position:setX(x)
                position:setY(y)

                local rotation = component.r.get(enemy)
                rotation.r = 225

                addOrder(enemy, orders.MoveOrder.new(x - 250, y - 250, 25), orders.MoveOrder)
            end

            addObjective('obj.defeat-enemies-9.id', 'obj.defeat-enemies-9.title', 'obj.defeat-enemies-9.desc')

            sendMessage('faces/guy', 'msg.defeat-enemies-9', 5) --image, message, seconds
            playSound('voices/defeat-enemies-9.wav') -- file
        end
    end
elseif event:getId() == 'triangle' or event:getId() == 'square' then
    local objective = getObjective('obj.fleet-formation-command.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        addObjective('obj.move-fleet-command.id', 'obj.move-fleet-command.title', 'obj.move-fleet-command.desc')

        sendMessage('faces/guy', 'msg.move-fleet-command', 5) --image, message, seconds
        playSound('voices/move-fleet-command.wav') -- file
    end
end
