/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.es;

import com.swe.es.ComponentsControl;
import com.jme3.scene.Spatial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public final class EntitySpatialsSystem implements EntityComponent {

    private static Map<Long, EntitySpatialsControl> spatialControl;

    public EntitySpatialsSystem() {
        spatialControl = new ConcurrentHashMap<Long, EntitySpatialsControl>();
    }

    public EntitySpatialsControl setSpatialControl(Spatial sp, long ID, ComponentsControl control) {
        if (spatialControl.get(ID) != null) {
            return null;
        }

        EntitySpatialsControl spControl = new EntitySpatialsControl(sp, ID, control);
        spatialControl.put(ID, spControl);
        return spControl;
    }

    public EntitySpatialsControl getSpatialControl(long ID) {
        return spatialControl.get(ID);
    }

    public void removeSpatialControl(long ID) {
        spatialControl.get(ID).destroy();
        spatialControl.remove(ID);
    }
}
