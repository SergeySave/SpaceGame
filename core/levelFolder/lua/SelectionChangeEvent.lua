-- @author sergeys


if event:getSelected():size() == 1 then
    local objective = getObjective('obj.one-ship.id')

    if objective and not objective:getCompleted() then
        objective:setCompleted(true)
        addObjective('obj.move-command.id', 'obj.move-command.title', 'obj.move-command.desc')
    end
elseif event:getSelected():size() == 10 then
    local objective = getObjective('obj.select-10ship.id')

    if objective and not objective:getCompleted() then
        objective:setCompleted(true)
        addObjective('obj.fleet-formation-command.id', 'obj.fleet-formation-command.title', 'obj.fleet-formation-command.desc')
    end
end

