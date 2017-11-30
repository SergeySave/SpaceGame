package com.sergey.spacegame.client.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * This component is used for entities that are currently selected
 *
 * @author sergeys
 */
public class SelectedComponent implements Component {
    
    public static final ComponentMapper<SelectedComponent> MAPPER = ComponentMapper.getFor(SelectedComponent.class);
}
