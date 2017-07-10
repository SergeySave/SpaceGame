-- @author sergeys

local entities = selected.iterator()
while entities.hasNext() do
    local entity = entities.next()
    if getMoney() >= 500 then
        addOrder(entity, orders.BuildBuildingOrder.new('factory1', 5, x1, y1, 500), orders.BuildBuildingOrder)
    end
end
