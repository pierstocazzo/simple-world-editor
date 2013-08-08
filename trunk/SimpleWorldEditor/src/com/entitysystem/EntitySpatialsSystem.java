/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

import com.entitysystem.ComponentsControl;
import com.jme3.scene.Spatial;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */


public final class EntitySpatialsSystem {
    
    private static ConcurrentHashMap <Long, EntitySpatialsControl> spatialControl;    

    public EntitySpatialsSystem() {
         spatialControl = new ConcurrentHashMap <Long, EntitySpatialsControl>();
    }
    
    public static EntitySpatialsControl addSpatialControl(Spatial sp, long ID, ComponentsControl control) {
         EntitySpatialsControl spControl = new EntitySpatialsControl(sp, ID, control);
         spatialControl.put(ID, spControl);
         return spControl;
    }    
    
    public static EntitySpatialsControl getSpatialControl(long ID) {
        return spatialControl.get(ID);
    }    

    public static void removeSpatialControl(long ID) {
        spatialControl.get(ID).destroy();
        spatialControl.remove(ID);
    }    
    
}
