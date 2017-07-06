package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.game.command.Command;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class HUDSystem extends EntitySystem implements EntityListener {
    
    private CommandUISystem        commandUI;
    private ImmutableArray<Entity> selectedEntities;
    private Skin skin = SpaceGame.getInstance().getSkin();
    
    private TooltipManager tooltipManager;
    
    private DrawingBatch batch;
    
    private Stage      stage;
    private Table      topTable;
    private Table      leftTable;
    private Table      rightTable;
    private Table      actionBar;
    private TextButton collapseMinimap;
    
    public HUDSystem(DrawingBatch batch, CommandUISystem commandUI) {
        super(6);
        this.batch = batch;
        this.commandUI = commandUI;
        tooltipManager = new TooltipManager();
        tooltipManager.initialTime = 0.50f;
        tooltipManager.subsequentTime = 0.00f;
        tooltipManager.resetTime = 0.00f;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        Family family = Family.all(SelectedComponent.class, ControllableComponent.class).get();
        selectedEntities = engine.getEntitiesFor(family);
        engine.addEntityListener(family, this);
        
        
        stage = new Stage(new ScreenViewport());
        SpaceGame.getInstance().getInputMultiplexer().addProcessor(0, stage);
        
        Table table = new Table();
        table.setFillParent(true);
        
        stage.addActor(table);
        
        {
            topTable = new Table();
            table.add(topTable)
                    .fillX()
                    .expandX()
                    .height(Value.percentHeight(0.035f, table))
                    .align(Align.top)
                    .colspan(3);
        }
        table.row();
        {
            leftTable = new Table();
            table.add(leftTable).fillY().expandY().width(Value.percentWidth(0.20f, table)).align(Align.left);
            
            table.add().fill().expand();
            
            rightTable = new Table();
            table.add(rightTable)
                    .height(Value.percentWidth(0.20f, table))
                    .width(Value.percentWidth(0.20f, table))
                    .align(Align.bottomRight);
        }
        table.row();
        {
            Table bottomTable = new Table();
            table.add(bottomTable)
                    .fillX()
                    .expandX()
                    .height(Value.percentHeight(0.075f, table))
                    .align(Align.bottom)
                    .colspan(3);
            
            actionBar = new Table();
            ScrollPane scroll = new ScrollPane(actionBar, skin, "noBg");
            actionBar.align(Align.left);
            bottomTable.add(scroll).expand().fill().align(Align.left);
            collapseMinimap = new TextButton("V", skin);
            bottomTable.add(collapseMinimap)
                    .expandY()
                    .fillY()
                    .width(Value.percentHeight(1f, bottomTable))
                    .align(Align.left);
        }
        table.setDebug(true); // This is optional, but enables debug lines for tables.
        
        recalculateUI();
    }
    
    private void recalculateUI() {
        
        LinkedHashSet<Command> commands = null;
        for (Entity e : selectedEntities) {
            if (commands == null) {
                commands = new LinkedHashSet<>();
                commands.addAll(ControllableComponent.MAPPER.get(e).commands);
            } else {
                commands.removeIf(command -> !ControllableComponent.MAPPER.get(e).commands.contains(command));
            }
        }
        
        actionBar.clear();
        if (commands != null) {
            Command           uiCmd   = commandUI.getCommand();
            List<ImageButton> buttons = new LinkedList<>();
            for (Command cmd : commands) {
                ImageButtonStyle ibs = skin.get(ImageButtonStyle.class);
                ibs = new ImageButtonStyle(ibs);
                ibs.imageUp = skin.getDrawable(cmd.getDrawableName());
                if (cmd.getDrawableCheckedName() != null) {
                    ibs.imageOver = skin.getDrawable(cmd.getDrawableCheckedName());
                }
                //ibs.imageChecked = skin.getDrawable(cmd.getDrawableCheckedName());
                //ibs.checkedOffsetX = 10;
                //ibs.checkedOffsetY = 20;
                ImageButton butt = new ImageButton(ibs);
                buttons.add(butt);
                butt.getImageCell().grow();
                butt.setChecked(cmd.equals(uiCmd));
                butt.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (butt.isChecked()) {
                            //Set this as the command
                            commandUI.setCommand(cmd);
                            //Disable other buttons
                            buttons.forEach((b) -> { if (b != butt) b.setChecked(false); });
                        } else {
                            //If trying to uncheck recheck
                            if (commandUI.getCommand() == cmd) {
                                butt.setChecked(true);
                            }
                        }
                    }
                });
                butt.addListener(new Tooltip<Actor>(new Label(SpaceGame.getInstance()
                                                                      .getLocalizations()
                                                                      .get(cmd.getName()), skin), tooltipManager));
                actionBar.add(butt)
                        .height(Value.percentHeight(1f, actionBar))
                        .width(Value.percentHeight(1f, actionBar))
                        .align(Align.left)
                        .pad(0, 5, 0, 5);
            }
        }
        
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        selectedEntities = null;
        engine.removeEntityListener(this);
        
        SpaceGame.getInstance().getInputMultiplexer().removeProcessor(stage);
        stage.dispose();
    }
    
    @Override
    public void update(float deltaTime) {
        batch.end();
        stage.act(deltaTime);
        stage.draw();
        batch.begin();
    }
    
    @Override
    public void entityAdded(Entity entity) {
        recalculateUI();
    }
    
    @Override
    public void entityRemoved(Entity entity) {
        recalculateUI();
    }
}
