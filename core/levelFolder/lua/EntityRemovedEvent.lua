-- @author sergeys

if getTag(event:getEntity()) == 'enemy' then
    local objective = getObjective('obj.defeat-enemies-1.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)

        setMoney(300 + getMoney())

        addObjective('obj.build-factory.id', 'obj.build-factory.title', 'obj.build-factory.desc')
    end
end
