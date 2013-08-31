package com.swe.entitysystem;

import java.util.concurrent.ConcurrentHashMap;

public final class EntityManager {

    private static long idx;

    public EntityManager() {
        idx = 0;
    }
    private static ConcurrentHashMap<Long, ComponentsControl> componentControl = new ConcurrentHashMap<Long, ComponentsControl>();

    public static long createEntity() {
        idx++;
//        addComponentControl(idx);
        return idx;
    }

    public static ComponentsControl addComponentControl(long ID) {
        ComponentsControl component = componentControl.get(ID);
        if (component == null) {
            component = new ComponentsControl(ID);
            componentControl.put(ID, component);
        }
        return component;
    }

    public static ComponentsControl getComponentControl(long ID) {
        return componentControl.get(ID);
    }

    public static boolean containsID(long ID) {
        return componentControl.containsKey(ID);
    }

    public static Object getComponent(long ID, Class getClass) {
        return componentControl.get(ID).getComponent(getClass);
    }

    private static void removeComponentControl(long ID) {
        componentControl.get(ID).clearComponents();
        componentControl.remove(ID);
    }

    public static long getIdx() {
        return idx;
    }

    public static void setIdx(long idx) {
        EntityManager.idx = idx;
    }

    public static ConcurrentHashMap<Long, ComponentsControl> getAllEntities() {
        return componentControl;
    }

    public static void removeEntity(long ID) {
        // remove entity
        removeComponentControl(ID);
    }
}
