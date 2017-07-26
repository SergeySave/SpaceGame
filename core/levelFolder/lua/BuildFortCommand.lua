-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    addOrder(entity, orders.BuildBuildingOrder.new('fort1', 5, x1, y1, 300), orders.BuildBuildingOrder)
end
