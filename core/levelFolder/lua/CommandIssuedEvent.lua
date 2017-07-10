-- @author sergeys

if (event:getId() == 'default') then
    local objective = getObjective('obj.move-command.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        setMoney(250)
        addObjective('obj.build-mine.id', 'obj.build-mine.title', 'obj.build-mine.desc')
    end
end
