-- @author sergeys

local objective = getObjective('obj.one-ship.id')

if (objective and event:getSelected():size() == 1) then
    removeObjective(objective)
end
