-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    if getMoney() >= 50 then
        addOrder(entity, orders.BuildShipOrder.new('ship1', 5, 50), orders.BuildShipOrder)
        if data.get0() == nil then
            data.set0(0)
        end
        data.set0(data.get0() + 1)
    end
end
