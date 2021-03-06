package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

/**
 * Represents a component that can be cloned
 *
 * @author sergeys
 */
public interface ClonableComponent extends Component {
    
    /**
     * This should return a deep copy of this component with the exception of immutable objects
     * and objects that don't make sense to clone.
     *
     * Depending on how the component works it can just return itself
     *
     * <code>this.copy().equals(this)</code> should always return true
     *
     * @return a copy of this component
     */
    Component copy();
}
