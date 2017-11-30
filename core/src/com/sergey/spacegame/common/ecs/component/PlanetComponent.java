package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.math.AngleRange;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This component represents a planet
 * It stores a list of free angle ranges where buildingsg can go
 *
 * @author sergeys
 */
public class PlanetComponent implements ClonableComponent {
    
    public static final ComponentMapper<PlanetComponent> MAPPER = ComponentMapper.getFor(PlanetComponent.class);
    
    private List<AngleRange> freeSpaces;
    
    /**
     * Create a new PlanetComponent
     */
    public PlanetComponent() {
        freeSpaces = new LinkedList<>();
    }
    
    /**
     * Check if a given range is free
     *
     * @param min - the minimum angle
     * @param max - the maximum angle
     * @param planet - the parent planet
     *
     * @return is the range described by this angle free
     */
    public boolean isFree(float min, float max, Entity planet) {
        if (freeSpaces.isEmpty()) return true;
    
        return freeSpaces.stream()
                .filter((ar) -> ar.isInRange(min))
                .findFirst()
                .map((ar) -> ar.isInRange(max))
                .orElse(false);
    }
    
    /**
     * Mark a given range as not free
     *
     * @param min - the minimum angle
     * @param max - the maximum angle
     * @param planet - the parent planet
     *
     * @return whether the component changed
     */
    public boolean addBuildingInRange(float min, float max, Entity planet) {
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
    
    /**
     * Mark a given filled range as free
     * The same range must be in the planet already
     *
     * @param min - the minimum angle
     * @param max - the maximum angle
     * @param planet - the parent planet
     *
     * @return whether the component changed
     */
    public boolean removeBuilding(float min, float max, Entity planet) {
        if (freeSpaces.isEmpty()) return false;
    
        Iterator<AngleRange> iterator = freeSpaces.iterator();
        while (iterator.hasNext()) {
            AngleRange range = iterator.next();
            double     rMax  = range.getMaxD();
            if (rMax > 360) rMax -= 360;
            if (rMax == min) {
                if (iterator.hasNext()) {
                    AngleRange nextRange = iterator.next();
                    range.setMax(nextRange.getMaxD());
                }
                iterator.remove();
                
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
