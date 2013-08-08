/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

/**
 *
 * @author mifth
 */
public class EntityModelPathComponent {
    
    private String modelPath;    
    
    public EntityModelPathComponent(String modelPath) {
        this.modelPath = modelPath;
    }
    
    public void setModelPath(String nameEnt) {
        modelPath = nameEnt;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    
}
