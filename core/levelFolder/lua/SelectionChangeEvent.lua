-- @author sergeys


if event:getSelected():size() == 1 then
    local objective = getObjective('obj.one-ship.id')

    if objective and not objective:getCompleted() then
        objective:setCompleted(true)
        addObjective('obj.move-command.id', 'obj.move-command.title', 'obj.move-command.desc')

        sendMessage('faces/guy', 'msg.move-command', 10) --image, message, seconds
        playSound('voices/move-command.wav') -- file
    end
elseif event:getSelected():size() == 10 then
    local objective = getObjective('obj.select-10ship.id')

    if objective and not objective:getCompleted() then
        objective:setCompleted(true)
        addObjective('obj.fleet-formation-command.id', 'obj.fleet-formation-command.title', 'obj.fleet-formation-command.desc')

        sendMessage('faces/guy', 'msg.fleet-formation-command', 5) --image, message, seconds
        playSound('voices/fleet-formation-command.wav') -- file
    end
end

