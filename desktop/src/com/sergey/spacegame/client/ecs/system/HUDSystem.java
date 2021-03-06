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
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sergey.spacegame.client.SpaceGameClient;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.game.command.ClientCommand;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.client.ui.scene2d.MinimapDrawable;
import com.sergey.spacegame.client.ui.scene2d.RadialSprite;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.MessageComponent;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.Objective;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.lua.LuaPredicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Represents the system used for drawing and managing the HUD
 *
 * @author sergeys
 */
public class HUDSystem extends EntitySystem implements EntityListener {
    
    private CommandUISystem        commandUI;
    private ImmutableArray<Entity> selectedEntities;
    private Skin skin = SpaceGameClient.INSTANCE.getSkin();
    
    private LinkedList<Entity> messageEntities;
    private EntityListener     messageListener;
    
    private TooltipManager tooltipManager;
    private Level          level;
    private Rectangle      screen;
    private DrawingBatch   batch;
    private int            lastObjectives;
    private int            lastMessages;
    private int            lastWidth;
    private int            lastHeight;
    
    private Stage         stage;
    private Table         topTable;
    private Label         moneyLabel;
    private ImageButton   collapseObjectives;
    private Table         objectivesTable;
    private LabelStyle    notCompletedStyle;
    private LabelStyle    completedStyle;
    private Image         minimap;
    private Table         actionBar;
    private ImageButton   collapseMinimap;
    private VerticalGroup messageGroup;
    
    private List<CommandButton> buttons;
    private boolean isVisible = true;
    private Table rightTable;
    private Table bottomTable;
    private Table leftTable;
    
    /**
     * Creates a new HUDSystem object
     *
     * @param batch     - the batch that had been being drawn to before this system
     * @param commandUI - the command system
     * @param level     - the level to represent
     * @param screen    - a rectangle containing the region that is visible on the screen
     */
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
        messageEntities = new LinkedList<>();
        
        buttons = new ArrayList<>();
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        Family family = Family.all(SelectedComponent.class, ControllableComponent.class).get();
        selectedEntities = engine.getEntitiesFor(family);
        messageEntities.clear();
        engine.addEntityListener(Family.all(MessageComponent.class).get(), messageListener = new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                messageEntities.add(entity);
            }
            
