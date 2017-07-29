package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.scene2d.MinimapDrawable;
import com.sergey.spacegame.client.ui.scene2d.RadialSprite;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.Objective;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.lua.LuaPredicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

public class HUDSystem extends EntitySystem implements EntityListener {
    
    private CommandUISystem        commandUI;
    private ImmutableArray<Entity> selectedEntities;
    private Skin skin = SpaceGame.getInstance().getSkin();
    
    private TooltipManager tooltipManager;
    private Level          level;
    private Rectangle      screen;
    private DrawingBatch   batch;
    private int            lastObjectives;
    
    private Stage      stage;
    private Table      topTable;
    private Label      moneyLabel;
    private TextButton collapseObjectives;
    private Table      objectivesTable;
    private LabelStyle notCompletedStyle;
    private LabelStyle completedStyle;
    private Image      minimap;
    private Table      actionBar;
    private TextButton collapseMinimap;
    
    private List<CommandButton> buttons;
    
    public HUDSystem(DrawingBatch batch, CommandUISystem commandUI, Level level, Rectangle screen) {
        super(6);
        this.batch = batch;
        this.commandUI = commandUI;
        this.level = level;
        this.screen = screen;
        tooltipManager = new TooltipManager();
        tooltipManager.initialTime = 0.00f;
        tooltipManager.subsequentTime = tooltipManager.initialTime;
        tooltipManager.resetTime = 0.00f;
        tooltipManager.hideAll();
    
        buttons = new ArrayList<>();
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        Family family = Family.all(SelectedComponent.class, ControllableComponent.class).get();
        selectedEntities = engine.getEntitiesFor(family);
        engine.addEntityListener(family, this);
        
        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(true);
        SpaceGame.getInstance().getInputMultiplexer().addProcessor(0, stage);
        
        Table table = new Table();
        table.setFillParent(true);
        
        stage.addActor(table);
    
        { //Top
            topTable = new Table();
            table.add(topTable)
                    .fillX()
                    .expandX()
                    .height(Value.percentHeight(0.035f, table))
                    .align(Align.topLeft)
                    .colspan(3);
    
            collapseObjectives = new TextButton("^", skin);
            collapseObjectives.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    objectivesTable.setVisible(!objectivesTable.isVisible());
                    collapseObjectives.setText(objectivesTable.isVisible() ? "^" : "V");
                }
            });
            topTable.add(collapseObjectives)
                    .expandY()
                    .fillY()
                    .width(Value.percentHeight(1f))
                    .align(Align.left)
                    .padRight(5f);
            
            topTable.add(new Label(SpaceGame.getInstance().localize("game.label.money"), skin, "small"))
                    .align(Align.left)
                    .pad(1, 5, 1, 1);
    
            moneyLabel = new Label("", skin, "small");
            topTable.add(moneyLabel).align(Align.left).pad(1, 5, 1, 1);
    
            topTable.add().expand();
        }
        table.row();
        {  //Left
            Table leftTable = new Table();
            table.add(leftTable).fillY().expandY().width(Value.percentWidth(0.20f, table)).align(Align.topLeft);
    
            objectivesTable = new Table();
            leftTable.add(objectivesTable).fill().expand().align(Align.topLeft);
    
            notCompletedStyle = skin.get("small", LabelStyle.class);
            completedStyle = skin.get("smallCompleted", LabelStyle.class);
        }
        table.add().fill().expand();
        { //Right
            Table rightTable = new Table();
    
            MinimapDrawable minimapDrawable = new MinimapDrawable(SpaceGame.getInstance()
                                                                          .getRegion("team1"), SpaceGame.getInstance()
                                                                          .getRegion("team2"), SpaceGame.getInstance()
                                                                          .getRegion("neutral"), SpaceGame.getInstance()
                                                                          .getRegion("whitePixel"), level, screen);
            minimap = new Image(minimapDrawable);
            minimap.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    //Prevent touch events from going through
                }
            });
            rightTable.add().expandY();
            rightTable.row();
            rightTable.add(minimap)
                    .expandX()
                    .prefWidth(Value.percentHeight(
                            level.getLimits().getWidth() / level.getLimits().getHeight(), rightTable))
                    .prefHeight(Value.percentWidth(level.getLimits().getHeight() / level.getLimits().getWidth()))
                    .align(Align.bottomRight);
            
            table.add(rightTable)
                    //.height(Value.percentWidth(0.20f, table))
                    .expandY()
                    .fillY()
                    .width(Value.percentWidth(0.20f, table))
                    .align(Align.bottomRight);
        }
        table.row();
        { //Bottom
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
            collapseMinimap.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    minimap.setVisible(!minimap.isVisible());
                    collapseMinimap.setText(minimap.isVisible() ? "V" : "^");
                }
            });
            bottomTable.add(collapseMinimap)
                    .expandY()
                    .fillY()
                    .width(Value.percentHeight(1f, bottomTable))
                    .align(Align.right);
        }
        //table.setDebug(true); // This is optional, but enables debug lines for tables.
        
        recalculateUI();
    }
    
    private void recalculateUI() {
        
        LinkedHashSet<Command> commands = null;
        for (Entity e : selectedEntities) {
            if (commands == null) {
                commands = new LinkedHashSet<>();
                commands.addAll(ControllableComponent.MAPPER.get(e).commands);
            } else {
                commands.removeIf(command -> !command.getAllowMulti() ||
                                             !ControllableComponent.MAPPER.get(e).commands.contains(command));
            }
        }
    
        if (commands != null) {
            if (commands.size() >= buttons.size()) {
                Iterator<Command> iterator = commands.iterator();
                int               i        = 0;
                while (iterator.hasNext()) {
                    CommandButton button;
                    if (i >= buttons.size()) {
                        buttons.add(button = newCommandButton());
                
                        actionBar.add(button.stack)
                                .expandY()
                                .fillY()
                                .width(Value.percentHeight(1f, button.stack))
                                .align(Align.left)
                                .pad(0, 5, 5, 5);
                    } else {
                        button = buttons.get(i);
                    }
            
                    setVisible(button, iterator.next());
            
                    ++i;
                }
            } else {
                Iterator<Command> iterator = commands.iterator();
                for (int i = 0; i < buttons.size(); i++) {
                    CommandButton button = buttons.get(i);
                    if (i >= commands.size()) {
                        button.stack.setVisible(false);
                        button.command = null;
                    } else {
                        setVisible(button, iterator.next());
                    }
                }
            }
        } else {
            for (CommandButton button : buttons) {
                button.stack.setVisible(false);
            }
        }
    }
    
    private CommandButton newCommandButton() {
        CommandButton commandButton = new CommandButton();
        commandButton.stack = new Stack();
        {
            ImageButtonStyle ibs = skin.get(ImageButtonStyle.class);
            ibs = new ImageButtonStyle(ibs);
            
            commandButton.button = new ImageButton(ibs);
            commandButton.button.getImageCell().grow();
            commandButton.button.setProgrammaticChangeEvents(false);
            commandButton.button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    commandButton.button.setChecked(true);
                    commandUI.setCommand(commandButton.command);
                }
            });
    
            commandButton.tooltipTable = new Table();
            commandButton.tooltipTable.pad(15f).background(skin.getDrawable("default-rect"));
    
            commandButton.tooltipTitle = new Label("", skin);
            commandButton.tooltipDesc = new Label("", skin, "small");
            commandButton.tooltipReqLabel = new Label("Requirements:", skin, "small");
            commandButton.tooltipReqs = new VerticalGroup();
    
            commandButton.button.addListener(new Tooltip<>(commandButton.tooltipTable, tooltipManager));
            
            commandButton.stack.add(commandButton.button);
        }
        {
            commandButton.radialSprite = new RadialSprite(SpaceGame.getInstance().getRegion("radialBar"));
        
            Image radialImage = new Image(commandButton.radialSprite);
            commandButton.radialSpriteContainer = new Container<>(radialImage);
            commandButton.radialSpriteContainer.fill();
            commandButton.radialSpriteContainer.setVisible(false);
            commandButton.radialSpriteContainer.setTouchable(Touchable.disabled);
        
            commandButton.stack.add(commandButton.radialSpriteContainer);
        }
        {
            commandButton.label = new Label("", skin, "small");
            commandButton.label.setAlignment(Align.bottomLeft);
            commandButton.label.setVisible(false);
            commandButton.label.setTouchable(Touchable.disabled);
            commandButton.stack.add(commandButton.label);
        }
        
        return commandButton;
    }
    
    private void setVisible(CommandButton button, Command command) {
        button.command = command;
        button.button.setChecked(button.command.equals(commandUI.getCommand()));
        button.stack.setVisible(true);
        ImageButtonStyle style = button.button.getStyle();
        style.imageUp = skin.getDrawable(button.command.getDrawableName());
        style.imageUp.setMinHeight(1f);
        style.imageUp.setMinWidth(1f);
        if (button.command.getDrawableCheckedName() == null) {
            style.imageOver = null;
        } else {
            style.imageOver = skin.getDrawable(button.command.getDrawableCheckedName());
            style.imageOver.setMinHeight(1f);
            style.imageOver.setMinWidth(1f);
        }
    
        button.tooltipTable.clearChildren();
    
        button.tooltipTitle.setText(SpaceGame.getInstance().localize(button.command.getName()));
        button.tooltipTable.add(button.tooltipTitle).align(Align.left);
        button.tooltipTable.row();
    
        String desc = SpaceGame.getInstance().localize(button.command.getDesc());
        button.tooltipDesc.setText(desc);
        if (!desc.isEmpty()) {
            button.tooltipTable.add(button.tooltipDesc).align(Align.left);
            button.tooltipTable.row();
        }
        if (command.getReq() != null) {
            button.tooltipTable.add(button.tooltipReqLabel).align(Align.left).padTop(20f);
            button.tooltipTable.row();
        
            button.tooltipTable.add(button.tooltipReqs).align(Align.left).padLeft(15f);
            button.tooltipTable.row();
        
            if (button.tooltipReqs.getChildren().size > command.getReq().size()) {
                Iterator<Entry<String, LuaPredicate>> iterator = command.getReq().entrySet().iterator();
                for (Actor actor : button.tooltipReqs.getChildren()) {
                    if (!iterator.hasNext()) {
                        button.tooltipReqs.removeActor(actor);
                    } else {
                        Entry<String, LuaPredicate> entry = iterator.next();
                        Label                       label = (Label) actor;
                        label.setText(SpaceGame.getInstance()
                                              .localize("command." + command.getId() + ".req." + entry.getKey() +
                                                        ".name"));
                    }
                }
            } else {
                Iterator<Actor> iterator = button.tooltipReqs.getChildren().iterator();
                for (Entry<String, LuaPredicate> entry : command.getReq().entrySet()) {
                    Label label;
                    if (iterator.hasNext()) {
                        label = ((Label) iterator.next());
                    } else {
                        label = new Label("", skin, "small");
                        button.tooltipReqs.addActor(label);
                    }
                    label.setText(SpaceGame.getInstance()
                                          .localize("command." + command.getId() + ".req." + entry.getKey() + ".name"));
                }
            }
        }
        
        button.label.setVisible(false);
        button.radialSpriteContainer.setVisible(false);
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
    
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    
        int hash = level.getObjectives().hashCode() * 31 + level.getObjectives()
                .size(); //So that a list of items with hashcode -30 won't be the same no matter the size
        if (lastObjectives != hash) {
            lastObjectives = hash;
            objectivesTable.clearChildren();
            for (Objective objective : level.getObjectives()) {
                Label label = new Label(SpaceGame.getInstance()
                                                .localize(objective.getTitle()), objective.getCompleted() ?
                                                completedStyle :
                                                notCompletedStyle);
                label.addListener(new Tooltip<Actor>(new Label(SpaceGame.getInstance()
                                                                       .localize(objective.getDescription()), skin, "small"), tooltipManager));
                objectivesTable.add(label).expandX().fillX().align(Align.topLeft).pad(1, 5, 1, 1);
                objectivesTable.row();
            }
            objectivesTable.add().expand();
        }
    
        moneyLabel.setText(String.format("%1$,.2f", level.getMoney()));
    
        updateCommands();
        
        stage.act(deltaTime);
        stage.draw();
    
        batch.begin();
    }
    
    private void updateCommands() {
        for (CommandButton button : buttons) {
            if (button.command == null) return;
    
            button.button.setChecked(button.command.equals(commandUI.getCommand()));
    
            boolean allEnabled = true;
            if (button.command.getReq() != null) {
                for (LuaPredicate predicate : button.command.getReq().values()) {
                    if (!predicate.test()) {
                        allEnabled = false;
                        break;
                    }
                }
            }
    
            if (allEnabled) {
                button.radialSpriteContainer.setVisible(false);
            } else {
                button.radialSpriteContainer.setVisible(true);
                button.radialSprite.setAngle(0);
            }
            
            if (!button.command.getAllowMulti()) {
                String orderTag = button.command.getOrderTag();
                if (selectedEntities.size() == 0 || orderTag == null) {
                    continue;
                }
                Entity entity = selectedEntities.first(); // Only one entity available since it's a not allow multi
                
                OrderComponent orderComponent = OrderComponent.MAPPER.get(entity);
                if (orderComponent == null) {
                    button.label.setVisible(false);
                    button.radialSpriteContainer.setVisible(false);
                } else {
                    int   count     = 0;
                    float firstTime = -1;
                    
                    for (IOrder order : orderComponent) {
                        if (orderTag.equals(order.getTag())) {
                            if (++count == 1) {
                                firstTime = order.getEstimatedPercentComplete();
                            }
                        }
                    }
    
                    if (count > 0) {
                        button.radialSpriteContainer.setVisible(true);
                        button.radialSprite.setAngle(360 * firstTime);
                    } else {
                        button.radialSpriteContainer.setVisible(false);
                    }
                    
                    if (count > 1) {
                        button.label.setVisible(true);
                        button.label.setText("" + count);
                    } else {
                        button.label.setVisible(false);
                    }
                }
            }
        }
    }
    
    @Override
    public void entityAdded(Entity entity) {
        recalculateUI();
    }
    
    @Override
    public void entityRemoved(Entity entity) {
        recalculateUI();
    }
    
    private static class CommandButton {
        
        public Command command;
        
        public Stack stack;
    
        public ImageButton      button;
    
        public Table         tooltipTable;
        public Label         tooltipTitle;
        public Label         tooltipDesc;
        public Label         tooltipReqLabel;
        public VerticalGroup tooltipReqs;
        
        public Label            label;
        public Container<Image> radialSpriteContainer;
        public RadialSprite     radialSprite;
    }
}
