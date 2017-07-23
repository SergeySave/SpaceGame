-- @author sergeys

if getTag(event:getEntity()) == 'enemy' then
    if data.get1() == nil then
        data.set1(0)
    end
    data.set1(data.get1() + 1)

    if data.get1() == 1 then
        local objective = getObjective('obj.defeat-enemies-1.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)

            setMoney(300 + getMoney())

            addObjective('obj.build-factory.id', 'obj.build-factory.title', 'obj.build-factory.desc')
        end
    elseif data.get1() == 10 then
        local objective = getObjective('obj.defeat-enemies-9.id')

        if (objective and not objective:getCompleted()) then
            objective:setCompleted(true)

            --
        end
    end
end
