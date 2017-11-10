
-- @author sergeys

local id = event:getId()

if id == 0 then
    --switchLevel("internal://cutscene.sgl")
    sendMessage('faces/guy', 'msg.welcome', 5) --image, message, seconds
    playSound('voices/welcome.wav') -- file
    postDelayEvent(3500, 1, 0)
elseif id == 1 then

    -- id, title, description
    addObjective('obj.one-ship.id', 'obj.one-ship.title', 'obj.one-ship.desc')

    sendMessage('faces/guy', 'msg.one-ship', 5) --image, message, seconds
    playSound('voices/one-ship.wav') -- file
elseif id == 2 then

    sendMessage('faces/guy', 'msg.enemy-NE', 5) --image, message, seconds
    playSound('voices/enemy-NE.wav') -- file
elseif id == 3 then

    sendMessage('faces/computer', 'msg.enemies-detected', 5) --image, message, seconds
    playSound('voices/enemies-detected.wav') -- file

    -- You have no power here
    viewport.setControllable(false)
    setControllable(false)

    -- Center along X axis and fill the entire width of the screen
    viewport.setWidth(1000)
    viewport.setX(1000)
    -- Top of level
    viewport.setY(1000)

    local w = 1000
    local h = viewport.getHeight()

    local centerX = 500
    local centerY = 1000 - h / 2
    local bottomY = 1000 - h

    local function spawnEnemy(x, y)
        local enemy = spawnEntity("enemy1")

        local position = component.p.get(enemy)
        position:setX(x)
        position:setY(y)

        local rotation = component.r.get(enemy)
        rotation.r = 180 / math.pi * math.atan2(centerY - y, centerX - x)

        addOrder(enemy, orders.TimeMoveOrder.new(centerX, centerY, 60), orders.TimeMoveOrder)
    end

    for i = 1, 34, 1 do
        spawnEnemy(i * w / 35, 1000)
        spawnEnemy(i * w / 35, bottomY)
    end
    for i = 1, 24, 1 do
        spawnEnemy(0, 1000 - i * h / 25)
        spawnEnemy(1000, 1000 - i * h / 25)
    end
end
