package com.sergey.spacegame.common.event;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.IDComponent;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Selection Change Event done in a way that the event is created through a builder that uses a single instance
 */
public class SelectionChangeEvent extends Event {
	private List<Entity> _selected;

	public final SelectionGetter selected = new SelectionGetter();

	public List<Entity> getSelected() {
		return _selected;
	}

	public static class Builder {
		private SelectionChangeEvent selectionChangeEvent =  new SelectionChangeEvent();

		public SelectionChangeEvent get(List<Entity> selected) {
			selectionChangeEvent._selected = selected;

			return  selectionChangeEvent;
		}
	}

	public class SelectionGetter extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return CoerceJavaToLua.coerce(_selected.stream().map((e)->IDComponent.MAPPER.get(e).id).collect(Collectors.toList()));
		}
	}
}
