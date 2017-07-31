-- @author sergeys

if getTag(event:getEntity()) == 'ship' then
    if data.get0() == nil then
        data.set0(0)
    end
    data.set0(data.get0() + 1)

    if data.get0() == 10 then
        local objective = getObjective('obj.build-10ship.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)
            addObjective('obj.select-10ship.id', 'obj.select-10ship.title', 'obj.select-10ship.desc')

            sendMessage('faces/guy', 'msg.select-10ship', 5) --image, message, seconds
            playSound('voices/select-10ship.wav') -- file
        end
    end
end
