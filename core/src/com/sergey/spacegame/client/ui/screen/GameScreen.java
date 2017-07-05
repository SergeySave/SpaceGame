package com.sergey.spacegame.client.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.system.CommandUISystem;
import com.sergey.spacegame.client.ecs.system.HUDSystem;
import com.sergey.spacegame.client.ecs.system.InConstructionRenderSystem;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectedRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectionSystem;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.UIUtil;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.event.BeginLevelEvent;
import com.sergey.spacegame.common.game.Level;

public class GameScreen extends BaseScreen {
    
    private OrthographicCamera camera;
    
    private OrderSystem                orderSystem;
    private MainRenderSystem           mainRenderSystem;
    private OrderRenderSystem          orderRenderSystem;
    private SelectedRenderSystem       selectedRenderSystem;
    private InConstructionRenderSystem inConstructionRenderSystem;
    private SelectionSystem            selectionControlSystem;
    private CommandUISystem            commandUISystem;
    private HUDSystem                  hudSystem;
    
    private ECSManager ecsManager;
    
    private Level        level;
    private DrawingBatch batch;
    
    public GameScreen(Level level) {
        this.level = level;
    }
    
    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new DrawingBatch(1000, UIUtil.compileShader(Gdx.files.internal("shaders/basic.vertex.glsl"), Gdx.files.internal("shaders/basic.fragment.glsl")), true);
        batch.setLineWidth(1f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        ecsManager = level.getECS();
        ecsManager.addSystem(orderSystem = new OrderSystem(level));
        
        ecsManager.addSystem(mainRenderSystem = new MainRenderSystem(batch));
        ecsManager.addSystem(orderRenderSystem = new OrderRenderSystem(batch));
        ecsManager.addSystem(selectedRenderSystem = new SelectedRenderSystem(batch));
        ecsManager.addSystem(inConstructionRenderSystem = new InConstructionRenderSystem(batch));
        ecsManager.addSystem(commandUISystem = new CommandUISystem(camera, batch, level));
        ecsManager.addSystem(selectionControlSystem = new SelectionSystem(camera, batch, commandUISystem));
        ecsManager.addSystem(hudSystem = new HUDSystem(batch, commandUISystem));
        
        SpaceGame.getInstance().getEventBus().post(new BeginLevelEvent());
    }
    
    @Override
    public void render(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        ecsManager.update(Gdx.graphics.getDeltaTime());
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
        ecsManager.removeSystem(orderSystem);
        ecsManager.removeSystem(mainRenderSystem);
        ecsManager.removeSystem(orderRenderSystem);
        ecsManager.removeSystem(selectedRenderSystem);
        ecsManager.removeSystem(selectionControlSystem);
        ecsManager.removeSystem(inConstructionRenderSystem);
        ecsManager.removeSystem(commandUISystem);
        ecsManager.removeSystem(hudSystem);
        
        batch.dispose();
    }
    
    @Override
    public void dispose() {}
}
