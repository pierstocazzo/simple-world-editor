/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.entitysystem;

/**
 *
 * @author mifth
 */
public class EntityNameComponent {
    
    private String name;    
    
    public EntityNameComponent(String name) {
        this.name = name;
    }
    
    public void setName(String nameEnt) {
        name = nameEnt;
    }
    
    public String getName() {
        return name;
    }
    
    
}
