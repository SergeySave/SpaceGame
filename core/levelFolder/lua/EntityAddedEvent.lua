-- @author sergeys

if getTag(event:getEntity()) == 'ship' then
    local objective = getObjective('obj.build-ship.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
    end
end
