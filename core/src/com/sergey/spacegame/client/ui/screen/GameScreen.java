package com.sergey.spacegame.client.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.system.CommandUISystem;
import com.sergey.spacegame.client.ecs.system.HUDSystem;
import com.sergey.spacegame.client.ecs.system.LineRenderSystem;
import com.sergey.spacegame.client.ecs.system.MainRenderSystem;
import com.sergey.spacegame.client.ecs.system.OrderRenderSystem;
import com.sergey.spacegame.client.ecs.system.SelectionSystem;
import com.sergey.spacegame.client.ecs.system.VisualUpdateSystem;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.UIUtil;
import com.sergey.spacegame.common.ecs.ECSManager;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.event.BeginLevelEvent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.LevelLimits;
import com.sergey.spacegame.common.ui.IViewport;
import org.jetbrains.annotations.NotNull;

public class GameScreen extends BaseScreen implements IViewport {
    
    private static final float DEF_MULT = Color.toFloatBits(1f, 1f, 1f, 1f);
    private static final float DEF_ADD  = Color.toFloatBits(0f, 0f, 0f, 0f);
    
    private OrthographicCamera camera;
    private OrthographicCamera screenCamera;
    
    private OrderSystem        orderSystem;
    private MainRenderSystem   mainRenderSystem;
    private LineRenderSystem   lineSystem;
    private OrderRenderSystem  orderRenderSystem;
    private VisualUpdateSystem visualUpdateSystem;
    private SelectionSystem    selectionControlSystem;
    private CommandUISystem    commandUISystem;
    private HUDSystem          hudSystem;
    
    private ECSManager ecsManager;
    
    private Level        level;
    private DrawingBatch batch;
    
    private Rectangle screen = new Rectangle();
    
    private InputAdapter gameInputAdapter;
    private boolean lastControllable = true;
    
    private boolean viewportControllable = true;
    
    public GameScreen(Level level) {
        this.level = level;
    }
    
    @Override
    public void show() {
        screenCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.x = 0;
        camera.position.y = 0;
        batch = new DrawingBatch(1000, UIUtil.compileShader(Gdx.files.internal("shaders/basic.vertex.glsl"), Gdx.files.internal("shaders/basic.fragment.glsl")), true);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    
        setLevel(level);
    }
    
    @Override
    public void render(float delta) {
        if (lastControllable != level.isControllable()) {
            lastControllable = level.isControllable();
            selectionControlSystem.setProcessing(lastControllable);
            commandUISystem.setProcessing(lastControllable);
            orderRenderSystem.setProcessing(lastControllable);
        }
        
        LevelLimits limits = level.getLimits();
    
        //In case foreign code updated camera position
        camera.position.x = screen.x;// + screen.width / 2;
        camera.position.y = screen.y;// + screen.height / 2;
    
        //Zoom limits
        if (camera.zoom * camera.viewportWidth > limits.getWidth() ||
            camera.zoom * camera.viewportHeight > limits.getHeight()) {
            camera.zoom = Math.min(
                    limits.getWidth() / camera.viewportWidth, limits.getHeight() / camera.viewportHeight);
        }
    
        if (viewportControllable) {
            //Translate controls
            if (Gdx.input.isKeyPressed(Keys.W)) {
                camera.position.y += camera.zoom * camera.viewportHeight * 0.01;
            }
            if (Gdx.input.isKeyPressed(Keys.S)) {
                camera.position.y -= camera.zoom * camera.viewportHeight * 0.01;
            }
            if (Gdx.input.isKeyPressed(Keys.D)) {
                camera.position.x += camera.zoom * camera.viewportWidth * 0.01;
            }
            if (Gdx.input.isKeyPressed(Keys.A)) {
                camera.position.x -= camera.zoom * camera.viewportWidth * 0.01;
            }
        }
    
        //Translate limits
        if (camera.position.x + camera.zoom * camera.viewportWidth / 2 > limits.getMaxX()) {
            camera.position.x = limits.getMaxX() - camera.zoom * camera.viewportWidth / 2;
        }
        if (camera.position.x - camera.zoom * camera.viewportWidth / 2 < limits.getMinX()) {
            camera.position.x = limits.getMinX() + camera.zoom * camera.viewportWidth / 2;
        }
        if (camera.position.y + camera.zoom * camera.viewportHeight / 2 > limits.getMaxY()) {
            camera.position.y = limits.getMaxY() - camera.zoom * camera.viewportHeight / 2;
        }
        if (camera.position.y - camera.zoom * camera.viewportHeight / 2 < limits.getMinY()) {
            camera.position.y = limits.getMinY() + camera.zoom * camera.viewportHeight / 2;
        }
        
        camera.update();
    
        screen.width = camera.zoom * camera.viewportWidth;
        screen.height = camera.zoom * camera.viewportHeight;
        screen.x = camera.position.x;// - screen.width / 2;
        screen.y = camera.position.y;// - screen.height / 2;
    
        batch.setProjectionMatrix(screenCamera.combined);
        batch.setMultTint(DEF_MULT);
        batch.setAddTint(DEF_ADD);
        batch.begin();
    
        TextureRegion region = SpaceGame.getInstance().getRegion(level.getBackground().getImage());
        float         width  = screenCamera.viewportWidth;
        float         height = screenCamera.viewportHeight;
        if (width < region.getRegionWidth() / region.getRegionHeight() * height) {
            width = region.getRegionWidth() / region.getRegionHeight() * height;
        } else if (height < region.getRegionHeight() / region.getRegionWidth() * width) {
            height = region.getRegionHeight() / region.getRegionWidth() * width;
        }
    
        batch.draw(region, screenCamera.position.x - height / 2, screenCamera.position.y - width / 2, width, height);
    
        batch.setProjectionMatrix(camera.combined);
        
        ecsManager.update(Gdx.graphics.getDeltaTime());
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    
        screenCamera.setToOrtho(false, width, height);
        //camera.setToOrtho(false, camera.viewportWidth, camera.viewportWidth * height/width);
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
        SpaceGame.getInstance().getInputMultiplexer().removeProcessor(gameInputAdapter);
        
        ecsManager.removeSystem(orderSystem);
        ecsManager.removeSystem(mainRenderSystem);
        ecsManager.removeSystem(lineSystem);
        ecsManager.removeSystem(orderRenderSystem);
        ecsManager.removeSystem(visualUpdateSystem);
        ecsManager.removeSystem(selectionControlSystem);
        ecsManager.removeSystem(commandUISystem);
        ecsManager.removeSystem(hudSystem);
        
        batch.dispose();
    }
    
