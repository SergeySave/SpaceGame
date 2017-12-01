
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

    switchLevel("internal://cutscene.sgl")
end
