-- @author sergeys

if getTag(event:getEntity()) == 'enemy' then
    local objective = getObjective('obj.defeat-enemies-1.id')

    if (objective and not objective:getCompleted()) then
        objective:setCompleted(true)
        --addObjective('obj..id', 'obj..title', 'obj..desc')
    end
end
