-- @author sergeys

if getTag(event:getEntity()) == 'ship' then
    if data.get0() == 1 then
        local objective = getObjective('obj.build-ship.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)
            addObjective('obj.build-10ship.id', 'obj.build-10ship.title', 'obj.build-10ship.desc')
        end
    elseif data.get0() == 10 then
        local objective = getObjective('obj.build-10ship.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)
        end
    end
end
