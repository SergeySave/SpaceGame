-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    addOrder(entity, orders.BuildBuildingOrder.new('mine1', 5, x1, y1, 250), orders.BuildBuildingOrder)
end
