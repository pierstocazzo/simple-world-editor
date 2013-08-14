/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.events;

/**
 *
 * @author mifth
 */
public class EditorEventManager {
    
    private static boolean action = false;
    
    public void setAction(boolean newAction) {
        action = newAction;
    }
    
    public boolean isActive() {
        return action;
    }
    
}
