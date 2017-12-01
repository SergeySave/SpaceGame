
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

    local function particles(ship)
        local pos = component.pos.get(ship)
        local facing = component.rot.get(ship).r

        local size = component.size.get(ship)

        local cos = math.cos(facing * math.pi / 180)
        local sin = math.sin(facing * math.pi / 180)

        for i = 1, 50 do

            local trans = math.random(127)
            local velMult = 127 / 127.0 * 0.5 + 0.5

            local particle = spawnParticle("whitePixel", pos:getX() + (math.random() * size.w - size.w / 2) - 10 * cos, pos:getY() + (math.random() * size.h - size.h / 2) - 10 * sin, 15, 0.75, 25 * cos * velMult, 25 * sin * velMult, 3000 - 22 * trans)

            local newFacing = component.rot.new()
            newFacing.r = facing

            local visual = component.vis.get(particle)

            if (not (visual:getVisualData() == NIL)) then
                local vData = visual:getVisualData()

                local whiteness = math.random(127)

                vData:setMultColor(colorToFloat(whiteness, whiteness, 255, trans + 64))
            end

            particle:add(newFacing)
        end
    end

    local function spawnEnemy(x, y)
        local enemy = spawnEntity("enemy1")

        local position = component.p.get(enemy)
        position:setX(x)
        position:setY(y)

        local rotation = component.r.get(enemy)
        rotation.r = 180 / math.pi * math.atan2(centerY - y, centerX - x)

        particles(enemy)

        addOrder(enemy, orders.TimeMoveOrder.new((centerX + x) / 2, (centerY + y) / 2, 60), orders.TimeMoveOrder)
    end

    for i = 0, 350, 10 do
        spawnEnemy(centerX + h / 2.1 * math.cos(i * math.pi / 180), centerY + h / 2.1 * math.sin(i * math.pi / 180))
    end
end
