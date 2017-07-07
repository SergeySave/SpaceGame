
-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    if getMoney() >= 750 then
        addOrder(entity, orders.BuildBuildingOrder.new('buildingTest', 5, x1, y1, 250), orders.BuildBuildingOrder)
    end
end
