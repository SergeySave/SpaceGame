-- @author sergeys

if getTag(event:getEntity()) == 'enemy' then
    if data.get1() == nil then
        data.set1(0)
    end
    data.set1(data.get1() + 1)

    if data.get1() == 1 then
        local objective = getObjective('obj.defeat-enemies-1.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)

            setPlayer1Money(300 + getPlayer1Money())

            addObjective('obj.build-factory.id', 'obj.build-factory.title', 'obj.build-factory.desc')

            sendMessage('faces/guy', 'msg.build-factory', 5) --image, message, seconds
            playSound('voices/build-factory.wav') -- file
        end
    elseif data.get1() == 10 then
        local objective = getObjective('obj.defeat-enemies-9.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)

            sendMessage('faces/guy', 'msg.enemies-defeated', 5) --image, message, seconds
            playSound('voices/enemies-defeated.wav') -- file
            postDelayEvent(500, 3, 0)
        end
    end
end
