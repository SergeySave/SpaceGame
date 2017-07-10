-- @author sergeys

if event:getId() == 'mine1' then
    local objective = getObjective('obj.build-mine.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        setMoney(500 + getMoney())
        addObjective('obj.build-factory.id', 'obj.build-factory.title', 'obj.build-factory.desc')
    end
elseif event:getId() == 'factory1' then
    local objective = getObjective('obj.build-factory.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        addObjective('obj.build-ship.id', 'obj.build-ship.title', 'obj.build-ship.desc')
    end
end
