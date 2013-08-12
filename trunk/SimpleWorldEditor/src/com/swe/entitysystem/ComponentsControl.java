/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.entitysystem;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class ComponentsControl {
    
    private ConcurrentHashMap <Class<?>, Object> components = new ConcurrentHashMap <Class<?>, Object>();        
    private long ID;
//    private boolean isDoUpdate = true;
    private boolean updateOnce = false;
    private UpdateType updatetype = UpdateType.staticEntity;
    
    public static enum UpdateType {
        staticEntity,
        dynamicServerEntity,
        dynamicClientEntity
       
    } 
    
    public void setUpdateType(UpdateType type) {
        updatetype = type;
    }
    
    public UpdateType getUpdateType() {
        return updatetype;
    }
    
    public ComponentsControl(long ID) {
        this.ID = ID;
    }
    
    public long getEntityID(ComponentsControl compControl) {
        return ID;
    }
    
//    public boolean isDoUpdateAlways() {
//        return isDoUpdate;
//    }

    public boolean isDoUpdateOnce() {
        return updateOnce;
    }
    
    public void updateOnce(boolean bool, UpdateType type) {
        updatetype = type;
         updateOnce = bool;
    }    
    
//    public void updateAlways(boolean bool) {
//        isDoUpdate = bool;
//    }    
    
    public Object getComponent(Class controlType) {
        return components.get(controlType);
    }

    public void setComponent(Object comp) {
        if (components.get(comp.getClass()) == null) components.put(comp.getClass(), comp);
    }
    
    public void clearComponent(Class componentType){
        components.remove(componentType);
    }
    
    public void clearComponents() {
        components.clear();
    }    
    
    
}
