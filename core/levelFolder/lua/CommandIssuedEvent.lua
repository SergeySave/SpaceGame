-- @author sergeys

local objective = getObjective('obj.move-command.id')

if (objective and not objective:getCompleted() and event:getId() == 'default') then
    objective:setCompleted(true)
end
