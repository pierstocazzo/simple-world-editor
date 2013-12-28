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

    private boolean shiftBool, ctrlBool, altBool, action = false;

    public void setAction(boolean newAction) {
        action = newAction;
    }

    public boolean isActive() {
        return action;
    }

    public boolean isShiftBool() {
        return shiftBool;
    }

    public void setShiftBool(boolean shiftBool) {
        this.shiftBool = shiftBool;
    }

    public boolean isCtrlBool() {
        return ctrlBool;
    }

    public void setCtrlBool(boolean ctrlBool) {
        this.ctrlBool = ctrlBool;
    }

    public boolean isAltBool() {
        return altBool;
    }

    public void setAltBool(boolean altBool) {
        this.altBool = altBool;
    }
    
}
