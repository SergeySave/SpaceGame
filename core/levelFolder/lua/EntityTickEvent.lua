-- @author sergeys

if event:getId() == 'moneyBuilding' and event:getCount() % 20 == 0 then
    setMoney(getMoney() + (event:getCount() % 100) / 100)
end
