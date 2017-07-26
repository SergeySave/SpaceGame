-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    addOrder(entity, orders.BuildShipOrder.new('ship1', 5, 50), orders.BuildShipOrder)
end
