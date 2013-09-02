package com.swe;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;



public class SimpleEditor extends SimpleApplication{

    public static void main(String[] args) {
        SimpleEditor app = new SimpleEditor();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
//        aps.setFrameRate(80);
//        aps.setResolution(1600, 800);
        app.setSettings(aps);
        app.setShowSettings(false);
        app.start();
    }

              
    

    @Override
    public void simpleInitApp() {

//        this.setShowSettings(false);
        this.setDisplayStatView(false);
        EditorBaseManager baseParts = new EditorBaseManager(this);        
        
    }

    

    
    @Override
    public void simpleUpdate(float tpf) {
       
        
    }    
    
    
    


}
