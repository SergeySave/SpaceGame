-- @author sergeys

if event:getId() == 'default' then
    if event:getCount() == 1 then
        local objective = getObjective('obj.move-command.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)
            setMoney(250)
            addObjective('obj.build-mine.id', 'obj.build-mine.title', 'obj.build-mine.desc')
        end
    elseif event:getCount() == 10 then
        local objective = getObjective('obj.move-fleet-command.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)


            addObjective('obj.defeat-enemies-1.id', 'obj.defeat-enemies-1.title', 'obj.defeat-enemies-1.desc')
        end
    end
elseif event:getId() == 'triangle' then
    local objective = getObjective('obj.triangle-command.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        addObjective('obj.square-command.id', 'obj.square-command.title', 'obj.square-command.desc')
    end
elseif event:getId() == 'square' then
    local objective = getObjective('obj.square-command.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        addObjective('obj.move-fleet-command.id', 'obj.move-fleet-command.title', 'obj.move-fleet-command.desc')
    end
end