    @Override
    public void dispose() {}
    
    @Override
    public boolean isViewportControllable() {
        return viewportControllable;
    }
    
    @Override
    public void setViewportControllable(boolean controllable) {
        viewportControllable = controllable;
    }
    
    @Override
    public float getViewportX() {
        return screen.x;
    }
    
    @Override
    public void setViewportX(float x) {
        screen.x = x;
    }
    
    @Override
    public float getViewportY() {
        return screen.y;
    }
    
    @Override
    public void setViewportY(float y) {
        screen.y = y;
    }
    
    @Override
    public float getViewportWidth() {
        return screen.width;
    }
    
    @Override
    public void setLevel(
            @NotNull
                    Level level) {
        if (this.ecsManager != null) {
            ecsManager.removeSystem(orderSystem);
            ecsManager.removeSystem(mainRenderSystem);
            ecsManager.removeSystem(lineSystem);
            ecsManager.removeSystem(orderRenderSystem);
            ecsManager.removeSystem(visualUpdateSystem);
            ecsManager.removeSystem(selectionControlSystem);
            ecsManager.removeSystem(commandUISystem);
            ecsManager.removeSystem(hudSystem);
        }
    
        this.level = level;
    
        camera.position.x = 0;
        camera.position.y = 0;
    
        level.setViewport(this);
        ecsManager = level.getECS();
        ecsManager.addSystem(orderSystem = new OrderSystem(level));
    
        ecsManager.addSystem(mainRenderSystem = new MainRenderSystem(batch));
        ecsManager.addSystem(lineSystem = new LineRenderSystem(batch));
        ecsManager.addSystem(orderRenderSystem = new OrderRenderSystem(batch));
        ecsManager.addSystem(visualUpdateSystem = new VisualUpdateSystem());
        ecsManager.addSystem(commandUISystem = new CommandUISystem(camera, batch, level));
        ecsManager.addSystem(selectionControlSystem = new SelectionSystem(camera, batch, commandUISystem, level.getPlayer1()
                .getTeam()));
        ecsManager.addSystem(hudSystem = new HUDSystem(batch, commandUISystem, level, screen));
    
        SpaceGame.getInstance().getEventBus().post(new BeginLevelEvent(level));
    
        if (gameInputAdapter != null) {
            SpaceGame.getInstance().getInputMultiplexer().removeProcessor(gameInputAdapter);
        }
    
        SpaceGame.getInstance().getInputMultiplexer().addProcessor(gameInputAdapter = new InputAdapter() {
            private final float STRENGTH = 0.95f;
        
            @Override
            public boolean scrolled(int amount) {
                if (viewportControllable) {
                    if (amount > 0) {
                        camera.zoom *= STRENGTH;
                    } else if (amount < 0) {
                        camera.zoom /= STRENGTH;
                    }
                }
                return false;
            }
        });
    }
    
    @Override
    public float getViewportHeight() {
        return screen.height;
    }
    
    @Override
    public void setViewportWidth(float width) {
        camera.zoom = width / camera.viewportWidth;
    }
    
    @Override
    public void setViewportHeight(float height) {
        camera.zoom = height / camera.viewportHeight;
    }
    
    @Override
    public void close() {
        SpaceGame.getInstance().setScreenAndDisposeOld(new MainMenuScreen());
    }
}
