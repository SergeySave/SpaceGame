-- @author sergeys

local objective = getObjective('obj.one-ship.id')

if (objective and not objective:getCompleted() and event:getSelected():size() == 1) then
    objective:setCompleted(true)
    --removeObjective(objective)
end
