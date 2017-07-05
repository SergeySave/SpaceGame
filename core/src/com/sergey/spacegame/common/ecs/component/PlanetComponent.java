package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.sergey.spacegame.common.math.AngleRange;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class PlanetComponent implements ClonableComponent {
    
    public static final ComponentMapper<PlanetComponent> MAPPER = ComponentMapper.getFor(PlanetComponent.class);
    
    private List<AngleRange> freeSpaces;
    
    public PlanetComponent() {
        freeSpaces = new LinkedList<>();
    }
    
    public boolean isFree(float min, float max) {
        if (freeSpaces.isEmpty()) return true;
        return freeSpaces.stream()
                .filter((ar) -> ar.isInRange(min))
                .findFirst()
                .map((ar) -> ar.isInRange(max))
                .orElse(false);
    }
    
    public boolean addBuildingInRange(float min, float max) {
        if (freeSpaces.isEmpty()) {
            //If no ranges add a range representing the open space
            freeSpaces.add(new AngleRange(max, min));
            return true;
        }
        
        ListIterator<AngleRange> iterator = freeSpaces.listIterator();
        while (iterator.hasNext()) {
            AngleRange curr = iterator.next();
            if (curr.isInRange(min)) {
                if (curr.isInRange(max)) {
                    AngleRange rangeHigh;
                    if (curr.getMaxD() > 360) {
                        rangeHigh = new AngleRange(max, curr.getMaxD() - 360);
                        curr.setMax(min);
                    } else {
                        rangeHigh = new AngleRange(max, curr.getMaxD());
                        curr.setMax(min);
                    }
                    
                    iterator.add(rangeHigh);
                    return true;
                } else {
                    //Only one side is in the range
                    return false;
                }
            }
        }
        //Failed to find a range
        return false;
    }
    
    public boolean removeBuilding(float min, float max) {
        if (freeSpaces.isEmpty()) return false;
        
        Iterator<AngleRange> iterator = freeSpaces.iterator();
        while (iterator.hasNext()) {
            AngleRange range = iterator.next();
            if (range.getMaxD() == min) {
                AngleRange nextRange = iterator.next();
                iterator.remove();
                
                range.setMax(nextRange.getMaxD());
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Component copy() {
        return new PlanetComponent();
    }
}