            @Override
            public void entityRemoved(Entity entity) {
                messageEntities.remove(entity);
            }
        });
        engine.addEntityListener(family, this);
        setup();
    }
    
    /**
     * Is the HUD currently visible
     *
     * @return is the HUD visible
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Called when a brand new button needs to be created
     *
     * @return a new blank button to be added to the screen
     */
    private CommandButton newCommandButton() {
        CommandButton commandButton = new CommandButton();
        commandButton.stack = new Stack();
        { //Add the actual button itself at the bottom of the stack
            ImageButtonStyle ibs = skin.get(ImageButtonStyle.class);
            ibs = new ImageButtonStyle(ibs);
            
            commandButton.button = new ImageButton(ibs);
            commandButton.button.getImageCell().grow();
            commandButton.button.setProgrammaticChangeEvents(false);
            commandButton.button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    commandButton.button.setChecked(true);
                    if (commandButton.command instanceof ClientCommand) {
                        commandUI.setCommand((ClientCommand) commandButton.command);
                    } else {
                        System.err.println("ERROR: Command not a ClientCommand on Client side");
                    }
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
        { //Add the radial progress bar on top of the button
            commandButton.radialSprite = new RadialSprite(SpaceGameClient.INSTANCE.getRegion("radialBar"));
            
            Image radialImage = new Image(commandButton.radialSprite);
            commandButton.radialSpriteContainer = new Container<>(radialImage);
            commandButton.radialSpriteContainer.fill();
            commandButton.radialSpriteContainer.setVisible(false);
            commandButton.radialSpriteContainer.setTouchable(Touchable.disabled);
            
            commandButton.stack.add(commandButton.radialSpriteContainer);
        }
        { //Add the label on top of the progress bar
            commandButton.label = new Label("", skin, "small");
            commandButton.label.setAlignment(Align.bottomLeft);
            commandButton.label.setVisible(false);
            commandButton.label.setTouchable(Touchable.disabled);
            commandButton.stack.add(commandButton.label);
        }
        
        return commandButton;
    }
    
    /**
     * Used to set a button to visible while also updating its contents to represent the given command
     *
     * @param button  - the button to update
     * @param command - the command to represent
     */
    private void setVisible(CommandButton button, Command command) {
        button.command = command;
        button.button.setChecked(button.command.equals(commandUI.getCommand()));
        button.stack.setVisible(true);
        ImageButtonStyle style = button.button.getStyle();
        
        //Update the images on the button
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
        
        //Update the tooltip on the button
        button.tooltipTable.clearChildren();
        button.tooltipTitle.setText(SpaceGameClient.INSTANCE.localize(button.command.getName()));
        button.tooltipTable.add(button.tooltipTitle).align(Align.left);
        button.tooltipTable.row();
        
        String desc = SpaceGameClient.INSTANCE.localize(button.command.getDesc());
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
                        label.setText(SpaceGameClient.INSTANCE
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
                    label.setText(SpaceGameClient.INSTANCE
                                          .localize("command." + command.getId() + ".req." + entry.getKey() + ".name"));
                }
            }
        }
        
        //Hide the label and the progress bar until they are updated later
        button.label.setVisible(false);
        button.radialSpriteContainer.setVisible(false);
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        engine.removeEntityListener(this);
        engine.removeEntityListener(messageListener);
        selectedEntities = null;
        messageEntities = null;
        
        unsetup();
    }
    
    @Override
    public void update(float deltaTime) {
        batch.end();
        
        //If the screen size changed we need to update our UI
        if (Gdx.graphics.getWidth() != lastWidth || Gdx.graphics.getHeight() != lastHeight) {
            unsetup();
            setup();
            lastWidth = Gdx.graphics.getWidth();
            lastHeight = Gdx.graphics.getHeight();
            lastObjectives = 0;
            lastMessages = 0;
        }
        { //Update the objectives table if it has changed
            int hash = level.getObjectives().hashCode() * 31 + level.getObjectives()
                    .size(); //So that a list of items with hashcode -30 won't be the same no matter the size
            if (lastObjectives != hash) {
                lastObjectives = hash;
                objectivesTable.clearChildren();
                for (Objective objective : level.getObjectives()) {
                    Label label = new Label(SpaceGameClient.INSTANCE
                                                    .localize(objective.getTitle()), objective.getCompleted() ?
                                                    completedStyle :
                                                    notCompletedStyle);
                    label.setWrap(true);
                    label.addListener(new Tooltip<Actor>(new Label(SpaceGameClient.INSTANCE
                                                                           .localize(objective.getDescription()), skin, "small"), tooltipManager));
                    objectivesTable.add(label).expandX().fillX().align(Align.topLeft).pad(1, 5, 1, 1);
                    objectivesTable.row();
                }
                objectivesTable.add().expand();
            }
        }
        
        //Update the money label
        moneyLabel.setText(String.format("%1$,.2f", level.getPlayer1().getMoney()));
        
        { //Update the messages if they have changed
            int hash = messageEntities.hashCode() * 31 + messageEntities.size();
            if (lastMessages != hash) {
                lastMessages = hash;
                messageGroup.clear();
                for (Entity messageEntity : messageEntities) {
                    MessageComponent messageComponent = MessageComponent.MAPPER.get(messageEntity);
                    
                    Image image = new Image(messageComponent.getRegion());
                    Label label = new Label(SpaceGameClient.INSTANCE
                                                    .localize(messageComponent.getMessage()), skin, "small");
                    label.setWrap(true);
                    label.setAlignment(Align.left);
                    
                    Table smallTable = new Table();
                    smallTable.add(image)
                            .pad(5)
                            .width(Value.percentWidth(0.075f, messageGroup))
                            .height(Value.percentWidth(1));
                    smallTable.add(label).pad(5).expandX().fillX();
                    //smallTable.add().expandX();
                    
                    messageGroup.addActor(smallTable);
                }
            }
        }
        
        //Update the commands
        updateCommands();
        
        //Update the UI
        stage.act(deltaTime);
        stage.draw();
        
        batch.begin();
    }
    
    private void unsetup() {
        SpaceGameClient.INSTANCE.getInputMultiplexer().removeProcessor(stage);
        stage.dispose();
    }
    
    /**
     * Set whether the HUD should be visible
     *
     * @param visible should the HUD be visible
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
        unsetup();
        setup();
    }
    
    private void setup() {
        stage = new Stage(new ScreenViewport());
        SpaceGameClient.INSTANCE.getInputMultiplexer().addProcessor(0, stage);
        
        Table table = new Table();
        table.setFillParent(true);
        
        stage.addActor(table);
        
        ImageButtonStyle upArrow = new ImageButtonStyle(skin.get("uncheckable", ImageButtonStyle.class));
        upArrow.imageUp = skin.getDrawable("upArrow");
        upArrow.imageUp.setMinHeight(1f);
        upArrow.imageUp.setMinWidth(1f);
        
        ImageButtonStyle downArrow = new ImageButtonStyle(skin.get("uncheckable", ImageButtonStyle.class));
        downArrow.imageUp = skin.getDrawable("downArrow");
        downArrow.imageUp.setMinHeight(1f);
        downArrow.imageUp.setMinWidth(1f);
    
        if (isVisible) { //Top
            topTable = new Table();
            table.add(topTable)
                    .fillX()
                    .expandX()
                    .height(Value.percentHeight(0.035f, table))
                    .align(Align.topLeft)
                    .colspan(3);
            
            collapseObjectives = new ImageButton(upArrow);
            collapseObjectives.getImageCell().grow();
            collapseObjectives.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    objectivesTable.setVisible(!objectivesTable.isVisible());
                    collapseObjectives.setStyle(objectivesTable.isVisible() ? upArrow : downArrow);
                }
            });
            topTable.add(collapseObjectives)
                    .expandY()
                    .fillY()
                    .width(Value.percentHeight(1f, topTable))
                    .align(Align.left)
                    .padRight(5f);
            
            topTable.add(new Label(SpaceGameClient.INSTANCE.localize("game.label.money"), skin, "small"))
                    .align(Align.left)
                    .pad(1, 5, 1, 1);
            
            moneyLabel = new Label("", skin, "small");
            topTable.add(moneyLabel).align(Align.left).pad(1, 5, 1, 1);
            
            topTable.add().expand();
        }
        table.row();
        if (isVisible) {  //Left
            leftTable = new Table();
            table.add(leftTable).fillY().expandY().width(Value.percentWidth(0.20f, table)).align(Align.topLeft);
            
            objectivesTable = new Table();
            leftTable.add(objectivesTable).fill().expand().align(Align.topLeft);
            
            notCompletedStyle = skin.get("small", LabelStyle.class);
            completedStyle = skin.get("smallCompleted", LabelStyle.class);
        }
        {
            messageGroup = new VerticalGroup();
            messageGroup.align(Align.top);
            messageGroup.expand();
            messageGroup.fill();
            table.add(messageGroup).grow();
        }
        if (isVisible) { //Right
            rightTable = new Table();
            
            MinimapDrawable minimapDrawable = new MinimapDrawable(SpaceGameClient.INSTANCE
                                                                          .getRegion("team1"), SpaceGameClient.INSTANCE
                                                                          .getRegion("team2"), SpaceGameClient.INSTANCE
                                                                          .getRegion("neutral"), SpaceGameClient.INSTANCE
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
                    .prefHeight(Value.percentWidth(
                            level.getLimits().getHeight() / level.getLimits().getWidth(), rightTable))
                    .align(Align.bottomRight);
            
            table.add(rightTable)
                    //.height(Value.percentWidth(0.20f, table))
                    .expandY()
                    .fillY()
                    .width(Value.percentWidth(0.20f, table))
                    .align(Align.bottomRight);
        }
        table.row();
        if (isVisible) { //Bottom
            bottomTable = new Table();
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
            collapseMinimap = new ImageButton(downArrow);
            collapseMinimap.getImageCell().grow();
            collapseMinimap.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    minimap.setVisible(!minimap.isVisible());
                    collapseMinimap.setStyle(minimap.isVisible() ? downArrow : upArrow);
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
    
    /**
     * Called to recalculate the UI and make changes if the backing data has been modified
     */
    private void recalculateUI() {
        //All commands that are in all selected entities
        //Commands only need to be checked if they are in the first entity
        LinkedHashSet<Command> commands = null;
        for (Entity e : selectedEntities) {
            //If this is the first entity then we make a new set of the commands in the entity
            if (commands == null) {
                commands = new LinkedHashSet<>(ControllableComponent.MAPPER.get(e).commands);
            } else {
                //If it is a later entity remove the command if it doesnt allow multi selection and it isnt also
                //In the other entity
                commands.removeIf(command -> !command.getAllowMulti() ||
                                             !ControllableComponent.MAPPER.get(e).commands.contains(command));
            }
        }
        
        //If we have commands
        if (commands != null) {
            //If we dont have enough buttons
            if (commands.size() >= buttons.size()) {
                Iterator<Command> iterator = commands.iterator();
                int               i        = 0;
                //Iterate over all of the commands
                while (iterator.hasNext()) {
                    CommandButton button;
                    if (i >= buttons.size()) {
                        //If we need a new button make a new button
                        buttons.add(button = newCommandButton());
                        
                        //Add it to the screen
                        actionBar.add(button.stack)
                                .expandY()
                                .fillY()
                                .width(Value.percentHeight(1f, button.stack))
                                .align(Align.left)
                                .pad(0, 5, 5, 5);
                    } else {
                        //If we dont need a new button get the current button in the list
                        button = buttons.get(i);
                    }
                    
                    //Show the button and update its contents
                    setVisible(button, iterator.next());
                    
                    ++i;
                }
            } else {
                //We have enough buttons
                Iterator<Command> iterator = commands.iterator();
                
                for (int i = 0; i < buttons.size(); i++) {
                    CommandButton button = buttons.get(i);
                    //If we dont need the button
                    if (i >= commands.size()) {
                        //hide the button and remove its command
                        button.stack.setVisible(false);
                        button.command = null;
                    } else {
                        //If we do need it
                        //Show it and update its contents
                        setVisible(button, iterator.next());
                    }
                }
            }
        } else {
            //If we have no commands hide all of the buttons
            for (CommandButton button : buttons) {
                button.stack.setVisible(false);
            }
        }
    }
    
    /**
     * Called to update each command
     */
    private void updateCommands() {
        for (CommandButton button : buttons) {
            //If this button isnt needed then we are done updating buttons
            if (button.command == null || !button.stack.isVisible()) return;
            
            //Set if it is the current command
            button.button.setChecked(button.command.equals(commandUI.getCommand()));
            
            //Check if it is enabled
            boolean allEnabled = true;
            if (button.command.getReq() != null) {
                for (LuaPredicate predicate : button.command.getReq().values()) {
                    if (!predicate.test()) {
                        allEnabled = false;
                        break;
                    }
                }
            }
            
            //If it isn't enabled use the progress bar to make it darkened
            if (allEnabled) {
                button.radialSpriteContainer.setVisible(false);
            } else {
                button.radialSpriteContainer.setVisible(true);
                button.radialSprite.setAngle(0);
            }
            
            //If the command doesnt allow multiple selection then we can add the progress bar and order count rendering
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
        
        public ImageButton button;
        
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
