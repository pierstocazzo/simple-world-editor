/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.entitysystem;

/**
 *
 * @author mifth
 */
public class UpdateStateComponent {
    
    private boolean update;
    private int timer;
    
    public UpdateStateComponent(boolean update, int timer) {
        this.update = update;
        this.timer = timer;
    }
    
    public void setUpdate(boolean boo) {
        update = boo;
    }
    
    public boolean getUpdate() {
        return update;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }
    
    public int getTimer() {
        return timer;
    }    
    
}
