-- @author sergeys

if event:getId() == 'moneyBuilding' and event:getCount() % 20 == 0 then
    setPlayer1Money(getPlayer1Money() + (event:getCount() % 100) / 100)
end
