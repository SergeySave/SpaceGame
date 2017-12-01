-- @author sergeys

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

local h = viewport.getHeight()

local centerX = 500
local centerY = 1000 - h / 2

local function spawnBoss(x, y)
    local enemy = spawnEntity("boss1")

    local position = component.p.get(enemy)
    position:setX(x)
    position:setY(y)

    local rotation = component.r.get(enemy)
    rotation.r = 180 / math.pi * math.atan2(centerY - y, centerX - x)

    particles(enemy)
end

if (event:getId() == 0) then
    sendMessage('faces/guy', 'msg.prepare-emergency-warp', 2.5) --image, message, seconds
    playSound('voices/prepare-emergency-warp.wav') -- file
    postDelayEvent(1500, 1, 0)
elseif event:getId() == 1 then
    sendMessage('faces/computer', 'msg.emergency-warp', 2.5) --image, message, seconds
    playSound('voices/emergency-warp.wav') -- file

    spawnBoss(centerX / 3, centerY)

    postDelayEvent(250, 2, 0)
elseif event:getId() == 2 then
    sendMessage('faces/computer', 'msg.critical-damage-sustained', 2.5) --image, message, seconds
    playSound('voices/critical-damage-sustained.wav') -- file

    particles(data.get0())
    removeEntity(data.get0())
elseif event:getId() == -1 then
    component.health.get(data.get0()):setHealth(2000)
end
