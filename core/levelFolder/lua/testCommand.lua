
-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    addOrder(entity, orders.BuildBuildingOrder.new('buildingTest', 5, x1, y1), orders.BuildBuildingOrder)
end
